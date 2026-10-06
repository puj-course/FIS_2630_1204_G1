# HU-88 — Diseño de la Capa de Vista (Presentation Layer)

## Inventario de vistas

| Vista | Archivo | Hoja de estilo |
|---|---|---|
| Login | `LoginFX.java` | estilos inline (paleta oficial) |
| Dashboard principal | `MainDashboardFX.java` | estilos inline (paleta oficial) |
| Registro de ingreso de lote | `IngresoLote.fxml` | `ingreso-lote.css` |
| Crear usuario | `CrearUsuarioFX.java` | estilos inline |
| Historial de accesos | `HistorialAccesosFX.java` | estilos inline |



## Diagrama de la estructura de la capa de Vista

```mermaid
flowchart TB
    subgraph Vistas FXML
        A[IngresoLote.fxml] --> AC[ingreso-lote.css]
    end
    subgraph Vistas en código JavaFX
        B[LoginFX.java]
        C[MainDashboardFX.java]
        D[CrearUsuarioFX.java]
        E[HistorialAccesosFX.java]
    end
    A --> IC[IngresoLoteController]
    IC --> SV[IngresoLoteService]
```
