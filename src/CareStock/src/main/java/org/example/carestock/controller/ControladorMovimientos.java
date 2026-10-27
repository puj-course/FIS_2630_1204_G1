package org.example.carestock.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.carestock.facade.Inventario;
import org.example.carestock.session.SesionUsuario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Formulario de movimientos con persistencia transaccional.
 */
public class ControladorMovimientos {

    @FXML private Label lblFarmacia;
    @FXML private Label lblUsuario;
    @FXML private Label lblStock;
    @FXML private Label lblEstado;

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<OpcionProducto> cmbProducto;
    @FXML private ComboBox<String> cmbMovimiento;

    @FXML private TextField txtCantidad;
    @FXML private TextArea txtMotivo;
    @FXML private Button btnValidar;
    @FXML private Button btnActualizar;

    private final ObservableList<OpcionProducto> productos =
            FXCollections.observableArrayList();

    private int idUsuario;
    private int idFarmacia;
    private boolean registroEnCurso;
    private String confirmacionPendiente;


    private record OpcionProducto(
            String categoria,
            int id,
            String codigo,
            String nombre,
            int stock
    ) {
        @Override
        public String toString() {
            String referencia = codigo == null || codigo.isBlank()
                    ? "" : " · " + codigo;

            return nombre + referencia + " · Stock: " + stock;
        }
    }

    @FXML
    private void initialize() {

        var sesion = SesionUsuario.actual();

        if (sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0) {

            throw new IllegalStateException(
                    "La sesión requiere farmacia asignada."
            );
        }

        idUsuario = sesion.id();
        idFarmacia = sesion.idFarmacia();

        lblUsuario.setText(sesion.nombre());

        lblFarmacia.setText(
                sesion.farmacia() + " (#" + idFarmacia + ")"
        );

        cmbCategoria.setItems(
                FXCollections.observableArrayList(
                        "Aseo", "Maternidad"
                )
        );

        cmbMovimiento.setItems(
                FXCollections.observableArrayList(
                        "ENTRADA", "SALIDA"
                )
        );

        cmbCategoria.valueProperty().addListener(
                (obs, anterior, nueva) -> filtrarProductos()
        );

        cmbProducto.valueProperty().addListener(
                (obs, anterior, nueva) -> mostrarStock(nueva)
        );

        txtCantidad.setTextFormatter(
                new TextFormatter<>(cambio ->
                        cambio.getControlNewText()
                                .matches("[0-9]{0,9}")
                                ? cambio : null
                )
        );

        cmbCategoria.getSelectionModel().selectFirst();

        cargarProductos();
    }

    private boolean sesionSinCambios() {

        if (!SesionUsuario.estaActiva()) {
            return false;
        }

        var sesion = SesionUsuario.actual();

        return sesion.id() == idUsuario
                && Objects.equals(
                        sesion.idFarmacia(), idFarmacia
                );
    }

    @FXML
    private void onActualizar() {
        cargarProductos();
    }

