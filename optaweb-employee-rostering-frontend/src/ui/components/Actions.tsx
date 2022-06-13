
import React, { FC, useState } from 'react';
import { Button, DropdownItem, Dropdown, KebabToggle } from '@patternfly/react-core';
import { withSize, SizeMeProps } from 'react-sizeme';

export interface Props {
  actions: { name: string; action: () => void; isDisabled?: boolean }[];
}

export const Actions: FC<Props & SizeMeProps> = ({ actions, size }) => {
  const width = size.width || 0;
  const emUnitSize = 16; // Pixels in a standard 'M'; can use to get an estimate of text width

  let firstElementThatCannotFitIndex = 0;
  let remainingWidth = width - 5;

  for (let i = 0; i < actions.length && remainingWidth > 0; i += 1) {
    remainingWidth -= emUnitSize * actions[i].name.length + 6;// subtract estimated width (3pt padding on the sides)
    if (remainingWidth >= 0) {
      firstElementThatCannotFitIndex += 1;
    }
    remainingWidth -= 5;// Subtract margin
  }

  const actionsOnButtons = actions.filter((a, index) => index < firstElementThatCannotFitIndex);
  const actionsInDropdown = actions.filter((a, index) => index >= firstElementThatCannotFitIndex);

  const dropdownItems = actionsInDropdown.map(a => (
    <DropdownItem key={a.name} isDisabled={a.isDisabled || false}>
      {a.name}
    </DropdownItem>
  ));

  const [isDropdownOpen, setDropdownOpen] = useState(false);

  return (
    <span style={{ display: 'grid', gridTemplateColumns: '1fr auto' }}>
      <span style={{ width: '100%' }} />
      <span>
        {actionsOnButtons.map(a => (
          <Button
            key={a.name}
            style={{ margin: '5px' }}
            aria-label={a.name}
            onClick={a.action}
            isDisabled={a.isDisabled || false}
          >
            {a.name}
          </Button>
        ))
        }
        {actionsInDropdown.length > 0 && (
          <Dropdown
            onSelect={(e) => {
              actionsInDropdown.filter(a => e && a.name === e.currentTarget.innerText).forEach(a => a.action());
              setDropdownOpen(false);
            }}
            position="right"
            isOpen={isDropdownOpen}
            toggle={<KebabToggle onToggle={() => setDropdownOpen(!isDropdownOpen)} />}
            isPlain
            dropdownItems={dropdownItems}
          />
        )}
      </span>
    </span>
  );
};

export default withSize()(Actions);
