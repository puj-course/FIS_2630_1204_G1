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
 * Issue #789: formulario de movimientos.
 * Captura y valida sin modificar la base de datos.
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
                mostrarError(
                        "La sesión cambió. No se muestran datos anteriores."
                );
                return;
            }

            productos.setAll(tarea.getValue());
            filtrarProductos();

            btnValidar.setDisable(false);

            lblEstado.setText(
                    productos.isEmpty()
                            ? "No hay productos activos de aseo o maternidad."
                            : "Productos disponibles: "
                                    + productos.size()
                                    + ". Selecciona un producto."
            );
        });

        tarea.setOnFailed(evento -> {

            btnActualizar.setDisable(false);
            productos.clear();
            filtrarProductos();

            mostrarError(
                    "No se pudieron consultar los productos: "
                            + (tarea.getException() == null
                            ? "Error desconocido"
                            : tarea.getException().getMessage())
            );
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
    private void onValidar() {

        if (!sesionSinCambios()) {
            mostrarError(
                    "La sesión no coincide con la farmacia del formulario."
            );
            return;
        }

        OpcionProducto producto = cmbProducto.getValue();
        String movimiento = cmbMovimiento.getValue();

        String cantidadTexto =
                txtCantidad.getText().trim();

        String motivo =
                txtMotivo.getText().trim();

        if (producto == null) {
            mostrarError(
                    "Selecciona un producto de la farmacia activa."
            );
            return;
        }

        if (movimiento == null) {
            mostrarError(
                    "Selecciona ENTRADA o SALIDA."
            );
            return;
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException ex) {
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

        if (motivo.isBlank()) {
            mostrarError(
                    "Debes indicar el motivo del movimiento."
            );
            return;
        }

        if (motivo.length() > 250) {
            mostrarError(
                    "El motivo no puede superar 250 caracteres."
            );
            return;
        }

        if ("SALIDA".equals(movimiento)
                && cantidad > producto.stock()) {

            mostrarError(
                    "La salida excede el stock mostrado. Actualiza los productos."
            );
            return;
        }

        // #790 incorporará el guardado transaccional.
        // #791 integrará la actualización de inventario y caché.

        lblEstado.setText(
                "Validación correcta: "
                        + movimiento + " de "
                        + cantidad + " unidad(es) de "
                        + producto.nombre() + ". Sin registrar."
        );

        Alert aviso = new Alert(
                Alert.AlertType.INFORMATION
        );

        aviso.setTitle("Movimiento validado");

        aviso.setHeaderText(
                "Datos correctos · Sin cambios en inventario"
        );

        aviso.setContentText(
                "Producto: " + producto.nombre()
                        + "\nMovimiento: " + movimiento
                        + "\nCantidad: " + cantidad
                        + "\nMotivo: " + motivo
                        + "\n\nLos datos son correctos. El movimiento aún no se ha registrado y las existencias no han cambiado."
        );

        aviso.showAndWait();
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
        org.example.carestock.AplicacionCareStock.getInstance().mostrarDashboard();
    }
}
