package com.openfinance.usecase.utils.aspect;

import com.openfinance.usecase.account.retrieve.list.GetAccountsInput;
import com.openfinance.usecase.account.retrieve.list.GetAccountsOutput;
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
 * Aspect para coleta automática de métricas usando Micrometer.
 *
 * Coleta métricas de performance, uso e compliance para monitoramento
 * e observabilidade da aplicação, seguindo as melhores práticas
 * do Open Finance Brasil.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MicrometerMetricsAspect {

    private final MeterRegistry meterRegistry;

    /**
     * Coleta métricas para operações de GetAccountsFacade
     */
    @Around("execution(* com.openfinance.usecase.account.facade.GetAccountsFacade.getAccounts(..))")
    public Object collectAccountsMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);

        GetAccountsInput input = (GetAccountsInput) joinPoint.getArgs()[0];
        String organizationId = input.organizationId();

        // Contador de requisições totais
        Counter.builder("open_finance.accounts.requests.total")
                .tag("organization_id", organizationId)
                .tag("endpoint", "/accounts")
                .register(meterRegistry)
                .increment();

        try {
            Object result = joinPoint.proceed();
            GetAccountsOutput output = (GetAccountsOutput) result;

            // Métricas de sucesso
            recordSuccessMetrics(organizationId, output, sample);

            return result;

        } catch (Exception e) {
            // Métricas de erro
            recordErrorMetrics(organizationId, e, sample);
            throw e;
        }
    }

    /**
     * Registra métricas de operações bem-sucedidas
     */
    private void recordSuccessMetrics(String organizationId, GetAccountsOutput output, Timer.Sample sample) {
        // Timer de resposta
        sample.stop(Timer.builder("open_finance.accounts.response_time")
                .tag("organization_id", organizationId)
                .tag("endpoint", "/accounts")
                .tag("status", "success")
                .register(meterRegistry));

        // Contador de sucessos
        Counter.builder("open_finance.accounts.requests.success")
                .tag("organization_id", organizationId)
                .tag("endpoint", "/accounts")
                .register(meterRegistry)
                .increment();

        // Gauge do número de contas retornadas
        meterRegistry.gauge("open_finance.accounts.returned_count",
                "organization_id", organizationId,
                output.getAccountsCount());

        // Histograma de distribuição de contas por página
        meterRegistry.summary("open_finance.accounts.page_distribution")
                .record(output.getAccountsCount());
    }

    /**
     * Registra métricas de operações com erro
     */
    private void recordErrorMetrics(String organizationId, Exception e, Timer.Sample sample) {
        String errorType = e.getClass().getSimpleName();

        // Timer de resposta com erro
        sample.stop(Timer.builder("open_finance.accounts.response_time")
                .tag("organization_id", organizationId)
                .tag("endpoint", "/accounts")
                .tag("status", "error")
                .tag("error_type", errorType)
                .register(meterRegistry));

        // Contador de erros
        Counter.builder("open_finance.accounts.requests.error")
                .tag("organization_id", organizationId)
                .tag("endpoint", "/accounts")
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Coleta métricas de rate limiting
     */
    @Around("execution(* com.openfinance.usecase.account.service.RateLimitValidationService.isWithinLimits(..))")
    public Object collectRateLimitMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String organizationId = (String) joinPoint.getArgs()[0];
        String endpoint = (String) joinPoint.getArgs()[1];

        Object result = joinPoint.proceed();
        boolean withinLimits = (Boolean) result;

        // Contador de verificações de rate limit
        Counter.builder("open_finance.rate_limit.checks")
                .tag("organization_id", organizationId)
                .tag("endpoint", endpoint)
                .tag("within_limits", String.valueOf(withinLimits))
                .register(meterRegistry)
                .increment();

        // Contador específico para violações
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
     * Coleta métricas de validação de consentimento
     */
    @Around("execution(* com.openfinance.usecase.account.service.ConsentValidationService.isValidConsent(..))")
    public Object collectConsentMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String consentId = (String) joinPoint.getArgs()[0];

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            boolean isValid = (Boolean) result;

            // Timer de validação de consentimento
            sample.stop(Timer.builder("open_finance.consent.validation_time")
                    .tag("consent_valid", String.valueOf(isValid))
                    .register(meterRegistry));

            // Contador de validações
            Counter.builder("open_finance.consent.validations")
                    .tag("consent_valid", String.valueOf(isValid))
                    .register(meterRegistry)
                    .increment();

            return result;

        } catch (Exception e) {
            sample.stop(Timer.builder("open_finance.consent.validation_time")
                    .tag("consent_valid", "error")
                    .tag("error_type", e.getClass().getSimpleName())
                    .register(meterRegistry));

            Counter.builder("open_finance.consent.validation_errors")
                    .tag("error_type", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();

            throw e;
        }
    }

    /**
     * Coleta métricas de SLA compliance
     */
    public void recordSLACompliance(String endpoint, long responseTimeMs,
                                    boolean withinSLA, String organizationId) {

        // Timer para SLA compliance
        Timer.builder("open_finance.sla.response_time")
                .tag("endpoint", endpoint)
                .tag("organization_id", organizationId)
                .tag("within_sla", String.valueOf(withinSLA))
                .register(meterRegistry)
                .record(responseTimeMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Contador de compliance
        Counter.builder("open_finance.sla.compliance")
                .tag("endpoint", endpoint)
                .tag("organization_id", organizationId)
                .tag("within_sla", String.valueOf(withinSLA))
                .register(meterRegistry)
                .increment();

        // Gauge para última medição de SLA
        meterRegistry.gauge("open_finance.sla.last_response_time",
                "endpoint", endpoint,
                "organization_id", organizationId,
                responseTimeMs);
    }

    /**
     * Coleta métricas de uso de paginação
     */
    @Around("execution(* com.openfinance.usecase.pagination.PaginationService.createPaginationInfo(..))")
    public Object collectPaginationMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof GetAccountsInput) {
            GetAccountsInput input = (GetAccountsInput) joinPoint.getArgs()[0];

            // Métricas de paginação
            meterRegistry.summary("open_finance.pagination.page_size")
                    .record(input.pageSize());

            meterRegistry.summary("open_finance.pagination.page_number")
                    .record(input.page());

            // Contador de uso de chave de paginação
            Counter.builder("open_finance.pagination.key_usage")
                    .tag("has_key", String.valueOf(input.hasPaginationKey()))
                    .register(meterRegistry)
                    .increment();
        }

        return result;
    }

    /**
     * Extrai organization ID dos argumentos do método
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