$ErrorActionPreference = "Stop"

if (-not $env:DB_URL) { throw "Debe definir DB_URL" }
if (-not $env:DB_USER) { throw "Debe definir DB_USER" }
if (-not $env:DB_PASSWORD) { throw "Debe definir DB_PASSWORD" }
if (-not $env:CARESTOCK_USER_EMAIL) { $env:CARESTOCK_USER_EMAIL = "admin@carestock.com" }

mvn javafx:run
