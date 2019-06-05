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
import { BellIcon, CogIcon } from '@patternfly/react-icons';
import * as React from 'react';
import Tenant from '../../domain/Tenant';
import TenantRestServiceClient from '../../services/TenantRestServiceClient';
import { isUndefined } from "util";

interface ToolbarProps { }
interface ToolbarState {
  currentTenant?: Tenant;
  tenantList: Tenant[];
  isTenantSelectOpen: boolean;
}

class ToolbarComponent extends React.Component<ToolbarProps, ToolbarState> {
  tenantRestService: TenantRestServiceClient = new TenantRestServiceClient();
  toolbarState: ToolbarState;

  constructor(props: ToolbarProps) {
    super(props);
    this.toolbarState = { tenantList: [], isTenantSelectOpen: false };
    this.updateTenantList();
  }

  updateTenantList() {
    this.tenantRestService.getTenantList().then(tenantList => {
      if (tenantList.length > 0) {
        if (isUndefined(this.toolbarState.currentTenant) || !tenantList.includes(this.toolbarState.currentTenant)) {
          this.toolbarState = {
            currentTenant: tenantList[0],
            tenantList: tenantList,
            isTenantSelectOpen: this.toolbarState.isTenantSelectOpen
          };
        }
        else {
          this.toolbarState = {
            currentTenant: this.toolbarState.currentTenant,
            tenantList: tenantList,
            isTenantSelectOpen: this.toolbarState.isTenantSelectOpen
          };
        }
      }
      else {
        this.toolbarState = { tenantList: [], isTenantSelectOpen: this.toolbarState.isTenantSelectOpen };
      }
      this.refresh();
    });
  }

  setCurrentTenant(newTenantId: number) {
    this.toolbarState = {
      currentTenant: this.toolbarState.tenantList[newTenantId],
      tenantList: this.toolbarState.tenantList,
      isTenantSelectOpen: false
    };
    this.refresh();
  }

  setIsTenantSelectOpen(isOpen: boolean) {
    this.toolbarState = { currentTenant: this.toolbarState.currentTenant, tenantList: this.toolbarState.tenantList, isTenantSelectOpen: isOpen };
    this.refresh();
  }

  refresh() {
    this.setState(this.toolbarState);
  }

  render() {
    if (isUndefined(this.toolbarState.currentTenant)) {
      return <Toolbar><ToolbarGroup /> <ToolbarGroup>
        <ToolbarItem>
          <Button
            id="horizontal-example-uid-01"
            aria-label="Notifications actions"
            variant={ButtonVariant.plain}
          >
            <BellIcon />
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            id="horizontal-example-uid-02"
            aria-label="Settings actions"
            variant={ButtonVariant.plain}
          >
            <CogIcon />
          </Button>
        </ToolbarItem>
      </ToolbarGroup></Toolbar>
    }
    else {
      let { currentTenant, tenantList, isTenantSelectOpen } = this.toolbarState;
      return <Toolbar>
        <ToolbarGroup>
          <ToolbarItem>
            <Dropdown
              isPlain={true}
              position="right"
              // tslint:disable-next-line:no-console
              onSelect={event => this.setCurrentTenant(parseInt(event.currentTarget.id))}
              isOpen={isTenantSelectOpen}
              toggle={
                <DropdownToggle onToggle={() => this.setIsTenantSelectOpen(!isTenantSelectOpen)}>
                  {currentTenant.name}
                </DropdownToggle>}
              dropdownItems={tenantList.map((tenant, index) => {
                return <DropdownItem id={index.toString()} key={index}>{tenant.name}</DropdownItem>;
              })}
            />
          </ToolbarItem>
        </ToolbarGroup>
        <ToolbarGroup>
          <ToolbarItem>
            <Button
              id="horizontal-example-uid-01"
              aria-label="Notifications actions"
              variant={ButtonVariant.plain}
            >
              <BellIcon />
            </Button>
          </ToolbarItem>
          <ToolbarItem>
            <Button
              id="horizontal-example-uid-02"
              aria-label="Settings actions"
              variant={ButtonVariant.plain}
            >
              <CogIcon />
            </Button>
          </ToolbarItem>
        </ToolbarGroup>
      </Toolbar>
    }
  }
}

export default ToolbarComponent;
