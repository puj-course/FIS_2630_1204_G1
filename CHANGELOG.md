# Changelog

## Integración de estructura — 2026-09-18

- Se unificó el código Java bajo el paquete `com.carestock`.
- Se migró el controlador de ingreso de lotes a `com.carestock.controller`.
- Se agregaron los modelos `Lote` y `Ubicacion` sin duplicar `Medicamento`.
- Se corrigió `IngresoLoteValidator` y se añadieron pruebas unitarias.
- Se agregaron `LoteDAO`, `UbicacionDAO`, `UsuarioDAO` e `IngresoLoteService`.
- El formulario FXML quedó en `src/main/resources/com/carestock/view/IngresoLote.fxml`.
- El Dashboard abre el ingreso de lotes y recarga métricas/stock después del registro.
- Se agregó Maven con JavaFX, PostgreSQL JDBC y JUnit.
- Se eliminaron drivers `.jar` versionados manualmente.
- Las credenciales PostgreSQL se retiraron del código y documentación y ahora se leen desde variables de entorno.
- Se agregaron `conf/`, scripts de setup/test/run/deploy y workflows de CI/package.
- Se actualizó el README para reflejar la arquitectura implementada realmente.

### Nota de seguridad

Si una contraseña real estuvo previamente incluida en el repositorio o en su historial Git, debe rotarse en el proveedor de base de datos antes de continuar utilizándola.
