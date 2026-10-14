FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Bibliotecas graficas de JavaFX y display virtual
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

# Maven Wrapper del proyecto real
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Codigo fuente de CareStock
COPY src ./src

# Construir el proyecto
RUN ./mvnw -B -DskipTests clean package

# JavaFX en modo grafico virtual
ENV JAVA_TOOL_OPTIONS="-Dprism.order=sw"

CMD ["xvfb-run", "-a", "./mvnw", "-q", "javafx:run"]
