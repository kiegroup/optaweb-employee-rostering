#!/usr/bin/env bash

script_dir="$(dirname "$(readlink -f "$0")")"

java -jar ${script_dir}/optaweb-employee-rostering-standalone-${project.version}-exec.jar \
--server.address=localhost \
--server.port=8080
