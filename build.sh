#!/usr/bin/env bash
# Manual build (no Gradle/AGP): aapt2 -> javac -> r8 -> package dex+jni -> zipalign -> apksigner
set -euo pipefail
cd "$(dirname "$0")"

if [[ -z "${JAVA_HOME:-}" ]]; then
    if [[ -x "$HOME/.local/jdk25/bin/javac" ]]; then
        export JAVA_HOME="$HOME/.local/jdk25"
        export PATH="$JAVA_HOME/bin:$PATH"
    elif [[ -x "$HOME/.local/jdk21/bin/javac" ]]; then
        export JAVA_HOME="$HOME/.local/jdk21"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

if [[ -n "${ANDROID_HOME:-}" ]]; then
    SDK="$ANDROID_HOME"
elif [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    SDK="$ANDROID_SDK_ROOT"
elif [[ -d "$HOME/Android/Sdk" ]]; then
    SDK="$HOME/Android/Sdk"
else
    SDK="$HOME/Library/Android/sdk"
fi

BT="$SDK/build-tools/36.0.0"
AJ="$SDK/platforms/android-36/android.jar"
OUT=build
APK=pixel-ims-apn-tuner.apk
MIN=31
TARGET=34

rm -rf "$OUT"; mkdir -p "$OUT/classes"
LIBS=$(ls libs/*.jar | tr '\n' ':'); LIBS=${LIBS%:}

echo "[1/6] aapt2 compile + link"
"$BT/aapt2" compile --dir res -o "$OUT/res.zip"
"$BT/aapt2" link -I "$AJ" \
  --manifest AndroidManifest.xml \
  --min-sdk-version $MIN --target-sdk-version $TARGET \
  "$OUT/res.zip" \
  --java "$OUT/gen" \
  -o "$OUT/base.apk"

echo "[2/6] javac"
javac --release 17 -nowarn -cp "$AJ:$LIBS" -d "$OUT/classes" $(find src "$OUT/gen" -name '*.java')

echo "[3/6] r8 (shrink + dex)"
java -cp "$BT/lib/d8.jar" com.android.tools.r8.R8 \
  --release \
  --dex \
  --min-api $MIN \
  --lib "$AJ" \
  --pg-conf proguard-rules.pro \
  --output "$OUT" \
  $(find "$OUT/classes" -name '*.class') libs/*.jar
ls "$OUT"/classes*.dex

echo "[4/6] package dex"
( cd "$OUT" && zip -9q base.apk classes*.dex )

echo "[5/6] zipalign"
"$BT/zipalign" -f -p 4 "$OUT/base.apk" "$OUT/aligned.apk"

echo "[6/6] sign"
KS_PATH="${KEYSTORE_PATH:-$HOME/.android/pixel-ims-apn-tuner.keystore}"
KS_PASS_FILE="${KEYSTORE_PASS_FILE:-$HOME/.android/pixel-ims-apn-tuner.ks.pass}"
KS_ALIAS="${KEY_ALIAS:-pixelimsapntuner}"
if [[ -z "${KEYSTORE_PASS:-}" ]]; then
    if [[ ! -f "$KS_PASS_FILE" ]]; then
        mkdir -p "$(dirname "$KS_PASS_FILE")"
        umask 077
        head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 32 > "$KS_PASS_FILE"
    fi
    KEYSTORE_PASS="$(cat "$KS_PASS_FILE")"
fi
if [[ ! -f "$KS_PATH" ]]; then
    mkdir -p "$(dirname "$KS_PATH")"
    umask 077
    "${JAVA_HOME:+$JAVA_HOME/bin/}keytool" -genkeypair \
      -keystore "$KS_PATH" \
      -storetype PKCS12 \
      -storepass "$KEYSTORE_PASS" \
      -keypass "$KEYSTORE_PASS" \
      -alias "$KS_ALIAS" \
      -keyalg RSA -keysize 2048 -validity 10000 \
      -dname "CN=Pixel IMS APN Tuner, O=PixelIMS, C=US" >/dev/null 2>&1
    chmod 600 "$KS_PATH"
fi
rm -f "$APK"
"$BT/apksigner" sign \
  --ks "$KS_PATH" \
  --ks-pass "pass:$KEYSTORE_PASS" --key-pass "pass:${KEY_PASS:-$KEYSTORE_PASS}" --ks-key-alias "$KS_ALIAS" \
  --out "$APK" "$OUT/aligned.apk" 2>/dev/null
"$BT/apksigner" verify --print-certs "$APK"
echo "Done -> $(pwd)/$APK ($(wc -c < "$APK" | tr -d ' ') bytes)"
