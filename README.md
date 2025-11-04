# 📱 ProAhorro - Aplicación de Gestión Financiera Personal

## 📋 Descripción
ProAhorro es una aplicación Android desarrollada en Kotlin que permite a los usuarios gestionar sus finanzas personales de manera eficiente. Incluye funcionalidades como registro de ingresos y gastos, presupuestos por categorías, metas de ahorro, y estadísticas detalladas.

## ⚙️ Requisitos previos para compilar el proyecto

### 1. Herramientas necesarias
- **Android Studio** (versión Hedgehog 2023.1.1 o superior)
- **JDK 11** o superior
- **SDK de Android** con:
  - Android SDK Platform 36 (API Level 36)
  - Android SDK Build-Tools 34.0.0 o superior
  - Android SDK Platform-Tools
  - Android Emulator (opcional, para pruebas)

### 2. Configuración de Firebase

Este proyecto utiliza Firebase Authentication para la gestión de usuarios. Para que la aplicación compile correctamente, necesitas:

#### Opción A: Usar el archivo google-services.json proporcionado
1. Coloca el archivo `google-services.json` en la carpeta `app/`
2. La estructura debe quedar así:
   ```
   losZETAS/
   ├── app/
   │   ├── google-services.json  ← Aquí
   │   ├── src/
   │   ├── build.gradle.kts
   │   └── ...
   └── ...
   ```

#### Opción B: Crear tu propio proyecto Firebase (opcional)
Si prefieres usar tu propia configuración de Firebase:

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Agrega una aplicación Android con el ID: `com.daniel.loszetas`
4. Descarga el archivo `google-services.json`
5. Colócalo en `app/google-services.json`
6. Habilita **Authentication > Sign-in method > Email/Password** en Firebase Console

## 🚀 Pasos para compilar el proyecto

### Desde Android Studio (Recomendado)

1. **Clonar/Descargar el proyecto**
   ```bash
   git clone <url-del-repositorio>
   cd losZETAS
   ```

2. **Abrir el proyecto en Android Studio**
   - Abre Android Studio
   - Selecciona "Open" o "File > Open"
   - Navega hasta la carpeta del proyecto y selecciónala
   - Espera a que Gradle sincronice (puede tardar algunos minutos la primera vez)

3. **Agregar google-services.json**
   - Copia el archivo `google-services.json` a la carpeta `app/`
   - El archivo debe estar al mismo nivel que `build.gradle.kts`

4. **Sincronizar Gradle**
   - Si no se sincroniza automáticamente, haz clic en "Sync Now" en la barra superior
   - O ve a: File > Sync Project with Gradle Files

5. **Compilar el proyecto**
   - Haz clic en **Build > Make Project** o presiona `Ctrl+F9` (Windows/Linux) o `Cmd+F9` (Mac)
   - Espera a que termine la compilación

6. **Ejecutar la aplicación**
   - Conecta un dispositivo Android o inicia un emulador
   - Haz clic en el botón **Run** (▶️) o presiona `Shift+F10`
   - Selecciona el dispositivo de destino

### Desde línea de comandos

1. **Clonar el proyecto**
   ```bash
   git clone <url-del-repositorio>
   cd losZETAS
   ```

2. **Agregar google-services.json**
   ```bash
   cp google-services.json app/
   ```

3. **Compilar el proyecto**
   ```bash
   # En Windows
   gradlew.bat assembleDebug

   # En Linux/Mac
   ./gradlew assembleDebug
   ```

4. **Instalar en dispositivo conectado**
   ```bash
   # En Windows
   gradlew.bat installDebug

   # En Linux/Mac
   ./gradlew installDebug
   ```

El APK compilado estará en: `app/build/outputs/apk/debug/app-debug.apk`

## 📂 Estructura del proyecto

```
losZETAS/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/daniel/loszetas/
│   │   │   │   ├── data/              # Capa de datos
│   │   │   │   │   ├── dao/           # Data Access Objects (Room)
│   │   │   │   │   ├── database/      # Configuración de Room Database
│   │   │   │   │   └── entities/      # Entidades de la base de datos
│   │   │   │   ├── utils/             # Utilidades (ConfiguracionApp)
│   │   │   │   └── *.kt               # Activities y Adapters
│   │   │   ├── res/                   # Recursos (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── ...
│   ├── google-services.json           # ← Archivo de Firebase (NECESARIO)
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## 🔧 Dependencias principales

- **Firebase Authentication**: Gestión de usuarios
- **Room Database**: Base de datos local SQLite
- **Material Design Components**: UI moderna
- **Kotlin Coroutines**: Programación asíncrona
- **View Binding**: Acceso seguro a vistas

## ⚠️ Solución de problemas comunes

### Error: "google-services.json is missing"
**Solución**: Asegúrate de que el archivo `google-services.json` está en la carpeta `app/` y no en otra ubicación.

### Error: "Failed to resolve: com.google.firebase:firebase-auth"
**Solución**: 
1. Verifica tu conexión a internet
2. En Android Studio: File > Invalidate Caches > Invalidate and Restart
3. Sincroniza Gradle nuevamente

### Error de compilación por versión de SDK
**Solución**: Asegúrate de tener instalado el SDK 36. Ve a Tools > SDK Manager y verifica que esté instalado.

### La app se cierra al iniciar (Firebase no configurado)
**Solución**: Verifica que el archivo `google-services.json` esté en la ubicación correcta y que coincida con el `applicationId` del proyecto (`com.daniel.loszetas`).

## 📱 Funcionalidades principales

- ✅ Registro e inicio de sesión con Firebase
- ✅ Registro de ingresos y gastos
- ✅ Categorización de transacciones
- ✅ Presupuestos por categoría (mensual, semanal, anual)
- ✅ Metas de ahorro con seguimiento de progreso
- ✅ Estadísticas detalladas (por mes, trimestre, año)
- ✅ Historial completo con filtros
- ✅ Soporte multi-moneda (CLP, BOB, USD, EUR, etc.)
- ✅ Base de datos local con Room

## 👤 Gestión de cuenta

- Cambiar contraseña
- Cerrar sesión
- Eliminar cuenta

## 📊 Tecnologías utilizadas

- **Lenguaje**: Kotlin
- **UI**: XML + Material Design 3
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Base de datos**: Room (SQLite)
- **Backend**: Firebase Authentication
- **Async**: Kotlin Coroutines + Flow
- **DI**: Manual (sin frameworks)

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 👥 Contacto

Para preguntas o soporte, contacta a: [tu-email@ejemplo.com]

---

**Nota importante**: Este proyecto requiere el archivo `google-services.json` para compilar correctamente. Asegúrate de tenerlo antes de intentar compilar la aplicación.
