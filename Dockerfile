ARG TAG_VERSION_BUILD=3.9-eclipse-temurin-25-alpine
ARG TAG_VERSION=25-jre-alpine

# STAGE 1 - BUILD

FROM maven:${TAG_VERSION_BUILD} AS build

WORKDIR /app

# Primero copiamos el pom con las dependencias, para que docker utilice esta capa
# "cacheada", ya que lo que es dependencias no cambia muy seguido
COPY pom.xml .

# Descarga de dependencias
RUN mvn dependency:go-offline

# Despues se copia el codigo que es lo que mas cambia
COPY src ./src

# Compila y genera el jar
RUN mvn clean package -DskipTests

# STAGE 2 - PRD


FROM eclipse-temurin:${TAG_VERSION} AS prd

ARG APP_VERSION=1.0.0
ARG BUILD_DATE

LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.version=$APP_VERSION \
      maintainer="Nayesz;Lucasb" \
      description="DevOps Project 2026 - G5"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup  # Se crea usuario no-root
USER appuser

COPY --from=build  --chown=appuser:appuser /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]