package com.openfinance.usecase.account.config;

import com.openfinance.usecase.IEventPublisher;
import com.openfinance.usecase.IUseCase;
import com.openfinance.usecase.account.port.IAccountPort;
import com.openfinance.usecase.account.port.IConsentPort;
import com.openfinance.usecase.account.port.IRateLimitPort;
import com.openfinance.usecase.account.retrieve.list.GetAccountsFacade;
import com.openfinance.usecase.account.retrieve.list.GetAccountsInput;
import com.openfinance.usecase.account.retrieve.list.GetAccountsOutput;
import com.openfinance.usecase.account.retrieve.list.GetAccountsUseCase;
import com.openfinance.usecase.account.service.AccountValidationService;
import com.openfinance.usecase.account.service.ConsentValidationService;
import com.openfinance.usecase.account.service.PermissionFilterService;
import com.openfinance.usecase.account.service.RateLimitValidationService;
import com.openfinance.usecase.pagination.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuração Spring para os casos de uso de Accounts.
 * Organiza e configura todos os beans necessários para o funcionamento
 * dos casos de uso relacionados a contas, seguindo os princípios da
 * arquitetura hexagonal e injeção de dependência.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AccountsUseCaseProperties.class)
@RequiredArgsConstructor
public class AccountsUseCaseConfiguration {

    private final AccountsUseCaseProperties properties;

    /**
     * Configura o caso de uso principal de obtenção de contas
     *
     * @param consentValidationService Serviço de validação de consentimentos
     * @param rateLimitValidationService Serviço de validação de rate limiting
     * @param accountValidationService Serviço de validação de contas
     * @param accountPort Port para acesso a dados de contas
     * @param paginationService Serviço de paginação
     * @param eventPublisher Publisher de eventos de domínio
     * @return Instância configurada do caso de uso
     */
    @Bean
    public IUseCase<GetAccountsInput, GetAccountsOutput> getAccountsUseCase(
            ConsentValidationService consentValidationService,
            RateLimitValidationService rateLimitValidationService,
            AccountValidationService accountValidationService,
            IAccountPort accountPort,
            PaginationService paginationService,
            IEventPublisher eventPublisher) {

        return new GetAccountsUseCase(
                consentValidationService,
                rateLimitValidationService,
                accountValidationService,
                accountPort,
                paginationService,
                eventPublisher
        );
    }

    /**
     * Configura a facade para o caso de uso de obtenção de contas
     *
     * @param useCase Caso de uso de obtenção de contas
     * @return Instância configurada da facade
     */
    @Bean
    public GetAccountsFacade getAccountsFacade(
            IUseCase<GetAccountsInput, GetAccountsOutput> useCase) {

        return new GetAccountsFacade(useCase);
    }

    /**
     * Configura o serviço de validação de consentimentos
     *
     * @param consentPort Port para acesso a dados de consentimento
     * @return Instância configurada do serviço
     */
    @Bean
    public ConsentValidationService consentValidationService(IConsentPort consentPort, PermissionFilterService permissionFilterService) {
        return new ConsentValidationService(consentPort, permissionFilterService);
    }

    /**
     * Configura o serviço de validação de rate limiting
     *
     * @param rateLimitPort Port para controle de rate limiting
     * @return Instância configurada do serviço
     */
    @Bean
    public RateLimitValidationService rateLimitValidationService(IRateLimitPort rateLimitPort) {
        return new RateLimitValidationService(rateLimitPort);
    }

    /**
     * Configura o serviço de validação de contas
     *
     * @return Instância configurada do serviço
     */
    @Bean
    public AccountValidationService accountValidationService() {
        return new AccountValidationService();
    }

    /**
     * Configura o serviço de paginação
     *
     * @return Instância configurada do serviço
     */
    @Bean
    public PaginationService paginationService() {
        return new PaginationService();
    }
}

/**
 * Propriedades de configuração para os casos de uso de Accounts.
 *
 * Centraliza todas as configurações específicas dos casos de uso,
 * permitindo externalização via application.yml/properties.
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "open-finance.accounts.usecase")
record AccountsUseCaseProperties(

        /**
         * Configurações de rate limiting
         */
        RateLimit rateLimit,

        /**
         * Configurações de paginação
         */
        Pagination pagination,

        /**
         * Configurações de SLA
         */
        Sla sla,

        /**
         * Configurações de auditoria
         */
        Audit audit
) {

    /**
     * Configurações específicas de rate limiting
     */
    record RateLimit(
            int defaultTpm,           // TPM padrão
            int defaultTps,           // TPS padrão
            int accountsTpm,          // TPM específico para /accounts
            int accountsTps,          // TPS específico para /accounts
            boolean failOpen,         // Permitir em caso de erro
            int cacheTimeoutSeconds   // Timeout do cache de rate limiting
    ) {}

    /**
     * Configurações específicas de paginação
     */
    record Pagination(
            int defaultPageSize,      // Tamanho padrão de página
            int maxPageSize,          // Tamanho máximo de página
            int keyTtlMinutes,        // TTL das chaves de paginação
            String baseUrl            // URL base para links HATEOAS
    ) {}

    /**
     * Configurações específicas de SLA
     */
    record Sla(
            long highFrequencyThresholdMs,    // Limite para alta frequência
            long mediumFrequencyThresholdMs,  // Limite para média frequência
            long lowFrequencyThresholdMs,     // Limite para baixa frequência
            boolean enableMonitoring,         // Habilitar monitoramento
            boolean enableAlerts              // Habilitar alertas
    ) {}

    /**
     * Configurações específicas de auditoria
     */
    record Audit(
            boolean enableDetailedLogging,    // Logging detalhado
            boolean logParameters,            // Log de parâmetros
            boolean logReturnValues,          // Log de retornos
            boolean enablePerformanceLogging, // Log de performance
            long warningThresholdMs           // Limite para warnings
    ) {}
}

/**
 * Configuração padrão para desenvolvimento/teste
 */
class DefaultAccountsUseCaseConfiguration {

    public static AccountsUseCaseProperties defaultProperties() {
        return new AccountsUseCaseProperties(
                new AccountsUseCaseProperties.RateLimit(
                        300,  // defaultTpm
                        10,   // defaultTps
                        300,  // accountsTpm
                        10,   // accountsTps
                        true, // failOpen
                        60    // cacheTimeoutSeconds
                ),
                new AccountsUseCaseProperties.Pagination(
                        25,   // defaultPageSize
                        1000, // maxPageSize
                        60,   // keyTtlMinutes
                        "https://api.banco.com.br/open-banking" // baseUrl
                ),
                new AccountsUseCaseProperties.Sla(
                        1500L, // highFrequencyThresholdMs
                        2000L, // mediumFrequencyThresholdMs
                        4000L, // lowFrequencyThresholdMs
                        true,  // enableMonitoring
                        true   // enableAlerts
                ),
                new AccountsUseCaseProperties.Audit(
                        true,  // enableDetailedLogging
                        false, // logParameters (evitar dados sensíveis)
                        true,  // logReturnValues
                        true,  // enablePerformanceLogging
                        1000L  // warningThresholdMs
                )
        );
    }
}