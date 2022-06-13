import * as React from 'react';
import { Predicate } from 'types';
import { InputGroupText, Tooltip, List, ListItem } from '@patternfly/react-core';
import { WarningTriangleIcon } from '@patternfly/react-icons';

export interface Validator<T> {
  predicate: Predicate<T>;
  errorMsg: (value: T) => string;
}
export type ValidatorGroupMap<K extends string, T> = Record<K, Validator<T>>;
export interface ValidationResult<K> {
  isValid: boolean;
  validationErrors: K[];
  showValidationErrors: (...validatorGroups: K[]) => React.ReactNode;
}

export interface ValidationListResult<K> {
  isValid: boolean;
  validationErrors: K[][];
  showValidationErrors: (index: number, ...validatorGroups: K[]) => React.ReactNode;
}


export function useValidators<K extends string, T>(value: T, validators: ValidatorGroupMap<K, T>): ValidationResult<K> {
  const validationErrors: K[] = Object.keys(validators).filter((key) => {
    const validator = validators[key as K];
    return !validator.predicate(value);
  }) as K[];
  const isValid = validationErrors.length === 0;
  const showValidationErrors = (...shownValidatorGroups: K[]) => {
    const invalidatedShownValidatorGroups = shownValidatorGroups.filter(group => validationErrors.includes(group));
    if (invalidatedShownValidatorGroups.length === 0) {
      // If there are no validation errors within the specified validation groups,
      // then nothing is rendered
      return null;
    }

    return (
      <InputGroupText key={0}>
        <Tooltip
          content={(
            <List
              style={{
                color: 'white',
              }}
            >
              {invalidatedShownValidatorGroups.map(key => (
                <ListItem key={key}>
                  {validators[key].errorMsg(value)}
                </ListItem>
              ))
              }
            </List>
          )}
        >
          <WarningTriangleIcon />
        </Tooltip>
      </InputGroupText>
    );
  };
  return {
    isValid,
    validationErrors,
    showValidationErrors,
  };
}

export function useListValidators<K extends string, T>(value: T[], validators: ValidatorGroupMap<K, T>):
ValidationListResult<K> {
  const validationErrors: K[][] = value.map(item => Object.keys(validators).filter((key) => {
    const validator = validators[key as K];
    return !validator.predicate(item);
  }) as K[]);
  const isValid = validationErrors.reduce((prev, curr) => prev && curr.length === 0, true);
  const showValidationErrors = (index: number, ...shownValidatorGroups: K[]) => {
    const invalidatedShownValidatorGroups = shownValidatorGroups.filter(group => validationErrors[index]
      .includes(group));
    if (invalidatedShownValidatorGroups.length === 0) {
      // null is rendered as null
      return null;
    }

    return (
      <InputGroupText key={0}>
        <Tooltip
          content={(
            <List
              style={{
                color: 'white',
              }}
            >
              {invalidatedShownValidatorGroups.map(key => (
                <ListItem key={key}>
                  {validators[key].errorMsg(value[index])}
                </ListItem>
              ))
              }
            </List>
          )}
        >
          <WarningTriangleIcon />
        </Tooltip>
      </InputGroupText>
    );
  };
  return {
    isValid,
    validationErrors,
    showValidationErrors,
  };
}
