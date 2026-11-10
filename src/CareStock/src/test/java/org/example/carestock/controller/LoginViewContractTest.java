package org.example.carestock.controller;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginViewContractTest {

    private static final String FXML =
            "/org/example/carestock/login-view.fxml";

    private static final String FX_NS =
            "http://javafx.com/fxml/1";

    private Document documento() throws Exception {
        var fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);

        fabrica.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true);

        fabrica.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, "");

        try (var recurso = getClass().getResourceAsStream(FXML)) {
            assertNotNull(recurso);
            return fabrica.newDocumentBuilder().parse(recurso);
        }
    }

    private Element control(Document documento, String id) {
        NodeList elementos = documento.getElementsByTagName("*");
        Element encontrado = null;
        int coincidencias = 0;

        for (int i = 0; i < elementos.getLength(); i++) {
            Element elemento = (Element) elementos.item(i);

            if (id.equals(elemento.getAttributeNS(FX_NS, "id"))) {
                coincidencias++;
                encontrado = elemento;
            }
        }

        assertEquals(1, coincidencias,
                "El control debe existir una sola vez: " + id);

        return encontrado;
    }

    @Test
    void declaraElControladorCorrecto() throws Exception {
        Document documento = documento();

        assertEquals(
                "org.example.carestock.controller.LoginController",
                documento.getDocumentElement()
                        .getAttributeNS(FX_NS, "controller"));
    }

    @Test
    void controlesTienenCamposEnElControlador()
            throws Exception {

        Document documento = documento();

        for (String id : List.of(
                "txtEmail",
                "txtPassword",
                "txtPasswordVisible",
                "chkRecordarme",
                "btnIngresar",
                "btnMostrarPassword",
                "loaderCarga",
                "lblNotificacion",
                "lblEmailError",
                "lblPasswordError")) {

            control(documento, id);

            assertNotNull(
                    LoginController.class.getDeclaredField(id),
                    "Falta el campo Java: " + id);
        }
    }

    @Test
    void botonesYCamposReferencianMetodosExistentes()
            throws Exception {

        Document documento = documento();

        Map<String, String> acciones = Map.of(
                "btnIngresar", "onIngresar",
                "btnMostrarPassword", "onTogglePassword",
                "txtEmail", "onEmailAction",
                "txtPassword", "onPasswordAction",
                "txtPasswordVisible", "onPasswordAction"
        );

        for (var accion : acciones.entrySet()) {
            Element elemento = control(
                    documento, accion.getKey());

            assertEquals(
                    "#" + accion.getValue(),
                    elemento.getAttribute("onAction"));

            assertNotNull(
                    LoginController.class.getDeclaredMethod(
                            accion.getValue()));
        }
    }

    @Test
    void elementosSensiblesComienzanOcultos()
            throws Exception {

        Document documento = documento();

        for (String id : List.of(
                "txtPasswordVisible",
                "loaderCarga",
                "lblNotificacion",
                "lblEmailError",
                "lblPasswordError")) {

            Element elemento = control(documento, id);

            assertEquals("false",
                    elemento.getAttribute("visible"));

            assertEquals("false",
                    elemento.getAttribute("managed"));
        }

        assertEquals("true",
                control(documento, "btnIngresar")
                        .getAttribute("defaultButton"));
    }

    @Test
    void cssIncluyeEstadosVisualesNecesarios()
            throws Exception {

        assertEquals("@login.css",
                documento().getDocumentElement()
                        .getAttribute("stylesheets"));

        try (var recurso = getClass().getResourceAsStream(
                "/org/example/carestock/login.css")) {

            assertNotNull(recurso);

            String css = new String(
                    recurso.readAllBytes(),
                    StandardCharsets.UTF_8);

            for (String estado : List.of(
                    ".login-action:hover",
                    ".login-action:pressed",
                    ".login-action:disabled",
                    ".login-input:invalido",
                    ".login-eye:hover")) {

                assertTrue(css.contains(estado),
                        "Falta el estilo " + estado);
            }
        }
    }
}
