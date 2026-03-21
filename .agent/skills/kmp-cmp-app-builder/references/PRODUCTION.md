# Production & Release Reference

Complete guide for taking a KMP/CMP app from development to production release.

## 1. ProGuard / R8 Rules

Create `composeApp/proguard-rules.pro`:

```proguard
# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mycompany.myapp.**$$serializer { *; }
-keepclassmembers class com.mycompany.myapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.mycompany.myapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# Keep Koin
-keep class org.koin.** { *; }

# Keep BuildKonfig
-keep class **.BuildKonfig { *; }
```

Enable in `composeApp/build.gradle.kts` (Android block):

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## 2. App Signing

### Android keystore

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

Add to `local.properties` (git-ignored):

```properties
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=your_password
RELEASE_KEY_ALIAS=release
RELEASE_KEY_PASSWORD=your_password
```

Configure in `composeApp/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        val localProperties = Properties()
        rootProject.file("local.properties").inputStream().use { localProperties.load(it) }

        create("release") {
            storeFile = file(localProperties.getProperty("RELEASE_STORE_FILE"))
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### iOS signing

Configure in Xcode: Team, Bundle Identifier, Provisioning Profile in Signing & Capabilities.

---

## 3. Release Build Commands

```bash
# Android — Release AAB (for Play Store)
./gradlew :composeApp:bundleRelease

# Android — Release APK
./gradlew :composeApp:assembleRelease

# Desktop — Native distribution
./gradlew :composeApp:packageDistributionForCurrentOS

# Desktop — Platform-specific
./gradlew :composeApp:packageDmg    # macOS
./gradlew :composeApp:packageMsi    # Windows
./gradlew :composeApp:packageDeb    # Linux

# iOS — Build via Xcode or:
xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -configuration Release
```

---

## 4. CI/CD with GitHub Actions

Create `.github/workflows/build.yml`:

```yaml
name: Build & Test

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]

jobs:
  build:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Create local.properties
        run: |
          echo "API_KEY=${{ secrets.API_KEY }}" >> local.properties
          echo "BASE_URL=${{ secrets.BASE_URL }}" >> local.properties

      - name: Build
        run: ./gradlew build

      - name: Run tests
        run: ./gradlew allTests

      - name: Coverage report
        run: ./gradlew koverHtmlReport

      - name: Verify coverage
        run: ./gradlew koverVerify

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: kover-report
          path: build/reports/kover/html/

  release-android:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Decode keystore
        run: echo "${{ secrets.RELEASE_KEYSTORE_BASE64 }}" | base64 --decode > release.jks

      - name: Create local.properties
        run: |
          echo "API_KEY=${{ secrets.API_KEY }}" >> local.properties
          echo "RELEASE_STORE_FILE=release.jks" >> local.properties
          echo "RELEASE_STORE_PASSWORD=${{ secrets.RELEASE_STORE_PASSWORD }}" >> local.properties
          echo "RELEASE_KEY_ALIAS=${{ secrets.RELEASE_KEY_ALIAS }}" >> local.properties
          echo "RELEASE_KEY_PASSWORD=${{ secrets.RELEASE_KEY_PASSWORD }}" >> local.properties

      - name: Build Release AAB
        run: ./gradlew :composeApp:bundleRelease

      - name: Upload AAB
        uses: actions/upload-artifact@v4
        with:
          name: release-aab
          path: composeApp/build/outputs/bundle/release/*.aab
```

---

## 5. Performance Optimization

| Area | Optimization |
|------|-------------|
| **Image loading** | Use Coil with disk cache + memory cache. Set `crossfade(true)` for smooth transitions. |
| **Lazy lists** | Use `LazyColumn`/`LazyRow` with `key` parameter. Never use `Column` with `forEach` for large lists. |
| **State management** | Use `derivedStateOf` for computed values. Avoid recomposing entire screens on small state changes. |
| **Network** | Enable response caching in Ktor. Use pagination for large data sets. |
| **Database** | Add indexes to frequently queried columns. Use `@Transaction` for complex queries. |
| **Startup** | Use splash screen to defer heavy initialization. Init Koin lazily where possible. |
| **Memory** | Cancel coroutines in `onCleared()`. Avoid holding `Context` references in ViewModels. |
| **Build speed** | Enable Gradle build cache, configuration cache, and parallel execution in `gradle.properties`. |

---

## 6. Pre-release Checklist

- [ ] All unit tests pass: `./gradlew allTests`
- [ ] Coverage meets minimum: `./gradlew koverVerify`
- [ ] ProGuard/R8 rules configured and tested with release build
- [ ] Release signing configured (keystore / provisioning profile)
- [ ] `local.properties` secrets are NOT committed
- [ ] Firebase config files are in place but git-ignored
- [ ] Chucker uses `no-op` variant in release builds
- [ ] All debug logs reviewed (no sensitive data in Timber/Kermit calls)
- [ ] Deep links tested on all platforms
- [ ] App version/code bumped in `libs.versions.toml`
- [ ] Release build tested on physical devices (Android + iOS)
- [ ] Desktop native distribution tested
- [ ] CI/CD pipeline green on main branch
- [ ] Play Store / App Store metadata prepared (screenshots, description)
