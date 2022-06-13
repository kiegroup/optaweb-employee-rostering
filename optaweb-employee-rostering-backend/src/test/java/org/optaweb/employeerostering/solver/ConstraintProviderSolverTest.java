package org.optaweb.employeerostering.solver;

import javax.inject.Inject;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.service.solver.EmployeeRosteringConstraintProvider;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ConstraintProviderSolverTest extends AbstractSolverTest {

    @Inject
    SolverConfig solverConfig;

    @Override
    public SolverFactory<Roster> getSolverFactory() {
        return SolverFactory.create(solverConfig.copyConfig()
                .withScoreDirectorFactory(
                        new ScoreDirectorFactoryConfig()
                                .withConstraintProviderClass(
                                        EmployeeRosteringConstraintProvider.class))
                .withTerminationConfig(
                        new TerminationConfig()
                                .withBestScoreLimit(AbstractSolverTest.BEST_SCORE_TERMINATION_LIMIT)
                                .withScoreCalculationCountLimit(10000L)));
    }
}
