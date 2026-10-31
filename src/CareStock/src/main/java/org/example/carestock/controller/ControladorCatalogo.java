
package org.example.carestock.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import org.example.carestock.AplicacionCareStock;
import org.example.carestock.DataTransferObject.LecturaCampos;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.facade.Inventario;

import org.example.carestock.model.Aseo;
import org.example.carestock.factory.FabricaAseo;
import org.example.carestock.factory.FabricaMaternidad;
import org.example.carestock.factory.FabricaMedicamento;
import org.example.carestock.factory.FabricaProducto;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;

import org.example.carestock.session.SesionUsuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
/**
 * Controlador del catalogo de productos.
 *
 * El CRUD utiliza la fachada Inventario.
 * La consulta unificada es una pantalla independiente.
 */
public class ControladorCatalogo {

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
    @FXML private TextField txtStockMinimoAseo;
    @FXML private TextField txtStockMinimoMaternidad;
    @FXML private CheckBox chkHipoalergenico;
    @FXML private VBox panelMaternidad;

    // ========================================
    // CAMPOS DE MEDICAMENTOS
    // ========================================

    @FXML private VBox panelMedicamentos;
    @FXML private VBox panelFormulario;
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

    private static final FabricaProducto<Aseo> FABRICA_ASEO =
            new FabricaAseo();

    private static final FabricaProducto<Maternidad> FABRICA_MATERNIDAD =
            new FabricaMaternidad();

    private static final FabricaProducto<Medicamento> FABRICA_MEDICAMENTO =
            new FabricaMedicamento();

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
    @FXML private Button btnLimpiar;

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

