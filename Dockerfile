# =========================================================================
# Etapa 1: Compilación del proyecto (Builder)
# =========================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiamos el wrapper de Maven y el pom.xml primero
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Descargamos dependencias en la caché de Docker
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiamos el código fuente y generamos el .jar ejecutable
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# =========================================================================
# Etapa 2: Imagen ligera para ejecución (Runtime)
# =========================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Creamos un usuario sin privilegios root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiamos ÚNICAMENTE el archivo .jar generado en la Etapa 1
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Render define automáticamente la variable PORT
ENV PORT=8080
EXPOSE ${PORT}

# Ajuste de memoria RAM para servidores cloud económicos
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]