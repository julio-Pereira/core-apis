package com.openfinance.usecase.account.retrieve.list;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.events.account.AccountAccessedEvent;
import com.openfinance.core.exceptions.BusinessRuleViolationException;
import com.openfinance.core.exceptions.ValidationException;
import com.openfinance.usecase.IEventPublisher;
import com.openfinance.usecase.account.mapper.IAccountUseCaseMapper;
import com.openfinance.usecase.account.port.IAccountPort;
import com.openfinance.usecase.account.service.AccountValidationService;
import com.openfinance.usecase.account.service.ConsentValidationService;
import com.openfinance.usecase.account.service.RateLimitValidationService;
import com.openfinance.usecase.pagination.PaginationInfo;
import com.openfinance.usecase.pagination.PaginationService;
import com.openfinance.usecase.utils.AuditLog;
import com.openfinance.usecase.utils.MonitorPerformance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementação do caso de uso para obtenção da lista de contas do cliente.
 *
 * Segue os princípios da arquitetura hexagonal, delegando responsabilidades
 * específicas para serviços especializados e mantendo o foco na coordenação
 * dos fluxos de negócio.
 *
 * @see IGetAccountsUseCase
 * @see GetAccountsInput
 * @see GetAccountsOutput
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class GetAccountsUseCase implements IGetAccountsUseCase {

    private final ConsentValidationService consentValidationService;
    private final RateLimitValidationService rateLimitValidationService;
    private final AccountValidationService accountValidationService;
    private final IAccountPort accountPort;
    private final PaginationService paginationService;
    private final IEventPublisher eventPublisher;
    private final IAccountUseCaseMapper accountMapper;

    /**
     * {@inheritDoc}
     *
     * Implementa o fluxo completo de obtenção de contas seguindo as especificações
     * do Open Finance Brasil e os princípios de compliance e auditoria.
     */
    @Override
    @AuditLog(
            operationType = "ACCOUNT_RETRIEVAL",
            sensitiveData = true,
            logParameters = false
    )
    @MonitorPerformance(
            operationName = "EXECUTE_GET_ACCOUNTS",
            detailedLogging = true,
            warningThresholdMs = 1200
    )
    public GetAccountsOutput execute(@Valid GetAccountsInput input) {
        log.debug("Starting account retrieval for consentId: {}, organizationId: {}",
                input.consentId(), input.organizationId());

        // 1. Validação de consentimento e permissões
        validateConsent(input);

        // 2. Validação de rate limiting
        validateRateLimit(input);

        // 3. Validação de parâmetros específicos de contas
        validateAccountParameters(input);

        // 4. Busca das contas no sistema externo (retorna entidades de domínio)
        List<Account> accounts = retrieveAccounts(input);

        // 5. Aplicação de filtros e processamento (ainda com entidades de domínio)
        List<Account> filteredAccounts = applyFilters(accounts, input);

        // 6. Conversão das entidades de domínio para DTOs do use case
        List<AccountOutputDto> accountOutputDtos = accountMapper.toAccountOutputDtoList(filteredAccounts);

        // 7. Geração de informações de paginação
        PaginationInfo paginationInfo = generatePaginationInfo(accountOutputDtos, input);

        // 8. Publicação de evento para auditoria (usa as entidades para o evento)
        publishAccountAccessEvent(input, filteredAccounts);

        LocalDateTime requestDateTime = LocalDateTime.now();

        log.debug("Account retrieval completed for consentId: {}, returning {} accounts",
                input.consentId(), accountOutputDtos.size());

        return new GetAccountsOutput(accountOutputDtos, paginationInfo, requestDateTime);
    }

    /**
     * Valida consentimento e permissões para acesso às contas
     */
    private void validateConsent(GetAccountsInput input) {
        log.debug("Validating consent for consentId: {}", input.consentId());

        // Validar contexto completo do consentimento
        ConsentValidationService.ConsentValidationResult validationResult =
                consentValidationService.validateConsentForOperation(
                        input.consentId(),
                        ConsentValidationService.AccountOperation.GET_ACCOUNTS
                );

        if (!validationResult.isValid()) {
            String errorCode = mapValidationStatusToErrorCode(validationResult.status());
            throw BusinessRuleViolationException.with(errorCode);
        }

        // Registrar acesso ao consentimento para auditoria
        consentValidationService.recordConsentAccess(
                input.consentId(),
                "GET_ACCOUNTS",
                true
        );

        log.debug("Consent validation successful for consentId: {}", input.consentId());
    }

    /**
     * Mapeia status de validação para código de erro
     */
    private String mapValidationStatusToErrorCode(
            ConsentValidationService.ConsentValidationResult.ConsentValidationStatus status) {

        return switch (status) {
            case INVALID -> "INVALID_CONSENT";
            case INSUFFICIENT_PERMISSIONS -> "INSUFFICIENT_PERMISSIONS";
            case EXPIRED -> "CONSENT_EXPIRED";
            case ERROR -> "CONSENT_VALIDATION_ERROR";
            default -> "CONSENT_ERROR";
        };
    }

    /**
     * Valida limites de tráfego (TPM/TPS) para a organização
     */
    private void validateRateLimit(GetAccountsInput input) {
        log.debug("Validating rate limits for organizationId: {}", input.organizationId());

        if (!rateLimitValidationService.isWithinLimits(input.organizationId(), "/accounts")) {
            throw BusinessRuleViolationException.with("RATE_LIMIT_EXCEEDED");
        }
    }

    /**
     * Valida parâmetros específicos de contas
     */
    private void validateAccountParameters(GetAccountsInput input) {
        log.debug("Validating account parameters for request");

        // Validação de chave de paginação se presente
        if (input.hasPaginationKey()) {
            if (!paginationService.isValidPaginationKey(input.paginationKey().get())) {
                throw ValidationException.with("INVALID_PAGINATION_KEY");
            }
        }

        // Validação de tipo de conta se especificado
        if (input.type().isPresent()) {
            accountValidationService.validateAccountType(input.type().get());
        }
    }

    /**
     * Realiza a busca das contas no sistema externo
     */
    private List<Account> retrieveAccounts(GetAccountsInput input) {
        log.debug("Retrieving accounts from external system for consentId: {}", input.consentId());

        try {
            return accountPort.findAccountsByConsent(
                    input.consentId(),
                    input.organizationId(),
                    input.type().orElse(null)
            );
        } catch (Exception e) {
            log.error("Error retrieving accounts for consentId: {}", input.consentId(), e);
            throw BusinessRuleViolationException.with("ACCOUNT_RETRIEVAL_ERROR");
        }
    }

    /**
     * Aplica filtros adicionais e processamento às contas
     */
    private List<Account> applyFilters(List<Account> accounts, GetAccountsInput input) {
        log.debug("Applying filters to {} accounts", accounts.size());

        // Aplicar filtros específicos se necessário
        // Por exemplo, filtros baseados em permissões do consentimento

        return accounts; // Por enquanto, retorna sem filtros adicionais
    }

    /**
     * Gera informações de paginação para a resposta
     */
    private PaginationInfo generatePaginationInfo(List<AccountOutputDto> accounts, GetAccountsInput input) {
        log.debug("Generating pagination info for {} accounts", accounts.size());

        return paginationService.createPaginationInfo(
                input,
                accounts.size(),
                "/accounts"
        );
    }

    /**
     * Publica evento de acesso às contas para auditoria
     */
    private void publishAccountAccessEvent(GetAccountsInput input, List<Account> accounts) {
        log.debug("Publishing account access event for consentId: {}", input.consentId());

        try {
            // Construir contexto do acesso
            AccountAccessedEvent.AccountAccessContext accessContext =
                    new AccountAccessedEvent.AccountAccessContext(
                            input.xFapiInteractionId(),
                            input.xFapiAuthDate().map(Object::toString).orElse(null),
                            input.xFapiCustomerIpAddress().orElse(null),
                            input.xCustomerUserAgent().orElse(null),
                            new AccountAccessedEvent.AccountAccessContext.PaginationContext(
                                    input.page(),
                                    input.pageSize(),
                                    input.paginationKey().orElse(null),
                                    input.hasPaginationKey()
                            ),
                            new AccountAccessedEvent.AccountAccessContext.FilterContext(
                                    input.type().map(Enum::toString).orElse(null),
                                    input.type().isPresent(),
                                    null, // transactionFromDate - não aplicável para accounts
                                    null, // transactionToDate - não aplicável para accounts
                                    false // hasDateFilter
                            )
                    );

            // Construir resultado do acesso
            AccountAccessedEvent.AccountAccessResult accessResult =
                    new AccountAccessedEvent.AccountAccessResult(
                            true, // success
                            accounts.size(),
                            0L, // executionTime será preenchido pelo aspect
                            null, // errorCode
                            null, // errorMessage
                            accounts.isEmpty() ? AccountAccessedEvent.AccountAccessResult.AccessResultType.ERROR_NOT_FOUND :
                                    AccountAccessedEvent.AccountAccessResult.AccessResultType.SUCCESS_WITH_DATA
                    );

            // Criar e publicar evento
            AccountAccessedEvent event = AccountAccessedEvent.builder()
                    .consentId(input.consentId())
                    .organizationId(input.organizationId())
                    .operation("GET_ACCOUNTS")
                    .endpoint("/accounts")
                    .accessContext(accessContext)
                    .accessResult(accessResult)
                    .build();

            eventPublisher.publish(event);

        } catch (Exception e) {
            log.warn("Failed to publish account access event for consentId: {}", input.consentId(), e);
            // Não falha a operação por erro de auditoria
        }
    }
}