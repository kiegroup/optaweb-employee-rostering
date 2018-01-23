package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.databinding.client.api.Converter;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;

public class SkillConvertor implements Converter<Set<Skill>, String> {

    Map<String, Skill> skillNameToSkillMap = new HashMap<>();

    @Override
    public Class<Set<Skill>> getModelType() {
        return (Class<Set<Skill>>) Collections.<Skill> emptySet().getClass();
    }

    @Override
    public Class<String> getComponentType() {
        return String.class;
    }

    public void setSkillSet(Collection<Skill> skillSet) {
        skillNameToSkillMap.clear();
        for (Skill skill : skillSet) {
            skillNameToSkillMap.put(skill.getName(), skill);
        }
    }

    @Override
    public Set<Skill> toModelValue(String componentValue) {
        String[] skillNames = componentValue.split(",");
        Set<Skill> output = new HashSet<>();
        for (String skillName : skillNames) {
            if (skillNameToSkillMap.containsKey(skillName)) {
                output.add(skillNameToSkillMap.get(skillName));
            }
        }
        return output;
    }

    @Override
    public String toWidgetValue(Set<Skill> modelValue) {
        return CommonUtils.delimitCollection(modelValue, (s) -> s.getName(), ",");
    }

}