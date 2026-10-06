# Fix KSP – KokoroFy APK Build

## Qué fallaba
```
Plugin [id: 'com.google.devtools.ksp'] was not found
```

## Cómo aplicar el fix

1. Copia estos 4 archivos a la **raíz** de tu repo `KokoroFy-V1`:

   - `build.gradle.kts`          ← aquí está el fix principal
   - `settings.gradle.kts`
   - `gradle.properties`
   - `app/build.gradle.kts`
   - `.github/workflows/android.yml` (opcional, ya está bien)

2. Commit + push a `main`:

```bash
git add build.gradle.kts settings.gradle.kts gradle.properties app/build.gradle.kts
git commit -m "fix: add KSP plugin version for Room"
git push origin main
```

3. Ve a **Actions** → el workflow se ejecuta solo.

## Cambio clave

En `build.gradle.kts` (raíz) se agregó:

```kotlin
id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
```

Compatible con Kotlin 1.9.24 que ya tenías.
