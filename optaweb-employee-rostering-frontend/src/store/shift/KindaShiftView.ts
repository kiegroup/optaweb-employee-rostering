
import { Shift } from 'domain/Shift';
import { shiftToShiftView, ShiftView } from 'domain/ShiftView';
import moment from 'moment';
import { getHardMediumSoftScoreFromString } from 'domain/HardMediumSoftScore';
import { objectWithout } from 'util/ImmutableCollectionOperations';
import { serializeLocalDateTime } from 'store/rest/DataSerialization';

type KindaShiftView1 = Pick<ShiftView, Exclude<keyof ShiftView, 'indictmentScore'>> & { indictmentScore?: string };
type KindaShiftView2 = Pick<KindaShiftView1, Exclude<keyof KindaShiftView1, 'startDateTime'>> &
{ startDateTime: string };
type KindaShiftView3 = Pick<KindaShiftView2, Exclude<keyof KindaShiftView2, 'endDateTime'>> & { endDateTime: string };
export type KindaShiftView = KindaShiftView3;

export function shiftAdapter(shift: Shift): KindaShiftView {
  return {
    ...objectWithout(shiftToShiftView(shift), 'indictmentScore', 'startDateTime', 'endDateTime',
      ...Object.keys(shift).filter(k => Array.isArray((shift as {[P: string]: any})[k])) as (keyof ShiftView)[]) as any,
    requiredSkillSetIdList: shift.requiredSkillSet.map(skill => skill.id as number),
    startDateTime: serializeLocalDateTime(shift.startDateTime),
    endDateTime: serializeLocalDateTime(shift.endDateTime),
  };
}

export function kindaShiftViewAdapter(kindaShiftView: KindaShiftView): ShiftView {
  const kindaShiftViewClone: any = { ...kindaShiftView };
  kindaShiftViewClone.indictmentScore = getHardMediumSoftScoreFromString(kindaShiftView.indictmentScore as string);

  // Since property P is related to indictments iff it is an array,
  // We can convert all indictments by mapping all keys that are arrays
  Object.keys(kindaShiftViewClone).forEach((key) => {
    if (key !== 'requiredSkillSetIdList' && Array.isArray(kindaShiftViewClone[key])) {
      kindaShiftViewClone[key] = kindaShiftViewClone[key].map((s: any) => (
        { ...s, score: getHardMediumSoftScoreFromString(s.score) }
      ));
    }
  });

  return {
    ...kindaShiftViewClone,
    startDateTime: moment(kindaShiftView.startDateTime).toDate(),
    endDateTime: moment(kindaShiftView.endDateTime).toDate(),
  };
}
