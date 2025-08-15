package com.openfinance.usecase.account.port;

import java.time.LocalDateTime;

/**
 * Port para controle de rate limiting.
 * Interface que define o contrato para operações de controle de tráfego,
 * seguindo os princípios da arquitetura hexagonal.
 *
 * Esta interface será implementada pelo adapter de infraestrutura
 * para integração com sistemas de controle de rate limiting.
 */
public interface IRateLimitPort {

    /**
     * Conta requisições em uma janela de tempo específica
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint acessado
     * @param startTime Início da janela de tempo
     * @param endTime Fim da janela de tempo
     * @return Número de requisições na janela de tempo
     */
    int countRequestsInTimeWindow(String organizationId, String endpoint,
                                  LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Registra uma nova requisição no sistema de rate limiting
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint acessado
     * @param timestamp Momento da requisição
     */
    void recordRequest(String organizationId, String endpoint, LocalDateTime timestamp);

    /**
     * Reseta todos os contadores para uma organização
     * Usado para fins administrativos ou de manutenção
     *
     * @param organizationId Identificador da organização
     */
    void resetCounters(String organizationId);

    /**
     * Reseta contadores específicos de um endpoint para uma organização
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint específico
     */
    void resetEndpointCounters(String organizationId, String endpoint);

    /**
     * Obtém estatísticas detalhadas de rate limiting para uma organização
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint específico
     * @return Estatísticas de uso atual
     */
    RateLimitStats getRateLimitStats(String organizationId, String endpoint);

    /**
     * Verifica se uma organização está próxima do limite (>80%)
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint específico
     * @return true se próximo do limite, false caso contrário
     */
    boolean isNearLimit(String organizationId, String endpoint);

    /**
     * Obtém o tempo até o reset da janela de rate limiting
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint específico
     * @return Segundos até o reset da janela
     */
    long getSecondsUntilReset(String organizationId, String endpoint);

    /**
     * Incrementa contador de violações de rate limit
     *
     * @param organizationId Identificador da organização
     * @param endpoint Endpoint que excedeu o limite
     */
    void recordRateLimitViolation(String organizationId, String endpoint);

    /**
     * Record com estatísticas detalhadas de rate limiting
     */
    record RateLimitStats(
            String organizationId,
            String endpoint,
            int currentTPM,           // Transações por minuto atual
            int currentTPS,           // Transações por segundo atual
            int maxTPM,               // Limite máximo TPM
            int maxTPS,               // Limite máximo TPS
            LocalDateTime windowStart, // Início da janela atual
            LocalDateTime lastRequest, // Última requisição registrada
            int remainingTPM,         // Requisições restantes no minuto
            int remainingTPS,         // Requisições restantes no segundo
            long violationCount,      // Número total de violações
            double utilizationPercentage // Porcentagem de utilização do limite
    ) {

        /**
         * Verifica se está dentro dos limites de TPM
         *
         * @return true se dentro do limite, false caso contrário
         */
        public boolean isWithinTPMLimit() {
            return currentTPM < maxTPM;
        }

        /**
         * Verifica se está dentro dos limites de TPS
         *
         * @return true se dentro do limite, false caso contrário
         */
        public boolean isWithinTPSLimit() {
            return currentTPS < maxTPS;
        }

        /**
         * Verifica se está dentro de todos os limites
         *
         * @return true se dentro de todos os limites, false caso contrário
         */
        public boolean isWithinLimits() {
            return isWithinTPMLimit() && isWithinTPSLimit();
        }

        /**
         * Verifica se está próximo do limite (>80% de utilização)
         *
         * @return true se próximo do limite, false caso contrário
         */
        public boolean isNearLimit() {
            return utilizationPercentage > 80.0;
        }

        /**
         * Retorna o menor número de requisições restantes entre TPM e TPS
         *
         * @return Requisições restantes até atingir algum limite
         */
        public int getMinRemainingRequests() {
            return Math.min(remainingTPM, remainingTPS);
        }
    }
}
