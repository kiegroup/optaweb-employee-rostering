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
import { Predicate } from "types";
import { stringFilter } from 'util/CommonFilters';
import Tenant from 'domain/Tenant';
import { tenantOperations } from 'store/tenant';
import FilterComponent from 'ui/components/FilterComponent';
import { Table, IRow, TableHeader, TableBody } from '@patternfly/react-table';
import { TrashIcon } from '@patternfly/react-icons';
import NewTenantFormModal from './NewTenantFormModal';
import { useTranslation } from 'react-i18next';

interface StateProps {
  tenantList: Tenant[];
}

const mapStateToProps = (state: AppState): StateProps => ({
  tenantList: state.tenantData.tenantList
}); 

export interface DispatchProps {
  removeTenant: typeof tenantOperations.removeTenant;
}

const mapDispatchToProps: DispatchProps = {
  removeTenant: tenantOperations.removeTenant
};

export type Props = StateProps & DispatchProps;
export interface State {
  isEditingOrCreatingTenant: boolean;
}

export const AdminPage: React.FC<Props> = (props) => {
  const { tenantList } = props;
  const { t } = useTranslation();
  const [ page, setPage ] = React.useState(1);
  const [ perPage, setPerPage ] = React.useState(10);
  const [ filter, setFilter ] = React.useState<Predicate<Tenant>>(() => () => true);
  const [ isCreatingTenant, setIsCreatingTenant ] = React.useState(false);

  const filteredRows = tenantList.filter(filter);
  const rowsInPage = filteredRows.filter((v, i) => (page - 1) * perPage <= i && i < page * perPage);

  return (
    <>
      <Level
        gutter="sm"
        style={{
          padding: "5px 5px 5px 5px",
          backgroundColor: "var(--pf-global--BackgroundColor--200)"
        }}
      >
        <LevelItem>
          <FilterComponent
            aria-label="Filter by Name"
            filter={stringFilter((t: Tenant) => t.name)}
            onChange={f => setFilter(() => f)}
          />
        </LevelItem>
        <LevelItem style={{ display: "flex" }}>
          <Button aria-label="Add Tenant" onClick={() => setIsCreatingTenant(true)}>{t("add")}</Button>
          <Pagination
            aria-label="Change Page"
            itemCount={filteredRows.length}
            perPage={perPage}
            page={page}
            onSetPage={(e, page) => setPage(page)}
            widgetId="pagination-options-menu-top"
            onPerPageSelect={(e, perPage) => setPerPage(perPage)}
          />
        </LevelItem>
      </Level>
      <NewTenantFormModal
        aria-label="Add Tenant Modal"
        isOpen={isCreatingTenant}
        onClose={() => setIsCreatingTenant(false)}
      />
      <Table
        caption={t("tenants")}
        cells={[t("name"), ""]}
        rows={
          rowsInPage.map<IRow>(tenant => (
            {
              cells: [
                (<td key={0}><Text>{tenant.name}</Text></td>),
                (
                  <td key={1}>
                    <span
                      style={{ 
                        display: "grid",
                        gridTemplateColumns: "1fr auto",
                        gridColumnGap: "5px"
                      }}
                    >
                      <span />
                      <Button variant="danger" onClick={() => props.removeTenant(tenant)}>
                        <TrashIcon />
                      </Button>
                    </span>
                  </td>
                )
              ]
            }))
        }
      >
        <TableHeader />
        <TableBody />
      </Table>
    </>
  );
}

export default connect(mapStateToProps, mapDispatchToProps)(AdminPage);