$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

Push-Location (Join-Path $raiz 'src/CareStock')
try {
    & .\mvnw.cmd -B -ntp clean verify
    if ($LASTEXITCODE -ne 0) {
        throw 'Fallaron las pruebas Maven.'
    }
} finally {
    Pop-Location
}
