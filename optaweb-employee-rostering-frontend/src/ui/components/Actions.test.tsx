import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { SizeMeProps } from 'react-sizeme';
import { Button } from '@patternfly/react-core';
import { Props, Actions } from './Actions';

describe('Actions component', () => {
  it('should render as all buttons when given enough space', () => {
    const actionsComponent = shallow(<Actions {...desktopProps} />);
    expect(toJson(actionsComponent)).toMatchSnapshot();
  });

  it('should render as both buttons and dropdown when given some space', () => {
    const actionsComponent = shallow(<Actions {...tabletProps} />);
    expect(toJson(actionsComponent)).toMatchSnapshot();
  });

  it('should render as all dropdown when given no space', () => {
    const actionsComponent = shallow(<Actions {...mobileProps} />);
    expect(toJson(actionsComponent)).toMatchSnapshot();
  });

  it('clicking on a button should call the action', () => {
    const actionsComponent = shallow(<Actions {...desktopProps} />);
    actionsComponent.find(Button).find('[aria-label="Action 1"]').simulate('click');
    expect(actions[0].action).toBeCalled();
  });

  it('clicking on a dropdown should call the action', () => {
    const actionsComponent = shallow(<Actions {...mobileProps} />);
    actionsComponent.find('Dropdown').simulate('select', { currentTarget: { innerText: 'Action 3' } });
    expect(actions[2].action).toBeCalled();
  });
});

const actions = [
  { name: 'Action 1', action: jest.fn() },
  { name: 'Action 2', action: jest.fn() },
  { name: 'Action 3', action: jest.fn() },
];

const desktopProps: Props & SizeMeProps = {
  actions,
  size: {
    width: 2000,
    height: 1500,
  },
};

const tabletProps: Props & SizeMeProps = {
  actions,
  size: {
    width: 400,
    height: 600,
  },
};

const mobileProps: Props & SizeMeProps = {
  actions,
  size: {
    width: 100,
    height: 150,
  },
};
