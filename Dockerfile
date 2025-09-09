FROM kestra/kestra:develop

# Copy the IMAP plugin JAR files
COPY build/libs/*.jar /app/plugins/

# Install curl for healthcheck
USER root
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
USER kestra

# Expose the default Kestra port
EXPOSE 8080