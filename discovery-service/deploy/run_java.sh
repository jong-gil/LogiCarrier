#!/bin/bash
cd /opt/discovery-service

sudo docker build -t discovery-service .
sudo docker run -d --name discovery-service -p 8761:8761 --network logicarrier-network discovery-service
echo "successfully run!"