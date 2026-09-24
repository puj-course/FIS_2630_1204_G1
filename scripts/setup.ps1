$ErrorActionPreference = "Stop"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw "Java no está instalado o no está en PATH." }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw "Maven no está instalado o no está en PATH." }

java -version
mvn -version
mvn clean test

Write-Host "CareStock listo. Configure DB_URL, DB_USER, DB_PASSWORD y CARESTOCK_USER_EMAIL antes de ejecutar."
