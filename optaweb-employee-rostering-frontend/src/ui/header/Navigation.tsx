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

import { Nav, NavItem, NavList, NavListProps } from '@patternfly/react-core';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { RouteComponentProps } from 'react-router';
import { Link, withRouter } from 'react-router-dom';
import { AppState } from 'store/types';
import { connect } from 'react-redux';

interface StateProps {
  tenantId: number;
}

const mapStateToProps = (state: AppState, ownProps: Pick<NavListProps, 'variant'>):
StateProps & Pick<NavListProps, 'variant'> => ({
  tenantId: state.tenantData.currentTenantId,
  variant: ownProps.variant,
});

export type NavigationProps = RouteComponentProps & StateProps & Pick<NavListProps, 'variant'>;

export const Navigation = ({ variant, tenantId, location }: NavigationProps) => {
  const { t } = useTranslation('Navigation');
  return (
    <Nav aria-label="Nav">
      <NavList variant={variant}>
        {['skills', 'wards', 'contracts', 'employees', 'availability', 'rotation', 'shift', 'adjust'].map((link) => {
          const itemId = link;
          const path = `/${tenantId}/${itemId}`;
          return (
            <NavItem
              key={itemId}
              itemId={itemId}
              isActive={location.pathname === path}
            >
              <Link to={path}>{t(link)}</Link>
            </NavItem>
          );
        })}
      </NavList>
    </Nav>
  );
};

export default connect(mapStateToProps)(withRouter(Navigation));
