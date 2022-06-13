import React from 'react';
import { shallow } from 'enzyme';
import { Popover } from '@patternfly/react-core';
import { ColorPickerProps, ColorPicker } from './ColorPicker';

describe('ColorPicker component', () => {
  const baseProps: ColorPickerProps = {
    currentColor: '#ff0000',
    onChangeColor: jest.fn(),
  };
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render correctly', () => {
    const colorPicker = shallow(<ColorPicker {...baseProps} />);
    expect(colorPicker).toMatchSnapshot();
  });

  it('should set color on click', () => {
    const colorPicker = shallow(<ColorPicker {...baseProps} />);
    const newColor = '#0000ff';
    shallow(colorPicker.find(Popover).prop('bodyContent') as React.ReactElement)
      .simulate('change', newColor);

    expect(baseProps.onChangeColor).toBeCalledWith(newColor);
  });
});
