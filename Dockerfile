FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Dependencias necesarias para JavaFX en Linux
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgtk-3-0 \
        libx11-6 \
        libxext6 \
        libxrender1 \
        libxtst6 \
        libxi6 \
        libgl1 \
        libasound2 \
        curl \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Maven Wrapper
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw

# Descargar dependencias primero para aprovechar cache
RUN ./mvnw -B -DskipTests dependency:go-offline

# Código fuente
COPY src ./src

# Configuración
COPY conf ./conf

# Compilar CareStock
RUN ./mvnw -B -DskipTests package

# Ejecutar JavaFX
CMD ["./mvnw", "-q", "javafx:run"]
