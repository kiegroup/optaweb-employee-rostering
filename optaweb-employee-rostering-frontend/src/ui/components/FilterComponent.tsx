import * as React from 'react';
import { TextInput, Button, ButtonVariant } from '@patternfly/react-core';
import './FilterComponent.css';
import { useTranslation } from 'react-i18next';
import 'index.css';

export interface FilterProps {
  filterText: string;
  onChange: (filterText: string) => void;
}

export const FilterComponent: React.FC<FilterProps> = (props) => {
  const { t } = useTranslation('FilterComponent');
  return (
    <div className="search-icons">
      <TextInput
        aria-label="Search"
        placeholder={t('search')}
        value={props.filterText}
        onChange={props.onChange}
      />
      {props.filterText.length !== 0 && (
        <Button
          variant={ButtonVariant.plain}
          onClick={() => props.onChange('')}
        >
          <svg
            style={{ verticalAlign: '-0.125em' }}
            fill="currentColor"
            height="1em"
            width="1em"
            viewBox="0 0 512 512"
            aria-hidden="true"
            role="img"
          >
            <path
              d={
                'M256 8C119 8 8 119 8 256s111 248 248 248 248-111 248-248S393 8 '
                + '256 8zm121.6 313.1c4.7 4.7 4.7 12.3 0 17L338 377.6c-4.7 4.7-12.3 4.7-17 '
                + '0L256 312l-65.1 65.6c-4.7 4.7-12.3 4.7-17 0L134.4 338c-4.7-4.7-4.7-12.3 '
                + '0-17l65.6-65-65.6-65.1c-4.7-4.7-4.7-12.3 0-17l39.6-39.6c4.7-4.7 12.3-4.7 '
                + '17 0l65 65.7 65.1-65.6c4.7-4.7 12.3-4.7 17 0l39.6 39.6c4.7 4.7 4.7 12.3 0 17L312 '
                + '256l65.6 65.1z'
              }
              transform=""
            />
          </svg>
        </Button>
      )}
    </div>
  );
};

export default FilterComponent;
