FROM adoptopenjdk:8-jdk-hotspot-bionic AS builder
WORKDIR /home/wf
COPY ./build/libs/WarpFleetSynchronizer-all.jar .
EXPOSE 8080
CMD java -jar WarpFleetSynchronizer.jar /data/conf.json
