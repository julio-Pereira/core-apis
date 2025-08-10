package com.openfinance.usecase.utils.aspect;

import com.openfinance.usecase.utils.CollectMetrics;
import com.openfinance.usecase.account.input.GetAccountsInput;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatic metrics collection using Micrometer
 * Integrates with monitoring systems like Prometheus, DataDog, etc.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MicrometerMetricsAspect {

    private final MeterRegistry meterRegistry;

    /**
     * Around advice for methods annotated with @CollectMetrics
     */
    @Around("@annotation(collectMetrics)")
    public Object collectMetrics(ProceedingJoinPoint joinPoint, CollectMetrics collectMetrics) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String metricPrefix = collectMetrics.metricPrefix().isEmpty() ?
                "open_finance.accounts." + methodName : collectMetrics.metricPrefix();

        // Extract organization ID for tagging
        String organizationId = extractOrganizationId(joinPoint.getArgs());

        Timer.Sample sample = null;
        if (collectMetrics.measureTime()) {
            sample = Timer.start(meterRegistry);
        }

        try {
            Object result = joinPoint.proceed();

            // Count successful executions
            if (collectMetrics.countSuccess()) {
                Counter.builder(metricPrefix + ".success")
                        .tag("operation", methodName)
                        .tag("organization_id", organizationId)
                        .tags(collectMetrics.tags())
                        .register(meterRegistry)
                        .increment();
            }

            // Record execution time
            if (sample != null) {
                sample.stop(Timer.builder(metricPrefix + ".duration")
                        .tag("operation", methodName)
                        .tag("organization_id", organizationId)
                        .tag("status", "success")
                        .tags(collectMetrics.tags())
                        .register(meterRegistry));
            }

            return result;

        } catch (Exception e) {
            // Count failed executions
            if (collectMetrics.countErrors()) {
                Counter.builder(metricPrefix + ".error")
                        .tag("operation", methodName)
                        .tag("organization_id", organizationId)
                        .tag("error_type", e.getClass().getSimpleName())
                        .tags(collectMetrics.tags())
                        .register(meterRegistry)
                        .increment();
            }

            // Record execution time for failures
            if (sample != null) {
                sample.stop(Timer.builder(metricPrefix + ".duration")
                        .tag("operation", methodName)
                        .tag("organization_id", organizationId)
                        .tag("status", "error")
                        .tag("error_type", e.getClass().getSimpleName())
                        .tags(collectMetrics.tags())
                        .register(meterRegistry));
            }

            throw e;
        }
    }

    /**
     * Automatic metrics for Open Finance compliance
     */
    @Around("execution(* com.openfinance.usecase.account.facade.GetAccountsFacade.*(..))")
    public Object collectOpenFinanceMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String organizationId = extractOrganizationId(joinPoint.getArgs());

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            // Success metrics
            sample.stop(Timer.builder("open_finance.operation.duration")
                    .tag("endpoint", "/accounts")
                    .tag("operation", methodName)
                    .tag("organization_id", organizationId)
                    .tag("status", "success")
                    .register(meterRegistry));

            Counter.builder("open_finance.operation.total")
                    .tag("endpoint", "/accounts")
                    .tag("operation", methodName)
                    .tag("organization_id", organizationId)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .increment();

            return result;

        } catch (Exception e) {
            // Error metrics
            sample.stop(Timer.builder("open_finance.operation.duration")
                    .tag("endpoint", "/accounts")
                    .tag("operation", methodName)
                    .tag("organization_id", organizationId)
                    .tag("status", "error")
                    .tag("error_type", e.getClass().getSimpleName())
                    .register(meterRegistry));

            Counter.builder("open_finance.operation.total")
                    .tag("endpoint", "/accounts")
                    .tag("operation", methodName)
                    .tag("organization_id", organizationId)
                    .tag("status", "error")
                    .tag("error_type", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();

            throw e;
        }
    }

    /**
     * Rate limiting metrics
     */
    @Around("execution(* com.openfinance.core.port.IRateLimitService.isWithinRateLimit(..))")
    public Object collectRateLimitMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String organizationId = (String) joinPoint.getArgs()[0];
        String endpoint = (String) joinPoint.getArgs()[1];

        Object result = joinPoint.proceed();
        boolean withinLimits = (Boolean) result;

        Counter.builder("open_finance.rate_limit.check")
                .tag("organization_id", organizationId)
                .tag("endpoint", endpoint)
                .tag("within_limits", String.valueOf(withinLimits))
                .register(meterRegistry)
                .increment();

        if (!withinLimits) {
            Counter.builder("open_finance.rate_limit.exceeded")
                    .tag("organization_id", organizationId)
                    .tag("endpoint", endpoint)
                    .register(meterRegistry)
                    .increment();
        }

        return result;
    }

    /**
     * SLA compliance metrics
     */
    public void recordSLACompliance(String endpoint, long responseTimeMs, boolean withinSLA, String organizationId) {
        Timer.builder("open_finance.sla.response_time")
                .tag("endpoint", endpoint)
                .tag("organization_id", organizationId)
                .tag("within_sla", String.valueOf(withinSLA))
                .register(meterRegistry)
                .record(responseTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        Counter.builder("open_finance.sla.compliance")
                .tag("endpoint", endpoint)
                .tag("organization_id", organizationId)
                .tag("within_sla", String.valueOf(withinSLA))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Extracts organization ID from method arguments
     */
    private String extractOrganizationId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof GetAccountsInput) {
                return ((GetAccountsInput) arg).organizationId();
            }
        }
        return "unknown";
    }
}