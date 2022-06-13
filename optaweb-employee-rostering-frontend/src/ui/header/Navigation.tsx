
import { Nav, NavItem, NavList, NavProps } from '@patternfly/react-core';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { RouteComponentProps } from 'react-router';
import { Link, withRouter } from 'react-router-dom';
import { AppState } from 'store/types';
import { connect } from 'react-redux';

interface StateProps {
  tenantId: number;
}

const mapStateToProps = (state: AppState): StateProps => ({
  tenantId: state.tenantData.currentTenantId,
});

export type NavigationProps = RouteComponentProps & StateProps & Pick<NavProps, 'variant'>;

export const Navigation = ({ tenantId, location, variant }: NavigationProps) => {
  const { t } = useTranslation('Navigation');
  return (
    <Nav aria-label="Nav" variant={variant}>
      <NavList>
        {['skills', 'spots', 'contracts', 'employees', 'availability', 'rotation', 'shift', 'adjust'].map((link) => {
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
