package com.openfinance.usecase.pagination;

public interface IPaginationRequest {

    /**
     * Gets the current page number
     * @return page number (starting from 1)
     */
    int page();

    /**
     * Gets the page size
     * @return number of records per page
     */
    int pageSize();

    /**
     * Gets additional query parameters for link building
     * This method should return a string with URL-encoded parameters
     * without the leading "?" or "&"
     *
     * Example: "accountType=CONTA_DEPOSITO_A_VISTA&status=ACTIVE"
     *
     * @return additional parameters or null/empty if none
     */
    default String getAdditionalParameters() {
        return null;
    }
}
