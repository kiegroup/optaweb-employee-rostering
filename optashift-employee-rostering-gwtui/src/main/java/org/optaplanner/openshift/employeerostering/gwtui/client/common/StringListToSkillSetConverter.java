package org.optaplanner.openshift.employeerostering.gwtui.client.common;

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
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.databinding.client.api.Converter;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

@Singleton
public class StringListToSkillSetConverter implements Converter<Set<Skill>, List<String>> {

    @Inject
    private TenantStore tenantStore;
    
    @Inject
    private PromiseUtils promiseUtils;
    
    private Map<String, Skill> skillMap;

    @SuppressWarnings("unchecked")
    @PostConstruct
    private void init() {
        skillMap = new HashMap<>();
        updateSkillMappings(Collections.emptyList());
    }

    @SuppressWarnings("unused")
    private void onTenantChanged(@Observes TenantStore.TenantChange event) {
        fetchSkillListAndUpdateSkillMapping();
    }

    @SuppressWarnings("unused")
    private void onSkillListInvalidation(@Observes DataInvalidation<Skill> event) {
        fetchSkillListAndUpdateSkillMapping();
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
        MessageBuilder.createMessage()
                .toSubject("SkillMapListener")
                .with("Map", getSkillMap())
                .noErrorHandling().sendNowWith(ErraiBus.getDispatcher());
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
