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

import * as React from 'react';
import { AppState } from 'store/types';
import { Text, Level, LevelItem, Pagination, Button } from '@patternfly/react-core';
import { connect } from 'react-redux';
import { DataTableUrlProps } from 'ui/components/DataTable';
import { Stream } from 'util/ImmutableCollectionOperations';
import { stringFilter } from 'util/CommonFilters';
import { Tenant } from 'domain/Tenant';
import { tenantOperations } from 'store/tenant';
import * as adminOperations from 'store/admin/operations';
import FilterComponent from 'ui/components/FilterComponent';
import { Table, IRow, TableHeader, TableBody } from '@patternfly/react-table';
import { TrashIcon } from '@patternfly/react-icons';
import { useTranslation } from 'react-i18next';
import { ConfirmDialog } from 'ui/components/ConfirmDialog';
import { getPropsFromUrl, setPropsInUrl } from 'util/BookmarkableUtils';
import { withRouter, RouteComponentProps } from 'react-router';
import NewTenantFormModal from './NewTenantFormModal';

interface StateProps {
  tenantList: Tenant[];
}

const mapStateToProps = (state: AppState): StateProps => ({
  tenantList: state.tenantData.tenantList,
});

export interface DispatchProps {
  removeTenant: typeof tenantOperations.removeTenant;
  resetApplication: typeof adminOperations.resetApplication;
}

const mapDispatchToProps: DispatchProps = {
  removeTenant: tenantOperations.removeTenant,
  resetApplication: adminOperations.resetApplication,
};

export type Props = StateProps & DispatchProps & RouteComponentProps;
export interface State {
  isEditingOrCreatingTenant: boolean;
}

export const AdminPage: React.FC<Props> = (props) => {
  const { tenantList } = props;
  const { t } = useTranslation('AdminPage');
  const [isCreatingTenant, setIsCreatingTenant] = React.useState(false);
  const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);

  const urlProps = getPropsFromUrl<DataTableUrlProps>(props, {
    page: '1',
    itemsPerPage: '10',
    filter: null,
    sortBy: null,
    asc: 'true',
  });

  const filterText = urlProps.filter || '';
  const page = parseInt(urlProps.page as string, 10);
  const itemsPerPage = parseInt(urlProps.itemsPerPage as string, 10);
  const filter = stringFilter((tenant: Tenant) => tenant.name)(filterText);
  const filteredRows = new Stream(tenantList)
    .filter(filter);

  const numOfFilteredRows = filteredRows.collect(c => c.length);

  const rowsInPage = filteredRows
    .page(page, itemsPerPage)
    .collect(c => c);

  return (
    <>
      <Button
        style={{ width: 'min-content' }}
        aria-label="Reset Application"
        data-cy="reset-application"
        variant="danger"
        onClick={() => setIsResetDialogOpen(true)}
      >
        {t('resetApplication')}
      </Button>
      <ConfirmDialog
        title={t('confirmResetTitle')}
        isOpen={isResetDialogOpen}
        onClose={() => setIsResetDialogOpen(false)}
        onConfirm={() => props.resetApplication()}
      >
        {t('confirmResetBody')}
      </ConfirmDialog>

      <Level
        gutter="sm"
        style={{
          padding: '5px 5px 5px 5px',
          backgroundColor: 'var(--pf-global--BackgroundColor--200)',
        }}
      >
        <LevelItem>
          <FilterComponent
            aria-label="Filter by Name"
            filterText={urlProps.filter || ''}
            onChange={newFilterText => setPropsInUrl<DataTableUrlProps>(props, { page: '1', filter: newFilterText })}
          />
        </LevelItem>
        <LevelItem style={{ display: 'flex' }}>
          <Button
            aria-label="Add Tenant"
            data-cy="add-tenant"
            onClick={() => setIsCreatingTenant(true)}
          >
            {t('add')}
          </Button>
          <Pagination
            aria-label="Change Page"
            itemCount={numOfFilteredRows}
            perPage={itemsPerPage}
            page={page}
            onSetPage={(e, newPage) => setPropsInUrl<DataTableUrlProps>(props, { page: String(newPage) })}
            widgetId="pagination-options-menu-top"
            onPerPageSelect={(e, newItemsPerPage) => setPropsInUrl<DataTableUrlProps>(props, {
              itemsPerPage: String(newItemsPerPage),
            })}
          />
        </LevelItem>
      </Level>
      <NewTenantFormModal
        aria-label="Add Tenant Modal"
        isOpen={isCreatingTenant}
        onClose={() => setIsCreatingTenant(false)}
      />
      <Table
        caption={t('tenants')}
        cells={[t('name'), '']}
        rows={
          rowsInPage.map<IRow>(tenant => (
            {
              cells: [
                (<td key={0}><Text>{tenant.name}</Text></td>),
                (
                  <td key={1}>
                    <span
                      style={{
                        display: 'grid',
                        gridTemplateColumns: '1fr auto',
                        gridColumnGap: '5px',
                      }}
                    >
                      <span />
                      <Button variant="danger" onClick={() => props.removeTenant(tenant)}>
                        <TrashIcon />
                      </Button>
                    </span>
                  </td>
                ),
              ],
            }))
        }
      >
        <TableHeader />
        <TableBody />
      </Table>
    </>
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(AdminPage));
