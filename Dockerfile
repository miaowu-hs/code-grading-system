FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/eclipse-temurin:17-jdk AS build

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src src
COPY deploy/maven/settings.xml /root/.m2/settings.xml

RUN mvn -B clean package -DskipTests

FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 g++ \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/code-grading-system-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
