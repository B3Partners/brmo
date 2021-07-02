FROM openjdk:11-jre-slim

LABEL org.opencontainers.image.source=https://github.com/b3partners/brmo/bgt-citygml-loader

ARG DEBIAN_FRONTEND="noninteractive"

ARG TZ="Europe/Amsterdam"

RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && apt-get update \
    && apt-get install -y --no-install-recommends locales tzdata \
    && apt-get -y --purge autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && sed -i 's/# nl_NL.UTF-8 UTF-8/nl_NL.UTF-8 UTF-8/' /etc/locale.gen \
    && locale-gen "nl_NL.UTF-8"

ENV LANG=nl_NL.UTF-8 LANGUAGE=nl_NL:nl LC_ALL=nl_NL.UTF-8

COPY target/lib/ /app/lib/
COPY target/bgt-citygml-loader-*.jar /app
RUN ln -s /app/bgt-citygml-loader-*.jar /app/bgt-citygml-loader.jar

ENTRYPOINT ["java","-jar","/app/bgt-citygml-loader.jar"]

