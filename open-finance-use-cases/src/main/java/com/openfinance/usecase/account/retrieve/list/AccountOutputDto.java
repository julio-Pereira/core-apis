package com.openfinance.usecase.account.retrieve.list;

/**
 * DTO de saída para representar uma conta nos casos de uso.
 *
 * Este DTO faz parte da camada de use case e serve como intermediário
 * entre as entidades de domínio e os DTOs da API, mantendo o isolamento
 * das camadas conforme a arquitetura hexagonal.
 */
public record AccountOutputDto(
        String accountId,
        String type,
        String subtype,
        String nickname,
        String number,
        String checkDigit,
        String branchCode,
        String branchCheckDigit
) {

    public AccountOutputDto {
        // Validações básicas para campos obrigatórios
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("AccountId cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
    }

    /**
     * Verifica se a conta possui um apelido
     */
    public boolean hasNickname() {
        return nickname != null && !nickname.trim().isEmpty();
    }

    /**
     * Verifica se a conta possui dígito verificador da agência
     */
    public boolean hasBranchCheckDigit() {
        return branchCheckDigit != null && !branchCheckDigit.trim().isEmpty();
    }
}
