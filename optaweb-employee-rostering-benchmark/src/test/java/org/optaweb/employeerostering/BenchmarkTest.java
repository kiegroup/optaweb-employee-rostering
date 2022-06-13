package org.optaweb.employeerostering;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class BenchmarkTest {

    private static Set<File> oldBenchmarkFilesInDirectory = Collections.emptySet();

    @Inject
    OptaWebEmployeeRosteringBenchmarkApplication app;

    @BeforeAll
    public static void setup() {
        Path benchmarkLocalDirectory = Paths.get("local/benchmarkReport");
        if (benchmarkLocalDirectory.toFile().exists()) {
            oldBenchmarkFilesInDirectory = new HashSet<>(Arrays.asList(benchmarkLocalDirectory.toFile().listFiles()));
        }
    }

    @Test
    public void isBenchmarkReportGeneratedTest() throws FileNotFoundException {
        app.run();
        File benchmarkReport = Arrays.stream(Paths.get("local/benchmarkReport").toFile().listFiles())
                .filter(f -> !oldBenchmarkFilesInDirectory.contains(f))
                .findAny()
                .orElseThrow(() -> new FileNotFoundException("No benchmark report found"));
        assertThat(benchmarkReport.exists())
                .withFailMessage("No Benchmark Report at " + benchmarkReport.getAbsolutePath())
                .isTrue();
    }
}
