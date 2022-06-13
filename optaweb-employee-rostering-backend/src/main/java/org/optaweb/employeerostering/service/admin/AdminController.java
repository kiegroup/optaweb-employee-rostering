package org.optaweb.employeerostering.service.admin;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/rest/admin")
@ApplicationScoped
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    @Inject
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Reset Application", description = "Resets the application")
    @POST
    @Path("/reset")
    public void resetApplication() {
        adminService.resetApplication();
    }
}
