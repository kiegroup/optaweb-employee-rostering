import { useValidators, Validator, useListValidators } from 'util/ValidationUtils';

describe('Validation Utils', () => {
  const lengthGreaterThan2: Validator<string> = {
    predicate: str => str.length > 2,
    errorMsg: str => `"${str}" should have a length of at least 3`,
  };
  const startsWithA: Validator<string> = {
    predicate: str => str.length === 0 || str.charAt(0) === 'A',
    errorMsg: str => `"${str}" should start with "A"`,
  };

  it('isValid should be true iff all validators predicates return true', () => {
    let validationResult = useValidators('An', {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(false);
    expect(validationResult.validationErrors).toEqual(['lengthGreaterThan2']);
    expect(
      validationResult.showValidationErrors('startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors('lengthGreaterThan2'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors('startsWithA', 'lengthGreaterThan2'),
    ).not.toEqual(null);

    validationResult = useValidators('Beth', {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(false);
    expect(validationResult.validationErrors).toEqual(['startsWithA']);
    expect(
      validationResult.showValidationErrors('startsWithA'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors('lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors('startsWithA', 'lengthGreaterThan2'),
    ).not.toEqual(null);

    validationResult = useValidators('B', {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(false);
    expect(validationResult.validationErrors).toEqual(
      expect.arrayContaining(['startsWithA', 'lengthGreaterThan2']),
    );
    expect(
      validationResult.showValidationErrors('startsWithA'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors('lengthGreaterThan2'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors('startsWithA', 'lengthGreaterThan2'),
    ).not.toEqual(null);

    validationResult = useValidators('Ants', {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(true);
    expect(validationResult.validationErrors).toEqual([]);
    expect(
      validationResult.showValidationErrors('startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors('lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors('startsWithA', 'lengthGreaterThan2'),
    ).toEqual(null);
  });

  it('isValid should be true iff all validators predicates return true for each item in list', () => {
    let validationResult = useListValidators(['An', 'Ants'], {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(false);
    expect(validationResult.validationErrors).toEqual([['lengthGreaterThan2'], []]);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'lengthGreaterThan2'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA', 'lengthGreaterThan2'),
    ).not.toEqual(null);

    expect(
      validationResult.showValidationErrors(1, 'startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'startsWithA', 'lengthGreaterThan2'),
    ).toEqual(null);

    validationResult = useListValidators(['Beth', 'Ants'], {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(false);
    expect(validationResult.validationErrors).toEqual([['startsWithA'], []]);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA', 'lengthGreaterThan2'),
    ).not.toEqual(null);

    expect(
      validationResult.showValidationErrors(1, 'startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'startsWithA', 'lengthGreaterThan2'),
    ).toEqual(null);

    validationResult = useListValidators(['B', 'Ants'], {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(false);
    expect(validationResult.validationErrors).toEqual(
      [expect.arrayContaining(['startsWithA', 'lengthGreaterThan2']), []],
    );
    expect(
      validationResult.showValidationErrors(0, 'startsWithA'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'lengthGreaterThan2'),
    ).not.toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA', 'lengthGreaterThan2'),
    ).not.toEqual(null);

    expect(
      validationResult.showValidationErrors(1, 'startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'startsWithA', 'lengthGreaterThan2'),
    ).toEqual(null);

    validationResult = useListValidators(['Ants', 'Ants'], {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(validationResult.isValid).toEqual(true);
    expect(validationResult.validationErrors).toEqual([[], []]);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(0, 'startsWithA', 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'startsWithA'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'lengthGreaterThan2'),
    ).toEqual(null);
    expect(
      validationResult.showValidationErrors(1, 'startsWithA', 'lengthGreaterThan2'),
    ).toEqual(null);
  });

  it('show validation errors should render correctly', () => {
    const validationResult = useValidators('B', {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(
      validationResult.showValidationErrors('lengthGreaterThan2'),
    ).toMatchSnapshot();
    expect(
      validationResult.showValidationErrors('startsWithA'),
    ).toMatchSnapshot();
    expect(
      validationResult.showValidationErrors('startsWithA', 'lengthGreaterThan2'),
    ).toMatchSnapshot();
  });

  it('show list validation errors should render correctly', () => {
    const validationResult = useListValidators(['B'], {
      lengthGreaterThan2,
      startsWithA,
    });
    expect(
      validationResult.showValidationErrors(0, 'lengthGreaterThan2'),
    ).toMatchSnapshot();
    expect(
      validationResult.showValidationErrors(0, 'startsWithA'),
    ).toMatchSnapshot();
    expect(
      validationResult.showValidationErrors(0, 'startsWithA', 'lengthGreaterThan2'),
    ).toMatchSnapshot();
  });
});
