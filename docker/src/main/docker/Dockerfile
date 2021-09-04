FROM tomcat:10-jdk11

ARG BRMO_VERSION=2.1.0
ARG MAIL_FROM=brmo-no-reply@b3partners.nl
ARG MAIL_HOST=mail.b3partners.nl
ARG AJP_ADDRESS=::1
ARG AJP_SECRET=noisyPurpl315
ARG PG_PORT=5432
ARG PG_HOST=127.0.0.1
ARG DB_NAME_RSGB=rsgb
ARG DB_USER_RSGB=rsgb
ARG DB_PASS_RSGB=rsgb
ARG DB_NAME_STAGING=staging
ARG DB_USER_STAGING=staging
ARG DB_PASS_STAGING=staging
ARG DB_NAME_RSGBBGT=rsgbbgt
ARG DB_USER_RSGBBGT=rsgbbgt
ARG DB_PASS_RSGBBGT=rsgbbgt
ARG DB_NAME_TOPNL=topnl
ARG DB_USER_TOPNL=topnl
ARG DB_PASS_TOPNL=topnl
ENV CATALINA_OPTS="-DAJP_SECRET=$AJP_SECRET -DAJP_ADDRESS=$AJP_ADDRESS -DMAIL_FROM=$MAIL_FROM -DMAIL_HOST=$MAIL_HOST -DPG_PORT=$PG_PORT -DPG_HOST=$PG_HOST -DDB_NAME_RSGB=$DB_NAME_RSGB -DDB_USER_RSGB=$DB_USER_RSGB -DDB_PASS_RSGB=$DB_PASS_RSGB -DDB_NAME_STAGING=$DB_NAME_STAGING -DDB_USER_STAGING=$DB_USER_STAGING -DDB_PASS_STAGING=$DB_PASS_STAGING -DDB_NAME_RSGBBGT=$DB_NAME_RSGBBGT -DDB_USER_RSGBBGT=$DB_USER_RSGBBGT -DDB_PASS_RSGBBGT=$DB_PASS_RSGBBGT -DDB_NAME_TOPNL=$DB_NAME_TOPNL -DDB_USER_TOPNL=$DB_USER_TOPNL -DDB_PASS_TOPNL=$DB_PASS_TOPNL"
ARG TZ="Europe/Amsterdam"
ARG DEBIAN_FRONTEND="noninteractive"

SHELL ["/bin/bash", "-eux", "-o", "pipefail", "-c"]
RUN mkdir -p /opt/brmo-data/BAG/ \
    && mkdir -p /opt/brmo-data/BRK/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top10NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top50NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top100NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top250NL/ \
    && mkdir -p /opt/brmo-data/sqlscripts/ \
    && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc -O- | apt-key add - \
    && sh -c "echo \"deb http://apt.postgresql.org/pub/repos/apt/ buster-pgdg main\" > /etc/apt/sources.list.d/pgdg.list" \
    && apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates postgresql-client-12 less tzdata \
    && apt-get -y --purge autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN set -eux;ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# kopieer webapp en jdni libs in tomcat directories
# maven zorgt ervoor dat deze worden neergezet
COPY bin_unzipped/wars/brmo-s*.war /usr/local/tomcat/webapps/
COPY bin_unzipped/drivers/pgsql/*.jar /usr/local/tomcat/lib/

# sla de SQL scripts op in de image om evt. database aan te kunnen maken
COPY ["bin_unzipped/db/rsgb/datamodel_postgresql.sql", \
      "bin_unzipped/db/topnl/postgres.sql", \
      "bin_unzipped/db/staging/create-brmo-persistence-postgresql.sql", \
      "/opt/brmo-data/sqlscripts/"]

COPY tomcat_conf /usr/local/tomcat/conf/

EXPOSE 8009

HEALTHCHECK CMD curl -f http://localhost:8080/brmo-service/ || exit 1

LABEL org.opencontainers.image.authors="@mprins" \
      description="BRMO services in Tomcat" \
      nl.b3p.vendor="B3Partners" \
      nl.b3p.image="BRMO service" \
      nl.b3p.version=$BRMO_VERSION

VOLUME ["/usr/local/tomcat/logs", "/opt/brmo-data"]