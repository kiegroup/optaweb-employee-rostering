#!/usr/bin/env bash

# Abort the script if any simple command outside an if, while, &&, ||, etc. exits with a non-zero status.
set -e

function confirm() {
  declare answer
  read -r -p "$1 [y/N]: " answer
  [[ "$answer" == "y" ]]
}

function abort() {
  echo "Aborted."
  exit 0
}

function standalone_jar_or_maven() {
  local -r standalone=optaweb-employee-rostering-standalone

  # BEGIN: Distribution use case
  #
  # We're running a copy of the script in the project root that has been moved to distribution's bin directory during
  # distribution assembly. The only difference is that the standalone JAR is in the same directory as the script (bin)
  # and project.version is set using resource filtering during assembly.

  # shellcheck disable=SC2154 #(project.version variable is not declared)
  if [[ ! -f pom.xml && -f ${standalone}-${project.version}/quarkus-run.jar ]]
  then
    readonly jar=${standalone}-${project.version}/quarkus-run.jar
    return 0
  fi
  # END: Distribution use case

  readonly jar=${standalone}/target/quarkus-app/quarkus-run.jar

  if [[ ! -f ${jar} ]]
  then
    confirm "Jarfile '$jar' does not exist. Run Maven build now?" || abort
    if ! mvn clean install -DskipTests
    then
      echo >&2 "Maven build failed. Aborting the script."
      exit 1
    fi
  fi
}

function run_optaweb() {
  java "$@" -jar "$jar"
}

# Change dir to the project root (where the script is located).
cd -P "$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"

standalone_jar_or_maven
run_optaweb "$@"
