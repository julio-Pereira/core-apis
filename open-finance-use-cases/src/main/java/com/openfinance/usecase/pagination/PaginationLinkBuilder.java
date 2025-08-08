package com.openfinance.usecase.pagination;

import com.openfinance.core.enums.AccountType;
import com.openfinance.usecase.account.input.GetAccountsInput;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Service responsible for building pagination links for the accounts API
 * Following Open Finance Brasil pagination standards
 */
@Getter
@Slf4j
@Component
public class PaginationLinkBuilder {

    /**
     * -- GETTER --
     *  Gets the configured base URL
     */
    private final String baseUrl;

    public PaginationLinkBuilder(String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        log.debug("PaginationLinkBuilderService initialized with baseUrl: {}", this.baseUrl);
    }

    /**
     * Builds all pagination links for the accounts response
     */
    public PaginationLinks buildLinks(GetAccountsInput input,
                                      int totalPages,
                                      String paginationKey) {
        log.debug("Building pagination links for page {} of {}, paginationKey: {}",
                input.page(), totalPages, paginationKey);

        String baseEndpoint = baseUrl + "/open-banking/accounts/v2/accounts";

        return PaginationLinks.builder()
                .selfLink(buildSelfLink(baseEndpoint, input, paginationKey))
                .firstLink(buildFirstLink(baseEndpoint, input, totalPages, paginationKey))
                .prevLink(buildPrevLink(baseEndpoint, input, totalPages, paginationKey))
                .nextLink(buildNextLink(baseEndpoint, input, totalPages, paginationKey))
                .lastLink(buildLastLink(baseEndpoint, input, totalPages, paginationKey))
                .build();
    }

    /**
     * Builds the self link (current page) - Always mandatory
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
            log.debug("First link not required - current page is first page or no pages");
            return null;
        }

        return buildLink(baseEndpoint, 1, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds the previous page link (mandatory if has previous page)
     */
    private String buildPrevLink(String baseEndpoint, GetAccountsInput input,
                                 int totalPages, String paginationKey) {
        if (input.page() <= 1 || totalPages == 0) {
            log.debug("Previous link not required - current page is first page or no pages");
            return null;
        }

        return buildLink(baseEndpoint, input.page() - 1, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds the next page link (mandatory if has next page)
     */
    private String buildNextLink(String baseEndpoint, GetAccountsInput input,
                                 int totalPages, String paginationKey) {
        if (input.page() >= totalPages || totalPages == 0) {
            log.debug("Next link not required - current page is last page or no pages");
            return null;
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
            log.debug("Last link not required - current page is last page or no pages");
            return null;
        }

        return buildLink(baseEndpoint, totalPages, input.pageSize(),
                input.accountType().orElse(null), paginationKey);
    }

    /**
     * Builds a complete link with all required parameters
     */
    private String buildLink(String baseEndpoint, int page, int pageSize,
                             AccountType accountType, String paginationKey) {
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

        String fullLink = baseEndpoint + "?" + params.toString();
        log.trace("Built link: {}", fullLink);
        return fullLink;
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
     * Normalizes the base URL removing trailing slash
     */
    private String normalizeBaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Validates if the base URL follows the expected pattern
     */
    public boolean isValidBaseUrl() {
        return baseUrl != null &&
                !baseUrl.trim().isEmpty() &&
                (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"));
    }

}