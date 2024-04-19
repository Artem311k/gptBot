# Start with a base image containing Java runtime (Java 17)
FROM openjdk:17

# Add Maintainer Info
LABEL maintainer="kuzmin chat bot"

# Add a volume pointing to /tmp
#VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8081

ENV botToken="";
ENV apiToken="";

# The application's jar file
ARG JAR_FILE=target/gptBot-0.10.jar

# Add the application's jar to the container
ADD ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT java -DbotToken=$botToken -DapiToken=$apiToken -jar /app.jar