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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OptaWebEmployeeRosteringBenchmarkApplication.class)
public class BenchmarkTest {

    private static Set<File> oldBenchmarkFilesInDirectory = Collections.emptySet();

    @BeforeClass
    public static void setup() {
        Path benchmarkLocalDirectory = Paths.get("local/benchmarkReport");
        if (benchmarkLocalDirectory.toFile().exists()) {
            oldBenchmarkFilesInDirectory = new HashSet<>(Arrays.asList(benchmarkLocalDirectory.toFile().listFiles()));
        }
    }

    @Test
    public void isBenchmarkReportGeneratedTest() throws FileNotFoundException {
        File benchmarkReport = Arrays.stream(Paths.get("local/benchmarkReport").toFile().listFiles())
                .filter(f -> !oldBenchmarkFilesInDirectory.contains(f))
                .findAny()
                .orElseThrow(() -> new FileNotFoundException("No benchmark report found"));
        assertTrue(benchmarkReport.exists());
    }
}
