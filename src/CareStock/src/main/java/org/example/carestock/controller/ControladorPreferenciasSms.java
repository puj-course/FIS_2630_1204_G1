package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.carestock.CareStockApp;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.sms.ServicioDirectorioSms;
import org.example.carestock.sms.ServicioPreferenciasSms;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class ControladorPreferenciasSms {

    @FXML private Label lblFarmacia;
    @FXML private Label lblSeleccion;
    @FXML private Label lblEstado;

    @FXML private TableView<ServicioDirectorioSms.Destinatario>
            tablaDestinatarios;

    @FXML private TableColumn<ServicioDirectorioSms.Destinatario, String>
            colUsuario;
    @FXML private TableColumn<ServicioDirectorioSms.Destinatario, String>
            colTelefono;
    @FXML private TableColumn<ServicioDirectorioSms.Destinatario, String>
            colEstado;

    @FXML private TextField txtTelefono;
    @FXML private TextField txtInicio;
    @FXML private TextField txtFin;

    @FXML private CheckBox chkAutorizado;
    @FXML private CheckBox chkConfirmacion;
    @FXML private CheckBox chkStock;
    @FXML private CheckBox chkVencimientos;

    @FXML private ComboBox<String> cmbZona;

    @FXML private Button btnGuardar;
    @FXML private Button btnDeshabilitar;

    private final ServicioPreferenciasSms preferencias =
            new ServicioPreferenciasSms();

    private final ServicioDirectorioSms directorio =
            new ServicioDirectorioSms();

    private boolean ocupado;
    private int idSesion;

    @FXML
    private void initialize() {
        var sesion = SesionUsuario.actual();

        if (!ValidacionFormularioSms.puedeAcceder(sesion)) {
            throw new SecurityException(
                    "Acceso restringido a administradores.");
        }

        idSesion = sesion.id();

        lblFarmacia.setText(
                sesion.nombre() + " | "
                + sesion.farmacia()
                + " (#" + sesion.idFarmacia() + ")");

        cmbZona.setItems(FXCollections.observableArrayList(
                "America/Bogota", "America/Lima",
                "America/Mexico_City", "America/New_York",
                "UTC"
        ));
        cmbZona.setEditable(true);
        cmbZona.setValue("America/Bogota");

        colUsuario.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(
                        c.getValue().nombre()));

        colTelefono.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(
                        c.getValue().telefono() == null
                                ? "Sin configurar"
                                : c.getValue().telefono()));

        colEstado.setCellValueFactory(c -> {
            var d = c.getValue();
            return new ReadOnlyStringWrapper(
                    !d.configurado() ? "Sin configurar"
                    : d.autorizado() ? "Autorizado"
                    : "No autorizado");
        });

        tablaDestinatarios.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) cargarDetalle(actual);
                });

        limpiarFormulario();
        onActualizar();
    }

    private boolean sesionVigente() {
        return SesionUsuario.estaActiva()
                && SesionUsuario.actual().id() == idSesion
                && ValidacionFormularioSms.puedeAcceder(
                        SesionUsuario.actual());
    }

    private ServicioDirectorioSms.Destinatario seleccionado() {
        return tablaDestinatarios.getSelectionModel()
                .getSelectedItem();
    }

    private void limpiarFormulario() {
        txtTelefono.clear();
        txtInicio.clear();
        txtFin.clear();
        chkAutorizado.setSelected(false);
        chkConfirmacion.setSelected(false);
        chkStock.setSelected(false);
        chkVencimientos.setSelected(false);
        cmbZona.setValue("America/Bogota");
    }

    @FXML
    private void onActualizar() {
        int anterior = seleccionado() == null
                ? -1 : seleccionado().idUsuario();

        ejecutar("Consultando destinatarios...",
                directorio::listar,
                registros -> {
                    tablaDestinatarios.setItems(
                            FXCollections.observableArrayList(registros));

                    ServicioDirectorioSms.Destinatario objetivo =
                            registros.stream()
                                    .filter(u -> u.idUsuario() == anterior)
                                    .findFirst()
                                    .orElse(registros.isEmpty()
                                            ? null : registros.getFirst());

                    if (objetivo == null) {
                        lblSeleccion.setText("Sin usuarios disponibles");
                        limpiarFormulario();
                    } else {
                        tablaDestinatarios.getSelectionModel()
                                .select(objetivo);
                        cargarDetalle(objetivo);
                    }

                    lblEstado.setText(
                            registros.size()
                            + " usuario(s) de la farmacia.");
                });
    }

    private void cargarDetalle(
            ServicioDirectorioSms.Destinatario usuario) {

        if (ocupado) return;

        lblSeleccion.setText(
                usuario.nombre() + " | " + usuario.email());

        limpiarFormulario();

        if (!usuario.configurado()) {
            lblEstado.setText("Destinatario sin configurar.");
            return;
        }

        ejecutar("Consultando preferencias...",
                () -> preferencias.consultar(usuario.idUsuario()),
                configuracion -> {
                    var actual = seleccionado();

                    if (actual == null
                            || actual.idUsuario() != usuario.idUsuario()) {
                        if (actual != null) cargarDetalle(actual);
                        return;
                    }

                    if (configuracion == null) {
                        limpiarFormulario();
                        return;
                    }

                    var s = configuracion.solicitud();

                    txtTelefono.setText(s.telefono());
                    chkAutorizado.setSelected(s.autorizado());
                    chkConfirmacion.setSelected(false);
                    chkStock.setSelected(s.notificaStockCritico());
                    chkVencimientos.setSelected(
                            s.notificaVencimientoCritico());

                    txtInicio.setText(hora(s.horarioInicio()));
                    txtFin.setText(hora(s.horarioFin()));
                    cmbZona.setValue(s.zonaHoraria());

                    lblEstado.setText("Configuracion cargada.");
                });
    }

    private static String hora(LocalTime valor) {
        if (valor == null) return "";
        return String.format("%02d:%02d",
                valor.getHour(), valor.getMinute());
    }

    @FXML
    private void onGuardar() {
        var usuario = seleccionado();

        if (usuario == null || ocupado) {
            lblEstado.setText("Selecciona un destinatario.");
            return;
        }

        ServicioPreferenciasSms.Solicitud solicitud;

        try {
            solicitud = ValidacionFormularioSms.crear(
                    txtTelefono.getText(),
                    chkAutorizado.isSelected(),
                    chkStock.isSelected(),
                    chkVencimientos.isSelected(),
                    txtInicio.getText(),
                    txtFin.getText(),
                    cmbZona.getValue(),
                    chkConfirmacion.isSelected()
            );
        } catch (IllegalArgumentException ex) {
            new Alert(Alert.AlertType.WARNING,
                    ex.getMessage()).showAndWait();
            return;
        }

        ejecutar("Guardando configuracion...",
                () -> {
                    preferencias.guardar(
                            usuario.idUsuario(), solicitud);
                    return true;
                },
                correcto -> {
                    chkConfirmacion.setSelected(false);
                    onActualizar();
                });
    }

    @FXML
    private void onDeshabilitar() {
        var usuario = seleccionado();

        if (usuario == null || !usuario.configurado() || ocupado) {
            lblEstado.setText(
                    "Selecciona una configuracion activa.");
            return;
        }

        Alert confirmar = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Se revocara la autorizacion SMS y se "
                + "deshabilitara el destinatario. ¿Continuar?",
                ButtonType.YES, ButtonType.NO);

        confirmar.setHeaderText(
                "Deshabilitar configuracion SMS");

        if (confirmar.showAndWait().orElse(ButtonType.NO)
                != ButtonType.YES) return;

        ejecutar("Deshabilitando destinatario...",
                () -> {
                    directorio.deshabilitar(usuario.idUsuario());
                    return true;
                },
                correcto -> onActualizar());
    }

    @FXML
    private void onVolver() {
        CareStockApp.getInstance().mostrarDashboard();
    }

    private <T> void ejecutar(
            String mensaje,
            Callable<T> operacion,
            Consumer<T> exito) {

        if (ocupado) return;

        ocupado = true;
        btnGuardar.setDisable(true);
        btnDeshabilitar.setDisable(true);
        lblEstado.setText(mensaje);

        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operacion.call();
            }
        };

        tarea.setOnSucceeded(e -> {
            ocupado = false;
            btnGuardar.setDisable(false);
            btnDeshabilitar.setDisable(false);

            if (sesionVigente()) {
                exito.accept(tarea.getValue());
            }
        });

        tarea.setOnFailed(e -> {
            ocupado = false;
            btnGuardar.setDisable(false);
            btnDeshabilitar.setDisable(false);

            if (!sesionVigente()) return;

            Throwable error = tarea.getException();

            String mensajeError = error instanceof SecurityException
                    ? "Operacion no autorizada."
                    : "No se pudo completar la operacion. "
                    + "Verifica los datos o la conexion.";

            lblEstado.setText(mensajeError);

            new Alert(Alert.AlertType.ERROR,
                    mensajeError).showAndWait();
        });

        Thread hilo = new Thread(tarea, "carestock-sms-ui");
        hilo.setDaemon(true);
        hilo.start();
    }
}
