#!/usr/bin/env bash

COLOR_OFF='\033[0m'

function out {
  COLOR='\033[0;35m'

  echo -e "${COLOR}[CI]: $1${COLOR_OFF}"
}

set -e

# идем вверх тк .drone.yml в папке devops
cd ../

HOME_FOLDER=`pwd`

MVN_PARAMS='-s /root/maven_settings.xml -B -Dusn.nexus.baseurl=http://nexus.k8s-usn.stm.local'
export NAMESPACE=cibuild-proxy-${DRONE_BUILD_NUMBER}
export MAVEN_HOME=`pwd`/.m2
export M2_HOME_PATH=$MAVEN_HOME
export MVN_REPO_PATH=http://nexus.k8s-usn.stm.local/repository/maven-snapshots/

echo "use M2_HOME_PATH=${M2_HOME_PATH}"

mvnp() {
    /usr/bin/mvn ${MVN_PARAMS} "$@"
}
export -f mvnp

function getDockerRepository {
    mvnp org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=docker.image.prefix 2>/dev/null | grep -Ev '(^\[|Download\w+:)'
}

function getDockerArtifactName {
    mvnp org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=docker.image.name 2>/dev/null | grep -Ev '(^\[|Download\w+:)'
}

function getArtifactFinalName {
    mvnp org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.build.finalName 2>/dev/null | grep -Ev '(^\[|Download\w+:)'
}

out "Create namespace for build ${NAMESPACE}"

DEPLOY_START=`date +%s`

# for deploy dockers
mkdir /root/.docker
echo $DOCKER_AUTH_CONFIG > /root/.docker/config.json

# for helm & kubectl
mkdir /root/.kube
echo $K8S_KUBECTL_CONFIG_BASE64 | base64 -d > /root/.kube/config

cd /
echo "Delete all ru.stm mvn cache"
set +e
rm -rf $MAVEN_HOME/ru/stm
set -e

cd $HOME_FOLDER

mvnp install

function buildSpringBoot {
  mvnp package -DskipTests=true -Dmaven.source.skip=true -Ddrone.build.number=${DRONE_BUILD_NUMBER} -Ddrone.build.created=${DRONE_BUILD_CREATED} -Ddrone.commit=${DRONE_COMMIT}

  DOCKER_IMAGE=`getDockerRepository``getDockerArtifactName`
  DOCKER_JOB_IMAGE_TAG=${DOCKER_IMAGE}:${DRONE_BUILD_NUMBER}
  echo "TAG: ${DOCKER_JOB_IMAGE_TAG}"

  echo "Tag pipeline job tag"
  docker build -t $DOCKER_JOB_IMAGE_TAG --build-arg JAR_FILE=target/`getArtifactFinalName`-exec.jar .
  docker push $DOCKER_JOB_IMAGE_TAG
}

function analyseSonarQube {
  mvnp sonar:sonar -Dsonar.projectKey=http2rpc -Dsonar.host.url=http://192.168.10.43:32324 -Dsonar.login=071f6df5a689c9e8f8f4036809ef69bc2331ef63 -Dsonar.projectVersion=${DRONE_BUILD_NUMBER} ##
}

 # tests were above - just upload dockerfile
 echo "Building and uploading idmz"
 cd $HOME_FOLDER/idmz/idmz && buildSpringBoot

 echo "Building and uploading inside"
 cd $HOME_FOLDER/inside/inside && buildSpringBoot

 echo "Start analyse SonarQube"
 cd $HOME_FOLDER &&  analyseSonarQube

status=0
echo -e "\n"

RED='\033[0;31m'
GREEN='\033[0;32m'

if [[ "${status}" == 0 ]];then

  # below is only cleanup
  # ignore errors - we successfully deployed dockers
  set +e

  echo -e "${GREEN}****************************"
  echo -e "${GREEN}[CI]: Success Build ${DRONE_BUILD_NUMBER}"
  echo -e "${GREEN}****************************"
else
  echo -e "${RED}xxxxxxxxxxxxxxxxxxxxxx"
  echo -e "${RED}[CI]: Failed"
  echo -e "${RED}xxxxxxxxxxxxxxxxxxxxxx"
fi

echo -e "${COLOR_OFF}"

exit $status
