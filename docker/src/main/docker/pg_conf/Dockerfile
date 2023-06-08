FROM postgis/postgis:15-3.3-alpine

ARG TZ="Europe/Amsterdam"
ARG BRMO_VERSION="snapshot"

#ENV DB_PASS_RSGB=rsgb
#ENV DB_PASS_STAGING=staging
#ENV DB_PASS_RSGBBGT=rsgbbgt
#ENV DB_PASS_TOPNL=topnl

LABEL org.opencontainers.image.authors="support@b3partners.nl" \
      org.opencontainers.image.description="BRMO services PostGIS database" \
      org.opencontainers.image.vendor="B3Partners BV" \
      org.opencontainers.image.title="BRMO service database" \
      org.opencontainers.image.documentation="https://github.com/B3Partners/brmo/wiki" \
      org.opencontainers.image.licenses="GNU General Public License v3.0" \
      org.opencontainers.image.source="https://github.com/B3Partners/brmo" \
      org.opencontainers.image.version=${BRMO_VERSION}

RUN set -eux; \
    apk upgrade --update && apk -U add --no-cache tzdata unzip openssl \
    && rm -rf /tmp/* /var/cache/apk/* /var/tmp/*

COPY ["bin_unzipped/db/rsgb/datamodel_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql_views.sql", \
      "bin_unzipped/db/brk/brk2.0_commentaar.sql", \
      "bin_unzipped/db/topnl/postgres.sql", \
      "bin_unzipped/db/staging/create-brmo-persistence-postgresql.sql", \
      "/opt/brmo-scripts/"]

COPY pg_conf/ssl /opt/ssl
RUN chown -R postgres:postgres /opt/ssl/
RUN chmod 600 /opt/ssl/private/*

EXPOSE 5432

USER postgres

HEALTHCHECK --interval=25s --timeout=10s --retries=3 CMD pg_isready || exit 1

COPY pg_conf/docker-entrypoint-initdb.d /docker-entrypoint-initdb.d
