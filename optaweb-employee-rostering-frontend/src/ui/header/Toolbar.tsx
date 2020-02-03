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

import {
  Button,
  ButtonVariant,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { BellIcon, CogIcon, BookIcon } from '@patternfly/react-icons';
import { Tenant } from 'domain/Tenant';
import * as React from 'react';
import { connect } from 'react-redux';
import { tenantOperations } from 'store/tenant';
import { AppState } from 'store/types';
import { withRouter, RouteComponentProps } from 'react-router-dom';

interface StateProps {
  currentTenantId: number;
  tenantList: Tenant[];
}

const mapStateToProps = ({ tenantData }: AppState): StateProps => ({
  currentTenantId: tenantData.currentTenantId,
  tenantList: tenantData.tenantList,
});

interface ToolbarState {
  isTenantSelectOpen: boolean;
}

export interface DispatchProps {
  refreshTenantList: typeof tenantOperations.refreshTenantList;
  changeTenant: typeof tenantOperations.changeTenant;
}

const mapDispatchToProps: DispatchProps = {
  refreshTenantList: tenantOperations.refreshTenantList,
  changeTenant: tenantOperations.changeTenant,
};

export type Props = RouteComponentProps & StateProps & DispatchProps;

export class ToolbarComponent extends React.Component<Props, ToolbarState> {
  constructor(props: Props) {
    super(props);
    this.state = { isTenantSelectOpen: false };
  }

  componentDidMount() {
    this.props.refreshTenantList();
  }

  setCurrentTenant(newTenantId: number) {
    this.setState({
      isTenantSelectOpen: false,
    });
    this.props.changeTenant({ tenantId: newTenantId, routeProps: this.props });
  }

  setIsTenantSelectOpen(isOpen: boolean) {
    this.setState({ isTenantSelectOpen: isOpen });
  }

  render() {
    const bellAndCog = (
      <ToolbarGroup>
        <ToolbarItem>
          <Button
            aria-label="Notifications"
            variant={ButtonVariant.plain}
          >
            <BellIcon />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            aria-label="Settings"
            variant={ButtonVariant.plain}
            data-cy="settings"
            onClick={() => this.props.history.push('/admin')}
          >
            <CogIcon />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <a
            aria-label="REST Reference"
            href={`${process.env.REACT_APP_BACKEND_URL}/swagger-ui.html`}
          >
            <Button
              variant={ButtonVariant.plain}
            >
              <BookIcon />
            </Button>
          </a>
        </ToolbarItem>
      </ToolbarGroup>
    );
    const { tenantList, currentTenantId } = this.props;
    const { isTenantSelectOpen } = this.state;
    if (tenantList.length === 0) {
      return (
        <Toolbar>
          <ToolbarGroup />
          {bellAndCog}
        </Toolbar>
      );
    }

    const currentTenant = tenantList.find(t => t.id === currentTenantId) as Tenant;
    return (
      <Toolbar>
        <ToolbarGroup>
          <ToolbarItem>
            <Dropdown
              isPlain
              position="right"
              onSelect={event => event && this.setCurrentTenant(
                parseInt((event.target as HTMLElement).dataset.tenantid as string, 10),
              )}
              isOpen={isTenantSelectOpen}
              toggle={(
                <DropdownToggle onToggle={() => this.setIsTenantSelectOpen(!isTenantSelectOpen)}>
                  {currentTenant.name}
                </DropdownToggle>
              )}
              dropdownItems={tenantList.map(tenant => (
                <DropdownItem data-tenantid={tenant.id} key={tenant.id}>
                  {tenant.name}
                </DropdownItem>
              ))}
            />
          </ToolbarItem>
        </ToolbarGroup>
        {bellAndCog}
      </Toolbar>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(ToolbarComponent));
