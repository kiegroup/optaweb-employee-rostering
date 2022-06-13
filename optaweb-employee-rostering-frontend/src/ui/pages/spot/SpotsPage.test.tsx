import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Spot } from 'domain/Spot';
import { getRouterProps } from 'util/BookmarkableTestUtils';
import { mockStore } from 'store/mockStore';
import { Skill } from 'domain/Skill';
import { Map } from 'immutable';
import DomainObjectView from 'domain/DomainObjectView';
import { spotOperations, spotSelectors } from 'store/spot';
import { mockRedux } from 'setupTests';
import { DataTable, RowEditButtons, RowViewButtons } from 'ui/components/DataTable';
import { doNothing } from 'types';
import { Button, TextInput } from '@patternfly/react-core';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { skillSelectors } from 'store/skill';
import { ArrowIcon } from '@patternfly/react-icons';
import { SpotsPage, SpotRow, EditableSpotRow } from './SpotsPage';

const noSpotsStore = mockStore({
  spotList: {
    isLoading: false,
    spotMapById: Map(),
  },
}).store;

const twoSpotsStore = mockStore({
  skillList: {
    isLoading: false,
    skillMapById: Map<number, Skill>()
      .set(0, {
        id: 0,
        version: 0,
        tenantId: 0,
        name: 'Skill 1',
      }),
  },
  spotList: {
    isLoading: false,
    spotMapById: Map<number, DomainObjectView<Spot>>()
      .set(1, {
        id: 1,
        version: 0,
        tenantId: 0,
        name: 'Spot 1',
        requiredSkillSet: [],
      })
      .set(2, {
        id: 2,
        version: 0,
        tenantId: 0,
        name: 'Spot 2',
        requiredSkillSet: [0],
      }),
  },
}).store;

describe('Spots page', () => {
  const addSpot = (spot: Spot) => ['add', spot];
  const updateSpot = (spot: Spot) => ['update', spot];
  const removeSpot = (spot: Spot) => ['remove', spot];

  beforeEach(() => {
    jest.spyOn(spotOperations, 'addSpot').mockImplementation(spot => addSpot(spot) as any);
    jest.spyOn(spotOperations, 'updateSpot').mockImplementation(spot => updateSpot(spot) as any);
    jest.spyOn(spotOperations, 'removeSpot').mockImplementation(spot => removeSpot(spot) as any);
    jest.spyOn(twoSpotsStore, 'dispatch').mockImplementation(doNothing);
  });

  it('should render correctly with no spots', () => {
    mockRedux(noSpotsStore);
    const spotsPage = shallow(<SpotsPage {...getRouterProps('/0/spot', {})} />);
    expect(toJson(spotsPage)).toMatchSnapshot();
  });

  it('should render correctly with a few spots', () => {
    mockRedux(twoSpotsStore);
    const skillsPage = shallow(
      <SpotsPage {...getRouterProps('/0/spot', {})} />,
    );
    expect(toJson(skillsPage)).toMatchSnapshot();
  });

  it('should render the viewer correctly', () => {
    const spot = spotSelectors.getSpotById(twoSpotsStore.getState(), 2);
    mockRedux(twoSpotsStore);
    getRouterProps('/0/spot', {});
    const viewer = shallow(<SpotRow {...spot} />);
    expect(toJson(viewer)).toMatchSnapshot();
  });

  it('clicking on the arrow should take you to the Adjustment Page', () => {
    const spot = spotSelectors.getSpotById(twoSpotsStore.getState(), 2);
    mockRedux(twoSpotsStore);
    const routerProps = getRouterProps('/0/spot', {});
    const viewer = shallow(<SpotRow {...spot} />);
    viewer.find(Button).filterWhere(wrapper => wrapper.contains(<ArrowIcon />)).simulate('click');
    expect(routerProps.history.push).toBeCalledWith(`/${spot.tenantId}/adjust?spot=${encodeURIComponent(spot.name)}`);
  });

  it('clicking on the edit button should show editor', () => {
    const spot = spotSelectors.getSpotById(twoSpotsStore.getState(), 2);
    mockRedux(twoSpotsStore);
    getRouterProps('/0/spot', {});
    const viewer = shallow(<SpotRow {...spot} />);
    viewer.find(RowViewButtons).simulate('edit');

    expect(viewer).toMatchSnapshot();
    viewer.find(EditableSpotRow).simulate('close');
    expect(viewer).toMatchSnapshot();
  });

  it('should render the editor correctly', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    expect(toJson(editor)).toMatchSnapshot();
  });

  it('no name should be invalid', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: '',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('duplicate name should be invalid', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 3,
      name: 'Spot 1',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    expect(editor.find(RowEditButtons).prop('isValid')).toBe(false);
  });

  it('saving new spot should call add spot', () => {
    const spot: Spot = {
      tenantId: 0,
      name: '',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew onClose={jest.fn()} />);

    const name = 'New Spot Name';
    const requiredSkillSet = skillSelectors.getSkillList(twoSpotsStore.getState());
    editor.find(TextInput).simulate('change', name);
    editor.find(MultiTypeaheadSelectInput).simulate('change', requiredSkillSet);
    editor.find(RowEditButtons).prop('onSave')();
    const newSpot = { ...spot, name, requiredSkillSet };

    expect(spotOperations.addSpot).toBeCalledWith(newSpot);
    expect(twoSpotsStore.dispatch).toBeCalledWith(addSpot(newSpot));
  });

  it('saving updated spot should call update spot', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const editor = shallow(<EditableSpotRow spot={spot} isNew={false} onClose={jest.fn()} />);
    editor.find(RowEditButtons).prop('onSave')();
    expect(spotOperations.updateSpot).toBeCalledWith(spot);
    expect(twoSpotsStore.dispatch).toBeCalledWith(updateSpot(spot));
  });

  it('clicking on the edit button in the viewer should show the editor', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const viewer = shallow(<SpotRow {...spot} />);

    // Clicking the edit button should show the editor
    viewer.find(RowViewButtons).prop('onEdit')();
    expect(viewer).toMatchSnapshot();

    // Clicking the close button should show the viwer
    viewer.find(EditableSpotRow).prop('onClose')();
    expect(viewer).toMatchSnapshot();
  });

  it('deleting should call delete spot', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const viewer = shallow(<SpotRow {...spot} />);
    viewer.find(RowViewButtons).prop('onDelete')();
    expect(spotOperations.removeSpot).toBeCalledWith(spot);
    expect(twoSpotsStore.dispatch).toBeCalledWith(removeSpot(spot));
  });

  it('DataTable rowWrapper should be SpotRow', () => {
    const spot: Spot = {
      tenantId: 0,
      id: 1,
      name: 'Spot',
      requiredSkillSet: [],
    };
    mockRedux(twoSpotsStore);
    const spotsPage = shallow(<SpotsPage {...getRouterProps('/0/spot', {})} />);
    const rowWrapper = shallow(spotsPage.find(DataTable).prop('rowWrapper')(spot));
    expect(rowWrapper).toMatchSnapshot();
  });

  it('DataTable newRowWrapper should be EditableSpotRow', () => {
    mockRedux(twoSpotsStore);
    const spotsPage = shallow(<SpotsPage {...getRouterProps('/0/skill', {})} />);
    const removeRow = jest.fn();
    const newRowWrapper = shallow((spotsPage.find(DataTable).prop('newRowWrapper') as any)(removeRow));
    expect(newRowWrapper).toMatchSnapshot();
    newRowWrapper.find(RowEditButtons).prop('onClose')();
    expect(removeRow).toBeCalled();
  });
});
