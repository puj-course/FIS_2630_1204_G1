FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Dependencias necesarias para JavaFX en Linux + display virtual
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgtk-3-0 \
        libx11-6 \
        libxext6 \
        libxrender1 \
        libxtst6 \
        libxi6 \
        libxrandr2 \
        libgl1 \
        libasound2 \
        xvfb \
        xauth \
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

# Forzar renderizado por software
ENV JAVA_TOOL_OPTIONS="-Dprism.order=sw"

# Ejecutar JavaFX usando un display virtual
CMD ["xvfb-run", "-a", "./mvnw", "-q", "javafx:run"]