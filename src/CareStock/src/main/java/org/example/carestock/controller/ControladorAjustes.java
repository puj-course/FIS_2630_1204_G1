package org.example.carestock.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.carestock.CareStockApp;
import org.example.carestock.facade.Inventario;
import org.example.carestock.model.AjusteExistencias.Categoria;
import org.example.carestock.service.ServicioAjustesInventario;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HU-794: Integracion del formulario JavaFX para ajustes
 * justificados de existencias.
 */
public final class ControladorAjustes {

    @FXML private Label lblUsuario;
    @FXML private Label lblFarmacia;
    @FXML private Label lblExistencias;
    @FXML private Label lblEstado;

    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Opcion> cmbReferencia;

    @FXML private TextField txtNuevaCantidad;
    @FXML private TextArea txtMotivo;

    @FXML private Button btnActualizar;
    @FXML private Button btnAplicar;

    private int idUsuario;
    private int idFarmacia;
    private boolean operando;
    private String confirmacionPendiente;

    private final List<Opcion> opciones = new ArrayList<>();

    private record Opcion(
            Categoria categoria,
            int id,
            String descripcion,
            int stock
    ) {
        @Override
        public String toString() {
            return descripcion + " - " + stock + " unidades";
        }
    }

    @FXML
    private void initialize() {

        var sesion = SesionUsuario.actual();

        if (sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0
                || !administrador(sesion.rol())) {
            throw new IllegalStateException(
                    "Se requiere un Administrador con farmacia asignada.");
        }

        idUsuario = sesion.id();
        idFarmacia = sesion.idFarmacia();

        lblUsuario.setText(sesion.nombre());
        lblFarmacia.setText(
                sesion.farmacia() + " (#" + idFarmacia + ")");

        cmbCategoria.setItems(
                FXCollections.observableArrayList(Categoria.values()));

        cmbCategoria.valueProperty().addListener(
                (obs, antes, despues) -> filtrarReferencias(null));

        cmbReferencia.valueProperty().addListener(
                (obs, antes, despues) -> {
                    lblExistencias.setText(
                            despues == null
                            ? "Selecciona un producto o lote."
                            : "Existencias actuales: "
                                    + despues.stock() + " unidades");
                    txtNuevaCantidad.clear();
                });

        txtNuevaCantidad.setTextFormatter(
                new TextFormatter<>(cambio ->
                        cambio.getControlNewText().matches("[0-9]{0,9}")
                                ? cambio : null));

        cmbCategoria.getSelectionModel().selectFirst();

        cargarReferencias();
    }

    private static boolean administrador(String rol) {
        return rol != null
                && ("ADMINISTRADOR".equalsIgnoreCase(rol.trim())
                || "SUPER_ADMIN".equalsIgnoreCase(rol.trim()));
    }

    private boolean sesionValida() {

        if (!SesionUsuario.estaActiva()) {
            return false;
        }

        var actual = SesionUsuario.actual();

        return actual.id() == idUsuario
                && Objects.equals(actual.idFarmacia(), idFarmacia)
                && administrador(actual.rol());
    }

    private void bloquear(boolean valor) {

        operando = valor;

        cmbCategoria.setDisable(valor);
        cmbReferencia.setDisable(valor);
        txtNuevaCantidad.setDisable(valor);
        txtMotivo.setDisable(valor);
        btnActualizar.setDisable(valor);
        btnAplicar.setDisable(valor);
    }

    @FXML
    private void onActualizar() {
        if (!operando) {
            cargarReferencias();
        }
    }

