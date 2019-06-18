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
import * as React from "react";
import { Select, SelectOption, SelectVariant } from "@patternfly/react-core";

export type Predicate<T> = (value: T) => boolean;

export interface Filter<T> {
    name: string,
    getComponent: (setFilter: (newFilter: Predicate<T>|undefined) => void) => JSX.Element
}

export interface FilterProps<T> {
    filters: Filter<T>[],
    filterListParentId: string,
    onChange: (filter: Predicate<T>) => void
}

export interface FilterState<T> {
    isFilterSelectExpanded: boolean,
    currentFilterComponent: number,
    editedFilter: Predicate<T>|undefined,
    mounted: boolean
}

export default class FilterComponent<T> extends React.Component<FilterProps<T>, FilterState<T>> {
    constructor(props: FilterProps<T>) {
        super(props);
        this.state = {
            isFilterSelectExpanded: false,
            currentFilterComponent: 0,
            editedFilter: undefined,
            mounted: false
        };
        this.onFilterSelectToggle = this.onFilterSelectToggle.bind(this);
        this.onFilterSelectSelect = this.onFilterSelectSelect.bind(this);
    }

    componentDidMount() {
      this.setState({mounted: true})
    }

    onFilterSelectToggle(isFilterSelectExpanded: boolean) {
      this.setState({
        isFilterSelectExpanded
      });
    }

    onFilterSelectSelect(event: React.SyntheticEvent<HTMLOptionElement,Event>, selection: string, isPlaceholder: boolean) {
      this.setState({
        currentFilterComponent: this.props.filters.findIndex((f) => f.name === selection)
      });
    }

    render() {
        return ((this.props.filters.length !== 0)?
            <span className="form-group toolbar-pf-filter" style={{display: "grid", gridTemplateColumns: "max-content max-content"}}>
              <Select
                variant={SelectVariant.single}
                aria-label={"Select a filter..."}
                onToggle={this.onFilterSelectToggle}
                onSelect={this.onFilterSelectSelect}
                selections={this.props.filters[this.state.currentFilterComponent].name}
                isExpanded={this.state.isFilterSelectExpanded}
              >
                {this.props.filters.map((option, index) => (
                  <SelectOption
                    isDisabled={false}
                    key={index}
                    value={option.name}
                  />
                ))}
              </Select>
              {this.props.filters[this.state.currentFilterComponent].getComponent((filter) => {
                this.setState({
                  editedFilter: filter,
                });
                if (filter !== undefined) {
                  this.props.onChange(filter);
                }
                else {
                  this.props.onChange(v => true);
                }
              })}
            </span> : <span />);
    }
}