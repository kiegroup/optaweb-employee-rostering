package org.optaweb.employeerostering.domain.roster;

import static java.lang.Math.max;

public class Pagination {

    private final Integer pageNumber;
    private final Integer numberOfItemsPerPage;

    /**
     * @param pageNumber null to retrieve all items
     * @param numberOfItemsPerPage null to retrieve all items
     * @return never null
     */
    public static Pagination of(final Integer pageNumber,
            final Integer numberOfItemsPerPage) {
        if (pageNumber == null) {
            if (numberOfItemsPerPage == null) {
                return new Pagination(0, Integer.MAX_VALUE);
            } else {
                throw new IllegalStateException("When pageNumber (" + pageNumber
                        + ") is null, then numberOfItemsPerPage ("
                        + numberOfItemsPerPage + ") must be null too.");
            }
        } else if (numberOfItemsPerPage == null) {
            throw new IllegalStateException("When numberOfItemsPerPage (" + numberOfItemsPerPage
                    + ") is null, then pageNumber (" + pageNumber
                    + ") must be null too.");
        }
        return new Pagination(pageNumber, numberOfItemsPerPage);
    }

    private Pagination(final Integer pageNumber,
            final Integer numberOfItemsPerPage) {
        this.pageNumber = pageNumber;
        this.numberOfItemsPerPage = numberOfItemsPerPage;
    }

    public Integer getNumberOfItemsPerPage() {
        return numberOfItemsPerPage;
    }

    public Integer getFirstResultIndex() {
        return pageNumber * numberOfItemsPerPage;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Pagination nextPage() {
        return new Pagination(pageNumber + 1, numberOfItemsPerPage);
    }

    public Pagination previousPage() {
        return new Pagination(max(0, pageNumber - 1), numberOfItemsPerPage);
    }

    public Pagination withNumberOfItemsPerPage(final Integer numberOfItemsPerPage) {
        return new Pagination(pageNumber, numberOfItemsPerPage);
    }

    public boolean isOnFirstPage() {
        return pageNumber == 0;
    }
}
