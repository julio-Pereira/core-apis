package com.openfinance.usecase.account.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Propriedades de configuração para os casos de uso de Accounts.
 *
 * Esta classe centraliza todas as configurações relacionadas aos casos de uso
 * de contas, incluindo URLs da API, configurações de paginação, timeouts,
 * e outras propriedades específicas do domínio de accounts.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "open-finance.use-cases.accounts")
public class AccountsUseCaseProperties {

    /**
     * Configurações da API
     */
    @Valid
    @NotNull
    private Api api = new Api();

    /**
     * Configurações de paginação
     */
    @Valid
    @NotNull
    private Pagination pagination = new Pagination();

    /**
     * Configurações de validação
     */
    @Valid
    @NotNull
    private Validation validation = new Validation();

    /**
     * Configurações de rate limiting
     */
    @Valid
    @NotNull
    private RateLimit rateLimit = new RateLimit();

    /**
     * Configurações de auditoria
     */
    @Valid
    @NotNull
    private Audit audit = new Audit();

    /**
     * Configurações da API
     */
    @Data
    public static class Api {

        /**
         * URL base da API para construção de links
         */
        @NotBlank(message = "Base URL cannot be blank")
        private String baseUrl = "https://api.banco.com.br/open-banking";

        /**
         * Timeout para chamadas externas (em milissegundos)
         */
        @Min(value = 1000, message = "Timeout must be at least 1000ms")
        @Max(value = 60000, message = "Timeout must not exceed 60000ms")
        private int timeoutMs = 30000;

        /**
         * Número máximo de tentativas para chamadas externas
         */
        @Min(value = 1, message = "Max retries must be at least 1")
        @Max(value = 5, message = "Max retries must not exceed 5")
        private int maxRetries = 3;
    }

    /**
     * Configurações de paginação
     */
    @Data
    public static class Pagination {

        /**
         * Tamanho padrão da página
         */
        @Min(value = 1, message = "Default page size must be at least 1")
        @Max(value = 1000, message = "Default page size must not exceed 1000")
        private int defaultPageSize = 25;

        /**
         * Tamanho máximo da página
         */
        @Min(value = 1, message = "Max page size must be at least 1")
        @Max(value = 1000, message = "Max page size must not exceed 1000")
        private int maxPageSize = 1000;

        /**
         * Tempo de expiração da chave de paginação (em minutos)
         */
        @Min(value = 1, message = "Key expiration must be at least 1 minute")
        @Max(value = 1440, message = "Key expiration must not exceed 1440 minutes (24h)")
        private int keyExpirationMinutes = 60;

        /**
         * Habilitar chaves de paginação para controle de limites operacionais
         */
        private boolean enablePaginationKeys = true;
    }

    /**
     * Configurações de validação
     */
    @Data
    public static class Validation {

        /**
         * Habilitar validação estrita de consentimentos
         */
        private boolean strictConsentValidation = true;

        /**
         * Habilitar validação de formato de AccountId
         */
        private boolean validateAccountIdFormat = true;

        /**
         * Comprimento mínimo do AccountId
         */
        @Min(value = 1, message = "Min account ID length must be at least 1")
        private int minAccountIdLength = 10;

        /**
         * Comprimento máximo do AccountId
         */
        @Min(value = 1, message = "Max account ID length must be at least 1")
        @Max(value = 100, message = "Max account ID length must not exceed 100")
        private int maxAccountIdLength = 100;
    }

    /**
     * Configurações de rate limiting
     */
    @Data
    public static class RateLimit {

        /**
         * Habilitar verificação de rate limiting
         */
        private boolean enabled = true;

        /**
         * Limite padrão de transações por minuto (TPM)
         */
        @Min(value = 1, message = "Default TPM must be at least 1")
        private int defaultTpm = 300;

        /**
         * Limite padrão de transações por segundo (TPS)
         */
        @Min(value = 1, message = "Default TPS must be at least 1")
        private int defaultTps = 5;

        /**
         * Janela de tempo para cálculo de rate limiting (em segundos)
         */
        @Min(value = 1, message = "Window size must be at least 1 second")
        @Max(value = 3600, message = "Window size must not exceed 3600 seconds")
        private int windowSizeSeconds = 60;
    }

    /**
     * Configurações de auditoria
     */
    @Data
    public static class Audit {

        /**
         * Habilitar logging de auditoria
         */
        private boolean enabled = true;

        /**
         * Habilitar logging de dados sensíveis (com mascaramento)
         */
        private boolean logSensitiveData = false;

        /**
         * Habilitar publicação de eventos de auditoria
         */
        private boolean publishEvents = true;

        /**
         * Incluir stack trace em logs de erro
         */
        private boolean includeStackTrace = false;

        /**
         * Nível de detalhamento dos logs (BASIC, DETAILED, FULL)
         */
        private LogLevel logLevel = LogLevel.DETAILED;

        public enum LogLevel {
            BASIC,    // Apenas informações essenciais
            DETAILED, // Informações detalhadas sem dados sensíveis
            FULL      // Todas as informações (apenas para desenvolvimento)
        }
    }
}
