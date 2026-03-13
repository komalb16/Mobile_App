FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Copy repo JAR as fallback
COPY target/demo-app-1.0.0.jar fallback.jar

# Read active JAR URL from active-jar.txt in repo
ARG GITHUB_REPO=komalb16/Mobile_App
RUN ACTIVE_URL=$(curl -s https://raw.githubusercontent.com/${GITHUB_REPO}/main/active-jar.txt 2>/dev/null | tr -d '[:space:]') \
    && if [ -n "$ACTIVE_URL" ] && [ "$ACTIVE_URL" != "none" ]; then \
         echo "Downloading active JAR from: $ACTIVE_URL" \
         && curl -L -o app.jar "$ACTIVE_URL" \
         && echo "Active JAR downloaded successfully"; \
       else \
         echo "No active JAR set, using repo JAR" \
         && cp fallback.jar app.jar; \
       fi

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
