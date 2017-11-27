package org.optaplanner.openshift.employeerostering.shared.employee;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;

@Path("/tenant/{tenantId}/employee")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface EmployeeRestService {

    @GET
    @Path("/")
    List<Employee> getEmployeeList(@PathParam("tenantId") Integer tenantId);

    /**
     * @param id never null
     * @return never null, the employee with the id
     */
    @GET
    @Path("/{id}")
    Employee getEmployee(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    /**
     * @param employee never null
     * @return never null, the id of the newly created employee
     */
    @POST
    @Path("/add")
    Long addEmployee(@PathParam("tenantId") Integer tenantId, Employee employee);
    
    /**
     * @param id never null
     * @param employee never null
     * @return true if the employee was successfully updated, false otherwise
     */
    @POST
    @Path("/update/{id}")
    Boolean updateEmployee(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id, Employee employee);

    /**
     * @param id never null
     * @return true if the employee was removed, false otherwise
     */
    @DELETE
    @Path("/{id}")
    Boolean removeEmployee(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    /**
     * @param employeeAvailability never null
     * @return never null, the id
     */
    @POST
    @Path("/availability/add")
    Long addEmployeeAvailability(@PathParam("tenantId") Integer tenantId, EmployeeAvailabilityView employeeAvailability);

    /**
     * @param employeeAvailability never null
     */
    @PUT
    @Path("/availability/update")
    void updateEmployeeAvailability(@PathParam("tenantId") Integer tenantId, EmployeeAvailabilityView employeeAvailability);

    /**
     * @param id never null
     * @return never null, the id
     */
    @DELETE
    @Path("/availability/{id}")
    Boolean removeEmployeeAvailability(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

}
