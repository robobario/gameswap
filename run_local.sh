#!/bin/sh
./build.sh -DskipTests -T1.0C $@
docker-compose build 
docker-compose up
