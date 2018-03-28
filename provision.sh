#!/bin/bash
set -e

command -v oc >/dev/null 2>&1 || {
  echo >&2 "ERROR: The oc client tools need to be installed to connect to OpenShift Online.";
  echo >&2 "       Download it from https://www.openshift.org/download.html and confirm that \"oc version\" runs.";
  exit 1;
}

################################################################################
# Provisioning script to deploy the app on an OpenShift environment           #
################################################################################
function usage() {
    echo
    echo "Usage:"
    echo " $0 [command] [app-name] [options]"
    echo " $0 --help"
    echo
    echo "Example:"
    echo " $0 setup --maven-mirror-url http://nexus.repo.com/content/groups/public/ --project-suffix s40d"
    echo
    echo "COMMANDS:"
    echo "   setup                    Set up the projects and deploy the apps"
    echo "   deploy                   Deploy the apps"
    echo "   delete                   Clean up and remove projects and objects"
    echo "   verify                   Verify the app is deployed correctly"
    echo "   idle                     Make all services idle"
    echo
    echo "APPS:"
    echo "   employee-rostering       OptaShift Employee Rostering"
    echo
    echo "OPTIONS:"
    echo "   --binary                  Performs an OpenShift 'binary-build', which builds the WAR file locally and sends it to the OpenShift BuildConfig. Requires less memory in OpenShift."
    echo "   --user [username]         The admin user for the project. mandatory if logged in as system:admin"
    echo "   --project-suffix [suffix] Suffix to be added to project names e.g. ci-SUFFIX. If empty, user will be used as suffix."
    echo "   --run-verify              Run verify after provisioning"
    # TODO support --maven-mirror-url
    echo
}

ARG_USERNAME=
ARG_PROJECT_SUFFIX=
ARG_COMMAND=
ARG_RUN_VERIFY=false
ARG_BINARY_BUILD=false
ARG_APP=

while :; do
    case $1 in
        setup)
            ARG_COMMAND=setup
            if [ -n "$2" ]; then
                ARG_APP=$2
                shift
            fi
            ;;
        deploy)
            ARG_COMMAND=deploy
            if [ -n "$2" ]; then
                ARG_APP=$2
                shift
            fi
            ;;
        delete)
            ARG_COMMAND=delete
            if [ -n "$2" ]; then
                ARG_APP=$2
                shift
            fi
            ;;
        verify)
            ARG_COMMAND=verify
            if [ -n "$2" ]; then
                ARG_APP=$2
                shift
            fi
            ;;
        idle)
            ARG_COMMAND=idle
            if [ -n "$2" ]; then
                ARG_APP=$2
                shift
            fi
            ;;
        --user)
            if [ -n "$2" ]; then
                ARG_USERNAME=$2
                shift
            else
                printf 'ERROR: "--user" requires a non-empty value.\n' >&2
                usage
                exit 255
            fi
            ;;
        --project-suffix)
            if [ -n "$2" ]; then
                ARG_PROJECT_SUFFIX=$2
                shift
            else
                printf 'ERROR: "--project-suffix" requires a non-empty value.\n' >&2
                usage
                exit 255
            fi
            ;;
        --run-verify)
            ARG_RUN_VERIFY=true
            ;;
        --binary)
            ARG_BINARY_BUILD=true
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        --)
            shift
            break
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            shift
            ;;
        *)               # Default case: If no more options then break out of the loop.
            break
    esac

    shift
done


################################################################################
# Configuration                                                                #
################################################################################
LOGGEDIN_USER=$(oc whoami)
OPENSHIFT_USER=${ARG_USERNAME:-$LOGGEDIN_USER}

# project
PRJ_SUFFIX=${ARG_PROJECT_SUFFIX:-`echo $OPENSHIFT_USER | sed -e 's/[-@].*//g'`}
PRJ=optashift-$PRJ_SUFFIX

PRJ_DISPLAY_NAME="OptaShift Employee Rostering"
PRJ_DESCRIPTION="Employee Rostering with OptaPlanner on OpenShift"

# config
GITHUB_ACCOUNT=${GITHUB_ACCOUNT:-kiegroup}
GIT_REF=${GITHUB_REF:-master}
GIT_URI=https://github.com/$GITHUB_ACCOUNT/optashift-employee-rostering

################################################################################
# APP MATRIX                                                                  #
################################################################################
case $ARG_APP in
    employee-rostering)
	   # No need to set anything here anymore.
	;;
    *)
        echo "ERROR: Invalid app name: \"$ARG_APP\"."
        usage
        exit 255
        ;;
esac


################################################################################
# Functions                                                                    #
################################################################################

function echo_header() {
  echo
  echo "########################################################################"
  echo $1
  echo "########################################################################"
}

function print_info() {
  echo_header "Configuration"

  OPENSHIFT_MASTER=$(oc status | head -1 | sed 's#.*\(https://[^ ]*\)#\1#g') # must run after projects are created

  echo "App name:            $ARG_APP"
  echo "OpenShift master:    $OPENSHIFT_MASTER"
  echo "Current user:        $LOGGEDIN_USER"
  echo "Project suffix:      $PRJ_SUFFIX"
  echo "GitHub repo:         $GIT_URI"
  echo "GitHub branch/tag:   $GITHUB_REF"
}

# waits while the condition is true until it becomes false or it times out
function wait_while_empty() {
  local _NAME=$1
  local _TIMEOUT=$(($2/5))
  local _CONDITION=$3

  echo "Waiting for $_NAME to be ready..."
  local x=1
  while [ -z "$(eval ${_CONDITION})" ]
  do
    echo "."
    sleep 5
    x=$(( $x + 1 ))
    if [ $x -gt $_TIMEOUT ]
    then
      echo "ERROR: $_NAME still not ready, I GIVE UP!"
      exit 255
    fi
  done

  echo "$_NAME is ready."
}

