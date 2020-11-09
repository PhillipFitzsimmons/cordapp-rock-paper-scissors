FROM openjdk:8-jdk-alpine

RUN apk --no-cache add curl
RUN apk --no-cache add bash

COPY clients/build/libs/*.jar /clients.jar
COPY build/nodes /nodes
COPY docker-starter.sh /docker-starter.sh
COPY docker-starter-java.sh /docker-starter-java.sh

CMD sh docker-starter.sh
#CMD ["java", "-jar", "clients.jar", "--server.port=10050", "--config.rpc.host=host.docker.internal", "--config.rpc.port=10006", "--config.rpc.username=user1", "--config.rpc.password=test"]