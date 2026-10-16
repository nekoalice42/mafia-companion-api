# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-alpine AS base

FROM base AS builder

WORKDIR /src

# Alpine wget doesn't support 'connect-timeout', and curl is preferred over wget
# for Kotlin Toolchain wrapper, so just install it instead of fixing wget.
RUN --mount=type=cache,target=/var/cache/apk,sharing=locked \
    apk update \
    && apk add curl

ENV KOTLIN_CLI_NO_WELCOME_BANNER=1 \
    KOTLIN_CLI_JAVA_HOME="${JAVA_HOME}" \
    KOTLIN_CLI_BOOTSTRAP_CACHE_DIR=/cache/kotlin-cli \
    KOTLIN_SHARED_CACHE_DIR=/cache/kotlin-libs

# Kotlin Toolchain wrapper, rarely changes.
COPY kotlin ./

# Provision Kotlin Toolchain.
RUN --mount=type=cache,target=/cache/kotlin-cli,sharing=locked \
    rm -f /cache/kotlin-cli/download-*.lock \
    && ./kotlin --version

# Config files.
COPY libs.versions.toml project.yaml ./
COPY module-templates ./module-templates

# Sources, in order of changes frequency/importance.
COPY dao ./dao
COPY migrations ./migrations
COPY cleaner ./cleaner
COPY dto ./dto
COPY apiContract ./apiContract
COPY server ./server

# Package modules.
RUN --mount=type=cache,target=/cache/kotlin-cli,sharing=locked \
    --mount=type=cache,target=/cache/kotlin-libs \
    ./kotlin package \
      -f executable-jar \
      -m server \
      -m cleaner \
      -m migrations

FROM base AS base-runtime

RUN addgroup -g 1000 app \
    && adduser -h /home/app -G app -D -u 1000 app

WORKDIR /home/app
CMD []

FROM base-runtime AS migrations

COPY --from=builder \
  /src/build/tasks/_migrations_executableJarJvm/migrations-jvm-executable.jar /usr/lib/migrations.jar

USER app
ENTRYPOINT ["/usr/bin/env", "java", "-jar", "/usr/lib/migrations.jar"]

FROM base-runtime AS cleaner

COPY --from=builder \
  /src/build/tasks/_cleaner_executableJarJvm/cleaner-jvm-executable.jar /usr/lib/cleaner.jar

USER app
ENTRYPOINT ["/usr/bin/env", "java", "-jar", "/usr/lib/cleaner.jar"]

FROM base-runtime AS server

# For healthcheck.
RUN --mount=type=cache,target=/var/cache/apk,sharing=locked \
    apk update \
    && apk add curl

COPY --from=builder \
  /src/build/tasks/_server_executableJarJvm/server-jvm-executable.jar /usr/lib/server.jar

EXPOSE 8080
USER app
ENTRYPOINT ["/usr/bin/env", "java", "-jar", "/usr/lib/server.jar"]
