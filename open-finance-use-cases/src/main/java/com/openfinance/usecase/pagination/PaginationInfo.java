package com.openfinance.usecase.pagination;

import lombok.Builder;

import java.util.Objects;

/**
 * Pagination information for the accounts response
 */
@Builder
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
}
