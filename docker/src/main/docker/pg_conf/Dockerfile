FROM postgis/postgis:15-3.3-alpine

ARG TZ="Europe/Amsterdam"
ARG BRMO_VERSION="3.0.0-SNAPSHOT"

#ENV DB_PASS_RSGB=rsgb
#ENV DB_PASS_STAGING=staging
#ENV DB_PASS_RSGBBGT=rsgbbgt
#ENV DB_PASS_TOPNL=topnl

# get schema export from tailormap persistence
RUN set -eux; \
    apk upgrade --update && apk -U add --no-cache tzdata curl unzip \
    && rm -rf /tmp/*

COPY ["bin_unzipped/db/rsgb/datamodel_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_commentaar.sql", \
      "bin_unzipped/db/topnl/postgres.sql", \
      "bin_unzipped/db/staging/create-brmo-persistence-postgresql.sql", \
      "/opt/brmo-scripts/"]

EXPOSE 5432

USER postgres

HEALTHCHECK --interval=25s --timeout=10s --retries=3 CMD pg_isready || exit 1

COPY pg_conf/docker-entrypoint-initdb.d /docker-entrypoint-initdb.d
