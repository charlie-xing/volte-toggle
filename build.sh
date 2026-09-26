#!/usr/bin/env bash
# 手工构建（不依赖 Gradle/AGP）：aapt2 -> javac -> d8 -> 打包 jni -> zipalign -> apksigner
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
BT="$SDK/build-tools/36.0.0"
AJ="$SDK/platforms/android-36/android.jar"
OUT=build
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

echo "[3/6] d8"
"$BT/d8" --lib "$AJ" --min-api $MIN --output "$OUT" \
  $(find "$OUT/classes" -name '*.class') libs/*.jar >/dev/null 2>&1 || \
"$BT/d8" --lib "$AJ" --min-api $MIN --output "$OUT" \
  $(find "$OUT/classes" -name '*.class') libs/*.jar
ls "$OUT"/classes*.dex

echo "[4/6] 打包 dex + jni"
mkdir -p "$OUT/pkg/lib/arm64-v8a"
cp libs/x_spake2/jni/arm64-v8a/libspake2.so "$OUT/pkg/lib/arm64-v8a/"
( cd "$OUT" && zip -q base.apk classes*.dex && cd pkg && zip -qr ../base.apk lib )

echo "[5/6] zipalign"
"$BT/zipalign" -f -p 4 "$OUT/base.apk" "$OUT/aligned.apk"

echo "[6/6] sign"
rm -f volte-toggle.apk
"$BT/apksigner" sign \
  --ks "$HOME/.android/debug.keystore" \
  --ks-pass pass:android --key-pass pass:android --ks-key-alias androiddebugkey \
  --out volte-toggle.apk "$OUT/aligned.apk" 2>/dev/null
echo "完成 -> $(pwd)/volte-toggle.apk ($(stat -f%z volte-toggle.apk) bytes)"
