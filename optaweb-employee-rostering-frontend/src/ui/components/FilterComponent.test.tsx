import { shallow } from 'enzyme';
import * as React from 'react';
import { Button, TextInput } from '@patternfly/react-core';
import { useTranslation, WithTranslation } from 'react-i18next';
import { FilterProps, FilterComponent } from './FilterComponent';

describe('Filter component', () => {
  it('should render correctly with no text', () => {
    const myProps = { ...props, filterText: '' };
    const filter = shallow(<FilterComponent {...myProps} />);
    expect(filter).toMatchSnapshot();
  });

  it('should render correctly with text', () => {
    const filter = shallow(<FilterComponent {...props} />);
    expect(filter).toMatchSnapshot();
  });

  it('should call filter and onChange on updateFilter', () => {
    const filter = shallow(<FilterComponent {...props} />);
    filter.find(TextInput).simulate('change', 'New Filter');
    expect(props.onChange).toBeCalled();
    expect(props.onChange).toBeCalledWith('New Filter');
  });

  it('should call updateFilter with empty text when button is clicked', () => {
    const filter = shallow(<FilterComponent {...props} />);

    filter.find(Button).simulate('click');
    expect(props.onChange).toBeCalled();
    expect(props.onChange).toBeCalledWith('');
  });
});

const props: FilterProps & WithTranslation = {
  ...useTranslation('FilterComponent'),
  tReady: true,
  filterText: 'Test',
  onChange: jest.fn(),
};
