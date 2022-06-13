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
        boundary={{ current: undefined }}
        style={{ top: '50%', height: '25%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });

  it('should render correctly when the event continues from earlier', () => {
    const eventWrapper = shallow(
      <EventWrapper
        continuesEarlier
        continuesLater={false}
        className="class"
        popoverHeader="Title"
        popoverBody="Body"
        boundary={{ current: undefined }}
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
        boundary={{ current: undefined }}
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
        boundary={{ current: undefined }}
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
        boundary={{ current: undefined }}
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
        boundary={{ current: undefined }}
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
        boundary={{ current: undefined }}
        style={{ top: '50%', height: '25%', color: 'white' }}
      />,
    );
    expect(eventWrapper).toMatchSnapshot();
  });
});
