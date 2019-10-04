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

package org.optaweb.employeerostering.service.solver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.service.roster.RosterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

// TODO Replace by real SolverManager once it exists in optaplanner-core
@ApplicationScope
@Component
public class WannabeSolverManager implements ApplicationRunner {

    public static final String SOLVER_CONFIG = "org/optaweb/employeerostering/service/solver/" +
            "employeeRosteringSolverConfig.xml";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private SolverFactory<Roster> solverFactory;
    private ScoreDirectorFactory<Roster> scoreDirectorFactory;

    // TODO Needs to default to size Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    private ThreadPoolTaskExecutor taskExecutor;

    private RosterService rosterService;

    private ConcurrentMap<Integer, SolverStatus> tenantIdToSolverStateMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, Solver<Roster>> tenantIdToSolverMap = new ConcurrentHashMap<>();

    public WannabeSolverManager(ThreadPoolTaskExecutor taskExecutor, RosterService rosterService) {
        this.taskExecutor = taskExecutor;
        this.rosterService = rosterService;
    }

    @Override
    public void run(ApplicationArguments args) {
        setUpSolverFactory();
    }

    public void setUpSolverFactory() {
        solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG, WannabeSolverManager.class.getClassLoader());
        scoreDirectorFactory = solverFactory.buildSolver().getScoreDirectorFactory();
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

    public CountDownLatch solve(Integer tenantId) {
        logger.info("Scheduling solver for tenantId ({})...", tenantId);
        // No 2 solve() calls of the same dataset in parallel
        tenantIdToSolverStateMap.compute(tenantId, (k, solverStatus) -> {
            if (solverStatus != null && solverStatus != SolverStatus.TERMINATED) {
                throw new IllegalStateException("The roster with tenantId (" + tenantId + ") is already solving " +
                                                        "with solverStatus (" + solverStatus + ").");
            }
            return SolverStatus.SCHEDULED;
        });

        final CountDownLatch solvingEndedLatch = new CountDownLatch(1);
        taskExecutor.execute(() -> {
            try {
                Solver<Roster> solver = solverFactory.buildSolver();
                tenantIdToSolverMap.put(tenantId, solver);
                solver.addEventListener(event -> {
                    if (event.isEveryProblemFactChangeProcessed()) {
                        logger.info("  New best solution found for tenantId ({}).", tenantId);
                        Roster newBestRoster = event.getNewBestSolution();
                        // TODO if this throws an OptimisticLockingException, does it kill the solver?
                        rosterService.updateShiftsOfRoster(newBestRoster);
                    }
                });
                Roster roster = rosterService.buildRoster(tenantId); // TODO rename to rosterService.loadRoster
                try {
                    tenantIdToSolverStateMap.put(tenantId, SolverStatus.SOLVING);
                    // TODO No need to store the returned roster because the SolverEventListener already does it?
                    solver.solve(roster);
                    solvingEndedLatch.countDown();
                } finally {
                    tenantIdToSolverMap.remove(tenantId);
                    tenantIdToSolverStateMap.put(tenantId, SolverStatus.TERMINATED);
                }
            } catch (Throwable e) {
                // TODO handle errors through Thread'sExceptionHandler
                logger.error("Error solving for tenantId (" + tenantId + ").", e);
            }
        });
        return solvingEndedLatch;
    }

    public Roster getRoster(final Integer tenantId) {
        Solver<Roster> solver = tenantIdToSolverMap.get(tenantId);
        return solver == null ? null : solver.getBestSolution();
    }

    public ScoreDirector<Roster> getScoreDirector() {
        return scoreDirectorFactory.buildScoreDirector();
    }
}
