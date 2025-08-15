package com.openfinance.usecase.account.port;

import com.openfinance.core.domain.account.Account;
import com.openfinance.core.domain.account.AccountType;

import java.util.List;
import java.util.Optional;

/**
 * Port para acesso a dados de contas.
 * Interface que define o contrato para busca de contas no sistema externo,
 * seguindo os princípios da arquitetura hexagonal.
 */
public interface IAccountPort {

    /**
     * Busca contas por consentimento
     *
     * @param consentId Identificador do consentimento
     * @param organizationId Identificador da organização
     * @param accountType Filtro opcional de tipo de conta
     * @return Lista de contas encontradas
     */
    List<Account> findAccountsByConsent(String consentId, String organizationId, AccountType accountType);

    /**
     * Busca uma conta específica por ID
     *
     * @param accountId Identificador da conta
     * @param consentId Identificador do consentimento
     * @return Conta encontrada, se existir
     */
    Optional<Account> findAccountById(String accountId, String consentId);

    /**
     * Verifica se uma conta existe e o usuário tem acesso
     *
     * @param accountId Identificador da conta
     * @param consentId Identificador do consentimento
     * @return true se existe e tem acesso, false caso contrário
     */
    boolean hasAccessToAccount(String accountId, String consentId);
}