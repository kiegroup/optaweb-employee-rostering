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
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Spot } from 'domain/Spot';
import { spotSelectors } from 'store/spot';
import { useSelector, useDispatch } from 'react-redux';
import {
  Button, EmptyState, EmptyStateVariant,
  EmptyStateIcon, Title, EmptyStateBody, Flex, FlexModifiers, FlexItem,
} from '@patternfly/react-core';

import TypeaheadSelectInput from 'ui/components/TypeaheadSelectInput';

import { timeBucketOperations, timeBucketSelectors } from 'store/rotation';
import { useTranslation, Trans } from 'react-i18next';
import { CubesIcon, PlusIcon } from '@patternfly/react-icons';
import { useHistory } from 'react-router';
import { v4 as uuid } from 'uuid';
import { useUrlState } from 'util/FunctionalComponentUtils';
import { rosterSelectors } from 'store/roster';
import { tenantSelectors } from 'store/tenant';
import moment from 'moment';
import { Employee } from 'domain/Employee';
import { SeatJigsaw } from './SeatJigsaw';
import { EditTimeBucketModal } from './EditTimeBucketModal';
import { EmployeeStubList, Stub } from './EmployeeStub';

export const RotationPage: React.FC<{}> = () => {
  const { t } = useTranslation('RotationPage');
  const history = useHistory();

  const tenantId = useSelector(tenantSelectors.getTenantId);
  const rosterState = useSelector(rosterSelectors.getRosterState);
  const isLoading = useSelector(timeBucketSelectors.isLoading);
  const spotList = useSelector(spotSelectors.getSpotList);
  const timeBucketList = useSelector(timeBucketSelectors.getTimeBucketList);

  const dispatch = useDispatch();

  const [selectedStub, setSelectedStub] = useState<Stub|null>(null);
  const [isEditingTimeBuckets, setIsEditingTimeBuckets] = useState(false);

  const [shownSpotName, setShownSpotName] = useUrlState('spot', (spotList.length > 0) ? spotList[0].name : undefined);
  const shownSpot = spotList.find(s => s.name === shownSpotName);
  const shownTimeBuckets = shownSpot ? timeBucketList.filter(tb => tb.spot.id === shownSpot.id) : [];
  const oldShownTimeBuckets = useRef(shownTimeBuckets.map(tb => tb.id).join(','));
  const getEmployeesInTimeBuckets = useCallback(() => {
    const employeesInTimeBuckets: Employee[] = [];
    shownTimeBuckets.forEach(tb => tb.seatList.forEach((seat) => {
      if (seat !== null && seat.employee !== null
          && employeesInTimeBuckets.find(employee => employee.id === (seat.employee as Employee).id) === undefined) {
        employeesInTimeBuckets.push(seat.employee);
      }
    }));
    return employeesInTimeBuckets;
  }, [shownTimeBuckets]);

  const [stubList, setStubList] = useState<Stub[]>(getEmployeesInTimeBuckets().map(employee => ({
    employee,
    color: employee.color,
  })));

  useEffect(() => {
    const theShownSpot = spotList.find(s => s.name === shownSpotName);
    const theShownTimeBuckets = theShownSpot ? timeBucketList.filter(tb => tb.spot.id === theShownSpot.id) : [];
    if (oldShownTimeBuckets.current !== theShownTimeBuckets.map(tb => tb.id).join(',')) {
      oldShownTimeBuckets.current = theShownTimeBuckets.map(tb => tb.id).join(',');
      setStubList(getEmployeesInTimeBuckets().map(employee => ({
        employee,
        color: employee.color,
      })));
      setSelectedStub(null);
    }
  },
  [oldShownTimeBuckets, shownSpotName, spotList, timeBucketList, getEmployeesInTimeBuckets]);

  if (rosterState === null || isLoading || spotList.length <= 0 || shownSpotName === null) {
    return (
      <EmptyState variant={EmptyStateVariant.full}>
        <EmptyStateIcon icon={CubesIcon} />
        <Trans
          t={t}
          i18nKey="noSpots"
          components={[
            <Title headingLevel="h5" size="lg" key={0} />,
            <EmptyStateBody key={1} />,
            <Button
              key={2}
              aria-label="Spots Page"
              variant="primary"
              onClick={() => history.push(`/${tenantId}/wards`)}
            />,
          ]}
        />
      </EmptyState>
    );
  }
  return (
    <>
      <Title size="2xl">Rotation</Title>
      <TypeaheadSelectInput
        aria-label="Select Spot"
        emptyText={t('selectSpot')}
        optionToStringMap={spot => spot.name}
        options={spotList}
        value={shownSpot}
        onChange={(s) => {
          setShownSpotName(s ? s.name : null);
        }}
        noClearButton
      />
      <EmployeeStubList
        selectedStub={selectedStub}
        stubList={stubList}
        onStubSelect={setSelectedStub}
        onUpdateStubList={setStubList}
      />

      <Flex breakpointMods={[{ modifier: FlexModifiers.column }]}>
        {shownTimeBuckets.map(timeBucket => (
          <FlexItem key={uuid()}>
            <SeatJigsaw
              selectedStub={selectedStub}
              timeBucket={timeBucket}
              onUpdateTimeBucket={tb => dispatch(timeBucketOperations.updateTimeBucket(tb))}
              onDeleteTimeBucket={() => dispatch(timeBucketOperations.removeTimeBucket(timeBucket))}
            />
          </FlexItem>
        ))}
        <FlexItem>
          <EditTimeBucketModal
            isOpen={isEditingTimeBuckets}
            timeBucket={{
              tenantId,
              spot: shownSpot as Spot,
              startTime: moment('00:00:00', 'HH:mm:ss').toDate(),
              endTime: moment('00:00:00', 'HH:mm:ss').toDate(),
              repeatOnDaySetList: [],
              additionalSkillSet: [],
              seatList: new Array(rosterState ? rosterState.rotationLength : 0).fill(null),
            }}
            onUpdateTimeBucket={timeBucket => dispatch(timeBucketOperations.addTimeBucket(timeBucket))}
            onClose={() => setIsEditingTimeBuckets(false)}
          />
          <Button
            onClick={() => setIsEditingTimeBuckets(true)}
            variant="link"
          >
            <PlusIcon />
            {' '}
Add New Time Bucket
          </Button>
        </FlexItem>
      </Flex>
    </>
  );
};
