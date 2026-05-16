#!/usr/bin/env bash
# ============================================================
#  newman-run.sh — Runs the Postman collection via Newman
#  for CI pipelines or local API smoke checks.
#
#  Prerequisites:
#    npm install -g newman newman-reporter-htmlextra
#
#  Usage:
#    ./newman-run.sh                        # QA env (default)
#    ./newman-run.sh staging                # Staging env
#    ENV=prod ./newman-run.sh               # Prod env (read-only tests)
# ============================================================

set -euo pipefail

COLLECTION="postman/chatbot-api-collection.json"
ENVIRONMENT="${1:-qa}"
ENV_FILE="postman/env-${ENVIRONMENT}.json"
REPORT_DIR="reports/newman"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# ── Validation ────────────────────────────────────────────────────────────────
if [ ! -f "$COLLECTION" ]; then
  echo "❌ Collection not found: $COLLECTION"
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "❌ Environment file not found: $ENV_FILE"
  exit 1
fi

mkdir -p "$REPORT_DIR"

echo "======================================================"
echo "  Newman API Test Runner"
echo "  Collection : $COLLECTION"
echo "  Environment: $ENVIRONMENT ($ENV_FILE)"
echo "  Report Dir : $REPORT_DIR"
echo "======================================================"

# ── Run ───────────────────────────────────────────────────────────────────────
newman run "$COLLECTION" \
  --environment "$ENV_FILE" \
  --reporters cli,htmlextra,junit \
  --reporter-htmlextra-export "$REPORT_DIR/newman-report-${TIMESTAMP}.html" \
  --reporter-htmlextra-title "Chatbot API Test Report — ${ENVIRONMENT^^} — ${TIMESTAMP}" \
  --reporter-junit-export "$REPORT_DIR/newman-junit-${TIMESTAMP}.xml" \
  --timeout-request 15000 \
  --delay-request 500 \
  --bail

echo ""
echo "✅ Newman run complete."
echo "   HTML Report: $REPORT_DIR/newman-report-${TIMESTAMP}.html"
