package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import org.example.carestock.session.SesionUsuario;

import java.util.List;

/** Consulta unificada (solo lectura) y filtros de inventario. */
public class ConsultaInventarioController {

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtBuscar;
    @FXML private Button btnActualizar;
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

    private final ObservableList<ProductoInventario> productos = FXCollections.observableArrayList();
    private FilteredList<ProductoInventario> filtrados;
    private Inventario inventario;
    private boolean consultaExitosa = false;

    @FXML
    public void initialize() {
        var sesion = SesionUsuario.actual();
        if (sesion.idFarmacia() == null) {
            throw new IllegalStateException("El usuario no tiene farmacia asignada");
        }
        lblFarmacia.setText("Farmacia: " + (sesion.farmacia() == null
                ? "#" + sesion.idFarmacia() : sesion.farmacia() + " (#" + sesion.idFarmacia() + ")"));
        inventario = new Inventario(SesionUsuario.usuarioParaFachada());

        colCodigo.setCellValueFactory(c -> celda(c.getValue().codigo()));
        colNombre.setCellValueFactory(c -> celda(c.getValue().nombre()));
        colCategoria.setCellValueFactory(c -> celda(c.getValue().categoria()));
        colPresentacion.setCellValueFactory(c -> celda(c.getValue().presentacion()));
        colExistencia.setCellValueFactory(c -> celda(c.getValue().existencia()));
        colEstado.setCellValueFactory(c -> celda(c.getValue().estado()));

        filtrados = new FilteredList<>(productos, p -> true);
        SortedList<ProductoInventario> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaInventario.comparatorProperty());
        tablaInventario.setItems(ordenados);
        tablaInventario.setRowFactory(t -> new TableRow<>() {
            @Override protected void updateItem(ProductoInventario producto, boolean vacio) {
                super.updateItem(producto, vacio);
                setStyle(vacio || producto == null ? ""
                        : producto.existencia() == 0 ? "-fx-text-fill: #8B4513;" : "");
            }
        });

        cmbCategoria.setItems(FXCollections.observableArrayList(
                FiltroProductoInventario.TODAS, "Medicamento", "Aseo", "Maternidad"));
        cmbCategoria.getSelectionModel().select(FiltroProductoInventario.TODAS);
        cmbCategoria.valueProperty().addListener((o, antes, ahora) -> aplicarFiltros());
        txtBuscar.textProperty().addListener((o, antes, ahora) -> aplicarFiltros());
        tablaInventario.setPlaceholder(new Label("Cargando productos..."));
        cargarDatos();
    }

    private ReadOnlyStringWrapper celda(Object valor) {
        return new ReadOnlyStringWrapper(String.valueOf(valor));
    }

    private void aplicarFiltros() {
        if (!consultaExitosa) return; // Un error JDBC no equivale a inventario vacio.
        filtrados.setPredicate(p -> FiltroProductoInventario.coincide(
                p, cmbCategoria.getValue(), txtBuscar.getText()));
        actualizarEstadoExitoso();
    }

    private void actualizarEstadoExitoso() {
        if (productos.isEmpty()) {
            lblEstado.setText("No hay productos registrados en esta farmacia.");
            tablaInventario.setPlaceholder(new Label(
                    "No hay productos registrados en esta farmacia."));
        } else if (filtrados.isEmpty()) {
            lblEstado.setText("No hay coincidencias para los filtros seleccionados.");
            tablaInventario.setPlaceholder(new Label(
                    "Sin coincidencias. Cambia la categoria o la busqueda."));
        } else {
            lblEstado.setText("Mostrando " + filtrados.size() + " de "
                    + productos.size() + " productos de la farmacia.");
            tablaInventario.setPlaceholder(new Label("Sin coincidencias."));
        }
        lblTotal.setText("Productos encontrados: " + filtrados.size());
    }

    private void cargarDatos() {
        btnActualizar.setDisable(true);
        lblEstado.setText("Consultando inventario...");
        try {
            List<ProductoInventario> datos = inventario.listarInventarioUnificado();
            productos.setAll(datos);
            consultaExitosa = true;
            aplicarFiltros();
        } catch (Exception e) {
            consultaExitosa = false;
            productos.clear();
            filtrados.setPredicate(p -> false);
            lblTotal.setText("Productos encontrados: --");
            lblEstado.setText("No se pudo cargar el inventario. Reintenta con Actualizar.");
            tablaInventario.setPlaceholder(new Label(
                    "Error de consulta. No se han cargado existencias. Pulsa Actualizar para reintentar."));
            System.err.println("Error consultando inventario unificado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            btnActualizar.setDisable(false);
        }
    }

    @FXML private void onActualizar() { cargarDatos(); }
    @FXML private void onVolver() { CareStockApp.getInstance().mostrarCatalogo(); }
    @FXML private void onCerrarSesion() { CareStockApp.getInstance().cerrarSesion(); }
}
