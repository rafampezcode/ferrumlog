#!/usr/bin/env sh
set -eu

if [ -n "${DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  normalized_url="${DATABASE_URL#postgres://}"
  normalized_url="${normalized_url#postgresql://}"

  credentials_and_host="${normalized_url%%/*}"
  database_name="${normalized_url#*/}"
  database_name="${database_name%%\?*}"
  host_and_port="${credentials_and_host#*@}"
  database_host="${host_and_port%%:*}"
  database_port="${host_and_port#*:}"

  export SPRING_DATASOURCE_URL="jdbc:postgresql://${database_host}:${database_port}/${database_name}"
fi

exec java ${JAVA_OPTS:-} \
  -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE:-dev}" \
  -jar /app/app.jar