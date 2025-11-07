@echo off
echo ========================================
echo Getting SHA-1 and SHA-256 Fingerprints
echo ========================================
echo.
echo This script will help you get the SHA-1 and SHA-256 fingerprints
echo needed for Firebase Authentication.
echo.
echo Make sure you have Java JDK installed and in your PATH.
echo.
pause

echo.
echo Getting debug keystore fingerprints...
echo.

REM Get SHA-1
echo SHA-1 Fingerprint:
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"

echo.
echo SHA-256 Fingerprint:
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA256"

echo.
echo ========================================
echo Instructions:
echo ========================================
echo 1. Copy the SHA-1 and SHA-256 fingerprints above
echo 2. Go to Firebase Console: https://console.firebase.google.com
echo 3. Select your project: chatgroupmanagemm
echo 4. Go to Project Settings (gear icon)
echo 5. Scroll down to "Your apps" section
echo 6. Find your Android app: com.prm392.taskmanaapp
echo 7. Click "Add fingerprint"
echo 8. Paste the SHA-1 fingerprint and click "Add"
echo 9. Click "Add fingerprint" again and paste the SHA-256 fingerprint
echo 10. Download the updated google-services.json file
echo 11. Replace app/google-services.json with the new file
echo 12. Rebuild your app
echo.
pause

