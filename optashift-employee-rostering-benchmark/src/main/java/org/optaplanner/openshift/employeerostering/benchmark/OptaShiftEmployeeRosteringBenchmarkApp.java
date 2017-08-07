package org.optaplanner.openshift.employeerostering.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.openshift.employeerostering.server.roster.RosterGenerator;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

public class OptaShiftEmployeeRosteringBenchmarkApp {

    public static void main(String[] args) {
        generateRosters();

        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "org/optaplanner/openshift/employeerostering/benchmark/employeeRosteringBenchmarkConfig.xml");
        PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmark();
    }

    private static void generateRosters() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("optashift-employee-rostering-persistence-unit");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        RosterGenerator rosterGenerator = new RosterGenerator(entityManager);

        XStreamSolutionFileIO<Roster> solutionFileIO = new XStreamSolutionFileIO<>(Roster.class);
        File unsolvedDir = new File("local/tmp/unsolved/");
        unsolvedDir.mkdirs();

        List<Roster> rosterList = new ArrayList<>();
        rosterList.add(rosterGenerator.generateRoster(10, 7, false));
        rosterList.add(rosterGenerator.generateRoster(80, (28 * 4), false));

        for (Roster roster : rosterList) {
            solutionFileIO.write(roster, new File(unsolvedDir,
                    roster.getSpotList().size() + "spots-" + roster.getTimeSlotList().size() + "timeslots.xml"));
        }

        int spotListSize = 10;
        int timeSlotListSize = 7;
        solutionFileIO.write(rosterGenerator.generateRoster(spotListSize, timeSlotListSize, false),
                new File(unsolvedDir, "10spots-7timeslots.xml"));

        entityManager.close();
        entityManagerFactory.close();
    }

}
