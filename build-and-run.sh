#!/bin/bash
# ────────────────────────────────────────────────
#  Demo App — Build & Run Script
# ────────────────────────────────────────────────

set -e

echo ""
echo "╔══════════════════════════════════════╗"
echo "║        Demo App — Build Script       ║"
echo "╚══════════════════════════════════════╝"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
  echo "❌ Java not found. Install Java 17+ from https://adoptium.net"
  exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -1)
echo "✓ Java: $JAVA_VER"

# Check Maven (try local wrapper first)
if [ -f "./mvnw" ]; then
  MVN="./mvnw"

elif command -v mvn >/dev/null 2>&1; then
  MVN="mvn"

else
  echo ""
  echo "📦 Maven not found. Downloading Maven..."

  MAVEN_VERSION="3.9.6"
  MAVEN_ARCHIVE="apache-maven-$MAVEN_VERSION-bin.tar.gz"
  MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/$MAVEN_ARCHIVE"

  curl -L -o "$MAVEN_ARCHIVE" "$MAVEN_URL"
  tar -xzf "$MAVEN_ARCHIVE"

  export PATH="$(pwd)/apache-maven-$MAVEN_VERSION/bin:$PATH"
  MVN="mvn"
fi

echo "✓ Maven: $MVN"

echo ""
echo "🔨 Building JAR..."
$MVN clean package -q -DskipTests

echo ""
echo "✅ Build complete!"
echo ""
echo "🚀 Starting app on http://localhost:8080 ..."
echo "   Press Ctrl+C to stop"
echo ""
java -jar target/demo-app-1.0.0.jar
