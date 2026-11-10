package org.example.carestock;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.carestock.session.SesionUsuario;
import org.example.carestock.controller.ControladorInventario;
import org.example.carestock.controller.ConsultaInventarioController;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.service.ResolucionNavegacionAlertas;
import org.example.carestock.service.PoliticaAccionesAlertas;
import org.example.carestock.dto.AlertaInventario.Categoria;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import org.example.carestock.config.PrecalentadorConexionBD;

import java.net.URL;

public class CareStockApp extends Application {
    private static final String VISTA_INVENTARIO = "/org/example/carestock/inventario-view.fxml";

    private static final AtomicReference<CareStockApp> INSTANCE = new AtomicReference<>();

    private static final System.Logger LOGGER = System.getLogger(CareStockApp.class.getName());
    private Stage primaryStage;
    private ControladorInventario controladorDashboard;

    private record DestinoCatalogo(
            String categoria,
            Integer id
    ) { }

    private DestinoCatalogo destinoCatalogo;

    public static CareStockApp getInstance() {
        return INSTANCE.get();
    }

    @Override
    public void start(Stage stage) throws Exception {
        INSTANCE.set(this);
        this.primaryStage = stage;

        stage.setTitle("CareStock - Control de Inventario y Farmacovigilancia");
        stage.setResizable(true);

        mostrarLogin();
        stage.show();
        // Prepara la conexion sin bloquear el hilo de JavaFX.
        PrecalentadorConexionBD.iniciarAsync();

        // Iniciar maximizado, respetando la resolucion del monitor.
        stage.setMaximized(true);

        // F11 activa o desactiva el modo Full-Screen real.
        // ESC permite salir de Full-Screen.
        stage.getScene().addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED,
                evento -> {
                    if (evento.getCode() == javafx.scene.input.KeyCode.F11) {
                        stage.setFullScreen(!stage.isFullScreen());
                        evento.consume();
                    }
                }
        );
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
                VISTA_INVENTARIO,
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

        if (SesionUsuario.actual().idFarmacia() == null) {
            Alert aviso = new Alert(
                    Alert.AlertType.WARNING,
                    "Se requiere una farmacia asignada para consultar el catalogo.");

            aviso.setHeaderText(null);
            aviso.showAndWait();
            return;
        }

        DestinoCatalogo destino = destinoCatalogo;
        destinoCatalogo = null;

        String categoria = destino == null
                ? null : destino.categoria();

        Integer id = destino == null
                ? null : destino.id();

