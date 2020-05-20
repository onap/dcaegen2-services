# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.1.0]
### Changed
* Added new API endpoint to fetch all Subscription data (DCAEGEN2-2154)
* Added support for config-binding-docker module in PMSH config fetch (DCAEGEN2-2156)

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
