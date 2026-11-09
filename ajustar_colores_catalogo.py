from pathlib import Path
import xml.etree.ElementTree as ET

base = Path("src/CareStock/src")

c = base / (
    "main/java/org/example/carestock/"
    "controller/ControladorCatalogo.java"
)

f = base / (
    "main/resources/org/example/carestock/"
    "catalogo-view.fxml"
)

e = base / (
    "main/java/org/example/carestock/"
    "controller/EstiloAlertas.java"
)

archivos = {
    p: p.read_text(encoding="utf-8")
    for p in (c, f, e)
}

def reemplazar(p, antes, despues):
    if archivos[p].count(antes) != 1:
        raise SystemExit(
            f"No coincide {p}. "
            "No se guardaron cambios."
        )
    archivos[p] = archivos[p].replace(
        antes, despues, 1
    )

# Regla reutilizable de stock.
reemplazar(
    e,
    """    static Nivel nivelPorExistencias(int existencias) {
        return existencias <= 0 ? Nivel.CRITICO : Nivel.ADVERTENCIA;
    }""",
    """    static Nivel nivelPorExistencias(int existencias) {
        return existencias <= 0 ? Nivel.CRITICO : Nivel.ADVERTENCIA;
    }

    static Nivel nivelStockCatalogo(int stock, Integer minimo) {
        if (stock <= 0) return Nivel.CRITICO;
        if (minimo != null && minimo > 0 && stock <= minimo)
            return Nivel.ADVERTENCIA;
        return Nivel.NINGUNO;
    }"""
)

# Rojo tiene prioridad, incluso si es inactivo.
reemplazar(
    c,
    '''                } else if ("INACTIVO".equalsIgnoreCase(item.estado())) {
                    setStyle("-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;");''',
    '''                } else if (stock(item.producto()) <= 0) {
                    setStyle(EstiloAlertas.estiloFila(
                            EstiloAlertas.Nivel.CRITICO, false));
                } else if ("INACTIVO".equalsIgnoreCase(item.estado())) {
                    setStyle("-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;");'''
)

# Naranja solamente para stock bajo positivo.
reemplazar(
    c,
    '''if (stockMinimo != null && stockActual <= stockMinimo) {
                        setStyle(EstiloAlertas.estiloFila(
                                EstiloAlertas.nivelPorExistencias(stockActual), false));''',
    '''if (EstiloAlertas.nivelStockCatalogo(
                            stockActual, stockMinimo)
                            == EstiloAlertas.Nivel.ADVERTENCIA) {
                        setStyle(EstiloAlertas.estiloFila(
                                EstiloAlertas.Nivel.ADVERTENCIA, false));'''
)

# Explicacion visual dentro del catalogo.
reemplazar(
    f,
    '''                        <VBox fx:id="panelListaCompleta"''',
    '''                        <FlowPane fx:id="leyendaStock"
                                  hgap="10" vgap="8"
                                  alignment="CENTER_LEFT"
                                  style="-fx-padding: 3 0 5 0;">
                            <children>
                                <Label text="ROJO: Sin existencias (stock 0)"
                                       style="-fx-background-color: #F0A8A8; -fx-text-fill: #7A2E2E; -fx-padding: 6 10; -fx-background-radius: 6; -fx-font-weight: bold;" />
                                <Label text="NARANJA: Stock bajo (1 al mínimo)"
                                       style="-fx-background-color: #F6CE8E; -fx-text-fill: #20302F; -fx-padding: 6 10; -fx-background-radius: 6;" />
                                <Label text="GRIS: Producto inactivo con stock"
                                       style="-fx-background-color: #ECEFF2; -fx-text-fill: #59636D; -fx-padding: 6 10; -fx-background-radius: 6;" />
                                <Label text="Sin color: Stock suficiente"
                                       style="-fx-text-fill: #45605C; -fx-padding: 6 2;" />
                            </children>
                        </FlowPane>
                        <VBox fx:id="panelListaCompleta"'''
)

ET.fromstring(archivos[f])

for ruta, contenido in archivos.items():
    ruta.write_text(contenido, encoding="utf-8")

print("OK: colores y leyenda actualizados.")
print("FXML validado.")
