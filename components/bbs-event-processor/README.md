# BBS use case event processor

---

## Overview

BBE-ep is responsible for handling two types of events for the BBS use case. 

First are PNF re-registration internal events published by PRH (in **unauthenticated.PNF_UPDATE** DMaaP topic). 
BBS-ep must process these internal events to understand if they actually constitute ONT(CPE) relocation events. 
In the relocation case, it publishes an event towards **unauthenticated.DCAE_CL_OUTPUT** DMaaP topic to trigger 
further Policy actions related to BBS use case.

Second type of events are CPE authentication events originally published by the Edge SDN M&C component of BBS 
use case architecture. Through RestConf-Collector or VES-Collector, these events are consumed by BBS-ep 
(in **unauthenticated.CPE_AUTHENTICATION** DMaaP topic) and they are forwarded towards **unauthenticated.DCAE_CL_OUTPUT** 
DMaaP topic to trigger further Policy actions related to BBS use case.

BBE-ep periodically polls for the two events. Polling interval is configurable and can be changed dynamically from Consul.
Its implementation is based on Reactive Streams (Reactor library), so it is fully asynchronous and non-blocking.

## Installation and Removal

BBS-ep is delivered as a Spring-Boot application ready to be deployed in Docker (via docker-compose). 

BBS-ep can be dynamically deployed in DCAE’s Cloudify environment via its blueprint deployment artifact.

- Blueprint can be found in

    https://git.onap.org/dcaegen2/platform/blueprints/tree/blueprints/k8s-bbs-event-processor.yaml
    
Steps to deploy are shown below

- Enter the Bootstrap POD
- Validate blueprint
    cfy blueprints validate /blueprints/k8s-bbs-event-processor.yaml
- Upload validated blueprint
    cfy blueprints upload -b bbs-ep /blueprints/k8s-bbs-event-processor.yaml
- Create deployment
    cfy deployments create -b bbs-ep -i /blueprints/k8s-bbs-event-processor.yaml bbs-ep
- Deploy blueprint
    cfy executions start -d bbs-ep install

To un-deploy BBS-ep, steps are shown below

- Validate blueprint by running command
    cfy uninstall bbs-ep
- Validate blueprint by running command
    cfy blueprints delete bbs-ep
 
## Functionality

For more details about the exact flows and where BBS-EP fits in the overall BBS flows, visit [use case official documentation](https://wiki.onap.org/display/DW/BBS+Notifications)

## Compiling BBS-ep

BBS-ep is a sub-project of dcaegen2/services (inside components directory).
To build just the BBS-ep component, run the following maven command from within **components/bbs-event-processor** directory

`mvn clean install`   

## Main API Endpoints

Running with dev-mode of BBS-ep

  - **Heartbeat**
    - GET http://<container_address>:8100/heartbeat
  - **Start Polling for events** 
    - POST http://<container_address>:8100/start-tasks
  - **Stop Polling for events** 
    - POST http://<container_address>:8100/cancel-tasks
  - **Execute just one polling for PNF re-registration internal events**
    - POST http://<container_address>:8100/poll-reregistration-events
  - **Execute just one polling for CPE authentication events** 
    - POST http://<container_address>:8100/poll-cpe-authentication-events
  - **Change application logging level** 
    - POST http://<container_address>:8100/logging/{level}