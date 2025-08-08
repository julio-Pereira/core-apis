package com.openfinance.usecase.account.output;

import com.openfinance.core.domain.account.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Output data for the Get Accounts use case
 */
public record GetAccountsOutput(
        List<Account> accounts,
        PaginationInfo paginationInfo,
        LocalDateTime requestDateTime
) {

    /**
     * Creates a new GetAccountsOutput with validation
     */
    public GetAccountsOutput {
        Objects.requireNonNull(accounts, "Accounts list cannot be null");
        Objects.requireNonNull(paginationInfo, "Pagination info cannot be null");
        Objects.requireNonNull(requestDateTime, "Request date time cannot be null");
    }

    /**
     * Creates a builder for GetAccountsOutput
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if there are any accounts in the result
     */
    public boolean hasAccounts() {
        return !accounts.isEmpty();
    }

    /**
     * Gets the total number of accounts in this page
     */
    public int getAccountCount() {
        return accounts.size();
    }

    /**
     * Pagination information for the accounts response
     */
    public record PaginationInfo(
            String selfLink,
            String firstLink,
            String prevLink,
            String nextLink,
            String lastLink,
            int totalRecords,
            int totalPages,
            int currentPage,
            int pageSize,
            String paginationKey
    ) {

        /**
         * Creates a new PaginationInfo with validation
         */
        public PaginationInfo {
            Objects.requireNonNull(selfLink, "Self link cannot be null");

            if (totalRecords < 0) {
                throw new IllegalArgumentException("Total records cannot be negative");
            }
            if (totalPages < 0) {
                throw new IllegalArgumentException("Total pages cannot be negative");
            }
            if (currentPage < 1) {
                throw new IllegalArgumentException("Current page must be greater than 0");
            }
            if (pageSize < 1) {
                throw new IllegalArgumentException("Page size must be greater than 0");
            }
        }

        /**
         * Checks if this is the first page
         */
        public boolean isFirstPage() {
            return currentPage == 1;
        }

        /**
         * Checks if this is the last page
         */
        public boolean isLastPage() {
            return currentPage == totalPages || totalPages == 0;
        }

        /**
         * Checks if there is a next page
         */
        public boolean hasNextPage() {
            return !isLastPage();
        }

        /**
         * Checks if there is a previous page
         */
        public boolean hasPreviousPage() {
            return !isFirstPage();
        }

        /**
         * Creates a builder for PaginationInfo
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder pattern for PaginationInfo
         */
        public static class Builder {
            private String selfLink;
            private String firstLink;
            private String prevLink;
            private String nextLink;
            private String lastLink;
            private int totalRecords;
            private int totalPages;
            private int currentPage;
            private int pageSize;
            private String paginationKey;

            public Builder selfLink(String selfLink) {
                this.selfLink = selfLink;
                return this;
            }

            public Builder firstLink(String firstLink) {
                this.firstLink = firstLink;
                return this;
            }

            public Builder prevLink(String prevLink) {
                this.prevLink = prevLink;
                return this;
            }

            public Builder nextLink(String nextLink) {
                this.nextLink = nextLink;
                return this;
            }

            public Builder lastLink(String lastLink) {
                this.lastLink = lastLink;
                return this;
            }

            public Builder totalRecords(int totalRecords) {
                this.totalRecords = totalRecords;
                return this;
            }

            public Builder totalPages(int totalPages) {
                this.totalPages = totalPages;
                return this;
            }

            public Builder currentPage(int currentPage) {
                this.currentPage = currentPage;
                return this;
            }

            public Builder pageSize(int pageSize) {
                this.pageSize = pageSize;
                return this;
            }

            public Builder paginationKey(String paginationKey) {
                this.paginationKey = paginationKey;
                return this;
            }

            public PaginationInfo build() {
                return new PaginationInfo(
                        selfLink,
                        firstLink,
                        prevLink,
                        nextLink,
                        lastLink,
                        totalRecords,
                        totalPages,
                        currentPage,
                        pageSize,
                        paginationKey
                );
            }
        }
    }

    /**
     * Builder pattern for GetAccountsOutput
     */
    public static class Builder {
        private List<Account> accounts;
        private PaginationInfo paginationInfo;
        private LocalDateTime requestDateTime;

        public Builder accounts(List<Account> accounts) {
            this.accounts = accounts;
            return this;
        }

        public Builder paginationInfo(PaginationInfo paginationInfo) {
            this.paginationInfo = paginationInfo;
            return this;
        }

        public Builder requestDateTime(LocalDateTime requestDateTime) {
            this.requestDateTime = requestDateTime;
            return this;
        }

        public GetAccountsOutput build() {
            return new GetAccountsOutput(
                    accounts,
                    paginationInfo,
                    requestDateTime != null ? requestDateTime : LocalDateTime.now()
            );
        }
    }
}
