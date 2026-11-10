package org.example.carestock.controller;
import org.example.carestock.utils.FechaNegocio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.IOException;
import org.example.carestock.service.IndicadoresCatalogo;
import org.example.carestock.service.ResumenCategoriaCatalogo;
import org.example.carestock.CareStockApp;
import org.example.carestock.session.SesionUsuario;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.carestock.facade.Inventario;
import org.example.carestock.service.ServicioAlertasInventario;
import org.example.carestock.service.ResolucionNavegacionAlertas;
import org.example.carestock.service.PoliticaAccionesAlertas;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.service.ValidadorDispensacion;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javafx.concurrent.Task;
import org.example.carestock.proxy.InventarioConsultas;

public class ControladorInventario {

    private static final System.Logger LOGGER =
            System.getLogger(ControladorInventario.class.getName());


    @FXML private TableView<Lote> tablaLotes;
    @FXML private TableColumn<Lote, Integer> colIdLote;
    @FXML private TableColumn<Lote, String> colNumeroLote;
    @FXML private TableColumn<Lote, Integer> colIdMedicamento;
    @FXML private TableColumn<Lote, Integer> colCantidad;
    @FXML private TableColumn<Lote, LocalDate> colFechaVencimiento;
    @FXML private TableColumn<Lote, String> colEstado;

    @FXML private Label lblUsuarioSesion;
    @FXML private Label lblEmailSesion;
    @FXML private Label lblRolSesion;
    @FXML private Label lblFarmaciaSesion;
    @FXML private Button btnCatalogo;
    @FXML private BorderPane dashboardRoot;
    @FXML private ScrollPane vistaLotes;

    private BorderPane vistaCatalogo;
    private ControladorCatalogo controladorCatalogo;
    private Label tituloTotalCatalogo;
    private Label tituloIntermedioCatalogo;
    private Label tituloAgotadosCatalogo;
    private Label totalMedicamentosCatalogo;
    private Label vencidosCatalogo;
    private Label agotadosCatalogo;
    private String estiloCatalogoOriginal;
    private int versionIndicadores;
    @FXML private Button btnHistorial;
    @FXML private Button btnAjustes;
    @FXML private Button btnAlertas;
    @FXML private Button btnExportarCSV;
    @FXML private Button btnNuevoLote;
    @FXML private Button btnEditarLote;
    @FXML private Button btnRetirarLote;

    @FXML private Label lblTotalLotes;
    @FXML private Label lblLotesVencidos;
    @FXML private Label lblLoteSeleccionadoInfo;

    @FXML private Label lblTotalMedicamentos;
    @FXML private Label lblTotalAseo;
    @FXML private Label lblTotalMaternidad;

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private DatePicker dateInicio;
    @FXML private DatePicker dateFin;
    
    @FXML private Label lblAlertaStock;
    @FXML private Label lblAlertaVencimiento;
    @FXML private Button btnCampana;
    @FXML private javafx.scene.shape.Circle badgeNotificaciones;
    @FXML private Label lblContadorNotificaciones;
    @FXML private Button btnGenerarQR;
    @FXML private TableColumn<Lote, String> colRefrigeracion;

    private static final String CATEGORIA_TODAS = "Todas";

    private Inventario inventario;
    private final ObservableList<Lote> listaLotes;
    private FilteredList<Lote> datosFiltrados;
    private Lote loteSeleccionado;
    private Integer idMedicamentoAlerta;
    private boolean cargaInicialEnCurso;
    private Destino destinoLotePendiente;
    private Predicate<Lote> filtroTexto = lote -> true;
    private Predicate<Lote> filtroEstado = lote -> true;
    private Predicate<Lote> filtroCategoria = lote -> true;
    private Map<Integer, String> categoriaPorMedicamento = new HashMap<>();

