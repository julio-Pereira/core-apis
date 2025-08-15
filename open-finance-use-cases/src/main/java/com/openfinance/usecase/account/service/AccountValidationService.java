package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.account.AccountType;
import com.openfinance.core.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * Serviço responsável por validações específicas relacionadas a contas
 * conforme especificações do Open Finance Brasil.
 *
 * Implementa validações de:
 * - Tipos de conta suportados
 * - Regras de negócio específicas de conta
 * - Validações de parâmetros de filtro
 * - Consistência de dados de conta
 */
@Slf4j
@Service
public class AccountValidationService {

    /**
     * Tipos de conta suportados pela API de Accounts
     */
    private static final Set<AccountType> SUPPORTED_ACCOUNT_TYPES = EnumSet.of(
            AccountType.CONTA_DEPOSITO_A_VISTA,
            AccountType.CONTA_POUPANCA,
            AccountType.CONTA_PAGAMENTO_PRE_PAGA
    );

    /**
     * Valida se o tipo de conta é suportado pela API
     *
     * @param accountType Tipo de conta a ser validado
     * @throws ValidationException se o tipo não for suportado
     */
    public void validateAccountType(AccountType accountType) {
        log.debug("Validating account type: {}", accountType);

        if (accountType == null) {
            throw ValidationException.with("INVALID_ACCOUNT_TYPE");
        }

        if (!SUPPORTED_ACCOUNT_TYPES.contains(accountType)) {
            log.warn("Unsupported account type: {}", accountType);
            throw ValidationException.with("UNSUPPORTED_ACCOUNT_TYPE");
        }

        log.debug("Account type {} is valid", accountType);
    }

    /**
     * Valida se os parâmetros de paginação são válidos para contas
     *
     * @param page Número da página
     * @param pageSize Tamanho da página
     */
    public void validatePaginationParameters(int page, int pageSize) {
        log.debug("Validating pagination parameters: page={}, pageSize={}", page, pageSize);

        if (page < 1) {
            throw ValidationException.with("INVALID_PAGE_NUMBER");
        }

        if (pageSize < 1) {
            throw ValidationException.with("INVALID_PAGE_SIZE");
        }

        if (pageSize > 1000) {
            throw ValidationException.with("INVALID_PAGE_SIZE");
        }

        // Validação específica para compliance Open Finance
        if (pageSize > 25 && page == 1) {
            log.info("Large page size requested on first page: {} (recommended: 25 or less)",
                    pageSize);
        }

        log.debug("Pagination parameters are valid");
    }

    /**
     * Valida consistência dos dados de conta retornados
     *
     * @param accountId Identificador da conta
     * @param accountType Tipo da conta
     * @param branchCode Código da agência
     */
    public void validateAccountConsistency(String accountId, AccountType accountType, String branchCode) {
        log.debug("Validating account consistency for accountId: {}", accountId);

        if (accountId == null || accountId.trim().isEmpty()) {
            throw ValidationException.with("INVALID_ACCOUNT_ID");
        }

        validateAccountType(accountType);

        if (branchCode != null && !branchCode.trim().isEmpty()) {
            validateBranchCode(branchCode);
        }

        // Validações específicas por tipo de conta
        validateTypeSpecificRules(accountId, accountType);

        log.debug("Account consistency validation passed for accountId: {}", accountId);
    }

    /**
     * Valida código de agência conforme padrões bancários brasileiros
     */
    private void validateBranchCode(String branchCode) {
        log.debug("Validating branch code: {}", branchCode);

        // Remover caracteres especiais para validação
        String cleanBranchCode = branchCode.replaceAll("[^0-9]", "");

        if (cleanBranchCode.length() < 3 || cleanBranchCode.length() > 5) {
            throw ValidationException.with("INVALID_BRANCH_CODE");
        }

        // Verificar se contém apenas números
        if (!cleanBranchCode.matches("\\d+")) {
            throw ValidationException.with("INVALID_BRANCH_CODE");
        }

        log.debug("Branch code {} is valid", branchCode);
    }

    /**
     * Aplica validações específicas baseadas no tipo de conta
     */
    private void validateTypeSpecificRules(String accountId, AccountType accountType) {
        log.debug("Applying type-specific validation rules for account type: {}", accountType);

        switch (accountType) {
            case CONTA_DEPOSITO_A_VISTA -> validateCheckingAccountRules(accountId);
            case CONTA_POUPANCA -> validateSavingsAccountRules(accountId);
            case CONTA_PAGAMENTO_PRE_PAGA -> validatePrepaidAccountRules(accountId);
            default -> log.warn("No specific validation rules for account type: {}", accountType);
        }
    }

    /**
     * Validações específicas para conta corrente
     */
    private void validateCheckingAccountRules(String accountId) {
        log.debug("Applying checking account validation rules for accountId: {}", accountId);

        // Contas correntes podem ter cheque especial
        // Validações específicas podem ser adicionadas aqui

        log.debug("Checking account validation passed for accountId: {}", accountId);
    }

    /**
     * Validações específicas para conta poupança
     */
    private void validateSavingsAccountRules(String accountId) {
        log.debug("Applying savings account validation rules for accountId: {}", accountId);

        // Contas poupança têm regras específicas de rendimento
        // Validações específicas podem ser adicionadas aqui

        log.debug("Savings account validation passed for accountId: {}", accountId);
    }

    /**
     * Validações específicas para conta pagamento pré-paga
     */
    private void validatePrepaidAccountRules(String accountId) {
        log.debug("Applying prepaid account validation rules for accountId: {}", accountId);

        // Contas pré-pagas têm limites e regras específicas
        // Validações específicas podem ser adicionadas aqui

        log.debug("Prepaid account validation passed for accountId: {}", accountId);
    }

    /**
     * Valida se um conjunto de contas tem tipos consistentes
     *
     * @param accountTypes Conjunto de tipos de conta para validação
     */
    public void validateAccountTypeConsistency(Set<AccountType> accountTypes) {
        log.debug("Validating account type consistency for {} types", accountTypes.size());

        for (AccountType accountType : accountTypes) {
            validateAccountType(accountType);
        }

        // Verificar se há tipos conflitantes ou inválidos
        boolean hasInvalidCombination = accountTypes.stream()
                .anyMatch(type -> !SUPPORTED_ACCOUNT_TYPES.contains(type));

        if (hasInvalidCombination) {
            throw ValidationException.with("INVALID_ACCOUNT_TYPE_COMBINATION");
        }

        log.debug("Account type consistency validation passed");
    }

    /**
     * Retorna os tipos de conta suportados
     *
     * @return Set com os tipos de conta suportados
     */
    public Set<AccountType> getSupportedAccountTypes() {
        return Set.copyOf(SUPPORTED_ACCOUNT_TYPES);
    }
}