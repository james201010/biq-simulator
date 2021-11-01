#!/bin/bash

java -DappdBiqSimConf=./conf/biq-sim-config.yaml -DappdBiqSimAction=delete -DappdBiqSimSchemaName=my_schema_2_delete -jar ./AD-BiQ-Simulator.jar
