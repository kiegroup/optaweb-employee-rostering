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

package org.optaweb.employeerostering.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.persistence.SkillRepository;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class SkillServiceUnitTest {

    @Mock
    SkillRepository skillRepository;

    @InjectMocks
    SkillService skillService;

    @Test
    public void getSkillTest() {

        Skill mockSkill = new Skill(0, "name");

        when(skillRepository.save(any(Skill.class))).thenReturn(new Skill());

        Skill newSkill = skillService.createSkill(0, mockSkill);

        assertEquals(newSkill.getName(), mockSkill.getName());
    }

}
