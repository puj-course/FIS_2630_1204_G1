package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.example.carestock.CareStockApp;
import org.example.carestock.DataTransferObject.FiltroProductoInventario;
import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.facade.Inventario;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;

import java.util.List;

/**
 * Consulta unificada del inventario.
 *
 * La fachada utiliza el Proxy para reutilizar consultas vigentes.
 * La carga se ejecuta fuera del hilo grafico de JavaFX.
 */
public class ConsultaInventarioController {

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtBuscar;
    @FXML private Button btnActualizar;
    @FXML private Button btnRegistrar;
    @FXML private Button btnEditar;

    @FXML private TableView<ProductoInventario> tablaInventario;

    @FXML private TableColumn<ProductoInventario, String> colCodigo;
    @FXML private TableColumn<ProductoInventario, String> colNombre;
    @FXML private TableColumn<ProductoInventario, String> colCategoria;
    @FXML private TableColumn<ProductoInventario, String> colPresentacion;
    @FXML private TableColumn<ProductoInventario, String> colExistencia;
    @FXML private TableColumn<ProductoInventario, String> colEstado;

    @FXML private Label lblFarmacia;
    @FXML private Label lblEstado;
    @FXML private Label lblTotal;

    private final ObservableList<ProductoInventario> productos =
            FXCollections.observableArrayList();

    private FilteredList<ProductoInventario> filtrados;
    private Inventario inventario;

    private SesionUsuario.DatosUsuario sesionVista;

    private boolean consultaExitosa;
    private boolean cargando;
    private boolean vistaActiva = true;

    @FXML
    public void initialize() {
        sesionVista = SesionUsuario.actual();

        if (sesionVista.idFarmacia() == null
                || sesionVista.idFarmacia() <= 0) {

            throw new IllegalStateException(
                    "El usuario no tiene farmacia asignada"
            );
        }

        lblFarmacia.setText(
                "Farmacia: "
                        + (sesionVista.farmacia() == null
                        ? "#" + sesionVista.idFarmacia()
                        : sesionVista.farmacia()
                        + " (#" + sesionVista.idFarmacia() + ")")
        );

        inventario = new Inventario(
                SesionUsuario.usuarioParaFachada()
        );

        configurarColumnas();
        configurarTabla();
        configurarFiltros();
        configurarAcciones();

        cargarDatos(false);
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(
                c -> celda(c.getValue().codigo())
        );

        colNombre.setCellValueFactory(
                c -> celda(c.getValue().nombre())
        );

        colCategoria.setCellValueFactory(
                c -> celda(c.getValue().categoria())
        );

        colPresentacion.setCellValueFactory(
                c -> celda(c.getValue().presentacion())
        );

        colExistencia.setCellValueFactory(
                c -> celda(c.getValue().existencia())
        );

        colEstado.setCellValueFactory(c -> {
            ProductoInventario producto = c.getValue();

            boolean esMedicamento =
                    "Medicamento".equalsIgnoreCase(
                            producto.categoria());

            boolean sinStock = producto.existencia() == 0;

            boolean estaActivo =
                    "ACTIVO".equalsIgnoreCase(producto.estado());

            String estadoVisual =
                    esMedicamento && sinStock && estaActivo
                            ? "DESHABILITADO"
                            : producto.estado();

            return celda(estadoVisual);
        });
    }

