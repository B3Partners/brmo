# Copyright (C) 2021 B3Partners B.V.
#
# SPDX-License-Identifier: GPL-3.0-or-later

FROM openjdk:11-jre-slim

LABEL org.opencontainers.image.source=https://github.com/b3partners/brmo/bgt-citygml-loader

ARG DEBIAN_FRONTEND="noninteractive"

ARG TZ="Europe/Amsterdam"

RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && apt-get update \
    && apt-get install -y --no-install-recommends locales tzdata \
    && apt-get install -y dumb-init \
    && apt-get -y --purge autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && sed -i 's/# nl_NL.UTF-8 UTF-8/nl_NL.UTF-8 UTF-8/' /etc/locale.gen \
    && locale-gen "nl_NL.UTF-8"

ENV LANG=nl_NL.UTF-8 LANGUAGE=nl_NL:nl LC_ALL=nl_NL.UTF-8

RUN useradd --user-group --system user -s /bin/false

COPY target/lib/ /app/lib/
COPY target/bgt-loader-*.jar /app

WORKDIR /app
RUN chown -R user:user /app
USER user

RUN ln -s bgt-loader-*.jar bgt-loader.jar

ENTRYPOINT ["dumb-init", "java","-jar","/app/bgt-loader.jar"]

