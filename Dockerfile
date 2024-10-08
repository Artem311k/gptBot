# Start with a base image containing Java runtime (Java 17)
FROM openjdk:17

# Add Maintainer Info
LABEL maintainer="kuzmin chat bot"

# Add a volume pointing to /tmp
#VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8081

ENV botToken="";
ENV apiTokens="";
ENV botPassword="";

# The application's jar file
ARG JAR_FILE=target/gptBot-0.20.jar

# Add the application's jar to the container
ADD ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT java -DbotToken=$botToken -DapiTokens=$apiTokens -DbotPassword=$botPassword -jar /app.jar