FROM eclipse-temurin:21
WORKDIR /usr/app
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} nameprocessing.jar
ENTRYPOINT ["java", "-jar", "nameprocessing.jar"]
