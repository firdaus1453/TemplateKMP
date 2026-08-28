---
name: kmp-security
description: >
  Security and secrets management in KMP projects using BuildKonfig, encrypted session storage, Certificate Pinning in Ktor, and ProGuard / R8 rules. Trigger when: managing API keys, storing authentication tokens / credentials securely, configuring SSL pinning, or reviewing ProGuard rules.
---

# Security & Secrets Management in KMP

This document defines security standards for handling API keys via `BuildKonfig`, persisting tokens securely via `SessionStorage`, and pinning certificates in Ktor.

---

## 🔑 BuildKonfig & Secrets Management

API keys, base URLs, and environment secrets are loaded at compile time from `local.properties` via the `template.buildkonfig` convention plugin:

```properties
# local.properties (NEVER commit to Git)
BASE_URL=https://dummyjson.com/
API_KEY=your_secret_api_key_here
```

### Accessing in Kotlin Code
```kotlin
import com.template.project.core.data.BuildKonfig

val apiUrl = BuildKonfig.BASE_URL
val apiKey = BuildKonfig.API_KEY
```

---

## 🔐 Secure Session Storage (`SessionStorage`)

User tokens must be stored through the `SessionStorage` interface:

```kotlin
data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
)

interface SessionStorage {
    suspend fun get(): AuthInfo?
    suspend fun set(info: AuthInfo?)
}
```

Implementations use multiplatform DataStore with encryption or platform-specific secure storage (Android EncryptedSharedPreferences / Keychain on iOS).

---

## 🛡️ SSL & Certificate Pinning in Ktor

When interacting with production backends, configure public key hashes or certificate validation on the platform engine:

```kotlin
// Example SHA-256 Public Key Pinning configuration in Ktor HttpClientFactory
```
