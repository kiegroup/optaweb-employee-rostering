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
import * as React from 'react';
import { shallow } from 'enzyme';
import EventWrapper from './EventWrapper';

describe('EventWrapper', () => {
  it('should render correctly when the event does not span multiple days', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier={false}
        continuesLater={false}
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
        style={{ top: '50%', height: '25%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly when the event continues from eariler', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier
        continuesLater={false}
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
        style={{ top: '0%', height: '25%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly when the event continues later', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier={false}
        continuesLater
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
        style={{ top: '75%', height: '25%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly when the event continues both before and after', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier
        continuesLater
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
        style={{ top: '0%', height: '100%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly when the event has no height or top', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier={false}
        continuesLater={false}
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
        style={{ color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly without a style', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier={false}
        continuesLater={false}
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly when the event does not have a class name', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier={false}
        continuesLater={false}
        className=""
        popoverHeader="Title"
        popoverBody="Body"
        style={{ top: '50%', height: '25%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });
});
