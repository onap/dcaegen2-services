#!/bin/sh

echo "start init db ..."

export PGPASSWORD=$PG_PASSWORD

sh db_init/11_create-table
sh db_init/20_db-initdb

echo "finish init db"

cmd=`find . -regex  '\./feeder-[0-9]+\.[0-9]+\.[0-9]+[-SNAPSHOT]+\-exec.jar'`
cmd1=`find . -regex '\./feeder-[0-9]+\.[0-9]+\.[0-9]+\-exec.jar'`
cmd=`find . -name feeder*-exec.jar`
if [ -n "$cmd" ]; then
    java -jar $cmd
elif [ -n "$cmd1" ]; then
    java -jar $cmd1
else
    echo "STRING is empty"
    sleep 10000
fi
