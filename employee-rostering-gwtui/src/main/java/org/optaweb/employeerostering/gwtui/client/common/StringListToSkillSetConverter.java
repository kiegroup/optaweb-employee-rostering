/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.gwtui.client.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.promise.Promise;
import org.jboss.errai.databinding.client.api.Converter;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.skill.SkillRestServiceBuilder;

@Singleton
public class StringListToSkillSetConverter implements Converter<Set<Skill>, List<String>> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private EventManager eventManager;

    private Map<String, Skill> skillMap;

    @PostConstruct
    private void init() {
        skillMap = new HashMap<>();
        updateSkillMappings(Collections.emptyList());
        eventManager.subscribeToEventForever(Event.DATA_INVALIDATION, this::onSkillListInvalidation);
    }

    @SuppressWarnings("unused")
    private void onTenantChanged(@Observes TenantStore.TenantChange event) {
        fetchSkillListAndUpdateSkillMapping();
    }

    @SuppressWarnings("unused")
    private void onSkillListInvalidation(Class<?> dataInvalidated) {
        if (dataInvalidated.equals(Skill.class)) {
            fetchSkillListAndUpdateSkillMapping();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getModelType() {
        return Set.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getComponentType() {
        return List.class;
    }

    @Override
    public Set<Skill> toModelValue(List<String> componentValue) {
        if (null == componentValue) {
            return Collections.emptySet();
        }
        return componentValue.stream().map((s) -> skillMap.get(s)).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public List<String> toWidgetValue(Set<Skill> modelValue) {
        if (null == modelValue) {
            return Collections.emptyList();
        }
        return modelValue.stream().map((s) -> s.getName()).collect(Collectors.toCollection(ArrayList::new));
    }

    public Map<String, Skill> getSkillMap() {
        return skillMap;
    }

    private void updateSkillMappings(List<Skill> skillList) {
        skillMap.clear();
        for (Skill skill : skillList) {
            skillMap.put(skill.getName(), skill);
        }
        eventManager.fireEvent(Event.SKILL_MAP_INVALIDATION, skillMap);
    }

    private Promise<List<Skill>> getSkillList() {
        return new Promise<>((resolve, reject) -> SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                .onSuccess(newSkillList -> {
                    resolve.onInvoke(newSkillList);
                })));
    }

    private void fetchSkillListAndUpdateSkillMapping() {
        getSkillList().then((skillList) -> {
            updateSkillMappings(skillList);
            return promiseUtils.resolve();
        });
    }
}
