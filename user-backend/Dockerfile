# Use a minimal OpenJDK runtime for deployment
FROM registry.access.redhat.com/ubi8/openjdk-17:latest

# Set the working directory
WORKDIR /opt
#RUN chown -R 1001:0 /app
#RUN chmod -R g=u /app
# Copy the built JAR file from the builder stage
COPY ./target/*.jar app.jar

# Expose application port
EXPOSE 8182

# Be non-root user

USER 1001

# Define the entry point
CMD ["java", "-jar", "app.jar"]
