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
import TypeaheadSelectInput, { TypeaheadSelectProps } from './TypeaheadSelectInput';

interface MockData {
  name: string;
}

type Select = TypeaheadSelectInput<MockData>;
describe('TypeaheadSelectInput component', () => {
  it('should set selected and call onChange', () => {
    jest.useFakeTimers();
    const defaultValue = { name: 'Option 2' };
    const select = mount(<TypeaheadSelectInput {...selectProps} value={defaultValue} />);
    select.setState({ isExpanded: true });
    (select.instance() as Select).onSelect({ value: { name: 'Option 1' } });
    expect(selectProps.onChange).toBeCalled();
    expect(selectProps.onChange).toBeCalledWith({ name: 'Option 1' });
  });

  it('should set selected to undefined on clear selection and call onChange', () => {
    const defaultValue = { name: 'Option 2' };
    const select = mount(<TypeaheadSelectInput {...selectProps} value={defaultValue} />);
    select.setState({ isExpanded: true });
    (select.instance() as Select).onSelect(undefined);
    expect(selectProps.onChange).toBeCalled();
    expect(selectProps.onChange).toBeCalledWith(undefined);
  });

  it('should render correctly', () => {
    const select = shallow(<TypeaheadSelectInput {...selectProps} />);
    expect(toJson(select)).toMatchSnapshot();
  });
});

const selectProps: TypeaheadSelectProps<MockData> = {
  emptyText: 'Enter some data',
  options: [{ name: 'Option 1' }, { name: 'Option 2' }, { name: 'Option 3' }],
  value: undefined,
  optionToStringMap: jest.fn(option => option.name),
  onChange: jest.fn(),
};
