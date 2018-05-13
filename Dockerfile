FROM tomcat
MAINTAINER evgeniyh@il.ibm.com

RUN rm -rf /usr/local/tomcat/webapps/*

ADD ./target/data_pipes.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]