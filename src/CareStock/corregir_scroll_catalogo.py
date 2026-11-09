from pathlib import Path

base = Path("src/CareStock/src")

controlador = base / (
    "main/java/org/example/carestock/"
    "controller/ControladorInventario.java"
)

prueba = base / (
    "test/java/org/example/carestock/"
    "controller/CatalogoRutasCoverageTest.java"
)

codigo = controlador.read_text(encoding="utf-8")
tests = prueba.read_text(encoding="utf-8")

antes = """                // Reemplaza la cabecera duplicada del catalogo.
                panel.setTop(encabezadoCatalogo());
"""

despues = """                // Integrar indicadores y catalogo
                // en el mismo ScrollPane.
                // La cabecera azul del dashboard
                // permanece fija.

                if (!(panel.getCenter() instanceof ScrollPane scrollCatalogo)
                        || scrollCatalogo.getContent() == null) {
                    throw new IllegalStateException(
                            "El catalogo requiere un ScrollPane con contenido");
                }

                javafx.scene.Node contenidoOriginal =
                        scrollCatalogo.getContent();

                VBox contenidoConCabecera = new VBox(
                        encabezadoCatalogo(),
                        contenidoOriginal
                );

                scrollCatalogo.setContent(contenidoConCabecera);
                panel.setTop(null);
"""

if codigo.count(antes) != 1:
    raise SystemExit(
        "La estructura del controlador cambio. "
        "No se modificaron archivos."
    )

codigo = codigo.replace(antes, despues)

tests = tests.replace(
    "import javafx.scene.layout.BorderPane;",
    "import javafx.scene.layout.BorderPane;\n"
    "import javafx.scene.layout.StackPane;",
    1
)

antes_test = """            var panel = new BorderPane();

            when(catalogo.categoriaActual())"""

despues_test = """            var panel = new BorderPane();
            var scrollCatalogo = new ScrollPane(new StackPane());
            panel.setCenter(scrollCatalogo);

            when(catalogo.categoriaActual())"""

if tests.count(antes_test) != 1:
    raise SystemExit(
        "No coincide la prueba original. "
        "No se modificaron archivos."
    )

tests = tests.replace(antes_test, despues_test)

antes_assert = """                assertSame(panel, raiz.getCenter());
                assertNotNull(panel.getTop());"""

despues_assert = """                assertSame(panel, raiz.getCenter());
                assertNull(panel.getTop());
                assertSame(scrollCatalogo, panel.getCenter());

                VBox contenido = assertInstanceOf(
                        VBox.class, scrollCatalogo.getContent());

                assertEquals(2, contenido.getChildren().size());
                assertTrue(contenido.getChildren().get(0) instanceof VBox);
                assertTrue(contenido.getChildren().get(1) instanceof StackPane);"""

if tests.count(antes_assert) != 1:
    raise SystemExit(
        "No coincide la asercion original. "
        "No se modificaron archivos."
    )

tests = tests.replace(antes_assert, despues_assert)

controlador.write_text(codigo, encoding="utf-8")
prueba.write_text(tests, encoding="utf-8")

print("OK: Navegacion del catalogo unificada.")
print("OK: Prueba del contenedor actualizada.")
