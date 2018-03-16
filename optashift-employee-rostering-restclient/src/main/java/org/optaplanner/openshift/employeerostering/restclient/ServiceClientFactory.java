package org.optaplanner.openshift.employeerostering.restclient;

import java.net.URL;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestService;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestService;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestService;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestService;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestService;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestService;

/**
 * Creates client proxies to remote REST API endpoints.
 **/
public final class ServiceClientFactory {

    private static final String REST_API_CONTEXT = "rest";

    private URL baseUrl;
    private ResteasyClient resteasyClient;
    private ResteasyWebTarget target;

    /**
     * @param baseUrl URL of the Employee Shift Rostering application
     **/
    public ServiceClientFactory(URL baseUrl) {
        this(baseUrl, new ResteasyClientBuilder().build());
    }

    /**
     * @param baseUrl URL of the Employee Shift Rostering application
     * @param resteasyClient Resteasy client to be used for restclient connections
     **/
    public ServiceClientFactory(URL baseUrl, ResteasyClient resteasyClient) {
        this.baseUrl = baseUrl;
        this.resteasyClient = resteasyClient;
        this.target = resteasyClient.target(baseUrl.toExternalForm() + REST_API_CONTEXT);
    }

    public SkillRestService createSkillRestServiceClient() {
        return target.proxy(SkillRestService.class);
    }

    public EmployeeRestService createEmployeeRestServiceClient() {
        return target.proxy(EmployeeRestService.class);
    }

    public RosterRestService createRosterRestServiceClient() {
        return target.proxy(RosterRestService.class);
    }

    public ShiftRestService createShiftRestServiceClient() {
        return target.proxy(ShiftRestService.class);
    }

    public SpotRestService createSpotRestServiceClient() {
        return target.proxy(SpotRestService.class);
    }

    public TenantRestService createTenantRestServiceClient() {
        return target.proxy(TenantRestService.class);
    }

}
