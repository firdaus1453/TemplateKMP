---
name: kmp-compose-ui
description: >
  Compose Multiplatform (CMP) UI guidelines, design system components, Coil3 image loading, Material3 tokens, and adaptive layouts across Android, iOS, and Desktop. Trigger when: creating or modifying @Composable UI functions, building reusable design system components, configuring AppTheme / MaterialTheme, loading multiplatform images with Coil3, or handling responsive screen sizes.
---

# Compose Multiplatform (CMP) UI & Design System

This document outlines Compose Multiplatform UI patterns, Design System structure, Material3 tokens, Coil3 image loading, and responsive layouts across Android, iOS, and Desktop.

---

## 🎨 Design System Architecture (`:core:designsystem`)

All visual styling, tokens, and reusable components live in `:core:designsystem`:

```
core/designsystem/
├── Color.kt              # Primary, secondary, surface, background color palettes
├── Theme.kt              # AppTheme, lightColorScheme, darkColorScheme
├── Type.kt               # Typography system (Display, Headline, Title, Body, Label)
└── components/
    ├── AppButton.kt      # Primary elevated & outlined buttons with loading states
    ├── AppTextField.kt   # Text inputs with label, error, and leading/trailing icons
    ├── AppTextButton.kt  # Plain text buttons
    └── LoadingIndicator.kt # Centralized loading animation
```

---

## 🌓 Multiplatform Theme Setup (`AppTheme`)

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 🖼️ Multiplatform Image Loading with Coil3

Use Coil 3.x (`coil3.compose`) for asynchronous image loading across Android, iOS, and Desktop:

```kotlin
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProductThumbnail(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp)),
    )
}
```

---

## 🧩 Reusable Component Standards

### 1. Primary Button with Loading State
```kotlin
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(8.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
```

### 2. Form Text Field with Error & Visibility Toggle
```kotlin
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    error: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth(),
    )
}
```

---

## 📐 Layout & Touch Target Best Practices

1. **Minimum Touch Targets**: Ensure interactive touch targets (buttons, icon clicks) are at least `48.dp x 48.dp`.
2. **Proper Spacing**: Use standard 4dp/8dp grid spacing (`4.dp`, `8.dp`, `12.dp`, `16.dp`, `24.dp`, `32.dp`).
3. **Safe Area & Insets**: Always use `Scaffold` and respect `paddingValues` to adapt to notches and status bars on Android & iOS.
