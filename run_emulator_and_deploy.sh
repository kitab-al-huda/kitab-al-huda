#!/usr/bin/env bash
# Redirection vers le script unifié run_debug.sh
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$DIR/run_debug.sh" "$@"
