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

NAMESPACE='stage01-http2rpc'

out "Will deploy to staging ${NAMESPACE}"

DEPLOY_START=`date +%s`

# for deploy dockers
mkdir /root/.docker
echo $DOCKER_AUTH_CONFIG > /root/.docker/config.json

# for helm & kubectl
mkdir /root/.kube
echo $K8S_KUBECTL_CONFIG_BASE64 | base64 -d > /root/.kube/config

# GIT_READ_TOKEN - это токен для аутентификации в bitbucket
# https://bitbucket.org/account/user/nbakaev_stm/app-passwords
git clone https://${GIT_READ_TOKEN}@bitbucket.org/stm-kkt/devops-components-charts.git

cd devops-components-charts

  if [ `git rev-parse --verify -q "origin/${DRONE_BRANCH}"` ]; then
    out "Checking out ${DRONE_BRANCH}"
    git checkout -f "origin/${DRONE_BRANCH}"
  else
    out "Checking out master"
    git checkout -f origin/master
  fi

cd ../

RELEASE_PLATFORM_NAME_DMZ=http2rpc-dmz
RELEASE_PLATFORM_NAME_INSIDE=http2rpc-inside
RELEASE_COMPONENT_NAME=comp-stage01-dr

REDIS_SENTINEL_PORT=22001

helm upgrade -i --namespace ${NAMESPACE} \
 --atomic \
 --set "image.build=$DRONE_BUILD_NUMBER" \
 --set "ingress.hosts[0].host=http2rpc-dmz.k8s.stm.local" \
 --set "ingress.hosts[0].paths[0].path='/'" \
 --set 'ingress.enabled=true' \
 --set "stm.rpcDmzRedisSentinelEnable=true" \
 --set "stm.systemCode=stage01-dr-restadapter" \
 --set "stm.rpcDmzRedisNodes=niff.stm.local:$REDIS_SENTINEL_PORT\,villi.stm.local:$REDIS_SENTINEL_PORT\,dilli.stm.local:$REDIS_SENTINEL_PORT" \
 --set "stm.rpcDmzKafka=$RELEASE_COMPONENT_NAME-cp-kafka.stage01-dr:9092" \
 --set 'stm.useJsonLogger=false' \
 --wait \
 $RELEASE_PLATFORM_NAME_DMZ \
 ./devops-components-charts/http2rpc/dmz

helm upgrade -i --namespace ${NAMESPACE} \
 --atomic \
 --set "image.build=$DRONE_BUILD_NUMBER" \
 --set "ingress.hosts[0].host=http2rpc-inside.k8s.stm.local" \
 --set "ingress.hosts[0].paths[0].path='/'" \
 --set 'ingress.enabled=true' \
 --set "stm.rpcDmzRedisSentinelEnable=true" \
 --set "stm.systemCode=stage01-dr-restadapter" \
 --set "stm.rpcDmzRedisNodes=niff.stm.local:$REDIS_SENTINEL_PORT\,villi.stm.local:$REDIS_SENTINEL_PORT\,dilli.stm.local:$REDIS_SENTINEL_PORT" \
 --set "stm.rpcDmzKafka=$RELEASE_COMPONENT_NAME-cp-kafka.stage01-dr:9092" \
 --set 'stm.useJsonLogger=false' \
 --wait \
 $RELEASE_PLATFORM_NAME_INSIDE \
 ./devops-components-charts/http2rpc/inside

  status=0
  echo -e "\n"

  RED='\033[0;31m'
  GREEN='\033[0;32m'

  if [[ "${status}" == 0 ]];then

    # below is only cleanup
    # ignore errors - we successfully deployed dockers
    set +e

    echo -e "${GREEN}***********************"
    echo -e "${GREEN}[CI]: Success Build ${DRONE_BUILD_NUMBER}"
    echo -e "${GREEN}***********************"
  else
    echo -e "${RED}xxxxxxxxxxxxxxxxxxxxxx"
    echo -e "${RED}[CI]: Failed"
    echo -e "${RED}xxxxxxxxxxxxxxxxxxxxxx"
  fi

  echo -e "${COLOR_OFF}"

exit $status
