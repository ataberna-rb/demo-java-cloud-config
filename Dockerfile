FROM maven:3.8.2-jdk-11

ARG TAG=latest

ENV VERSION=${TAG}

EXPOSE 8080 8787

CMD java -jar demo-0.0.1-SNAPSHOT.war \
    --server.port=8080 \
    --logging.file=./logs/springboot.log

WORKDIR /var/sources/
COPY ./target/demo-0.0.1-SNAPSHOT.war .