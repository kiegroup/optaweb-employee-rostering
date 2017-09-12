package org.optaplanner.openshift.employeerostering.webapp.skill;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestService;
import org.optaplanner.openshift.employeerostering.webapp.AbstractClientArquillianTest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkillRestServiceTest extends AbstractClientArquillianTest {
    private static int numOfTestRun = 1000;
    private static final String[] SKILLS = {"A", "B", "C"};
    
    @Test
    public void testAddSkill(@ArquillianResource URL baseUrl) throws IOException {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(baseUrl.toExternalForm() + "rest");
        SkillRestService skillRestService = target.proxy(SkillRestService.class);
        int tenantId = generateDatabase(baseUrl);
        
        skillRestService.addSkill(tenantId, new Skill(tenantId, "D"));
        
        List<Skill> skills = skillRestService.getSkillList(tenantId);
        
        assertEquals("List size don't match", SKILLS.length + 1, skills.size());
        
        for (String name : SKILLS) {
            assertContainsSkill(skills, tenantId, name);
        }
        assertContainsSkill(skills, tenantId, "D");
    }
    
    @Test
    public void testGetSkill(@ArquillianResource URL baseUrl) throws IOException {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(baseUrl.toExternalForm() + "rest");
        SkillRestService skillRestService = target.proxy(SkillRestService.class);
        int tenantId = generateDatabase(baseUrl);
        
        List<Skill> skills = skillRestService.getSkillList(tenantId);
        
        Skill skill1 = getSkill(skills, "A").get();
        Skill skill2 = skillRestService.getSkill(tenantId, skill1.getId());
        
        assertNotNull("REST method did not return a result", skill2);
        assertEquals(skill1.getId(), skill2.getId());
        assertEquals(skill1.getName(), skill2.getName());
        assertEquals(skill1.getTenantId(), skill2.getTenantId());
        
        assertEquals("List size don't match", SKILLS.length, skills.size());
        
        for (String name : SKILLS) {
            assertContainsSkill(skills, tenantId, name);
        }
    }
    
    @Test
    public void testRemoveSkill(@ArquillianResource URL baseUrl) throws IOException {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(baseUrl.toExternalForm() + "rest");
        SkillRestService skillRestService = target.proxy(SkillRestService.class);
        int tenantId = generateDatabase(baseUrl);
        
        List<Skill> skills = skillRestService.getSkillList(tenantId);
        
        Skill skill1 = getSkill(skills, "A").get();
        boolean result = skillRestService.removeSkill(tenantId, skill1.getId());
        
        assertTrue(result);
        skills = skillRestService.getSkillList(tenantId);
        assertEquals("List size don't match", SKILLS.length - 1, skills.size());
        
        for (String name : Arrays.asList(SKILLS).stream().filter((s) -> !"A".equals(s)).collect(Collectors.toSet())) {  
            assertContainsSkill(skills, tenantId, name);
        }
        
        result = skillRestService.removeSkill(tenantId, skill1.getId());
        
        assertFalse(result);
        skills = skillRestService.getSkillList(tenantId);
        assertEquals("List size don't match", SKILLS.length - 1, skills.size());
        
        for (String name : Arrays.asList(SKILLS).stream().filter((s) -> !"A".equals(s)).collect(Collectors.toSet())) {  
            assertContainsSkill(skills, tenantId, name);
        }
    }
    
    @Test
    public void testGetSkillList(@ArquillianResource URL baseUrl) throws IOException {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(baseUrl.toExternalForm() + "rest");
        SkillRestService skillRestService = target.proxy(SkillRestService.class);
        
        int tenantId = generateDatabase(baseUrl);
        List<Skill> skills = skillRestService.getSkillList(tenantId);
        
        assertEquals("List size don't match", SKILLS.length, skills.size());
        
        for (String name : SKILLS) {
            assertContainsSkill(skills, tenantId, name);
        }
    }
    
    private void assertContainsSkill(List<Skill> skills, int tenantId, String name) {
        assertTrue("List does not contain skill " + name, skills.stream()
                .anyMatch((s) -> s.getName().equals(name) && tenantId == s.getTenantId()));
    }
    
    private Optional<Skill> getSkill(List<Skill> skills, String name) {
        return skills.stream().filter((s) -> s.getName().equals(name)).findFirst(); 
    }
    
    private static int generateDatabase(URL baseUrl) { 
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(baseUrl.toExternalForm() + "rest");
        SkillRestService skillRestService = target.proxy(SkillRestService.class);
        
        for (String name : SKILLS) {
            skillRestService.addSkill(numOfTestRun, new Skill(numOfTestRun,name));
        }
        
        numOfTestRun++;
        return numOfTestRun - 1;
    }
}
