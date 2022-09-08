#!/bin/sh

echo "start init db ..."

export PGPASSWORD=$PG_PASSWORD

sh db_init/11_create-database
sh db_init/20_db-initdb

echo "finish init db"

cmd=`find . -name "*.jar" | grep -E '.*(feeder)-([0-9]+\.[0-9]+\.[0-9]+)(-SNAPSHOT)(-exec\.jar)$'`
cmd1=`find . -name "*.jar" | grep -E '.*(feeder)-([0-9]+\.[0-9]+\.[0-9]+)(-exec.jar)$'`

if [ -n "$cmd" ]; then
    java -jar $cmd
elif [ -n "$cmd1" ]; then
    java -jar $cmd1
else
    echo "STRING is empty"
    sleep 10000
fi
