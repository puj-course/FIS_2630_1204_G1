# 🚀 Guía de Despliegue y Ejecución - CareStock (Prototipo Entrega 1)

Esta guía detalla los pasos necesarios para configurar el entorno local, compilar y ejecutar el primer incremento funcional del sistema **CareStock**, conectando la interfaz gráfica de JavaFX con la base de datos PostgreSQL en la nube (Neon DB).

---

## 📋 Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado en tu equipo:
1. **Java Development Kit (JDK 21):** Asegúrate de que esté correctamente configurado en las variables de entorno de tu sistema.
2. **JavaFX SDK 21:** Descargado y descomprimido localmente en tu equipo (necesario para la interfaz gráfica).
3. **Driver JDBC de PostgreSQL:** El archivo `postgresql-driver.jar` debe estar ubicado en la raíz del proyecto para permitir la persistencia de datos.
4. **Conexión a Internet:** Requerida para que el prototipo pueda consultar y escribir en la base de datos de Neon DB.

---

## 🛠️ Configuración de Variables de Entorno (Opcional)

Si el prototipo lee la cadena de conexión mediante variables de entorno, asegúrate de configurarlas en tu terminal o sistema operativo:
* `DB_URL`: URL de conexión a tu base de datos PostgreSQL (Neon DB).
* `DB_USER`: Usuario de la base de datos.
* `DB_PASSWORD`: Contraseña de acceso.

*(Nota: Si las credenciales están quemadas directamente en el DAO de prueba, puedes omitir este paso).*

---

## ▶️ Instrucciones de Ejecución

Abre tu terminal (por ejemplo, Git Bash o CMD) posicionándote en la raíz del repositorio y sigue estos pasos:

### 1. Definir la ruta de JavaFX
Configura la variable `JAVAFX_LIB` apuntando a la carpeta `lib` de tu SDK de JavaFX descargado. 
*(Ejemplo en Git Bash / Linux):*
```bash
export JAVAFX_LIB="/ruta/a/javafx-sdk-21/lib"