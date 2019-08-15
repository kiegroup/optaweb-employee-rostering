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
import { Split, SplitItem, Button, Level, LevelItem, Popover, Text, ButtonVariant } from "@patternfly/react-core";
import { EditIcon, TrashIcon, OkIcon, WarningTriangleIcon, ErrorCircleOIcon } from '@patternfly/react-icons';
import moment from "moment";

export interface AvailabilityEventProps {
  availability: EmployeeAvailability;
  onEdit: (ea: EmployeeAvailability) => void;
  onDelete: (ea: EmployeeAvailability) => void;
  updateEmployeeAvailability: (ea: EmployeeAvailability) => void;
  removeEmployeeAvailability: (ea: EmployeeAvailability) => void;
}
  
const AvailabilityEvent: React.FC<AvailabilityEventProps> = (props: AvailabilityEventProps) => {
  const { t } = useTranslation();
  return (
    <Popover
      className="my-popup"
      key={props.availability.id}
      position="right"
      bodyContent={(<></>)}
      headerContent={(
        <span>
          <Text>
            {
              props.availability.employee.name + ", " + 
            moment(props.availability.startDateTime).format("LT") + "-" + 
            moment(props.availability.endDateTime).format("LT")
            }
          </Text>
          <Button
            onClick={() => props.onEdit(props.availability)}
            variant={ButtonVariant.link}
          >
            <EditIcon />
          </Button>
          <Button
            onClick={() => props.onDelete(props.availability)}
            variant={ButtonVariant.link}
          >
            <TrashIcon />
          </Button>
        </span>
      )}
    >
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
              title={t("EmployeeAvailabilityState.DESIRED")}
              onClick={() => props.updateEmployeeAvailability({
                ...props.availability,
                state: "DESIRED"
              })}
              style={{
                backgroundColor: "green",
                margin: "1px",
                width: "min-content"
              }}
              variant="tertiary"
            >
              <OkIcon />
            </Button>
            <Button
              title={t("EmployeeAvailabilityState.UNDESIRED")}
              onClick={() => props.updateEmployeeAvailability({
                ...props.availability,
                state: "UNDESIRED"
              })}
              style={{
                backgroundColor: "yellow",
                margin: "1px",
                width: "min-content"
              }}
              variant="tertiary"
            >
              <WarningTriangleIcon />
            </Button>
            <Button
              title={t("EmployeeAvailabilityState.UNAVAILABLE")}
              onClick={() => props.updateEmployeeAvailability({
                ...props.availability,
                state: "UNAVAILABLE"
              })}
              style={{
                backgroundColor: "red",
                margin: "1px",
                width: "min-content"
              }}
              variant="tertiary"
            >
              <ErrorCircleOIcon />
            </Button>
          </LevelItem>
        </Level>
      </span>
    </Popover>
  );
}

export default AvailabilityEvent;