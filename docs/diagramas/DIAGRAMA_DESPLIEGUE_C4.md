# CareStock - Diagrama de despliegue C4

**Tipo:** diagrama de despliegue (vista complementaria al C4 estático)  
**Sistema:** CareStock, aplicación JavaFX 21 / Java 21  
**Escenarios:** ejecución local habitual y ejecución Docker opcional.

## Alcance

Se muestran **instancias ejecutadas** y los nodos que las alojan. No se presenta GitHub, Maven ni GitHub Actions como servidores de producción: pertenecen al proceso de construcción y entrega. Neon aloja la base PostgreSQL y **no** ejecuta una API de CareStock.
<img width="872" height="2394" alt="Diagrama De Despliegue " src="https://github.com/user-attachments/assets/27783537-ca13-47a6-8984-d95318ddd4f4" />


Figura 1. Vista de Despliegue del sistema CareStock, elaborada en Visual Paradigm Online.

## Nodos, tecnología y artefactos

| Nodo | Tecnología | Qué contiene o ejecuta |
|---|---|---|
| Equipo del usuario | Sistema operativo con Java 21 y JavaFX 21 | Aplicación de escritorio CareStock |
| JVM | Java 21 | Controladores, servicios, modelos, DAO, HikariCP y caché en memoria |
| Nube Neon | PostgreSQL administrado | Base de datos persistente |
| Docker Engine (opcional) | Docker / Compose | Contenedor construido desde `src/CareStock/Dockerfile` |
| Contenedor Docker (opcional) | Eclipse Temurin 21, Xvfb | Ejecución JavaFX sobre display virtual |

## Conexiones y configuración

- **Aplicación a Neon:** JDBC con protocolo PostgreSQL, puerto típico **5432**, con TLS exigido mediante `sslmode=require` en la URL de ejemplo.
- **Credenciales y parámetros:** archivo local `src/CareStock/config/conexion-local.properties`, leído por `ConexionBD.java`. Las claves son `db.url`, `db.user`, `db.password`, `db.pool.max` y `db.pool.min`.
- **Configuración de referencia:** `src/CareStock/config/conexion-local.properties.example`. Nunca versionar credenciales reales.
- **Docker Compose:** `compose.yaml` monta el archivo `conexion-local.properties` en `/app/config/conexion-local.properties` en modo solo lectura.
- **Rendimiento:** HikariCP administra el pool de conexiones; el Proxy mantiene su caché en memoria dentro del proceso Java.

## Alcances que NO deben aparecer como hechos implementados

- No existe servidor HTTP, API REST, Spring Boot, microservicios, balanceador, Kubernetes o Redis operando en la arquitectura actual.
- No se ejecuta una aplicación web o móvil para los usuarios finales.
- GitHub Actions ayuda a construir y comprobar software; no es un nodo que hospeda permanentemente la aplicación.
- Docker no es obligatorio para la instalación local ni equivale al nivel de contenedores C4.

## Evidencia en el repositorio

- `src/CareStock/pom.xml`: Java 21, dependencias JavaFX y Maven.
- `src/CareStock/src/main/java/org/example/carestock/CareStockApp.java`: entrada JavaFX.
- `src/CareStock/src/main/java/org/example/carestock/config/ConexionBD.java`: JDBC y HikariCP con configuración local.
- `src/CareStock/config/conexion-local.properties.example`: configuración de base de datos.
- `src/CareStock/Dockerfile` y `compose.yaml`: ejecución Docker opcional.
