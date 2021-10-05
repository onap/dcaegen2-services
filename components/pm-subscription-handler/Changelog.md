# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [2.0.0]
### Changed
* Updated PMSH app configuration, simplified existing config (DCAEGEN2-2814)

## [1.3.2]
### Changed
* Update to use version 2.2.1 of https://pypi.org/project/onap_dcae_cbs_docker_client/
* Updated DB under Enhanced API for PMSH subscription management Feature (DCAEGEN2-2802) 

## [1.3.1]
### Changed
* Updated Subscription object retrieving key from App Config data (DCAEGEN2-2713)

## [1.3.0]
### Changed
* Change pmsh baseOS img to integration (DCAEGEN2-2420)

## [1.2.0]
### Changed
* Bug fix prevent sub threads from crashing permanently (DCAEGEN2-2501)
* Added Resource Name (model-name) to filter (DCAEGEN2-2402)
* Added retry mechanism for DELETE_FAILED subscriptions on given NFs (DCAEGEN2-2152)
* Added func to update the subscription object on ACTIVATE/UNLOCK (DCAEGEN2-2152)
* Added validation for schema of PMSH monitoring policy (DCAEGEN2-2152)

## [1.1.2]
### Changed
* Bug fix for missing sdnc params in DELETE event (DCAEGEN2-2483)
* Fix to add IP to event sent to Policy framework (DCAEGEN2-2486)

## [1.1.1]
### Changed
* Moved to alpine base image (DCAEGEN2-2292)
* Added model-invariant-id and model-version-id to filter (DCAEGEN2-2151)
* Added support for multiple CDS blueprints (DCAEGEN2-2405) 

## [1.1.0]
### Changed
* Added new API endpoint to fetch all Subscription data (DCAEGEN2-2154)
* Added support for config-binding-docker module in PMSH config fetch (DCAEGEN2-2156)
* Replaced logging implementation with onappylog module (DCAEGEN2-2155)
* Added support for TLS enable/disable switch via blueprint inputs (DCEAGEN2-2146)

## [1.0.3]
### Fixed
* Fixed bug where PMSH pushes subscription to xnf regardless of it's orchestration status (DCAEGEN2-2173)
* Fixed bug where undeploying PMSH would not deactivate newly added pnfs (DCAEGEN2-2175)
* Fixed bug to prevent aai_event handler from incorrectly LOCKING the subscription (DCAEGEN2-2181)

## [1.0.2]
### Changed
* Moved subscription processing from main into its own subscription_handler module
* Removed policy response handling functions from pmsh_utils and introduced policy_response_handler
* Network function filter now resides in network_function instead of subscription
* Added graceful handling upon receiving SIGTERM signal

## [1.0.1]
### Fixed
* Fixed Deletion of Network Function 

## [1.0.0]

* Initial release of the PM Subscription Handler.
