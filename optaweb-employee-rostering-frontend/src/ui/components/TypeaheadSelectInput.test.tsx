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
