#!/bin/bash
cd /opt/gateway-service

sudo docker stop gateway-service
sudo docker rm gateway-service
sudo docker image rm gateway-service