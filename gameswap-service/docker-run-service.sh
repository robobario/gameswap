/opt/wait-for-it.sh postgres:5432
/opt/wait-for-it.sh rabbitmq:5672
java $JAVA_OPTS -jar /opt/gameswap-service.jar db test /opt/gameswap.yml && java $JAVA_OPTS -Ddw.redirectAllToHttps=false -jar /opt/gameswap-service.jar server /opt/gameswap.yml
