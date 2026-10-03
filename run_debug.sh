#!/usr/bin/env bash
set -eo pipefail

# ==============================================================================
# run_debug.sh - Kitab al-Huda : Build, Deploy & Live Logcat
#
# Description :
#   1. Détecte automatiquement l'appareil cible (smartphone USB ou émulateur).
#   2. Compile l'APK Debug avec le cache Gradle optimisé.
#   3. Installe l'APK sur l'appareil.
#   4. Lance l'application via son point d'entrée officiel (SplashActivity).
#   5. Stream directement le logcat en temps réel, filtré sur l'application.
#
# Utilisation :
#   ./run_debug.sh              # Détection automatique de l'appareil
#   ./run_debug.sh <DEVICE_ID>  # Cibler un appareil spécifique
# ==============================================================================

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_NAME="com.alfred.kitabalhuda"
LAUNCH_ACTIVITY=".SplashActivity"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
AVD_NAME="Pixel_5"
ANDROID_CLI="${ANDROID_CLI:-$HOME/.local/bin/android}"

# Palette de couleurs pour le terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${BLUE}${BOLD}══════════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}${BOLD}           📱 Kitab al-Huda — Fast Deploy & Live Logcat               ${NC}"
echo -e "${BLUE}${BOLD}══════════════════════════════════════════════════════════════════════${NC}"

# ------------------------------------------------------------------------------
# 1. Détection de l'appareil
# ------------------------------------------------------------------------------
DEVICE_ID="${1:-}"

if [[ -z "$DEVICE_ID" ]]; then
    mapfile -t CONNECTED_DEVICES < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
    mapfile -t UNAUTHORIZED_DEVICES < <(adb devices | awk 'NR>1 && $2=="unauthorized" {print $1}')

    if [[ ${#UNAUTHORIZED_DEVICES[@]} -gt 0 ]]; then
        echo -e "${RED}⚠️  Appareil connecté mais non autorisé (${UNAUTHORIZED_DEVICES[0]}).${NC}"
        echo -e "${YELLOW}👉 Déverrouillez votre écran et cochez 'Toujours autoriser ce PC'.${NC}"
        exit 1
    fi

    if [[ ${#CONNECTED_DEVICES[@]} -eq 1 ]]; then
        DEVICE_ID="${CONNECTED_DEVICES[0]}"
    elif [[ ${#CONNECTED_DEVICES[@]} -gt 1 ]]; then
        # Donner la priorité à un appareil physique branché en USB
        for dev in "${CONNECTED_DEVICES[@]}"; do
            if [[ ! "$dev" =~ ^emulator- ]]; then
                DEVICE_ID="$dev"
                break
            fi
        done
        # Si que des émulateurs, sélectionner le premier
        if [[ -z "$DEVICE_ID" ]]; then
            DEVICE_ID="${CONNECTED_DEVICES[0]}"
        fi
        echo -e "${CYAN}ℹ️  Plusieurs appareils détectés, sélection automatique : ${BOLD}$DEVICE_ID${NC}"
    else
        # Aucun appareil : démarrer l'émulateur
        echo -e "${YELLOW}Aucun appareil détecté. Démarrage de l'émulateur $AVD_NAME...${NC}"
        if command -v "$ANDROID_CLI" &>/dev/null; then
            "$ANDROID_CLI" emulator start "$AVD_NAME" > /dev/null 2>&1 &
        elif command -v emulator &>/dev/null; then
            emulator -avd "$AVD_NAME" > /dev/null 2>&1 &
        else
            echo -e "${RED}❌ Aucun appareil détecté et impossible de lancer un émulateur.${NC}"
            exit 1
        fi

        echo -e "Attente de disponibilité de l'émulateur (max 120s)..."
        ELAPSED=0
        while (( ELAPSED < 120 )); do
            sleep 2
            ELAPSED=$((ELAPSED + 2))
            DEVICE_ID=$(adb devices | awk 'NR>1 && $2=="device" {print $1}' | head -n 1)
            if [[ -n "$DEVICE_ID" ]]; then
                echo -e "${GREEN}Émulateur prêt : $DEVICE_ID${NC}"
                break
            fi
        done

        if [[ -z "$DEVICE_ID" ]]; then
            echo -e "${RED}❌ L'émulateur n'a pas répondu dans le délai imparti.${NC}"
            exit 1
        fi
    fi
fi

# Métadonnées sur la cible
MODEL=$(adb -s "$DEVICE_ID" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || echo "Appareil Android")
ANDROID_VER=$(adb -s "$DEVICE_ID" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r' || echo "?")
echo -e "${GREEN}🎯 Appareil cible : ${BOLD}$MODEL${NC} (Android $ANDROID_VER | Serial: $DEVICE_ID)"

# ------------------------------------------------------------------------------
# 2. Compilation de l'APK Debug
# ------------------------------------------------------------------------------
echo -e "\n${CYAN}📦 [1/4] Compilation de l'APK Debug (:app:assembleDebug)...${NC}"
cd "$PROJECT_ROOT"
./gradlew assembleDebug

if [[ ! -f "$APK_PATH" ]]; then
    echo -e "${RED}❌ Fichier APK introuvable : $APK_PATH${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 3. Installation sur l'appareil
# ------------------------------------------------------------------------------
echo -e "\n${CYAN}📲 [2/4] Installation de l'APK sur $MODEL...${NC}"
adb -s "$DEVICE_ID" install -r "$APK_PATH"

# ------------------------------------------------------------------------------
# 4. Lancement de l'application
# ------------------------------------------------------------------------------
echo -e "\n${CYAN}🚀 [3/4] Lancement de $PACKAGE_NAME...${NC}"

# Réinitialisation du buffer des logs pour ne voir que la session actuelle
adb -s "$DEVICE_ID" logcat -c

# Démarrage de l'activité Splash
adb -s "$DEVICE_ID" shell am start -n "$PACKAGE_NAME/$LAUNCH_ACTIVITY" > /dev/null

# Attendre que le processus démarre pour récupérer son PID
PID=""
for i in {1..25}; do
    PID=$( (adb -s "$DEVICE_ID" shell pidof "$PACKAGE_NAME" 2>/dev/null || true) | awk '{print $1}' | tr -d '\r' )
    if [[ -n "$PID" ]]; then
        break
    fi
    sleep 0.2
done

# ------------------------------------------------------------------------------
# 5. Streaming en direct du logcat filtré
# ------------------------------------------------------------------------------
echo -e "\n${CYAN}📑 [4/4] Logcat en direct (Ctrl+C pour arrêter)...${NC}"
echo -e "${BLUE}──────────────────────────────────────────────────────────────────────${NC}"

cleanup() {
    echo -e "\n${YELLOW}Arrêt du streaming du logcat. L'application reste active sur l'appareil.${NC}"
    exit 0
}
trap cleanup INT TERM

if [[ -n "$PID" ]]; then
    echo -e "${GREEN}✅ Processus connecté avec succès (PID: ${BOLD}$PID${NC}${GREEN}) — Filtre actif.${NC}\n"
    adb -s "$DEVICE_ID" logcat -v color --pid="$PID"
else
    echo -e "${YELLOW}⚠️ PID introuvable immédiatement. Affichage en mode filtrage par tag/nom :${NC}\n"
    adb -s "$DEVICE_ID" logcat -v color | grep --line-buffered -E "$PACKAGE_NAME|AndroidRuntime|FATAL EXCEPTION"
fi
