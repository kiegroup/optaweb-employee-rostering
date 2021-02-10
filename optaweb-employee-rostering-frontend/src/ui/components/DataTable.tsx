/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { PropsWithChildren, useState } from 'react';
import { Button, Pagination, Level, LevelItem } from '@patternfly/react-core';
import { SaveIcon, CloseIcon, EditIcon, TrashIcon } from '@patternfly/react-icons';
import { Sorter } from 'types';
import { useTranslation } from 'react-i18next';
import { setPropsInUrl, UrlProps } from 'util/BookmarkableUtils';
import { RouteComponentProps } from 'react-router';
import { PaginationData } from 'util/FunctionalComponentUtils';
import { TableComposable, Caption, Thead, Tbody, Th, Tr, Td } from '@patternfly/react-table';
import FilterComponent from './FilterComponent';

export type DataTableUrlProps = UrlProps<'page'|'itemsPerPage'|'filter'|'sortBy'|'asc'>;

export const RowViewButtons = (props: { onEdit: () => void; onDelete: () => void }) => (
  <Td role="cell">
    <span
      style={{
        display: 'grid',
        gridTemplateColumns: '1fr auto auto',
        gridColumnGap: '5px',
      }}
    >
      <span />
      <span>
        <Button
          variant="link"
          onClick={props.onEdit}
        >
          <EditIcon />
        </Button>
      </span>
      <span>
        <Button
          variant="danger"
          onClick={props.onDelete}
        >
          <TrashIcon />
        </Button>
      </span>
    </span>
  </Td>
);

export const RowEditButtons = (props: { isValid: boolean; onClose: () => void; onSave: () => void }) => (
  <Td role="cell">
    <span
      style={{
        display: 'grid',
        gridTemplateColumns: '1fr auto auto',
        gridColumnGap: '5px',
      }}
    >
      <span />
      <span>
        <Button
          variant="link"
          isDisabled={!props.isValid}
          onClick={() => {
            props.onSave();
            props.onClose();
          }}
        >
          <SaveIcon />
        </Button>
      </span>
      <span>
        <Button
          variant="link"
          onClick={props.onClose}
        >
          <CloseIcon />
        </Button>
      </span>
    </span>
  </Td>
);

export const TableRow = (props: PropsWithChildren<{}>) => (
  <Tr role="row">
    {props.children}
  </Tr>
);

export const TableCell = (props: PropsWithChildren<{ columnName: string }>) => (
  <Td role="cell" dataLabel={props.columnName}>
    {props.children}
  </Td>
);

export const PaginationControls = (props: PaginationData<any> & RouteComponentProps & {
  isCreatingNewRow: boolean;
  onCreateNewRow: () => void;
}) => {
  const { t } = useTranslation();
  return (
    <Level
      hasGutter
      style={{
        padding: '5px 5px 5px 5px',
        backgroundColor: 'var(--pf-global--BackgroundColor--200)',
      }}
    >
      <LevelItem>
        <FilterComponent
          filterText={props.filterText}
          onChange={(newFilterText) => {
            setPropsInUrl(props, { page: '1', filter: newFilterText });
          }}
        />
      </LevelItem>
      <LevelItem style={{ display: 'flex' }}>
        <Button
          isDisabled={props.isCreatingNewRow}
          onClick={props.onCreateNewRow}
        >
          {t('add')}
        </Button>
        <Pagination
          itemCount={props.numOfFilteredRows}
          perPage={props.itemsPerPage}
          page={props.page}
          onSetPage={(e, newPage) => setPropsInUrl(props, { page: `${newPage}` })}
          widgetId="pagination-options-menu-top"
          onPerPageSelect={(e, newPerPage) => {
            const firstItemOnOldPage = props.itemsPerPage * (props.page - 1) + 1;
            const newPage = Math.ceil(firstItemOnOldPage / newPerPage);
            setPropsInUrl(props, { page: `${newPage}`, itemsPerPage: `${newPerPage}` });
          }}
        />
      </LevelItem>
    </Level>
  );
};

export function setSorterInUrl(props: RouteComponentProps, urlProps: { asc: string | null },
  oldIndex: number, newIndex: number) {
  if (oldIndex === newIndex) {
    setPropsInUrl(props, {
      asc: (urlProps.asc === 'true') ? 'false' : 'true',
    });
  } else {
    setPropsInUrl(props, {
      sortBy: `${newIndex}`,
      asc: 'true',
    });
  }
}
export interface DataTableProps<T> {
  title: string;
  columns: { name: string; sorter?: Sorter<T>}[];
  sortByIndex: number;
  rowWrapper: (item: T) => JSX.Element;
  newRowWrapper?: (removeRow: () => void) => JSX.Element;
  onSorterChange: (sortByColumnIndex: number) => void;
  // Only use this if you want to override add button behavior to NOT inline edit a row
  onAddButtonClick?: () => void;
}

export const DataTable = (props: PaginationData<any> & RouteComponentProps & DataTableProps<any>) => {
  const [isCreatingNewRow, setIsCreatingNewRow] = useState(false);

  return (
    <>
      <PaginationControls
        {...props}
        isCreatingNewRow={isCreatingNewRow}
        onCreateNewRow={() => {
          if (props.onAddButtonClick) {
            props.onAddButtonClick();
          } else {
            setIsCreatingNewRow(true);
          }
        }}
      />
      <TableComposable>
        <Caption>{props.title}</Caption>
        <Thead>
          <Tr role="row">
            {props.columns.map((header, index) => {
              const sorterProps = (header.sorter) ? {
                sort: {
                  sortBy: {
                    index: props.sortByIndex,
                    direction: (props.isReversed ? 'desc' : 'asc') as 'desc' | 'asc',
                  },
                  onSort: () => props.onSorterChange(index),
                  columnIndex: index,
                },
              } : {};
              return (
                <Th
                  key={header.name}
                  role="columnheader"
                  scope="col"
                  {...sorterProps}
                >
                  {header.name}
                </Th>
              );
            })}
            <Th role="columnheader" scope="col" />
          </Tr>
        </Thead>
        <Tbody>
          {(isCreatingNewRow && props.newRowWrapper) ? props.newRowWrapper(() => {
            setIsCreatingNewRow(false);
          }) : undefined}
          {props.rowsInPage.map(item => props.rowWrapper(item)).toArray()}
        </Tbody>
      </TableComposable>
    </>
  );
};
