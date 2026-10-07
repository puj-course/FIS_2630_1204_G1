# HU-90 — Trazabilidad: Mockup → Componente de Código


| Mockup | Ruta del mockup | Componente de código | Ruta del código | Fidelidad |
|---|---|---|---|---|
| Mensaje de éxito/error | `docs/HU/HU43/mockup-mensajes-hu43.png` | `AlertUtil.java` | `src/main/java/com/carestock/view/` | ✅ Exacta |
| Botón "Guardar lote" con paleta oficial | `docs/architecture/DOCUMENTACION_UI.md` (sec. 6) | `ingreso-lote.css` (.btn-guardar) | `src/main/resources/com/carestock/view/` | ✅ Exacta |
| Selector de ubicación | `docs/architecture/DOCUMENTACION_UI.md` (sec. 8) | ComboBox `cmbUbicacion` | `IngresoLote.fxml` | ⚠️ Lista fija, pendiente conectar a BD |
| Campo de fecha bloqueada | `docs/architecture/DOCUMENTACION_UI.md` (sec. 9) | `dpFechaVencimiento` + `IngresoLoteValidator` | `src/main/java/com/carestock/utils/` | ✅ Exacta |
| Badge de farmacia activa | `docs/HU/HU62/mockup-badge-farmacia-hu62.png` | Badge en `CrearUsuarioFX.java` | `src/main/java/com/carestock/view/` | ⚠️ Lista de farmacias de ejemplo |
| Login | `docs/HU/HU56/mockup-login-hu56.png` | `LoginFX.java` | `src/main/java/com/carestock/view/` | ✅ Exacta |



## Pendientes detectados
- Conectar el selector de ubicación (HU-44) a la tabla real de ubicaciones en base de datos, en vez de la lista fija actual.
- Conectar el badge y el ComboBox de farmacia (HU-61, HU-62) a la futura tabla FARMACIAS, cuando esté implementada.
