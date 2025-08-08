package com.openfinance.usecase.pagination;

import lombok.Builder;

@Builder
public record PaginationLinks(
        String selfLink,
        String firstLink,
        String prevLink,
        String nextLink,
        String lastLink
) {
}
