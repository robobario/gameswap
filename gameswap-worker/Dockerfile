FROM openjdk:8-jre

COPY wait-for-it.sh /opt/wait-for-it.sh
COPY docker-run-worker.sh /opt/docker-run-worker.sh
COPY target/gameswap-worker-0.0.1-SNAPSHOT.jar /opt/gameswap-worker.jar
COPY gameswap.json /opt/gameswap.json

CMD '/opt/docker-run-worker.sh'
