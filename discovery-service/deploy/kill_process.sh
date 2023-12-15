#!/bin/bash
cd /opt/discovery-service


sudo docker stop discovery-service
sudo docker rm discovery-service
sudo docker image rm discovery-service