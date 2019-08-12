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
import * as React from 'react';
import EmployeeAvailability from "domain/EmployeeAvailability";
import { useTranslation } from "react-i18next";
import { Split, SplitItem, Button, Level, LevelItem } from "@patternfly/react-core";
import { TrashIcon, OkIcon, WarningTriangleIcon, ErrorCircleOIcon } from '@patternfly/react-icons';

export interface AvailabilityEventProps {
  availability: EmployeeAvailability;
  updateEmployeeAvailability: (ea: EmployeeAvailability) => void;
  removeEmployeeAvailability: (ea: EmployeeAvailability) => void;
}
  
const AvailabilityEvent: React.FC<AvailabilityEventProps> = (props: AvailabilityEventProps) => {
  const { t } = useTranslation();
  return (
    <span
      data-tip
      data-for={String(props.availability.id)}
      className="availability-event"
  
    >
      <Split>
        <SplitItem isFilled={false}>{t("EmployeeAvailabilityState." + props.availability.state)}</SplitItem>
        <SplitItem isFilled />
        <SplitItem isFilled={false}>
          <Button
            onClick={() => props.removeEmployeeAvailability(props.availability)}
            variant="danger"
          >
            <TrashIcon />
          </Button>
        </SplitItem>
      </Split>
      <Level gutter="sm">
        <LevelItem>
          <Button
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: "DESIRED"
            })}
            style={{
              backgroundColor: "green",
              margin: "5px"
            }}
            variant="tertiary"
          >
            <OkIcon />
          </Button>
          <Button
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: "UNDESIRED"
            })}
            style={{
              backgroundColor: "yellow",
              margin: "5px"
            }}
            variant="tertiary"
          >
            <WarningTriangleIcon />
          </Button>
          <Button
            onClick={() => props.updateEmployeeAvailability({
              ...props.availability,
              state: "UNAVAILABLE"
            })}
            style={{
              backgroundColor: "red",
              margin: "5px"
            }}
            variant="tertiary"
          >
            <ErrorCircleOIcon />
          </Button>
        </LevelItem>
      </Level>
    </span>
  );
}

export default AvailabilityEvent;