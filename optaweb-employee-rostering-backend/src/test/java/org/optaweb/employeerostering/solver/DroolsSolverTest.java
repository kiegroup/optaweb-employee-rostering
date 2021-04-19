/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.io.File;

import javax.inject.Inject;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaweb.employeerostering.domain.roster.Roster;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DroolsSolverTest extends AbstractSolverTest {

    @Inject
    SolverConfig solverConfig;

    @Override
    public SolverFactory<Roster> getSolverFactory() {
        return SolverFactory.create(solverConfig.copyConfig()
                .withScoreDirectorFactory(new ScoreDirectorFactoryConfig()
                        .withScoreDrlFiles(new File(DroolsSolverTest.class
                                .getResource("/org/optaweb/employeerostering/service/solver/employeeRosteringScoreRules.drl")
                                .getFile())))
                .withTerminationConfig(
                        new TerminationConfig()
                                .withBestScoreLimit(AbstractSolverTest.BEST_SCORE_TERMINATION_LIMIT)
                                .withScoreCalculationCountLimit(10000L)));
    }
}
