package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.carestock.CareStockApp;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.facade.Inventario;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.AseoBuilder;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.MaternidadBuilder;
import org.example.carestock.session.SesionUsuario;

import java.util.ArrayList;
import java.util.List;

/** Controlador de presentacion; la persistencia se gestiona via Inventario (Facade). */
public class CatalogoController {

    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TableView<RegistroCatalogo<?>> tablaProductos;
    @FXML private TableColumn<RegistroCatalogo<?>, String> colId, colCodigo,
            colNombre, colDescripcion, colPrecio, colStock, colEstado;
    @FXML private TextField txtCodigo, txtNombre, txtDescripcion, txtPrecio, txtStock;
    @FXML private TextField txtTipoAseo, txtComponentesActivos;
    @FXML private CheckBox chkBiodegradable;
    @FXML private TextField txtEtapa, txtEdadGestacional;
    @FXML private CheckBox chkHipoalergenico;
    @FXML private VBox panelAseo, panelMaternidad;
    @FXML private Label lblSeleccion, lblEstado;
    @FXML private Button btnGuardar, btnActualizar, btnDesactivar;

    private Inventario inventario;
    private RegistroCatalogo<?> seleccionado;

    @FXML
    public void initialize() {
        inventario = new Inventario(SesionUsuario.usuarioParaFachada());
        cmbCategoria.setItems(FXCollections.observableArrayList("Aseo", "Maternidad"));

        colId.setCellValueFactory(c -> str(c.getValue().id()));
        colCodigo.setCellValueFactory(c -> str(codigo(c.getValue().producto())));
        colNombre.setCellValueFactory(c -> str(nombre(c.getValue().producto())));
        colDescripcion.setCellValueFactory(c -> str(descripcion(c.getValue().producto())));
        colPrecio.setCellValueFactory(c -> str(precio(c.getValue().producto())));
        colStock.setCellValueFactory(c -> str(stock(c.getValue().producto())));
        colEstado.setCellValueFactory(c -> str(c.getValue().estado()));

        tablaProductos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    seleccionado = actual;
                    if (actual == null) {
                        lblSeleccion.setText("Nuevo producto");
                    } else {
                        lblSeleccion.setText("Editando registro #" + actual.id());
                        llenarFormulario(actual.producto());
                    }
                });

        cmbCategoria.valueProperty().addListener((obs, anterior, actual) -> {
            panelAseo.setVisible("Aseo".equals(actual));
            panelAseo.setManaged("Aseo".equals(actual));
            panelMaternidad.setVisible("Maternidad".equals(actual));
            panelMaternidad.setManaged("Maternidad".equals(actual));
            limpiar();
            cargarTabla();
        });
        cmbCategoria.getSelectionModel().select("Aseo");
    }

    private ReadOnlyStringWrapper str(Object valor) {
        return new ReadOnlyStringWrapper(valor == null ? "" : valor.toString());
    }

    private String codigo(Object p) {
        return p instanceof Aseo a ? a.getCodigo() : ((Maternidad)p).getCodigo();
    }
    private String nombre(Object p) {
        return p instanceof Aseo a ? a.getNombre() : ((Maternidad)p).getNombre();
    }
    private String descripcion(Object p) {
        return p instanceof Aseo a ? a.getDescripcion() : ((Maternidad)p).getDescripcion();
    }
    private double precio(Object p) {
        return p instanceof Aseo a ? a.getPrecio() : ((Maternidad)p).getPrecio();
    }
    private int stock(Object p) {
        return p instanceof Aseo a ? a.getStock() : ((Maternidad)p).getStock();
    }

    private void cargarTabla() {
        try {
            List<RegistroCatalogo<?>> datos = new ArrayList<>();
            if ("Aseo".equals(cmbCategoria.getValue())) {
                datos.addAll(inventario.listarAseo());
            } else if ("Maternidad".equals(cmbCategoria.getValue())) {
                datos.addAll(inventario.listarMaternidad());
            }
            tablaProductos.setItems(FXCollections.observableArrayList(datos));
            lblEstado.setText("Se cargaron " + datos.size() + " registros.");
        } catch (Exception e) {
            mostrarError("No se pudo cargar el catalogo", e);
        }
    }

    @FXML private void onRefrescar() { cargarTabla(); }
    @FXML private void onVolver() { CareStockApp.getInstance().mostrarDashboard(); }
    @FXML private void onCerrarSesion() { CareStockApp.getInstance().cerrarSesion(); }

    @FXML
    private void onNuevo() {
        tablaProductos.getSelectionModel().clearSelection();
        limpiar();
    }

    private void limpiar() {
        seleccionado = null;
        tablaProductos.getSelectionModel().clearSelection();
        txtCodigo.clear(); txtNombre.clear(); txtDescripcion.clear();
        txtPrecio.clear(); txtStock.clear(); txtTipoAseo.clear();
        txtComponentesActivos.clear(); txtEtapa.clear(); txtEdadGestacional.clear();
        chkBiodegradable.setSelected(false);
        chkHipoalergenico.setSelected(false);
        lblSeleccion.setText("Nuevo producto");
    }

    private void llenarFormulario(Object p) {
        txtCodigo.setText(codigo(p));
        txtNombre.setText(nombre(p));
        txtDescripcion.setText(descripcion(p));
        txtPrecio.setText(Double.toString(precio(p)));
        txtStock.setText(Integer.toString(stock(p)));
        if (p instanceof Aseo a) {
            txtTipoAseo.setText(nulo(a.getTipoAseo()));
            txtComponentesActivos.setText(nulo(a.getComponentesActivos()));
            chkBiodegradable.setSelected(a.isBiodegradable());
        } else if (p instanceof Maternidad m) {
            txtEtapa.setText(nulo(m.getEtapaRecomendada()));
            txtEdadGestacional.setText(m.getEdadGestacionalSugerida() == 0
                    ? "" : Integer.toString(m.getEdadGestacionalSugerida()));
            chkHipoalergenico.setSelected(m.isHipoalergenico());
        }
    }

    private String nulo(String text) { return text == null ? "" : text; }

    private double leerPrecio() {
        double valor = Double.parseDouble(txtPrecio.getText().trim());
        if (!Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException("El precio debe ser finito y no negativo");
        }
        return valor;
    }

    private int leerStock() {
        int valor = Integer.parseInt(txtStock.getText().trim());
        if (valor < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        return valor;
    }

    private Aseo construirAseo() {
        return new AseoBuilder()
                .setCodigo(txtCodigo.getText())
                .setNombre(txtNombre.getText())
                .setDescripcion(txtDescripcion.getText())
                .setPrecio(leerPrecio())
                .setStock(leerStock())
                .tipoAseo(txtTipoAseo.getText())
                .componentesActivos(txtComponentesActivos.getText())
                .biodegradable(chkBiodegradable.isSelected())
                .build();
    }

    private Maternidad construirMaternidad() {
        int edad = txtEdadGestacional.getText().isBlank() ? 0
                : Integer.parseInt(txtEdadGestacional.getText().trim());
        return new MaternidadBuilder()
                .setCodigo(txtCodigo.getText())
                .setNombre(txtNombre.getText())
                .setDescripcion(txtDescripcion.getText())
                .setPrecio(leerPrecio())
                .setStock(leerStock())
                .etapaRecomendada(txtEtapa.getText())
                .hipoalergenico(chkHipoalergenico.isSelected())
                .edadGestacionalSugerida(edad)
                .build();
    }

    @FXML
    private void onGuardar() {
        try {
            int id;
            if ("Aseo".equals(cmbCategoria.getValue())) {
                id = inventario.registrarAseo(construirAseo());
            } else {
                id = inventario.registrarMaternidad(construirMaternidad());
            }
            limpiar(); cargarTabla();
            new Alert(Alert.AlertType.INFORMATION,
                    "Producto registrado con ID " + id, ButtonType.OK).showAndWait();
        } catch (Exception e) { mostrarError("No se pudo registrar", e); }
    }

    @FXML
    private void onActualizar() {
        if (seleccionado == null) { advertencia("Seleccione un producto para actualizar"); return; }
        try {
            boolean ok;
            if ("Aseo".equals(cmbCategoria.getValue())) {
                ok = inventario.actualizarAseo(seleccionado.id(), construirAseo());
            } else {
                ok = inventario.actualizarMaternidad(seleccionado.id(), construirMaternidad());
            }
            if (!ok) { advertencia("El registro ya no esta activo o no pertenece a su farmacia"); return; }
            limpiar(); cargarTabla();
            lblEstado.setText("Producto actualizado correctamente.");
        } catch (Exception e) { mostrarError("No se pudo actualizar", e); }
    }

    @FXML
    private void onDesactivar() {
        if (seleccionado == null) { advertencia("Seleccione un producto"); return; }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desactivar el producto #" + seleccionado.id() + "?", ButtonType.YES, ButtonType.NO);
        if (confirmacion.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        try {
            boolean ok = "Aseo".equals(cmbCategoria.getValue())
                    ? inventario.desactivarAseo(seleccionado.id())
                    : inventario.desactivarMaternidad(seleccionado.id());
            if (!ok) { advertencia("No se pudo desactivar (ya inactivo o no existe)"); return; }
            limpiar(); cargarTabla();
            lblEstado.setText("Producto desactivado.");
        } catch (Exception e) { mostrarError("No se pudo desactivar", e); }
    }

    private void advertencia(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK).showAndWait();
    }
    private void mostrarError(String titulo, Exception e) {
        lblEstado.setText(titulo);
        new Alert(Alert.AlertType.ERROR, titulo + ": " + e.getMessage(), ButtonType.OK)
                .showAndWait();
    }
}
