ARG TAG_VERSION_BUILD=3.9-eclipse-temurin-25-alpine

FROM maven:${TAG_VERSION_BUILD}

ARG APP_VERSION
ARG BUILD_DATE

LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.version=$APP_VERSION \
      maintainer="Nayesz;Lucasb" \
      description="DevOps Project 2026 - G5"

WORKDIR /app

# Se crea un usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Primero copiamos el POM para aprovechar la caché de dependencias
COPY pom.xml .

RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Compilamos y generamos el JAR
RUN mvn clean package -DskipTests

# Ejecutamos la aplicación como usuario no-root
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/target/tu-aplicacion.jar"]