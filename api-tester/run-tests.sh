#!/usr/bin/env bash
set -Eeuo pipefail

BASE_URL="${BASE_URL:-http://api:8080}"
ITERATIONS="${ITERATIONS:-3}"
SLEEP_SECONDS="${SLEEP_SECONDS:-2}"
MAX_WAIT="${MAX_WAIT:-120}"

PASS=0
FAIL=0

log() {
  printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

# Ejecuta una petición y valida el código HTTP esperado.
request() {
  local method="$1"
  local path="$2"
  local expected="$3"
  local body="${4:-}"
  local response_file
  local code
  local content_type

  response_file="$(mktemp)"
  RESPONSE=""

  local args=(
    -sS
    --connect-timeout 5
    --max-time 15
    -o "$response_file"
    -w $'%{http_code}\n%{content_type}'
    -X "$method"
  )

  if [[ -n "$body" ]]; then
    args+=(-H 'Content-Type: application/json' -d "$body")
  fi

  if ! result="$(curl "${args[@]}" "${BASE_URL}${path}")"; then
    log "FAIL $method $path: error de conexión o timeout"
    rm -f "$response_file"
    FAIL=$((FAIL + 1))
    return 1
  fi

  code="${result%%$'\n'*}"
  content_type="${result#*$'\n'}"

  if [[ "$code" == "$expected" ]]; then
    log "PASS $method $path -> HTTP $code"
    PASS=$((PASS + 1))
  else
    log "FAIL $method $path -> esperado $expected, recibido HTTP $code"

    # Mostrar solo respuestas JSON; evitar imprimir páginas HTML de Render.
    if [[ "$content_type" == *"application/json"* ]]; then
      cat "$response_file"
      printf '\n'
    elif [[ "$content_type" == *"text/html"* ]]; then
      log "Respuesta HTML recibida (probablemente una página de error del servidor)"
    fi

    rm -f "$response_file"
    FAIL=$((FAIL + 1))
    return 1
  fi

  RESPONSE="$(cat "$response_file")"
  rm -f "$response_file"
}

# Extrae un campo JSON sin depender de jq.
json_field() {
  python3 -c '
import json, sys
obj = json.load(sys.stdin)
value = obj
for key in sys.argv[1].split("."):
    value = value[key]
print(value)
' "$1" <<< "$2"
}

# Espera a que la API esté disponible.
wait_for_api() {
  local elapsed=0

  log "Esperando API en $BASE_URL..."

  until curl -sS --max-time 10 \
    -o /dev/null \
    "${BASE_URL}/api/tasks"; do

    log "La API todavía no respondió; reintentando..."

    if (( elapsed >= MAX_WAIT )); then
      log "ERROR: la API no respondió en ${MAX_WAIT}s"
      exit 1
    fi

    sleep 3
    elapsed=$((elapsed + 3))
  done

  log "API disponible"
}
run_cycle() {
  local iteration="$1"
  local suffix="test-${iteration}-$(date +%s)-$$"
  local task_id
  local user_id
  local task_id_not_found='AASD'

  log "========== CICLO $iteration =========="

  # TASKS: crear, listar, obtener, actualizar y eliminar.
  request POST /api/tasks 201 \
    "{\"name\":\"Task ${suffix}\",\"description\":\"Prueba automatizada\",\"status\":\"pending\",\"type\":\"devops\",\"owner\":\"Tester\"}" \
    || return 1

  task_id="$(json_field id "$RESPONSE")" || return 1

  request GET /api/tasks 200 || return 1
  request GET "/api/tasks/${task_id}" 200 || return 1

  request PUT "/api/tasks/${task_id}" 200 \
    "{\"name\":\"Task actualizada ${suffix}\",\"description\":\"Actualizada\",\"status\":\"done\",\"type\":\"testing\",\"owner\":\"Tester\"}" \
    || return 1

  request GET "/api/tasks/${task_id}" 200 || return 1
  request DELETE "/api/tasks/${task_id}" 204 || return 1
  request GET "/api/tasks/${task_id}" 404 || return 1

  # USERS: crear, listar, obtener, actualizar y eliminar.
  request POST /api/users 201 \
    "{\"name\":\"Test\",\"lastname\":\"User\",\"username\":\"${suffix}\",\"email\":\"${suffix}@example.com\",\"rol\":\"developer\"}" \
    || return 1

  user_id="$(json_field id "$RESPONSE")" || return 1

  request GET /api/users 200 || return 1
  request GET "/api/users/${user_id}" 200 || return 1

  request PUT "/api/users/${user_id}" 200 \
    "{\"name\":\"Test Updated\",\"lastname\":\"User\",\"username\":\"${suffix}\",\"email\":\"${suffix}@example.com\",\"rol\":\"designer\"}" \
    || return 1

  request GET "/api/users/${user_id}" 200 || return 1

  # ASIGNACIÓN: crear otra tarea y asignársela al usuario.
  request POST /api/tasks 201 \
    "{\"name\":\"Task asignable ${suffix}\",\"status\":\"pending\",\"type\":\"devops\"}" \
    || return 1

  task_id="$(json_field id "$RESPONSE")" || return 1

  request PATCH "/api/tasks/${task_id}/assignee" 200 \
    "{\"userId\":\"${user_id}\"}" || return 1

  # Una segunda asignación debe fallar con 409.
  request PATCH "/api/tasks/${task_id}/assignee" 409 \
    "{\"userId\":\"${user_id}\"}" || return 1

  # Limpiar los datos creados por el ciclo.
  request DELETE "/api/tasks/${task_id}" 204 || return 1
  request DELETE "/api/users/${user_id}" 204 || return 1


  # Ciclo de 400's
  request GET "/api/users/${task_id_not_found}" 404 || return 1

  log "Ciclo $iteration completado"
}

wait_for_api

for ((i = 1; i <= ITERATIONS; i++)); do
  if run_cycle "$i"; then
    :
  else
    log "Ciclo $i interrumpido por un error"
  fi

  if (( i < ITERATIONS )); then
    sleep "$SLEEP_SECONDS"
  fi
done

log "========== RESUMEN =========="
log "Pruebas correctas: $PASS"
log "Pruebas fallidas: $FAIL"

if (( FAIL > 0 )); then
  exit 1
fi
