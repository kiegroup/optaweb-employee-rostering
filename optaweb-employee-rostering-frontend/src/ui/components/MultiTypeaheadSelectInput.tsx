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
import Select from 'react-select';

export interface MultiTypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  value: T[];
  optionToStringMap: (option: T) => string;
  onChange: (selected: T[]) => void;
  optional?: boolean;
  noClearButton?: boolean;
  autoSize?: boolean;
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

export default class MultiTypeaheadSelectInput<T> extends React.Component<
MultiTypeaheadSelectProps<T>
> {
  constructor(props: MultiTypeaheadSelectProps<T>) {
    super(props);
    this.onSelect = this.onSelect.bind(this);
  }

  onSelect(selections: { value: T }[]|null) {
    if (selections !== null) {
      this.props.onChange(selections.map(s => s.value));
    } else {
      this.props.onChange([]);
    }
  }

  render() {
    const { optionToStringMap, emptyText, optional, options, autoSize } = this.props;
    const selectOptions = this.props.options.map(o => ({ value: o }));
    // Why map to string first?
    // Clone of objects are not the same, and sometimes we are passed
    // clones instead of the real thing, and "===" and "==" are
    // both identity/is comparators on objects, so we compare their
    // "toString" values to check for equality
    const selected = selectOptions.filter(o => this.props.value
      .find(item => optionToStringMap(item) === optionToStringMap(o.value)) !== undefined);
    const parentStyle: React.CSSProperties = {
      position: 'relative',
    };
    if (autoSize === undefined || autoSize) {
      const maxLabelLength = Math.max(emptyText.length, ...options.map(optionToStringMap).map(s => s.length));
      parentStyle.width = `${maxLabelLength + 2}em`;
    }

    return (
      <div style={parentStyle}>
        <Select
          aria-label={emptyText}
          styles={{
            menuPortal: provided => ({
              ...provided,
              zIndex: 9999999,
            }),
          }}
          onChange={this.onSelect}
          defaultValue={selected}
          value={selected}
          selected={selected}
          placeholder={emptyText}
          getOptionLabel={o => optionToStringMap(o.value)}
          getOptionValue={o => optionToStringMap(o.value)}
          options={selectOptions}
          menuPortalTarget={document.body}
          isMulti
          isClearable={optional}
        />
      </div>
    );
  }
}
