import moment from 'moment';

export function serializeLocalDate(date: Date): string {
  return moment(date).format('YYYY-MM-DD');
}

export function serializeLocalDateTime(date: Date): string {
  return moment(date).local().format('YYYY-MM-DDTHH:mm:ss');
}