        boolean puedeEditar = SesionUsuario.esAdministrador();
        panelFormulario.setDisable(!puedeEditar);
        if (!puedeEditar) {
            lblEstado.setText("Modo consulta: solo ADMINISTRADOR puede modificar productos.");
        }
    }

    // ========================================
    public void abrirDesdeConsulta(
            String categoria,
            Integer idProducto
    ) {
        if (!SesionUsuario.esAdministrador()) {
            lblEstado.setText(
                    "Sin permisos para modificar productos."
            );
            return;
        }

        String categoriaFormulario = switch (categoria) {
            case "Medicamento", "Medicamentos" -> "Medicamentos";
            case "Aseo" -> "Aseo";
            case "Maternidad" -> "Maternidad";
            default -> throw new IllegalArgumentException(
                    "Categoria no reconocida"
            );
        };

        cmbCategoria.getSelectionModel()
                .select(categoriaFormulario);

        if (idProducto == null) {
            limpiar();
            lblEstado.setText(
                    "Formulario listo para registrar."
            );
            txtCodigo.requestFocus();
            return;
        }

        for (RegistroCatalogo<?> fila :
                tablaProductos.getItems()) {

            if (fila.id() == idProducto.intValue()) {
                tablaProductos.getSelectionModel().select(fila);
                tablaProductos.scrollTo(fila);
                return;
            }
        }

        lblEstado.setText(
                "Producto no disponible en la farmacia activa."
        );
    }

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

        colStock.setCellFactory(col -> new TableCell<RegistroCatalogo<?>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    
                    RegistroCatalogo<?> registro = getTableRow().getItem();
                    Integer minimoProducto = registro == null
                            ? null : stockMinimoDe(registro.producto());
                    if (registro != null && minimoProducto != null) {
                        if (stock(registro.producto()) <= minimoProducto) {
                            Tooltip tooltip = new Tooltip("¡Alerta de stock mínimo!");
                            tooltip.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-weight: bold;");
                            setTooltip(tooltip);
                        } else {
                            setTooltip(null);
                        }
                    } else {
                        setTooltip(null);
                    }
                }
            }
        });

        colEstado.setCellValueFactory(
                c -> str(c.getValue().estado())
        );
        tablaProductos.setRowFactory(tv -> new TableRow<RegistroCatalogo<?>>() {
            @Override
            protected void updateItem(RegistroCatalogo<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    int stockActual = stock(item.producto());
                    Integer stockMinimo = stockMinimoDe(item.producto());

                    if (stockMinimo != null && stockActual <= stockMinimo) {
                        setStyle("-fx-background-color: #FFE5E5; -fx-text-fill: #C0392B;");
                    } else {
                        setStyle(""); 
                    }
                }
            }
        });
    }

    private static Integer stockMinimoDe(Object producto) {
        if (producto instanceof Medicamento m) {
            return m.getStockMinimo();
        }
        if (producto instanceof Aseo a) {
            return a.getStockMinimo();
        }
        if (producto instanceof Maternidad m) {
            return m.getStockMinimo();
        }
        return null;
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

        AplicacionCareStock.getInstance()
                .mostrarConsultaUnificada();
    }

    @FXML
    private void onVolver() {

        AplicacionCareStock.getInstance()
                .mostrarDashboard();
    }

    @FXML
    private void onCerrarSesion() {

        AplicacionCareStock.getInstance()
                .cerrarSesion();
    }

    @FXML
    private void onNuevo() {
        tablaProductos.getSelectionModel().clearSelection();
        limpiar();
        txtCodigo.requestFocus();
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
        txtStockMinimoAseo.clear();
        txtStockMinimoMaternidad.clear();

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

            txtStockMinimoAseo.setText(
                    Integer.toString(a.getStockMinimo())
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

            txtStockMinimoMaternidad.setText(
                    Integer.toString(m.getStockMinimo())
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
        return LecturaCampos.precio(txtPrecio.getText());
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
        return LecturaCampos.entero(txtStock.getText(), "El stock");
    }

    // ========================================
    // FACTORY METHOD GOF - ASEO
    // ========================================

    private Aseo construirAseo() {
        int minimo = LecturaCampos.enteroOpcional(
                txtStockMinimoAseo.getText(),
                "El stock mínimo");

        return FABRICA_ASEO.crear(p -> {
            p.setCodigo(txtCodigo.getText());
            p.setNombre(txtNombre.getText());
            p.setDescripcion(txtDescripcion.getText());
            p.setPrecio(leerPrecio());
            p.setStock(leerStock());
            p.setStockMinimo(minimo);
            p.setTipoAseo(txtTipoAseo.getText());
            p.setComponentesActivos(
                    txtComponentesActivos.getText());
            p.setBiodegradable(
                    chkBiodegradable.isSelected());
        });
    }

    // ========================================
    // FACTORY METHOD GOF - MATERNIDAD
    // ========================================

    private Maternidad construirMaternidad() {
        int edad = LecturaCampos.enteroOpcional(
                txtEdadGestacional.getText(),
                "La edad gestacional");
        int minimo = LecturaCampos.enteroOpcional(
                txtStockMinimoMaternidad.getText(),
                "El stock mínimo");

        return FABRICA_MATERNIDAD.crear(p -> {
            p.setCodigo(txtCodigo.getText());
            p.setNombre(txtNombre.getText());
            p.setDescripcion(txtDescripcion.getText());
            p.setPrecio(leerPrecio());
            p.setStock(leerStock());
            p.setStockMinimo(minimo);
            p.setEtapaRecomendada(txtEtapa.getText());
            p.setHipoalergenico(
                    chkHipoalergenico.isSelected());
            p.setEdadGestacionalSugerida(edad);
        });
    }

    // ========================================
    // FACTORY METHOD GOF - MEDICAMENTO
    // ========================================

    private Medicamento construirMedicamento() {
        CategoriaMedicamento categoria =
                cmbCategoriaMedicamento.getValue();

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Seleccione una categoria farmacologica");
        }

        int minimo = LecturaCampos.entero(
                txtStockMinimo.getText(),
                "El stock minimo");

        return FABRICA_MEDICAMENTO.crear(p -> {
            p.setCodigoInvima(txtCodigo.getText());
            p.setNombreComercial(txtNombre.getText());
            p.setPrincipioActivo(
                    txtPrincipioActivo.getText());
            p.setConcentracion(txtConcentracion.getText());
            p.setFormaFarmaceutica(
                    txtFormaFarmaceutica.getText());
            p.setPresentacion(txtPresentacion.getText());
            p.setIdCategoria(categoria.id());
            p.setStockMinimo(minimo);
            p.setPrecio(leerPrecioMedicamento());
            p.setStockTotal(0);
            p.setEstado("ACTIVO");
        });
    }

    // ========================================
    // REGISTRAR PRODUCTO
    // ========================================

    @FXML
        private void onGuardar() {
        if (!validarPermisoEdicion()) return;
        try {

            String nombreProducto = txtNombre.getText().trim();

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

            if (id <= 0) {
                throw new Exception("La base de datos no devolvió un ID válido.");
            }

            limpiar();
            cargarTabla();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Registro Exitoso");
            alerta.setHeaderText(null);
            alerta.setContentText("El producto '" + nombreProducto + "' fue guardado correctamente. ID: " + id);

            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
            delay.setOnFinished(e -> alerta.close());
            delay.play();

            alerta.showAndWait();

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
        if (!validarPermisoEdicion()) return;
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

            String nombreProducto = txtNombre.getText().trim();

            limpiar();
            cargarTabla();
            
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Actualización Exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("El producto '" + nombreProducto + "' fue actualizado correctamente.");

            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
            delay.setOnFinished(e -> alerta.close());
            delay.play();

            alerta.showAndWait();

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
        if (!validarPermisoEdicion()) return;
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

    private boolean validarPermisoEdicion() {
        if (SesionUsuario.esAdministrador()) return true;
        advertencia("Solo ADMINISTRADOR puede modificar el catalogo.");
        return false;
    }

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

        Alert alertaError = new Alert(Alert.AlertType.ERROR);
        alertaError.setTitle("Error en el Sistema");
        alertaError.setHeaderText(titulo);
        alertaError.setContentText(e.getMessage() != null ? e.getMessage() : "Ocurrió un error inesperado.");
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        delay.setOnFinished(event -> alertaError.close());
        delay.play();

        alertaError.showAndWait();
    }
}
