import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { act } from 'react-dom/test-utils';
import ReactDOM from 'react-dom';
import { NewTenantFormModal, Props } from './NewTenantFormModal';

describe('New Tenant Form Modal', () => {
  let container: any;

  beforeEach(() => {
    container = document.createElement('div');
    document.body.appendChild(container);
  });

  afterEach(() => {
    document.body.removeChild(container);
    container = null;
    jest.clearAllMocks();
  });

  it('should render correctly when opened', () => {
    const modal = shallow(<NewTenantFormModal {...baseProps} />);
    expect(toJson(modal)).toMatchSnapshot();
  });

  it('should render correctly when closed', () => {
    const modal = shallow(<NewTenantFormModal {...baseProps} isOpen={false} />);
    expect(toJson(modal)).toMatchSnapshot();
  });

  it('should call refreshSupportedTimezones when first rendered', async () => {
    act(() => {
      ReactDOM.render(<NewTenantFormModal {...baseProps} />, container);
    });
    expect(baseProps.refreshSupportedTimezones).toBeCalled();
  });

  it('should work correctly when you fill in the form and save', () => {
    const modal = shallow(<NewTenantFormModal {...baseProps} isOpen={false} />);
    act(() => {
      modal.find('[aria-label="Name"]').simulate('change', 'New Name');
      modal.find('[aria-label="Schedule Start Date"]').simulate('change', '2018-12-31');
      modal.find('[aria-label="Draft Length"]').simulate('change', 14);
      modal.find('[aria-label="Rotation Length"]').simulate('change', 21);
      modal.find('[aria-label="Publish Notice"]').simulate('change', 7);
      modal.find('[aria-label="Publish Length"]').simulate('change', 28);
      modal.find('[aria-label="Timezone"]').simulate('change', 'Timezone/2');
      mount(modal.prop('actions')[1]).simulate('click');
    });
    expect(baseProps.addTenant).toBeCalled();
    expect(baseProps.addTenant).toBeCalledWith({
      publishNotice: 7,
      draftLength: 14,
      rotationLength: 21,
      publishLength: 28,
      lastHistoricDate: new Date('2018-12-30'),
      firstDraftDate: new Date('2018-12-31'),
      timeZone: 'Timezone/2',
      unplannedRotationOffset: 0,
      tenant: {
        name: 'New Name',
      },
    });
  });

  it('should not submit an incomplete form', () => {
    const modal = shallow(<NewTenantFormModal {...baseProps} isOpen={false} />);
    act(() => {
      shallow(modal.prop('actions')[1]).simulate('click');
    });
    expect(baseProps.addTenant).not.toBeCalled();
    act(() => {
      modal.find('[aria-label="Name"]').simulate('change', 'New Name');
      modal.find('[aria-label="Rotation Length"]').simulate('change', 21);
      modal.find('[aria-label="Publish Notice"]').simulate('change', 7);
      modal.find('[aria-label="Publish Length"]').simulate('change', 28);
      modal.find('[aria-label="Timezone"]').simulate('change', 'Timezone/2');
      shallow(modal.prop('actions')[1]).simulate('click');
    });
    expect(baseProps.addTenant).not.toBeCalled();
  });
});

const baseProps: Props = {
  timezoneList: ['Timezone/1', 'Timezone/2', 'Timezone/3', 'UTC'],
  isOpen: true,
  addTenant: jest.fn(),
  refreshSupportedTimezones: jest.fn(),
  onClose: jest.fn(),
};
