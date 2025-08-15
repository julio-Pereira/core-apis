package com.openfinance.core.port;

import com.openfinance.core.domain.account.AccountId;

/**
* Service port for consent validation operations
 */
public interface IConsentValidationService {

    /**
     * Validates if the consent is valid and authorized
     *
     * @param consentId the consent identifier
     * @return true if consent is valid and authorized
     */
    boolean isConsentValid(String consentId);

    /**
     * Validates if the consent has the required permission
     *
     * @param consentId the consent identifier
     * @param permission the required permission
     * @return true if consent has the permission
     */
    boolean hasPermission(String consentId, String permission);

    /**
     * Validates if the consent can access the specific account
     *
     * @param consentId the consent identifier
     * @param accountId the account identifier
     * @return true if consent can access the account
     */
    boolean canAccessAccount(String consentId, AccountId accountId);
}
