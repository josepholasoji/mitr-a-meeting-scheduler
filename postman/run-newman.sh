#!/usr/bin/env bash
# Waits for the API to report healthy, then runs the full Postman collection
# with Newman and exits non-zero if any assertion fails - suitable for CI or a
# one-shot local smoke test.
#
# Usage:
#   ./postman/run-newman.sh                                   # http://localhost:8080
#   BASE_URL=http://localhost:8088 ./postman/run-newman.sh    # docker compose port
#   ./postman/run-newman.sh --reporters cli,junit --reporter-junit-export postman/newman-report.xml
#
# Any extra arguments are passed straight through to `newman run`.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COLLECTION="$SCRIPT_DIR/Doodle-Meeting-Scheduler.postman_collection.json"
BASE_URL="${BASE_URL:-http://localhost:8088}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-60}"

echo "Waiting for $BASE_URL/actuator/health (timeout ${TIMEOUT_SECONDS}s)..."
elapsed=0
until curl -sf -o /dev/null "$BASE_URL/actuator/health"; do
  if [ "$elapsed" -ge "$TIMEOUT_SECONDS" ]; then
    echo "App did not become healthy within ${TIMEOUT_SECONDS}s - is it running?" >&2
    exit 1
  fi
  sleep 2
  elapsed=$((elapsed + 2))
done
echo "App is healthy after ${elapsed}s. Running collection against $BASE_URL ..."

npx --yes newman run "$COLLECTION" --env-var "baseUrl=$BASE_URL" "$@"
