import org.kie.jenkins.jobdsl.templates.KogitoJobTemplate

def getDefaultJobParams(String repoName = 'optaweb-employee-rostering') {
    return KogitoJobTemplate.getDefaultJobParams(this, repoName)
}

Map getMultijobPRConfig() {
    return [
        parallel: true,
        buildchain: true,
        jobs : [
            [
                id: 'optaweb-employee-rostering',
                primary: true,
            ]
        ],
    ]
}

setupMultijobPrDefaultChecks()
// setupMultijobPrNativeChecks() // There is no requirement for Native support; there is no Native setup in this repo.
setupMultijobPrLTSChecks()

/////////////////////////////////////////////////////////////////
// Methods
/////////////////////////////////////////////////////////////////

void setupMultijobPrDefaultChecks() {
    KogitoJobTemplate.createMultijobPRJobs(this, getMultijobPRConfig()) { return getDefaultJobParams() }
}

void setupMultijobPrNativeChecks() {
    KogitoJobTemplate.createMultijobNativePRJobs(this, getMultijobPRConfig()) { return getDefaultJobParams() }
}

void setupMultijobPrLTSChecks() {
    KogitoJobTemplate.createMultijobLTSPRJobs(this, getMultijobPRConfig()) { return getDefaultJobParams() }
}
