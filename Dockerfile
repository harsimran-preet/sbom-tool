# Dockerfile
FROM openjdk:17-jdk-slim

# Install Curl
RUN apt-get update && apt-get install -y curl

# Install Syft
RUN curl -sSfL https://raw.githubusercontent.com/anchore/syft/main/install.sh | sh -s -- -b /usr/local/bin

WORKDIR /app
COPY target/sbom-tool-1.0-SNAPSHOT.jar /app/sbom-tool.jar

ENTRYPOINT ["java", "-jar", "/app/sbom-tool.jar"]
