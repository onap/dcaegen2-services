###############################################################################
#  ============LICENSE_START=======================================================
#  ml-prediction-ms
#  ================================================================================
#   Copyright (C) 2022 Wipro Limited
#   ==============================================================================
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.
#  ============LICENSE_END=========================================================
#
###############################################################################
set -ex

echo "running script: [$0] for module [$1] at stage [$2]"

MVN_PROJECT_MODULEID="$1"
MVN_PHASE="$2"
PROJECT_ROOT=$(dirname $0)


echo "MVN_RELEASE_TAG is set to [$MVN_RELEASE_TAG]"
RELEASE_TAG=${MVN_RELEASE_TAG:-R6}
if [[ "$RELEASE_TAG" != "R1" ]]; then
  RELEASE_TAGGED_DIR="${RELEASE_TAG}/"
else
  RELEASE_TAGGED_DIR=""
fi


if [[ ! -f "${PROJECT_ROOT}/mvn-phase-lib.sh" ]]; then
    if ! wget -O ${PROJECT_ROOT}/mvn-phase-lib.sh \
    "$MVN_RAWREPO_BASEURL_DOWNLOAD"/org.onap.dcaegen2.utils/${RELEASE_TAGGED_DIR}scripts/mvn-phase-lib.sh; then
      echo "Fail to download mvn-phase-lib.sh"
      exit 1
    fi
fi

source ./mvn-phase-lib.sh

# Customize the section below for each project
case ${MVN_PHASE} in
clean)
  echo "==> clean phase script"
  clean_templated_files
  clean_tox_files
  rm -rf ./venv-* ./*.wgn
  ;;
generate-sources)
  echo "==> generate-sources phase script"
  ;;
compile)
  echo "==> compile phase script"
  ;;
test)
  echo "==> test phase script"
  run_tox_test
  ;;
package)
  echo "==> package phase script"
  ;;
install)
  echo "==> install phase script"
  ;;
deploy)
  echo "==> deploy phase script"
  # below segments are example of how to deploy various artifacts
  # copy the ones suitable for your repo

  # build docker image from Docker file (under root of repo) and push to registry
  build_and_push_docker
  ;;
*)
  echo "==> unprocessed phase"
  ;;
esac

