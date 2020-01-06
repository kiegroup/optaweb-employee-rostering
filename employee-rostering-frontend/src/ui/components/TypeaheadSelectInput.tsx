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
import { Select, SelectOption, SelectVariant } from '@patternfly/react-core';
import { stringFilter } from 'util/CommonFilters';
import './TypeaheadSelectInput.css';

export interface TypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  value: T | undefined;
  optionToStringMap: (option: T) => string;
  onChange: (selected: T | undefined) => void;
  optional?: boolean;
  noClearButton?: boolean;
}

export interface TypeaheadSelectState {
  isExpanded: boolean;
}

const StatefulTypeaheadSelectInput: React.FC<TypeaheadSelectProps<any>> = (props) => {
  const [value, setValue] = React.useState(props.value);
  return (
    <TypeaheadSelectInput
      {...props}
      value={value}
      onChange={(v) => { props.onChange(v); setValue(v); }}
    />
  );
};

export { StatefulTypeaheadSelectInput };

export const substringFilter = (props: TypeaheadSelectProps<any>) => (e: React.ChangeEvent<HTMLInputElement>) => {
  const filter = stringFilter((child: any) => child.props.value)(e.target.value);
  const options = props.options.map(option => (
    <SelectOption
      isDisabled={false}
      key={props.optionToStringMap(option)}
      value={props.optionToStringMap(option)}
    />
  ));
  const typeaheadFilteredChildren = e.target.value !== ''
    ? options.filter(filter)
    : options;
  return typeaheadFilteredChildren;
};

export default class TypeaheadSelectInput<T> extends React.Component<
TypeaheadSelectProps<T>,
TypeaheadSelectState
> {
  constructor(props: TypeaheadSelectProps<T>) {
    super(props);

    this.onToggle = this.onToggle.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.clearSelection = this.clearSelection.bind(this);

    this.state = {
      isExpanded: false,
    };
  }

  onToggle(isExpanded: boolean) {
    this.setState({
      isExpanded,
    });
  }

  clearSelection() {
    this.props.onChange(undefined);
    this.setState({
      isExpanded: false,
    });
  }

  onSelect(event: any,
    selection: string) {
    const selectedOption = this.props.options.find(
      option => this.props.optionToStringMap(option) === selection,
    ) as T;
    setTimeout(() => {
      this.props.onChange(selectedOption);
      this.setState(() => ({
        isExpanded: false,
      }));
    }, 0); // HACK: For some reason, when there are two or more Select, the
    // clear button is clicked on Keyboard enter.
  }

  render() {
    const { isExpanded } = this.state;
    const className = this.props.noClearButton ? 'no-clear-button' : '';
    const selected = this.props.value;

    const { emptyText } = this.props;
    const selection = selected !== undefined ? this.props.optionToStringMap(selected) : null;

    return (
      <div>
        <Select
          ref={(select) => {
            // Hack to get select to display selection without needing to toggle
            if (select !== null && selection !== null) {
              select.setState({
                typeaheadInputValue: selection,
              });
            }
          }}
          variant={SelectVariant.typeahead}
          aria-label={emptyText}
          onToggle={this.onToggle}
          onSelect={this.onSelect as any}
          onClear={this.clearSelection}
          onFilter={substringFilter(this.props)}
          selections={selection as any}
          isExpanded={isExpanded}
          placeholderText={emptyText}
          required={!this.props.optional}
          className={className}
        >
          {this.props.options.map(option => (
            <SelectOption
              isDisabled={false}
              key={this.props.optionToStringMap(option)}
              value={this.props.optionToStringMap(option)}
            />
          ))}
        </Select>
      </div>
    );
  }
}
