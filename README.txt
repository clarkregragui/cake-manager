Cake Manager Micro Service
===========================

TECHNOLOGIES

Maven
Spring Boot
Spring Data JPA
Spring Security 5 (OAuth2 for Github)
Apache Camel
    - Camel Rest
    - Camel Velocity
    - Camel Swagger
HSQLDB
Spring Test
Junit5
Mockito
Docker
Github Actions CI, Docker Image creation, published to DockerHub



Solution delivered as Camel Spring Boot Rest Service Application secured with Github OAuth2
and integration tests against fully deployed application.

Github Actions script used for CI, Docker Image building and CD of image to DockerHub

GitHub

https://github.com/clarkregragui/cake-manager

DockerHub

https://hub.docker.com/repository/docker/clarkregragui/docker-cakes-manager-repo


TO RUN
=======

mvn package

followed by

mvn spring-boot:run

To run Docker image

docker run -d -p 8080:8080 clarkregragui/docker-cakes-manager-repo:latest

Alternatively directly with

java -jar cake-manager-1.0-SNAPSHOT.jar

Under my java 11 installation I required -Djdk.tls.client.protocols=TLSv1.2 in order for PSK handshake to succceed for multiuser login
as TLSv1.3 PSK(pre-shared-key) handshake was failing for some reason. So depending on your Java installation, you may need 

-Djdk.tls.client.protocols=TLSv1.2 

if you have any problem logging in using Github OAuth2.


Additionally for the OAuth2 version, run with profile prod:

-Dspring.profiles.active=prod

Without OAuth2:

-Dspring.profiles.active=test

If running in an IDE then the lombok jar must be installed.


URLs
====

Swagger API Docs

GET http://localhost:8080/api-doc


Velocity template HTML Cakes View

GET http://localhost:8080/

Pretty Print Json Cakes List

GET http://localhost:8080/cakes

Create new Cake with CakeDTO Json HttpRequest.Body

POST http://localhost:8080/cakes


ADDITIONAL CHANGES
==================

- deleted incomplete legacy solution
- moved cakes.json into version control so that there's no remote dependency and changes to the file are versioned and tested.
- removed all the duplicates from cakes.json file.
- updated some of the photos.


