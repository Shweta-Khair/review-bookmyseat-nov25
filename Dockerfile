FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -g 1001 -S reviewservice && \
    adduser -S reviewservice -u 1001 -G reviewservice

# Copy the jar file
COPY target/review-service-*.jar app.jar

# Change ownership of the app
RUN chown reviewservice:reviewservice app.jar

# Switch to non-root user
USER reviewservice

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]