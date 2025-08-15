package com.openfinance.usecase.pagination;

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
public class Pagination {

    private final String baseUrl;

    public Pagination(String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        log.debug("PaginationLinkBuilder initialized with baseUrl: {}", this.baseUrl);
    }

    /**
     * Builds all pagination links for any API response
     *
     * @param endpoint the full endpoint path (e.g., "/open-banking/accounts/v2/accounts")
     * @param request the pagination request containing page info and additional parameters
     * @param totalPages total number of pages available
     * @param paginationKey pagination key for operational control
     * @return complete pagination links
     */
    public PaginationLinks buildLinks(String endpoint,
                                      IPaginationRequest request,
                                      int totalPages,
                                      String paginationKey) {
        log.debug("Building pagination links for page {} of {}, endpoint: {}, paginationKey: {}",
                request.page(), totalPages, endpoint, paginationKey);

        String fullEndpoint = baseUrl + endpoint;

        return PaginationLinks.builder()
                .selfLink(buildSelfLink(fullEndpoint, request, paginationKey))
                .firstLink(buildFirstLink(fullEndpoint, request, totalPages, paginationKey))
                .prevLink(buildPrevLink(fullEndpoint, request, totalPages, paginationKey))
                .nextLink(buildNextLink(fullEndpoint, request, totalPages, paginationKey))
                .lastLink(buildLastLink(fullEndpoint, request, totalPages, paginationKey))
                .build();
    }

    /**
     * Builds the self link (current page) - Always mandatory
     */
    private String buildSelfLink(String endpoint, IPaginationRequest request, String paginationKey) {
        return buildLink(endpoint, request.page(), request.pageSize(),
                request.getAdditionalParameters(), paginationKey);
    }

    /**
     * Builds the first page link (mandatory if not first page)
     */
    private String buildFirstLink(String endpoint, IPaginationRequest request,
                                  int totalPages, String paginationKey) {
        if (request.page() == 1 || totalPages == 0) {
            log.debug("First link not required - current page is first page or no pages");
            return null;
        }

        return buildLink(endpoint, 1, request.pageSize(),
                request.getAdditionalParameters(), paginationKey);
    }

    /**
     * Builds the previous page link (mandatory if has previous page)
     */
    private String buildPrevLink(String endpoint, IPaginationRequest request,
                                 int totalPages, String paginationKey) {
        if (request.page() <= 1 || totalPages == 0) {
            log.debug("Previous link not required - current page is first page or no pages");
            return null;
        }

        return buildLink(endpoint, request.page() - 1, request.pageSize(),
                request.getAdditionalParameters(), paginationKey);
    }

    /**
     * Builds the next page link (mandatory if has next page)
     */
    private String buildNextLink(String endpoint, IPaginationRequest request,
                                 int totalPages, String paginationKey) {
        if (request.page() >= totalPages || totalPages == 0) {
            log.debug("Next link not required - current page is last page or no pages");
            return null;
        }

        return buildLink(endpoint, request.page() + 1, request.pageSize(),
                request.getAdditionalParameters(), paginationKey);
    }

    /**
     * Builds the last page link (mandatory if not last page)
     */
    private String buildLastLink(String endpoint, IPaginationRequest request,
                                 int totalPages, String paginationKey) {
        if (request.page() == totalPages || totalPages == 0) {
            log.debug("Last link not required - current page is last page or no pages");
            return null;
        }

        return buildLink(endpoint, totalPages, request.pageSize(),
                request.getAdditionalParameters(), paginationKey);
    }

    /**
     * Builds a complete link with all required parameters
     */
    private String buildLink(String endpoint, int page, int pageSize,
                             String additionalParameters, String paginationKey) {
        StringJoiner params = new StringJoiner("&");

        // Always include page and page-size
        params.add("page=" + page);
        params.add("page-size=" + pageSize);

        // Include additional parameters if present
        if (additionalParameters != null && !additionalParameters.trim().isEmpty()) {
            params.add(additionalParameters);
        }

        // Include pagination key for operational limits control
        if (paginationKey != null && !paginationKey.trim().isEmpty()) {
            params.add("pagination-key=" + urlEncode(paginationKey));
        }

        String fullLink = endpoint + "?" + params.toString();
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