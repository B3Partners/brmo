# Copyright (C) 2021 B3Partners B.V.
#
# SPDX-License-Identifier: MIT

FROM eclipse-temurin:17.0.11_9-jre-alpine

LABEL org.opencontainers.image.source=https://github.com/b3partners/brmo/bgt-citygml-loader

ARG BRMO_VERSION=3.0.1-SNAPSHOT
ARG DEBIAN_FRONTEND="noninteractive"
ARG TZ="Europe/Amsterdam"

LABEL org.opencontainers.image.authors="support@b3partners.nl" \
      org.opencontainers.image.description="BRMO BGT Loader" \
      org.opencontainers.image.vendor="B3Partners BV" \
      org.opencontainers.image.licenses="MIT" \
      org.opencontainers.image.title="BRMO BGT Loader" \
      org.opencontainers.image.documentation="https://github.com/B3Partners/brmo/blob/master/bgt-loader/README.md" \
      org.opencontainers.image.version=$BRMO_VERSION

RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && apk upgrade --update \
    && apk -U add --no-cache tzdata musl-locales dumb-init \
    && rm -rf /tmp/* /var/cache/apk/* /var/tmp/*

ENV LANG=nl_NL.UTF-8 LANGUAGE=nl_NL:nl LC_ALL=nl_NL.UTF-8 TZ=$TZ

RUN addgroup user && adduser -G user -S -s /bin/false -H user

COPY target/lib/ /app/lib/
COPY target/bgt-loader.jar /app

WORKDIR /app
RUN chown -R user:user /app
USER user

ENTRYPOINT ["dumb-init", "java","-jar","/app/bgt-loader.jar"]

