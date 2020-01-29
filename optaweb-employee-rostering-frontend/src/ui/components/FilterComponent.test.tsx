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
