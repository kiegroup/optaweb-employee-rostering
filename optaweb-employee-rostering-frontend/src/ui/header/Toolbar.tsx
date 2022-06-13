
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
import { List } from 'immutable';

interface StateProps {
  currentTenantId: number;
  tenantList: List<Tenant>;
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
    const currentTenantName = tenantList.find(t => t.id === currentTenantId)?.name ?? 'TENANT ERROR';
    const tenantDropdown = (tenantList.size > 0) ? (
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
                {currentTenantName}
              </DropdownToggle>
            )}
            dropdownItems={tenantList.map(tenant => (
              <DropdownItem data-tenantid={tenant.id} key={tenant.id}>
                {tenant.name}
              </DropdownItem>
            )).toArray()}
          />
        </ToolbarItem>
      </ToolbarGroup>
    ) : <ToolbarGroup />;
    return (
      <Toolbar
        style={{
          backgroundColor: '#0000', // transparent background for background image
        }}
      >
        {tenantDropdown}
        {bellAndCog}
      </Toolbar>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(ToolbarComponent));
