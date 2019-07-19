FROM openjdk:8
MAINTAINER Holonix Srl <musumeci.holonix@gmail.com>
VOLUME /tmp
ARG finalName
ENV JAR '/'$finalName
ARG port
ADD $finalName $JAR
RUN touch $JAR
ENV PORT 8889
EXPOSE $PORT

RUN env

ENTRYPOINT ["java", "-jar", "data-pipes-service.jar"]