#!/bin/sh

echo "start init db ..."

/bin/run-parts /home/datalake/db_init

echo "finish init db"

cmd=`ls feeder-*.jar`
if [ -z "$cmd" ]; then
    echo "STRING is empty"
    sleep 10000
else
    java -jar $cmd
fi
