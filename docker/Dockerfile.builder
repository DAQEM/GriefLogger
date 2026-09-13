FROM eclipse-temurin:25-jdk-noble

RUN apt-get update && apt-get install -y python3 python3-pip python3-venv && rm -rf /var/lib/apt/lists/*

WORKDIR /app

CMD ./gradlew shadowJar --no-daemon && mkdir -p /build/fabric/build/libs && cp -r fabric/build/libs/* /build/fabric/build/libs/