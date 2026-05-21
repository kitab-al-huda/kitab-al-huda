#!/usr/bin/env bash
set -e

# ------------------------------------------------------------
# run_emulator_and_deploy.sh
#   • Compile the debug APK
#   • Start the Pixel_5 AVD if not already running
#   • Install the APK on the emulator
#   • Launch the main activity
#   • Optionally stream logcat
# ------------------------------------------------------------

# ---- Configuration ---------------------------------------------------
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_CLI="$HOME/.local/bin/android"
AVD_NAME="Pixel_5"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.alfred.kitabalhuda"
MAIN_ACTIVITY=".MainActivity"
TIMEOUT=120   # seconds to wait for the emulator to become ready

# ---- Helper ----------------------------------------------------------
function get_emulator_device() {
  adb devices | awk 'NR>1 && $2=="device" {print $1}'
}

# ---- 1. Build APK ----------------------------------------------------
echo "[1/5] Building debug APK..."
cd "$PROJECT_ROOT"
./gradlew assembleDebug --no-daemon

# ---- 2. Ensure AVD is running ----------------------------------------
DEVICE_ID=$(get_emulator_device)
if [[ -z "$DEVICE_ID" ]]; then
  echo "[2/5] Starting AVD $AVD_NAME..."
  "$ANDROID_CLI" emulator start "$AVD_NAME" > /dev/null 2>&1 &
  echo "   Waiting for emulator to be online (max $TIMEOUT s)..."
  ELAPSED=0
  while [[ $ELAPSED -lt $TIMEOUT ]]; do
    sleep 2
    ELAPSED=$((ELAPSED+2))
    DEVICE_ID=$(get_emulator_device)
    if [[ -n "$DEVICE_ID" ]]; then
      echo "   Emulator ready (device $DEVICE_ID)."
      break
    fi
  done
  if [[ -z "$DEVICE_ID" ]]; then
    echo "✖ Emulator did not become ready within $TIMEOUT seconds."
    exit 1
  fi
else
  echo "[2/5] Emulator already running (device $DEVICE_ID)."
fi

# ---- 3. Install APK -------------------------------------------------
echo "[3/5] Installing APK..."
adb -s "$DEVICE_ID" install -r "$APK_PATH"

# ---- 4. Launch app --------------------------------------------------
echo "[4/5] Launching application..."
adb -s "$DEVICE_ID" shell am start -n "${PACKAGE_NAME}/${MAIN_ACTIVITY}"

# ---- 5. Optional logcat ---------------------------------------------
read -p "[5/5] Stream logcat now? (y/N) " -n1 answer
echo
if [[ "$answer" =~ ^[Yy]$ ]]; then
  echo "--- logcat (Ctrl+C to stop) ---"
  adb -s "$DEVICE_ID" logcat
fi

echo "✅ Script finished successfully."

set -euo pipefail

# ------------------------------------------------------------
# run_emulator_and_deploy.sh
#   • Build the debug APK
#   • Start Pixel_5 AVD if needed
#   • Install the APK on the emulator
#   • Launch the main activity
#   • Optionally stream logcat
# ------------------------------------------------------------

# ---- Configuration ------------------------------------------------
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_CLI="$HOME/.local/bin/android"
AVD_NAME="Pixel_5"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.alfred.kitabalhuda"
MAIN_ACTIVITY=".MainActivity"
TIMEOUT=120   # seconds to wait for emulator to become ready

# ---- Helper -------------------------------------------------------
get_emulator_device() {
  adb devices | awk 'NR>1 && $2=="device" {print $1}'
}

# ---- 1. Build APK -------------------------------------------------
echo "[1/5] Building debug APK…"
cd "$PROJECT_ROOT"
./gradlew assembleDebug --no-daemon

# ---- 2. Ensure AVD is running ------------------------------------
DEVICE_ID=$(get_emulator_device || true)
if [[ -z "$DEVICE_ID" ]]; then
  echo "[2/5] Starting AVD $AVD_NAME…"
  "$ANDROID_CLI" emulator start "$AVD_NAME" > /dev/null 2>&1 &
  echo "   Waiting for emulator to be online (max $TIMEOUT s)…"
  ELAPSED=0
  while (( ELAPSED < TIMEOUT )); do
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    DEVICE_ID=$(get_emulator_device || true)
    if [[ -n "$DEVICE_ID" ]]; then
      echo "   Emulator ready (device $DEVICE_ID)."
      break
    fi
  done
  if [[ -z "$DEVICE_ID" ]]; then
    echo "✖ Emulator did not become ready within $TIMEOUT seconds."
    exit 1
  fi
else
  echo "[2/5] Emulator already running (device $DEVICE_ID)."
fi

# ---- 3. Install APK ------------------------------------------------
echo "[3/5] Installing APK…"
adb -s "$DEVICE_ID" install -r "$APK_PATH"

# ---- 4. Launch app -------------------------------------------------
echo "[4/5] Launching application…"
adb -s "$DEVICE_ID" shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"

# ---- 5. Optional logcat --------------------------------------------
read -p "[5/5] Stream logcat now? (y/N) " -n1 answer
echo
if [[ "$answer" =~ ^[Yy]$ ]]; then
  echo "--- logcat (Ctrl+C to stop) ---"
  adb -s "$DEVICE_ID" logcat
fi

echo "✅ Script finished successfully."
