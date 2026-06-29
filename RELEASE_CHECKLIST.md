# Release Checklist

## Pre-release
- [ ] Bump `versionCode` and `versionName` in `app/build.gradle.kts`
- [ ] Update `CHANGELOG.md` with new version entry
- [ ] Update `RELEASE_CHECKLIST.md` with any new tasks
- [ ] Run `./gradlew :app:testDebugUnitTest`
- [ ] Run `./gradlew :app:lintDebug`

## Build
- [ ] Run `./gradlew :app:bundleRelease :app:assembleRelease`
- [ ] Copy & rename outputs: `kitab-al-huda-<version>.aab`, `.apk`, `mapping-<version>.txt`
- [ ] Verify APK size (target < 10 MB for Play Store)

## Signing
- [ ] Upload key (`release.jks`) is backed up
- [ ] Keystore info saved in `Kitab-al-Huda-Keystore Info.txt`

## Release
- [ ] Git tag: `git tag -a v<version> -m "Version <version>" && git push origin v<version>`
- [ ] GitHub Release created with `.aab`, `.apk`, `mapping.txt` attached

## Play Console
- [ ] Upload AAB to Play Console (Internal/Closed/Open track)
- [ ] Data Safety form completed
- [ ] Content Rating questionnaire completed
- [ ] App description + screenshots in Arabic
- [ ] Release notes in Arabic

## Post-release
- [ ] Install test on real device
- [ ] Monitor crash reports (Play Console + mapping.txt)
- [ ] Verify zero-rated CDN functionality
- [ ] Mark release as ready for production
