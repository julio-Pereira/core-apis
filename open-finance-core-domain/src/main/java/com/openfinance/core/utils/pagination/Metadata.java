package com.openfinance.core.utils.pagination;

import java.time.LocalDateTime;

public record Metadata(int totalRecords, int totalPages, LocalDateTime requestDateTime) {}
