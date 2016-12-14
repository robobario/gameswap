/opt/wait-for-it.sh rabbitmq:5672
java $JAVA_OPTS -jar /opt/gameswap-worker.jar /opt/gameswap.json
