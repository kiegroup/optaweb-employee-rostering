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

package org.optaweb.employeerostering.benchmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaweb.employeerostering.server.roster.RosterGenerator;
import org.optaweb.employeerostering.shared.roster.Roster;

public class OptaWebEmployeeRosteringBenchmarkApp {

    public static void main(String[] args) {
        List<Roster> rosterList = generateRosters();

        SolverFactory<Roster> solverFactory = SolverFactory.createFromXmlResource(
                "org/optaweb/employeerostering/server/solver/employeeRosteringSolverConfig.xml");
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromSolverFactory(solverFactory);
        PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark(rosterList);
        plannerBenchmark.benchmark();
    }

    private static List<Roster> generateRosters() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
        // Failed attempts to avoid persistence.xml duplication by overwriting jta-data-source
        // TODO https://stackoverflow.com/questions/51514433/jpa-reuse-persistence-xml-with-jta-data-source-in-jse-and-junit-by-overriding-t
//        properties.put("javax.persistence.jtaDataSource", "");
//        properties.put("javax.persistence.nonJtaDataSource", "");
        properties.put("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
        properties.put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:testdb");
        properties.put("javax.persistence.jdbc.user", "sa");
        properties.put("javax.persistence.jdbc.password", "");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(
                "optaweb-employee-rostering-persistence-unit", properties);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        RosterGenerator rosterGenerator = new RosterGenerator(entityManager);

        List<Roster> rosterList = new ArrayList<>();
        rosterList.add(rosterGenerator.generateRoster(10, 7));
        rosterList.add(rosterGenerator.generateRoster(80, (28 * 4)));

        entityManager.close();
        entityManagerFactory.close();
        return rosterList;
    }

}
