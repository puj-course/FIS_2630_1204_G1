package org.example.carestock;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

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

        stage.setTitle("CareStock - Control de Inventario y Farmacovigilancia");
        stage.setResizable(true);

        mostrarLogin();
        stage.show();
    }

    public void mostrarLogin() {
        transicionarEscena("/org/example/carestock/login-view.fxml", 1100, 650);
    }

    public void mostrarDashboard() {
        transicionarEscena("/org/example/carestock/inventario-view.fxml", 950, 650);
    }

    /**
     * Método genérico para realizar una transición suave (Fade-In) entre vistas FXML
     */
    private void transicionarEscena(String fxmlRuta, double ancho, double alto) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlRuta);
            if (fxmlUrl == null) {
                System.err.println("¡No se encontró el archivo FXML en la ruta: " + fxmlRuta + "!");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root, ancho, alto);

            // Ocultamos inicialmente el contenedor raíz para la animación de entrada
            root.setOpacity(0);

            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

            // Animación de desvanecimiento suave (Fade-In de 400 milisegundos)
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}