# LovePlay (Android APK)

Aplikasi couple yang simple, seru, dan interaktif. Fitur:
- Spin the Wheel (ide kencan)
- Truth or Dare
- Love Counter (hitung tap hati per pasangan)
- Settings (nama pasangan + warna tema)

Teknologi: Kotlin + Jetpack Compose + Material 3. Build otomatis APK via GitHub Actions.

## Struktur
- app/src/main/java/com/loveplay/MainActivity.kt — UI Compose (seluruh fitur)
- .github/workflows/android.yml — CI untuk build APK dan rilis

## Cara pakai (GitHub)
1) Buat repository di GitHub (misal: LovePlay).
2) Di terminal, jalankan:

```bash path=null start=null
# inisialisasi git (jika belum)
cd /home/thecoderscients/LovePlay
git init

# tambah semua file dan commit awal
git add .
git commit -m "LovePlay: init project"

# hubungkan remote dan push
# Ganti URL di bawah dengan repo kamu (contoh HTTPS)
# git remote add origin https://github.com/<username>/LovePlay.git
# atau jika pakai gh CLI (opsional):
# gh repo create LovePlay --public --source . --push

# push (jika remote sudah ditambahkan)
# git push -u origin main
```

3) Build APK via GitHub Actions
- Masuk tab Actions > workflow "Android CI" > Run workflow (workflow_dispatch).
- Setelah sukses, unduh APK dari artifact bernama "LovePlay-debug-apk".

4) Rilis APK (opsional, via tag)
- Buat tag versi dan push:

```bash path=null start=null
cd /home/thecoderscients/LovePlay
git tag v1.0.0
git push origin v1.0.0
```
- Workflow akan membuat GitHub Release berisi APK (debug) yang bisa diunduh publik.

Catatan: APK yang dibuild adalah debug APK (sudah signed dengan debug keystore). Untuk rilis ke Play Store, perlu signing release dan konfigurasi lebih lanjut.

## Build Lokal (opsional)
Jika ingin build di lokal, siapkan:
- Java 17 (Temurin/OpenJDK)
- Android SDK (platform 34, build-tools 34.0.0)
- Gradle 8.7 (atau gunakan wrapper jika ditambahkan nanti)

Contoh (setelah SDK siap dan `gradle` tersedia di PATH):

```bash path=null start=null
cd /home/thecoderscients/LovePlay
gradle assembleDebug
ls app/build/outputs/apk/debug/app-debug.apk
```

Selamat bersenang-senang dengan LovePlay! 💕
