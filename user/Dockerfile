FROM tomcat:9.0
ADD ./target/user-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/user.war
EXPOSE 8080
CMD ["catalina.sh", "run"]