        if (controladorDashboard != null) {
            controladorDashboard.abrirCatalogoIntegrado(
                    categoria, id);
        } else {
            transicionarEscena(
                    VISTA_INVENTARIO,
                    1100, 750,
                    controlador -> ((ControladorInventario) controlador)
                            .abrirCatalogoIntegrado(categoria, id));
        }
    }

    public void mostrarCatalogoParaRegistro(String categoria) {
        abrirFormularioCatalogo(categoria, null);
    }

    public void mostrarCatalogoParaEditar(
            String categoria,
            int id
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID de producto invalido");
        }

        abrirFormularioCatalogo(categoria, id);
    }

    private void abrirFormularioCatalogo(
            String categoria,
            Integer id
    ) {
        if (!SesionUsuario.estaActiva()
                || !SesionUsuario.esAdministrador()
                || SesionUsuario.actual().idFarmacia() == null) {

            Alert aviso = new Alert(
                    Alert.AlertType.WARNING,
                    "No tienes permisos para modificar productos."
            );

            aviso.setHeaderText(null);
            aviso.showAndWait();
            return;
        }

        destinoCatalogo = new DestinoCatalogo(categoria, id);
        mostrarCatalogo();
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
    
    public void mostrarDetalleAlerta(
            AlertaInventario alerta, Integer farmaciaOrigen) {

        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        var destino = ResolucionNavegacionAlertas.resolver(
                alerta, farmaciaOrigen,
                SesionUsuario.actual().idFarmacia());

        if (destino.tipo() == TipoAlerta.STOCK_MINIMO) {
            transicionarEscena(
                    "/org/example/carestock/consulta-inventario-view.fxml",
                    1120, 720,
                    controlador -> ((ConsultaInventarioController) controlador)
                            .abrirDesdeAlerta(destino));
        } else {
            transicionarEscena(
                    VISTA_INVENTARIO,
                    1100, 750,
                    controlador -> ((ControladorInventario) controlador)
                            .abrirDesdeAlerta(destino));
        }
    }

    /** Abre una operación existente; nunca modifica inventario al navegar. */
    public void mostrarGestionAlerta(AlertaInventario alerta, Integer farmaciaOrigen) {
        if (!SesionUsuario.estaActiva()) {
            throw new SecurityException("Debe iniciar sesión para gestionar alertas.");
        }
        var sesion = SesionUsuario.actual();
        var accion = PoliticaAccionesAlertas.exigir(alerta, sesion, farmaciaOrigen);
        var destino = ResolucionNavegacionAlertas.resolver(
                alerta, farmaciaOrigen, sesion.idFarmacia());

        if (accion == PoliticaAccionesAlertas.Accion.GESTIONAR_LOTE) {
            transicionarEscena(
                    VISTA_INVENTARIO, 1100, 750,
                    controlador -> ((ControladorInventario) controlador)
                            .abrirDesdeAlerta(destino));
        } else if (destino.categoria() == Categoria.MEDICAMENTO) {
            transicionarEscena(
                    VISTA_INVENTARIO, 1100, 750,
                    controlador -> ((ControladorInventario) controlador)
                            .abrirGestionStockMedicamento(destino));
        } else {
            transicionarEscena(
                    "/org/example/carestock/movimientos-view.fxml", 1100, 750,
                    controlador -> ((org.example.carestock.controller.ControladorMovimientos) controlador)
                            .abrirDesdeAlerta(destino));
        }
    }

    public void mostrarFormularioMovimientos() {

        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        var sesion = SesionUsuario.actual();
        if (sesion.idFarmacia() == null || sesion.idFarmacia() <= 0
                || !PoliticaAccionesAlertas.gestorPermitido(sesion.rol())) {

            Alert alerta = new Alert(
                    Alert.AlertType.WARNING,
                    "Solo administradores y farmacéuticos de la farmacia activa pueden registrar movimientos."
            );

            alerta.setHeaderText(null);
            alerta.showAndWait();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/movimientos-view.fxml",
                1100,
                750
        );
    }

    public void mostrarFormularioAjustes() {
        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }
        var sesion = SesionUsuario.actual();
        String rol = sesion.rol() == null ? "" : sesion.rol().trim();
        boolean admin = "ADMINISTRADOR".equalsIgnoreCase(rol)
                || "SUPER_ADMIN".equalsIgnoreCase(rol);
        if (!admin || sesion.idFarmacia() == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING,
                    "Solo administradores con farmacia asignada pueden ajustar existencias.");
            alerta.setHeaderText(null);
            alerta.showAndWait();
            return;
        }
        transicionarEscena("/org/example/carestock/ajustes-view.fxml", 1100, 750);
    }

    public void mostrarHistorialMovimientos() {
        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        var sesion = SesionUsuario.actual();
        String rol = sesion.rol() == null ? "" : sesion.rol().trim();
        boolean autorizado = "ADMINISTRADOR".equalsIgnoreCase(rol)
                || "FARMACEUTICO".equalsIgnoreCase(rol);

        if (!autorizado || sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0) {
            Alert aviso = new Alert(Alert.AlertType.WARNING,
                    "Solo Administrador y Farmaceutico con farmacia asignada pueden consultar el historial.");
            aviso.setHeaderText(null);
            aviso.showAndWait();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/historial-movimientos-view.fxml",
                1200, 760);
    }

    public void mostrarConfiguracionSms() {
        if (!SesionUsuario.estaActiva()
                || !SesionUsuario.esAdministrador()
                || SesionUsuario.actual().idFarmacia() == null) {
            Alert aviso = new Alert(Alert.AlertType.WARNING,
                    "Solo administradores con farmacia asignada pueden configurar SMS.");
            aviso.setHeaderText(null);
            aviso.showAndWait();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/preferencias-sms-view.fxml",
                1100, 750);
    }

    /** Abre la planificación solo para administradores. */
    public void mostrarReposicion() {
        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        var sesion = SesionUsuario.actual();

        if (!sesion.esAdministrador()
                || sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0) {

            Alert aviso = new Alert(
                    Alert.AlertType.WARNING,
                    "Solo administradores con farmacia activa "
                    + "pueden consultar la reposición.");

            aviso.setHeaderText(null);
            aviso.showAndWait();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/reposicion-view.fxml",
                1180, 750);
    }

    public void mostrarAlertas() {
        if (!SesionUsuario.estaActiva()) {
            mostrarLogin();
            return;
        }

        Integer farmacia = SesionUsuario.actual().idFarmacia();
        if (farmacia == null || farmacia <= 0) {
            Alert aviso = new Alert(Alert.AlertType.WARNING,
                    "Se requiere una farmacia asignada para consultar las alertas.");
            aviso.setHeaderText(null);
            aviso.showAndWait();
            return;
        }

        transicionarEscena(
                "/org/example/carestock/alertas-view.fxml",
                1240, 760);
    }

    public void cerrarSesion() {

        SesionUsuario.cerrar();

        mostrarLogin();
    }

    // ========================================
    // TRANSICION ENTRE VISTAS
    // ========================================

    void transicionarEscena(String fxmlRuta, double ancho, double alto) {
        transicionarEscena(fxmlRuta, ancho, alto, null);
    }

    private void transicionarEscena(
            String fxmlRuta, double ancho, double alto,
            Consumer<Object> configuracion) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlRuta);

            if (fxmlUrl == null) {
                LOGGER.log(System.Logger.Level.WARNING, "No se encontro el archivo FXML: {0}", fxmlRuta);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Object controlador = loader.getController();
            controladorDashboard =
                    controlador instanceof ControladorInventario panel
                            ? panel : null;

            if (configuracion != null) {
                configuracion.accept(loader.getController());
            }

            root.setOpacity(0);

            // Reutilizar la Scene para conservar el estado de ventana.
            Scene scene = primaryStage.getScene();

            if (scene == null) {
                // Dimensiones iniciales, solo al crear la Scene.
                primaryStage.setScene(
                        new Scene(root, ancho, alto)
                );
            } else {
                // Cambiar la vista sin cambiar el Stage ni su estado.
                scene.setRoot(root);
            }

            FadeTransition fadeIn = new FadeTransition(
                    Duration.millis(400),
                    root
            );

            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Error al cargar la pantalla: " + fxmlRuta, e);
            throw new IllegalStateException("No se pudo cargar la vista " + fxmlRuta, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