    private void cargarProductos() {
        OpcionProducto seleccionPrevia = cmbProducto.getValue();


        if (!sesionSinCambios()) {
            mostrarError(
                    "La sesión cambió. Vuelve a ingresar."
            );
            return;
        }

        btnActualizar.setDisable(true);
        btnValidar.setDisable(true);

        lblEstado.setText(
                "Consultando productos de la farmacia activa..."
        );

        Task<List<OpcionProducto>> tarea = new Task<>() {

            @Override
            protected List<OpcionProducto> call()
                    throws Exception {

                Inventario inventario = new Inventario(
                        SesionUsuario.usuarioParaFachada()
                );

                List<OpcionProducto> opciones =
                        new ArrayList<>();

                for (var registro : inventario.listarAseo()) {

                    if ("ACTIVO".equalsIgnoreCase(
                            registro.estado())) {

                        var producto = registro.producto();

                        opciones.add(new OpcionProducto(
                                "Aseo",
                                registro.id(),
                                producto.getCodigo(),
                                producto.getNombre(),
                                producto.getStock()
                        ));
                    }
                }

                for (var registro : inventario.listarMaternidad()) {

                    if ("ACTIVO".equalsIgnoreCase(
                            registro.estado())) {

                        var producto = registro.producto();

                        opciones.add(new OpcionProducto(
                                "Maternidad",
                                registro.id(),
                                producto.getCodigo(),
                                producto.getNombre(),
                                producto.getStock()
                        ));
                    }
                }

                opciones.sort(
                        Comparator.comparing(
                                OpcionProducto::nombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

                return opciones;
            }
        };


        tarea.setOnSucceeded(evento -> {
            btnActualizar.setDisable(false);

            if (!sesionSinCambios()) {
                productos.clear();
                filtrarProductos();
                lblEstado.setText(
                        "La sesión cambió. Vuelve a iniciar sesión."
                );
                return;
            }

            productos.setAll(tarea.getValue());
            filtrarProductos();

            // Mantener la selección después de actualizar.
            if (seleccionPrevia != null) {
                cmbProducto.getItems().stream()
                        .filter(p ->
                                p.id() == seleccionPrevia.id()
                                && p.categoria().equals(
                                        seleccionPrevia.categoria()
                                )
                        )
                        .findFirst()
                        .ifPresent(cmbProducto::setValue);
            }

            btnValidar.setDisable(false);

            if (confirmacionPendiente != null) {
                lblEstado.setText(
                        confirmacionPendiente
                        + " Existencias actualizadas."
                );
                confirmacionPendiente = null;
            } else {
                lblEstado.setText(
                        productos.isEmpty()
                                ? "No hay productos activos de aseo o maternidad."
                                : "Productos disponibles: "
                                        + productos.size()
                                        + ". Selecciona un producto."
                );
            }
        });

        tarea.setOnFailed(evento -> {
            btnActualizar.setDisable(false);
            productos.clear();
            filtrarProductos();

            if (confirmacionPendiente != null) {
                String confirmacion = confirmacionPendiente;
                confirmacionPendiente = null;

                lblEstado.setText(
                        confirmacion
                        + " No se pudieron actualizar las existencias en pantalla."
                );

                new Alert(
                        Alert.AlertType.WARNING,
                        "El movimiento fue registrado, pero no fue posible "
                        + "actualizar la lista de productos. "
                        + "Intenta Actualizar productos."
                ).showAndWait();

            } else {
                mostrarError(
                        "No se pudieron consultar los productos: "
                        + (tarea.getException() == null
                                ? "Error desconocido"
                                : tarea.getException().getMessage())
                );
            }
        });

        Thread hilo = new Thread(
                tarea, "cargar-productos-movimientos"
        );

        hilo.setDaemon(true);
        hilo.start();
    }

    private void filtrarProductos() {

        String categoria = cmbCategoria.getValue();

        cmbProducto.setItems(
                FXCollections.observableArrayList(
                        productos.filtered(
                                p -> p.categoria().equals(categoria)
                        )
                )
        );

        cmbProducto.getSelectionModel().clearSelection();

        mostrarStock(null);
    }

    private void mostrarStock(OpcionProducto producto) {

        lblStock.setText(
                producto == null
                        ? "Selecciona un producto para ver sus existencias."
                        : "Existencias disponibles: "
                                + producto.stock()
                                + " unidades"
        );
    }


    @FXML
    private void onRegistrar() {
        if (registroEnCurso) {
            return;
        }

        if (!sesionSinCambios()) {
            mostrarError(
                    "La sesión cambió. Vuelve a iniciar sesión."
            );
            return;
        }

        OpcionProducto producto = cmbProducto.getValue();
        String movimiento = cmbMovimiento.getValue();
        String motivo = txtMotivo.getText().trim();

        if (producto == null) {
            mostrarError(
                    "Selecciona un producto de la farmacia activa."
            );
            return;
        }

        if (movimiento == null) {
            mostrarError("Selecciona ENTRADA o SALIDA.");
            return;
        }

        final int cantidad;

        try {
            cantidad = Integer.parseInt(
                    txtCantidad.getText().trim()
            );
        } catch (NumberFormatException error) {
            mostrarError(
                    "La cantidad debe ser un número entero positivo."
            );
            return;
        }

        if (cantidad <= 0) {
            mostrarError(
                    "La cantidad debe ser mayor que cero."
            );
            return;
        }

        if (motivo.isBlank() || motivo.length() > 250) {
            mostrarError(
                    "Indica un motivo de máximo 250 caracteres."
            );
            return;
        }

        if ("SALIDA".equals(movimiento)
                && cantidad > producto.stock()) {
            mostrarError(
                    "El stock mostrado es insuficiente. "
                    + "Actualiza los productos antes de continuar."
            );
            return;
        }

        // Confirmar antes de modificar existencias.
        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Confirmar movimiento");
        confirmacion.setHeaderText(
                "¿Registrar este movimiento?"
        );

        confirmacion.setContentText(
                "Producto: " + producto.nombre()
                + "\nTipo: " + movimiento
                + "\nCantidad: " + cantidad
                + "\nMotivo: " + motivo
        );

        if (confirmacion.showAndWait().orElse(
                ButtonType.CANCEL
        ) != ButtonType.OK) {
            return;
        }

        final org.example.carestock.model.Usuario usuario =
                SesionUsuario.usuarioParaFachada();

        final org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento tipo =
                org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento.valueOf(
                        movimiento
                );

        registroEnCurso = true;
        bloquearFormulario(true);

        lblEstado.setText(
                "Registrando movimiento, espera un momento..."
        );

        Task<org.example.carestock.service.ServicioMovimientosProducto.ResultadoMovimiento> tarea =
                new Task<>() {

                    @Override
                    protected org.example.carestock.service.ServicioMovimientosProducto.ResultadoMovimiento call()
                            throws Exception {

                        if (!sesionSinCambios()) {
                            throw new IllegalStateException(
                                    "La sesión cambió antes del registro."
                            );
                        }

                        Inventario inventario = new Inventario(
                                usuario
                        );

                        if ("Aseo".equals(producto.categoria())) {
                            return inventario.registrarMovimientoAseo(
                                    producto.id(),
                                    tipo,
                                    cantidad,
                                    motivo
                            );
                        }

                        return inventario.registrarMovimientoMaternidad(
                                producto.id(),
                                tipo,
                                cantidad,
                                motivo
                        );
                    }
                };

        tarea.setOnSucceeded(evento -> {
            registroEnCurso = false;
            bloquearFormulario(false);

            var resultado = tarea.getValue();

            if (!sesionSinCambios()) {
                lblEstado.setText(
                        "Movimiento registrado. La sesión cambió; "
                        + "vuelve a iniciar sesión para consultar existencias."
                );
                return;
            }

            confirmacionPendiente =
                    "Movimiento registrado correctamente "
                    + "(N.º " + resultado.idMovimiento() + "). "
                    + "Stock: " + resultado.stockAnterior()
                    + " → " + resultado.stockPosterior() + ".";

            txtCantidad.clear();
            txtMotivo.clear();

            // Recargar productos después del COMMIT.
            cargarProductos();

            Alert aviso = new Alert(
                    Alert.AlertType.INFORMATION
            );

            aviso.setTitle("Movimiento registrado");
            aviso.setHeaderText(
                    "El movimiento se guardó correctamente"
            );

            aviso.setContentText(
                    "Producto: " + producto.nombre()
                    + "\nMovimiento: " + movimiento
                    + "\nCantidad: " + cantidad
                    + "\nStock anterior: "
                    + resultado.stockAnterior()
                    + "\nStock actual: "
                    + resultado.stockPosterior()
                    + "\nN.º de registro: "
                    + resultado.idMovimiento()
            );

            aviso.showAndWait();
        });

        tarea.setOnFailed(evento -> {
            registroEnCurso = false;
            bloquearFormulario(false);

            Throwable causa = tarea.getException();

            if (causa instanceof org.example.carestock.exception.ReglaNegocioException
                    || causa instanceof IllegalStateException) {

                mostrarError(causa.getMessage());

            } else {
                mostrarError(
                        "No se pudo confirmar el resultado del movimiento. "
                        + "Comprueba el historial antes de volver a intentarlo."
                );
            }
        });

        Thread hilo = new Thread(
                tarea,
                "registrar-movimiento-carestock"
        );

        hilo.setDaemon(true);
        hilo.start();
    }

    private void bloquearFormulario(boolean bloqueado) {
        cmbCategoria.setDisable(bloqueado);
        cmbProducto.setDisable(bloqueado);
        cmbMovimiento.setDisable(bloqueado);
        txtCantidad.setDisable(bloqueado);
        txtMotivo.setDisable(bloqueado);
        btnActualizar.setDisable(bloqueado);
        btnValidar.setDisable(bloqueado);
    }

    private void mostrarError(String mensaje) {

        lblEstado.setText(mensaje);

        Alert alerta = new Alert(
                Alert.AlertType.WARNING,
                mensaje
        );

        alerta.setHeaderText(
                "Revisa los datos del formulario"
        );

        alerta.showAndWait();
    }

    @FXML
    private void onVolver() {
        if (registroEnCurso) {
            mostrarError(
                    "Espera a que termine el registro antes de salir."
            );
            return;
        }

        org.example.carestock.CareStockApp.getInstance().mostrarDashboard();
    }
}
