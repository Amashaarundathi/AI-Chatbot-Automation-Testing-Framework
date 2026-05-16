#!/usr/bin/env bash
# ============================================================
#  run-tests.sh — Main test runner script.
#
#  Usage:
#    ./run-tests.sh                      # Full suite
#    ./run-tests.sh smoke                # Smoke only
#    ./run-tests.sh regression           # Regression (no perf)
#    ./run-tests.sh performance          # Performance only
#    ./run-tests.sh security             # Security only
#    ./run-tests.sh api                  # API only
#    BROWSER=firefox ./run-tests.sh      # Override browser
#    HEADLESS=false ./run-tests.sh smoke # Non-headless smoke
# ============================================================

set -euo pipefail

SUITE="${1:-full}"
BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-true}"
ENV="${ENV:-QA}"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="logs/test-run-${TIMESTAMP}.log"
mkdir -p logs reports screenshots

echo "======================================================"
echo "  AI Chatbot Test Runner"
echo "  Suite     : $SUITE"
echo "  Browser   : $BROWSER (headless=$HEADLESS)"
echo "  Env       : $ENV"
echo "  Timestamp : $TIMESTAMP"
echo "======================================================"

# Map suite name → testng XML file
case "$SUITE" in
  smoke)       XML="testng-smoke.xml" ;;
  regression)  XML="testng-regression.xml" ;;
  performance) XML="testng-performance.xml" ;;
  full)        XML="testng.xml" ;;
  *)
    # Pass suite name as a TestNG group filter
    XML="testng.xml"
    GROUPS="-Dgroups=$SUITE"
    ;;
esac

GROUPS="${GROUPS:-}"

mvn test \
  -Dbrowser="$BROWSER" \
  -Dbrowser.headless="$HEADLESS" \
  -Denvironment="$ENV" \
  ${GROUPS} \
  -Dsurefire.suiteXmlFiles="$XML" \
  --no-transfer-progress \
  2>&1 | tee "$LOG_FILE"

EXIT_CODE=${PIPESTATUS[0]}

echo ""
echo "======================================================"
if [ $EXIT_CODE -eq 0 ]; then
  echo "  ✅ Tests PASSED"
else
  echo "  ❌ Tests FAILED (exit code: $EXIT_CODE)"
fi
echo "  Log   : $LOG_FILE"
echo "======================================================"

# Generate Allure report
if command -v allure &>/dev/null; then
  echo "Generating Allure report..."
  allure generate target/allure-results --clean -o reports/allure-report
  echo "  Report: reports/allure-report/index.html"
fi

exit $EXIT_CODE
