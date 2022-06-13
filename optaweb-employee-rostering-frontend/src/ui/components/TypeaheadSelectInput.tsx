import React from 'react';
import Select, { CommonProps, components } from 'react-select';

export interface TypeaheadSelectProps<T> {
  emptyText: string;
  options: T[];
  value: T | undefined;
  optionToStringMap: (option: T) => string;
  onChange: (selected: T | undefined) => void;
  optional?: boolean;
  noClearButton?: boolean;
  autoSize?: boolean;
  valueComponent?: React.FC<CommonProps<{ value: T}> & { data: {value: T}}>;
}

export default class TypeaheadSelectInput<T> extends React.Component<TypeaheadSelectProps<T>> {
  constructor(props: TypeaheadSelectProps<T>) {
    super(props);
    this.onSelect = this.onSelect.bind(this);
  }

  onSelect(selection: { value: T }|undefined) {
    this.props.onChange(selection ? selection.value : undefined);
  }

  render() {
    const { optionToStringMap, emptyText, optional, options, autoSize } = this.props;
    const selectOptions = this.props.options.map(o => ({ value: o }));
    const Display = this.props.valueComponent;
    const SingleValue = Display || components.SingleValue;
    const Option = Display ? (props: any, children: any) => (
      <components.Option {...props}>
        <Display {...props}>{children}</Display>
      </components.Option>
    )
      : components.Option;

    // Why map to string first?
    // Clone of objects are not the same, and sometimes we are passed
    // clones instead of the real thing, and "===" and "==" are
    // both identity/is comparators on objects, so we compare their
    // "toString" values to check for equality
    const selected = (this.props.value !== undefined) ? selectOptions
      .find(o => optionToStringMap(o.value) === optionToStringMap(this.props.value as T)) : undefined;
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
            singleValue: provided => ({
              ...provided,
            }),
          }}
          components={{ SingleValue, Option }}
          onChange={this.onSelect}
          defaultValue={selected}
          value={selected}
          selected={selected}
          placeholder={emptyText}
          getOptionLabel={o => optionToStringMap(o.value)}
          getOptionValue={o => optionToStringMap(o.value)}
          options={selectOptions}
          menuPortalTarget={document.body}
          isClearable={optional}
        />
      </div>
    );
  }
}
