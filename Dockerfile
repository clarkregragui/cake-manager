FROM adoptopenjdk/openjdk11:alpine-jre

ARG JAR_FILE=target/cake-manager-1.0-SNAPSHOT.jar

WORKDIR /opt/app

COPY ${JAR_FILE} cake-manager.jar

ENTRYPOINT ["java","-jar","cake-manager.jar"]