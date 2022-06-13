import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { ConfirmDialogProps, ConfirmDialog } from './ConfirmDialog';

describe('ConfirmDialog component', () => {
  it('should render correctly when closed', () => {
    const confirmDialogComponent = shallow(<ConfirmDialog {...confirmDialogProps} isOpen={false}>Body</ConfirmDialog>);
    expect(toJson(confirmDialogComponent)).toMatchSnapshot();
  });

  it('should render correctly when opened', () => {
    const confirmDialogComponent = shallow(<ConfirmDialog {...confirmDialogProps}>Body</ConfirmDialog>);
    expect(toJson(confirmDialogComponent)).toMatchSnapshot();
  });

  it('should call onClose when close or the cross is clicked', () => {
    const confirmDialogComponent = shallow(<ConfirmDialog {...confirmDialogProps}>Body</ConfirmDialog>);
    confirmDialogComponent.simulate('close');
    expect(confirmDialogProps.onClose).toBeCalled();
    expect(confirmDialogProps.onConfirm).not.toBeCalled();

    jest.resetAllMocks();
    mount(confirmDialogComponent.prop('actions')[0]).simulate('click');
    expect(confirmDialogProps.onClose).toBeCalled();
    expect(confirmDialogProps.onConfirm).not.toBeCalled();
  });

  it('should call both onClose and onConfirm when the confirm button is clicked', () => {
    const confirmDialogComponent = shallow(<ConfirmDialog {...confirmDialogProps}>Body</ConfirmDialog>);
    mount(confirmDialogComponent.prop('actions')[1]).simulate('click');
    expect(confirmDialogProps.onClose).toBeCalled();
    expect(confirmDialogProps.onConfirm).toBeCalled();
  });
});

const confirmDialogProps: ConfirmDialogProps = {
  title: 'Confirm Dialog Title',
  isOpen: true,
  onConfirm: jest.fn(),
  onClose: jest.fn(),
};
