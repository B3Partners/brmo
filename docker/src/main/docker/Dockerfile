FROM tomcat:9.0.98-jre17-temurin

ARG TZ="Europe/Amsterdam"
ARG DEBIAN_FRONTEND="noninteractive"
ARG BRMO_VERSION="snapshot"

LABEL org.opencontainers.image.authors="support@b3partners.nl" \
      org.opencontainers.image.description="BRMO services in Tomcat" \
      org.opencontainers.image.vendor="B3Partners BV" \
      org.opencontainers.image.title="BRMO service" \
      org.opencontainers.image.documentation="https://github.com/B3Partners/brmo/wiki" \
      org.opencontainers.image.licenses="GNU General Public License v3.0" \
      org.opencontainers.image.source="https://github.com/B3Partners/brmo" \
      org.opencontainers.image.version=${BRMO_VERSION}

SHELL ["/bin/bash", "-eux", "-o", "pipefail", "-c"]
RUN set -eux;ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && mkdir -p /opt/brmo-data/BGT/ \
    && mkdir -p /opt/brmo-data/NHR/certificaten/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top10NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top50NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top100NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top250NL/ \
    && mkdir -p /opt/brmo-data/sqlscripts/ \
    && chown -R www-data:www-data /usr/local/tomcat/webapps \
    && chown -R www-data:www-data /usr/local/tomcat/logs \
    && chown -R www-data:www-data /opt/brmo-data \
    && apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client less \
    && apt-get -y --purge autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# kopieer webapp en jdni libs in tomcat directories
# maven zorgt ervoor dat deze worden neergezet/uitgepakt
COPY bin_unzipped/wars/brmo-service.war /usr/local/tomcat/webapps/ROOT.war
COPY bin_unzipped/drivers/pgsql/*.jar /usr/local/tomcat/lib/

# sla de SQL scripts op in de image om evt. database aan te kunnen maken
COPY ["bin_unzipped/db/rsgb/datamodel_postgresql.sql", \
      "bin_unzipped/db/rsgb/postgresql/209_bag2_rsgb_views.sql", \
      "bin_unzipped/db/rsgb/postgresql/210_bag2_brk2.0_mat_views.sql", \
      "bin_unzipped/db/rsgb/postgresql/211_nhr_views.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql_views.sql", \
      "bin_unzipped/db/brk/brk2.0_commentaar.sql", \
      "bin_unzipped/db/bag/postgresql/208_bag2_views.sql", \
      "bin_unzipped/db/topnl/postgres.sql", \
      "bin_unzipped/db/staging/create-brmo-persistence-postgresql.sql", \
      "/opt/brmo-data/sqlscripts/"]

COPY tomcat_conf /usr/local/tomcat/conf/

EXPOSE 8080

WORKDIR /usr/local/tomcat

USER www-data:www-data

VOLUME ["/usr/local/tomcat/logs", "/opt/brmo-data"]
