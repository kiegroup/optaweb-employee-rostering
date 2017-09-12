package org.optaplanner.openshift.employeerostering.webapp;

import java.io.File;
import java.io.IOException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractClientArquillianTest {

    private static final String POM_DIRECTORY_NAME = "optashift-employee-rostering-webapp";

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File file = findPomFile();
        return ShrinkWrap.create(MavenImporter.class)
                .loadPomFromFile(file)
                .importBuildOutput()
                .as(WebArchive.class);
    }

    private static File findPomFile() {
        File file = new File("pom.xml");
        if (!file.exists()) {
            throw new IllegalStateException("The file (" + file + ") does not exist.\n"
                    + "This test needs to be run with the working directory " +  POM_DIRECTORY_NAME + ".");
        }
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException("Could not get cannonical file for file (" + file + ").", e);
        }
        if (!file.getParentFile().getName().equals(POM_DIRECTORY_NAME)) {
            throw new IllegalStateException("The file (" + file + ") is not correct.\n"
                    + "This test needs to be run with the working directory " + POM_DIRECTORY_NAME + ".");
        }
        return file;
    }
    
}