import org.kie.jenkins.jobdsl.model.Folder
import org.kie.jenkins.jobdsl.templates.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoJobUtils

OPTAWEB_EMPLOYEE_ROSTERING = 'optaweb-employee-rostering'

def getDefaultJobParams() {
    return KogitoJobUtils.getDefaultJobParams(this, OPTAWEB_EMPLOYEE_ROSTERING)
}

Map getMultijobPRConfig() {
    return [
        parallel: true,
        buildchain: true,
        jobs : [
            [
                id: OPTAWEB_EMPLOYEE_ROSTERING,
                primary: true,
            ]
        ],
    ]
}

// PR checks
KogitoJobUtils.createAllEnvsPerRepoPRJobs(this, { jobFolder -> getMultijobPRConfig() }, { return getDefaultJobParams() })

// Create all Nightly/Release jobs
KogitoJobUtils.createAllJobsForArtifactsRepository(this, OPTAWEB_EMPLOYEE_ROSTERING, ['optaplanner'])