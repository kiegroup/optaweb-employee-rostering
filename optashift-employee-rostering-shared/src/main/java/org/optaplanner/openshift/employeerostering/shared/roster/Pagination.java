/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.shared.roster;

import static java.lang.Math.max;

public class Pagination {

    private final Integer pageNumber;
    private final Integer numberOfItemsPerPage;

    public static Pagination of(final Integer pageNumber,
                                final Integer numberOfItemsPerPage) {

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
}
