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
import { EditableComponent, EditableComponentProps } from './EditableComponent';

describe('EditableComponent component', () => {
  it('should render viewer when not editing', () => {
    const component = shallow(<EditableComponent {...notEditing} />);
    expect(toJson(component)).toMatchSnapshot();
  });

  it('should render editor when editing', () => {
    const component = shallow(<EditableComponent {...isEditing} />);
    expect(toJson(component)).toMatchSnapshot();
  });
});

const notEditing: EditableComponentProps = {
  viewer: <span>Viewer</span>,
  editor: <div>Editor</div>,
  isEditing: false,
};

const isEditing: EditableComponentProps = {
  viewer: <span>Viewer</span>,
  editor: <div>Editor</div>,
  isEditing: true,
};