# Create Project
function create_projects() {
  echo_header "Creating project..."

  echo "Creating project $PRJ"
  oc new-project $PRJ --display-name="$PRJ_DISPLAY_NAME" --description="$PRJ_DESCRIPTION" >/dev/null
}


function create_application() {
  echo_header "Creating OptaShift Build and Deployment config."
  oc process -f openshift/templates/optashift-employee-rostering-template.yaml -p GIT_URI="$GIT_URI" -p GIT_REF="$GIT_REF" -n $PRJ | oc create -f - -n $PRJ
}

function create_application_binary() {
  echo_header "Creating OptaShift Build and Deployment config."
  oc process -f openshift/templates/optashift-employee-rostering-template-binary.yaml -p GIT_URI="$GIT_URI" -p GIT_REF="$GIT_REF" -n $PRJ | oc create -f - -n $PRJ
}

function build_and_deploy() {
  echo_header "Starting OpenShift build and deploy..."
  oc start-build employee-rostering
}

function build_and_deploy_binary() {
  start_maven_build
  
  echo_header "Starting OpenShift binary deploy..."
  oc start-build employee-rostering --from-file=target/ROOT.war
}

function start_maven_build() {
    command -v mvn >/dev/null 2>&1 || {
      echo >&2 "ERROR: The Maven build tool need to be installed to build with Maven.";
      echo >&2 "       Download it and confirm that \"mvn --version\" runs.";
      exit 1;
    }
    echo_header "Starting local Maven build..."
    mvn clean install -P openshift
}

function verify_build_and_deployments() {
  echo_header "Verifying build and deployments"

  # verify builds
  local _BUILDS_FAILED=false
  for buildconfig in optaplanner-employee-rostering
  do
    if [ -n "$(oc get builds -n $PRJ | grep $buildconfig | grep Failed)" ] && [ -z "$(oc get builds -n $PRJ | grep $buildconfig | grep Complete)" ]; then
      _BUILDS_FAILED=true
      echo "WARNING: Build $project/$buildconfig has failed..."
    fi
  done

  # verify deployments
  for project in $PRJ
  do
    local _DC=
    for dc in $(oc get dc -n $project -o=custom-columns=:.metadata.name,:.status.replicas); do
      if [ $dc = 0 ] && [ -z "$(oc get pods -n $project | grep "$dc-[0-9]\+-deploy" | grep Running)" ] ; then
        echo "WARNING: Deployment $project/$_DC in project $project is not complete..."
      fi
      _DC=$dc
    done
  done
}

function make_idle() {
  echo_header "Idling Services"
  oc idle -n $PRJ_CI --all
  oc idle -n $PRJ_TRAVEL_AGENCY_PROD --all
}

# GPTE convention
function set_default_project() {
  if [ $LOGGEDIN_USER == 'system:admin' ] ; then
    oc project default >/dev/null
  fi
}

################################################################################
# Main deployment                                                              #
################################################################################

if [ "$LOGGEDIN_USER" == 'system:admin' ] && [ -z "$ARG_USERNAME" ] ; then
  # for verify and delete, --project-suffix is enough
  if [ "$ARG_COMMAND" == "delete" ] || [ "$ARG_COMMAND" == "verify" ] && [ -z "$ARG_PROJECT_SUFFIX" ]; then
    echo "--user or --project-suffix must be provided when running $ARG_COMMAND as 'system:admin'"
    exit 255
  # deploy command
  elif [ "$ARG_COMMAND" != "delete" ] && [ "$ARG_COMMAND" != "verify" ] ; then
    echo "--user must be provided when running $ARG_COMMAND as 'system:admin'"
    exit 255
  fi
fi

#pushd ~ >/dev/null
START=`date +%s`

echo_header "$PRJ_DISPLAY_NAME ($(date))"

case "$ARG_COMMAND" in
    delete)
        echo "Delete $PRJ_DISPLAY_NAME ($ARG_APP)..."
        oc delete project $PRJ
        ;;

    verify)
        echo "Verifying $PRJ_DISPLAY_NAME ($ARG_APP)..."
        print_info
        verify_build_and_deployments
        ;;

    idle)
        echo "Idling $PRJ_DISPLAY_NAME ($ARG_APP)..."
        print_info
        make_idle
        ;;

    setup)
        echo "Setting up and deploying $PRJ_DISPLAY_NAME ($ARG_APP)..."

        print_info
        create_projects

        if [ "$ARG_BINARY_BUILD" = true ] ; then
            create_application_binary
            build_and_deploy_binary
        else
            create_application
            build_and_deploy
        fi

        if [ "$ARG_RUN_VERIFY" = true ] ; then
          echo "Waiting for deployments to finish..."
          sleep 30
          verify_build_and_deployments
        fi
        ;;

    deploy)
        echo "Deploying $PRJ_DISPLAY_NAME ($ARG_APP)..."

        print_info

        if [ "$ARG_BINARY_BUILD" = true ] ; then
            build_and_deploy_binary
        else
            build_and_deploy
        fi

        if [ "$ARG_RUN_VERIFY" = true ] ; then
          echo "Waiting for deployments to finish..."
          sleep 30
          verify_build_and_deployments
        fi
        ;;

    *)
        echo "Invalid command specified: '$ARG_COMMAND'"
        usage
        ;;
esac

set_default_project
#popd >/dev/null

END=`date +%s`
echo
echo "Provisioning done! (Completed in $(( ($END - $START)/60 )) min $(( ($END - $START)%60 )) sec)"
