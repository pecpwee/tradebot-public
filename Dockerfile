FROM openjdk:8-jdk-alpine

RUN apk add tzdata
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone
RUN apk del tzdata

COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]