# Issue #984 - Acciones contextuales de alertas

Parent: #980 (HU.136). Dependencia: #983.

## Matriz y seguridad

- Administrador y Farmacéutico: consulta y gestión de stock mínimo y vencimiento.
- Auxiliar: solo consulta; las escrituras se bloquean también en servicio.
- Super Admin: se conserva la autorización preexistente para gestionar lotes.
- El servicio de movimientos valida el rol en sesión y BD, la farmacia y el producto activo.
- El CRUD de lotes conserva sus validaciones de usuario/farmacia, estado sanitario y stock.
- Abrir o cancelar una alerta no ejecuta cambios en inventario.

## Navegación

- Stock Aseo/Maternidad: movimientos con producto preseleccionado, ENTRADA sin guardar al abrir.
- Stock Medicamento: crear nuevo lote con medicamento preseleccionado y bloqueado.
- Vencimiento: lote identificado y operaciones CRUD existentes según permisos.

## Evidencias pendientes (registrar resultados reales)

- [ ] `mvnw clean verify`, guardar salida en `docs/qa/evidence/issue-984-verify.txt`.
- [ ] `mvnw javafx:run` y UAT por Administrador, Farmacéutico y Auxiliar.
- [ ] Rechazo de otra farmacia/otro producto, y ausencia de escrituras al consultar.
- [ ] Verificación de filtros, severidad y paleta oficial.
- [ ] JaCoCo y Quality Gate SonarCloud; anotar Coverage on New Code y Reliability Rating on New Code.
