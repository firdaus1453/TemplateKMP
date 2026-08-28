#!/usr/bin/env bash
set -e

# ==============================================================================
# Maestro Automated Test Runner for TemplateKMP
# Designed for 100% automated AI agent / CI/CD execution without human intervention.
# ==============================================================================

# Ensure Maestro CLI is in PATH
export PATH="$HOME/.maestro/bin:$PATH"

if ! command -v maestro &> /dev/null; then
    echo "⚠️ Maestro is not installed. Installing Maestro..."
    curl -fsSL "https://get.maestro.mobile.dev" | bash
    export PATH="$HOME/.maestro/bin:$PATH"
fi

echo "=================================================="
echo "🤖 Maestro Automated Testing Suite"
echo "Maestro version: $(maestro --version 2>/dev/null | tail -n 1)"
echo "=================================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Check for running Android device or emulator
echo "🔍 Checking connected devices..."
ADB_DEVICES=$(adb devices 2>/dev/null | grep -v "List of devices" | grep "device$" || true)

if [ -z "$ADB_DEVICES" ]; then
    echo "⚠️ No running Android device or emulator detected via adb."
    echo "   If testing Android, start an emulator (e.g. 'emulator -avd <name> -no-window -no-audio &') or connect a device."
    echo "   If testing iOS, Maestro will automatically attach to active iOS Simulator."
else
    echo "✅ Android device(s) detected:"
    FIRST_DEVICE=$(echo "$ADB_DEVICES" | grep "emulator" | head -n 1 | awk '{print $1}')
    if [ -z "$FIRST_DEVICE" ]; then
        FIRST_DEVICE=$(echo "$ADB_DEVICES" | head -n 1 | awk '{print $1}')
    fi
    DEVICE_FLAG="--device $FIRST_DEVICE"
    echo "🎯 Targeting primary device: $FIRST_DEVICE"
    
    APK_PATH="$PROJECT_ROOT/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
    if [ ! -f "$APK_PATH" ]; then
        echo "📦 Building debug APK with Gradle..."
        ./gradlew :androidApp:assembleDebug
    fi
    
    echo "📲 Installing latest debug APK on $FIRST_DEVICE..."
    adb -s "$FIRST_DEVICE" install -r "$APK_PATH" || echo "Note: ADB install skipped or already up to date"
fi

TEST_TARGET="${1:-.maestro/flows/}"
REPORT_DIR="$PROJECT_ROOT/build/reports/maestro"
mkdir -p "$REPORT_DIR"

echo "🚀 Running Maestro test flow(s): $TEST_TARGET"
export MAESTRO_CLI_NO_ANALYTICS=true
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true

if [ -f "$TEST_TARGET" ]; then
    echo "Running single flow: $TEST_TARGET"
    maestro $DEVICE_FLAG test "$TEST_TARGET" --format junit --output "$REPORT_DIR/$(basename "$TEST_TARGET" .yaml).xml"
elif [ -d "$TEST_TARGET" ]; then
    echo "Running all flows in $TEST_TARGET sequentially..."
    TOTAL_PASSED=0
    TOTAL_FAILED=0
    for flow in "$TEST_TARGET"/*.yaml; do
        [ -e "$flow" ] || continue
        flow_name=$(basename "$flow" .yaml)
        echo "--------------------------------------------------"
        echo "▶️  Executing Flow: $flow_name"
        if maestro $DEVICE_FLAG test "$flow" --format junit --output "$REPORT_DIR/${flow_name}.xml"; then
            echo "✅  Passed: $flow_name"
            ((TOTAL_PASSED++))
        else
            echo "❌  Failed: $flow_name"
            ((TOTAL_FAILED++))
        fi
        sleep 2
    done
    echo "=================================================="
    echo "Test Summary: $TOTAL_PASSED Passed, $TOTAL_FAILED Failed"
    if [ "$TOTAL_FAILED" -gt 0 ]; then
        exit 1
    fi
else
    echo "❌ Specified test target not found: $TEST_TARGET"
    exit 1
fi

echo "=================================================="
echo "✅ Maestro Automated Tests Completed Successfully!"
echo "Report generated at: $REPORT_DIR/maestro-results.xml"
echo "=================================================="
