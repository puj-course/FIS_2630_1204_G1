# HU-90 — Trazabilidad: Mockup → Componente de Código

| Mockup | Ruta del mockup | Componente de código | Ruta del código |
|---|---|---|---|
| Mensaje de éxito/error | `docs/HU/HU43/mockup-mensajes-hu43.png` | `AlertUtil.java` | `src/main/java/com/carestock/view/` |
| Botón "Guardar lote" con paleta oficial | `docs/architecture/DOCUMENTACION_UI.md` (sec. 6) | `ingreso-lote.css` (.btn-guardar) | `src/main/resources/com/carestock/view/` |
| Selector de ubicación | `docs/architecture/DOCUMENTACION_UI.md` (sec. 8) | ComboBox `cmbUbicacion` | `IngresoLote.fxml` |
| Campo de fecha bloqueada | `docs/architecture/DOCUMENTACION_UI.md` (sec. 9) | `dpFechaVencimiento` + `IngresoLoteValidator` | `src/main/java/com/carestock/utils/` |
| Badge de farmacia activa | `docs/HU/HU62/mockup-badge-farmacia-hu62.png` | Badge en `CrearUsuarioFX.java` | `src/main/java/com/carestock/view/` |
| Login | `docs/HU/HU56/mockup-login-hu56.png` | `LoginFX.java` | `src/main/java/com/carestock/view/` |
