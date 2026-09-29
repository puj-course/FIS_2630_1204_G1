package com.carestock.view;

import com.carestock.dao.PreferenciaSesionDAO;

import com.carestock.model.PreferenciaSesion;

import com.carestock.session.IdleSessionManager;

import javafx.animation.PauseTransition;

import com.carestock.controller.IngresoLoteController;
import com.carestock.controller.SessionController;
import com.carestock.dao.LoteDAO;
import com.carestock.dao.MedicamentoDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Medicamento;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.util.Duration;

public class MainDashboardFX extends Application {

    private Stage primaryStage;

    private final IdleSessionManager idleSessionManager =
            IdleSessionManager.getInstance();

    private final SessionController sessionController =
            new SessionController();

    private final TableView<Medicamento> tablaInventario =
            new TableView<>();

    private final ObservableList<Medicamento> listaMedicamentos =
            FXCollections.observableArrayList();

    private final FilteredList<Medicamento> listaFiltrada =
            new FilteredList<>(
                    listaMedicamentos,
                    p -> true
            );

    private final MedicamentoDAO medicamentoDAO =
            new MedicamentoDAO();

    private final LoteDAO loteDAO =
            new LoteDAO();

    private final PreferenciaSesionDAO preferenciaSesionDAO =
            new PreferenciaSesionDAO();

    private PreferenciaSesion preferenciaSesionActual;

    private final Label lblTotalStock =
            new Label("0");

    private final Label lblProximosVencer =
            new Label("0");

    private final Label lblAlertasCriticas =
            new Label("0");

    private Button btnFiltrarCriticos;

    private boolean filtrandoCriticos = false;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage =
                primaryStage;

        if (!ProtectedNavigationGuard.ensureAuthenticated(
                primaryStage
        )) {
            return;
        }

        if (!UserSession.getInstance().isLoggedIn()) {

            AlertUtil.mostrarSesionExpirada();

            redirigirAlLogin();

            return;
        }

        BorderPane root =
                new BorderPane();

        root.setLeft(
                buildSidebar()
        );

        VBox mainContent =
                new VBox(20);

        mainContent.setPadding(
                new Insets(20)
        );

        mainContent.setStyle(
                "-fx-background-color: #F8F9FA;"
        );

        mainContent
                .getChildren()
                .addAll(
                        buildTopbar(),
                        buildMetricCards(),
                        buildTableSection()
                );

        root.setCenter(
                mainContent
        );

        Scene scene =
                new Scene(
                        root,
                        1200,
                        700
                );

        primaryStage.setTitle(
                "CareStock - Gestión de Inventario"
        );

        primaryStage.setScene(scene);

        primaryStage.setResizable(true);

        primaryStage.show();

        configurarTemporizadorSegunPreferencia();

