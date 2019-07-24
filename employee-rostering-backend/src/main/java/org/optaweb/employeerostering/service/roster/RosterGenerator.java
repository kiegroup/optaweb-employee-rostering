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

package org.optaweb.employeerostering.service.roster;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Pair;
import org.optaweb.employeerostering.service.common.generator.StringDataGenerator;
import org.optaweb.employeerostering.service.contract.ContractRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.rotation.ShiftTemplateRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.springframework.stereotype.Component;

@Component
public class RosterGenerator {

    private static final double[] EXTRA_SHIFT_THRESHOLDS = {0.5, 0.8, 0.95};

    private static class GeneratorType {

        private final String tenantNamePrefix;
        private final StringDataGenerator skillNameGenerator;
        private final StringDataGenerator spotNameGenerator;
        private final List<Pair<LocalTime, LocalTime>> timeslotRangeList; // Start and end time per timeslot
        private final int rotationLength;
        private final int rotationEmployeeListSize;
        private final BiFunction<Integer, Integer, Integer> rotationEmployeeIndexCalculator;

        public GeneratorType(String tenantNamePrefix, StringDataGenerator skillNameGenerator,
                             StringDataGenerator spotNameGenerator, List<Pair<LocalTime, LocalTime>> timeslotRangeList,
                             int rotationLength, int rotationEmployeeListSize,
                             BiFunction<Integer, Integer, Integer> rotationEmployeeIndexCalculator) {
            this.tenantNamePrefix = tenantNamePrefix;
            this.skillNameGenerator = skillNameGenerator;
            this.spotNameGenerator = spotNameGenerator;
            this.timeslotRangeList = timeslotRangeList;
            this.rotationLength = rotationLength;
            this.rotationEmployeeListSize = rotationEmployeeListSize;
            this.rotationEmployeeIndexCalculator = rotationEmployeeIndexCalculator;
        }
    }

    private final StringDataGenerator tenantNameGenerator = StringDataGenerator.buildLocationNames();
    private final StringDataGenerator employeeNameGenerator = StringDataGenerator.buildFullNames();

