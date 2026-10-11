
package org.example.carestock;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.carestock.session.SesionUsuario;

import java.net.URL;

public class CareStockApp extends Application {

    private static CareStockApp instance;

    private Stage primaryStage;

    public static CareStockApp getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) throws Exception {

        instance = this;
        this.primaryStage = stage;

        stage.setTitle(
                "CareStock - Control de Inventario y Farmacovigilancia"
        );

        stage.setResizable(true);

        mostrarLogin();

        stage.show();
    }

    // ========================================
    // NAVEGACION: LOGIN
    // ========================================

    public void mostrarLogin() {

        SesionUsuario.cerrar();

        transicionarEscena(
                "/org/example/carestock/login-view.fxml",
                1100,
                650
        );
    }

    // ========================================
    // NAVEGACION: DASHBOARD
    // ========================================

    public void mostrarDashboard() {

        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/inventario-view.fxml",
                1100,
                750
        );
    }

    // ========================================
    // NAVEGACION: CATALOGO CRUD
    // ========================================

    public void mostrarCatalogo() {

        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/catalogo-view.fxml",
                1150,
                760
        );
    }

    // ========================================
    // NAVEGACION: CONSULTA UNIFICADA
    // ========================================

    /**
     * Abre la consulta unificada del inventario.
     *
     * Permite visualizar medicamentos, aseo
     * y maternidad de la farmacia autenticada.
     *
     * Es una pantalla de solo consulta.
     */
    public void mostrarConsultaUnificada() {

        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/consulta-inventario-view.fxml",
                1120,
                720
        );
    }

    // ========================================
    // CERRAR SESION
    // ========================================

    public void cerrarSesion() {

        SesionUsuario.cerrar();

        mostrarLogin();
    }

    // ========================================
    // TRANSICION ENTRE VISTAS
    // ========================================

    private void transicionarEscena(
            String fxmlRuta,
            double ancho,
            double alto
    ) {

        try {

            URL fxmlUrl = getClass().getResource(fxmlRuta);

            if (fxmlUrl == null) {

                System.err.println(
                        "No se encontro el archivo FXML: "
                                + fxmlRuta
                );

                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            Parent root = loader.load();

            Scene scene = new Scene(
                    root,
                    ancho,
                    alto
            );

            root.setOpacity(0);

            primaryStage.setScene(scene);

            primaryStage.centerOnScreen();

            FadeTransition fadeIn =
                    new FadeTransition(
                            Duration.millis(400),
                            root
                    );

            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (Exception e) {

            System.err.println(
                    "Error al cargar la pantalla: " + fxmlRuta
            );

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
