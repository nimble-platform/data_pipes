FROM java:8
MAINTAINER evgeniyh@il.ibm.com

WORKDIR /
ADD target/data_pipes.jar data_pipes.jar
EXPOSE 8080

CMD java -jar data_pipes.jar --start-streams