    private final GeneratorType hospitalGeneratorType = new GeneratorType(
            "Hospital",
            new StringDataGenerator()
                    .addPart(
                            "Ambulatory care",
                            "Critical care",
                            "Midwife",
                            "Gastroenterology",
                            "Neuroscience",
                            "Oncology",
                            "Pediatric",
                            "Psychiatric",
                            "Geriatric",
                            "Radiology")
                    .addPart(
                            "nurse",
                            "physician",
                            "doctor",
                            "attendant",
                            "specialist",
                            "surgeon",
                            "medic",
                            "practitioner",
                            "pharmacist",
                            "researcher"),
            new StringDataGenerator(true)
                    .addPart(false, 0,
                             "Basic",
                             "Advanced",
                             "Expert",
                             "Specialized",
                             "Elder",
                             "Child",
                             "Infant",
                             "Baby",
                             "Male",
                             "Female",
                             "Common",
                             "Uncommon",
                             "Research",
                             "Administrative",
                             "Regressing")
                    .addPart(true, 1,
                             "anaesthetics",
                             "cardiology",
                             "critical care",
                             "emergency",
                             "ear nose throat",
                             "gastroenterology",
                             "haematology",
                             "maternity",
                             "neurology",
                             "oncology",
                             "ophthalmology",
                             "orthopaedics",
                             "physiotherapy",
                             "radiotherapy",
                             "urology")
                    .addPart(false, 0,
                             "Alpha",
                             "Beta",
                             "Gamma",
                             "Delta",
                             "Epsilon",
                             "Zeta",
                             "Eta",
                             "Theta",
                             "Iota",
                             "Kappa",
                             "Lambda",
                             "Mu",
                             "Nu",
                             "Xi",
                             "Omicron"),
            Arrays.asList(
                    Pair.of(LocalTime.of(6, 0), LocalTime.of(14, 0)),
                    Pair.of(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                    Pair.of(LocalTime.of(14, 0), LocalTime.of(22, 0)),
                    Pair.of(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            // Morning:   A A A A A D D B B B B B D D C C C C C D D
            // Day:       F F B B B F F F F C C C F F F F A A A F F
            // Afternoon: D D D E E E E D D D E E E E D D D E E E E
            // Night:     E C C C C C C E A A A A A A E B B B B B B
            21, 6, (startDayOffset, timeslotRangesIndex) -> {
        switch (timeslotRangesIndex) {
            case 0:
                return startDayOffset % 7 >= 5 ? 3 : startDayOffset / 7;
            case 1:
                return (startDayOffset + 2) % 7 < 4 ? 5 : (startDayOffset - 16 + 21) % 21 / 7;
            case 2:
                return startDayOffset % 7 < 3 ? 3 : 4;
            case 3:
                return startDayOffset % 7 < 1 ? 4 : (startDayOffset - 8 + 21) % 21 / 7;
            default:
                throw new IllegalStateException("Impossible state for timeslotRangesIndex (" + timeslotRangesIndex
                                                        + ").");
        }
    });
    private final GeneratorType factoryAssemblyGeneratorType = new GeneratorType(
            "Factory assembly",
            new StringDataGenerator()
                    .addPart(
                            "Mechanical",
                            "Electrical",
                            "Safety",
                            "Transportation",
                            "Operational",
                            "Physics",
                            "Monitoring",
                            "ICT")
                    .addPart(
                            "bachelor",
                            "engineer",
                            "instructor",
                            "coordinator",
                            "manager",
                            "expert",
                            "inspector",
                            "analyst"),
            StringDataGenerator.buildAssemblyLineNames(),
            Arrays.asList(
                    Pair.of(LocalTime.of(6, 0), LocalTime.of(14, 0)),
                    Pair.of(LocalTime.of(14, 0), LocalTime.of(22, 0)),
                    Pair.of(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            // Morning:   A A A A A A A B B B B B B B C C C C C C C D D D D D D D
            // Afternoon: C C D D D D D D D A A A A A A A B B B B B B B C C C C C
            // Night:     B B B B C C C C C C C D D D D D D D A A A A A A A B B B
            28, 4, (startDayOffset, timeslotRangesIndex) -> (startDayOffset - (9 * timeslotRangesIndex) + 28) % 28 / 7);
    private final GeneratorType guardSecurityGeneratorType = new GeneratorType(
            "Guard security",
            new StringDataGenerator()
                    .addPart(
                            "Martial art",
                            "Armed",
                            "Surveillance",
                            "Technical",
                            "Computer")
                    .addPart(
                            "basic",
                            "advanced",
                            "expert",
                            "master",
                            "novice"),
            new StringDataGenerator()
                    .addPart("Airport",
                             "Harbor",
                             "Bank",
                             "Office",
                             "Warehouse",
                             "Store",
                             "Factory",
                             "Station",
                             "Museum",
                             "Mansion",
                             "Monument",
                             "City hall",
                             "Prison",
                             "Mine",
                             "Palace")
                    .addPart("north gate",
                             "south gate",
                             "east gate",
                             "west gate",
                             "roof",
                             "cellar",
                             "north west gate",
                             "north east gate",
                             "south west gate",
                             "south east gate",
                             "main door",
                             "back door",
                             "side door",
                             "balcony",
                             "patio")
                    .addPart("Alpha",
                             "Beta",
                             "Gamma",
                             "Delta",
                             "Epsilon",
                             "Zeta",
                             "Eta",
                             "Theta",
                             "Iota",
                             "Kappa",
                             "Lambda",
                             "Mu",
                             "Nu",
                             "Xi",
                             "Omicron"),
            Arrays.asList(
                    Pair.of(LocalTime.of(7, 0), LocalTime.of(19, 0)),
                    Pair.of(LocalTime.of(19, 0), LocalTime.of(7, 0))),
            // Day:   A A A B B B B C C C A A A A B B B C C C C
            // Night: C C C A A A A B B B C C C C A A A B B B B
            21, 3, (startDayOffset, timeslotRangesIndex) -> {
        int offset = timeslotRangesIndex == 0 ? startDayOffset : (startDayOffset + 7) % 21;
        return offset < 3 ? 0 : offset < 7 ? 1 : offset < 10 ? 2 : offset < 14 ? 0 : offset < 17 ? 1 :
                offset < 21 ? 2 : -1;
    });
    private final GeneratorType callCenterGeneratorType = new GeneratorType(
            "Call center",
            new StringDataGenerator()
                    .addPart(
                            "English",
                            "Spanish",
                            "French",
                            "German",
                            "Japanese",
                            "Chinese",
                            "Dutch",
                            "Portuguese",
                            "Italian"),
            new StringDataGenerator()
                    .addPart("Business loans",
                             "Checking and savings accounts",
                             "Debit and credit cards",
                             "Insurances",
                             "Merchant services",
                             "Cash management",
                             "Tax management",
                             "Wealth management",
                             "Mortgages",
                             "Personal loans",
                             "Online payment"),
            Arrays.asList(
                    Pair.of(LocalTime.of(7, 0), LocalTime.of(16, 0)),
                    Pair.of(LocalTime.of(11, 0), LocalTime.of(20, 0))),
            // Morning:   B A A A A A B
            // Afternoon: C C B B C C C
            7, 3, (startDayOffset, timeslotRangesIndex) -> {
        return timeslotRangesIndex == 0
                ? startDayOffset < 1 ? 1 : startDayOffset < 6 ? 0 : startDayOffset < 7 ? 1 : -1
                : startDayOffset < 2 ? 2 : startDayOffset < 4 ? 1 : startDayOffset < 7 ? 2 : -1;
    });

    private Random random;

    // TODO: Make fields 'final' once Benchmark is added
    private SkillRepository skillRepository;
    private SpotRepository spotRepository;
    private ContractRepository contractRepository;
    private EmployeeRepository employeeRepository;
    private RosterStateRepository rosterStateRepository;
    private ShiftTemplateRepository shiftTemplateRepository;

    // TODO: Add ShiftRepository once Shift CRUD is implemented

    // TODO: Add EmployeeAvailabilityRepository once EmployeeAvailability CRUD is implemented

    // TODO: Add RosterParametrizationRepository once RosterParametrization CRUD is implemented

    // TODO: Add TenantRepository once Tenant CRUD is implemented

    @SuppressWarnings("unused")
    public RosterGenerator() {
    }

    /**
     * For benchmark only
     * @param skillRepository never null
     * @param spotRepository never null
     * @param contractRepository never null
     * @param employeeRepository never null
     * @param rosterStateRepository never null
     * @param shiftTemplateRepository never null
     */
    public RosterGenerator(SkillRepository skillRepository, SpotRepository spotRepository,
                           ContractRepository contractRepository, EmployeeRepository employeeRepository,
                           RosterStateRepository rosterStateRepository,
                           ShiftTemplateRepository shiftTemplateRepository) {
        this.skillRepository = skillRepository;
        this.spotRepository = spotRepository;
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        random = new Random(37);
    }
}
