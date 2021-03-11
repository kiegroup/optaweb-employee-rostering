/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import { Modal, Text, TextContent, TextVariants } from '@patternfly/react-core';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UnpluggedIcon } from '@patternfly/react-icons';
import { useSelector } from 'react-redux';
import { AppState } from 'store/types';

export const ConnectionStatus: React.FC<{}> = () => {
  const { t } = useTranslation('ConnectionStatus');
  const isConnected = useSelector((state: AppState) => state.isConnected);
  return (
    <Modal title={t('cannotConnect')} isOpen={!isConnected} variant="small">
      <TextContent>
        <Text component={TextVariants.h3}>
          <UnpluggedIcon />
          {t('cannotConnectMsg')}
        </Text>
      </TextContent>
    </Modal>
  );
};
