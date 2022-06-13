package org.optaweb.employeerostering.roster;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response.Status;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.roster.PublishResult;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.rotation.Seat;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.util.ShiftRosterXlsxFileIO;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class RosterRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {
    private final String rosterPathURI = "/rest/tenant/{tenantId}/roster/";
    private final String spotPathURI = "/rest/tenant/{tenantId}/spot/";
    private final String contractPathURI = "/rest/tenant/{tenantId}/contract/";
    private final String employeePathURI = "/rest/tenant/{tenantId}/employee/";
    private final String shiftPathURI = "/rest/tenant/{tenantId}/shift/";
    private final String rotationPathURI = "/rest/tenant/{tenantId}/rotation/";
    private final String employeeAvailabilityPathURI =
            "/rest/tenant/{tenantId}/employee/availability/";

    private List<Spot> spotList;
    private List<Employee> employeeList;
    private List<ShiftView> shiftViewList;
    private List<EmployeeAvailabilityView> employeeAvailabilityViewList;

    private Response getRosterState(Integer id) {
        return RestAssured.get(rosterPathURI + id, TENANT_ID);
    }

    private Response getCurrentShiftRosterView(Integer pageNumber,
            Integer numberOfItemsPerPage) {
        return RestAssured.given()
                .basePath(rosterPathURI + "shiftRosterView/current")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .get();
    }

    private Response getShiftRosterView(Integer pageNumber,
            Integer numberOfItemsPerPage, String startDateString,
            String endDateString) {
        return RestAssured.given()
                .basePath(rosterPathURI + "shiftRosterView")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .get();
    }

    private Response getShiftRosterViewFor(String startDateString,
            String endDateString, List<Spot> spots) {
        return RestAssured.given().basePath(rosterPathURI + "shiftRosterView/for")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .body(spots)
                .post();
    }

    private Response getCurrentAvailabilityRosterView(Integer pageNumber,
            Integer numberOfItemsPerPage) {
        return RestAssured.given().basePath(rosterPathURI + "availabilityRosterView/current")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .get();
    }

    private Response getAvailabilityRosterView(Integer pageNumber,
            Integer numberOfItemsPerPage,
            String startDateString,
            String endDateString) {
        return RestAssured.given().basePath(rosterPathURI + "availabilityRosterView")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .get();
    }

    private Response getAvailabilityRosterViewFor(String startDateString,
            String endDateString,
            List<Employee> employees) {
        return RestAssured.given().basePath(rosterPathURI + "availabilityRosterView/for")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .body(employees)
                .post();
    }

    private Response getShiftRosterAsExcel(List<Spot> spotList,
            LocalDate startDate,
            LocalDate endDate) {
        return RestAssured.given().basePath(rosterPathURI + "shiftRosterView/excel")
                .pathParam("tenantId", TENANT_ID)
                .queryParam("startDate", startDate.toString())
                .queryParam("endDate", endDate.toString())
                .queryParam("spotList", spotList.stream().map(Spot::getId).map(id -> id.toString())
                        .collect(Collectors.joining(",")))
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .accept("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .get();
    }

    private Response publishAndProvision() {
        return RestAssured.post(rosterPathURI + "publishAndProvision", TENANT_ID);
    }

    private Response commitChanges() {
        return RestAssured.post(rosterPathURI + "commitChanges", TENANT_ID);
    }

    private Response provision(Integer tenantId, Integer startRotationOffset, LocalDate fromDate,
            LocalDate toDate, List<Long> timeBucketIdList) {
        return RestAssured.given().basePath(rosterPathURI + "provision")
                .pathParam("tenantId", tenantId)
                .queryParam("startRotationOffset", startRotationOffset.toString())
                .queryParam("fromDate", fromDate.toString())
                .queryParam("toDate", toDate.toString())
                .body(timeBucketIdList)
                .post();
    }

    private Spot addSpot(String name) {
        SpotView spotView = new SpotView(TENANT_ID, name, Collections.emptySet());
        return RestAssured.given().basePath(spotPathURI + "add")
                .pathParam("tenantId", TENANT_ID)
                .body(spotView)
                .post().as(Spot.class);
    }

    private Contract addContract(String name) {
        ContractView contractView = new ContractView(TENANT_ID, name);
        return RestAssured.given().basePath(contractPathURI + "add")
                .pathParam("tenantId", TENANT_ID)
                .body(contractView)
                .post().as(Contract.class);
    }

    private Employee addEmployee(String name, Contract contract) {
        Employee employee = new Employee(TENANT_ID, name, contract, Collections.emptySet());
        return RestAssured.given().basePath(employeePathURI + "add")
                .pathParam("tenantId", TENANT_ID)
                .body(employee)
                .post().as(Employee.class);
    }

    private TimeBucketView addTimeBucket(TimeBucketView timeBucket) {
        return RestAssured.given().basePath(rotationPathURI + "add")
                .pathParam("tenantId", TENANT_ID)
                .body(timeBucket)
                .post().as(TimeBucketView.class);
    }

    private ShiftView addShift(Spot spot, Employee employee, LocalDateTime startDateTime,
            Duration duration) {
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, startDateTime.plus(duration));
        if (employee != null) {
            shiftView.setEmployeeId(employee.getId());
        }
        return RestAssured.given().basePath(shiftPathURI + "add")
                .pathParam("tenantId", TENANT_ID)
                .body(shiftView)
                .post().as(ShiftView.class);
    }

    private EmployeeAvailabilityView addEmployeeAvailability(Employee employee,
            EmployeeAvailabilityState employeeAvailabilityState,
            LocalDateTime startDateTime,
            Duration duration) {
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime,
                startDateTime.plus(duration),
                employeeAvailabilityState);
        return RestAssured.given().basePath(employeeAvailabilityPathURI + "add")
                .pathParam("tenantId", TENANT_ID)
                .body(employeeAvailabilityView)
                .post().as(EmployeeAvailabilityView.class);
    }

    private void createTestRoster() {
        createTestTenant();

        Contract contract = addContract("contract");
        Employee employeeA = addEmployee("Employee A", contract);
        Employee employeeB = addEmployee("Employee B", contract);

        Spot spotA = addSpot("Spot A");
        Spot spotB = addSpot("Spot B");

        EmployeeAvailabilityView employeeAvailabilityA = addEmployeeAvailability(employeeA,
                EmployeeAvailabilityState.UNAVAILABLE,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                Duration.ofDays(1));

        ShiftView shiftA = addShift(spotA, null, LocalDateTime.of(2000, 1, 1, 9, 0), Duration.ofHours(8));
        ShiftView shiftB = addShift(spotB, employeeB, LocalDateTime.of(2000, 1, 1, 9, 0), Duration.ofHours(8));
        ShiftView shiftC = addShift(spotA, employeeA, LocalDateTime.of(2000, 1, 2, 9, 0), Duration.ofHours(8));
        ShiftView shiftD = addShift(spotB, employeeB, LocalDateTime.of(2000, 1, 2, 9, 0), Duration.ofHours(8));

        spotList = Arrays.asList(spotA, spotB);
        employeeList = Arrays.asList(employeeA, employeeB);
        shiftViewList = Arrays.asList(shiftA, shiftB, shiftC, shiftD);
        employeeAvailabilityViewList = Arrays.asList(employeeAvailabilityA);
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getRosterStateTest() {
        RosterStateView rosterStateView = new RosterStateView(null, 7, LocalDate.of(2000, 1, 1), 1, 7, 0, 7,
                LocalDate.of(1999, 12, 24),
                ZoneOffset.UTC);
        rosterStateView.setTenant(new Tenant("test"));
        Tenant tenant = createTestTenant(rosterStateView);
        rosterStateView.setTenant(tenant);
        rosterStateView.setTenantId(tenant.getId());

        Response rosterStateResponseEntity = getRosterState(TENANT_ID);
        assertThat(rosterStateResponseEntity.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        RosterState rosterState = rosterStateResponseEntity.as(RosterState.class);
        assertThat(rosterState.getPublishNotice()).isEqualTo(7);
        assertThat(rosterState.getFirstDraftDate().toString()).isEqualTo("2000-01-01");
        assertThat(rosterState.getPublishLength()).isEqualTo(1);
        assertThat(rosterState.getDraftLength()).isEqualTo(7);
        assertThat(rosterState.getUnplannedRotationOffset()).isEqualTo(0);
        assertThat(rosterState.getRotationLength()).isEqualTo(7);
        assertThat(rosterState.getLastHistoricDate().toString()).isEqualTo("1999-12-24");
        assertThat(rosterState.getTimeZone().toString()).isEqualTo("Z");
        assertThat(rosterState.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(rosterState.getTenant().getName()).isEqualTo("TestTenant");
        assertThat(rosterState.getTenant().getId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetCurrentShiftRosterView() {
        createTestRoster();

        Response shiftRosterViewResponse = getCurrentShiftRosterView(0, 1);
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(0, 1));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterViewResponse = getCurrentShiftRosterView(1, 1);
        shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(1, 2));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetShiftRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        Response shiftRosterViewResponse = getShiftRosterView(0, 1,
                startDate.toString(),
                endDate.toString());
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(0, 1));
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).containsOnly(
                entry(spotList.get(0).getId(), Arrays.asList(shiftViewList.get(0))));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterViewResponse = getShiftRosterView(1, 1, startDate.toString(), endDate.toString());
        shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(1, 2));
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).containsOnly(
                entry(spotList.get(1).getId(), Arrays.asList(shiftViewList.get(1))));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetShiftRosterViewFor() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        Response shiftRosterViewResponse = getShiftRosterViewFor(startDate.toString(),
                endDate.toString(),
                spotList);
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).isEqualTo(spotList);
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterViewResponse = getShiftRosterViewFor(startDate.toString(), endDate.toString(),
                spotList);
        shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).isEqualTo(spotList);
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testCurrentGetAvailabilityRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(1999, 12, 24);
        LocalDate endDate = LocalDate.of(2000, 1, 25);
        Response availabilityRosterViewResponse = getCurrentAvailabilityRosterView(0, 1);
        AvailabilityRosterView availabilityRosterView = availabilityRosterViewResponse.as(AvailabilityRosterView.class);
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(0, 1));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).containsOnly(
                entry(employeeList.get(0).getId(), Arrays.asList(employeeAvailabilityViewList.get(0))));
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterViewResponse = getCurrentAvailabilityRosterView(1, 1);
        availabilityRosterView = availabilityRosterViewResponse.as(AvailabilityRosterView.class);
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(1, 2));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetAvailabilityRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        Response availabilityRosterViewResponse = getAvailabilityRosterView(
                0, 1,
                startDate.toString(),
                endDate.toString());
        AvailabilityRosterView availabilityRosterView = availabilityRosterViewResponse.as(AvailabilityRosterView.class);
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(0, 1));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).containsOnly(
                entry(employeeList.get(0).getId(), Arrays.asList(employeeAvailabilityViewList.get(0))));
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterViewResponse = getAvailabilityRosterView(1, 1, startDate.toString(), endDate.toString());
        availabilityRosterView = availabilityRosterViewResponse.as(AvailabilityRosterView.class);
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(1, 2));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).containsOnly(
                entry(employeeList.get(1).getId(), Arrays.asList(shiftViewList.get(1))));
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetAvailabilityRosterViewFor() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        Response availabilityRosterViewResponse =
                getAvailabilityRosterViewFor(startDate.toString(), endDate.toString(), Collections.emptyList());
        AvailabilityRosterView availabilityRosterView = availabilityRosterViewResponse.as(AvailabilityRosterView.class);
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).isEmpty();
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterViewResponse = getAvailabilityRosterViewFor(startDate.toString(), endDate.toString(),
                Collections.emptyList());
        availabilityRosterView = availabilityRosterViewResponse.as(AvailabilityRosterView.class);
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).isEmpty();
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testProvision() {
        createTestTenant();

        // To date before from date should result in an error
        Response publishResultResponseEntity = provision(TENANT_ID, 0, LocalDate.of(2000, 1, 5),
                LocalDate.of(2000, 1, 1),
                Collections.emptyList());
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());

        // Negative rotation start offset should result in an error
        publishResultResponseEntity = provision(TENANT_ID, -5, LocalDate.of(2000, 1, 5),
                LocalDate.of(2000, 1, 5),
                Collections.emptyList());
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());

        // rotation start offset over rotation length should result in an error
        publishResultResponseEntity = provision(TENANT_ID, 9001, LocalDate.of(2000, 1, 5),
                LocalDate.of(2000, 1, 5),
                Collections.emptyList());
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());

        Spot spot = addSpot("Spot");
        Contract contract = addContract("Contract");
        Employee employee = addEmployee("Employee", contract);
        TimeBucketView timeBucketView = addTimeBucket(
                new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0), LocalTime.of(17, 0),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.singletonList(new Seat(0, employee)))));

        LocalDate fromDate = LocalDate.of(2000, 1, 5);
        LocalDate toDate = LocalDate.of(2000, 1, 5);

        // time bucket from different tenant should fail
        publishResultResponseEntity = provision(0, 0, fromDate,
                toDate,
                Collections.singletonList(timeBucketView.getId()));
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(Status.NOT_FOUND.getStatusCode());

        // Test valid request
        assertThat(timeBucketView.getTenantId()).isEqualTo(TENANT_ID);
        publishResultResponseEntity = provision(TENANT_ID, 0, fromDate,
                toDate,
                Collections.singletonList(timeBucketView).stream()
                        .map(TimeBucketView::getId).collect(Collectors.toList()));
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());

        Response shiftRosterViewResponse = getShiftRosterViewFor(fromDate.toString(),
                toDate.plusDays(1).toString(),
                Collections
                        .singletonList(spot));
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.as(ShiftRosterView.class);
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getSpotList()).containsExactly(spot);

        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).hasSize(1);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap().get(spot.getId())).hasSize(1);
        ShiftView shift = shiftRosterView.getSpotIdToShiftViewListMap().get(spot.getId()).get(0);

        assertThat(shift.getSpotId()).isEqualTo(spot.getId());
        assertThat(shift.getStartDateTime().toLocalDate()).isEqualTo(fromDate);
        assertThat(shift.getEndDateTime().toLocalDate()).isEqualTo(fromDate);

        assertThat(shift.getStartDateTime().toLocalTime()).isEqualTo(timeBucketView.getStartTime());
        assertThat(shift.getEndDateTime().toLocalTime()).isEqualTo(timeBucketView.getEndTime());
    }

    @Test
    public void testPublishAndProvision() {
        createTestRoster();

        Response publishResultResponseEntity = publishAndProvision();
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        PublishResult publishResult = publishResultResponseEntity.as(PublishResult.class);
        assertThat(publishResult.getPublishedFromDate()).isEqualTo("2000-01-01");
        assertThat(publishResult.getPublishedToDate()).isEqualTo("2000-01-08");
    }

    @Test
    public void testGetShiftRosterAsExcel() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);

        List<Spot> requestSpotList = spotList.subList(0, 1);
        Response response = getShiftRosterAsExcel(requestSpotList, startDate, endDate);

        assertThat(response.getStatusCode()).as("Response should be successful for one spot")
                .isEqualTo(Status.OK.getStatusCode());
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.asByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).as("There should only be one sheet in the file").isEqualTo(1);
            Sheet spotSheet = workbook.getSheet(requestSpotList.get(0).getName());
            assertSheetForSpotIsCorrect(spotSheet, requestSpotList.get(0), startDate, endDate);
        } catch (IOException e) {
            fail("Unable to read response as an Excel file");
        }

        requestSpotList = spotList;
        response = getShiftRosterAsExcel(requestSpotList, startDate, endDate);

        assertThat(response.getStatusCode()).as("Response should be successful for two spots")
                .isEqualTo(Status.OK.getStatusCode());
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.asByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).as("There should only be two sheets in the file").isEqualTo(2);
            Sheet spotSheet = workbook.getSheet(requestSpotList.get(0).getName());
            assertSheetForSpotIsCorrect(spotSheet, requestSpotList.get(0), startDate, endDate);
            spotSheet = workbook.getSheet(requestSpotList.get(1).getName());
            assertSheetForSpotIsCorrect(spotSheet, requestSpotList.get(1), startDate, endDate);
        } catch (IOException e) {
            fail("Unable to read response as an Excel file");
        }

        Spot badSpot = new Spot(TENANT_ID, "Bad", Collections.emptySet());
        badSpot.setId(-1L);

        requestSpotList = Arrays.asList(badSpot);
        response = getShiftRosterAsExcel(requestSpotList, startDate, endDate);

        assertThat(response.getStatusCode()).as("Response should fail with BAD_REQUEST for non-existing spot in tenant")
                .isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    private void assertSheetForSpotIsCorrect(Sheet spotSheet, Spot spot, LocalDate from, LocalDate to) {
        assertThat(spotSheet).as("Spot %s sheet should not be null", spot.getName()).isNotNull();

        Map<Long, String> employeeIdToNameList = employeeList.stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        Map<List<LocalDateTime>, List<ShiftView>> requestShiftMapByStartAndEndTime = shiftViewList.stream()
                // Include only shift views for this spot, between from and to
                .filter(sv -> sv.getSpotId().equals(spot.getId()) &&
                // to is exclusive, from is inclusive
                        !sv.getStartDateTime().toLocalDate().isAfter(to.minusDays(1)) &&
                        !sv.getEndDateTime().toLocalDate().isBefore(from))
                .collect(Collectors.groupingBy(sv -> Arrays.asList(sv.getStartDateTime(), sv.getEndDateTime())));

        assertThat(spotSheet.getRow(0).getCell(0).getStringCellValue())
                .as("Header Column A should be Start").isEqualTo("Start");
        assertThat(spotSheet.getRow(0).getCell(1).getStringCellValue())
                .as("Header Column B should be End").isEqualTo("End");
        assertThat(spotSheet.getRow(0).getCell(2).getStringCellValue())
                .as("Header Column C should be Employee").isEqualTo("Employee");

        // Sort startEndTimePairs by start time then end time
        List<List<LocalDateTime>> sortedStartEndTimePairs = requestShiftMapByStartAndEndTime.keySet().stream()
                .sorted(Comparator.comparing((List<LocalDateTime> pair) -> pair.get(0))
                        .thenComparing(pair -> pair.get(1)))
                .collect(Collectors.toList());

        int rowIndex = 1;
        assertThat(requestShiftMapByStartAndEndTime).isNotEmpty();

        for (List<LocalDateTime> startEndTimePair : sortedStartEndTimePairs) {
            // First row of each time slot group has start date time and end date time
            assertThat(spotSheet.getRow(rowIndex).getCell(0).getStringCellValue())
                    .as("Start time for row %d", rowIndex)
                    .isEqualTo(startEndTimePair.get(0).format(ShiftRosterXlsxFileIO.DATE_TIME_FORMATTER));
            assertThat(spotSheet.getRow(rowIndex).getCell(1).getStringCellValue())
                    .as("End time for row %d", rowIndex)
                    .isEqualTo(startEndTimePair.get(1).format(ShiftRosterXlsxFileIO.DATE_TIME_FORMATTER));

            rowIndex++;
            for (ShiftView shiftView : requestShiftMapByStartAndEndTime.get(startEndTimePair)) {
                // Employee name is in the third column
                assertThat(spotSheet.getRow(rowIndex).getCell(2).getStringCellValue())
                        .as("%s to %s Shift's Employee Name", startEndTimePair.get(0).toString(),
                                startEndTimePair.get(1).toString())
                        .isEqualTo(employeeIdToNameList.getOrDefault(shiftView.getEmployeeId(), "Unassigned"));
                rowIndex++;
            }
        }
        assertThat(spotSheet.getRow(rowIndex)).as("Sheet should end on row %d", rowIndex).isNull();
    }

    @Test
    public void testCommitChanges() {
        createTestRoster();

        Response commitChangesResponseEntity = commitChanges();
        assertThat(commitChangesResponseEntity.getStatusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    }
}
