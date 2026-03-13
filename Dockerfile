FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Copy repo JAR as fallback
COPY target/demo-app-1.0.0.jar fallback.jar

# Read active JAR URL from active-jar.txt in repo
ARG GITHUB_REPO=komalb16/Mobile_App
ARG GITHUB_TOKEN

RUN ACTIVE_URL=$(curl -s \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github.raw" \
    "https://api.github.com/repos/${GITHUB_REPO}/contents/active-jar.txt" \
    | grep -o '"content":"[^"]*"' \
    | cut -d'"' -f4 \
    | base64 -d \
    | tr -d '[:space:]') \
    && if [ -n "$ACTIVE_URL" ] && [ "$ACTIVE_URL" != "none" ]; then \
         echo "Downloading: $ACTIVE_URL" \
         && curl -L \
            -H "Authorization: token ${GITHUB_TOKEN}" \
            -o app.jar "$ACTIVE_URL" \
         && echo "Done"; \
       else \
         echo "No active JAR, using fallback" \
         && cp fallback.jar app.jar; \
       fi

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
