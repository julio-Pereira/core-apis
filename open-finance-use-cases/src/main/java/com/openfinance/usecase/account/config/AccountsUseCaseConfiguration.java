package com.openfinance.usecase.account.config;

import com.openfinance.usecase.IEventPublisher;
import com.openfinance.usecase.IUseCase;
import com.openfinance.usecase.account.mapper.IAccountUseCaseMapper;
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
     * @param accountMapper Mapper para conversão entre entidades e DTOs
     * @return Instância configurada do caso de uso
     */
    @Bean
    public IUseCase<GetAccountsInput, GetAccountsOutput> getAccountsUseCase(
            ConsentValidationService consentValidationService,
            RateLimitValidationService rateLimitValidationService,
            AccountValidationService accountValidationService,
            IAccountPort accountPort,
            PaginationService paginationService,
            IEventPublisher eventPublisher,
            IAccountUseCaseMapper accountMapper) {

        return new GetAccountsUseCase(
                consentValidationService,
                rateLimitValidationService,
                accountValidationService,
                accountPort,
                paginationService,
                eventPublisher,
                accountMapper
        );
    }

    /**
     * Configura o facade para o caso de uso de obtenção de contas
     *
     * @param getAccountsUseCase Caso de uso configurado
     * @return Instância do facade
     */
    @Bean
    public GetAccountsFacade getAccountsFacade(
            IUseCase<GetAccountsInput, GetAccountsOutput> getAccountsUseCase) {
        return new GetAccountsFacade(getAccountsUseCase);
    }

    /**
     * Configura o serviço de validação de contas
     *
     * @return Instância do serviço de validação
     */
    @Bean
    public AccountValidationService accountValidationService() {
        return new AccountValidationService();
    }

    /**
     * Configura o serviço de validação de consentimentos
     *
     * @param consentPort Port para acesso a dados de consentimento
     * @return Instância do serviço de validação
     */
    @Bean
    public ConsentValidationService consentValidationService(IConsentPort consentPort) {
        return new ConsentValidationService(consentPort);
    }

    /**
     * Configura o serviço de validação de rate limiting
     *
     * @param rateLimitPort Port para controle de rate limiting
     * @return Instância do serviço de validação
     */
    @Bean
    public RateLimitValidationService rateLimitValidationService(IRateLimitPort rateLimitPort) {
        return new RateLimitValidationService(rateLimitPort);
    }

    /**
     * Configura o serviço de paginação
     *
     * @return Instância do serviço de paginação
     */
    @Bean
    public PaginationService paginationService() {
        return new PaginationService();
    }

    /**
     * Configura o serviço de filtros de permissão
     *
     * @return Instância do serviço de filtros
     */
    @Bean
    public PermissionFilterService permissionFilterService() {
        return new PermissionFilterService();
    }
}