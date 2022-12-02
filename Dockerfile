FROM eclipse-temurin:18-jre-focal

WORKDIR /home/wf
COPY ./build/libs/WarpFleetSynchronizer-all.jar /home/wf/.
EXPOSE 8080
CMD java -jar /home/wf/WarpFleetSynchronizer-all.jar /data/conf.json
