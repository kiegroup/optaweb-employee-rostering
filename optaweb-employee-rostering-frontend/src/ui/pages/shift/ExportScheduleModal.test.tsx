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
import DatePicker from 'react-datepicker';
import { Spot } from 'domain/Spot';
import moment from 'moment';
import { useTranslation } from 'react-i18next';
import { Modal } from '@patternfly/react-core';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { ExportScheduleModal } from './ExportScheduleModal';

describe('Export Schedule Modal', () => {
  it('should render correctly when closed', () => {
    const exportScheduleModal = shallow(<ExportScheduleModal
      {...baseProps}
      isOpen={false}
    />);
    expect(toJson(exportScheduleModal)).toMatchSnapshot();
  });

  it('should render correctly when opened', () => {
    const exportScheduleModal = shallow(<ExportScheduleModal
      {...baseProps}
    />);
    expect(toJson(exportScheduleModal)).toMatchSnapshot();
  });

  it('should export the schedule with default from and to date with the full spot list by default', () => {
    const exportScheduleModal = shallow(<ExportScheduleModal
      {...baseProps}
    />);
    const urlButton = shallow(exportScheduleModal.find(Modal).prop('actions')[1]);
    expect(urlButton.prop('href'))
      .toEqual(getExportUrlFor(baseProps.defaultFromDate, baseProps.defaultToDate, baseProps.spotList));
  });

  it('should export to _blank if spot list is empty', () => {
    const exportScheduleModal = shallow(<ExportScheduleModal
      {...baseProps}
      spotList={[]}
    />);
    const urlButton = shallow(exportScheduleModal.find(Modal).prop('actions')[1]);
    expect(urlButton.prop('href')).toEqual('_blank');
  });

  it('should export with new params if user changes fields', () => {
    const exportScheduleModal = shallow(<ExportScheduleModal
      {...baseProps}
    />);

    const newFromDate = moment('2020-08-01').toDate();
    exportScheduleModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=fromDate)"]')
      .simulate('change', newFromDate);

    const newToDate = moment('2021-07-02').toDate();
    exportScheduleModal.find(DatePicker).filter('[aria-label="Trans(i18nKey=toDate)"]')
      .simulate('change', moment(newToDate).toDate());

    const otherSpot: Spot = {
      tenantId: 1,
      id: 2,
      version: 0,
      name: 'Spot',
      requiredSkillSet: [],
    };

    exportScheduleModal.find(MultiTypeaheadSelectInput).simulate('change', [spot, otherSpot]);
    const urlButton = shallow(exportScheduleModal.find(Modal).prop('actions')[1]);
    expect(urlButton.prop('href'))
      .toEqual(getExportUrlFor(newFromDate, newToDate, [spot, otherSpot]));
  });
});

function getExportUrlFor(fromDate: Date, toDate: Date, spotList: Spot[]): string {
  return `${process.env.REACT_APP_BACKEND_URL}/rest/tenant/1/roster/shiftRosterView/excel?`
                    + `startDate=${moment(fromDate).format('YYYY-MM-DD')}&`
                    + `endDate=${moment(toDate).format('YYYY-MM-DD')}&spotList=${spotList.map(s => s.id).join(',')}`;
}

const spot: Spot = {
  tenantId: 1,
  id: 2,
  version: 0,
  name: 'Spot',
  requiredSkillSet: [
    {
      tenantId: 0,
      id: 3,
      version: 0,
      name: 'Skill',
    },
  ],
};

const baseProps = {
  ...useTranslation('ExportScheduleModal'),
  tReady: true,
  isOpen: true,
  onClose: jest.fn(),
  defaultFromDate: moment('2018-07-01').toDate(),
  defaultToDate: moment('2018-07-07').toDate(),
  tenantId: 1,
  spotList: [spot],
};
