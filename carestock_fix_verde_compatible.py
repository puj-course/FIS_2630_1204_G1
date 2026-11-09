#!/usr/bin/env python3
# Reconciliación segura de colores del catálogo CareStock.
# Acepta el parche de alertas inicial o el parche de históricos ya aplicado.
# No cambia Neon ni el flujo de navegación.
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path('src/CareStock/src')
java = root / 'main/java/org/example/carestock/controller'
paths = {
    'estilo': java / 'EstiloAlertas.java',
    'controlador': java / 'ControladorCatalogo.java',
    'prueba': root / 'test/java/org/example/carestock/controller/EstiloAlertasTest.java',
    'fxml': root / 'main/resources/org/example/carestock/catalogo-view.fxml',
}
try:
    data = {key: path.read_text(encoding='utf-8') for key, path in paths.items()}
except OSError as exc:
    sys.exit(f'ERROR: no se pudo leer un archivo: {exc}')


def method_bounds(src, regex, *, from_index=0):
    m = re.search(regex, src[from_index:])
    if not m:
        return None
    start = from_index + m.start()
    brace = src.find('{', from_index + m.end())
    if brace < 0:
        raise ValueError(f'No se encontro apertura de metodo {regex}')
    depth = 0
    for i in range(brace, len(src)):
        if src[i] == '{':
            depth += 1
        elif src[i] == '}':
            depth -= 1
            if depth == 0:
                return start, i + 1
    raise ValueError(f'Metodo sin cierre {regex}')


def replace_method(src, regex, code):
    bounds = method_bounds(src, regex)
    if bounds is None:
        return None
    start, end = bounds
    return src[:start] + code + src[end:]


