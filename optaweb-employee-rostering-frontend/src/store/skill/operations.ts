
import { Skill } from 'domain/Skill';
import { alert } from 'store/alert';
import { AddAlertAction } from 'store/alert/types';
import { ThunkCommandFactory } from '../types';
import * as actions from './actions';
import {
  SetSkillListLoadingAction, AddSkillAction, RemoveSkillAction, UpdateSkillAction,
  RefreshSkillListAction,
} from './types';

export const addSkill:
ThunkCommandFactory<Skill, AddAlertAction | AddSkillAction> = skill => (dispatch, state, client) => {
  const { tenantId } = skill;
  return client.post<Skill>(`/tenant/${tenantId}/skill/add`, skill).then((newSkill) => {
    dispatch(alert.showSuccessMessage('addSkill', { name: newSkill.name }));
    dispatch(actions.addSkill(newSkill));
  });
};

export const removeSkill:
ThunkCommandFactory<Skill, AddAlertAction | RemoveSkillAction> = skill => (dispatch, state, client) => {
  const { tenantId } = skill;
  const skillId = skill.id;
  return client.delete<boolean>(`/tenant/${tenantId}/skill/${skillId}`).then((isSuccess) => {
    if (isSuccess) {
      dispatch(alert.showSuccessMessage('removeSkill', { name: skill.name }));
      dispatch(actions.removeSkill(skill));
    } else {
      dispatch(alert.showErrorMessage('removeSkillError', { name: skill.name }));
    }
  });
};

export const updateSkill:
ThunkCommandFactory<Skill, AddAlertAction |UpdateSkillAction> = skill => (dispatch, state, client) => {
  const { tenantId } = skill;
  return client.post<Skill>(`/tenant/${tenantId}/skill/update`, skill).then((updatedSkill) => {
    dispatch(alert.showSuccessMessage('updateSkill', { id: skill.id }));
    dispatch(actions.updateSkill(updatedSkill));
  });
};

export const refreshSkillList:
ThunkCommandFactory<void, SetSkillListLoadingAction | RefreshSkillListAction> = () => (dispatch, state, client) => {
  const tenantId = state().tenantData.currentTenantId;
  dispatch(actions.setIsSkillListLoading(true));
  return client.get<Skill[]>(`/tenant/${tenantId}/skill/`).then((skillList) => {
    dispatch(actions.refreshSkillList(skillList));
    dispatch(actions.setIsSkillListLoading(false));
  });
};
