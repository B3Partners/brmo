FROM postgis/postgis:14-3.2-alpine

ARG TZ="Europe/Amsterdam"
ARG BRMO_VERSION="2.2.1-SNAPSHOT"

# get schema export from tailormap persistence
RUN set -eux; \
    apk upgrade --update && apk -U add --no-cache tzdata curl unzip \
    && rm -rf /tmp/*

COPY ["bin_unzipped/db/rsgb/datamodel_postgresql.sql", \
      "bin_unzipped/db/topnl/postgres.sql", \
      "bin_unzipped/db/staging/create-brmo-persistence-postgresql.sql", \
      "/opt/brmo-scripts/"]

COPY pg_conf/docker-entrypoint-initdb.d /docker-entrypoint-initdb.d
