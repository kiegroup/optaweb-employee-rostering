#!/usr/bin/env bash
set -e

readonly dir_backend=employee-rostering-backend
readonly dir_frontend=employee-rostering-frontend

# Change dir to the project root (where provision.sh is located) to correctly resolve module paths.
# This needed in case the script was called from a different location than the project root.
cd "$(dirname "$(readlink -f "$0")")"

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
oc project

if oc status | grep "You have no services" > /dev/null
then
  echo "The project appears to be empty."
else
  echo >&2 "ERROR: The project is not empty."
  exit 1
fi

declare -l answer_continue # -l converts the value to lower case before it's assigned
read -r -p "Do you want to continue? [y/N]: " "answer_continue"
[[ "$answer_continue" == "y" ]] || {
  echo "Aborted."
  exit 0
}

get_all=$(oc get all -o name)
[[ "x$get_all" == "x" ]] || {
  echo >&2 "Project not empty:"
  echo >&2 "${get_all}"
  exit 1
}

# Set up PostgreSQL
oc new-app postgresql-persistent

# Backend
# -- binary build (upload local artifacts + Dockerfile)
oc new-build --name backend --strategy=docker --binary
oc start-build backend --from-dir=${dir_backend} --follow
# -- new app
oc new-app backend
# -- use PostgreSQL secret
oc set env dc/backend --from=secret/postgresql

# Frontend
# -- binary build
oc new-build --name frontend --strategy=docker --binary -e BACKEND_URL=http://backend:8080
oc start-build frontend --from-dir=${dir_frontend}/docker --follow
# -- new app
oc new-app frontend
# -- expose the service
oc expose svc/frontend
# -- change target port to 8080
oc patch route frontend -p '{"spec":{"port":{"targetPort":"8080-tcp"}}}'

echo "You can access the application at http://$(oc get route frontend -o custom-columns=:spec.host | tr -d '\n') \
once the deployment is done."
