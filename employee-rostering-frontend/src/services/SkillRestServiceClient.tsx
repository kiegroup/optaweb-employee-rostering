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


import RestServiceClient from './RestServiceClient';
import Skill from '../domain/Skill';

class SkillRestServiceClient {
    restClient = new RestServiceClient();

    getSkillList(tenantId : number) : Promise<Skill[]> {
        return this.restClient.get(`/tenant/${tenantId}/skill/`);
    }

    getSkill(tenantId : number, skillId : number) : Promise<Skill> {
        return this.restClient.get(`/tenant/${tenantId}/skill/${skillId}`);
    }

    addSkill(tenantId : number, skill : Skill) : Promise<Skill> {
        return this.restClient.post(`/tenant/${tenantId}/skill/add`, skill);
    }

    updateSkill(tenantId : number, skill : Skill) : Promise<Skill> {
        return this.restClient.post(`/tenant/${tenantId}/skill/update`, skill);
    }

    deleteSkill(tenantId : number, skillId : number) : Promise<boolean> {
        return this.restClient.delete(`/tenant/${tenantId}/skill/${skillId}`);
    }
}

export default SkillRestServiceClient;
