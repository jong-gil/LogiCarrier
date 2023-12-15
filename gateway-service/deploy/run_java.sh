#!/bin/bash
cd /opt/gateway-service

sudo docker stop gateway-service
sudo docker rm gateway-service
sudo docker image rm gateway-service
sudo docker build -t gateway-service .
sudo docker run -d --name gateway-service -p 8080:8080 --network logicarrier-network -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" gateway-service
echo "successfully run!"