package com.openfinance.usecase.account.service;

import com.openfinance.usecase.account.port.IRateLimitPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Serviço responsável por controlar e validar limites de tráfego (rate limiting)
 * conforme especificações do Open Finance Brasil.
 *
 * Implementa controles de:
 * - TPM (Transações Por Minuto)
 * - TPS (Transações Por Segundo)
 * - Limites por organização
 * - Limites por endpoint
 * - Janelas deslizantes de tempo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitValidationService {

    private final IRateLimitPort rateLimitPort;

    @Value("${open-finance.rate-limit.default-tpm:300}")
    private int defaultTransactionsPerMinute;

    @Value("${open-finance.rate-limit.default-tps:10}")
    private int defaultTransactionsPerSecond;

    @Value("${open-finance.rate-limit.accounts.tpm:300}")
    private int accountsTransactionsPerMinute;

    @Value("${open-finance.rate-limit.accounts.tps:10}")
    private int accountsTransactionsPerSecond;

    /**
     * Verifica se a organização está dentro dos limites de tráfego para o endpoint
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint sendo acessado
     * @return true se está dentro dos limites, false caso contrário
     */
    public boolean isWithinLimits(String organizationId, String endpoint) {
        log.debug("Checking rate limits for organization {} and endpoint {}",
                organizationId, endpoint);

        try {
            boolean withinTPM = isWithinTPMLimit(organizationId, endpoint);
            boolean withinTPS = isWithinTPSLimit(organizationId, endpoint);

            boolean withinLimits = withinTPM && withinTPS;

            if (!withinLimits) {
                log.warn("Rate limit exceeded for organization {} on endpoint {}. " +
                                "TPM within limits: {}, TPS within limits: {}",
                        organizationId, endpoint, withinTPM, withinTPS);
            } else {
                log.debug("Organization {} is within rate limits for endpoint {}",
                        organizationId, endpoint);
            }

            return withinLimits;

        } catch (Exception e) {
            log.error("Error checking rate limits for organization {} and endpoint {}: {}",
                    organizationId, endpoint, e.getMessage(), e);
            // Em caso de erro, permitir a requisição (fail-open approach)
            return true;
        }
    }

    /**
     * Verifica limite de Transações Por Minuto (TPM)
     */
    private boolean isWithinTPMLimit(String organizationId, String endpoint) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);

        int currentTPM = rateLimitPort.countRequestsInTimeWindow(
                organizationId,
                endpoint,
                oneMinuteAgo,
                LocalDateTime.now()
        );

        int tpmLimit = getTPMLimit(endpoint);

        log.debug("TPM check for organization {} on endpoint {}: {}/{}",
                organizationId, endpoint, currentTPM, tpmLimit);

        return currentTPM < tpmLimit;
    }

    /**
     * Verifica limite de Transações Por Segundo (TPS)
     */
    private boolean isWithinTPSLimit(String organizationId, String endpoint) {
        LocalDateTime oneSecondAgo = LocalDateTime.now().minus(1, ChronoUnit.SECONDS);

        int currentTPS = rateLimitPort.countRequestsInTimeWindow(
                organizationId,
                endpoint,
                oneSecondAgo,
                LocalDateTime.now()
        );

        int tpsLimit = getTPSLimit(endpoint);

        log.debug("TPS check for organization {} on endpoint {}: {}/{}",
                organizationId, endpoint, currentTPS, tpsLimit);

        return currentTPS < tpsLimit;
    }

    /**
     * Registra uma nova requisição para controle de rate limiting
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint acessado
     */
    public void recordRequest(String organizationId, String endpoint) {
        log.debug("Recording request for organization {} on endpoint {}",
                organizationId, endpoint);

        try {
            rateLimitPort.recordRequest(organizationId, endpoint, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error recording request for rate limiting: {}", e.getMessage(), e);
            // Não falhar a operação por erro de logging
        }
    }

    /**
     * Obtém o número de requisições restantes na janela atual
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint sendo consultado
     * @return Número de requisições restantes no minuto atual
     */
    public int getRemainingRequests(String organizationId, String endpoint) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);

        int currentTPM = rateLimitPort.countRequestsInTimeWindow(
                organizationId,
                endpoint,
                oneMinuteAgo,
                LocalDateTime.now()
        );

        int tpmLimit = getTPMLimit(endpoint);
        return Math.max(0, tpmLimit - currentTPM);
    }

    /**
     * Verifica se a organização está próxima do limite (>80% do limite)
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint sendo consultado
     * @return true se está próximo do limite, false caso contrário
     */
    public boolean isNearLimit(String organizationId, String endpoint) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);

        int currentTPM = rateLimitPort.countRequestsInTimeWindow(
                organizationId,
                endpoint,
                oneMinuteAgo,
                LocalDateTime.now()
        );

        int tpmLimit = getTPMLimit(endpoint);
        double utilizationPercentage = (double) currentTPM / tpmLimit;

        return utilizationPercentage > 0.8; // 80% do limite
    }

    /**
     * Obtém o limite de TPM para um endpoint específico
     */
    private int getTPMLimit(String endpoint) {
        return switch (endpoint) {
            case "/accounts" -> accountsTransactionsPerMinute;
            case "/accounts/{accountId}" -> accountsTransactionsPerMinute;
            case "/accounts/{accountId}/balances" -> accountsTransactionsPerMinute;
            case "/accounts/{accountId}/transactions" -> accountsTransactionsPerMinute;
            default -> defaultTransactionsPerMinute;
        };
    }

    /**
     * Obtém o limite de TPS para um endpoint específico
     */
    private int getTPSLimit(String endpoint) {
        return switch (endpoint) {
            case "/accounts" -> accountsTransactionsPerSecond;
            case "/accounts/{accountId}" -> accountsTransactionsPerSecond;
            case "/accounts/{accountId}/balances" -> accountsTransactionsPerSecond;
            case "/accounts/{accountId}/transactions" -> accountsTransactionsPerSecond;
            default -> defaultTransactionsPerSecond;
        };
    }

    /**
     * Reseta os contadores de rate limit para uma organização (uso administrativo)
     *
     * @param organizationId Identificador da organização
     */
    public void resetRateLimitCounters(String organizationId) {
        log.info("Resetting rate limit counters for organization: {}", organizationId);

        try {
            rateLimitPort.resetCounters(organizationId);
        } catch (Exception e) {
            log.error("Error resetting rate limit counters for organization {}: {}",
                    organizationId, e.getMessage(), e);
        }
    }
}