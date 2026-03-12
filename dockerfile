FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
ARG GITHUB_REPO
RUN LATEST_URL=$(curl -s https://api.github.com/repos/${GITHUB_REPO}/releases/latest \
    | grep "browser_download_url" | grep "\.jar\"" | head -1 | cut -d'"' -f4) \
    && curl -L -o app.jar "$LATEST_URL"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## One-time Render setup:

In Render Dashboard → your service → **Environment**:
```
GITHUB_REPO = komalb16/demo-spring-app