    private void configurarTabla() {
        filtrados = new FilteredList<>(productos, p -> true);

        SortedList<ProductoInventario> ordenados =
                new SortedList<>(filtrados);

        ordenados.comparatorProperty().bind(
                tablaInventario.comparatorProperty()
        );

        tablaInventario.setItems(ordenados);

        tablaInventario.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(
                    ProductoInventario producto,
                    boolean vacio
            ) {
                super.updateItem(producto, vacio);

                if (vacio || producto == null) {
                    setStyle("");
                } else if (producto.existencia() == 0) {
                    setStyle("-fx-background-color: #FFF3E0;");
                } else {
                    setStyle("");
                }
            }
        });

        tablaInventario.setPlaceholder(
                new Label("Cargando productos...")
        );
    }

    private void configurarFiltros() {
        cmbCategoria.setItems(
                FXCollections.observableArrayList(
                        FiltroProductoInventario.TODAS,
                        "Medicamento",
                        "Aseo",
                        "Maternidad"
                )
        );

        cmbCategoria.getSelectionModel().select(
                FiltroProductoInventario.TODAS
        );

        cmbCategoria.valueProperty().addListener(
                (observable, anterior, actual) -> aplicarFiltros()
        );

        txtBuscar.textProperty().addListener(
                (observable, anterior, actual) -> aplicarFiltros()
        );
    }

    private void configurarAcciones() {
        boolean autorizado = sesionVista.esAdministrador();

        btnRegistrar.setVisible(autorizado);
        btnRegistrar.setManaged(autorizado);

        btnEditar.setVisible(autorizado);
        btnEditar.setManaged(autorizado);

        tablaInventario.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) ->
                        actualizarAcciones());

        actualizarAcciones();
    }

    private void actualizarAcciones() {
        boolean autorizado =
                sesionVista.esAdministrador()
                && mismaSesionYVista();

        btnRegistrar.setDisable(!autorizado || cargando);

        btnEditar.setDisable(
                !autorizado || cargando
                || tablaInventario.getSelectionModel()
                        .getSelectedItem() == null
        );
    }

    private ReadOnlyStringWrapper celda(Object valor) {
        return new ReadOnlyStringWrapper(
                valor == null ? "" : String.valueOf(valor)
        );
    }

    private void aplicarFiltros() {
        if (!consultaExitosa) {
            return;
        }

        filtrados.setPredicate(
                producto -> FiltroProductoInventario.coincide(
                        producto,
                        cmbCategoria.getValue(),
                        txtBuscar.getText()
                )
        );

        actualizarEstadoExitoso();
    }

    private void actualizarEstadoExitoso() {
        if (productos.isEmpty()) {
            lblEstado.setText(
                    "No hay productos registrados en esta farmacia."
            );

            tablaInventario.setPlaceholder(new Label(
                    "No hay productos registrados en esta farmacia."
            ));

        } else if (filtrados.isEmpty()) {
            lblEstado.setText(
                    "No hay coincidencias para los filtros seleccionados."
            );

            tablaInventario.setPlaceholder(new Label(
                    "Sin coincidencias. Cambia la categoria o la busqueda."
            ));

        } else {
            lblEstado.setText(
                    "Mostrando " + filtrados.size()
                            + " de " + productos.size()
                            + " productos de la farmacia."
            );

            tablaInventario.setPlaceholder(
                    new Label("Sin coincidencias.")
            );
        }

        lblTotal.setText(
                "Productos encontrados: " + filtrados.size()
        );
    }

    /**
     * false: permite utilizar la cache vigente.
     * true: invalida la cache y solicita informacion nueva.
     */
    private void cargarDatos(boolean forzarActualizacion) {
        if (cargando || !mismaSesionYVista()) {
            return;
        }

        cargando = true;
        consultaExitosa = false;

        btnActualizar.setDisable(true);
        productos.clear();
        actualizarAcciones();

        lblTotal.setText("Productos encontrados: --");

        lblEstado.setText(
                forzarActualizacion
                        ? "Actualizando inventario desde la base de datos..."
                        : "Consultando inventario..."
        );

        tablaInventario.setPlaceholder(
                new Label("Cargando productos...")
        );

        Task<List<ProductoInventario>> tarea = new Task<>() {
            @Override
            protected List<ProductoInventario> call() throws Exception {
                // Verifica que esta tarea pertenezca a la sesion original.
                if (SesionUsuario.actual() != sesionVista) {
                    throw new IllegalStateException(
                            "La sesion ha cambiado"
                    );
                }

                if (forzarActualizacion) {
                    return inventario.actualizarInventarioUnificado();
                }

                return inventario.listarInventarioUnificado();
            }
        };

        tarea.setOnSucceeded(evento -> {
            cargando = false;

            if (!mismaSesionYVista()) {
                return;
            }

            productos.setAll(tarea.getValue());
            consultaExitosa = true;

            aplicarFiltros();
            btnActualizar.setDisable(false);
            actualizarAcciones();

            mostrarEstadisticasProxy();
        });

        tarea.setOnFailed(evento -> {
            cargando = false;

            if (!mismaSesionYVista()) {
                return;
            }

            mostrarErrorConsulta();
            btnActualizar.setDisable(false);
            actualizarAcciones();

            Throwable error = tarea.getException();

            System.err.println(
                    "Fallo de consulta de inventario: "
                            + (error == null
                            ? "Error desconocido"
                            : error.getClass().getSimpleName())
            );
        });

        Thread hilo = new Thread(tarea, "consulta-inventario");
        hilo.setDaemon(true);
        hilo.start();
    }

    private boolean mismaSesionYVista() {
        if (!vistaActiva) {
            return false;
        }

        try {
            // Compara la instancia de sesion, no solamente el usuario.
            return SesionUsuario.actual() == sesionVista;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private void mostrarErrorConsulta() {
        consultaExitosa = false;

        productos.clear();
        filtrados.setPredicate(p -> false);

        lblTotal.setText("Productos encontrados: --");

        lblEstado.setText(
                "No se pudo cargar el inventario. Reintenta con Actualizar."
        );

        tablaInventario.setPlaceholder(new Label(
                "Error de consulta. No se han cargado existencias."
        ));
    }

    private void mostrarEstadisticasProxy() {
        var proxy = InventarioConsultas.compartida();

        System.out.printf(
                "Proxy inventario: aciertos=%d, consultas reales=%d%n",
                proxy.aciertos(),
                proxy.consultasReales()
        );
    }

    @FXML
    private void onActualizar() {
        cargarDatos(true);
    }

    @FXML
    private void onRegistrar() {
        if (!mismaSesionYVista()
                || !sesionVista.esAdministrador()
                || cargando) {
            return;
        }

        ProductoInventario seleccionado =
                tablaInventario.getSelectionModel()
                        .getSelectedItem();

        String categoria = seleccionado == null
                ? cmbCategoria.getValue()
                : seleccionado.categoria();

        if (categoria == null
                || FiltroProductoInventario.TODAS
                        .equalsIgnoreCase(categoria)) {
            categoria = "Medicamento";
        }

        vistaActiva = false;

        CareStockApp.getInstance()
                .mostrarCatalogoParaRegistro(categoria);
    }

    @FXML
    private void onEditar() {
        if (!mismaSesionYVista()
                || !sesionVista.esAdministrador()
                || cargando) {
            return;
        }

        ProductoInventario seleccionado =
                tablaInventario.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        vistaActiva = false;

        CareStockApp.getInstance()
                .mostrarCatalogoParaEditar(
                        seleccionado.categoria(),
                        seleccionado.id()
                );
    }

    @FXML
    private void onVolver() {
        vistaActiva = false;
        CareStockApp.getInstance().mostrarCatalogo();
    }

    @FXML
    private void onCerrarSesion() {
        vistaActiva = false;
        CareStockApp.getInstance().cerrarSesion();
    }
}