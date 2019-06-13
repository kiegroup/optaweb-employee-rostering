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

export interface OptionalInputProps<T extends Record<string, any>> {
  defaultValue: T|null;
  isValid: (value: string) => boolean;
  valueMapper: (value: string) => T;
  onChange: (value: T|null) => void;
  label: string;
}

interface OptionalInputState<T extends Record<string, any>> {
  inputValue: T|null;
  currentValue: T|null;
  isChecked: boolean;
}

export default class OptionalInput<T  extends Record<string, any>> extends React.Component<OptionalInputProps<T>,OptionalInputState<T>> {
  constructor(props: OptionalInputProps<T>) {
    super(props);
    this.state = {
      inputValue: props.defaultValue,
      currentValue: props.defaultValue,
      isChecked: props.defaultValue !== null
    };
    this.handleToggle = this.handleToggle.bind(this);
  }

  handleToggle(isChecked: boolean) {
    if (isChecked) {
      this.setState({currentValue: this.state.inputValue, isChecked: true});
      this.props.onChange(this.state.inputValue);
    }
    else {
      this.setState({currentValue: null, isChecked: false});
      this.props.onChange(null);
    }
  }

  render() {
    const { isChecked } = this.state;
    return (
      <span>
        <TextInput aria-label={this.props.label} isDisabled={!isChecked}
          defaultValue={(this.props.defaultValue !== null)? this.props.defaultValue.toString() : ''}
          onChange={(value) => {
            if (this.props.isValid(value)) {
              const mappedValue = this.props.valueMapper(value);
              this.setState({inputValue: mappedValue});
              if (this.state.isChecked) {
                this.setState({currentValue: mappedValue});
                this.props.onChange(mappedValue);
              }
            }
          }}/>
        <Switch aria-label="Enabled" isChecked={isChecked} onChange={this.handleToggle} />
      </span>
    );
  }
}
