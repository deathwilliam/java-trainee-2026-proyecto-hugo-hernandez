
# Etapa 1: compilar FINCOM con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS compilar

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

# Las pruebas de integracion se ejecutan por separado
RUN mvn -B -q package -Dmaven.test.skip=true

# Etapa 2: ejecutar FINCOM
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=compilar /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
