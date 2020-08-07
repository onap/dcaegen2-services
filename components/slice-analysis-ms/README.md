SLICE-ANALYSIS-MS 

### Build Instructions

This project is organized as a mvn project and is a sub-project of dcaegen2/services (inside components directory). The build generate a jar and package into docker container. 

```
git clone https://gerrit.onap.org/r/dcaegen2/services
To build slice-analysis-ms run `mvn clean install` from **components/slice-analysis-ms** directory
To build docker image run `mvn clean install docker:build`
```


### Environment variables in Docker Container


Variables coming from deployment system:

- APP_NAME - slice-analysis-ms application name that will be registered with consul
- CONSUL_PROTOCOL - Consul protocol by default set to **https**, if it is need to change it then that can be set to different value 
- CONSUL_HOST - used with conjunction with CBSPOLLTIMER, should be a host address (without port! e.g my-ip-or-host) where Consul service lies
- CBS_PROTOCOL - Config Binding Service protocol by default set to **https**, if it is need to change it then that can be set to different value
- CONFIG_BINDING_SERVICE - used with conjunction with CBSPOLLTIMER, should be a name of CBS as it is registered in Consul
- HOSTNAME - used with conjunction with CBSPOLLTIMER, should be a name of slice-analysis-ms application as it is registered in CBS catalog


### Deployment


### Standalone deployment
Slice analysis ms can be deployed standalone using docker-compose.

Navigate to src/main/docker directory. docker-compose.yaml can be found there.

To install :
    docker-compose up

To uninstall :
    docker-compose down