    public ControladorInventario() {
        this.listaLotes = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        if (!SesionUsuario.estaActiva()) {
            throw new IllegalStateException("No hay sesion autenticada");
        }
        inventario = new Inventario(SesionUsuario.usuarioParaFachada());
        var sesion = SesionUsuario.actual();
        lblUsuarioSesion.setText(sesion.nombre());
        lblEmailSesion.setText(sesion.email());
        lblRolSesion.setText(sesion.rol());
        lblFarmaciaSesion.setText(sesion.farmacia() == null
                ? "Sin farmacia asignada"
                : sesion.farmacia() + " (#" + sesion.idFarmacia() + ")");
        boolean puedeConsultarCatalogo = sesion.idFarmacia() != null;
        btnCatalogo.setVisible(puedeConsultarCatalogo);
        btnCatalogo.setManaged(puedeConsultarCatalogo);
        boolean puedeGestionarLotes = SesionUsuario.esAdministrador()
                || "SUPER_ADMIN".equalsIgnoreCase(sesion.rol())
                || "FARMACEUTICO".equalsIgnoreCase(sesion.rol());
        puedeGestionarLotes = puedeGestionarLotes
                && sesion.idFarmacia() != null;
        for (Button boton : List.of(btnNuevoLote, btnEditarLote, btnRetirarLote)) {
            boton.setVisible(puedeGestionarLotes);
            boton.setManaged(puedeGestionarLotes);
        }
        String rolHistorial = sesion.rol() == null ? "" : sesion.rol().trim();
        boolean puedeVerHistorial = puedeConsultarCatalogo
                && ("ADMINISTRADOR".equalsIgnoreCase(rolHistorial)
                    || "FARMACEUTICO".equalsIgnoreCase(rolHistorial));
        btnHistorial.setVisible(puedeVerHistorial);
        btnHistorial.setManaged(puedeVerHistorial);
        String rol = sesion.rol() == null ? "" : sesion.rol().trim();
        boolean puedeAjustar = puedeConsultarCatalogo
                && ("ADMINISTRADOR".equalsIgnoreCase(rol)
                    || "SUPER_ADMIN".equalsIgnoreCase(rol));
        btnAjustes.setVisible(puedeAjustar);
        btnAjustes.setManaged(puedeAjustar);
        btnAlertas.setVisible(puedeConsultarCatalogo);
        btnAlertas.setManaged(puedeConsultarCatalogo);

        colIdLote.setCellValueFactory(new PropertyValueFactory<>("idLote"));
        colNumeroLote.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colIdMedicamento.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadActual"));
        colFechaVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colFechaVencimiento.setCellFactory(col -> new TableCell<Lote, LocalDate>() {
            @Override
    protected void updateItem(LocalDate fecha, boolean empty) {
        super.updateItem(fecha, empty);
        if (empty || fecha == null) {
            setText(null);
            setTooltip(null);
        } else {
            setText(fecha.toString());
            long dias = java.time.temporal.ChronoUnit.DAYS.between(FechaNegocio.hoy(), fecha);
            String mensaje;
            if (dias > 0) {
                mensaje = "Vence en " + dias + " días";
            } else if (dias == 0) {
                mensaje = "Vence hoy";
            } else {
                mensaje = "Vencido hace " + Math.abs(dias) + " días";
            }
            setTooltip(new Tooltip(mensaje));
        }
    }
});
        colEstado.setCellValueFactory(datos ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        estadoMostrado(datos.getValue())));

        cmbCategoria.getItems().add(CATEGORIA_TODAS);
        cmbCategoria.getSelectionModel().selectFirst();
        // Las categorias se consultan en la carga asincrona.