try:
    # 1. Regla de prioridad: rojo -> gris historico -> naranja -> verde.
    estilo = data['estilo']
    nivel_method = '''static Nivel nivelStockCatalogo(int stock, Integer minimo) {
        if (stock <= 0) return Nivel.CRITICO;
        if (minimo != null && minimo > 0 && stock < minimo) return Nivel.ADVERTENCIA;
        return Nivel.NINGUNO;
    }'''
    fila_method = '''static String estiloFilaCatalogo(String estado, int stock, Integer minimo) {
        if (stock <= 0) return estiloFila(Nivel.CRITICO, false);
        if ("INACTIVO".equalsIgnoreCase(estado)) {
            return "-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;"
                    + " -fx-text-background-color: #59636D;";
        }
        if (nivelStockCatalogo(stock, minimo) == Nivel.ADVERTENCIA) {
            return estiloFila(Nivel.ADVERTENCIA, false);
        }
        return "-fx-background-color: #D8F2DF; -fx-text-fill: #236344;"
                + " -fx-text-background-color: #236344;";
    }'''
    replaced = replace_method(estilo, r'\bstatic\s+Nivel\s+nivelStockCatalogo\s*\(', nivel_method)
    if replaced is None:
        # Original de proyecto sin la utilidad de stock del catálogo.
        if estilo.rstrip().endswith('}'):
            estilo = estilo.rstrip()[:-1] + '\n\n    ' + nivel_method + '\n}\n'
        else:
            raise ValueError('EstiloAlertas.java tiene una estructura inesperada')
    else:
        estilo = replaced
    replaced = replace_method(estilo, r'\bstatic\s+String\s+estiloFilaCatalogo\s*\(', fila_method)
    if replaced is None:
        estilo = estilo.rstrip()[:-1] + '\n\n    ' + fila_method + '\n}\n'
    else:
        estilo = replaced
    data['estilo'] = estilo

    # 2. Actualizar solamente updateItem de la RowFactory del catálogo.
    ctrl = data['controlador']
    marker = 'tablaProductos.setRowFactory('
    if ctrl.count(marker) != 1:
        raise ValueError('Se esperaba exactamente una RowFactory en la tabla del catálogo')
    pos = ctrl.index(marker)
    bounds = method_bounds(ctrl, r'protected\s+void\s+updateItem\s*\(\s*RegistroCatalogo<\?>\s+item,\s*boolean\s+empty\s*\)', from_index=pos)
    if bounds is None:
        raise ValueError('No se encontro updateItem de filas del catálogo')
    start, end = bounds
    old_method = ctrl[start:end]
    if 'super.updateItem(item, empty);' not in old_method or 'setStyle(' not in old_method:
        raise ValueError('updateItem contiene comportamiento distinto: no se modifico')
    if 'setTooltip(' in old_method or 'setGraphic(' in old_method or 'setText(' in old_method:
        raise ValueError('updateItem incluye otras operaciones: requiere revision')
    new_method = '''protected void updateItem(RegistroCatalogo<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(EstiloAlertas.estiloFilaCatalogo(
                            item.estado(), stock(item.producto()),
                            stockMinimoDe(item.producto())));
                }
            }'''
    ctrl = ctrl[:start] + new_method + ctrl[end:]
    ctrl = ctrl.replace('new Tooltip("Producto inactivo (histórico).")',
                        'new Tooltip("REGISTRO HISTÓRICO INACTIVO: no disponible para dispensación.")')
    data['controlador'] = ctrl

    # 3. Leyenda: mantener padding y otras opciones del usuario intactas.
    fxml = data['fxml']
    legend_match = re.search(r'<FlowPane\b[^>]*fx:id="leyendaStock"[^>]*>.*?</FlowPane>', fxml, re.S)
    if legend_match is None:
        raise ValueError('No existe la leyenda leyendaStock en el FXML')
    legend = legend_match.group()
    encountered = set()
    def update_label(m):
        tag = m.group(0)
        text_match = re.search(r'\btext="([^"]*)"', tag)
        if not text_match:
            return tag
        value = text_match.group(1)
        replacement = None
        if value.startswith('ROJO:'):
            kind, replacement = 'rojo', 'ROJO: Sin existencias (stock 0)'
        elif value.startswith('NARANJA:'):
            kind, replacement = 'naranja', 'NARANJA: Stock bajo (menos del mínimo)'
        elif value.startswith('GRIS:'):
            kind, replacement = 'gris', 'GRIS: Registro histórico inactivo'
        elif value.startswith('Sin color:') or value.startswith('VERDE:'):
            kind, replacement = 'verde', 'VERDE: Stock igual o superior al mínimo'
        else:
            return tag
        if kind in encountered:
            raise ValueError('Etiqueta duplicada en la leyenda: ' + kind)
        encountered.add(kind)
        tag = tag[:text_match.start(1)] + replacement + tag[text_match.end(1):]
        if kind == 'verde':
            new_style = ('-fx-background-color: #D8F2DF; -fx-text-fill: #236344; '
                         '-fx-padding: 6 10; -fx-background-radius: 6;')
            if re.search(r'\bstyle="[^"]*"', tag):
                tag = re.sub(r'\bstyle="[^"]*"', 'style="' + new_style + '"', tag, count=1)
            else:
                tag = tag.replace('/>', 'style="' + new_style + '" />', 1)
        return tag
    updated_legend = re.sub(r'<Label\b[^>]*\/?>', update_label, legend)
    if encountered != {'rojo', 'naranja', 'gris', 'verde'}:
        raise ValueError('Se esperaban cuatro etiquetas; encontradas: ' + repr(encountered))
    data['fxml'] = fxml[:legend_match.start()] + updated_legend + fxml[legend_match.end():]
    ET.fromstring(data['fxml'])

    # 4. Tests para todas las transiciones y el límite de stock mínimo.
    prueba = data['prueba']
    prueba = prueba.replace('assertEquals(Nivel.ADVERTENCIA, EstiloAlertas.nivelStockCatalogo(10, 10));',
                            'assertEquals(Nivel.NINGUNO, EstiloAlertas.nivelStockCatalogo(10, 10));')
    prueba = prueba.replace('assertEquals("", EstiloAlertas.estiloFilaCatalogo("ACTIVO", 10, 10));',
                            'assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 10, 10).contains("#D8F2DF"));')
    test_name = 'void catalogoUsaVerdeYRespetaPrioridades()'
    if test_name not in prueba:
        first = re.search(r'\s*@Test\s*\n', prueba)
        if first is None:
            raise ValueError('No se encontro punto de insercion para nueva prueba')
        block = '''\n    @Test
    void catalogoUsaVerdeYRespetaPrioridades() {
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 0, 0).contains("#F0A8A8"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 4, 10).contains("#F6CE8E"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 10, 10).contains("#D8F2DF"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("ACTIVO", 20, 10).contains("#D8F2DF"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("INACTIVO", 20, 10).contains("#ECEFF2"));
        assertTrue(EstiloAlertas.estiloFilaCatalogo("INACTIVO", 0, 10).contains("#F0A8A8"));
        assertEquals(Nivel.NINGUNO, EstiloAlertas.nivelStockCatalogo(10, 10));
    }
'''
        prueba = prueba[:first.start()] + '\n' + block + prueba[first.start():]
    data['prueba'] = prueba

except (ValueError, OSError, ET.ParseError) as exc:
    sys.exit('NO SE MODIFICARON ARCHIVOS: ' + str(exc))

for key, path in paths.items():
    if data[key] != path.read_text(encoding='utf-8'):
        path.write_text(data[key], encoding='utf-8')
        print('ACTUALIZADO:', path)
    else:
        print('SIN CAMBIOS:', path)
print('OK: 4 colores, FXML válido y prueba del límite del mínimo.')
