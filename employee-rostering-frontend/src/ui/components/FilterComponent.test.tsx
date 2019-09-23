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
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { FilterProps, FilterComponent } from './FilterComponent';
import { Button } from '@patternfly/react-core';
import { useTranslation, WithTranslation } from 'react-i18next';

interface MockData {
  name: string;
}

describe('Filter component', () => {
  it('should render correctly with no text', () => {
    const filter = shallow(<FilterComponent {...props} />);
    expect(toJson(filter)).toMatchSnapshot();
  });

  it('should render correctly with text', () => {
    const filter = shallow(<FilterComponent {...props} />);
    (filter.instance() as FilterComponent<MockData>).updateFilter("Test");
    expect(toJson(filter)).toMatchSnapshot();
  });

  it('should call filter and onChange on updateFilter', () => {
    const filter = new FilterComponent<MockData>(props);
    const predicate = jest.fn();
    filter.setState = jest.fn();
    (props.filter as jest.Mock).mockReturnValue(predicate);
    filter.updateFilter("Test");
    expect(props.filter).toBeCalled();
    expect(props.filter).toBeCalledWith("Test");
    expect(props.onChange).toBeCalled();
    expect(props.onChange).toBeCalledWith(predicate);
    expect(filter.setState).toBeCalledWith({filterText: "Test"});
  });

  it('should call updateFilter with empty text when button is clicked', () => {
    const filter = shallow(<FilterComponent {...props} />);
    const predicate = jest.fn();
    (props.filter as jest.Mock).mockReturnValue(predicate);
    filter.instance().setState = jest.fn();

    filter.find(Button).simulate("click");
    expect(props.filter).toBeCalled();
    expect(props.filter).toBeCalledWith("");
    expect(props.onChange).toBeCalled();
    expect(props.onChange).toBeCalledWith(predicate);
    expect(filter.instance().setState).toBeCalledWith({filterText: ""});
  });
});

const props: FilterProps<MockData> & WithTranslation = {
  ...useTranslation("FilterComponent"),
  tReady: true,
  filter: jest.fn(),
  onChange: jest.fn()
};