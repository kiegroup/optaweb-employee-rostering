package org.optaplanner.openshift.employeerostering.webapp.skill;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SkillRestServiceIT extends AbstractRestServiceIT {

    private static final int TENANT_ID = 1000;

    private SkillRestService skillRestService;

    public SkillRestServiceIT() {
        skillRestService = serviceClientFactory.createSkillRestServiceClient();
    }

    @Before
    public void setup() {
        skillRestService.getSkillList(TENANT_ID)
                .forEach(skill -> skillRestService.removeSkill(TENANT_ID, skill.getId()));
    }

    @Test
    public void testDeleteNonExistingSkill() {
        final long nonExistingSkillId = 123456L;
        boolean result = skillRestService.removeSkill(TENANT_ID, nonExistingSkillId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingSkill() {
        final long nonExistingSkillId = 123456L;
        Skill nonExistingSkill = new Skill(TENANT_ID, "Non-existing skill");
        nonExistingSkill.setId(nonExistingSkillId);
        Skill updatedSkill = skillRestService.updateSkill(TENANT_ID, nonExistingSkill);

        assertThat(updatedSkill.getName()).isEqualTo(nonExistingSkill.getName());
        assertThat(updatedSkill.getId()).isNotNull().isNotEqualTo(nonExistingSkillId);
    }

    @Test
    public void testGetOfNonExistingSkill() {
        final long nonExistingSkillId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> skillRestService.getSkill(TENANT_ID, nonExistingSkillId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testCrudSkill() {
        Skill testAddSkill = new Skill(TENANT_ID, "A");
        skillRestService.addSkill(TENANT_ID, testAddSkill);
        assertClientResponseOk();

        List<Skill> skills = skillRestService.getSkillList(TENANT_ID);
        assertClientResponseOk();
        assertThat(skills).usingElementComparatorIgnoringFields(IGNORED_FIELDS).containsExactly(testAddSkill);

        Skill testUpdateSkill = skills.get(0);
        testUpdateSkill.setName("B");
        skillRestService.updateSkill(TENANT_ID, testUpdateSkill);

        Skill retrievedSkill = skillRestService.getSkill(TENANT_ID, testUpdateSkill.getId());
        assertClientResponseOk();
        assertThat(retrievedSkill).isNotNull().isEqualToIgnoringGivenFields(testUpdateSkill, "version");

        boolean result = skillRestService.removeSkill(TENANT_ID, retrievedSkill.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        skills = skillRestService.getSkillList(TENANT_ID);
        assertThat(skills).isEmpty();
    }
}
