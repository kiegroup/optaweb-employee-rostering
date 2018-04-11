/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.solver;

import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.openshift.employeerostering.server.roster.RosterGenerator;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;

import static org.junit.Assert.*;

public class SolverTest {

    @Test(timeout = 600000)
    public void solve() {
        SolverFactory<Roster> solverFactory = SolverFactory.createFromXmlResource(WannabeSolverManager.SOLVER_CONFIG);
        solverFactory.getSolverConfig().setTerminationConfig(new TerminationConfig().withBestScoreFeasible(true));
        Solver<Roster> solver = solverFactory.buildSolver();
        Roster roster = new RosterGenerator().generateRoster(10, 7);
        solver.solve(roster);
        assertNotNull(roster.getScore());
    }

}
