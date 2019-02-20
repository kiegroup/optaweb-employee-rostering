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

package org.optaweb.employeerostering.webapp.tools;

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
