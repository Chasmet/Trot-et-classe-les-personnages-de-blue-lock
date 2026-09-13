#!/usr/bin/env sh
set -eu
GRADLE_VERSION=8.7
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/chk-wrapper/gradle-${GRADLE_VERSION}"
if [ ! -x "$BASE/bin/gradle" ]; then
  TMP="${TMPDIR:-/tmp}/gradle-${GRADLE_VERSION}-bin.zip"
  mkdir -p "$(dirname "$BASE")"
  curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$TMP"
  rm -rf "$BASE"
  unzip -q "$TMP" -d "$(dirname "$BASE")"
fi
exec "$BASE/bin/gradle" "$@"
