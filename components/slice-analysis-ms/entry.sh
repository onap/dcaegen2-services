#!/bin/bash
export trustpass=`cat /opt/app/sliceanalysisms/etc/cert/trust.pass`
java -Djavax.net.ssl.trustStore=/opt/app/sliceanalysisms/etc/cert/trust.jks -Djavax.net.ssl.trustStorePassword=$trustpass -jar /bin/application.jar