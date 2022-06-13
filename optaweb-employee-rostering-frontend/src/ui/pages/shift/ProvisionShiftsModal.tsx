import React from 'react';
import {
  Modal, Button, ButtonVariant, Form, InputGroup, Label, TextInput,
  Accordion, AccordionItem, AccordionContent, AccordionToggle, Checkbox,
} from '@patternfly/react-core';
import DatePicker from 'react-datepicker';
import { useTranslation } from 'react-i18next';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { timeBucketSelectors } from 'store/rotation';
import { rosterSelectors, rosterOperations } from 'store/roster';
import { useDispatch, useSelector } from 'react-redux';
import { spotSelectors } from 'store/spot';
import { Spot } from 'domain/Spot';
import { TimeBucket } from 'domain/TimeBucket';
import moment from 'moment';

export interface SpotTimeBucketSelectProps {
  spot: Spot;
  timeBucketList: TimeBucket[];
  selectedTimeBucketList: TimeBucket[];
  onUpdateSelectedTimeBucketList: (timeBucketList: TimeBucket[]) => void;
}

export const SpotTimeBucketSelect: React.FC<SpotTimeBucketSelectProps> = (props) => {
  const [isExpanded, setIsExpanded] = React.useState(false);
  const timeBucketListForSpot = props.timeBucketList.filter(tb => tb.spot.id === props.spot.id);
  let spotAllTimeBucketChecked: boolean | null = null;
  if (timeBucketListForSpot
    .filter(tb => props.selectedTimeBucketList.includes(tb)).length === timeBucketListForSpot.length) {
    spotAllTimeBucketChecked = true;
  } else if (timeBucketListForSpot
    .filter(tb => props.selectedTimeBucketList.includes(tb)).length === 0) {
    spotAllTimeBucketChecked = false;
  }
  return (
    <AccordionItem>
      <AccordionToggle
        id={props.spot.name}
        onClick={(e) => {
          if (e.currentTarget === e.target) {
            setIsExpanded(!isExpanded);
          }
        }}
        isExpanded={isExpanded}
      >
        <span>
          {props.spot.name}
        </span>
        <span
          style={{
            display: 'inline-block',
            paddingLeft: '2em',
          }}
        >
          <Checkbox
            id={`${props.spot.name}-toggle-all`}
            isChecked={spotAllTimeBucketChecked as boolean /* Patternfly example use null for minus, yet it not in
            their typescript definition as a valid value */}
            onChange={(isChecked) => {
              if (isChecked) {
                props.onUpdateSelectedTimeBucketList(props.selectedTimeBucketList.concat(timeBucketListForSpot
                  .filter(tb => !props.selectedTimeBucketList.includes(tb))));
              } else {
                props.onUpdateSelectedTimeBucketList(props.selectedTimeBucketList
                  .filter(tb => !timeBucketListForSpot.includes(tb)));
              }
            }}
          />
        </span>
      </AccordionToggle>
      <AccordionContent
        isFixed
        isHidden={!isExpanded}
      >
        {timeBucketListForSpot.map(tb => (
          <div key={tb.id}>
            <span>
              {`${tb.additionalSkillSet.length > 0 ? `${tb.additionalSkillSet.join(', ')} - ` : ''}
              ${moment(tb.startTime).format('LT')} to ${moment(tb.endTime).format('LT')} on
              ${tb.repeatOnDaySetList.join(', ')}`}
            </span>
            <span>
              <Checkbox
                id={`timebucket-${tb.id}-toggle`}
                isChecked={props.selectedTimeBucketList.includes(tb)}
                onChange={() => {
                  if (props.selectedTimeBucketList.includes(tb)) {
                    props.onUpdateSelectedTimeBucketList(
                      props.selectedTimeBucketList.filter(otherTb => otherTb.id !== tb.id),
                    );
                  } else {
                    props.onUpdateSelectedTimeBucketList(
                      [...props.selectedTimeBucketList, tb],
                    );
                  }
                }}
              />
            </span>
          </div>
        ))}
      </AccordionContent>
    </AccordionItem>
  );
};

export interface ProvisionShiftsModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultFromDate: Date;
  defaultToDate: Date;
}

