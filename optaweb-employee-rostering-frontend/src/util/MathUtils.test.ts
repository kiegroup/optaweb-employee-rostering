import * as mathUtils from './MathUtils';

describe('Math Utils', () => {
  it('Modulo should return the smallest non-negative integer that is congruent to n mod base', () => {
    expect(mathUtils.modulo(-10, 100)).toEqual(90);
    expect(mathUtils.modulo(10, -100)).toEqual(10);
    expect(mathUtils.modulo(10, 100)).toEqual(10);
    expect(mathUtils.modulo(110, 100)).toEqual(10);
    expect(mathUtils.modulo(100, 10)).toEqual(0);
  });
});
