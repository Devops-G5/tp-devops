#!/usr/bin/env bash
set -Eeuo pipefail

source "$(dirname "$0")/run-tests.sh"

wait_for_api

i=1

while true; do
  log "Iniciando ciclo de pruebas"

  if run_cycle "$i"; then
    log "Ciclo $i completado correctamente"
  else
    log "Ciclo $i finalizado con errores"
  fi

  i=$((i + 1))
  sleep "$SLEEP_SECONDS"
done