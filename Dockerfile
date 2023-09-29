FROM maven:3.9.4-eclipse-temurin-17-alpine

COPY ./compiler compiler/
COPY ./runtime runtime/
COPY ./pom.xml pom.xml

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
COPY --from=0 ./runtime/target/runtime-1.0-SNAPSHOT.jar runtime.jar
COPY --from=0 ./compiler/target/compiler-1.0-SNAPSHOT.jar compiler.jar

ENTRYPOINT ["java", "-Xss256M", "-cp", "runtime.jar", "-jar", "compiler.jar"]
CMD ["--run", "/var/rinha/source.rinha.json"]