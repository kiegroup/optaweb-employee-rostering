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

package org.optaweb.employeerostering;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.service.roster.RosterGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class OptaWebEmployeeRosteringBenchmarkApplication implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(OptaWebEmployeeRosteringBenchmarkApplication.class, args);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Roster> rosterList = generateRosters();

        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "employeeRosteringBenchmarkConfig.xml", getClass().getClassLoader());
        PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark(rosterList);
        plannerBenchmark.benchmark();
    }

    private List<Roster> generateRosters() {
        RosterGenerator rosterGenerator = new RosterGenerator(entityManager);

        List<Roster> rosterList = new ArrayList<>();
        rosterList.add(rosterGenerator.generateRoster(10, 7));
        rosterList.add(rosterGenerator.generateRoster(80, (28 * 4)));

        return rosterList;
    }
}
