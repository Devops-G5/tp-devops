#!/usr/bin/env bash

set -u

INTERVAL_SECONDS="${INTERVAL_SECONDS:-300}"

while true; do
  echo "Iniciando pruebas: $(date)"
  ./run-tests.sh
  echo "Próxima ejecución en ${INTERVAL_SECONDS} segundos"
  sleep "$INTERVAL_SECONDS"
done