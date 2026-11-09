from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path('src/CareStock/src')
java = root / 'main/java/org/example/carestock/controller'
fxml = root / 'main/resources/org/example/carestock/catalogo-view.fxml'
files = {
    java / 'EstiloAlertas.java': None,
    java / 'ControladorCatalogo.java': None,
    root / 'test/java/org/example/carestock/controller/EstiloAlertasTest.java': None,
    fxml: None,
}
for f in files:
    files[f] = f.read_text(encoding='utf-8')

estilo, controlador, test = list(files)[:3]

def cambiar(f, viejo, nuevo):
    if files[f].count(viejo) != 1:
        raise SystemExit('NO SE APLICARON CAMBIOS: fragmento inesperado en ' + str(f))
    files[f] = files[f].replace(viejo, nuevo, 1)

# El usuario ha definido "MENOS que el mínimo" (estricto).
cambiar(estilo,
    'if (minimo != null && minimo > 0 && stock <= minimo) return Nivel.ADVERTENCIA;',
    'if (minimo != null && minimo > 0 && stock < minimo) return Nivel.ADVERTENCIA;')

# Política visual única, testeable y consistente para las tres categorías.
cambiar(estilo,
    '''    static Nivel nivelStockCatalogo(int stock, Integer minimo) {''',
    '''    static String estiloFilaCatalogo(String estado, int stock, Integer minimo) {
        if (stock <= 0) return estiloFila(Nivel.CRITICO, false);
        if ("INACTIVO".equalsIgnoreCase(estado)) {
            return "-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;";
        }
        if (nivelStockCatalogo(stock, minimo) == Nivel.ADVERTENCIA) {
            return estiloFila(Nivel.ADVERTENCIA, false);
        }
        // Producto activo, stock disponible y suficiente: verde pastel.
        return "-fx-background-color: #D8F2DF; -fx-text-fill: #236344;"
                + " -fx-text-background-color: #236344;";
    }

    static Nivel nivelStockCatalogo(int stock, Integer minimo) {''')

viejo = '''                if (item == null || empty) {
                    setStyle("");
                } else if (stock(item.producto()) <= 0) {
                    setStyle(EstiloAlertas.estiloFila(EstiloAlertas.Nivel.CRITICO, false));
                } else if ("INACTIVO".equalsIgnoreCase(item.estado())) {
                    setStyle("-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;");
                } else {
                    int stockActual = stock(item.producto());
                    Integer stockMinimo = stockMinimoDe(item.producto());

                    if (EstiloAlertas.nivelStockCatalogo(stockActual, stockMinimo)
                            == EstiloAlertas.Nivel.ADVERTENCIA) {
                        setStyle(EstiloAlertas.estiloFila(
                                EstiloAlertas.Nivel.ADVERTENCIA, false));
                    } else {
                        setStyle(""); 
                    }
                }'''
nuevo = '''                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle(EstiloAlertas.estiloFilaCatalogo(
                            item.estado(), stock(item.producto()),
                            stockMinimoDe(item.producto())));
                }'''
cambiar(controlador, viejo, nuevo)

cambiar(controlador,
    'setTooltip(new Tooltip("Producto inactivo (histórico)."));',
    'setTooltip(new Tooltip("REGISTRO HISTÓRICO INACTIVO: existencias registradas, "\n                                    + "no disponibles para dispensación."));')

cambiar(test,
    'assertEquals(Nivel.ADVERTENCIA, EstiloAlertas.nivelStockCatalogo(10, 10));',
    'assertEquals(Nivel.NINGUNO, EstiloAlertas.nivelStockCatalogo(10, 10));')

cambiar(test,
    '''    @Test
    void filaCriticaUsaCoralConTextoOscuroYNegrita()''',
    '''    @Test
    void politicaVisualConservaHistorialYPriorizaAgotados() {
        assertTrue(EstiloAlertas.estiloFilaCatalogo("INACTIVO", 20, 10)
                .contains("#ECEFF2"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("INACTIVO", 0, 10)
                .contains("#F0A8A8"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 4, 10)
                .contains("#F6CE8E"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 10, 10)
                .contains("#D8F2DF"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 20, 10)
                .contains("#D8F2DF"));
    }

    @Test
    void filaCriticaUsaCoralConTextoOscuroYNegrita()''')

# Respeta el estilo y los valores de padding de la persona usuaria.
s = files[fxml]
for prefijo, valor in (
    ('NARANJA:', 'NARANJA: Stock bajo (menos del mínimo)'),
    ('GRIS:', 'GRIS: Registro histórico inactivo'),
    ('Sin color:', 'VERDE: Stock suficiente (en el mínimo o superior)')
):
    expresion = r'(<Label\s+text=")' + re.escape(prefijo) + r'[^\"]*(")'
    coincidencias = re.findall(expresion, s)
    if len(coincidencias) != 1:
        raise SystemExit('NO SE APLICARON CAMBIOS: falta etiqueta ' + prefijo)
    s = re.sub(expresion, lambda m: m.group(1)+valor+m.group(2), s, count=1)
# Aplicar también el fondo verde en la leyenda (no solo el texto).
patron_verde = (r'(<Label\s+text="VERDE:[^"]*"\s*'
                r'style=")([^"]*)(")')
coincidencias_verde = list(re.finditer(patron_verde, s))
if len(coincidencias_verde) != 1:
    raise SystemExit('NO SE APLICARON CAMBIOS: etiqueta verde inesperada')
s = re.sub(patron_verde,
    lambda m: (m.group(1)
        + '-fx-background-color: #D8F2DF; -fx-text-fill: #236344; '
        + '-fx-padding: 6 10; -fx-background-radius: 6;'
        + m.group(3)), s, count=1)
files[fxml] = s
ET.fromstring(s)  # XML bien formado

for file, content in files.items():
    file.write_text(content, encoding='utf-8')
print('OK: 4 archivos actualizados; FXML válido; prueba visual añadida.')