    private void cargarReferencias() {

        if (!sesionValida()) {
            error("La sesion cambio. Vuelve a iniciar sesion.");
            return;
        }

        Opcion seleccionAnterior = cmbReferencia.getValue();

        bloquear(true);
        lblEstado.setText("Consultando existencias...");

        Task<List<Opcion>> tarea = new Task<>() {

            @Override
            protected List<Opcion> call() throws Exception {

                Inventario inventario = new Inventario(
                        SesionUsuario.usuarioParaFachada());

                List<Opcion> filas = new ArrayList<>();

                // Productos de aseo
                for (var registro : inventario.listarAseo()) {

                    if ("ACTIVO".equalsIgnoreCase(registro.estado())) {

                        var producto = registro.producto();

                        filas.add(new Opcion(
                                Categoria.ASEO,
                                registro.id(),
                                producto.getNombre()
                                        + " - " + producto.getCodigo(),
                                producto.getStock()));
                    }
                }

                // Productos de maternidad
                for (var registro : inventario.listarMaternidad()) {

                    if ("ACTIVO".equalsIgnoreCase(registro.estado())) {

                        var producto = registro.producto();

                        filas.add(new Opcion(
                                Categoria.MATERNIDAD,
                                registro.id(),
                                producto.getNombre()
                                        + " - " + producto.getCodigo(),
                                producto.getStock()));
                    }
                }

                // Medicamentos activos
                Map<Integer, String> medicamentos = new HashMap<>();

                for (var registro : inventario.listarMedicamentos()) {

                    if ("ACTIVO".equalsIgnoreCase(registro.estado())) {
                        medicamentos.put(
                                registro.id(),
                                registro.producto().getNombreComercial());
                    }
                }

                // Medicamentos: ajuste por lote
                for (var lote : inventario.listarLotes()) {

                    String nombre = medicamentos.get(
                            lote.getIdMedicamento());

                    if (nombre != null
                            && lote.getFechaVencimiento() != null
                            && lote.getFechaVencimiento()
                                    .isAfter(LocalDate.now())
                            && ("DISPONIBLE".equalsIgnoreCase(
                                    lote.getEstadoLote())
                            || "AGOTADO".equalsIgnoreCase(
                                    lote.getEstadoLote()))) {

                        filas.add(new Opcion(
                                Categoria.MEDICAMENTO,
                                lote.getIdLote(),
                                nombre + " - Lote "
                                        + lote.getNumeroLote(),
                                lote.getCantidadActual()));
                    }
                }

                filas.sort(Comparator.comparing(
                        Opcion::descripcion,
                        String.CASE_INSENSITIVE_ORDER));

                return filas;
            }
        };

        tarea.setOnSucceeded(evento -> {

            bloquear(false);

            if (!sesionValida()) {
                opciones.clear();
                filtrarReferencias(null);
                error("La sesion cambio. Vuelve a iniciar sesion.");
                return;
            }

            opciones.clear();
            opciones.addAll(tarea.getValue());

            filtrarReferencias(seleccionAnterior);

            if (confirmacionPendiente != null) {
                lblEstado.setText(confirmacionPendiente
                        + " Existencias actualizadas.");
                confirmacionPendiente = null;
            } else {
                lblEstado.setText(
                        "Inventario actualizado. Selecciona una referencia.");
            }
        });

        tarea.setOnFailed(evento -> {

            bloquear(false);
            opciones.clear();
            filtrarReferencias(null);

            if (confirmacionPendiente != null) {
                lblEstado.setText(confirmacionPendiente
                        + " No se pudo refrescar el inventario.");
                confirmacionPendiente = null;
            } else {
                error("No fue posible consultar existencias: "
                        + mensaje(tarea.getException()));
            }
        });

        ejecutar(tarea, "consultar-ajustes");
    }

    private void filtrarReferencias(Opcion anterior) {

        Categoria categoria = cmbCategoria.getValue();

        cmbReferencia.setItems(
                FXCollections.observableArrayList(
                        opciones.stream()
                                .filter(p -> p.categoria() == categoria)
                                .toList()));

        cmbReferencia.getSelectionModel().clearSelection();

        if (anterior != null && anterior.categoria() == categoria) {
            cmbReferencia.getItems().stream()
                    .filter(p -> p.id() == anterior.id())
                    .findFirst()
                    .ifPresent(cmbReferencia::setValue);
        }

        if (cmbReferencia.getValue() == null) {
            lblExistencias.setText(
                    "Selecciona un producto o lote.");
            txtNuevaCantidad.clear();
        }
    }

