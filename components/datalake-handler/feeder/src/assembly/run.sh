#!/bin/sh

echo "start init db ..."

/bin/run-parts /datalake/db_init

echo "finish init db"

cmd=`find . -name "*.jar" | grep -E '.*(feeder)-([0-9]+\.[0-9]+\.[0-9]+)(-SNAPSHOT)(-exec\.jar)$'`
cmd1=`find . -name "*.jar" | grep -E '.*(feeder)-([0-9]+\.[0-9]+\.[0-9]+)(-exec.jar)$'`

if [ -n "$cmd" ]; then
    java -jar $cmd
elif [ -n "$cmd1" ]; then
    java -jar $cmd1
fi
