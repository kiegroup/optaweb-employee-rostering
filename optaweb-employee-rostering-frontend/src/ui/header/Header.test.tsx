
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import Header from './Header';

describe('Header component', () => {
  it('should render correctly on a desktop', () => {
    const mediaQuery = jest.requireMock('react-responsive');
    mediaQuery.useMediaQuery.mockReturnValueOnce(true);

    const header = shallow(<Header {...props} />);

    expect(mediaQuery.useMediaQuery).toBeCalledWith({ minWidth: 1400 });
    expect(toJson(header)).toMatchSnapshot();
  });

  it('should render correctly on a mobile', () => {
    const mediaQuery = jest.requireMock('react-responsive');
    mediaQuery.useMediaQuery.mockReturnValueOnce(false);

    const header = shallow(<Header {...props} />);

    expect(mediaQuery.useMediaQuery).toBeCalledWith({ minWidth: 1400 });
    expect(toJson(header)).toMatchSnapshot();
  });
});

const props = {
  onNavToggle: jest.fn(),
};
