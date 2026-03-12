FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Copy JAR from repo as fallback
COPY target/demo-app-1.0.0.jar fallback.jar

# Try to download latest JAR from GitHub Releases, fall back to repo JAR
ARG GITHUB_REPO=komalb16/Mobile_App
RUN LATEST_URL=$(curl -s https://api.github.com/repos/${GITHUB_REPO}/releases/latest \
    | grep "browser_download_url" \
    | grep "\.jar" \
    | head -1 \
    | cut -d'"' -f4) \
    && if [ -n "$LATEST_URL" ]; then \
         echo "Downloading JAR from GitHub Release: $LATEST_URL" \
         && curl -L -o app.jar "$LATEST_URL" \
         && echo "Release JAR downloaded successfully"; \
       else \
         echo "No GitHub Release found, using repo JAR" \
         && cp fallback.jar app.jar; \
       fi

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
