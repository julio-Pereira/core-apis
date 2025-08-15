package com.openfinance.usecase.account.service;

import com.openfinance.core.domain.consent.OpenFinancePermission;
import com.openfinance.usecase.account.port.IConsentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Serviço responsável por validar consentimentos e permissões
 * conforme especificações do Open Finance Brasil.
 *
 * Implementa as regras de validação de consentimento, incluindo:
 * - Verificação de existência e validade
 * - Validação de permissões específicas usando enums
 * - Controle de expiração
 * - Verificação de status (AUTHORISED, CONSUMED, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentValidationService {

    private final IConsentPort consentPort;
    private final PermissionFilterService permissionFilterService;

    /**
     * Valida se um consentimento é válido e ativo
     *
     * @param consentId Identificador do consentimento
     * @return true se o consentimento é válido, false caso contrário
     */
    public boolean isValidConsent(String consentId) {
        log.debug("Validating consent: {}", consentId);

        try {
            var consent = consentPort.findByConsentId(consentId);

            if (consent.isEmpty()) {
                log.warn("Consent not found: {}", consentId);
                return false;
            }

            var consentData = consent.get();

            // Verificar se está no status AUTHORISED
            if (!"AUTHORISED".equals(consentData.status())) {
                log.warn("Consent {} is not in AUTHORISED status: {}",
                        consentId, consentData.status());
                return false;
            }

            // Verificar se não expirou
            if (consentData.isExpired()) {
                log.warn("Consent {} has expired: {}",
                        consentId, consentData.expirationDateTime());
                return false;
            }

            // Verificar se está dentro do período de transações
            if (!consentData.isWithinTransactionPeriod()) {
                log.warn("Consent {} is outside transaction period", consentId);
                return false;
            }

            log.debug("Consent {} is valid", consentId);
            return true;

        } catch (Exception e) {
            log.error("Error validating consent {}: {}", consentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se o consentimento possui permissão para leitura de contas
     * Aceita tanto ACCOUNTS_READ quanto ACCOUNTS_BALANCES_READ
     *
     * @param consentId Identificador do consentimento
     * @return true se possui permissão, false caso contrário
     */
    public boolean hasAccountsReadPermission(String consentId) {
        log.debug("Checking accounts read permission for consent: {}", consentId);

        Set<OpenFinancePermission> requiredPermissions = Set.of(
                OpenFinancePermission.ACCOUNTS_READ,
                OpenFinancePermission.ACCOUNTS_BALANCES_READ
        );

        return hasAnyPermission(consentId, requiredPermissions);
    }

    /**
     * Verifica se o consentimento possui permissão específica usando enum
     *
     * @param consentId Identificador do consentimento
     * @param permission Permissão a ser verificada
     * @return true se possui a permissão, false caso contrário
     */
    public boolean hasPermission(String consentId, OpenFinancePermission permission) {
        log.debug("Checking permission {} for consent: {}", permission.getPermission(), consentId);

        try {
            var consent = consentPort.findByConsentId(consentId);

            if (consent.isEmpty()) {
                log.warn("Consent not found for permission check: {}", consentId);
                return false;
            }

            var permissions = consent.get().permissions();
            boolean hasPermission = permissionFilterService.hasRequiredPermission(
                    permissions, permission);

            if (!hasPermission) {
                log.debug("Consent {} does not have permission {}. Available permissions: {}",
                        consentId, permission.getPermission(), permissions);
            } else {
                log.debug("Consent {} has permission {}", consentId, permission.getPermission());
            }

            return hasPermission;

        } catch (Exception e) {
            log.error("Error checking permission {} for consent {}: {}",
                    permission.getPermission(), consentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se o consentimento possui pelo menos uma das permissões do conjunto
     *
     * @param consentId Identificador do consentimento
     * @param permissions Conjunto de permissões aceitas
     * @return true se possui pelo menos uma permissão, false caso contrário
     */
    public boolean hasAnyPermission(String consentId, Set<OpenFinancePermission> permissions) {
        log.debug("Checking any permission from set {} for consent: {}",
                permissions.size(), consentId);

        try {
            var consent = consentPort.findByConsentId(consentId);

            if (consent.isEmpty()) {
                log.warn("Consent not found for permission check: {}", consentId);
                return false;
            }

            var consentPermissions = consent.get().permissions();
            boolean hasAnyPermission = permissionFilterService.hasAnyPermission(
                    consentPermissions, permissions);

            if (!hasAnyPermission) {
                log.debug("Consent {} does not have any required permissions. " +
                                "Required: {}, Available: {}",
                        consentId,
                        permissions.stream().map(OpenFinancePermission::getPermission).toList(),
                        consentPermissions);
            } else {
                log.debug("Consent {} has at least one required permission", consentId);
            }

            return hasAnyPermission;

        } catch (Exception e) {
            log.error("Error checking permissions for consent {}: {}",
                    consentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Valida permissões específicas para balances
     *
     * @param consentId Identificador do consentimento
     * @return true se pode acessar balances, false caso contrário
     */
    public boolean hasBalancesPermission(String consentId) {
        return hasPermission(consentId, OpenFinancePermission.ACCOUNTS_BALANCES_READ);
    }

    /**
     * Valida permissões específicas para transactions
     *
     * @param consentId Identificador do consentimento
     * @return true se pode acessar transactions, false caso contrário
     */
    public boolean hasTransactionsPermission(String consentId) {
        return hasPermission(consentId, OpenFinancePermission.ACCOUNTS_TRANSACTIONS_READ);
    }

    /**
     * Valida permissões específicas para overdraft limits
     *
     * @param consentId Identificador do consentimento
     * @return true se pode acessar overdraft limits, false caso contrário
     */
    public boolean hasOverdraftLimitsPermission(String consentId) {
        return hasPermission(consentId, OpenFinancePermission.ACCOUNTS_OVERDRAFT_LIMITS_READ);
    }

    /**
     * Valida se o consentimento ainda tem transações restantes
     * (para casos com limite de uso)
     *
     * @param consentId Identificador do consentimento
     * @return true se ainda tem transações disponíveis, false caso contrário
     */
    public boolean hasRemainingTransactions(String consentId) {
        log.debug("Checking remaining transactions for consent: {}", consentId);

        try {
            var consent = consentPort.findByConsentId(consentId);

            if (consent.isEmpty()) {
                return false;
            }

            var consentData = consent.get();

            // Se não há limite definido, sempre retorna true
            if (consentData.transactionFromDate() == null &&
                    consentData.transactionToDate() == null) {
                return true;
            }

            // Verificar se está dentro do período permitido
            return consentData.isWithinTransactionPeriod();

        } catch (Exception e) {
            log.error("Error checking remaining transactions for consent {}: {}",
                    consentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Filtra e valida permissões de um consentimento conforme produtos suportados
     *
     * @param consentId Identificador do consentimento
     * @return Resultado da filtragem de permissões
     */
    public PermissionFilterService.PermissionFilterResult filterConsentPermissions(String consentId) {
        log.debug("Filtering permissions for consent: {}", consentId);

        try {
            var consent = consentPort.findByConsentId(consentId);

            if (consent.isEmpty()) {
                throw new IllegalArgumentException("Consent not found: " + consentId);
            }

            var permissions = consent.get().permissions();
            return permissionFilterService.filterSupportedPermissions(permissions);

        } catch (Exception e) {
            log.error("Error filtering permissions for consent {}: {}",
                    consentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtém todas as permissões válidas de um consentimento
     *
     * @param consentId Identificador do consentimento
     * @return Lista de permissões válidas
     */
    public java.util.List<String> getValidPermissions(String consentId) {
        log.debug("Getting valid permissions for consent: {}", consentId);

        try {
            var consent = consentPort.findByConsentId(consentId);

            if (consent.isEmpty()) {
                return java.util.List.of();
            }

            var permissions = consent.get().permissions();
            var filterResult = permissionFilterService.filterSupportedPermissions(permissions);

            return filterResult.filteredPermissions();

        } catch (Exception e) {
            log.error("Error getting valid permissions for consent {}: {}",
                    consentId, e.getMessage(), e);
            return java.util.List.of();
        }
    }

    /**
     * Valida se o consentimento tem permissões mínimas necessárias para operação
     *
     * @param consentId Identificador do consentimento
     * @param operation Tipo de operação sendo realizada
     * @return true se tem permissões suficientes, false caso contrário
     */
    public boolean hasMinimumPermissionsForOperation(String consentId, AccountOperation operation) {
        log.debug("Checking minimum permissions for operation {} on consent: {}",
                operation, consentId);

        Set<OpenFinancePermission> requiredPermissions = switch (operation) {
            case GET_ACCOUNTS -> Set.of(OpenFinancePermission.ACCOUNTS_READ);
            case GET_ACCOUNT_DETAILS -> Set.of(OpenFinancePermission.ACCOUNTS_READ);
            case GET_BALANCES -> Set.of(OpenFinancePermission.ACCOUNTS_BALANCES_READ);
            case GET_TRANSACTIONS -> Set.of(OpenFinancePermission.ACCOUNTS_TRANSACTIONS_READ);
            case GET_OVERDRAFT_LIMITS -> Set.of(OpenFinancePermission.ACCOUNTS_OVERDRAFT_LIMITS_READ);
        };

        return hasAnyPermission(consentId, requiredPermissions);
    }

    /**
     * Enum para tipos de operações de conta
     */
    public enum AccountOperation {
        GET_ACCOUNTS,
        GET_ACCOUNT_DETAILS,
        GET_BALANCES,
        GET_TRANSACTIONS,
        GET_OVERDRAFT_LIMITS
    }

    /**
     * Registra acesso ao consentimento para auditoria
     *
     * @param consentId Identificador do consentimento
     * @param operation Operação realizada
     * @param success Se a operação foi bem-sucedida
     */
    public void recordConsentAccess(String consentId, String operation, boolean success) {
        log.debug("Recording consent access: {} - {} - {}", consentId, operation,
                success ? "SUCCESS" : "FAILURE");

        try {
            // Atualizar último acesso se operação foi bem-sucedida
            if (success) {
                consentPort.updateLastAccess(consentId);
            }

            // Log estruturado para auditoria
            if (success) {
                log.info("CONSENT_ACCESS - ConsentId: {}, Operation: {}, Status: SUCCESS, Timestamp: {}",
                        consentId, operation, LocalDateTime.now());
            } else {
                log.warn("CONSENT_ACCESS - ConsentId: {}, Operation: {}, Status: FAILURE, Timestamp: {}",
                        consentId, operation, LocalDateTime.now());
            }

        } catch (Exception e) {
            log.error("Error recording consent access for {}: {}", consentId, e.getMessage(), e);
            // Não falhar a operação principal por erro de logging
        }
    }

    /**
     * Valida contexto completo do consentimento para uma operação
     *
     * @param consentId Identificador do consentimento
     * @param operation Operação sendo realizada
     * @return ConsentValidationResult com resultado detalhado
     */
    public ConsentValidationResult validateConsentForOperation(String consentId, AccountOperation operation) {
        log.debug("Validating consent {} for operation {}", consentId, operation);

        try {
            // Validações básicas
            if (!isValidConsent(consentId)) {
                return ConsentValidationResult.invalid("Consentimento inválido ou expirado");
            }

            // Validar permissões específicas
            if (!hasMinimumPermissionsForOperation(consentId, operation)) {
                return ConsentValidationResult.insufficientPermissions(
                        "Consentimento não possui permissões necessárias para a operação");
            }

            // Validar transações restantes
            if (!hasRemainingTransactions(consentId)) {
                return ConsentValidationResult.expired("Período de transações do consentimento expirado");
            }

            return ConsentValidationResult.valid("Consentimento válido para operação");

        } catch (Exception e) {
            log.error("Error during consent validation for {}: {}", consentId, e.getMessage(), e);
            return ConsentValidationResult.error("Erro interno durante validação do consentimento");
        }
    }

    /**
     * Record com resultado detalhado da validação de consentimento
     */
    public record ConsentValidationResult(
            boolean isValid,
            String reason,
            ConsentValidationStatus status
    ) {

        public static ConsentValidationResult valid(String reason) {
            return new ConsentValidationResult(true, reason, ConsentValidationStatus.VALID);
        }

        public static ConsentValidationResult invalid(String reason) {
            return new ConsentValidationResult(false, reason, ConsentValidationStatus.INVALID);
        }

        public static ConsentValidationResult insufficientPermissions(String reason) {
            return new ConsentValidationResult(false, reason, ConsentValidationStatus.INSUFFICIENT_PERMISSIONS);
        }

        public static ConsentValidationResult expired(String reason) {
            return new ConsentValidationResult(false, reason, ConsentValidationStatus.EXPIRED);
        }

        public static ConsentValidationResult error(String reason) {
            return new ConsentValidationResult(false, reason, ConsentValidationStatus.ERROR);
        }

        public enum ConsentValidationStatus {
            VALID,
            INVALID,
            INSUFFICIENT_PERMISSIONS,
            EXPIRED,
            ERROR
        }
    }
}