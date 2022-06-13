import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { mockStore } from 'store/mockStore';
import { ConnectionStatus } from './ConnectionStatus';

describe('ConnectionStatus component', () => {
  it('should render correctly when not connected', () => {
    mockStore({ isConnected: false });
    const confirmDialogComponent = shallow(<ConnectionStatus />);
    expect(toJson(confirmDialogComponent)).toMatchSnapshot();
  });

  it('should render correctly when connected', () => {
    mockStore({ isConnected: true });
    const confirmDialogComponent = shallow(<ConnectionStatus />);
    expect(toJson(confirmDialogComponent)).toMatchSnapshot();
  });
});
