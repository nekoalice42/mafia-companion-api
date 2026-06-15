# syntax=docker/dockerfile:1
FROM gradle:8.14-alpine AS build

WORKDIR /app
COPY . /app
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    --mount=type=cache,target=/app/.gradle,sharing=locked \
    gradle --no-daemon :migrations:installDist :server:installDist

FROM eclipse-temurin:21-alpine AS base-runtime

RUN addgroup -g 1000 app \
    && adduser -h /home/app -G app -D -u 1000 app

WORKDIR /home/app
CMD []

FROM base-runtime AS migrations

COPY --from=build /app/migrations/build/install/migrations /opt/migrations

USER app
ENTRYPOINT ["/opt/migrations/bin/migrations"]

FROM base-runtime AS server

RUN --mount=type=cache,target=/var/cache/apk,sharing=locked \
    apk update \
    && apk add curl
COPY --from=build /app/server/build/install/server /opt/server

EXPOSE 8080
USER app
ENTRYPOINT ["/opt/server/bin/server"]
