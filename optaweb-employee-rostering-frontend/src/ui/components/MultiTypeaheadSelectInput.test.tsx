import { shallow, mount } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import MultiTypeaheadSelectInput, { MultiTypeaheadSelectProps } from './MultiTypeaheadSelectInput';

interface MockData {
  name: string;
}

type Select = MultiTypeaheadSelectInput<MockData>;
describe('MultiTypeaheadSelectInput component', () => {
  it('should remove selected from list when selected again and call onChange', () => {
    const defaultValue = [{ name: 'Option 2' }];
    const select = mount(<MultiTypeaheadSelectInput {...selectProps} value={defaultValue} />);
    (select.instance() as Select).onSelect([]);
    expect(selectProps.onChange).toBeCalled();
    expect(selectProps.onChange).toBeCalledWith([]);
  });

  it('should add option to selection when it is selected and not already in the list and call onChange', () => {
    const defaultValue = [{ name: 'Option 2' }];
    const select = mount(<MultiTypeaheadSelectInput {...selectProps} value={defaultValue} />);
    (select.instance() as Select).onSelect([...defaultValue, { name: 'Option 1' }].map(o => ({ value: o })));
    expect(selectProps.onChange).toBeCalled();
    expect(selectProps.onChange).toBeCalledWith([...defaultValue, { name: 'Option 1' }]);
  });

  it('should render correctly', () => {
    const select = shallow(<MultiTypeaheadSelectInput {...selectProps} />);
    expect(toJson(select)).toMatchSnapshot();
  });
});

const selectProps: MultiTypeaheadSelectProps<MockData> = {
  emptyText: 'Enter some data',
  options: [{ name: 'Option 1' }, { name: 'Option 2' }, { name: 'Option 3' }],
  value: [],
  optionToStringMap: jest.fn(option => option.name),
  onChange: jest.fn(),
};
