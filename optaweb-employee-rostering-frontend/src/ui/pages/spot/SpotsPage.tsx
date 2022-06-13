import React, { useState } from 'react';
import {
  TableRow, TableCell, RowViewButtons, RowEditButtons,
  DataTableUrlProps, setSorterInUrl, DataTable,
} from 'ui/components/DataTable';
import MultiTypeaheadSelectInput from 'ui/components/MultiTypeaheadSelectInput';
import { spotSelectors, spotOperations } from 'store/spot';
import { skillSelectors } from 'store/skill';
import { Spot } from 'domain/Spot';
import { TextInput, Text, Chip, ChipGroup, Button, ButtonVariant, FlexItem, Flex } from '@patternfly/react-core';
import { useDispatch, useSelector } from 'react-redux';
import { Sorter } from 'types';
import { stringSorter } from 'util/CommonSorters';
import { useTranslation } from 'react-i18next';
import * as router from 'react-router';
import { ArrowIcon } from '@patternfly/react-icons';
import { tenantSelectors } from 'store/tenant';
import { useValidators } from 'util/ValidationUtils';
import { getPropsFromUrl } from 'util/BookmarkableUtils';
import { usePageableData } from 'util/FunctionalComponentUtils';

export type Props = router.RouteComponentProps;

export const SpotRow = (spot: Spot) => {
  const [isEditing, setIsEditing] = useState(false);
  const dispatch = useDispatch();
  const tenantId = useSelector(tenantSelectors.getTenantId);
  const history = router.useHistory();
  const { t } = useTranslation('SpotsPage');

  if (isEditing) {
    return (<EditableSpotRow spot={spot} isNew={false} onClose={() => setIsEditing(false)} />);
  }

  return (
    <TableRow>
      <TableCell columnName={t('name')}>
        <Flex>
          <FlexItem>
            <Text>{spot.name}</Text>
          </FlexItem>
          <FlexItem>
            <Button
              variant={ButtonVariant.link}
              onClick={() => {
                history.push(`/${tenantId}/adjust?spot=${encodeURIComponent(spot.name)}`);
              }}
            >
              <ArrowIcon />
            </Button>
          </FlexItem>
        </Flex>
      </TableCell>
      <TableCell columnName={t('requiredSkillSet')}>
        <ChipGroup>
          {spot.requiredSkillSet.map(skill => (
            <Chip key={skill.name} isReadOnly>
              {skill.name}
            </Chip>
          ))}
        </ChipGroup>
      </TableCell>
      <RowViewButtons
        onEdit={() => setIsEditing(true)}
        onDelete={() => dispatch(spotOperations.removeSpot(spot))}
      />
    </TableRow>
  );
};

export const EditableSpotRow = (props: { spot: Spot; isNew: boolean; onClose: () => void }) => {
  const [name, setName] = useState(props.spot.name);
  const [requiredSkillSet, setRequiredSkillSet] = useState(props.spot.requiredSkillSet);
  const spotList = useSelector(spotSelectors.getSpotList);
  const skillList = useSelector(skillSelectors.getSkillList);
  const dispatch = useDispatch();
  const { t } = useTranslation('SpotsPage');

  const validators = {
    nameMustNotBeEmpty: {
      predicate: (spot: Spot) => spot.name.length > 0,
      errorMsg: () => t('spotEmptyNameError'),
    },
    nameAlreadyTaken: {
      predicate: (spot: Spot) => spotList.filter(otherSpot => otherSpot.name === spot.name
        && otherSpot.id !== spot.id).length === 0,
      errorMsg: (spot: Spot) => t('spotNameAlreadyTakenError', { name: spot.name }),
    },
  };

  const validationErrors = useValidators({
    ...props.spot,
    name,
    requiredSkillSet,
  }, validators);

  return (
    <TableRow>
      <TableCell columnName={t('name')}>
        <TextInput value={name} onChange={setName} />
        {validationErrors.showValidationErrors('nameMustNotBeEmpty', 'nameAlreadyTaken')}
      </TableCell>
      <TableCell columnName={t('requiredSkillSet')}>
        <MultiTypeaheadSelectInput
          value={requiredSkillSet}
          options={skillList}
          optionToStringMap={skill => skill.name}
          onChange={setRequiredSkillSet}
          emptyText={t('selectRequiredSkills')}
        />
      </TableCell>
      <RowEditButtons
        isValid={validationErrors.isValid}
        onSave={() => {
          if (props.isNew) {
            dispatch(spotOperations.addSpot({
              ...props.spot,
              name,
              requiredSkillSet,
            }));
          } else {
            dispatch(spotOperations.updateSpot({
              ...props.spot,
              name,
              requiredSkillSet,
            }));
          }
        }}
        onClose={() => props.onClose()}
      />
    </TableRow>
  );
};

export const SpotsPage: React.FC<Props> = (props) => {
  const spotList = useSelector(spotSelectors.getSpotList);
  const tenantId = useSelector(tenantSelectors.getTenantId);

  const { t } = useTranslation('SpotsPage');

  const columns = [
    { name: t('name'), sorter: stringSorter<Spot>(spot => spot.name) },
    { name: t('requiredSkillSet') },
  ];

  const urlProps = getPropsFromUrl<DataTableUrlProps>(props, {
    page: '1',
    itemsPerPage: '10',
    filter: null,
    sortBy: '0',
    asc: 'true',
  });

  const sortBy = parseInt(urlProps.sortBy || '-1', 10);
  const sorter = columns[sortBy].sorter as Sorter<Spot>;

  const pageableData = usePageableData(urlProps, spotList, spot => [spot.name,
    ...spot.requiredSkillSet.map(skill => skill.name)], sorter);

  return (
    <DataTable
      {...props}
      {...pageableData}
      title={t('spots')}
      columns={columns}
      rowWrapper={spot => (<SpotRow key={spot.id} {...spot} />)}
      sortByIndex={sortBy}
      onSorterChange={index => setSorterInUrl(props, urlProps, sortBy, index)}
      newRowWrapper={removeRow => (
        <EditableSpotRow
          isNew
          onClose={removeRow}
          spot={{
            tenantId,
            name: '',
            requiredSkillSet: [],
          }}
        />
      )}
    />
  );
};

export default router.withRouter(SpotsPage);
