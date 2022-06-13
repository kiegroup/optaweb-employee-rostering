
import React, { FC, PropsWithChildren } from 'react';
import { Modal, Button, ButtonVariant } from '@patternfly/react-core';
import { useTranslation } from 'react-i18next';

export interface ConfirmDialogProps {
  title: string;
  isOpen: boolean;
  onConfirm: () => void;
  onClose: () => void;
}

export const ConfirmDialog: FC<PropsWithChildren<ConfirmDialogProps>> = (props) => {
  const { t } = useTranslation('ConfirmDialog');
  return (
    <Modal
      title={props.title}
      onClose={props.onClose}
      isOpen={props.isOpen}
      actions={
        [(
          <Button
            aria-label="Close Modal"
            variant={ButtonVariant.tertiary}
            key={0}
            onClick={props.onClose}
          >
            {t('close')}
          </Button>
        ),
        (
          <Button
            aria-label={t('confirm')}
            data-cy="confirm"
            key={2}
            onClick={() => { props.onClose(); props.onConfirm(); }}
          >
            {t('confirm')}
          </Button>
        ),
        ]
      }
      variant="small"
    >
      {props.children}
    </Modal>
  );
};
