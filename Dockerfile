FROM maven:3.5.0-jdk-8-alpine

WORKDIR /usr/src/app

COPY . /usr/src/app/

RUN mvn clean package

CMD ["java","-jar","/usr/src/app/jar/BitcoinTracker.jar"]
