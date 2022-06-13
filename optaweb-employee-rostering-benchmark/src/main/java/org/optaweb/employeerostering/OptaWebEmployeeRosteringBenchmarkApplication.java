package org.optaweb.employeerostering;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.service.admin.SystemPropertiesRetriever;
import org.optaweb.employeerostering.service.roster.RosterGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class OptaWebEmployeeRosteringBenchmarkApplication implements QuarkusApplication {

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    UserTransaction userTransaction;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Quarkus.run(OptaWebEmployeeRosteringBenchmarkApplication.class, args);
    }

    @Override
    public int run(String... args) {
        List<Roster> rosterList = generateRosters();

        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "employeeRosteringBenchmarkConfig.xml", getClass().getClassLoader());
        PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark(rosterList);
        plannerBenchmark.benchmark();
        return 0;
    }

    private List<Roster> generateRosters() {
        try {
            userTransaction.begin();
            RosterGenerator rosterGenerator = new RosterGenerator(entityManager, new SystemPropertiesRetriever());

            List<Roster> rosterList = new ArrayList<>();
            rosterList.add(rosterGenerator.generateRoster(10, 7));
            rosterList.add(rosterGenerator.generateRoster(80, (28 * 4)));
            userTransaction.commit();

            return rosterList;
        } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | RollbackException
                | NotSupportedException e) {
            throw new IllegalStateException("Failed to generate rosters.", e);
        }
    }
}