        cargarDatosDesdeBD();
    }

    public void cargarDatosDesdeBD() {

        if (!ProtectedNavigationGuard.ensureAuthenticated(
                primaryStage
        )) {
            return;
        }

        try {

            int idFarmacia =
                    new SessionContext()
                            .requireAuthenticatedPharmacyId();

            List<Medicamento> desdeBD =
                    medicamentoDAO
                            .obtenerPorFarmacia(
                                    idFarmacia
                            );

            int totalStock =
                    medicamentoDAO
                            .obtenerTotalUnidadesStockPorFarmacia(
                                    idFarmacia
                            );

            int alertasCriticas =
                    medicamentoDAO
                            .obtenerAlertasCriticasPorFarmacia(
                                    idFarmacia
                            );

            int proximosVencer =
                    loteDAO
                            .contarProximosAVencerPorFarmacia(
                                    30,
                                    idFarmacia
                            );

            listaMedicamentos.setAll(
                    desdeBD
            );

            lblTotalStock.setText(
                    String.format(
                            "%,d",
                            totalStock
                    )
            );

            lblAlertasCriticas.setText(
                    String.valueOf(
                            alertasCriticas
                    )
            );

            lblProximosVencer.setText(
                    String.valueOf(
                            proximosVencer
                    )
            );

        } catch (
                SQLException
                | IllegalStateException e
        ) {

            listaMedicamentos.clear();

            lblTotalStock.setText(
                    "0"
            );

            lblAlertasCriticas.setText(
                    "0"
            );

            lblProximosVencer.setText(
                    "0"
            );

            System.err.println(
                    "No fue posible cargar el inventario "
                    + "de la farmacia activa: "
                    + e.getMessage()
            );
        }
    }


    private VBox buildSidebar() {

        VBox sidebar =
                new VBox(15);

        sidebar.setPadding(
                new Insets(20)
        );

        sidebar.setPrefWidth(210);

        sidebar.setStyle(
                "-fx-background-color: #A3D9D2;"
        );

        Label logo =
                new Label("CareStock");

        logo.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1C313A;"
        );

        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        Label lblNombre =
                new Label(
                        usuario.getNombre()
                );

        lblNombre.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1C313A;"
        );

        Label lblRol =
                new Label(
                        usuario.getRol()
                );

        lblRol.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #546E7A;"
        );

        Button btnDashboard =
                new Button("Dashboard");

        btnDashboard.setMaxWidth(
                Double.MAX_VALUE
        );

        btnDashboard.setStyle(
                "-fx-background-color: #B39DDB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        Button btnIngresoLote =
                new Button(
                        "Ingreso de lotes"
                );

        btnIngresoLote.setMaxWidth(
                Double.MAX_VALUE
        );

        btnIngresoLote.setStyle(
                "-fx-background-color: #D1C4E9;" +
                "-fx-text-fill: #37474F;" +
                "-fx-font-weight: bold;"
        );

        btnIngresoLote.setVisible(
                "ADMINISTRADOR".equalsIgnoreCase(
                        usuario.getRol()
                )
        );

        btnIngresoLote.setManaged(
                btnIngresoLote.isVisible()
        );

        btnIngresoLote.setOnAction(
                e -> abrirIngresoLote()
        );

        btnFiltrarCriticos =
                new Button(
                        "Ver alertas críticas"
                );

        btnFiltrarCriticos.setMaxWidth(
                Double.MAX_VALUE
        );

        btnFiltrarCriticos.setStyle(
                "-fx-background-color: #FFCDD2;" +
                "-fx-text-fill: #C62828;" +
                "-fx-font-weight: bold;"
        );

        btnFiltrarCriticos.setOnAction(
                e -> alternarFiltroCriticos()
        );

        Button btnGestionInventario =
                new Button(
                        "Gestionar inventario"
                );

        btnGestionInventario.setMaxWidth(
                Double.MAX_VALUE
        );

        btnGestionInventario.setStyle(
                "-fx-background-color: #B2DFDB;"
                + "-fx-text-fill: #004D40;"
                + "-fx-font-weight: bold;"
        );

        btnGestionInventario.setVisible(
                "SUPER_ADMIN".equalsIgnoreCase(
                        usuario.getRol()
                )
                || "ADMINISTRADOR".equalsIgnoreCase(
                        usuario.getRol()
                )
        );

        btnGestionInventario.setManaged(
                btnGestionInventario.isVisible()
        );

        btnGestionInventario.setOnAction(
                e -> abrirGestionInventario()
        );


        Button btnConfiguracionSesion =
                new Button(
                        "Configuración de sesión"
                );

        btnConfiguracionSesion.setMaxWidth(
                Double.MAX_VALUE
        );

        btnConfiguracionSesion.setStyle(
                "-fx-background-color: #ECEFF1;" +
                "-fx-text-fill: #37474F;" +
                "-fx-font-weight: bold;"
        );

        btnConfiguracionSesion.setOnAction(
                e -> abrirConfiguracionSesion()
        );

        Button btnHistorialAccesos =
                new Button(
                        "Historial de accesos"
                );

        btnHistorialAccesos.setMaxWidth(
                Double.MAX_VALUE
        );

        btnHistorialAccesos.setVisible(
                "SUPER_ADMIN".equalsIgnoreCase(
                        usuario.getRol()
                )
                || "ADMINISTRADOR".equalsIgnoreCase(
                        usuario.getRol()
                )
        );

        btnHistorialAccesos.setManaged(
                btnHistorialAccesos.isVisible()
        );

        btnHistorialAccesos.setOnAction(
                e -> abrirHistorialAccesos()
        );

        Button btnCrearUsuario =
                new Button(
                        "Crear usuario"
                );

        btnCrearUsuario.setMaxWidth(
                Double.MAX_VALUE
        );

        btnCrearUsuario.setStyle(
                "-fx-background-color: #C5E1A5;"
                + "-fx-text-fill: #33691E;"
                + "-fx-font-weight: bold;"
        );

        btnCrearUsuario.setVisible(
                "SUPER_ADMIN".equalsIgnoreCase(
                        usuario.getRol()
                )
                || "ADMINISTRADOR".equalsIgnoreCase(
                        usuario.getRol()
                )
        );

        btnCrearUsuario.setManaged(
                btnCrearUsuario.isVisible()
        );

        btnCrearUsuario.setOnAction(
                e -> abrirCrearUsuario()
        );

        sidebar
                .getChildren()
                .addAll(
                        logo,
                        new Separator(),
                        lblNombre,
                        lblRol,
                        new Separator(),
                        btnDashboard,
                        btnIngresoLote,
                        btnFiltrarCriticos,
                        btnGestionInventario,
                        btnConfiguracionSesion,
                        btnHistorialAccesos,
                        btnCrearUsuario
                );

        return sidebar;
    }

    private HBox buildTopbar() {

        HBox topbar =
                new HBox(10);

        topbar.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label(
                        "Dashboard general"
                );

        title.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        Label lblUsuario =
                new Label(
                        "Sesión: "
                        + usuario.getNombre()
                        + " | "
                        + usuario.getRol()
                );

        lblUsuario.setStyle(
                "-fx-text-fill: #607D8B;" +
                "-fx-font-size: 12px;"
        );

        Button btnCerrarSesion =
                new Button(
                        "Cerrar sesión"
                );

        btnCerrarSesion.setStyle(
                "-fx-background-color: #ECEFF1;" +
                "-fx-text-fill: #C62828;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;"
        );

        btnCerrarSesion.setOnAction(
                e -> solicitarCierreSesion()
        );

        topbar
                .getChildren()
                .addAll(
                        title,
                        spacer,
                        lblUsuario,
                        btnCerrarSesion
                );

        return topbar;
    }

    private void alternarFiltroCriticos() {

        filtrandoCriticos =
                !filtrandoCriticos;

        if (filtrandoCriticos) {

            listaFiltrada.setPredicate(
                    m ->
                            m.getStockTotal() != null
                            && m.getStockMinimo() != null
                            && m.getStockTotal()
                            <= m.getStockMinimo()
            );

            btnFiltrarCriticos.setText(
                    "Ver todos los medicamentos"
            );

            btnFiltrarCriticos.setStyle(
                    "-fx-background-color: #E0E0E0;" +
                    "-fx-text-fill: #333333;" +
                    "-fx-font-weight: bold;"
            );

        } else {

            listaFiltrada.setPredicate(
                    p -> true
            );

            btnFiltrarCriticos.setText(
                    "Ver alertas críticas"
            );

            btnFiltrarCriticos.setStyle(
                    "-fx-background-color: #FFCDD2;" +
                    "-fx-text-fill: #C62828;" +
                    "-fx-font-weight: bold;"
            );
        }
    }

    private HBox buildMetricCards() {

        HBox container =
                new HBox(15);

        container
                .getChildren()
                .addAll(

                        createCard(
                                lblTotalStock,
                                "Unidades en stock"
                        ),

                        createCard(
                                lblProximosVencer,
                                "Lotes próximos a vencer (30 días)"
                        ),

                        createCard(
                                lblAlertasCriticas,
                                "Alertas críticas"
                        )
                );

        return container;
    }

    private VBox createCard(
            Label numLabel,
            String label
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(15)
        );

        card.setPrefWidth(230);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8;"
        );

        numLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Label subText =
                new Label(label);

        subText.setStyle(
                "-fx-text-fill: #7F8C8D;" +
                "-fx-font-size: 11px;"
        );

        card
                .getChildren()
                .addAll(
                        numLabel,
                        subText
                );

        return card;
    }

    private VBox buildTableSection() {

        VBox section =
                new VBox(10);

        section.setPadding(
                new Insets(15)
        );

        section.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 8;"
        );

        Label lblSection =
                new Label(
                        "Inventario PostgreSQL"
                );

        lblSection.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        TableColumn<Medicamento, Long> colId =
                new TableColumn<>("ID");

        colId.setCellValueFactory(
                new PropertyValueFactory<>(
                        "idMedicamento"
                )
        );

        colId.setPrefWidth(50);

        TableColumn<Medicamento, String> colInvima =
                new TableColumn<>("INVIMA");

        colInvima.setCellValueFactory(
                new PropertyValueFactory<>(
                        "codigoInvima"
                )
        );

        TableColumn<Medicamento, String> colNombre =
                new TableColumn<>("NOMBRE");

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>(
                        "nombreComercial"
                )
        );

        TableColumn<Medicamento, String> colPrincipio =
                new TableColumn<>("PRINCIPIO");

        colPrincipio.setCellValueFactory(
                new PropertyValueFactory<>(
                        "principioActivo"
                )
        );

        TableColumn<Medicamento, String> colCategoria =
                new TableColumn<>("CATEGORÍA");

        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>(
                        "categoria"
                )
        );

        TableColumn<Medicamento, Integer> colStock =
                new TableColumn<>("STOCK");

        colStock.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockTotal"
                )
        );

        TableColumn<Medicamento, Integer> colStockMin =
                new TableColumn<>("STOCK MÍN");

        colStockMin.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockMinimo"
                )
        );

        tablaInventario
                .getColumns()
                .setAll(
                        colId,
                        colInvima,
                        colNombre,
                        colPrincipio,
                        colCategoria,
                        colStock,
                        colStockMin
                );

        tablaInventario.setItems(
                listaFiltrada
        );

        tablaInventario.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        section
                .getChildren()
                .addAll(
                        lblSection,
                        tablaInventario
                );

        return section;
    }

    

    private void abrirGestionInventario() {

        if (!validarSesionActiva()) {
            return;
        }


        new GestionInventarioFX()
                .mostrar(
                        primaryStage
                );


        cargarDatosDesdeBD();
    }


    private void abrirIngresoLote() {

        if (!validarSesionActiva()) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/carestock/view/IngresoLote.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            IngresoLoteController controller =
                    loader.getController();

            controller.setOnSaved(
                    this::cargarDatosDesdeBD
            );

            controller.setOnSessionExpired(
                    this::redirigirAlLogin
            );

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "CareStock - Ingreso de lote"
            );

            stage.initModality(
                    Modality.WINDOW_MODAL
            );

            if (
                    tablaInventario.getScene()
                    != null
            ) {

                stage.initOwner(
                        tablaInventario
                                .getScene()
                                .getWindow()
                );
            }

            stage.setScene(
                    new Scene(root)
            );

            stage.setResizable(false);

            stage.showAndWait();

        } catch (
                IOException
                | RuntimeException e
        ) {

            AlertUtil.mostrarError(
                    "No fue posible abrir el formulario "
                    + "de ingreso de lote. "
                    + e.getMessage()
            );
        }
    }

    


    private void abrirHistorialAccesos() {

        if (!validarSesionActiva()) {
            return;
        }

        HistorialAccesosFX historial = new HistorialAccesosFX();

        Window owner =
                tablaInventario.getScene() != null
                        ? tablaInventario.getScene().getWindow()
                        : null;

        historial.mostrar(owner);
    }

    private void abrirCrearUsuario() {

        if (!validarSesionActiva()) {
            return;
        }

        CrearUsuarioFX crearUsuario = new CrearUsuarioFX();

        Window owner =
                tablaInventario.getScene() != null
                        ? tablaInventario.getScene().getWindow()
                        : null;

        crearUsuario.mostrar(owner);
    }

    private boolean validarSesionActiva() {

        return ProtectedNavigationGuard
                .ensureAuthenticated(
                        primaryStage
                );
    }

    private void manejarSesionExpirada() {

        AlertUtil.mostrarSesionExpirada();

        redirigirAlLogin();
    }

    private void redirigirAlLogin() {

        try {

            LoginFX login =
                    new LoginFX();

            login.start(
                    primaryStage
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    "No fue posible regresar a la pantalla "
                    + "de inicio de sesión."
            );

            System.err.println(
                    "Error redirigiendo al Login: "
                    + e.getMessage()
            );
        }
    }

    private void solicitarCierreSesion() {

        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.initOwner(primaryStage);
        confirmacion.setTitle("CareStock");
        confirmacion.setHeaderText("Cerrar sesión");
        confirmacion.setContentText(
                "¿Estás seguro que deseas cerrar sesión?"
        );

        ButtonType btnConfirmar =
                new ButtonType(
                        "Cerrar sesión"
                );

        ButtonType btnCancelar =
                new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        confirmacion
                .getButtonTypes()
                .setAll(
                        btnConfirmar,
                        btnCancelar
                );

        Optional<ButtonType> respuesta =
                confirmacion.showAndWait();

        if (
                respuesta.isPresent()
                && respuesta.get() == btnConfirmar
        ) {
            cerrarSesion();
        }
    }

    private void cerrarSesion() {

        idleSessionManager.stopMonitoring();

        sessionController.cerrarSesion();

        redirigirAlLogin();

        mostrarNotificacionCierreSeguro();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void mostrarNotificacionCierreSeguro() {

        Alert notificacion =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        notificacion.initOwner(
                primaryStage
        );

        notificacion.setTitle(
                "CareStock"
        );

        notificacion.setHeaderText(
                "Cierre de sesión exitoso"
        );

        notificacion.setContentText(
                "Sesión finalizada de forma segura"
        );

        notificacion
                .getDialogPane()
                .setStyle(
                        "-fx-background-color: #E8F5E9;"
                );

        notificacion.show();

        PauseTransition cierreAutomatico =
                new PauseTransition(
                        Duration.seconds(3)
                );

        cierreAutomatico.setOnFinished(
                event -> notificacion.close()
        );

        cierreAutomatico.play();
    }

    private void configurarTemporizadorSegunPreferencia() {

        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (usuario == null) {
            return;
        }

        try {

            preferenciaSesionActual =
                    preferenciaSesionDAO
                            .obtenerOCrearPorUsuario(
                                    usuario.getId()
                            );

        } catch (SQLException e) {

            preferenciaSesionActual =
                    PreferenciaSesion
                            .porDefecto(
                                    usuario.getId()
                            );

            System.err.println(
                    "No fue posible cargar las preferencias "
                    + "de sesión. Se aplicará el valor "
                    + "predeterminado: "
                    + e.getMessage()
            );
        }

        aplicarPreferenciaSesion(
                preferenciaSesionActual
        );
    }

    private void abrirConfiguracionSesion() {

        if (!validarSesionActiva()) {
            return;
        }

        UserSession.CurrentUser usuario =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (usuario == null) {
            return;
        }

        try {

            PreferenciaSesion actual =
                    preferenciaSesionDAO
                            .obtenerOCrearPorUsuario(
                                    usuario.getId()
                            );

            ConfiguracionSesionDialog dialog =
                    new ConfiguracionSesionDialog(
                            actual
                    );

            dialog.initOwner(
                    primaryStage
            );

            Optional<PreferenciaSesion> resultado =
                    dialog.showAndWait();

            if (resultado.isEmpty()) {
                return;
            }

            PreferenciaSesion nuevaPreferencia =
                    resultado.get();

            preferenciaSesionDAO.guardar(
                    nuevaPreferencia
            );

            preferenciaSesionActual =
                    nuevaPreferencia;

            aplicarPreferenciaSesion(
                    nuevaPreferencia
            );

            String estado =
                    nuevaPreferencia.isTimeoutActivo()
                            ? "activado a "
                              + nuevaPreferencia
                                    .getTimeoutMinutos()
                              + " minuto(s)."
                            : "desactivado.";

            AlertUtil.mostrarExito(
                    "La configuración de sesión fue "
                    + "guardada correctamente. "
                    + "El cierre por inactividad quedó "
                    + estado
            );

        } catch (SQLException e) {

            AlertUtil.mostrarError(
                    "No fue posible guardar la configuración "
                    + "de sesión. "
                    + e.getMessage()
            );
        }
    }

    private void aplicarPreferenciaSesion(
            PreferenciaSesion preferencia
    ) {

        idleSessionManager.stopMonitoring();

        if (!preferencia.isTimeoutActivo()) {

            System.out.println(
                    "Timeout por inactividad desactivado "
                    + "para el usuario "
                    + preferencia.getIdUsuario()
            );

            return;
        }

        idleSessionManager.startMonitoring(
                preferencia.getTimeoutMinutos(),
                this::cerrarSesionPorInactividad
        );

        System.out.println(
                "Timeout por inactividad configurado en "
                + preferencia.getTimeoutMinutos()
                + " minuto(s)."
        );
    }

    private void cerrarVentanasSecundarias() {

        for (
                Window window
                : List.copyOf(
                        Window.getWindows()
                )
        ) {

            if (
                    window != primaryStage
                    && window.isShowing()
            ) {

                window.hide();
            }
        }
    }

    private void cerrarSesionPorInactividad() {

        idleSessionManager.stopMonitoring();

        sessionController.cerrarSesion();

        cerrarVentanasSecundarias();

        redirigirAlLogin();

        mostrarNotificacionSesionCaducada();
    }

    private void mostrarNotificacionSesionCaducada() {

        Alert alerta =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alerta.initOwner(
                primaryStage
        );

        alerta.setTitle(
                "CareStock"
        );

        alerta.setHeaderText(
                "Sesión caducada"
        );

        alerta.setContentText(
                "Tu sesión ha caducado por inactividad"
        );

        alerta.show();
    }

}

