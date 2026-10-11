
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
import org.example.carestock.model.Medicamento;

import org.example.carestock.session.SesionUsuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador del catalogo de productos.
 *
 * El CRUD utiliza la fachada Inventario.
 * La consulta unificada es una pantalla independiente.
 */
public class CatalogoController {

    // ========================================
    // COMPONENTES GENERALES
    // ========================================

    @FXML private ComboBox<String> cmbCategoria;

    @FXML
    private TableView<RegistroCatalogo<?>> tablaProductos;

    @FXML
    private TableColumn<RegistroCatalogo<?>, String>
            colId, colCodigo, colNombre, colDescripcion,
            colPrecio, colStock, colEstado;

    @FXML
    private Label lblSeleccion, lblEstado,
            lblCodigoCampo, lblNombreCampo;

    @FXML
    private TextField txtCodigo, txtNombre,
            txtDescripcion, txtPrecio, txtStock;

    @FXML
    private VBox panelDescripcion, panelPrecioStock;

    // ========================================
    // CAMPOS DE ASEO
    // ========================================

    @FXML private TextField txtTipoAseo;
    @FXML private TextField txtComponentesActivos;
    @FXML private CheckBox chkBiodegradable;
    @FXML private VBox panelAseo;

    // ========================================
    // CAMPOS DE MATERNIDAD
    // ========================================

    @FXML private TextField txtEtapa;
    @FXML private TextField txtEdadGestacional;
    @FXML private CheckBox chkHipoalergenico;
    @FXML private VBox panelMaternidad;

    // ========================================
    // CAMPOS DE MEDICAMENTOS
    // ========================================

    @FXML private VBox panelMedicamentos;
    @FXML private TextField txtPrecioMedicamento;
    @FXML private TextField txtPrincipioActivo;
    @FXML private TextField txtConcentracion;
    @FXML private TextField txtFormaFarmaceutica;
    @FXML private TextField txtPresentacion;
    @FXML private TextField txtStockMinimo;

    @FXML
    private ComboBox<CategoriaMedicamento>
            cmbCategoriaMedicamento;

    @FXML private Label lblStockMedicamento;

    // ========================================
    // FACHADA GOF
    // ========================================

    private Inventario inventario;
    private RegistroCatalogo<?> seleccionado;

    public record CategoriaMedicamento(
            int id,
            String nombre
    ) {
        @Override
        public String toString() {
            return nombre + " (#" + id + ")";
        }
    }

    // ========================================
    // INICIALIZACION
    // ========================================

    @FXML
    public void initialize() {

        if (txtPrecioMedicamento == null ||
                panelMedicamentos == null) {

            throw new IllegalStateException(
                    "El FXML esta desactualizado: "
                            + "faltan controles de medicamentos."
            );
        }

        inventario = new Inventario(
                SesionUsuario.usuarioParaFachada()
        );

        cmbCategoria.setItems(
                FXCollections.observableArrayList(
                        "Medicamentos",
                        "Aseo",
                        "Maternidad"
                )
        );

        configurarTabla();
        configurarSeleccion();
        configurarCategorias();

        cmbCategoria.getSelectionModel()
                .select("Medicamentos");
    }

    // ========================================
    // CONFIGURACION DE TABLA
    // ========================================

    private void configurarTabla() {

        colId.setCellValueFactory(
                c -> str(c.getValue().id())
        );

        colCodigo.setCellValueFactory(
                c -> str(codigo(c.getValue().producto()))
        );

        colNombre.setCellValueFactory(
                c -> str(nombre(c.getValue().producto()))
        );

        colDescripcion.setCellValueFactory(
                c -> str(descripcion(c.getValue().producto()))
        );

        colPrecio.setCellValueFactory(
                c -> str(precioTexto(c.getValue().producto()))
        );

        colStock.setCellValueFactory(
                c -> str(stock(c.getValue().producto()))
        );

        colEstado.setCellValueFactory(
                c -> str(c.getValue().estado())
        );
    }

    private ReadOnlyStringWrapper str(Object valor) {

        return new ReadOnlyStringWrapper(
                valor == null ? "" : valor.toString()
        );
    }

    // ========================================
    // DATOS DE PRODUCTO
    // ========================================

