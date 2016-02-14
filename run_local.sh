#!/bin/sh

mvn exec:exec -pl gameswap-service & mvn exec:exec -pl gameswap-worker && fg
