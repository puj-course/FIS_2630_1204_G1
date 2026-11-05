$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$proyecto = Join-Path $raiz 'src/CareStock'

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'Falta Java 21 en PATH.'
}

Push-Location $proyecto
try {
    java -version
    & .\mvnw.cmd -B -ntp clean test
    if ($LASTEXITCODE -ne 0) {
        throw 'Fallaron las pruebas Maven.'
    }
} finally {
    Pop-Location
}

Write-Host 'Configuracion comprobada.'
