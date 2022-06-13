package org.optaweb.employeerostering.service.roster;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.Triple;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.rotation.Seat;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.service.admin.SystemPropertiesRetriever;
import org.optaweb.employeerostering.service.common.generator.StringDataGenerator;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RosterGenerator {

    private static final double[] EXTRA_SHIFT_THRESHOLDS = { 0.5, 0.8, 0.95 };

    public static class GeneratorType {

        public final String tenantNamePrefix;
        public final StringDataGenerator skillNameGenerator;
        public final StringDataGenerator spotNameGenerator;

        // Start and end time per timeslot
        public final List<Triple<LocalTime, LocalTime, List<DayOfWeek>>> timeslotRangeList;
        public final int rotationLength;
        public final int rotationEmployeeListSize;
        public final BiFunction<Integer, Integer, Integer> rotationEmployeeIndexCalculator;

        public GeneratorType(String tenantNamePrefix, StringDataGenerator skillNameGenerator,
                StringDataGenerator spotNameGenerator,
                List<Triple<LocalTime, LocalTime, List<DayOfWeek>>> timeslotRangeList,
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

    private final List<DayOfWeek> ALL_WEEK = Arrays.asList(DayOfWeek.values());
    private final List<DayOfWeek> WEEKDAYS = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
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
                    Triple.of(LocalTime.of(6, 0), LocalTime.of(14, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(9, 0), LocalTime.of(17, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(14, 0), LocalTime.of(22, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(22, 0), LocalTime.of(6, 0), ALL_WEEK)),
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
                    Triple.of(LocalTime.of(6, 0), LocalTime.of(14, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(14, 0), LocalTime.of(22, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(22, 0), LocalTime.of(6, 0), ALL_WEEK)),
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
                    Triple.of(LocalTime.of(7, 0), LocalTime.of(19, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(19, 0), LocalTime.of(7, 0), ALL_WEEK)),
            // Day:   A A A B B B B C C C A A A A B B B C C C C
            // Night: C C C A A A A B B B C C C C A A A B B B B
            21, 3, (startDayOffset, timeslotRangesIndex) -> {
                int offset = timeslotRangesIndex == 0 ? startDayOffset : (startDayOffset + 7) % 21;
                return offset < 3 ? 0
                        : offset < 7 ? 1 : offset < 10 ? 2 : offset < 14 ? 0 : offset < 17 ? 1 : offset < 21 ? 2 : -1;
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
                    Triple.of(LocalTime.of(7, 0), LocalTime.of(16, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(11, 0), LocalTime.of(20, 0), ALL_WEEK)),
            // Morning:   B A A A A A B
            // Afternoon: C C B B C C C
            7, 3, (startDayOffset, timeslotRangesIndex) -> {
                return timeslotRangesIndex == 0
                        ? startDayOffset < 1 ? 1 : startDayOffset < 6 ? 0 : startDayOffset < 7 ? 1 : -1
                        : startDayOffset < 2 ? 2 : startDayOffset < 4 ? 1 : startDayOffset < 7 ? 2 : -1;
            });
    private final GeneratorType postOfficeGeneratorType = new GeneratorType(
            "Post office",
            new StringDataGenerator()
                    .addPart(
                            "Truck license",
                            "Bicycle license",
                            "Computer certification",
                            "Administration",
                            "Transportation",
                            "Monitoring",
                            "Logistics",
                            "Coordination",
                            "Customer service"),
            new StringDataGenerator()
                    .addPart(true, 1,
                            "North",
                            "South",
                            "East",
                            "West",
                            "North West",
                            "North East",
                            "South West",
                            "South East",
                            "Central")
                    .addPart(true, 1,
                            "Uptown",
                            "Harbor",
                            "Lakeshore",
                            "Point",
                            "Valley",
                            "Port",
                            "Heights",
                            "Beach",
                            "Downtown"),
            Arrays.asList(
                    Triple.of(LocalTime.of(9, 0), LocalTime.of(17, 0), WEEKDAYS),
                    Triple.of(LocalTime.of(9, 0), LocalTime.of(15, 0), Arrays.asList(DayOfWeek.SATURDAY))),
            7, 3, (startDayOffset, timeslotRangesIndex) -> {
                return timeslotRangesIndex == 0
                        ? startDayOffset < 1 ? 1 : startDayOffset < 6 ? 0 : startDayOffset < 7 ? 1 : -1
                        : startDayOffset < 2 ? 2 : startDayOffset < 4 ? 1 : startDayOffset < 7 ? 2 : -1;
            });

    private Random random;

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    SystemPropertiesRetriever systemPropertiesRetriever;

    @SuppressWarnings("unused")
    public RosterGenerator() {
        this(null, new SystemPropertiesRetriever());
    }

    /**
     * For benchmark only
     *
     * @param entityManager never null
     */
    @Inject
    public RosterGenerator(EntityManager entityManager,
            SystemPropertiesRetriever systemPropertiesRetriever) {
        this.entityManager = entityManager;
        this.systemPropertiesRetriever = systemPropertiesRetriever;
        random = new Random(37);
    }

    @Transactional
    public void run(@Observes StartupEvent event) {
        checkForExistingData();
    }

    @Transactional
    public void checkForExistingData() {
        // Check if Tenant entities already exist before generating data
        @SuppressWarnings("unchecked")
        List<Tenant> tenantList = entityManager.createQuery("select t from Tenant t").getResultList();

        if (!tenantList.isEmpty()) {
            return;
        }

        setUpGeneratedData();
    }

    @Transactional
    public void setUpGeneratedData() {
        ZoneId zoneId = systemPropertiesRetriever.determineZoneId();
        SystemPropertiesRetriever.InitialData initialData = systemPropertiesRetriever.determineInitialData();

        random = new Random(37);

        switch (initialData) {
            case EMPTY:
                return;
            case DEMO_DATA:
                tenantNameGenerator.predictMaximumSizeAndReset(12);
                generateRoster(10, 7, hospitalGeneratorType, zoneId);
                generateRoster(10, 7, factoryAssemblyGeneratorType, zoneId);
                generateRoster(10, 7, guardSecurityGeneratorType, zoneId);
                generateRoster(10, 7, callCenterGeneratorType, zoneId);
                generateRoster(10, 7, postOfficeGeneratorType, zoneId);
                generateRoster(10, 7 * 4, factoryAssemblyGeneratorType, zoneId);
                generateRoster(20, 7 * 4, factoryAssemblyGeneratorType, zoneId);
                generateRoster(40, 7 * 2, factoryAssemblyGeneratorType, zoneId);
                generateRoster(80, 7 * 4, factoryAssemblyGeneratorType, zoneId);
                generateRoster(10, 7 * 4, factoryAssemblyGeneratorType, zoneId);
                generateRoster(20, 7 * 4, factoryAssemblyGeneratorType, zoneId);
                generateRoster(40, 7 * 2, factoryAssemblyGeneratorType, zoneId);
                generateRoster(80, 7 * 4, factoryAssemblyGeneratorType, zoneId);
        }
    }

    @Transactional
    public Roster generateRoster(int spotListSize,
            int lengthInDays,
            RosterGenerator.GeneratorType generatorType,
            ZoneId zoneId) {
        int maxShiftSizePerDay = generatorType.timeslotRangeList.size() + EXTRA_SHIFT_THRESHOLDS.length;
        // The average employee works 5 days out of 7
        int employeeListSize = spotListSize * maxShiftSizePerDay * 7 / 5;
        int skillListSize = (spotListSize + 4) / 5;

        Tenant tenant = createTenant(generatorType, employeeListSize);
        Integer tenantId = tenant.getId();
        RosterConstraintConfiguration rosterConstraintConfiguration = createTenantConfiguration(generatorType,
                tenantId, zoneId);
        RosterState rosterState = createRosterState(generatorType, tenant, zoneId, lengthInDays);

        List<Skill> skillList = createSkillList(generatorType, tenantId, skillListSize);
        List<Spot> spotList = createSpotList(generatorType, tenantId, spotListSize, skillList);
        List<Contract> contractList = createContractList(tenantId);
        List<Employee> employeeList = createEmployeeList(generatorType, tenantId, employeeListSize,
                contractList, skillList);
        List<TimeBucket> timeBucketList = createTimeBucketList(generatorType, tenantId,
                rosterConstraintConfiguration
                        .getWeekStartDay(),
                rosterState,
                spotList,
                employeeList, skillList);
        List<Shift> shiftList = createShiftList(generatorType, tenantId, rosterConstraintConfiguration,
                rosterState, spotList, timeBucketList);
        List<EmployeeAvailability> employeeAvailabilityList = createEmployeeAvailabilityList(
                generatorType, tenantId, rosterConstraintConfiguration, rosterState, employeeList, shiftList);

        return new Roster((long) tenantId, tenantId, rosterConstraintConfiguration, skillList, spotList, employeeList,
                employeeAvailabilityList, rosterState, shiftList);
    }

    @Transactional
    public Roster generateRoster(int spotListSize, int lengthInDays) {
        ZoneId zoneId = systemPropertiesRetriever.determineZoneId();
        return generateRoster(spotListSize, lengthInDays, hospitalGeneratorType, zoneId);
    }

    @Transactional
    public Tenant createTenant(GeneratorType generatorType, int employeeListSize) {
        String tenantName = generatorType.tenantNamePrefix + " " + tenantNameGenerator.generateNextValue() + " ("
                + employeeListSize + " employees)";
        Tenant tenant = new Tenant(tenantName);
        entityManager.persist(tenant);
        return tenant;
    }

    @Transactional
    public RosterConstraintConfiguration createTenantConfiguration(GeneratorType generatorType, Integer tenantId,
            ZoneId zoneId) {
        RosterConstraintConfiguration rosterConstraintConfiguration = new RosterConstraintConfiguration();
        rosterConstraintConfiguration.setTenantId(tenantId);
        entityManager.persist(rosterConstraintConfiguration);
        return rosterConstraintConfiguration;
    }

    @Transactional
    public RosterState createRosterState(GeneratorType generatorType, Tenant tenant, ZoneId zoneId, int lengthInDays) {
        RosterState rosterState = new RosterState();
        rosterState.setTenantId(tenant.getId());
        int publishNotice = 14;
        rosterState.setPublishNotice(publishNotice);
        LocalDate firstDraftDate = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plusDays(publishNotice);
        rosterState.setFirstDraftDate(firstDraftDate);
        // publishLength is read-only and set to 1 day
        //rosterState.setPublishLength(1);
        rosterState.setDraftLength(14);
        rosterState.setUnplannedRotationOffset(0);
        rosterState.setRotationLength(generatorType.rotationLength);
        rosterState.setLastHistoricDate(LocalDate.now().minusDays(1));
        rosterState.setTimeZone(zoneId);
        rosterState.setTenant(tenant);
        entityManager.persist(rosterState);
        return rosterState;
    }

    @Transactional
    public List<Skill> createSkillList(GeneratorType generatorType, Integer tenantId, int size) {
        List<Skill> skillList = new ArrayList<>(size + 3);
        generatorType.skillNameGenerator.predictMaximumSizeAndReset(size);

        for (int i = 0; i < size; i++) {
            String name = generatorType.skillNameGenerator.generateNextValue();
            Skill skill = new Skill(tenantId, name);
            entityManager.persist(skill);
            skillList.add(skill);
        }
        return skillList;
    }

    @Transactional
    public List<Spot> createSpotList(GeneratorType generatorType, Integer tenantId, int size, List<Skill> skillList) {
        List<Spot> spotList = new ArrayList<>(size);
        generatorType.spotNameGenerator.predictMaximumSizeAndReset(size);

        for (int i = 0; i < size; i++) {
            String name = generatorType.spotNameGenerator.generateNextValue();
            Set<Skill> requiredSkillSet = new HashSet<>(extractRandomSubList(
                    skillList,
                    0.5, 0.9, 1.0));

            Spot spot = new Spot(tenantId, name, requiredSkillSet);
            entityManager.persist(spot);
            spotList.add(spot);
        }
        return spotList;
    }

    @Transactional
    public List<Contract> createContractList(Integer tenantId) {
        List<Contract> contractList = new ArrayList<>(3);
        Contract contract = new Contract(tenantId, "Part Time Contract");
        entityManager.persist(contract);
        contractList.add(contract);

        contract = new Contract(tenantId, "Max 16 Hours Per Week Contract", null, 16 * 60, null, null);
        entityManager.persist(contract);
        contractList.add(contract);

        contract = new Contract(tenantId, "Max 16 Hours Per Week, 32 Hours Per Month Contract",
                null, 16 * 60, 32 * 60, null);
        entityManager.persist(contract);
        contractList.add(contract);

        return contractList;
    }

    @Transactional
    public List<Employee> createEmployeeList(GeneratorType generatorType, Integer tenantId, int size,
            List<Contract> contractList, List<Skill> generalSkillList) {
        List<Employee> employeeList = new ArrayList<>(size);
        employeeNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = employeeNameGenerator.generateNextValue();
            HashSet<Skill> skillProficiencySet = new HashSet<>(extractRandomSubList(generalSkillList,
                    0.1, 0.3, 0.5, 0.7, 0.9, 1.0));
            Employee employee = new Employee(tenantId, name,
                    contractList.get(generateRandomIntFromThresholds(0.7, 0.5)),
                    skillProficiencySet);
            entityManager.persist(employee);
            employeeList.add(employee);
        }
        return employeeList;
    }

    @Transactional
    public List<TimeBucket> createTimeBucketList(GeneratorType generatorType,
            Integer tenantId,
            DayOfWeek startOfWeek,
            RosterState rosterState,
            List<Spot> spotList,
            List<Employee> employeeList,
            List<Skill> skillList) {
        List<TimeBucket> timeBucketList = new ArrayList<>(spotList.size() * generatorType.timeslotRangeList.size());
        List<Employee> remainingEmployeeList = new ArrayList<>(employeeList);
        Consumer<Spot> createTimeBucketsForSpot = (spot) -> {
            // Use if we take advantage of the additional skills for shifts feature
            final Function<Predicate<Employee>, List<Employee>> findEmployees = p -> {
                List<Employee> out = remainingEmployeeList.stream()
                        .filter(employee -> employee.getSkillProficiencySet().containsAll(spot.getRequiredSkillSet()) &&
                                employee.getContract().getMaximumMinutesPerWeek() == null &&
                                p.test(employee))
                        .limit(generatorType.rotationEmployeeListSize).collect(toList());
                remainingEmployeeList.removeAll(out);
                return out;
            };

            List<Employee> rotationEmployeeList = findEmployees.apply(t -> true);
            for (int timeslotRangesIndex = 0; timeslotRangesIndex < generatorType.timeslotRangeList
                    .size(); timeslotRangesIndex++) {
                timeBucketList.add(getTimeBucketForTimeslotRangeListTriple(timeslotRangesIndex,
                        tenantId,
                        spot,
                        generatorType,
                        startOfWeek,
                        rosterState,
                        rotationEmployeeList));
            }
        };
        spotList.stream().forEach(createTimeBucketsForSpot);
        return timeBucketList;
    }

    private TimeBucket getTimeBucketForTimeslotRangeListTriple(int timeslotRangesIndex,
            int tenantId,
            Spot spot,
            GeneratorType generatorType,
            DayOfWeek startOfWeek,
            RosterState rosterState,
            List<Employee> rotationEmployeeList) {
        Triple<LocalTime, LocalTime, List<DayOfWeek>> tr = generatorType.timeslotRangeList.get(timeslotRangesIndex);
        Set<DayOfWeek> repeatDays = tr.getRight().stream().collect(Collectors.toCollection(HashSet::new));
        List<Seat> seatList = new ArrayList<Seat>(rosterState.getRotationLength());

        // For Every Day In the Rotation
        for (int dayOffset = 0; dayOffset < rosterState.getRotationLength(); dayOffset++) {
            DayOfWeek dayOfWeek = startOfWeek.plus(dayOffset);

            // If this Time Bucket occurs on this dayOfWeek
            if (repeatDays.contains(dayOfWeek)) {
                int rotationEmployeeIndex = generatorType.rotationEmployeeIndexCalculator
                        .apply(dayOffset, timeslotRangesIndex);
                if (rotationEmployeeIndex < 0 || rotationEmployeeIndex >= generatorType.rotationEmployeeListSize) {
                    throw new IllegalStateException(
                            "The rotationEmployeeIndexCalculator for generatorType (" +
                                    generatorType.tenantNamePrefix +
                                    ") returns an invalid rotationEmployeeIndex (" + rotationEmployeeIndex +
                                    ") for startDayOffset (" + dayOffset + ") and timeslotRangesIndex (" +
                                    timeslotRangesIndex + ").");
                }
                // There might be less employees than we need (overconstrained planning)
                Employee rotationEmployee = (rotationEmployeeIndex >= rotationEmployeeList.size()) ? null
                        : rotationEmployeeList.get(rotationEmployeeIndex);
                seatList.add(new Seat(dayOffset, rotationEmployee));
            }
        }
        TimeBucket timeBucket = new TimeBucket(tenantId, spot, tr.getLeft(), tr.getMiddle(), new HashSet<>(),
                repeatDays, seatList);
        entityManager.persist(timeBucket);
        return timeBucket;
    }

    @Transactional
    public List<Shift> createShiftList(GeneratorType generatorType,
            Integer tenantId,
            RosterConstraintConfiguration rosterConstraintConfiguration,
            RosterState rosterState,
            List<Spot> spotList,
            List<TimeBucket> timeBucketList) {
        int rotationLength = rosterState.getRotationLength();
        LocalDate nextDate = rosterState.getLastHistoricDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate firstUnplannedDate = rosterState.getFirstUnplannedDate();

        List<Shift> shiftList = new ArrayList<>();

        int nextDayOffset = 0;
        while (nextDate.compareTo(firstUnplannedDate) < 0) {
            final LocalDate date = nextDate;
            final int dayOffset = nextDayOffset;
            spotList.forEach(spot -> {
                shiftList.addAll(
                        generateShiftsForSpotOnDate(generatorType,
                                tenantId,
                                rosterConstraintConfiguration,
                                rosterState,
                                spot,
                                date,
                                dayOffset,
                                timeBucketList));
            });

            nextDate = date.plusDays(1);
            nextDayOffset = (dayOffset + 1) % rotationLength;
        }
        rosterState.setUnplannedRotationOffset(nextDayOffset);
        return shiftList;
    }

    private List<Shift> generateShiftsForSpotOnDate(GeneratorType generatorType,
            Integer tenantId,
            RosterConstraintConfiguration rosterConstraintConfiguration,
            RosterState rosterState,
            Spot spot,
            LocalDate date,
            int rotationDay,
            List<TimeBucket> timeBucketList) {
        List<Shift> shiftList = new ArrayList<>();
        LocalDate firstDraftDate = rosterState.getFirstDraftDate();
        ZoneId zoneId = rosterState.getTimeZone();

        List<TimeBucket> spotTimeBucketList = timeBucketList.stream().filter(tb -> tb.getSpot().equals(spot))
                .collect(Collectors.toList());
        spotTimeBucketList.forEach(timeBucket -> {
            boolean defaultToRotationEmployee = date.compareTo(firstDraftDate) < 0;
            Optional<Shift> maybeShift = timeBucket.createShiftForOffset(date, rotationDay,
                    zoneId, defaultToRotationEmployee);
            maybeShift.ifPresent(shift -> {
                if (date.compareTo(firstDraftDate) < 0 && defaultToRotationEmployee) {
                    shift.setOriginalEmployee(shift.getRotationEmployee());
                }
                entityManager.persist(shift);
                shiftList.add(shift);
            });
        });
        if (date.compareTo(firstDraftDate) >= 0 && !spotTimeBucketList.isEmpty()) {
            int extraShiftCount = generateRandomIntFromThresholds(EXTRA_SHIFT_THRESHOLDS);
            for (int i = 0; i < extraShiftCount; i++) {
                TimeBucket timeBucket = extractRandomElement(spotTimeBucketList);
                Optional<Shift> maybeShift = timeBucket.createShiftForOffset(date, rotationDay,
                        zoneId, false);
                maybeShift.ifPresent(shift -> {
                    entityManager.persist(shift);
                    shiftList.add(shift);
                });
            }
        }

        return shiftList;
    }

    @Transactional
    public List<EmployeeAvailability> createEmployeeAvailabilityList(GeneratorType generatorType,
            Integer tenantId,
            RosterConstraintConfiguration rosterConstraintConfiguration,
            RosterState rosterState,
            List<Employee> employeeList,
            List<Shift> shiftList) {
        ZoneId zoneId = rosterState.getTimeZone();
        // Generate a feasible published schedule: no EmployeeAvailability instance during the published period
        // nor on the first draft day (because they might overlap with shift on the last published day)
        LocalDate date = rosterState.getFirstDraftDate().plusDays(1);
        LocalDate firstUnplannedDate = rosterState.getFirstUnplannedDate();
        List<EmployeeAvailability> employeeAvailabilityList = new ArrayList<>();
        Map<LocalDate, List<Shift>> startDayToShiftListMap = shiftList.stream()
                .collect(groupingBy(shift -> shift.getStartDateTime().toLocalDate()));

        while (date.compareTo(firstUnplannedDate) < 0) {
            List<Shift> dayShiftList = startDayToShiftListMap.getOrDefault(date, Collections.emptyList());
            List<Employee> availableEmployeeList = new ArrayList<>(employeeList);
            int stateCount = (employeeList.size() - dayShiftList.size()) / 4;
            if (stateCount <= 0) {
                // Heavy overconstrained planning (more shifts than employees)
                stateCount = 1;
            }
            for (EmployeeAvailabilityState state : EmployeeAvailabilityState.values()) {
                for (int i = 0; i < stateCount; i++) {
                    Employee employee = availableEmployeeList.remove(random.nextInt(availableEmployeeList.size()));
                    LocalDateTime startDateTime = date.atTime(LocalTime.MIN);
                    LocalDateTime endDateTime = date.plusDays(1).atTime(LocalTime.MIN);
                    OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDateTime,
                            zoneId.getRules().getOffset(startDateTime));
                    OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDateTime,
                            zoneId.getRules().getOffset(endDateTime));
                    EmployeeAvailability employeeAvailability = new EmployeeAvailability(tenantId, employee,
                            startOffsetDateTime,
                            endOffsetDateTime);
                    employeeAvailability.setState(state);
                    entityManager.persist(employeeAvailability);
                    employeeAvailabilityList.add(employeeAvailability);
                }
            }
            date = date.plusDays(1);
        }
        return employeeAvailabilityList;
    }

    private <E> E extractRandomElement(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <E> List<E> extractRandomSubList(List<E> list, double... thresholds) {
        int size = generateRandomIntFromThresholds(thresholds);
        if (size > list.size()) {
            size = list.size();
        }
        return extractRandomSubListOfSize(list, size);
    }

    private <E> List<E> extractRandomSubListOfSize(List<E> list, int size) {
        List<E> subList = new ArrayList<>(list);
        Collections.shuffle(subList, random);
        // Remove elements not in the sublist (so it can be garbage collected)
        subList.subList(size, subList.size()).clear();
        return subList;
    }

    private int generateRandomIntFromThresholds(double... thresholds) {
        double randomDouble = random.nextDouble();
        for (int i = 0; i < thresholds.length; i++) {
            if (randomDouble < thresholds[i]) {
                return i;
            }
        }
        return thresholds.length;
    }
}
