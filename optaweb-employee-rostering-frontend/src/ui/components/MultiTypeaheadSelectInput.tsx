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

export interface MultiTypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  value: T[];
  optionToStringMap: (option: T) => string;
  onChange: (selected: T[]) => void;
}

export interface MultiTypeaheadSelectState {
  isExpanded: boolean;
}

const StatefulMultiTypeaheadSelectInput: React.FC<MultiTypeaheadSelectProps<any>> = (props) => {
  const [value, setValue] = React.useState(props.value);
  return (
    <MultiTypeaheadSelectInput
      {...props}
      value={value}
      onChange={(v) => { props.onChange(v); setValue(v); }}
    />
  );
};

export { StatefulMultiTypeaheadSelectInput };

export const substringFilter = (props: MultiTypeaheadSelectProps<any>) => (e: React.ChangeEvent<HTMLInputElement>) => {
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

export default class MultiTypeaheadSelectInput<T> extends React.Component<MultiTypeaheadSelectProps<T>,
MultiTypeaheadSelectState> {
  constructor(props: MultiTypeaheadSelectProps<T>) {
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

  onSelect(event: any, selection: string) {
    const selected = this.props.value;
    const selectedOption = this.props.options.find(option => this.props.optionToStringMap(option) === selection) as T;
    if (selected.map(this.props.optionToStringMap).includes(selection)) {
      this.props.onChange(this.props.value.filter(option => this.props.optionToStringMap(option) !== selection));
    } else {
      this.props.onChange([...this.props.value, selectedOption]);
    }
  }

  clearSelection() {
    this.setState({
      isExpanded: false,
    });
    this.props.onChange([]);
  }

  render() {
    const { isExpanded } = this.state;
    const selected = this.props.value;
    const titleId = 'multi-typeahead-select-id';
    const { emptyText } = this.props;
    const selections = selected.map(this.props.optionToStringMap);

    return (
      <div>
        <span id={titleId} hidden>
          {emptyText}
        </span>
        <Select
          variant={SelectVariant.typeaheadMulti}
          aria-label={emptyText}
          onToggle={this.onToggle}
          onSelect={this.onSelect as any}
          onClear={this.clearSelection}
          onFilter={substringFilter(this.props)}
          selections={selections}
          isExpanded={isExpanded}
          ariaLabelledBy={titleId}
          placeholderText={emptyText}
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
