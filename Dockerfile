FROM amazoncorretto:21 as base

FROM base as development

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

RUN ./mvnw package

EXPOSE 10228
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=dev", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000'"]

FROM base as production

VOLUME /tmp
VOLUME /target

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

EXPOSE 8228
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
