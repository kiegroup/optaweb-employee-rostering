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

package org.optaweb.employeerostering.server.solver;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaweb.employeerostering.server.roster.RosterGenerator;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.roster.Roster;
import org.optaweb.employeerostering.shared.tenant.Tenant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class SolverTest {

    @Test(timeout = 600000)
    public void solve() {
        SolverFactory<Roster> solverFactory = SolverFactory.createFromXmlResource(WannabeSolverManager.SOLVER_CONFIG);
        solverFactory.getSolverConfig().setTerminationConfig(new TerminationConfig().withMillisecondsSpentLimit(2000L));
        Solver<Roster> solver = solverFactory.buildSolver();

        RosterGenerator rosterGenerator = buildRosterGenerator();
        Roster roster = rosterGenerator.generateRoster(10, 7);

        roster = solver.solve(roster);
        assertNotNull(roster.getScore());
        // Due to overconstrained planning, the score is always feasible
        assertTrue(roster.getScore().isFeasible());
        assertFalse(roster.getShiftList().isEmpty());
    }

    protected RosterGenerator buildRosterGenerator() {
        EntityManager entityManager = mock(EntityManager.class);
        AtomicInteger tenantIdGenerator = new AtomicInteger(0);
        doAnswer(invocation -> {
            Tenant tenant = (Tenant) invocation.getArgument(0);
            tenant.setId(tenantIdGenerator.getAndIncrement());
            return invocation;
        }).when(entityManager).persist(any(Tenant.class));
        AtomicLong idGenerator = new AtomicLong(0L);
        doAnswer(invocation -> {
            AbstractPersistable o = (AbstractPersistable) invocation.getArgument(0);
            o.setId(idGenerator.getAndIncrement());
            return invocation;
        }).when(entityManager).persist(any(AbstractPersistable.class));

        RosterGenerator rosterGenerator = new RosterGenerator(entityManager);
        rosterGenerator.setUpGeneratedData();
        return rosterGenerator;
    }

}
