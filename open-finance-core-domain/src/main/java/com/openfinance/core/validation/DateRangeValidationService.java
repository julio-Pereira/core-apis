package com.openfinance.core.validation;

import com.openfinance.core.exceptions.BusinessRuleViolationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Domain service for date range validation business rules
 */
public class DateRangeValidationService {

    private static final int MAX_TRANSACTION_RANGE_MONTHS = 12;
    private static final int MAX_RECENT_TRANSACTION_DAYS = 7;

    /**
     * Validates transaction date range (max 12 months in the past + 12 months in the future)
     *
     * @param fromDate start date (optional)
     * @param toDate end date (optional)
     */
    public void validateTransactionDateRange(Optional<LocalDate> fromDate, Optional<LocalDate> toDate) {
        LocalDate today = LocalDate.now();
        LocalDate maxPastDate = today.minusMonths(MAX_TRANSACTION_RANGE_MONTHS);
        LocalDate maxFutureDate = today.plusMonths(MAX_TRANSACTION_RANGE_MONTHS);

        // Validate from date
        if (fromDate.isPresent()) {
            LocalDate from = fromDate.get();
            if (from.isBefore(maxPastDate)) {
                throw new BusinessRuleViolationException(
                        "From date cannot be more than " + MAX_TRANSACTION_RANGE_MONTHS + " months in the past"
                );
            }
            if (from.isAfter(maxFutureDate)) {
                throw new BusinessRuleViolationException(
                        "From date cannot be more than " + MAX_TRANSACTION_RANGE_MONTHS + " months in the future"
                );
            }
        }

        // Validate to date
        if (toDate.isPresent()) {
            LocalDate to = toDate.get();
            if (to.isBefore(maxPastDate)) {
                throw new BusinessRuleViolationException(
                        "To date cannot be more than " + MAX_TRANSACTION_RANGE_MONTHS + " months in the past"
                );
            }
            if (to.isAfter(maxFutureDate)) {
                throw new BusinessRuleViolationException(
                        "To date cannot be more than " + MAX_TRANSACTION_RANGE_MONTHS + " months in the future"
                );
            }
        }

        // Validate date range consistency
        if (fromDate.isPresent() && toDate.isPresent()) {
            if (fromDate.get().isAfter(toDate.get())) {
                throw new BusinessRuleViolationException("From date cannot be after to date");
            }
        }
    }

    /**
     * Validates recent transaction date range (max 7 days in the past + 12 months in the future)
     *
     * @param fromDate start date (optional)
     * @param toDate end date (optional)
     */
    public void validateRecentTransactionDateRange(Optional<LocalDate> fromDate, Optional<LocalDate> toDate) {
        LocalDate today = LocalDate.now();
        LocalDate maxPastDate = today.minusDays(MAX_RECENT_TRANSACTION_DAYS);
        LocalDate maxFutureDate = today.plusMonths(MAX_TRANSACTION_RANGE_MONTHS);

        // Validate from date for recent transactions
        if (fromDate.isPresent()) {
            LocalDate from = fromDate.get();
            if (from.isBefore(maxPastDate)) {
                throw new BusinessRuleViolationException(
                        "From date for recent transactions cannot be more than " + MAX_RECENT_TRANSACTION_DAYS + " days in the past"
                );
            }
            if (from.isAfter(maxFutureDate)) {
                throw new BusinessRuleViolationException(
                        "From date cannot be more than " + MAX_TRANSACTION_RANGE_MONTHS + " months in the future"
                );
            }
        }

        // Validate to date
        if (toDate.isPresent()) {
            LocalDate to = toDate.get();
            if (to.isBefore(maxPastDate)) {
                throw new BusinessRuleViolationException(
                        "To date for recent transactions cannot be more than " + MAX_RECENT_TRANSACTION_DAYS + " days in the past"
                );
            }
            if (to.isAfter(maxFutureDate)) {
                throw new BusinessRuleViolationException(
                        "To date cannot be more than " + MAX_TRANSACTION_RANGE_MONTHS + " months in the future"
                );
            }
        }

        // Validate date range consistency
        if (fromDate.isPresent() && toDate.isPresent()) {
            if (fromDate.get().isAfter(toDate.get())) {
                throw new BusinessRuleViolationException("From date cannot be after to date");
            }
        }
    }

    /**
     * Calculates the number of days between two dates
     *
     * @param fromDate start date
     * @param toDate end date
     * @return number of days
     */
    public long calculateDaysBetween(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }

    /**
     * Checks if the date range is within the allowed limits
     *
     * @param fromDate start date (optional)
     * @param toDate end date (optional)
     * @param maxDays maximum allowed days
     * @return true if within limits
     */
    public boolean isWithinAllowedRange(Optional<LocalDate> fromDate, Optional<LocalDate> toDate, long maxDays) {
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            return true; // No range specified
        }

        long daysBetween = calculateDaysBetween(fromDate.get(), toDate.get());
        return daysBetween <= maxDays;
    }
}

