package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.databinding.client.api.Converter;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

@Singleton
public class StringListToSkillSetConverter implements Converter<Set<Skill>, List<String>> {

    @Inject
    private TenantStore tenantStore;

    private Map<String, Skill> skillMap = new HashMap<>();;

    private Collection<Updatable<Map<String, Skill>>> skillMapListeners = new HashSet<>();

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        if (tenantStore.getCurrentTenantId() != null) {
            SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(
                                                                                                                      skillList -> {
                                                                                                                          skillMap.clear();
                                                                                                                          for (Skill skill : skillList) {
                                                                                                                              skillMap.put(skill.getName(), skill);
                                                                                                                          }
                                                                                                                          skillMapListeners.forEach((l) -> l.onUpdate(skillMap));
                                                                                                                      }));
        }

    }

    public void onAnyInvalidationEvent(@Observes DataInvalidation<Skill> skill) {
        onAnyTenantEvent(null);
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
        return new HashSet<Skill>(componentValue.stream().map((s) -> skillMap.get(s)).collect(
                                                                                              Collectors.toSet()));
    }

    @Override
    public List<String> toWidgetValue(Set<Skill> modelValue) {
        if (null == modelValue) {
            return Collections.emptyList();
        }
        return new ArrayList<>(modelValue.stream().map((s) -> s.getName()).collect(Collectors.toList()));
    }

    public Collection<Skill> getTenantSkillCollection() {
        return skillMap.values();
    }

    public void registerSkillMapListener(Updatable<Map<String, Skill>> listener) {
        skillMapListeners.add(listener);
        listener.onUpdate(skillMap);
    }

    public void dereigsterSkillMapListener(Updatable<Map<String, Skill>> listener) {
        skillMapListeners.remove(listener);
    }

}
