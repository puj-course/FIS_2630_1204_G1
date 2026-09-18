# Guía de compilación y ejecución — CareStock

## Requisitos

- JDK 17+
- Maven 3.9+
- PostgreSQL/Neon accesible desde el equipo

## Variables de entorno

La aplicación lee:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `CARESTOCK_USER_EMAIL` (opcional; por defecto `admin@carestock.com`)

No agregue credenciales reales a archivos versionados.

## Windows PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://HOST:5432/neondb?sslmode=require"
$env:DB_USER="usuario"
$env:DB_PASSWORD="contraseña"
$env:CARESTOCK_USER_EMAIL="admin@carestock.com"

mvn clean verify
mvn javafx:run
```

También puede ejecutar `scripts/run.ps1` una vez definidas las variables.

## Linux / macOS / Git Bash

```bash
export DB_URL='jdbc:postgresql://HOST:5432/neondb?sslmode=require'
export DB_USER='usuario'
export DB_PASSWORD='contraseña'
export CARESTOCK_USER_EMAIL='admin@carestock.com'

mvn clean verify
mvn javafx:run
```

## GitHub Actions

Las pruebas de CI no necesitan conexión a la base porque las pruebas actuales son unitarias. Las credenciales de producción no deben almacenarse en el repositorio; si más adelante se agregan pruebas de integración o despliegue, use GitHub Actions Secrets.