        configurarEstiloFilas();
        configurarFiltroBusqueda();
                // #809: Configurar los DatePickers para el filtro
        dateInicio.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        dateFin.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        // Selección intuitiva de fila
        tablaLotes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            loteSeleccionado = newSel;
            if (newSel != null) {
                lblLoteSeleccionadoInfo.setText("ID " + newSel.getIdLote() + " | " + newSel.getNumeroLote() +
                        " (Med #" + newSel.getIdMedicamento() + ") - Stock: " + newSel.getCantidadActual());
                lblLoteSeleccionadoInfo.setStyle("-fx-text-fill: #20302F; -fx-font-weight: bold;");
            } else {
                lblLoteSeleccionadoInfo.setText("Ninguno (Haga clic en una fila de la tabla)");
                lblLoteSeleccionadoInfo.setStyle("-fx-text-fill: #5B6663; -fx-font-style: italic;");
            }
        });

        cargarDatosInicialesAsync();
    }

    public void abrirDesdeAlerta(Destino destino) {
        if (destino == null || !SesionUsuario.estaActiva()
                || !java.util.Objects.equals(
                        destino.idFarmacia(),
                        SesionUsuario.actual().idFarmacia())) {

            lblLoteSeleccionadoInfo.setText(
                    "La farmacia activa ha cambiado. No es posible abrir el lote.");
            return;
        }

        if (cargaInicialEnCurso) {
            destinoLotePendiente = destino;
            lblLoteSeleccionadoInfo.setText("Cargando lote solicitado...");
            return;
        }

        onFiltrarTodos();

        var encontrado = ResolucionNavegacionAlertas.buscarLote(
                destino, listaLotes);

        if (encontrado.isPresent()) {
            datosFiltrados.setPredicate(
                    lote -> lote.getIdLote() == destino.idReferencia());

            tablaLotes.getSelectionModel().select(encontrado.get());
            tablaLotes.scrollTo(encontrado.get());
        } else {
            tablaLotes.getSelectionModel().clearSelection();
            lblLoteSeleccionadoInfo.setText(
                    "El lote ya no existe o no esta disponible en la farmacia activa.");
        }
    }

    public void abrirGestionStockMedicamento(Destino destino) {
        if (destino == null
                || destino.tipo() != org.example.carestock.dto.AlertaInventario.TipoAlerta.STOCK_MINIMO
                || destino.categoria() != org.example.carestock.dto.AlertaInventario.Categoria.MEDICAMENTO
                || !SesionUsuario.estaActiva()
                || !PoliticaAccionesAlertas.gestorPermitido(SesionUsuario.actual().rol())
                || !java.util.Objects.equals(destino.idFarmacia(),
                        SesionUsuario.actual().idFarmacia())) {
            throw new SecurityException("Sin permiso para reponer este medicamento.");
        }
        idMedicamentoAlerta = destino.idReferencia();
        lblLoteSeleccionadoInfo.setText("Reponer " + destino.producto()
                + ": seleccione 'Nuevo lote' para registrar la entrada.");
    }

    private void configurarFiltroBusqueda() {
        datosFiltrados = new FilteredList<>(listaLotes, p -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroTexto(newVal));
        cmbCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroCategoria(newVal));

        SortedList<Lote> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaLotes.comparatorProperty());
        tablaLotes.setItems(datosOrdenados);
        tablaLotes.setPlaceholder(new Label("No hay datos disponibles para los filtros seleccionados."));
    }

        private void aplicarFiltros() {

        LocalDate inicio = dateInicio.getValue();
        LocalDate fin = dateFin.getValue();

        Predicate<Lote> filtroFechas = lote -> {
            if (lote.getFechaIngreso() == null) return false;
            LocalDate fechaLote = lote.getFechaIngreso().toLocalDate();
            
            boolean cumpleInicio = (inicio == null) || !fechaLote.isBefore(inicio);
            boolean cumpleFin = (fin == null) || !fechaLote.isAfter(fin);
            
            return cumpleInicio && cumpleFin;
        };

        datosFiltrados.setPredicate(filtroTexto.and(filtroEstado).and(filtroCategoria).and(filtroFechas));

        if (datosFiltrados.isEmpty()) {
            tablaLotes.setPlaceholder(new Label("No hay movimientos en este rango de fechas."));
        } else {
            tablaLotes.setPlaceholder(new Label("No hay datos disponibles para los filtros seleccionados."));
        }
    }
    
        private void aplicarFiltroTexto(String texto) {
        String filtro = texto == null ? "" : texto.toLowerCase().trim();
        filtroTexto = lote -> filtro.isEmpty()
                || (lote.getNumeroLote() != null && lote.getNumeroLote().toLowerCase().contains(filtro))
                || String.valueOf(lote.getIdMedicamento()).contains(filtro)
                || (lote.getEstadoLote() != null && lote.getEstadoLote().toLowerCase().contains(filtro));
        aplicarFiltros();
    }

    private void aplicarFiltroCategoria(String categoria) {
        if (categoria == null || CATEGORIA_TODAS.equals(categoria)) {
            filtroCategoria = lote -> true;
        } else {
            filtroCategoria = lote -> categoria.equalsIgnoreCase(categoriaPorMedicamento.get(lote.getIdMedicamento()));
        }
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarTodos() {
        filtroEstado = lote -> true;
        txtBuscar.clear();
        cmbCategoria.getSelectionModel().selectFirst();
        dateInicio.setValue(null); 
        dateFin.setValue(null);   
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarVencidos() {
        filtroEstado = lote -> lote.getFechaVencimiento() != null && !lote.getFechaVencimiento().isAfter(FechaNegocio.hoy());
        aplicarFiltros();
    }

    @FXML
    private void onFiltrarDisponibles() {
        filtroEstado = Lote::esAptoParaDispensar;
        aplicarFiltros();
    }

    private String estadoMostrado(Lote lote) {
        if ("RETENIDO".equalsIgnoreCase(lote.getEstadoLote())) {
            return "RETENIDO";
        }
        if (lote.getFechaVencimiento() != null
                && !lote.getFechaVencimiento().isAfter(FechaNegocio.hoy())) {
            return "DESHABILITADO";
        }
        return lote.getEstadoLote() == null
                ? "SIN ESTADO"
                : lote.getEstadoLote();
    }

    private void configurarEstiloFilas() {
        tablaLotes.setRowFactory(tv -> new TableRow<Lote>() {
            {
                selectedProperty().addListener((obs, antes, ahora) -> pintar());
            }

            private void pintar() {
                Lote lote = getItem();
                if (lote == null || isEmpty()) {
                    setStyle("");
                } else if (lote.getFechaVencimiento() != null
                        && !lote.getFechaVencimiento().isAfter(FechaNegocio.hoy())) {
                    setStyle(EstiloAlertas.estiloFila(EstiloAlertas.Nivel.CRITICO, isSelected()));
                } else if (!"DISPONIBLE".equalsIgnoreCase(lote.getEstadoLote())) {
                    setStyle(EstiloAlertas.estiloFila(EstiloAlertas.Nivel.ADVERTENCIA, isSelected()));
                } else {
                    setStyle("");
                }
            }

            @Override
            protected void updateItem(Lote item, boolean empty) {
                super.updateItem(item, empty);
                pintar();
            }
        });
    }

    @FXML
    private void onCrearLote() {
        GestorLotesDialogo gestor = new GestorLotesDialogo(
                tablaLotes.getScene().getWindow());
        boolean creado = idMedicamentoAlerta == null
                ? gestor.crear() : gestor.crearParaMedicamento(idMedicamentoAlerta);
        if (creado) onRefrescar();
    }

    @FXML
    private void onEditarLote() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccion requerida",
                    "Selecciona primero un lote de la tabla.");
            return;
        }
        GestorLotesDialogo gestor = new GestorLotesDialogo(
                tablaLotes.getScene().getWindow());
        if (gestor.editar(loteSeleccionado)) onRefrescar();
    }

    @FXML
    private void onRetirarLote() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccion requerida",
                    "Selecciona primero un lote de la tabla.");
            return;
        }
        GestorLotesDialogo gestor = new GestorLotesDialogo(
                tablaLotes.getScene().getWindow());
        if (gestor.retirar(loteSeleccionado)) onRefrescar();
    }

    @FXML
    private void onAbrirMovimientos() {
        org.example.carestock.CareStockApp.getInstance()
                .mostrarFormularioMovimientos();
    }



    @FXML
    private void onAbrirAjustes() {
        CareStockApp.getInstance().mostrarFormularioAjustes();
    }

    @FXML
    private void onAbrirHistorial() {
        CareStockApp.getInstance().mostrarHistorialMovimientos();
    }

    @FXML
    private void onAbrirReposicion() {
        CareStockApp.getInstance().mostrarReposicion();
    }

    @FXML
    private void onAbrirCatalogo() {
        CareStockApp.getInstance().mostrarCatalogo();
    }

    /**
     * Integra el CRUD existente dentro del MainDashboard.
     * No crea una nueva Scene ni una nueva Stage.
     */
    public void abrirCatalogoIntegrado(
            String categoria, Integer idProducto) {

        if (!SesionUsuario.estaActiva()
                || SesionUsuario.actual().idFarmacia() == null) {
            throw new SecurityException(
                    "Se requiere una farmacia activa");
        }

        if (vistaCatalogo == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/org/example/carestock/catalogo-view.fxml"));

                Parent contenido = loader.load();

                if (!(contenido instanceof BorderPane panel)) {
                    throw new IllegalStateException(
                            "El catalogo debe usar BorderPane");
                }

                controladorCatalogo = loader.getController();

                // Integrar indicadores y catalogo
                // en el mismo ScrollPane.
                // La cabecera azul del dashboard
                // permanece fija.

                if (!(panel.getCenter() instanceof ScrollPane scrollCatalogo)
                        || scrollCatalogo.getContent() == null) {
                    throw new IllegalStateException(
                            "El catalogo requiere un ScrollPane con contenido");
                }

                javafx.scene.Node contenidoOriginal =
                        scrollCatalogo.getContent();

                VBox contenidoConCabecera = new VBox(
                        encabezadoCatalogo(),
                        contenidoOriginal
                );

                scrollCatalogo.setContent(contenidoConCabecera);
                panel.setTop(null);

                controladorCatalogo.alActualizarIndicadores(
                        this::refrescarIndicadoresCatalogo);

                vistaCatalogo = panel;
                estiloCatalogoOriginal = btnCatalogo.getStyle();

            } catch (IOException e) {
                throw new IllegalStateException(
                        "No se pudo integrar el catalogo", e);
            }
        }

        dashboardRoot.setCenter(vistaCatalogo);

        btnCatalogo.setStyle(
                "-fx-background-color: #62518C; "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 8; "
                + "-fx-border-color: white; "
                + "-fx-border-radius: 8; "
                + "-fx-border-width: 2;");

        if (categoria != null) {
            controladorCatalogo.abrirDesdeConsulta(
                    categoria, idProducto);
        }

        refrescarIndicadoresCatalogo();
    }

    private VBox encabezadoCatalogo() {

        Label titulo = new Label("Catálogo unificado");
        titulo.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;");

        Label subtitulo = new Label(
                "Productos de la farmacia activa");

        VBox izquierda = new VBox(4, titulo, subtitulo);
        HBox.setHgrow(izquierda, Priority.ALWAYS);

        Button volver = new Button("← Inventario de lotes");

        volver.setStyle(
                "-fx-background-color: #EDE7F7; "
                + "-fx-text-fill: #62518C; "
                + "-fx-font-weight: bold; "
                + "-fx-cursor: hand; "
                + "-fx-padding: 10 14;");

        volver.setOnAction(evento -> volverALotes());

        HBox fila = new HBox(16, izquierda, volver);
        fila.setAlignment(Pos.CENTER_LEFT);

        totalMedicamentosCatalogo = new Label("...");
        vencidosCatalogo = new Label("...");
        agotadosCatalogo = new Label("...");

        tituloTotalCatalogo = new Label();
        tituloIntermedioCatalogo = new Label();
        tituloAgotadosCatalogo = new Label();

        HBox tarjetas = new HBox(12,
                tarjetaCatalogo(
                        tituloTotalCatalogo, totalMedicamentosCatalogo),
                tarjetaCatalogo(
                        tituloIntermedioCatalogo, vencidosCatalogo),
                tarjetaCatalogo(
                        tituloAgotadosCatalogo, agotadosCatalogo));

        VBox cabecera = new VBox(14, fila, tarjetas);
        cabecera.setPadding(new Insets(15, 24, 12, 24));
        cabecera.setStyle(
                "-fx-background-color: #FDFBF7;");

        return cabecera;
    }

    private VBox tarjetaCatalogo(
            Label etiqueta, Label valor) {
        etiqueta.setWrapText(true);

        etiqueta.setStyle(
                "-fx-font-size: 11; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #5B6663;");

        valor.setStyle(
                "-fx-font-size: 25; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #62518C;");

        VBox tarjeta = new VBox(6, etiqueta, valor);
        tarjeta.setPadding(new Insets(12));
        tarjeta.setMaxWidth(Double.MAX_VALUE);

        tarjeta.setStyle(
                "-fx-background-color: white; "
                + "-fx-background-radius: 10; "
                + "-fx-border-color: #E3D9F5; "
                + "-fx-border-radius: 10;");

        HBox.setHgrow(tarjeta, Priority.ALWAYS);

        return tarjeta;
    }

    private void volverALotes() {

        versionIndicadores++;

        dashboardRoot.setCenter(vistaLotes);

        btnCatalogo.setStyle(estiloCatalogoOriginal);

        onRefrescar();
    }

    /**
     * Consulta en segundo plano para no bloquear JavaFX.
     */
    private void refrescarIndicadoresCatalogo() {

        if (vistaCatalogo == null
                || dashboardRoot.getCenter() != vistaCatalogo) {
            return;
        }

        var sesion = SesionUsuario.actual();
        int version = ++versionIndicadores;
        String categoria = controladorCatalogo.categoriaActual();

        if (!"Medicamentos".equals(categoria)) {
            String grupo = "Aseo".equals(categoria)
                    ? "ASEO" : "MATERNIDAD";

            tituloTotalCatalogo.setText("TOTAL DE " + grupo);
            tituloIntermedioCatalogo.setText(
                    grupo + " BAJO MÍNIMO");
            tituloAgotadosCatalogo.setText(
                    grupo + " AGOTADOS");

            try {
                ResumenCategoriaCatalogo resumen =
                        ResumenCategoriaCatalogo.calcular(
                                categoria,
                                controladorCatalogo.registrosDeCategoria());

                totalMedicamentosCatalogo.setText(
                        Integer.toString(resumen.total()));
                vencidosCatalogo.setText(
                        Integer.toString(resumen.bajoMinimo()));
                agotadosCatalogo.setText(
                        Integer.toString(resumen.agotados()));

            } catch (RuntimeException e) {
                LOGGER.log(
                        System.Logger.Level.WARNING,
                        "Error consultando indicadores de categoria",
                        e);

                totalMedicamentosCatalogo.setText("N/D");
                vencidosCatalogo.setText("N/D");
                agotadosCatalogo.setText("N/D");
            }

            return;
        }

        tituloTotalCatalogo.setText("TOTAL DE MEDICAMENTOS");
        tituloIntermedioCatalogo.setText(
                "MEDICAMENTOS CON LOTES VENCIDOS");
        tituloAgotadosCatalogo.setText(
                "MEDICAMENTOS AGOTADOS");

        totalMedicamentosCatalogo.setText("...");
        vencidosCatalogo.setText("...");
        agotadosCatalogo.setText("...");

        Task<IndicadoresCatalogo> tarea = new Task<>() {

            @Override
            protected IndicadoresCatalogo call()
                    throws Exception {

                return IndicadoresCatalogo.calcular(
                        inventario.listarMedicamentos(),
                        inventario.listarLotes(),
                        FechaNegocio.hoy());
            }
        };

        tarea.setOnSucceeded(evento -> {

            if (indicadoresVigentes(version, sesion)) {

                var datos = tarea.getValue();

                totalMedicamentosCatalogo.setText(
                        Integer.toString(datos.total()));

                vencidosCatalogo.setText(
                        Integer.toString(
                                datos.conLotesVencidos()));

                agotadosCatalogo.setText(
                        Integer.toString(datos.agotados()));
            }
        });

        tarea.setOnFailed(evento -> {

            if (indicadoresVigentes(version, sesion)) {

                LOGGER.log(
                        System.Logger.Level.WARNING,
                        "Error consultando indicadores del catalogo",
                        tarea.getException());

                totalMedicamentosCatalogo.setText("N/D");
                vencidosCatalogo.setText("N/D");
                agotadosCatalogo.setText("N/D");
            }
        });

        Thread hilo = new Thread(
                tarea, "catalogo-indicadores");

        hilo.setDaemon(true);
        hilo.start();
    }

    private boolean indicadoresVigentes(
            int version,
            SesionUsuario.DatosUsuario sesion) {

        return version == versionIndicadores
                && vistaCatalogo == dashboardRoot.getCenter()
                && SesionUsuario.estaActiva()
                && SesionUsuario.actual() == sesion;
    }

    @FXML
    private void onCerrarSesion() {
        CareStockApp.getInstance().cerrarSesion();
    }

    @FXML
    private void onRefrescar() {
        cargarDatosTabla();
        cargarContadoresPorCategoria();
    }

    private void cargarResumenAlertas() {
        try {
            var resumen = inventario.resumenAlertas();
            if (lblAlertaStock == null || lblAlertaVencimiento == null || badgeNotificaciones == null || lblContadorNotificaciones == null) {
                return; 
            }

            if (resumen == null) {
                lblAlertaStock.setText("0 productos");
                lblAlertaVencimiento.setText("0 lotes");
                badgeNotificaciones.setVisible(false);
                lblContadorNotificaciones.setVisible(false);
                return;
            }

            int stockBajo = resumen.stockMinimo();
            int proximosVencer = resumen.vencimiento();
            int total = stockBajo + proximosVencer;

            lblAlertaStock.setText(stockBajo + (stockBajo == 1 ? " producto" : " productos"));
            lblAlertaVencimiento.setText(proximosVencer + (proximosVencer == 1 ? " lote" : " lotes"));

            if (total > 0) {
                badgeNotificaciones.setVisible(true);
                lblContadorNotificaciones.setText(String.valueOf(total));
                lblContadorNotificaciones.setVisible(true);
            } else {
                badgeNotificaciones.setVisible(false);
                lblContadorNotificaciones.setVisible(false);
            }

        } catch (Exception e) {
            System.out.println("Error al cargar alertas: " + e.getMessage());
            if (lblAlertaStock != null) lblAlertaStock.setText("0 productos");
            if (lblAlertaVencimiento != null) lblAlertaVencimiento.setText("0 lotes");
            if (badgeNotificaciones != null) badgeNotificaciones.setVisible(false);
            if (lblContadorNotificaciones != null) lblContadorNotificaciones.setVisible(false);
        }
    }

    private void mostrarResumenAlertas(ServicioAlertasInventario.Resumen resumen) {
        btnAlertas.setText(resumen.total() == 0
                ? "Alertas"
                : "Alertas (" + resumen.total() + ")");
        btnAlertas.setStyle(EstiloAlertas.estiloInsignia(
                resumen.criticas(), resumen.advertencias()));
    }

    @FXML
    private void onAbrirAlertas() {
        CareStockApp.getInstance().mostrarAlertas();
    }

    @FXML
    private void onDispensar() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Por favor seleccione un lote directamente de la tabla.");
            return;
        }

        if (!loteSeleccionado.esAptoParaDispensar()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Lote deshabilitado",
                    "No se puede dispensar un lote vencido, retenido o agotado.");
            return;
        }

        String strCantidad = txtCantidad.getText();
        if (strCantidad == null || strCantidad.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo Incompleto", "Por favor ingrese la cantidad a dispensar.");
            return;
        }

        int cantidad;
        try {
            cantidad = ValidadorDispensacion.validarCantidad(
                    strCantidad, loteSeleccionado.getCantidadActual());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Cantidad no válida", e.getMessage());
            return;
        }

        Lote lote = loteSeleccionado;
        int existencias = lote.getCantidadActual();

        if (!confirmarDispensacion(lote.getNumeroLote(), cantidad, existencias)) {
            return;
        }

        try {
            inventario.dispensarMedicamento(lote.getIdLote(), cantidad);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Dispensación Exitosa",
                    ValidadorDispensacion.mensajeExito(lote.getNumeroLote(), cantidad, existencias));

            txtCantidad.clear();
            cargarDatosTabla();

        } catch (ReglaNegocioException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA DE FARMACOVIGILANCIA", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", "Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    private boolean confirmarDispensacion(String numeroLote, int cantidad, int existencias) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("CareStock - Confirmar dispensación");
        confirmacion.setHeaderText("¿Confirmar la dispensación?");
        confirmacion.setContentText(
                ValidadorDispensacion.mensajeConfirmacion(numeroLote, cantidad, existencias));
        return confirmacion.showAndWait()
                .filter(boton -> boton == ButtonType.OK)
                .isPresent();
    }


    private record DatosInicioDashboard(
            List<Lote> lotes,
            Map<Integer, String> categorias,
            Map<Integer, String> categoriasPorMedicamento,
            Map<String, Integer> conteos,
            ServicioAlertasInventario.Resumen alertas) { }

    private void cargarDatosInicialesAsync() {
        var sesionInicial = SesionUsuario.actual();
        var fachada = inventario;
        cargaInicialEnCurso = true;

        lblTotalLotes.setText("Cargando...");
        lblLoteSeleccionadoInfo.setText("Cargando inventario...");

        Task<DatosInicioDashboard> tarea = new Task<>() {
            @Override
            protected DatosInicioDashboard call() throws Exception {
                Map<Integer, String> categorias =
                        fachada.listarCategoriasMedicamento();

                List<Lote> lotes = fachada.listarLotes();

                Map<Integer, String> porMedicamento =
                        fachada.categoriaPorMedicamento();

                Map<String, Integer> conteos =
                        fachada.contarPorCategoria();

                ServicioAlertasInventario.Resumen resumen = null;

                try {
                    resumen = fachada.resumenAlertas();
                } catch (Exception ex) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "No fue posible consultar las alertas", ex);
                }

                return new DatosInicioDashboard(
                        lotes, categorias, porMedicamento,
                        conteos, resumen);
            }
        };

        tarea.setOnSucceeded(evento -> {
            cargaInicialEnCurso = false;

            if (!SesionUsuario.estaActiva()
                    || SesionUsuario.actual() != sesionInicial) {
                return;
            }

            var datos = tarea.getValue();

            cmbCategoria.getItems().addAll(
                    datos.categorias().values());

            categoriaPorMedicamento =
                    datos.categoriasPorMedicamento();

            tablaLotes.getSelectionModel().clearSelection();
            listaLotes.setAll(datos.lotes());
            aplicarFiltros();

            lblTotalLotes.setText(
                    String.valueOf(datos.lotes().size()));

            long vencidos = datos.lotes().stream()
                    .filter(l -> l.getFechaVencimiento() != null
                            && !l.getFechaVencimiento()
                                    .isAfter(FechaNegocio.hoy()))
                    .count();

            lblLotesVencidos.setText(Long.toString(vencidos));

            lblTotalMedicamentos.setText(String.valueOf(
                    datos.conteos().getOrDefault("Medicamento", 0)));
            lblTotalAseo.setText(String.valueOf(
                    datos.conteos().getOrDefault("Aseo", 0)));
            lblTotalMaternidad.setText(String.valueOf(
                    datos.conteos().getOrDefault("Maternidad", 0)));

            if (datos.alertas() != null) {
                var resumen = datos.alertas();

                if (lblAlertaStock != null) {
                    lblAlertaStock.setText(
                            resumen.stockMinimo() + " productos");
                }
                if (lblAlertaVencimiento != null) {
                    lblAlertaVencimiento.setText(
                            resumen.vencimiento() + " lotes");
                }
                if (badgeNotificaciones != null) {
                    badgeNotificaciones.setVisible(
                            resumen.total() > 0);
                }
                if (lblContadorNotificaciones != null) {
                    lblContadorNotificaciones.setText(
                            String.valueOf(resumen.total()));
                    lblContadorNotificaciones.setVisible(
                            resumen.total() > 0);
                }
                if (btnAlertas != null) {
                    mostrarResumenAlertas(resumen);
                }
            } else {
                if (lblAlertaStock != null) {
                    lblAlertaStock.setText("No disponible");
                }
                if (lblAlertaVencimiento != null) {
                    lblAlertaVencimiento.setText("No disponible");
                }
                if (badgeNotificaciones != null) {
                    badgeNotificaciones.setVisible(false);
                }
                if (lblContadorNotificaciones != null) {
                    lblContadorNotificaciones.setVisible(false);
                }
            }

            lblLoteSeleccionadoInfo.setText(
                    "Ninguno (seleccione un lote)");

            Destino pendiente = destinoLotePendiente;
            destinoLotePendiente = null;

            if (pendiente != null) {
                abrirDesdeAlerta(pendiente);
            }
        });

        tarea.setOnFailed(evento -> {
            cargaInicialEnCurso = false;
            destinoLotePendiente = null;

            if (!SesionUsuario.estaActiva()
                    || SesionUsuario.actual() != sesionInicial) {
                return;
            }

            LOGGER.log(System.Logger.Level.ERROR,
                    "Error al cargar el inventario inicial",
                    tarea.getException());

            lblTotalLotes.setText("No disponible");
            lblLoteSeleccionadoInfo.setText(
                    "Error al cargar inventario. "
                    + "Use Refrescar para reintentar.");
        });

        Thread hilo = new Thread(
                tarea, "carestock-dashboard-carga");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void cargarContadoresPorCategoria() {
    try {
        Map<String, Integer> conteo = inventario.contarPorCategoria();
        lblTotalMedicamentos.setText(String.valueOf(conteo.getOrDefault("Medicamento", 0)));
        lblTotalAseo.setText(String.valueOf(conteo.getOrDefault("Aseo", 0)));
        lblTotalMaternidad.setText(String.valueOf(conteo.getOrDefault("Maternidad", 0)));
    } catch (Exception e) {
        LOGGER.log(
                System.Logger.Level.WARNING,
                "No fue posible cargar los contadores del inventario.",
                e
        );
    }
}

    private void cargarDatosTabla() {
        try {
            List<Lote> lotes = inventario.listarLotes();
            tablaLotes.getSelectionModel().clearSelection();
            loteSeleccionado = null;
            listaLotes.setAll(lotes);
            try {
                categoriaPorMedicamento = inventario.categoriaPorMedicamento();
            } catch (Exception ex) {
                categoriaPorMedicamento = Map.of();
                System.err.println("No fue posible consultar las categorias: " + ex.getMessage());
            }
            aplicarFiltros();

            // Actualizar contadores KPI
            lblTotalLotes.setText(String.valueOf(lotes.size()));
            long vencidos = lotes.stream()
                    .filter(l -> l.getFechaVencimiento() != null && !l.getFechaVencimiento().isAfter(FechaNegocio.hoy()))
                    .count();
            lblLotesVencidos.setText(String.valueOf(vencidos));
            cargarResumenAlertas();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudieron obtener los datos de Neon DB: " + e.getMessage());
        }
    }

    private void cargarCategorias() {
        try {
            cmbCategoria.getItems().addAll(inventario.listarCategoriasMedicamento().values());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudieron obtener las categorías: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("CareStock - " + titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
               @FXML
    private void onExportarCSV() {
        if (tablaLotes.getItems() == null || tablaLotes.getItems().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin datos para exportar", 
                    "No hay lotes visibles en la tabla. Si aplicaste filtros, intenta limpiarlos antes de exportar.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar Historial de Lotes");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Archivos CSV (*.csv)", "*.csv")
        );
        
        java.io.File archivo = fileChooser.showSaveDialog(tablaLotes.getScene().getWindow());

        if (archivo != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(archivo, "UTF-8")) {
                
                writer.println("ID Lote;Numero Lote;ID Medicamento;Cantidad;Fecha Vencimiento;Estado");

                for (Lote lote : tablaLotes.getItems()) {
                    writer.println(
                            lote.getIdLote() + ";" +
                            lote.getNumeroLote() + ";" +
                            lote.getIdMedicamento() + ";" +
                            lote.getCantidadActual() + ";" +
                            lote.getFechaVencimiento() + ";" +
                            lote.getEstadoLote()
                    );
                }

                mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación Exitosa", 
                        "El archivo CSV se guardó correctamente con " + tablaLotes.getItems().size() + " registros.\n\nRuta:\n" + archivo.getAbsolutePath());

            } catch (java.io.IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Exportación", 
                        "No se pudo guardar el archivo: " + e.getMessage());
            }
        }
    }
    @FXML
    private void onAbrirNotificaciones() {
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        
        try {
            var alertas = inventario.listarAlertas();

            if (alertas.isEmpty()) {
                javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem("No hay notificaciones nuevas");
                item.setDisable(true);
                contextMenu.getItems().add(item);
            } else {
                for (var alerta : alertas) {
                    String texto = (alerta.tipoAlerta() == org.example.carestock.dto.AlertaInventario.TipoAlerta.STOCK_MINIMO) 
                            ? "⚠️ Stock bajo: " + alerta.producto()
                            : "⏳ Vence pronto: " + alerta.producto();

                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    hbox.setStyle("-fx-padding: 5;");
                    
                    javafx.scene.control.Label label = new javafx.scene.control.Label(texto);
                    label.setStyle("-fx-font-size: 12px;");
                    
                    javafx.scene.control.Button btnLeida = new javafx.scene.control.Button("X");
                    btnLeida.setStyle("-fx-background-color: #F0A8A8; -fx-text-fill: #7A2E2E; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 2 6;");
                    
                    btnLeida.setOnAction(e -> {
                        try {
                            inventario.marcarAlertaComoLeida(alerta.tipoAlerta().name(), alerta.idReferencia());
                            
                            cargarResumenAlertas();
                            contextMenu.hide();
                            
                        } catch (Exception ex) {
                            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo marcar la alerta: " + ex.getMessage());
                        }
                    });

                    hbox.getChildren().addAll(label, btnLeida);
                    
                    javafx.scene.control.CustomMenuItem menuItem = new javafx.scene.control.CustomMenuItem(hbox);
                    contextMenu.getItems().add(menuItem);
                }
            }
        } catch (Exception e) {
            javafx.scene.control.MenuItem itemError = new javafx.scene.control.MenuItem("Error al cargar notificaciones");
            itemError.setDisable(true);
            contextMenu.getItems().add(itemError);
            LOGGER.log(System.Logger.Level.ERROR, "No fue posible cargar las notificaciones.", e);
        }
        contextMenu.show(btnCampana, javafx.geometry.Side.BOTTOM, 0, 0);
    }
        @FXML
    private void onGenerarQR() {
        if (loteSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Selecciona un lote de la tabla para generar su código QR.");
            return;
        }

        try {
            String textoQR = "Lote: " + loteSeleccionado.getNumeroLote() + 
                             "\nMedicamento ID: " + loteSeleccionado.getIdMedicamento() + 
                             "\nVence: " + loteSeleccionado.getFechaVencimiento();

            javafx.scene.image.Image imagenQR = org.example.carestock.utils.GeneradorQR.generar(textoQR, 250);
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Código QR del Lote");
            dialog.setHeaderText("QR generado para el lote: " + loteSeleccionado.getNumeroLote());
            javafx.scene.control.ButtonType btnCerrar = new javafx.scene.control.ButtonType("Cerrar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(btnCerrar);
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
            vbox.setPadding(new javafx.geometry.Insets(20));
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(imagenQR);
            javafx.scene.control.Label lblInfo = new javafx.scene.control.Label(textoQR);
            lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #20302F;");
            vbox.getChildren().addAll(imageView, lblInfo);
            dialog.getDialogPane().setContent(vbox);
            dialog.showAndWait();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo generar el código QR: " + e.getMessage());
        }
    }
}
