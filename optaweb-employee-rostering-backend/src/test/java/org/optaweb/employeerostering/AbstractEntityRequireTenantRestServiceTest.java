package org.optaweb.employeerostering;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.Tenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapper;
import io.restassured.mapper.ObjectMapperDeserializationContext;
import io.restassured.mapper.ObjectMapperSerializationContext;

// Cannot inject tenantService if this will be reused for native tests
public class AbstractEntityRequireTenantRestServiceTest {

    private final String tenantPathURI = "/rest/tenant/";
    private final String adminPathURI = "/rest/admin/";

    protected Integer TENANT_ID;

    /**
     * Create a tenant with timezone UTC
     */
    protected Tenant createTestTenant() {
        return createTestTenant(ZoneOffset.UTC);
    }

    /**
     * Create a tenant with timezone zoneId
     * 
     * @param zoneId the timezone for the tenant to be in, not null
     */
    protected Tenant createTestTenant(ZoneId zoneId) {
        return createTestTenant(new RosterStateView(null, 7, LocalDate.of(2000, 1, 1), 7, 24, 0, 7,
                LocalDate.of(1999, 12, 24), zoneId));
    }

    /**
     * Create a tenant with the specified roster state
     * 
     * @param rosterStateView the initial roster state for the tenant, not null
     */
    protected Tenant createTestTenant(RosterStateView rosterStateView) {
        setupRestAssured();
        rosterStateView.setTenant(new Tenant("TestTenant"));
        rosterStateView.setTenantId(-1);
        rosterStateView.getTenant().setId(-1);
        Tenant tenant = RestAssured.given()
                .basePath(tenantPathURI + "add")
                .body(rosterStateView)
                .post()
                .as(Tenant.class);
        TENANT_ID = tenant.getId();
        return tenant;
    }

    protected void deleteTestTenant() {
        RestAssured.given()
                .basePath(adminPathURI + "reset")
                .post();
        TENANT_ID = null;
    }

    protected void setupRestAssured() {
        JacksonCustomizer jacksonCustomizer = new JacksonCustomizer();
        com.fasterxml.jackson.databind.ObjectMapper jacksonMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        jacksonCustomizer.customize(jacksonMapper);
        jacksonMapper.registerModule(new JavaTimeModule());
        jacksonMapper.registerModule(OptaPlannerJacksonModule.createModule());

        ObjectMapper mapper = new ObjectMapper() {
            @Override
            public Object deserialize(ObjectMapperDeserializationContext context) {
                try {
                    if (context.getType() instanceof Class<?>) {
                        return jacksonMapper.readerFor((Class<?>) context.getType())
                                .readValue(context.getDataToDeserialize().asString());
                    } else {
                        return jacksonMapper.readerFor((TypeReference<?>) context.getType())
                                .readValue(context.getDataToDeserialize().asString());
                    }
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Unable to deserialize response", e);
                }
            }

            @Override
            public Object serialize(ObjectMapperSerializationContext context) {
                try {
                    return jacksonMapper.writerFor(context.getObjectToSerialize().getClass())
                            .writeValueAsString(context.getObjectToSerialize());
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Unable to serialize request", e);
                }
            }
        };
        RestAssured.config = RestAssured.config().objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().defaultObjectMapper(mapper));
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