    private String codigo(Object producto) {

        if (producto instanceof Aseo a) {
            return a.getCodigo();
        }

        if (producto instanceof Maternidad m) {
            return m.getCodigo();
        }

        return ((Medicamento) producto).getCodigoInvima();
    }

    private String nombre(Object producto) {

        if (producto instanceof Aseo a) {
            return a.getNombre();
        }

        if (producto instanceof Maternidad m) {
            return m.getNombre();
        }

        return ((Medicamento) producto).getNombreComercial();
    }

    private String descripcion(Object producto) {

        if (producto instanceof Aseo a) {
            return a.getDescripcion();
        }

        if (producto instanceof Maternidad m) {
            return m.getDescripcion();
        }

        Medicamento m = (Medicamento) producto;

        return m.getPrincipioActivo() == null
                ? ""
                : "Principio activo: " + m.getPrincipioActivo();
    }

    private String precioTexto(Object producto) {

        if (producto instanceof Aseo a) {
            return Double.toString(a.getPrecio());
        }

        if (producto instanceof Maternidad m) {
            return Double.toString(m.getPrecio());
        }

        Medicamento m = (Medicamento) producto;

        return m.getPrecio() == null
                ? "Sin precio"
                : m.getPrecio().toPlainString();
    }

    private int stock(Object producto) {

        if (producto instanceof Aseo a) {
            return a.getStock();
        }

        if (producto instanceof Maternidad m) {
            return m.getStock();
        }

        return ((Medicamento) producto).getStockTotal();
    }

    // ========================================
    // SELECCION
    // ========================================

