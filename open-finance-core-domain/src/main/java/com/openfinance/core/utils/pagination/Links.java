package com.openfinance.core.utils.pagination;

public record Links(
        String self,
        String first,
        String prev,
        String next,
        String last
        ) {}
