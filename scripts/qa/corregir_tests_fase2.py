from pathlib import Path

ROOT = Path("src/CareStock/src/test/java/org/example/carestock/controller")

def editar(nombre, transformar):
    archivo = ROOT / nombre
    if not archivo.exists():
        raise SystemExit(f"No existe: {archivo}")

    datos = archivo.read_bytes()
    salto = "\r\n" if b"\r\n" in datos else "\n"
    original = datos.decode("utf-8").replace("\r\n", "\n")
    nuevo = transformar(original)

    if nuevo != original:
        archivo.write_bytes(nuevo.replace("\n", salto).encode("utf-8"))
        print(f"CORREGIDO: {nombre}")
    else:
        print(f"SIN CAMBIOS: {nombre}")


def corregir_categorias(texto):
    inicio = texto.find(
        "    @Test\n"
        "    void inventarioQaConsultaCategoriasYControlaErrores()"
    )
    fin = texto.find("    // ===== Lote 4:", inicio)

    if inicio < 0 or fin < 0:
        raise SystemExit("No se encontro el bloque de prueba de categorias")

    bloque = texto[inicio:fin]

    if 'llamar(c, "cargarCategorias")' not in bloque:
        return texto

    nuevo = '''    @Test
    void inventarioQaConsultaCategoriasYControlaErrores()
            throws Exception {
        enFx(() -> {
            ControladorInventario c = inventarioFormulario();
            llamar(c, "configurarFiltroBusqueda");

            Inventario fachada = obtener(c, "inventario");

            when(fachada.listarLotes()).thenReturn(List.of(
                    lotePrueba(1, "LOTE-QA", "DISPONIBLE", -1, 20)));

            when(fachada.categoriaPorMedicamento())
                    .thenReturn(Map.of(1, "Analgesicos"))
                    .thenThrow(new IllegalStateException(
                            "Error de categorias simulado"));

            llamar(c, "cargarDatosTabla");

            Map<Integer, String> categorias =
                    obtener(c, "categoriaPorMedicamento");

            TableView<Lote> tabla = obtener(c, "tablaLotes");

            assertEquals("Analgesicos", categorias.get(1));
            assertEquals(1, tabla.getItems().size());

            llamar(c, "cargarDatosTabla");

            Map<Integer, String> categoriasTrasError =
                    obtener(c, "categoriaPorMedicamento");

            assertTrue(categoriasTrasError.isEmpty());
            assertEquals(1, tabla.getItems().size());

            verify(fachada, times(2)).categoriaPorMedicamento();

            return null;
        });
    }


'''
    return texto[:inicio] + nuevo + texto[fin:]


def corregir_refresco(texto):
    anterior = '                Timeline refresco = obtener(c, "refresco");\n\n'

    if anterior not in texto:
        if "refresco.stop();" in texto:
            raise SystemExit("Estructura de refresco no reconocida")
        return texto

    if texto.count(anterior) != 1:
        raise SystemExit("Se encontraron referencias inesperadas")

    if texto.count("                    refresco.stop();") != 1:
        raise SystemExit("No se encontro el cierre del Timeline")

    texto = texto.replace(anterior, "", 1)

    texto = texto.replace(
        "                    refresco.stop();",
        '''                    // Simular salida de la escena para detener
                    // automaticamente el refresco del controlador.
                    TableView<AlertaInventario> tabla =
                            obtener(c, "tablaAlertas");

                    javafx.scene.layout.StackPane raizEscena =
                            new javafx.scene.layout.StackPane(tabla);

                    javafx.scene.Scene escena =
                            new javafx.scene.Scene(raizEscena);

                    assertSame(escena, tabla.getScene());

                    raizEscena.getChildren().clear();

                    assertNull(tabla.getScene());''',
        1
    )

    if "Timeline" not in texto.replace(
            "import javafx.animation.Timeline;", ""):
        texto = texto.replace(
            "import javafx.animation.Timeline;\n", "")

    return texto


editar(
    "ControladorCatalogoFormularioFxTest.java",
    corregir_categorias
)

editar(
    "NavegacionContextualControladoresTest.java",
    corregir_refresco
)

print("Correcciones terminadas.")
