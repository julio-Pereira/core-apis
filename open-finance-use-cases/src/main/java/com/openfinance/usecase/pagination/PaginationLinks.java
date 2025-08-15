package com.openfinance.usecase.pagination;


/**
 * Represents pagination links for navigating through paginated resources.
 * This record contains links to the current page, first page, previous page,
 * next page, and last page.
 */
public record PaginationLinks(
        String selfLink,
        String firstLink,
        String prevLink,
        String nextLink,
        String lastLink
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String selfLink;
        private String firstLink;
        private String prevLink;
        private String nextLink;
        private String lastLink;

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

        public PaginationLinks build() {
            return new PaginationLinks(selfLink, firstLink, prevLink, nextLink, lastLink);
        }
    }
}
