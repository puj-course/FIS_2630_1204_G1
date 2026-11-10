FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# JavaFX y acceso grafico
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
        libgstreamer1.0-0 \
        libgstreamer-plugins-base1.0-0 \
        xvfb \
        xauth \
        x11-utils \
        x11vnc \
        novnc \
        websockify \
        fontconfig \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

COPY target/CareStock-1.0-SNAPSHOT.jar /app/carestock.jar
COPY target/docker-libs-linux/ /app/libs/
COPY docker-start.sh /app/docker-start.sh

RUN chmod +x /app/docker-start.sh

RUN java --module-path /app/carestock.jar:/app/libs \
    --validate-modules

ENV JAVA_TOOL_OPTIONS="-Dprism.order=sw"

EXPOSE 6080

CMD ["/app/docker-start.sh"]
