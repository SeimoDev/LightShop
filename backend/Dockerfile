# Build stage
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY settings.xml /root/.m2/settings.xml
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM tomcat:9-jdk11-temurin
LABEL maintainer="LightShop Team"

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file
COPY --from=build /app/target/lightshop.war /usr/local/tomcat/webapps/ROOT.war

# Create data directory for SQLite
RUN mkdir -p /app/data /app/uploads

# Set environment variables
ENV CATALINA_OPTS="-Xms256m -Xmx512m"
ENV DB_PATH=/app/data/lightshop.db
ENV UPLOAD_PATH=/app/uploads

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Start Tomcat
CMD ["catalina.sh", "run"]

