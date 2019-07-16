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

export interface MultiTypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  defaultValue: T[];
  optionToStringMap: (option: T) => string;
  onChange: (selected: T[]) => void;
}

export interface MultiTypeaheadSelectState<T> {
  isExpanded: boolean;
  selected: T[];
}

export default class MultiTypeaheadSelectInput<T> extends React.Component<MultiTypeaheadSelectProps<T>,
MultiTypeaheadSelectState<T>>  {

  constructor(props: MultiTypeaheadSelectProps<T>) {
    super(props);

    this.onToggle = this.onToggle.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.clearSelection = this.clearSelection.bind(this);

    this.state = {
      isExpanded: false,
      selected: [...props.defaultValue]
    };
  }

  onToggle(isExpanded: boolean) {
    this.setState({
      isExpanded
    });
  }

  onSelect(event: React.SyntheticEvent<HTMLOptionElement,Event>, selection: string, isPlaceholder: boolean) {
    const { selected } = this.state;
    const selectedOption = this.props.options.find((option) => this.props.optionToStringMap(option) === selection) as T;
    if (selected.map(this.props.optionToStringMap).includes(selection)) {
      this.setState(
        prevState => {
          const newState = {selected: prevState.selected.filter(option => this.props.optionToStringMap(option) !== selection)};
          this.props.onChange(newState.selected);
          return newState;
        }
      );
    } else {
      this.setState(
        prevState => {
          const newState = { selected: [...prevState.selected, selectedOption] };
          this.props.onChange(newState.selected);
          return newState;
        }
      );
    }
  }

  clearSelection() {
    this.setState({
      selected: [],
      isExpanded: false,
    });
    this.props.onChange([]);
  }

  render() {
    const { isExpanded, selected } = this.state;
    const titleId = 'multi-typeahead-select-id';
    const emptyText = this.props.emptyText;
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
          selections={selections}
          isExpanded={isExpanded}
          ariaLabelledBy={titleId}
          placeholderText={emptyText}
        >
          {this.props.options.map((option) => (
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
