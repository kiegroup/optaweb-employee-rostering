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
import React from "react";
import { Select, SelectOption, SelectVariant } from "@patternfly/react-core";

export interface TypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  defaultValue: T | undefined;
  optionToStringMap: (option: T) => string;
  onChange: (selected: T | undefined) => void;
}

export interface TypeaheadSelectState<T> {
  isExpanded: boolean;
  selected: T | undefined;
}

export default class TypeaheadSelectInput<T> extends React.Component<
TypeaheadSelectProps<T>,
TypeaheadSelectState<T>
> { 
  constructor(props: TypeaheadSelectProps<T>) {
    super(props);

    this.onToggle = this.onToggle.bind(this);
    this.onSelect = this.onSelect.bind(this);
    this.clearSelection = this.clearSelection.bind(this);

    this.state = {
      isExpanded: false,
      selected: props.defaultValue
    };
  }

  onToggle(isExpanded: boolean) {
    this.setState({
      isExpanded
    });
  }

  clearSelection() {
    this.props.onChange(undefined);
    this.setState({
      selected: undefined,
      isExpanded: false
    });
  }

  onSelect(
    event: React.SyntheticEvent<HTMLOptionElement, Event>,
    selection: string,
    isPlaceholder: boolean
  ) {
    const selectedOption = this.props.options.find(
      option => this.props.optionToStringMap(option) === selection
    ) as T;

    this.props.onChange(selectedOption);
    this.setState({
      selected: selectedOption
    });
  }

  render() {
    const { isExpanded, selected } = this.state;
    const titleId = "typeahead-select-id";
    const emptyText = this.props.emptyText;
    const selection =
      selected !== undefined ? this.props.optionToStringMap(selected) : null;

    return (
      <div>
        <span id={titleId} hidden>
          {emptyText}
        </span>
        <Select
          variant={SelectVariant.typeahead}
          aria-label={emptyText}
          onToggle={this.onToggle}
          onSelect={this.onSelect}
          onClear={this.clearSelection}
          selections={selection}
          isExpanded={isExpanded}
          ariaLabelledBy={titleId}
          placeholderText={emptyText}
        >
          {this.props.options.map((option, index) => (
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
