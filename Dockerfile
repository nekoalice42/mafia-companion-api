# syntax=docker/dockerfile:1
FROM gradle:8.14-alpine AS builder

WORKDIR /app
COPY gradle.properties ./
COPY gradle ./gradle
COPY *.gradle.kts ./

FROM builder AS build-migrations

COPY migrations ./migrations

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    --mount=type=cache,target=/app/.gradle,sharing=locked \
    gradle --no-daemon :migrations:installDist

FROM builder AS build-cleaner

COPY dao ./dao
COPY cleaner ./cleaner

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    --mount=type=cache,target=/app/.gradle,sharing=locked \
    gradle --no-daemon :cleaner:installDist

FROM builder AS build-server

COPY dto ./dto
COPY dao ./dao
COPY apiContract ./apiContract
COPY server ./server

RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    --mount=type=cache,target=/app/.gradle,sharing=locked \
    gradle --no-daemon :server:installDist

FROM eclipse-temurin:21-alpine AS base-runtime

RUN addgroup -g 1000 app \
    && adduser -h /home/app -G app -D -u 1000 app

WORKDIR /home/app
CMD []

FROM base-runtime AS migrations

COPY --from=build-migrations /app/migrations/build/install/migrations /opt/migrations

USER app
ENTRYPOINT ["/opt/migrations/bin/migrations"]

FROM base-runtime AS cleaner

COPY --from=build-cleaner /app/cleaner/build/install/cleaner /opt/cleaner

USER app
ENTRYPOINT ["/opt/cleaner/bin/cleaner"]

FROM base-runtime AS server

RUN --mount=type=cache,target=/var/cache/apk,sharing=locked \
    apk update \
    && apk add curl
COPY --from=build-server /app/server/build/install/server /opt/server

EXPOSE 8080
USER app
ENTRYPOINT ["/opt/server/bin/server"]
