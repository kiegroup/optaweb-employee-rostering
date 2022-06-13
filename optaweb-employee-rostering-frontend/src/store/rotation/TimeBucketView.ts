import { TimeBucket } from 'domain/TimeBucket';
import { DomainObject } from 'domain/DomainObject';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import moment from 'moment';
import DomainObjectView from 'domain/DomainObjectView';

export function timeBucketToTimeBucketView(timeBucket: TimeBucket): TimeBucketView {
  return {
    ...objectWithout(timeBucket, 'spot', 'additionalSkillSet', 'seatList'),
    spotId: timeBucket.spot.id as number,
    additionalSkillSetIdList: timeBucket.additionalSkillSet.map(skill => skill.id as number),
    seatList: timeBucket.seatList
      .map(seat => ({
        dayInRotation: seat.dayInRotation,
        employeeId: seat.employee ? seat.employee.id as number : null,
      })),
    startTime: moment(timeBucket.startTime).format('HH:mm:ss'),
    endTime: moment(timeBucket.endTime).format('HH:mm:ss'),
  };
}

export function timeBucketViewToDomainObjectView(view: TimeBucketView): DomainObjectView<TimeBucket> {
  return {
    ...objectWithout(view, 'spotId', 'additionalSkillSetIdList'),
    spot: view.spotId,
    additionalSkillSet: view.additionalSkillSetIdList,
    seatList: view.seatList.map(sv => ({ dayInRotation: sv.dayInRotation, employee: sv.employeeId })),
    startTime: moment(view.startTime, 'HH:mm:ss').toDate(),
    endTime: moment(view.endTime, 'HH:mm:ss').toDate(),
  };
}

export interface SeatView {
  dayInRotation: number;
  employeeId: number|null;
}

export interface TimeBucketView extends DomainObject {
  spotId: number;
  additionalSkillSetIdList: number[];
  repeatOnDaySetList: string[];
  startTime: string;
  endTime: string;
  seatList: SeatView[];
}
