#!/bin/sh

echo "start init db ..."

/bin/run-parts /home/datalake/db_init

echo "finish init db"

cmd=`find . -regex  '\./feeder-[0-9]+\.[0-9]+\.[0-9]+[-SNAPSHOT]+\.jar'`
cmd1= `find . -regex '\./feeder-[0-9]+\.[0-9]+\.[0-9]+\.jar'`
if [ -n "$cmd" ]; then
    java -jar $cmd
elif [ -n "$cmd1" ]; then
    java -jar $cmd
else
    echo"STRING is empty"
    sleep 10000
fi
