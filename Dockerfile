FROM adoptopenjdk:8-jdk-hotspot-bionic AS builder
WORKDIR /home/wf
COPY ./build/libs/WarpFleetSynchronizer-all.jar /home/wf/.
EXPOSE 8080
CMD java -jar /home/wf/WarpFleetSynchronizer-all.jar /data/conf.json
