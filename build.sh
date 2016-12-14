mkdir -p ./.m2
docker build ./docker --file ./docker/Dockerfile.maven -t gameswap-maven:latest --build-arg user=$USER
docker run -ti --rm -e MAVEN_CONFIG=/app/.m2 -v `pwd`:/app -w /app gameswap-maven:latest mvn -Dmaven.repo.local=/app/.m2 install $@
