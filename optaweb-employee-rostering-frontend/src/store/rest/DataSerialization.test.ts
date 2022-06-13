import { serializeLocalDate, serializeLocalDateTime } from './DataSerialization';

describe('Data Serializers', () => {
  it('Local date serializer should be in YYYY-MM-DD format', () => {
    expect(serializeLocalDate(new Date('2018-01-02'))).toEqual('2018-01-02');
  });

  it('Local datetime serializer should be in YYYY-MM-DDThh:mm:ss format', () => {
    expect(serializeLocalDateTime(new Date('2018-01-02T16:30:15'))).toEqual('2018-01-02T16:30:15');
  });
});