export const ProvisionShiftsModal: React.FC<ProvisionShiftsModalProps> = (props) => {
  const { t } = useTranslation('ProvisionShiftsModal');

  const [fromDate, setFromDate] = React.useState<Date | null>(props.defaultFromDate);
  const [toDate, setToDate] = React.useState<Date | null>(props.defaultToDate);
  const [rotationOffset, setRotationOffset] = React.useState(0);
  const [provisionedSpots, setProvisionedSpots] = React.useState<Spot[]>([]);
  const [provisionedTimeBuckets, setProvisionedTimeBuckets] = React.useState<TimeBucket[]>([]);

  const timeBucketList = useSelector(timeBucketSelectors.getTimeBucketList);
  const spotList = useSelector(spotSelectors.getSpotList);
  const rosterState = useSelector(rosterSelectors.getRosterState);
  const dispatch = useDispatch();

  // Work around since useEffect use shallowEquality, and the same date created at different times are not equal
  const defaultFromDateTime = props.defaultFromDate.getTime();
  const defaultToDateTime = props.defaultToDate.getTime();

  React.useEffect(() => {
    if (props.isOpen) {
      setFromDate(new Date(defaultFromDateTime));
      setToDate(new Date(defaultToDateTime));
      setRotationOffset(rosterState ? rosterState.unplannedRotationOffset : 0);
      setProvisionedSpots(spotList);
      setProvisionedTimeBuckets(timeBucketList);
    }
  }, [props.isOpen, rosterState, spotList, timeBucketList, defaultFromDateTime, defaultToDateTime]);

  return (
    <Modal
      title={t('provisionShifts')}
      isOpen={props.isOpen}
      onClose={props.onClose}
      actions={
        [
          <Button
            aria-label="Close Modal"
            variant={ButtonVariant.tertiary}
            key={0}
            onClick={props.onClose}
          >
            {t('close')}
          </Button>,
          <Button
            variant={ButtonVariant.primary}
            key={1}
            onClick={() => {
              dispatch(rosterOperations.provision({
                startRotationOffset: rotationOffset,
                fromDate: fromDate as Date,
                toDate: toDate as Date,
                timeBucketList: provisionedTimeBuckets,
              }));
              props.onClose();
            }}
          >
            {t('provisionShifts')}
          </Button>,
        ]
      }
      variant="small"
    >
      <Form id="modal-element" onSubmit={e => e.preventDefault()}>
        <InputGroup>
          <Label>{t('fromDate')}</Label>
          <DatePicker
            aria-label={t('fromDate')}
            selected={fromDate}
            onChange={setFromDate}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('toDate')}</Label>
          <DatePicker
            aria-label={t('toDate')}
            selected={toDate}
            onChange={setToDate}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('startingFromRotationOffset')}</Label>
          <TextInput
            aria-label={t('startingFromRotationOffset')}
            value={rotationOffset}
            onChange={v => setRotationOffset(parseInt(v, 10))}
          />
        </InputGroup>
        <InputGroup>
          <Label>{t('forSpots')}</Label>
          <MultiTypeaheadSelectInput
            aria-label={t('forSpots')}
            emptyText={t('selectSpots')}
            value={provisionedSpots}
            options={spotList}
            optionToStringMap={spot => spot.name}
            onChange={(newSpotList) => {
              setProvisionedSpots(newSpotList);
              let newTimeBucketList = provisionedTimeBuckets;
              newSpotList.filter(spot => !provisionedSpots.includes(spot)).forEach((spot) => {
                // New spot added
                newTimeBucketList = [...newTimeBucketList, ...timeBucketList.filter(tb => tb.spot.id === spot.id)];
              });
              provisionedSpots.filter(spot => !newSpotList.includes(spot)).forEach((spot) => {
                // Spot removed
                newTimeBucketList = newTimeBucketList.filter(tb => tb.spot.id !== spot.id);
              });
              setProvisionedTimeBuckets(newTimeBucketList);
            }}
          />
        </InputGroup>
        <Accordion asDefinitionList={false}>
          {provisionedSpots.map(spot => (
            <SpotTimeBucketSelect
              key={spot.id}
              spot={spot}
              timeBucketList={timeBucketList}
              selectedTimeBucketList={provisionedTimeBuckets}
              onUpdateSelectedTimeBucketList={setProvisionedTimeBuckets}
            />
          ))}
        </Accordion>
      </Form>
    </Modal>
  );
};
