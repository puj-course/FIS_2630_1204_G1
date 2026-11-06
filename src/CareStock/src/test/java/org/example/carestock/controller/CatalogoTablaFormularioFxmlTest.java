package org.example.carestock.controller;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoTablaFormularioFxmlTest {

    private static final String FX =
            "http://javafx.com/fxml/1";

    private Element cargar() throws Exception {
        var fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true);

        try (var entrada = getClass().getResourceAsStream(
                "/org/example/carestock/catalogo-view.fxml")) {

            assertNotNull(entrada);
            return fabrica.newDocumentBuilder()
                    .parse(entrada)
                    .getDocumentElement();
        }
    }

    private Element buscar(Element raiz, String id) {
        var nodos = raiz.getElementsByTagName("*");

        for (int i = 0; i < nodos.getLength(); i++) {
            var nodo = (Element) nodos.item(i);

            if (id.equals(nodo.getAttributeNS(FX, "id"))) {
                return nodo;
            }
        }

        fail("No existe: " + id);
        return null;
    }

    @Test
    void tablaVisiblePorDefecto() throws Exception {
        Element raiz = cargar();

        assertEquals(
                "TableView",
                buscar(raiz, "tablaProductos").getLocalName());

        assertEquals(
                "true",
                buscar(raiz, "panelListaCompleta")
                        .getAttribute("visible"));
    }

    @Test
    void formularioOcultoInicialmente() throws Exception {
        Element capa = buscar(cargar(), "capaFormulario");

        assertEquals("false", capa.getAttribute("visible"));
        assertEquals("false", capa.getAttribute("managed"));
    }

    @Test
    void nuevoYEditarTienenAccionesIndependientes()
            throws Exception {

        Element raiz = cargar();

        assertEquals(
                "#onNuevo",
                buscar(raiz, "btnLimpiar")
                        .getAttribute("onAction"));

        assertEquals(
                "#onEditarSeleccionado",
                buscar(raiz, "btnEditarCatalogo")
                        .getAttribute("onAction"));

        assertEquals(
                "#onGuardar",
                buscar(raiz, "btnRegistrarFormulario")
                        .getAttribute("onAction"));
    }
}
