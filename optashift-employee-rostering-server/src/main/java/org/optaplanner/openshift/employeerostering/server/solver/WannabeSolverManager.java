/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Replace by real SolverManager once it exists in optaplanner-core
@ApplicationScoped
public class WannabeSolverManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private SolverFactory<Roster> solverFactory;
    // TODO Needs to default to size Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    @Resource(name = "DefaultManagedExecutorService")
    private ManagedExecutorService executorService;

    @Inject
    private RosterRestService rosterRestService;

    private ConcurrentMap<Integer, SolverStatus> tenantIdToSolverStateMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, Solver<Roster>> tenantIdToSolverMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void setUpSolverFactory() {
        solverFactory = SolverFactory.createFromXmlResource(
                "org/optaplanner/openshift/employeerostering/server/solver/employeeRosteringSolverConfig.xml");
    }

    public void terminate(Integer tenantId) {
        Solver<Roster> solver = tenantIdToSolverMap.get(tenantId);
        if (null != solver) {
            solver.terminateEarly();
        } else {
            throw new IllegalStateException("The roster with tenantId (" + tenantId
                                                    + ") is not being solved currently.");
        }
    }

    public void solve(Integer tenantId) {
        logger.info("Scheduling solver for tenantId ({})...", tenantId);
        // No 2 solve() calls of the same dataset in parallel
        tenantIdToSolverStateMap.compute(tenantId, (k, solverStatus) -> {
            if (solverStatus != null && solverStatus != SolverStatus.TERMINATED) {
                throw new IllegalStateException("The roster with tenantId (" + tenantId
                                                        + ") is already solving with solverStatus (" + solverStatus + ").");
            }
            return SolverStatus.SCHEDULED;
        });
        executorService.submit(() -> {
            try {
                Solver<Roster> solver = solverFactory.buildSolver();
                tenantIdToSolverMap.put(tenantId, solver);
                solver.addEventListener(event -> {
                    if (event.isEveryProblemFactChangeProcessed()) {
                        logger.info("  New best solution found for tenantId ({}).", tenantId);
                        Roster newBestRoster = event.getNewBestSolution();
                        // TODO if this throws an OptimisticLockingException, does it kill the solver?
                        rosterRestService.updateShiftsOfRoster(newBestRoster);
                    }
                });
                Roster roster = rosterRestService.buildRoster(tenantId);
                try {
                    tenantIdToSolverStateMap.put(tenantId, SolverStatus.SOLVING);
                    // TODO No need to store the returned roster because the SolverEventListener already does it?
                    solver.solve(roster);
                } finally {
                    tenantIdToSolverMap.remove(tenantId);
                    tenantIdToSolverStateMap.put(tenantId, SolverStatus.TERMINATED);
                }
            } catch (Throwable e) {
                // TODO handle errors through Thread'sExceptionHandler
                logger.error("Error solving for tenantId (" + tenantId + ").", e);
            }
        });
    }

    public Optional<Roster> getRoster(final Integer tenantId) {
        return Optional.ofNullable(tenantIdToSolverMap.get(tenantId)).map(Solver::getBestSolution);
    }
}
