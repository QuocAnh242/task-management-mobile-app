# Firebase Configuration Package

This package contains Firebase configuration and initialization code.

## Files

- **FirebaseApplication.java**: Custom Application class that initializes Firebase when the app starts
- **FirebaseConfig.java**: Helper class for Firebase configuration and initialization

## Setup

The `FirebaseApplication` class is automatically used by Android (configured in AndroidManifest.xml).

## Configuration Requirements

For Firebase Authentication to work properly, you need to:

1. **Add SHA-1 and SHA-256 fingerprints** to Firebase Console
   - Go to Firebase Console → Project Settings → Your apps
   - Find your Android app (com.prm392.taskmanaapp)
   - Click "Add fingerprint" and add both SHA-1 and SHA-256

2. **Download updated google-services.json**
   - After adding fingerprints, download the new google-services.json
   - Replace app/google-services.json with the new file

3. **Enable Email/Password Authentication**
   - Go to Firebase Console → Authentication → Sign-in method
   - Enable "Email/Password" provider

## Troubleshooting

If you see `CONFIGURATION_NOT_FOUND` error:
- Check that SHA fingerprints are added in Firebase Console
- Verify google-services.json has oauth_client entries (not empty array)
- Make sure Email/Password authentication is enabled
- Wait a few minutes after adding fingerprints for changes to propagate

