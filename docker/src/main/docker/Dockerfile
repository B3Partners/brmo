FROM tomcat:9.0.73-jre11-temurin

ARG TZ="Europe/Amsterdam"
ARG DEBIAN_FRONTEND="noninteractive"
ARG BRMO_VERSION="snapshot"

#ENV MAIL_FROM=brmo-no-reply@b3partners.nl
#ENV MAIL_HOST=mail.b3partners.nl
#ENV PG_PORT=5432
#ENV PG_HOST=127.0.0.1
#ENV DB_PASS_RSGB=rsgb
#ENV DB_PASS_STAGING=staging
#ENV DB_PASS_RSGBBGT=rsgbbgt
#ENV DB_PASS_TOPNL=topnl
#ENV CATALINA_OPTS="-DMAIL_FROM=$MAIL_FROM -DMAIL_HOST=$MAIL_HOST -DPG_PORT=$PG_PORT -DPG_HOST=$PG_HOST -DDB_PASS_RSGB=$DB_PASS_RSGB -DDB_PASS_STAGING=$DB_PASS_STAGING -DDB_PASS_RSGBBGT=$DB_PASS_RSGBBGT -DDB_PASS_TOPNL=$DB_PASS_TOPNL"

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
    && mkdir -p /opt/brmo-data/BAG/ \
    && mkdir -p /opt/brmo-data/BGT/ \
    && mkdir -p /opt/brmo-data/BRK/berichtendump \
    && mkdir -p /opt/brmo-data/TOPNL/Top10NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top50NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top100NL/ \
    && mkdir -p /opt/brmo-data/TOPNL/Top250NL/ \
    && mkdir -p /opt/brmo-data/sqlscripts/ \
    && chown -R www-data:www-data /usr/local/tomcat/webapps \
    && chown -R www-data:www-data /usr/local/tomcat/logs \
#    && chown -R www-data:www-data /usr/local/tomcat/conf/Catalina/localhost \
    && chown -R www-data:www-data /opt/brmo-data \
    && apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client less \
    && apt-get -y --purge autoremove \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# kopieer webapp en jdni libs in tomcat directories
# maven zorgt ervoor dat deze worden neergezet
COPY bin_unzipped/wars/brmo-service.war /usr/local/tomcat/webapps/ROOT.war
COPY bin_unzipped/drivers/pgsql/*.jar /usr/local/tomcat/lib/

# sla de SQL scripts op in de image om evt. database aan te kunnen maken
COPY ["bin_unzipped/db/rsgb/datamodel_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql.sql", \
      "bin_unzipped/db/brk/brk2.0_postgresql_views.sql", \
      "bin_unzipped/db/brk/brk2.0_commentaar.sql", \
      "bin_unzipped/db/topnl/postgres.sql", \
      "bin_unzipped/db/staging/create-brmo-persistence-postgresql.sql", \
      "/opt/brmo-data/sqlscripts/"]

COPY tomcat_conf /usr/local/tomcat/conf/

EXPOSE 8080

WORKDIR /usr/local/tomcat

USER www-data:www-data

HEALTHCHECK --interval=25s --timeout=5s --retries=2 CMD curl -f http://localhost:8080/brmo-service/ || exit 1

VOLUME ["/usr/local/tomcat/logs", "/opt/brmo-data"]
