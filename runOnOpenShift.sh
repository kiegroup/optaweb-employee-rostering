#!/usr/bin/env bash
set -e

readonly dir_frontend=optaweb-employee-rostering-frontend
readonly dir_backend=optaweb-employee-rostering-backend
readonly dir_standalone=optaweb-employee-rostering-standalone

# Change dir to the project root (where the script is located) to correctly resolve module paths.
# This is needed in case the script was called from a different location than the project root.
cd "$(dirname "$(readlink -f "$0")")"

# Fail fast if the project hasn't been built
if [[ ! -d ${dir_frontend}/docker/build ]]
then
  echo >&2 "ERROR: Frontend not built! Build the project before running this script."
  exit 1
fi


if ! stat -t ${dir_backend}/target/quarkus-app/quarkus-run.jar > /dev/null 2>&1
then
  echo >&2 "ERROR: Backend not built! Build the project before running this script."
  exit 1
fi

command -v oc > /dev/null 2>&1 || {
  echo >&2 "ERROR: The oc client tool needs to be installed to connect to OpenShift."
  exit 1
}

[[ -x $(command -v oc) ]] || {
  echo >&2 "ERROR: The oc client tool is not executable. Please make it executable by running \
‘chmod u+x \$(command -v oc)’."
  exit 1
}

# Print info about the current user and project
echo "Current user: $(oc whoami)"
# Check that the current user has at least one project
[[ -z "$(oc projects -q)" ]] && {
  echo >&2 "You have no projects. Use ‘oc new-project <project-name>’ to create one."
  exit 1
}
# Display info about the current project
oc project

# Check that the current project is empty
get_all=$(oc get all -o name)
if [[ -z "$get_all" ]]
then
  echo "The project appears to be empty."
else
  echo >&2
  echo >&2 "Project content:"
  echo >&2
  echo >&2 "$get_all"
  echo >&2
  echo >&2 "ERROR: The project is not empty."
  exit 1
fi

declare -l answer_continue # -l converts the value to lower case before it's assigned
read -r -p "Do you want to continue? [y/N]: " "answer_continue"
[[ "$answer_continue" == "y" ]] || {
  echo "Aborted."
  exit 0
}

# Build custom standalone that works with Postgres
cd ${dir_standalone}
mvn clean install -DskipTests -Dquarkus.profile=postgres
if [[ "$?" -ne 0 ]] ; then
  echo "Standalone Postgres build failed; check the logs above."
  exit 1
fi

# Set up PostgreSQL
oc new-app --name postgresql postgresql-persistent

# Standalone
# -- binary build (upload local artifacts + Dockerfile)
oc new-build --name standalone --strategy=docker --binary
oc start-build standalone --from-dir=. --follow
# -- new app
oc new-app standalone
# -- use PostgreSQL secret
oc set env deployment/standalone --from=secret/postgresql
oc expose service/standalone

echo
echo "You can access the application at http://$(oc get route standalone -o custom-columns=:spec.host | tr -d '\n') \
once the deployment is done."
