# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Etapa 2: imagen liviana de ejecución ----------
FROM eclipse-temurin:21-jre
ENV TZ=America/La_Paz
WORKDIR /app
RUN useradd --system --create-home kingtokky && mkdir -p /app/data/archivos /app/data/respaldos && chown -R kingtokky /app
COPY --from=build /build/target/king-tokky-backend.jar app.jar
USER kingtokky
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
