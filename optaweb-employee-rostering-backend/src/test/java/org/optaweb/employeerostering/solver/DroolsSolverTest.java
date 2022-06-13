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
