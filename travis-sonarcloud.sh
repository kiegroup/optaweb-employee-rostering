#!/usr/bin/env bash
JACOCO=org.jacoco:jacoco-maven-plugin:0.7.5.201505241946
if [ "$TRAVIS_EVENT_TYPE" == "pull_request" ]
then
  mvn --quiet $JACOCO:report $JACOCO:merge sonar:sonar -Preport-code-coverage -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=kiegroup -Dsonar.login=$SONARCLOUD_TOKEN -Dsonar.projectKey=optaweb-employee-rostering -Dsonar.pullrequest.base=$TRAVIS_BRANCH -Dsonar.pullrequest.branch=$TRAVIS_PULL_REQUEST_BRANCH -Dsonar.pullrequest.key=$TRAVIS_PULL_REQUEST -Dsonar.pullrequest.provider=GitHub -Dsonar.pullrequest.github.repository=$TRAVIS_PULL_REQUEST_SLUG
else
  mvn --quiet $JACOCO:report $JACOCO:merge sonar:sonar -Preport-code-coverage -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=kiegroup -Dsonar.login=$SONARCLOUD_TOKEN -Dsonar.projectKey=optaweb-employee-rostering
fi
