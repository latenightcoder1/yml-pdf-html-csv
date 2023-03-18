FROM public.ecr.aws/amazoncorretto/amazoncorretto:latest as builder

WORKDIR application

COPY target/yml-pdf-html-csv*.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM public.ecr.aws/amazoncorretto/amazoncorretto:latest

MAINTAINER Suraj Kumar <surajkr.id@gmail.com>

VOLUME /tmp

#ADD https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem /rds-ca-root.pem

EXPOSE 8080

#EXPOSE 8443

WORKDIR application

COPY --from=builder application/dependencies/ ./

COPY --from=builder application/spring-boot-loader/ ./

COPY --from=builder application/snapshot-dependencies/ ./

COPY --from=builder application/application/ ./

ENTRYPOINT java $JAVA_OPTS -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.JarLauncher
