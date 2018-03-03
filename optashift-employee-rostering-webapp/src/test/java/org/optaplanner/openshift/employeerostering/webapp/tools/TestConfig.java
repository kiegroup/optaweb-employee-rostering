package org.optaplanner.openshift.employeerostering.webapp.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test configuration read from a filtered classpath property file
 **/
public final class TestConfig {

    private static final String TEST_CONFIG_RESOURCE_LOCATION = "/test.properties";

    private static Properties testConfiguration = new Properties();

    static {
        try (InputStream testConfigInputStream = TestConfig.class.getResourceAsStream(TEST_CONFIG_RESOURCE_LOCATION)) {
            testConfiguration.load(testConfigInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read test configuration from " + TEST_CONFIG_RESOURCE_LOCATION, e);
        }
    }

    public static String getApplicationUrl() {
        return testConfiguration.getProperty("application.url");
    }
}
