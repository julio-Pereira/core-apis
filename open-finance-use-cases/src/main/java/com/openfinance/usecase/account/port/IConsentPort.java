package com.openfinance.usecase.account.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IConsentPort {

    Optional<ConsentData> findByConsentId(String consentId);

    boolean isConsentActive(String consentId);


    void updateLastAccess(String consentId);

    /**
     * Represents the consent data structure.
     * This class encapsulates the details of a consent, including its ID, status,
     * expiration date, transaction date range, permissions, and last access time.
     */
    record ConsentData(
            String consentId,
            String status,
            LocalDateTime expirationDateTime,
            LocalDateTime transactionFromDate,
            LocalDateTime transactionToDate,
            List<String> permissions,
            LocalDateTime lastAccess
    ) {

        public boolean isExpired() {
            return expirationDateTime != null && expirationDateTime.isBefore(LocalDateTime.now());
        }

        public boolean isAuthorised() {
            return "AUTHORISED".equals(status);
        }


        public boolean isWithinTransactionPeriod() {
            LocalDateTime now = LocalDateTime.now();

            if (transactionFromDate != null && now.isBefore(transactionFromDate)) {
                return false;
            }

            if (transactionToDate != null && now.isAfter(transactionToDate)) {
                return false;
            }

            return true;
        }

        public boolean hasPermission(String permission) {
            return permissions != null && permissions.contains(permission);
        }

    }
}
