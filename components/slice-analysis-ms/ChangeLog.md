# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.1.4] - 2022/09/16
         - [DCAEGEN2-3242](https://jira.onap.org/browse/DCAEGEN2-3242) - Fix bug in ConfigFectchFromCbs that fetch will get hung when policy config in pdp engine is empty

## [1.1.3] - 2022/05/11
         - [DCAEGEN2-3156](https://jira.onap.org/browse/DCAEGEN2-3156) - Fix bug in fetching PM data from DES

## [1.1.2] - 2022/05/01

         - [DCAEGEN2-3145](https://jira.onap.org/browse/DCAEGEN2-3145) - Filter RAN related service instances in AAI

         - [DCAEGEN2-3146](https://jira.onap.org/browse/DCAEGEN2-3146) - Fetch CU Cells instead of DU cells data for ML message processing

         - [DCAEGEN2-3147](https://jira.onap.org/browse/DCAEGEN2-3147) - Fix the message content that SLICEANALYSISMS sends to Policy for CCVPN bw adjustment and suppress exceptions and error in job builder

         - [DCAEGEN2-3143](https://jira.onap.org/browse/DCAEGEN2-3143) - Suppress exceptions and ERRORS in slice-analysis-ms job building

## [1.1.1] - 2022/04/12
         - [DCAEGEN2-3142](https://jira.onap.org/browse/DCAEGEN2-3142) - Filter data from AAI to avoid possible exceptions, remove null parameters in policy payload and add logs

         - [DCAEGEN2-3141](https://jira.onap.org/browse/DCAEGEN2-3141) - Bugfix in DCAE-SliceAnalysisMs for IBN user-triggered CCVPN closed-loop

## [1.1.0] - 2022/3/10
         - [DCAEGEN2-3063](https://jira.onap.org/browse/DCAEGEN2-3063) - IBN user-triggered CLoud Leased Line update and CCVPN closed-loop

## [1.0.7] - 2021/12/16
         - [DCAEGEN2-2963](https://jira.onap.org/browse/DCAEGEN2-2963) - Use onap/integration-java11 image

         - [DCAEGEN2-2966](https://jira.onap.org/browse/DCAEGEN2-2966) - Switch CBS client library to 1.8.7

         - [DCAEGEN2-3025](https://jira.onap.org/browse/DCAEGEN2-3025) - Fix null pointer exception while fetching slice-config

         - [DCAEGEN2-3054](https://jira.onap.org/browse/DCAEGEN2-3054) - Remove security vulnerabilities

         - [DCAEGEN2-2942](https://jira.onap.org/browse/DCAEGEN2-2942) - Calculate slice utilization data

         - [DCAEGEN2-3034](https://jira.onap.org/browse/DCAEGEN2-3034) - Migrate SliceAnalysis MS to use unauthenticated topic

         - [DCAEGEN2-3100](https://jira.onap.org/browse/DCAEGEN2-3100) - Update onset message test data

## [1.0.6] - 2021/08/28
         - [DCAEGEN2-2885](https://jira.onap.org/browse/DCAEGEN2-2885) - DCAE SliceAnalysis MS - CPS Integration

         - [DCAEGEN2-2811](https://jira.onap.org/browse/DCAEGEN2-2811) - Remove security vulnerabilities
