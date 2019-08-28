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
import { Modal, Button, ButtonVariant, InputGroup, Label, TextInput, Form } from "@patternfly/react-core";
import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';
import RosterState from 'domain/RosterState';
import { tenantOperations } from 'store/tenant';
import { connect } from 'react-redux';
import { AppState } from 'store/types';

interface StateProps {
  timezoneList: string[];
}

const mapStateToProps = (state: AppState, props: OwnProps) => ({
  ...props,
  timezoneList: state.tenantData.timezoneList
}); 

interface DispatchProps {
  addTenant: typeof tenantOperations.addTenant;
  refreshSupportedTimezones: typeof tenantOperations.refreshSupportedTimezones;
}

const mapDispatchToProps: DispatchProps = {
  addTenant: tenantOperations.addTenant,
  refreshSupportedTimezones: tenantOperations.refreshSupportedTimezones
}

interface OwnProps {
  isOpen: boolean;
  onClose: () => void;
}

type Props = StateProps & DispatchProps & OwnProps;

export const NewTenantFormModal: React.FC<Props> = (props) => {
  const { refreshSupportedTimezones } = props;
  React.useEffect(() => {
    refreshSupportedTimezones() 
  }, [refreshSupportedTimezones]);

  const [ timezone, setTimezone ] = React.useState<string | undefined>(undefined);
  return (
    <Modal
      title="Create Tenant"
      onClose={props.onClose}
      isOpen={props.isOpen}
      actions={
        [(
          <Button 
            aria-label="Close Modal"
            variant={ButtonVariant.tertiary}
            key={0}
            onClick={props.onClose}
          >
            Close
          </Button>
        ),
        (
          <Button aria-label="Save" key={2}>Save</Button>
        )
        ]
      }
      isSmall
    >
      <Form>
        <InputGroup>
          <Label>Name</Label>
          <TextInput aria-label="Name" />
        </InputGroup>
        <InputGroup>
          <Label>Schedule Start Date</Label>
          <TextInput type="date" aria-label="Schedule Start Date" />
        </InputGroup>
        <InputGroup>
          <Label>Draft Length (days)</Label>
          <TextInput type="number" aria-label="Draft Length" />
        </InputGroup>
        <InputGroup>
          <Label>Publish Notice (days)</Label>
          <TextInput type="number" aria-label="Publish Notice" />
        </InputGroup>
        <InputGroup>
          <Label>Publish Length (days)</Label>
          <TextInput defaultValue="7" type="number" aria-label="Publish Length" isDisabled />
        </InputGroup>
        <InputGroup>
          <Label>Rotation Length (days)</Label>
          <TextInput type="number" aria-label="Rotation Length" />
        </InputGroup>
        <InputGroup>
          <Label>Timezone</Label>
          <TypeaheadSelectInput
            emptyText="Select a timezone"
            defaultValue={timezone}
            options={props.timezoneList}
            optionToStringMap={s => s}
            onChange={s => setTimezone(s)}
          />
        </InputGroup>
      </Form>
    </Modal>
  );
};

export default connect(mapStateToProps, mapDispatchToProps)(NewTenantFormModal);