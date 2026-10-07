FROM eclipse-temurin:21-jdk-jammy

# Instalar todas las librerías gráficas y de GTK requeridas por JavaFX en Linux
RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxxf86vm1 \
    libgl1-mesa-glx \
    libgl1-mesa-dri \
    libgtk-3-0 \
    libfontconfig1 \
    libfreetype6 \
    && rm -rf /var/lib/apt/lists*

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN ./mvnw clean package -DskipTests

ENTRYPOINT ["./mvnw", "javafx:run"]