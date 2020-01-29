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
import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { TextInput } from '@patternfly/react-core';
import OptionalInput, { OptionalInputProps } from './OptionalInput';

interface MockData {
  name: string;
}

describe('OptionalInput component', () => {
  it('should be disabled initially if default value is null', () => {
    const optionalInput = new OptionalInput<MockData>(emptyProps);
    expect(optionalInput.state.isChecked).toEqual(false);
    expect(optionalInput.state.inputValue).toEqual(null);
  });

  it('should be enabled initially if default value is not null', () => {
    const optionalInput = new OptionalInput<MockData>(dataProps);
    expect(optionalInput.state.isChecked).toEqual(true);
    expect(optionalInput.state.inputValue).toEqual(dataProps.defaultValue);
  });

  it('should call onChange when input changes if valid', () => {
    const optionalInput = mount(<OptionalInput {...dataProps} />);
    (dataProps.isValid as jest.Mock).mockReturnValue(true);

    (optionalInput.find(TextInput).props().onChange as (value: string) => void)('A');
    expect(dataProps.isValid).toBeCalled();
    expect(dataProps.isValid).toBeCalledWith('A');
    expect(dataProps.valueMapper).toBeCalled();
    expect(dataProps.valueMapper).toBeCalledWith('A');
    expect(dataProps.onChange).toBeCalled();
    expect(dataProps.onChange).toBeCalledWith({ name: 'A' });
  });

  it('should call onChange with undefined when input changes if invalid', () => {
    const optionalInput = mount(<OptionalInput {...dataProps} />);
    (dataProps.isValid as jest.Mock).mockReturnValue(false);
    (dataProps.onChange as jest.Mock).mockClear();

    (optionalInput.find(TextInput).props().onChange as (value: string) => void)('A');
    expect(dataProps.isValid).toBeCalled();
    expect(dataProps.isValid).toBeCalledWith('A');
    expect(dataProps.onChange).toBeCalled();
    expect(dataProps.onChange).toBeCalledWith(undefined);
  });

  it('should be enabled after a toggle', () => {
    const optionalInput = new OptionalInput<MockData>(emptyProps);
    optionalInput.setState = jest.fn();

    optionalInput.handleToggle(true);
    expect(optionalInput.setState).toBeCalled();
    expect(optionalInput.setState).toBeCalledWith({ isChecked: true });
    expect(emptyProps.onChange).toBeCalled();
    expect(emptyProps.onChange).toBeCalledWith(null);
  });

  it('should be disabled after a toggle', () => {
    const optionalInput = new OptionalInput<MockData>(dataProps);
    optionalInput.setState = jest.fn();

    optionalInput.handleToggle(false);
    expect(optionalInput.setState).toBeCalled();
    expect(optionalInput.setState).toBeCalledWith({ isChecked: false });
    expect(dataProps.onChange).toBeCalled();
    expect(dataProps.onChange).toBeCalledWith(null);
  });

  it('should load the old value after a toggle', () => {
    const optionalInput = new OptionalInput<MockData>(dataProps);
    optionalInput.setState = jest.fn();

    optionalInput.handleToggle(false);
    expect(optionalInput.setState).toBeCalled();
    expect(optionalInput.setState).toBeCalledWith({ isChecked: false });
    expect(dataProps.onChange).toBeCalled();
    expect(dataProps.onChange).toBeCalledWith(null);

    optionalInput.handleToggle(true);
    expect(optionalInput.setState).toBeCalled();
    expect(optionalInput.setState).toBeCalledWith({ isChecked: true });
    expect(dataProps.onChange).toBeCalled();
    expect(dataProps.onChange).toBeCalledWith(dataProps.defaultValue);
  });

  it('should render correctly', () => {
    const select = shallow(<OptionalInput {...dataProps} />);
    expect(dataProps.valueToString).toBeCalled();
    expect(dataProps.valueToString).toBeCalledWith(dataProps.defaultValue);
    expect(toJson(select)).toMatchSnapshot();
  });
});

const emptyProps: OptionalInputProps<MockData> = {
  defaultValue: null,
  valueToString: jest.fn(value => value.name),
  valueMapper: jest.fn(option => ({ name: option })),
  onChange: jest.fn(),
  isValid: jest.fn(),
  label: 'empty',
};

const dataProps: OptionalInputProps<MockData> = {
  defaultValue: { name: 'Some name' },
  valueToString: jest.fn(value => value.name),
  valueMapper: jest.fn(option => ({ name: option })),
  onChange: jest.fn(),
  isValid: jest.fn(),
  label: 'data',
};
