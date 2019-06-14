FROM java:8-jdk

MAINTAINER "Khisa Hamphrey"

ENV TZ=Africa/Nairobi
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY . /opt/saf/
COPY pom.xml/ /opt/saf/lib/
WORKDIR /opt/saf
EXPOSE 8080
CMD java -cp "target/khisaham-1.0-SNAPSHOT.jar:lib/*" com.safaricom.Main