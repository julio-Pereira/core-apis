package com.openfinance.usecase;

import com.openfinance.core.enums.AccountType;
import com.openfinance.usecase.account.GetAccountsUseCase;
import com.openfinance.usecase.account.input.GetAccountsInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Service responsible for building pagination links for the accounts API
 * Following Open Finance Brasil pagination standards
 */
@Component
public class PaginationLinkBuilder {

    private final String baseUrl;

    public PaginationLinkBuilder(@Value("${open-finance.api.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Builds all pagination links for the accounts response
     */
    public GetAccountsUseCase.PaginationLinks buildLinks(GetAccountsInput input,
                                                         int totalPages,
                                                         String paginationKey) {
        String baseEndpoint = baseUrl + "/open-banking/accounts/v2/accounts";

        return new GetAccountsUseCase.PaginationLinks(
                buildSelfLink(baseEndpoint, input, paginationKey),
                buildFirstLink(baseEndpoint, input, totalPages, paginationKey),
                buildPrevLink(baseEndpoint, input, totalPages, paginationKey),
                buildNextLink(baseEndpoint, input, totalPages, paginationKey),
                buildLastLink(baseEndpoint, input, totalPages, paginationKey)
        );
    }

    /**
     * Builds the self link (current page)
     */
    private String buildSelfLink(String baseEndpoint, GetAccountsInput input, String paginationKey) {
        return buildLink(baseEndpoint, input.page(), input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds the first page link (mandatory if not first page)
     */
    private String buildFirstLink(String baseEndpoint, GetAccountsInput input,
                                  int totalPages, String paginationKey) {
        if (input.page() == 1 || totalPages == 0) {
            return null; // Not required for first page
        }

        return buildLink(baseEndpoint, 1, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds the previous page link (mandatory if not first page)
     */
    private String buildPrevLink(String baseEndpoint, GetAccountsInput input,
                                 int totalPages, String paginationKey) {
        if (input.page() <= 1 || totalPages == 0) {
            return null; // No previous page
        }

        return buildLink(baseEndpoint, input.page() - 1, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds the next page link (mandatory if not last page)
     */
    private String buildNextLink(String baseEndpoint, GetAccountsInput input,
                                 int totalPages, String paginationKey) {
        if (input.page() >= totalPages || totalPages == 0) {
            return null; // No next page
        }

        return buildLink(baseEndpoint, input.page() + 1, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds the last page link (mandatory if not last page)
     */
    private String buildLastLink(String baseEndpoint, GetAccountsInput input,
                                 int totalPages, String paginationKey) {
        if (input.page() == totalPages || totalPages == 0) {
            return null; // Not required for last page
        }

        return buildLink(baseEndpoint, totalPages, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds a complete link with all required parameters
     */
    private String buildLink(String baseEndpoint, int page, int pageSize,
                             AccountType accountType,
                             String paginationKey) {
        StringJoiner params = new StringJoiner("&");

        // Always include page and page-size
        params.add("page=" + page);
        params.add("page-size=" + pageSize);

        // Include account type filter if present
        if (accountType != null) {
            params.add("accountType=" + urlEncode(accountType.name()));
        }

        // Include pagination key for operational limits control
        if (paginationKey != null && !paginationKey.trim().isEmpty()) {
            params.add("pagination-key=" + urlEncode(paginationKey));
        }

        return baseEndpoint + "?" + params.toString();
    }

    /**
     * URL encodes a parameter value
     */
    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Validates if the base URL follows the expected pattern
     */
    public boolean isValidBaseUrl() {
        return baseUrl != null &&
                !baseUrl.trim().isEmpty() &&
                (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"));
    }

    /**
     * Gets the configured base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}