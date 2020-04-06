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
import { useTranslation } from 'react-i18next';
import { HardMediumSoftScore, convertHardMediumSoftScoreToString } from 'domain/HardMediumSoftScore';
import { Chip, Button, ButtonVariant, Popover, List } from '@patternfly/react-core';
import { IndictmentSummary } from 'domain/indictment/IndictmentSummary';
import { HelpIcon } from '@patternfly/react-icons';

export interface ScoreDisplayProps {
  score: HardMediumSoftScore;
  indictmentSummary: IndictmentSummary;
}

const ConstraintMatches: React.FC<ScoreDisplayProps> = props => (
  <List>
    {Object.keys(props.indictmentSummary.constraintToCountMap).sort().map(constraint => (
      <li>
        {
          `${constraint}: ${props.indictmentSummary.constraintToCountMap[constraint]}
                (Impact: 
                ${convertHardMediumSoftScoreToString(props.indictmentSummary.constraintToScoreImpactMap[constraint])
      })`
        }
      </li>
    ))}
  </List>
);

export const ScoreDisplay: React.FC<ScoreDisplayProps> = (props) => {
  const { t } = useTranslation('ScoreDisplay');
  const { hardScore, mediumScore, softScore } = props.score;
  const [isOpen, setIsOpen] = React.useState(false);

  return (
    <span>
      <Chip isReadOnly>
        {t('hardScore', { hardScore })}
      </Chip>
      <Chip isReadOnly>
        {t('mediumScore', { mediumScore })}
      </Chip>
      <Chip isReadOnly>
        {t('softScore', { softScore })}
      </Chip>
      <Popover
        headerContent="Constraint Matches"
        bodyContent={<ConstraintMatches {...props} />}
        boundary="viewport"
        maxWidth="800px"
        shouldClose={() => setIsOpen(false)}
        isVisible={isOpen}
      >
        <Button variant={ButtonVariant.plain} onClick={() => setIsOpen(!isOpen)}>
          <HelpIcon />
        </Button>
      </Popover>
    </span>
  );
};
