#!/bin/bash

nohup java -DappdBiqSimConf=./conf/biq-sim-config.yaml -DappdBiqSimAction=publish -jar ./AD-BiQ-Simulator.jar &
