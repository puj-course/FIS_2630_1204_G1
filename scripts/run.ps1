$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

if (-not $env:CARESTOCK_USER_EMAIL) {
    $env:CARESTOCK_USER_EMAIL = 'admin@carestock.com'
}

Push-Location (Join-Path $raiz 'src/CareStock')
try {
    & .\mvnw.cmd -B -ntp javafx:run
    if ($LASTEXITCODE -ne 0) {
        throw 'No fue posible iniciar JavaFX.'
    }
} finally {
    Pop-Location
}
