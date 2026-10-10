# SayTap

Aplicación Android de accesibilidad para personas con discapacidad auditiva. Facilita la comunicación cotidiana transformando el teléfono en un puente: **frases que se escuchan con un toque** y **voz que se lee en pantalla**.

> Proyecto de la asignatura Desarrollo de Aplicaciones Móviles (DSY2204) · Duoc UC Online.

## Funcionalidades

| Función | Descripción |
|---|---|
| **Escribir** (texto a voz) | Pad de frases que se reproducen en voz alta al tocarlas. Categorías, pestaña de frecuentes y CRUD de frases y categorías. |
| **Hablar** (voz a texto) | Reconocimiento de voz en español que muestra en pantalla lo que dice la otra persona y guarda las últimas 3 transcripciones. |
| **Buscar dispositivo** | Simulación para encontrar un audífono extraviado, con guía por vibración y animaciones en lugar de sonido. |
| **Ajustes** | Edición de perfil, tipo de voz y vibración, cierre de sesión y eliminación de cuenta con confirmación. |
| **Cuenta** | Registro con validaciones, inicio de sesión con "Recordarme" y recuperación de contraseña por correo. |
| **Ayuda** | Tutorial breve de uso desde el menú principal. |
| **Content Provider** | Expone las frases del usuario en solo lectura a otras aplicaciones. |

Además: tema claro/oscuro de alto contraste, selector de tamaño de texto y barra de navegación inferior.

## Tecnologías

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** con rutas type-safe (Kotlin Serialization)
- **Firebase Authentication** y **Realtime Database**
- **TextToSpeech**, **SpeechRecognizer** y **Palette** (`androidx.palette`)
- **Extensiones KTX** (`core-ktx`)
- **JUnit 4** para pruebas unitarias

## Estructura del proyecto

```
app/src/main/java/com/saytap/app/
├── MainActivity.kt
├── data/            Modelos y repositorios (Firebase, Content Provider)
├── navigation/      Rutas type-safe y grafo de navegación
├── ui/
│   ├── components/  Barra superior e inferior
│   ├── screens/     Login, Registro, Recuperar, Bienvenida, Escribir,
│   │                Hablar, Buscar dispositivo y Ajustes
│   └── theme/       Colores, tipografía y tema
└── util/            Validaciones y mensajes de error de Firebase
```

## Datos en Firebase

Realtime Database, con un nodo por usuario (`uid`) en cada colección:

```
usuarios/{uid}          perfil y preferencias
frases/{uid}/{id}       texto, categoría y cantidad de usos
categorias/{uid}/{id}   categorías del usuario
transcripciones/{uid}   últimas 3 transcripciones
```

## Instalación

### Desde el APK
1. Descarga `app-release.apk` desde [Releases](../../releases).
2. Permite la instalación de apps de origen desconocido en el teléfono.
3. Instala SayTap y regístrate o inicia sesión.

**Requisitos:** Android 6.0 (API 23) o superior y conexión a internet.

### Desde el código
1. Clona el repositorio y ábrelo en Android Studio.
2. Sincroniza Gradle y ejecuta la app en un dispositivo o emulador.

## Pruebas

Pruebas unitarias de la validación de contraseñas en `app/src/test/.../ExampleUnitTest.kt`:

```bash
./gradlew testDebugUnitTest
```

Consulta del Content Provider desde fuera de la app:

```bash
adb shell content query --uri content://com.saytap.app.provider
```

## Autor

**Sebastián Santander** · Ingeniería en Desarrollo de Software, Duoc UC Online
