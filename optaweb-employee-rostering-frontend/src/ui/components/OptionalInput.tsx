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
import React from 'react';
import { Switch, TextInput } from '@patternfly/react-core';

export interface OptionalInputProps<T> {
  defaultValue: T|null;
  isValid: (value: string) => boolean;
  valueMapper: (value: string) => T;
  valueToString: (value: T) => string;
  onChange: (value: T|null|undefined) => void;
  label: string;
}

interface OptionalInputState<T> {
  inputValue: T|null|undefined;
  isChecked: boolean;
}

export default class OptionalInput<T> extends React.Component<OptionalInputProps<T>, OptionalInputState<T>> {
  constructor(props: OptionalInputProps<T>) {
    super(props);
    this.state = {
      inputValue: props.defaultValue,
      isChecked: props.defaultValue !== null,
    };
    this.handleToggle = this.handleToggle.bind(this);
  }

  handleToggle(isChecked: boolean) {
    if (isChecked) {
      this.setState({ isChecked: true });
      this.props.onChange(this.state.inputValue);
    } else {
      this.setState({ isChecked: false });
      this.props.onChange(null);
    }
  }

  render() {
    const { isChecked } = this.state;
    return (
      <span>
        <TextInput
          aria-label={this.props.label}
          isDisabled={!isChecked}
          defaultValue={(this.props.defaultValue !== null) ? this.props.valueToString(this.props.defaultValue) : ''}
          onChange={(value) => {
            if (this.props.isValid(value)) {
              const mappedValue = this.props.valueMapper(value);
              this.setState({ inputValue: mappedValue });
              if (this.state.isChecked) {
                this.props.onChange(mappedValue);
              }
            } else {
              this.props.onChange(undefined);
            }
          }}
        />
        <Switch aria-label="Enabled" isChecked={isChecked} onChange={this.handleToggle} />
      </span>
    );
  }
}
