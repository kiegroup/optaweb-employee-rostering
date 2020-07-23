/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
