ARG BUILD_IMAGE=sbtscala/scala-sbt:eclipse-temurin-alpine-21.0.7_6_1.11.4_3.3.6
FROM ${BUILD_IMAGE} AS builder
WORKDIR /workspace
COPY project project
COPY build.sbt .
RUN sbt update
COPY src src
RUN sbt clean test assembly

FROM eclipse-temurin:21-jre
RUN apt-get update \
  && apt-get install --yes --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/* \
  && useradd --system --uid 10001 --create-home app
WORKDIR /app
COPY --from=builder /workspace/target/scala-3.3.8/clearing-engine.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
