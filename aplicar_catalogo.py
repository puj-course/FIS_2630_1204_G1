from pathlib import Path
import xml.etree.ElementTree as ET
from io import StringIO

R = Path('src/CareStock/src')
C = R/'main/java/org/example/carestock/controller/ControladorCatalogo.java'
D = R/'main/java/org/example/carestock/dao/ConsultasSqlCatalogo.java'
F = R/'main/resources/org/example/carestock/catalogo-view.fxml'
T = R/'test/java/org/example/carestock/dao/ConsultasSqlCatalogoTest.java'
X = R/'test/java/org/example/carestock/controller/CatalogoTablaFormularioFxmlTest.java'

files = {p: p.read_text(encoding='utf-8') for p in (C,D,F,T,X)}

def r(p,a,b):
    s=files[p]
    if s.count(a)!=1:
        raise SystemExit(
            f'No coincide {p}: {a[:90]!r}. Sin cambios.'
        )
    files[p]=s.replace(a,b,1)

# Mostrar el stock real en la tabla.
r(C,'        colStock.setCellFactory(','''
        colStock.setCellValueFactory(
                c -> str(stock(c.getValue().producto())));
        tablaProductos.setFixedCellSize(30);
        tablaProductos.setFocusTraversable(true);
        colStock.setCellFactory('''.lstrip('\n'))

# No generar alertas visuales para históricos.
r(C,
'''if (registro != null && minimoProducto != null) {''',
'''if (registro != null && minimoProducto != null
                            && !"INACTIVO".equalsIgnoreCase(registro.estado())) {''')

# Distinguir productos inactivos con fondo gris.
r(C,
'''                } else {
                    int stockActual = stock(item.producto());''',
'''                } else if ("INACTIVO".equalsIgnoreCase(item.estado())) {
                    setStyle("-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;");
                } else {
                    int stockActual = stock(item.producto());''')

# Control de disponibilidad del botón.
r(C,
'''        return ((Medicamento) producto).getStockTotal();
    }
''',
'''        return ((Medicamento) producto).getStockTotal();
    }

    private void actualizarDisponibilidadDesactivar() {
        if (btnDesactivarFormulario == null) return;
        boolean permitido = seleccionado != null
                && "ACTIVO".equalsIgnoreCase(seleccionado.estado())
                && stock(seleccionado.producto()) == 0;
        btnDesactivarFormulario.setDisable(!permitido);
        btnDesactivarFormulario.setTooltip(permitido ? null
                : new Tooltip("Solo se puede desactivar con stock cero."));
    }
''')

r(C,
'''                        llenarFormulario(actual.producto());
                    }
                });''',
'''                        llenarFormulario(actual.producto());
                    }
                    actualizarDisponibilidadDesactivar();
                });''')

r(C,
'''        habilitarAccion(btnDesactivarFormulario, !nuevo);
''',
'''        habilitarAccion(btnDesactivarFormulario, !nuevo);
        actualizarDisponibilidadDesactivar();
''')

# Revalidar antes de ejecutar la acción.
r(C,
'''            advertencia("Seleccione un producto");
            return;
        }

        Alert confirmar''',
'''            advertencia("Seleccione un producto");
            return;
        }
        if (!"ACTIVO".equalsIgnoreCase(seleccionado.estado())
                || stock(seleccionado.producto()) != 0) {
            advertencia("Solo se desactivan productos activos con stock cero.");
            return;
        }

        Alert confirmar''')

# Protección SQL adicional para Aseo.
r(D,
'''        WHERE id_aseo = ? AND id_farmacia = ?
          AND estado = 'ACTIVO'
        """
    );''',
'''        WHERE id_aseo = ? AND id_farmacia = ?
          AND estado = 'ACTIVO' AND stock = 0
        """
    );''')

# Protección SQL adicional para Maternidad.
r(D,
'''        WHERE id_maternidad = ? AND id_farmacia = ?
          AND estado = 'ACTIVO'
        """
    );''',
'''        WHERE id_maternidad = ? AND id_farmacia = ?
          AND estado = 'ACTIVO' AND stock = 0
        """
    );''')

# Desplazamiento vertical como en Inventario de Lotes.
r(F,
'''    <center>
        <StackPane style="-fx-background-color: #FDFBF7;">''',
'''    <center>
        <ScrollPane fx:id="scrollCatalogo"
                    fitToWidth="true" pannable="true"
                    hbarPolicy="NEVER" vbarPolicy="AS_NEEDED"
                    style="-fx-background: #FDFBF7; -fx-background-color: #FDFBF7;">
            <content>
        <StackPane style="-fx-background-color: #FDFBF7;">''')

r(F,
'''        </StackPane>
    </center>
</BorderPane>''',
'''        </StackPane>
            </content>
        </ScrollPane>
    </center>
</BorderPane>''')

# Prueba de la restricción de stock.
r(T,
'''            assertTrue(sql.contains("estado = 'ACTIVO'"));
            assertEquals(3, contarParametros(sql));''',
'''            assertTrue(sql.contains("estado = 'ACTIVO'"));
            assertTrue(sql.contains("stock = 0"));
            assertEquals(3, contarParametros(sql));''')

# Prueba de navegación.
r(X,
'''    @Test
    void tablaVisiblePorDefecto() throws Exception {''',
'''    @Test
    void desplazamientoVerticalDelCatalogo() throws Exception {
        Element scroll = buscar(cargar(), "scrollCatalogo");
        assertEquals("ScrollPane", scroll.getLocalName());
        assertEquals("AS_NEEDED", scroll.getAttribute("vbarPolicy"));
    }

    @Test
    void tablaVisiblePorDefecto() throws Exception {''')

ET.parse(StringIO(files[F]))

for p,s in files.items():
    p.write_text(s,encoding='utf-8')

print('OK: 5 archivos corregidos y FXML validado.')
