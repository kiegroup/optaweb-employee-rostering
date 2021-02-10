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
import { Text, Button } from '@patternfly/react-core';
import { DataTableUrlProps, setSorterInUrl, TableCell, TableRow, DataTable } from 'ui/components/DataTable';
import { Tenant } from 'domain/Tenant';
import { TrashIcon } from '@patternfly/react-icons';
import { useTranslation } from 'react-i18next';
import { ConfirmDialog } from 'ui/components/ConfirmDialog';
import { getPropsFromUrl } from 'util/BookmarkableUtils';
import { withRouter, RouteComponentProps } from 'react-router';
import { usePageableData } from 'util/FunctionalComponentUtils';
import { stringSorter } from 'util/CommonSorters';
import { tenantOperations, tenantSelectors } from 'store/tenant';
import { useDispatch, useSelector } from 'react-redux';
import { resetApplication } from 'store/admin/operations';
import NewTenantFormModal from './NewTenantFormModal';

export type Props = RouteComponentProps;
export interface State {
  isEditingOrCreatingTenant: boolean;
}

export const TenantRow = (tenant: Tenant) => {
  const currentTenantId = useSelector(tenantSelectors.getTenantId);
  const dispatch = useDispatch();
  const { t } = useTranslation('AdminPage');
  return (
    <TableRow>
      <TableCell columnName={t('name')}>
        <Text>{tenant.name}</Text>
      </TableCell>
      <TableCell columnName="">
        <span
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr auto',
            gridColumnGap: '5px',
          }}
        >
          <span />
          <span title={(currentTenantId === tenant.id) ? t('cannotDeleteCurrentTenant') : undefined}>
            <Button
              variant="danger"
              onClick={() => dispatch(tenantOperations.removeTenant(tenant))}
              isDisabled={currentTenantId === tenant.id}
            >
              <TrashIcon />
            </Button>
          </span>
        </span>
      </TableCell>
    </TableRow>
  );
};

export const AdminPage: React.FC<Props> = (props) => {
  const tenantList = useSelector(tenantSelectors.getTenantList);
  const dispatch = useDispatch();
  const { t } = useTranslation('AdminPage');
  const [isCreatingTenant, setIsCreatingTenant] = React.useState(false);
  const [isResetDialogOpen, setIsResetDialogOpen] = React.useState(false);

  const urlProps = getPropsFromUrl<DataTableUrlProps>(props, {
    page: '1',
    itemsPerPage: '10',
    filter: null,
    sortBy: '0',
    asc: 'true',
  });

  const sortBy = parseInt(urlProps.sortBy || '-1', 10);
  const pageableData = usePageableData(urlProps, tenantList.toArray(), tenant => [tenant.name],
    stringSorter<Tenant>(tenant => tenant.name));

  const columns = [
    { name: 'Name', sorter: stringSorter<Tenant>(tenant => tenant.name) },
  ];

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
        onConfirm={() => dispatch(resetApplication())}
      >
        {t('confirmResetBody')}
      </ConfirmDialog>
      <NewTenantFormModal
        aria-label="Add Tenant Modal"
        isOpen={isCreatingTenant}
        onClose={() => setIsCreatingTenant(false)}
      />
      <DataTable
        {...props}
        {...pageableData}
        title={t('tenants')}
        columns={columns}
        sortByIndex={sortBy}
        onSorterChange={index => setSorterInUrl(props, urlProps, sortBy, index)}
        onAddButtonClick={() => setIsCreatingTenant(true)}
        rowWrapper={tenant => <TenantRow {...tenant} />}
      />
    </>
  );
};

export default withRouter(AdminPage);
