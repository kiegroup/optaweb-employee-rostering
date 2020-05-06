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

package org.optaweb.employeerostering.solver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.service.roster.RosterGenerator;
import org.optaweb.employeerostering.service.solver.WannabeSolverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class SolverManagerTest {

    @Autowired
    private RosterGenerator rosterGenerator;

    @Autowired
    protected WannabeSolverManager solverManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testSolverManager() throws InterruptedException {
        solverManager.setUpSolverFactory();
        Roster roster = rosterGenerator.generateRoster(10, 7);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> solverManager.terminate(roster.getTenantId()), 30, TimeUnit.SECONDS);
        CountDownLatch solverEndedLatch = solverManager.solve(roster.getTenantId());

        solverEndedLatch.await();
        ScoreDirector<Roster> scoreDirector = solverManager.getScoreDirector();
        scoreDirector.setWorkingSolution(roster);
        roster.setScore((HardMediumSoftLongScore) scoreDirector.calculateScore());
        assertNotNull(roster.getScore());
        // Due to overconstrained planning, the score is always feasible
        assertTrue(roster.getScore().isFeasible());
        assertFalse(roster.getShiftList().isEmpty());
        assertTrue(roster.getShiftList().stream().anyMatch(s -> s.getEmployee() != null));
    }
}
