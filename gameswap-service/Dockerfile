FROM openjdk:8-jre

COPY wait-for-it.sh /opt/wait-for-it.sh
COPY docker-run-service.sh /opt/docker-run-service.sh
COPY target/gameswap-service-0.0.1-SNAPSHOT.jar /opt/gameswap-service.jar
COPY gameswap.yml /opt/gameswap.yml

CMD '/opt/docker-run-service.sh'
