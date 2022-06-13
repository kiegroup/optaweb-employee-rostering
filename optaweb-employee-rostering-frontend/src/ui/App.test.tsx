import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import App from './App';

describe('App', () => {
  it('should render correctly on a desktop', () => {
    const mediaQuery = jest.requireMock('react-responsive');
    mediaQuery.useMediaQuery.mockReturnValueOnce(false);
    const app = shallow(<App />);

    expect(mediaQuery.useMediaQuery).toBeCalledWith({ maxWidth: 1399 });
    expect(toJson(app)).toMatchSnapshot();
  });

  it('should render correctly on a mobile', () => {
    const mediaQuery = jest.requireMock('react-responsive');
    mediaQuery.useMediaQuery.mockReturnValueOnce(true);
    const app = shallow(<App />);

    expect(mediaQuery.useMediaQuery).toBeCalledWith({ maxWidth: 1399 });
    expect(toJson(app)).toMatchSnapshot();
  });

  it('clicking the nav button should show the sidebar', () => {
    const mediaQuery = jest.requireMock('react-responsive');
    mediaQuery.useMediaQuery.mockReturnValueOnce(true);
    const app = shallow(<App />);

    expect(mediaQuery.useMediaQuery).toBeCalledWith({ maxWidth: 1399 });
    shallow(app.prop('header')).simulate('navToggle');
    expect(toJson(app)).toMatchSnapshot();
  });

  it('clicking the nav button twice should hide the sidebar', () => {
    const mediaQuery = jest.requireMock('react-responsive');
    mediaQuery.useMediaQuery.mockReturnValueOnce(true);
    const app = shallow(<App />);

    expect(mediaQuery.useMediaQuery).toBeCalledWith({ maxWidth: 1399 });
    shallow(app.prop('header')).simulate('navToggle');
    shallow(app.prop('header')).simulate('navToggle');
    expect(toJson(app)).toMatchSnapshot();
  });
});