    private void configurarSeleccion() {

        tablaProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) -> {

                    seleccionado = actual;

                    if (actual == null) {

                        lblSeleccion.setText("Nuevo producto");

                    } else {

                        lblSeleccion.setText(
                                "Editando registro #" + actual.id()
                        );

                        llenarFormulario(actual.producto());
                    }
                });
    }

    // ========================================
    // CAMBIO DE CATEGORIA
    // ========================================

    private void configurarCategorias() {

        cmbCategoria.valueProperty()
                .addListener((obs, anterior, actual) -> {

                    boolean esMedicamento =
                            "Medicamentos".equals(actual);

                    visible(
                            panelAseo,
                            "Aseo".equals(actual)
                    );

                    visible(
                            panelMaternidad,
                            "Maternidad".equals(actual)
                    );

                    visible(
                            panelMedicamentos,
                            esMedicamento
                    );

                    visible(
                            panelDescripcion,
                            !esMedicamento
                    );

                    visible(
                            panelPrecioStock,
                            !esMedicamento
                    );

                    lblCodigoCampo.setText(
                            esMedicamento
                                    ? "Registro INVIMA"
                                    : "Codigo"
                    );

                    lblNombreCampo.setText(
                            esMedicamento
                                    ? "Nombre comercial"
                                    : "Nombre"
                    );

                    limpiar();

                    if (esMedicamento) {
                        cargarCategoriasMedicamento();
                    }

                    cargarTabla();
                });
    }

    private static void visible(
            VBox panel,
            boolean mostrar
    ) {

        panel.setVisible(mostrar);
        panel.setManaged(mostrar);
    }

    // ========================================
    // CONSULTAR PRODUCTOS
    // ========================================

    private void cargarTabla() {

        if (cmbCategoria.getValue() == null) {
            return;
        }

        try {

            List<RegistroCatalogo<?>> datos =
                    new ArrayList<>();

            switch (cmbCategoria.getValue()) {

                case "Aseo" ->
                        datos.addAll(inventario.listarAseo());

                case "Maternidad" ->
                        datos.addAll(inventario.listarMaternidad());

                case "Medicamentos" ->
                        datos.addAll(inventario.listarMedicamentos());

                default ->
                        throw new IllegalArgumentException(
                                "Categoria no reconocida"
                        );
            }

            tablaProductos.setItems(
                    FXCollections.observableArrayList(datos)
            );

            lblEstado.setText(
                    "Se cargaron " + datos.size()
                            + " registros de "
                            + cmbCategoria.getValue() + "."
            );

        } catch (Exception e) {

            tablaProductos.setItems(
                    FXCollections.observableArrayList()
            );

            mostrarError(
                    "No se pudo cargar el catalogo",
                    e
            );
        }
    }

    // ========================================
    // CATEGORIAS FARMACOLOGICAS
    // ========================================

    private void cargarCategoriasMedicamento() {

        try {

            int seleccion =
                    cmbCategoriaMedicamento.getValue() == null
                            ? -1
                            : cmbCategoriaMedicamento.getValue().id();

            Map<Integer, String> categorias =
                    inventario.listarCategoriasMedicamento();

            var opciones =
                    FXCollections
                            .<CategoriaMedicamento>observableArrayList();

            categorias.forEach(
                    (id, nombre) ->
                            opciones.add(
                                    new CategoriaMedicamento(id, nombre)
                            )
            );

            cmbCategoriaMedicamento.setItems(opciones);

            opciones.stream()
                    .filter(c -> c.id() == seleccion)
                    .findFirst()
                    .ifPresent(
                            cmbCategoriaMedicamento::setValue
                    );

        } catch (Exception e) {

            mostrarError(
                    "No se pudieron cargar categorias",
                    e
            );
        }
    }

    // ========================================
    // ACCIONES Y NAVEGACION
    // ========================================

    @FXML
    private void onRefrescar() {

        if ("Medicamentos".equals(cmbCategoria.getValue())) {
            cargarCategoriasMedicamento();
        }

        cargarTabla();
    }

    /**
     * NUEVA FUNCIONALIDAD:
     * Abre la consulta unificada del inventario.
     */
    @FXML
    private void onConsultaUnificada() {

        CareStockApp.getInstance()
                .mostrarConsultaUnificada();
    }

    @FXML
    private void onVolver() {

        CareStockApp.getInstance()
                .mostrarDashboard();
    }

    @FXML
    private void onCerrarSesion() {

        CareStockApp.getInstance()
                .cerrarSesion();
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    // ========================================
    // LIMPIAR FORMULARIO
    // ========================================

    private void limpiar() {

        seleccionado = null;

        tablaProductos.getSelectionModel()
                .clearSelection();

        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();

        txtTipoAseo.clear();
        txtComponentesActivos.clear();

        txtEtapa.clear();
        txtEdadGestacional.clear();

        chkBiodegradable.setSelected(false);
        chkHipoalergenico.setSelected(false);

        txtPrincipioActivo.clear();
        txtConcentracion.clear();
        txtFormaFarmaceutica.clear();
        txtPresentacion.clear();
        txtStockMinimo.clear();
        txtPrecioMedicamento.clear();

        cmbCategoriaMedicamento.getSelectionModel()
                .clearSelection();

        lblStockMedicamento.setText(
                "0 (gestionado mediante lotes)"
        );

        lblSeleccion.setText("Nuevo producto");
    }

    // ========================================
    // LLENAR FORMULARIO
    // ========================================

    private void llenarFormulario(Object producto) {

        txtCodigo.setText(codigo(producto));
        txtNombre.setText(nombre(producto));

        if (producto instanceof Aseo a) {

            txtDescripcion.setText(a.getDescripcion());

            txtPrecio.setText(
                    Double.toString(a.getPrecio())
            );

            txtStock.setText(
                    Integer.toString(a.getStock())
            );

            txtTipoAseo.setText(
                    nulo(a.getTipoAseo())
            );

            txtComponentesActivos.setText(
                    nulo(a.getComponentesActivos())
            );

            chkBiodegradable.setSelected(
                    a.isBiodegradable()
            );

        } else if (producto instanceof Maternidad m) {

            txtDescripcion.setText(m.getDescripcion());

            txtPrecio.setText(
                    Double.toString(m.getPrecio())
            );

            txtStock.setText(
                    Integer.toString(m.getStock())
            );

            txtEtapa.setText(
                    nulo(m.getEtapaRecomendada())
            );

            txtEdadGestacional.setText(
                    m.getEdadGestacionalSugerida() == 0
                            ? ""
                            : Integer.toString(
                            m.getEdadGestacionalSugerida()
                    )
            );

            chkHipoalergenico.setSelected(
                    m.isHipoalergenico()
            );

        } else if (producto instanceof Medicamento m) {

            txtPrincipioActivo.setText(
                    nulo(m.getPrincipioActivo())
            );

            txtConcentracion.setText(
                    nulo(m.getConcentracion())
            );

            txtFormaFarmaceutica.setText(
                    nulo(m.getFormaFarmaceutica())
            );

            txtPresentacion.setText(
                    nulo(m.getPresentacion())
            );

            txtStockMinimo.setText(
                    Integer.toString(m.getStockMinimo())
            );

            txtPrecioMedicamento.setText(
                    m.getPrecio() == null
                            ? ""
                            : m.getPrecio().toPlainString()
            );

            lblStockMedicamento.setText(
                    m.getStockTotal()
                            + " (solo lectura, gestionado por lotes)"
            );

            cmbCategoriaMedicamento.getItems()
                    .stream()
                    .filter(c ->
                            m.getIdCategoria() != null
                                    && c.id() == m.getIdCategoria()
                    )
                    .findFirst()
                    .ifPresent(
                            cmbCategoriaMedicamento::setValue
                    );
        }
    }

    private static String nulo(String texto) {
        return texto == null ? "" : texto;
    }

    // ========================================
    // VALIDACION DE PRECIOS
    // ========================================

    private double leerPrecio() {

        double valor = Double.parseDouble(
                txtPrecio.getText().trim()
        );

        if (!Double.isFinite(valor) || valor < 0) {

            throw new IllegalArgumentException(
                    "El precio debe ser no negativo"
            );
        }

        return valor;
    }

    private BigDecimal leerPrecioMedicamento() {

        String texto = txtPrecioMedicamento.getText();

        // Medicamentos historicos sin precio.
        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {

            BigDecimal valor = new BigDecimal(
                    texto.trim()
            );

            if (valor.signum() < 0
                    || valor.scale() > 2
                    || valor.precision() - valor.scale() > 10) {

                throw new IllegalArgumentException(
                        "El precio debe ser no negativo, "
                                + "con maximo dos decimales "
                                + "y 10 digitos enteros."
                );
            }

            return valor;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Precio invalido. Use: 15000.00"
            );
        }
    }

    private int leerStock() {

        int valor = Integer.parseInt(
                txtStock.getText().trim()
        );

        if (valor < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }

        return valor;
    }

    // ========================================
    // BUILDER GOF - ASEO
    // ========================================

    private Aseo construirAseo() {

        return new AseoBuilder()
                .setCodigo(txtCodigo.getText())
                .setNombre(txtNombre.getText())
                .setDescripcion(txtDescripcion.getText())
                .setPrecio(leerPrecio())
                .setStock(leerStock())
                .tipoAseo(txtTipoAseo.getText())
                .componentesActivos(
                        txtComponentesActivos.getText()
                )
                .biodegradable(
                        chkBiodegradable.isSelected()
                )
                .build();
    }

    // ========================================
    // BUILDER GOF - MATERNIDAD
    // ========================================

    private Maternidad construirMaternidad() {

        int edad =
                txtEdadGestacional.getText().isBlank()
                        ? 0
                        : Integer.parseInt(
                        txtEdadGestacional.getText().trim()
                );

        return new MaternidadBuilder()
                .setCodigo(txtCodigo.getText())
                .setNombre(txtNombre.getText())
                .setDescripcion(txtDescripcion.getText())
                .setPrecio(leerPrecio())
                .setStock(leerStock())
                .etapaRecomendada(txtEtapa.getText())
                .hipoalergenico(
                        chkHipoalergenico.isSelected()
                )
                .edadGestacionalSugerida(edad)
                .build();
    }

    // ========================================
    // CONSTRUCCION DE MEDICAMENTO
    // ========================================

    private Medicamento construirMedicamento() {

        CategoriaMedicamento categoria =
                cmbCategoriaMedicamento.getValue();

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Seleccione una categoria farmacologica"
            );
        }

        int minimo = Integer.parseInt(
                txtStockMinimo.getText().trim()
        );

        return Medicamento.builder()
                .codigoInvima(txtCodigo.getText())
                .nombreComercial(txtNombre.getText())
                .principioActivo(
                        txtPrincipioActivo.getText()
                )
                .concentracion(
                        txtConcentracion.getText()
                )
                .formaFarmaceutica(
                        txtFormaFarmaceutica.getText()
                )
                .presentacion(
                        txtPresentacion.getText()
                )
                .idCategoria(categoria.id())
                .stockMinimo(minimo)
                .precio(leerPrecioMedicamento())
                .stockTotal(0)
                .estado("ACTIVO")
                .build();
    }

    // ========================================
    // REGISTRAR PRODUCTO
    // ========================================

    @FXML
    private void onGuardar() {

        try {

            int id = switch (cmbCategoria.getValue()) {

                case "Aseo" ->
                        inventario.registrarAseo(
                                construirAseo()
                        );

                case "Maternidad" ->
                        inventario.registrarMaternidad(
                                construirMaternidad()
                        );

                case "Medicamentos" ->
                        inventario.registrarMedicamento(
                                construirMedicamento()
                        ).getIdMedicamento();

                default ->
                        throw new IllegalArgumentException(
                                "Seleccione una categoria"
                        );
            };

            limpiar();
            cargarTabla();

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Producto registrado con ID " + id,
                    ButtonType.OK
            ).showAndWait();

        } catch (Exception e) {

            mostrarError(
                    "No se pudo registrar",
                    e
            );
        }
    }

    // ========================================
    // ACTUALIZAR PRODUCTO
    // ========================================

    @FXML
    private void onActualizar() {

        if (seleccionado == null) {

            advertencia(
                    "Seleccione un producto para actualizar"
            );

            return;
        }

        try {

            boolean ok = switch (cmbCategoria.getValue()) {

                case "Aseo" ->
                        inventario.actualizarAseo(
                                seleccionado.id(),
                                construirAseo()
                        );

                case "Maternidad" ->
                        inventario.actualizarMaternidad(
                                seleccionado.id(),
                                construirMaternidad()
                        );

                case "Medicamentos" ->
                        inventario.actualizarMedicamento(
                                seleccionado.id(),
                                construirMedicamento()
                        );

                default ->
                        throw new IllegalArgumentException(
                                "Seleccione una categoria"
                        );
            };

            if (!ok) {

                advertencia(
                        "Registro inexistente, inactivo "
                                + "o de otra farmacia"
                );

                return;
            }

            limpiar();
            cargarTabla();

            lblEstado.setText(
                    "Producto actualizado correctamente."
            );

        } catch (Exception e) {

            mostrarError(
                    "No se pudo actualizar",
                    e
            );
        }
    }

    // ========================================
    // DESACTIVAR PRODUCTO
    // ========================================

    @FXML
    private void onDesactivar() {

        if (seleccionado == null) {

            advertencia("Seleccione un producto");
            return;
        }

        Alert confirmar = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desactivar producto #"
                        + seleccionado.id()
                        + "?",
                ButtonType.YES,
                ButtonType.NO
        );

        if (confirmar.showAndWait()
                .orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {

            boolean ok = switch (cmbCategoria.getValue()) {

                case "Aseo" ->
                        inventario.desactivarAseo(
                                seleccionado.id()
                        );

                case "Maternidad" ->
                        inventario.desactivarMaternidad(
                                seleccionado.id()
                        );

                case "Medicamentos" ->
                        inventario.desactivarMedicamento(
                                seleccionado.id()
                        );

                default -> false;
            };

            if (!ok) {

                advertencia(
                        "No se pudo desactivar. "
                                + "Revise estado, farmacia y stock."
                );

                return;
            }

            limpiar();
            cargarTabla();

            lblEstado.setText("Producto desactivado.");

        } catch (Exception e) {

            mostrarError(
                    "No se pudo desactivar",
                    e
            );
        }
    }

    // ========================================
    // MENSAJES
    // ========================================

    private void advertencia(String mensaje) {

        new Alert(
                Alert.AlertType.WARNING,
                mensaje,
                ButtonType.OK
        ).showAndWait();
    }

    private void mostrarError(
            String titulo,
            Exception e
    ) {

        lblEstado.setText(titulo);

        new Alert(
                Alert.AlertType.ERROR,
                titulo + ": " + e.getMessage(),
                ButtonType.OK
        ).showAndWait();
    }
}
