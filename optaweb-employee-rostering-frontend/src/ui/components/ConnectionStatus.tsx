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
