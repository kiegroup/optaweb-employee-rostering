/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