    @FXML
    private void onAplicar() {

        if (operando) {
            return;
        }

        if (!sesionValida()) {
            error("La sesion cambio. Vuelve a iniciar sesion.");
            return;
        }

        Opcion opcion = cmbReferencia.getValue();

        if (opcion == null) {
            error("Selecciona un producto o lote.");
            return;
        }

        int nuevaCantidad;

        try {
            nuevaCantidad = Integer.parseInt(
                    txtNuevaCantidad.getText().trim());
        } catch (NumberFormatException ex) {
            error("La nueva cantidad debe ser un entero "
                    + "mayor o igual a cero.");
            return;
        }

        String motivo = txtMotivo.getText().trim();

        if (nuevaCantidad < 0
                || nuevaCantidad == opcion.stock()) {

            error("La nueva cantidad debe ser diferente "
                    + "de las existencias actuales.");
            return;
        }

        if (motivo.isBlank() || motivo.length() > 250) {

            error("El motivo es obligatorio y no puede "
                    + "superar 250 caracteres.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar ajuste");
        confirmacion.setHeaderText(
                "¿Aplicar ajuste justificado?");

        confirmacion.setContentText(
                "Referencia: " + opcion.descripcion()
                + "\nAnterior: " + opcion.stock()
                + "\nNueva cantidad: " + nuevaCantidad
                + "\nDiferencia: "
                + ((long) nuevaCantidad - opcion.stock())
                + "\nMotivo: " + motivo);

        if (confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        bloquear(true);

        lblEstado.setText(
                "Aplicando ajuste y registrando auditoria...");

        Task<ServicioAjustesInventario.ResultadoAjuste> tarea =
                new Task<>() {

                    @Override
                    protected ServicioAjustesInventario.ResultadoAjuste
                    call() throws Exception {

                        if (!sesionValida()) {
                            throw new IllegalStateException(
                                    "La sesion cambio.");
                        }

                        return new ServicioAjustesInventario().ajustar(
                                opcion.categoria(),
                                opcion.id(),
                                opcion.stock(),
                                nuevaCantidad,
                                motivo);
                    }
                };

        tarea.setOnSucceeded(evento -> {

            bloquear(false);

            var resultado = tarea.getValue();

            txtNuevaCantidad.clear();
            txtMotivo.clear();

            confirmacionPendiente =
                    "Ajuste #" + resultado.idAuditoria()
                    + " confirmado. Existencias: "
                    + resultado.anterior()
                    + " -> " + resultado.nuevo() + ".";

            lblEstado.setText(
                    confirmacionPendiente
                    + " Actualizando inventario...");

            // El servicio invalida el cache despues del COMMIT.
            if (sesionValida()) {
                cargarReferencias();
            }

            Alert alerta = new Alert(
                    Alert.AlertType.INFORMATION,
                    "Ajuste confirmado y auditado (#"
                            + resultado.idAuditoria() + ").");

            alerta.showAndWait();
        });

        tarea.setOnFailed(evento -> {

            bloquear(false);

            Throwable fallo = tarea.getException();

            if (fallo instanceof IllegalArgumentException
                    || fallo instanceof ReglaNegocioException
                    || fallo instanceof IllegalStateException) {

                error(mensaje(fallo));

            } else {

                error("No se pudo confirmar el ajuste. "
                        + "Comprueba el estado del inventario "
                        + "antes de reintentar. Detalle: "
                        + mensaje(fallo));
            }
        });

        ejecutar(tarea, "registrar-ajuste");
    }

    @FXML
    private void onVolver() {

        if (operando) {
            error("Finaliza la operacion antes de salir.");
            return;
        }

        CareStockApp.getInstance().mostrarDashboard();
    }

    private static void ejecutar(
            Task<?> tarea,
            String nombre
    ) {

        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        hilo.start();
    }

    private static String mensaje(Throwable error) {

        return error == null || error.getMessage() == null
                ? "Error no identificado"
                : error.getMessage();
    }

    private void error(String texto) {

        lblEstado.setText(texto);

        Alert alerta = new Alert(
                Alert.AlertType.WARNING,
                texto);

        alerta.setHeaderText(
                "No fue posible completar la operacion");

        alerta.showAndWait();
    }
}
