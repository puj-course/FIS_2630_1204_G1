package com.carestock.view;

import com.carestock.dao.FarmaciaDAO;
import com.carestock.model.Farmacia;
import com.carestock.model.LoteGestion;
import com.carestock.model.MedicamentoGestion;
import com.carestock.model.Ubicacion;
import com.carestock.service.InventarioCrudService;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;
import com.carestock.util.UserMessageResolver;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.collections.FXCollections;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;


public class GestionInventarioFX {

    private final InventarioCrudService service =
            new InventarioCrudService();

    private final FarmaciaDAO farmaciaDAO =
            new FarmaciaDAO();


    private final TableView<MedicamentoGestion>
            tablaMedicamentos =
            new TableView<>();

    private final TableView<LoteGestion>
            tablaLotes =
            new TableView<>();


    private final Label lblFarmacia =
            new Label();


    private Stage stage;

    private UserSession.CurrentUser actor;

    private Integer idFarmaciaActual;


    public void mostrar(
            Window owner
    ) {

        actor =
                UserSession
                        .getInstance()
                        .getCurrentUser();


        if (
                actor == null
                || (
                    !actor.esSuperAdmin()
                    && !actor.esAdministrador()
                )
        ) {

            AlertUtil.mostrarError(
                    "Solo SUPER_ADMIN o ADMINISTRADOR "
                    + "pueden gestionar inventario."
            );

            return;
        }


        stage =
                new Stage();

        stage.setTitle(
                "CareStock - Gestión de inventario"
        );

        stage.initModality(
                Modality.WINDOW_MODAL
        );


        if (owner != null) {

            stage.initOwner(
                    owner
            );
        }


        VBox root =
                new VBox(14);

        root.setPadding(
                new Insets(20)
        );


        Label titulo =
                new Label(
                        "Gestión de medicamentos y lotes"
                );

        titulo.setStyle(
                "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
        );


        root.getChildren()
                .addAll(
                        titulo,
                        construirSelectorFarmacia(),
                        construirTabs()
                );


        Scene scene =
                new Scene(
                        root,
                        1120,
                        690
                );


        stage.setScene(
                scene
        );

        stage.setResizable(
                true
        );


        inicializarFarmacia();


        stage.showAndWait();
    }


    private HBox construirSelectorFarmacia() {

        HBox fila =
                new HBox(10);

        fila.setAlignment(
                Pos.CENTER_LEFT
        );

        Label etiqueta =
                new Label(
                        "Farmacia activa:"
                );

        etiqueta.setStyle(
                "-fx-font-weight: bold;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label contexto =
                new Label(
                        "Sesión: "
                        + actor.getNombre()
                        + " | "
                        + actor.getRol()
                );

        contexto.setStyle(
                "-fx-text-fill: #607D8B;"
        );

        fila.getChildren()
                .addAll(
                        etiqueta,
                        lblFarmacia,
                        spacer,
                        contexto
                );

        return fila;
    }


    private TabPane construirTabs() {

        configurarTablaMedicamentos();

        configurarTablaLotes();


        Tab medicamentos =
                new Tab(
                        "Medicamentos",
                        construirPanelMedicamentos()
                );

        medicamentos.setClosable(
                false
        );


        Tab lotes =
                new Tab(
                        "Lotes",
                        construirPanelLotes()
                );

        lotes.setClosable(
                false
        );


        TabPane tabs =
                new TabPane(
                        medicamentos,
                        lotes
                );


        VBox.setVgrow(
                tabs,
                Priority.ALWAYS
        );


        return tabs;
    }


    private VBox construirPanelMedicamentos() {

        Button nuevo =
                new Button(
                        "+ Nuevo medicamento"
                );

        Button editar =
                new Button(
                        "Editar"
                );

        Button estado =
                new Button(
                        "Activar / Desactivar"
                );

        Button recargar =
                new Button(
                        "Recargar"
                );


        nuevo.setOnAction(
                e -> crearMedicamento()
        );

        editar.setOnAction(
                e -> editarMedicamento()
        );

        estado.setOnAction(
                e -> alternarEstadoMedicamento()
        );

        recargar.setOnAction(
                e -> recargarMedicamentos()
        );


        HBox acciones =
                new HBox(
                        10,
                        nuevo,
                        editar,
                        estado,
                        recargar
                );

        acciones.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox box =
                new VBox(
                        10,
                        acciones,
                        tablaMedicamentos
                );

        box.setPadding(
                new Insets(12)
        );


        VBox.setVgrow(
                tablaMedicamentos,
                Priority.ALWAYS
        );


        return box;
    }


    private VBox construirPanelLotes() {

        Button nuevo =
                new Button(
                        "+ Nuevo lote"
                );

        Button editar =
                new Button(
                        "Editar lote"
                );

        Button estado =
                new Button(
                        "Retener / Reactivar"
                );

        Button recargar =
                new Button(
                        "Recargar"
                );


        nuevo.setOnAction(
                e -> crearLote()
        );

        editar.setOnAction(
                e -> editarLote()
        );

        estado.setOnAction(
                e -> alternarEstadoLote()
        );

        recargar.setOnAction(
                e -> recargarLotes()
        );


        HBox acciones =
                new HBox(
                        10,
                        nuevo,
                        editar,
                        estado,
                        recargar
                );

        acciones.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox box =
                new VBox(
                        10,
                        acciones,
                        tablaLotes
                );

        box.setPadding(
                new Insets(12)
        );


        VBox.setVgrow(
                tablaLotes,
                Priority.ALWAYS
        );


        return box;
    }


    private void configurarTablaMedicamentos() {

        TableColumn<MedicamentoGestion, Number> id =
                new TableColumn<>(
                        "ID"
                );

        id.setCellValueFactory(
                c ->
                        new ReadOnlyLongWrapper(
                                c.getValue()
                                        .getIdMedicamento()
                        )
        );


        TableColumn<MedicamentoGestion, String> invima =
                new TableColumn<>(
                        "INVIMA"
                );

        invima.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getCodigoInvima()
                        )
        );


        TableColumn<MedicamentoGestion, String> nombre =
                new TableColumn<>(
                        "Medicamento"
                );

        nombre.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getNombreComercial()
                        )
        );


        TableColumn<MedicamentoGestion, String> principio =
                new TableColumn<>(
                        "Principio activo"
                );

        principio.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getPrincipioActivo()
                        )
        );


        TableColumn<MedicamentoGestion, String> categoria =
                new TableColumn<>(
                        "Categoría"
                );

        categoria.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getCategoria()
                        )
        );


        TableColumn<MedicamentoGestion, Number> stock =
                new TableColumn<>(
                        "Stock"
                );

        stock.setCellValueFactory(
                c ->
                        new ReadOnlyIntegerWrapper(
                                c.getValue()
                                        .getStockTotal()
                        )
        );


        TableColumn<MedicamentoGestion, Number> minimo =
                new TableColumn<>(
                        "Mínimo"
                );

        minimo.setCellValueFactory(
                c ->
                        new ReadOnlyIntegerWrapper(
                                c.getValue()
                                        .getStockMinimo()
                        )
        );


        TableColumn<MedicamentoGestion, String> estado =
                new TableColumn<>(
                        "Estado"
                );

        estado.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getEstado()
                        )
        );


        tablaMedicamentos
                .getColumns()
                .setAll(
                        id,
                        invima,
                        nombre,
                        principio,
                        categoria,
                        stock,
                        minimo,
                        estado
                );


        tablaMedicamentos
                .setColumnResizePolicy(
                        TableView
                                .CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
                );


        tablaMedicamentos.setPlaceholder(
                new Label(
                        "Inventario vacío para esta sede"
                )
        );
    }


    private void configurarTablaLotes() {

        TableColumn<LoteGestion, Number> id =
                new TableColumn<>(
                        "ID"
                );

        id.setCellValueFactory(
                c ->
                        new ReadOnlyIntegerWrapper(
                                c.getValue()
                                        .getIdLote()
                        )
        );


        TableColumn<LoteGestion, String> lote =
                new TableColumn<>(
                        "Lote"
                );

        lote.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getNumeroLote()
                        )
        );


        TableColumn<LoteGestion, String> medicamento =
                new TableColumn<>(
                        "Medicamento"
                );

        medicamento.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getMedicamento()
                        )
        );


        TableColumn<LoteGestion, Number> cantidad =
                new TableColumn<>(
                        "Cantidad"
                );

        cantidad.setCellValueFactory(
                c ->
                        new ReadOnlyIntegerWrapper(
                                c.getValue()
                                        .getCantidadActual()
                        )
        );


        TableColumn<LoteGestion, LocalDate> vence =
                new TableColumn<>(
                        "Vence"
                );

        vence.setCellValueFactory(
                c ->
                        new ReadOnlyObjectWrapper<>(
                                c.getValue()
                                        .getFechaVencimiento()
                        )
        );


        TableColumn<LoteGestion, String> ubicacion =
                new TableColumn<>(
                        "Ubicación"
                );

        ubicacion.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getUbicacion()
                        )
        );


        TableColumn<LoteGestion, String> estado =
                new TableColumn<>(
                        "Estado"
                );

        estado.setCellValueFactory(
                c ->
                        new ReadOnlyStringWrapper(
                                c.getValue()
                                        .getEstadoLote()
                        )
        );


        tablaLotes
                .getColumns()
                .setAll(
                        id,
                        lote,
                        medicamento,
                        cantidad,
                        vence,
                        ubicacion,
                        estado
                );


        tablaLotes
                .setColumnResizePolicy(
                        TableView
                                .CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
                );


        tablaLotes.setPlaceholder(
                new Label(
                        "No hay lotes registrados para esta sede"
                )
        );
    }


    private void inicializarFarmacia() {

        try {

            idFarmaciaActual =
                    new SessionContext()
                            .requireAuthenticatedPharmacyId();

            Farmacia farmacia =
                    farmaciaDAO
                            .buscarPorId(
                                    idFarmaciaActual
                            );

            lblFarmacia.setText(
                    farmacia == null
                            ? "Farmacia ID "
                              + idFarmaciaActual
                            : farmacia.getNombre()
            );

            recargarTodo();

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar la farmacia activa "
                    + "de la sesión. "
                    + mensaje(e)
            );
        }
    }


    private void recargarTodo() {

        recargarMedicamentos();

        recargarLotes();
    }


    private void recargarMedicamentos() {

        if (idFarmaciaActual == null) {
            return;
        }


        try {

            tablaMedicamentos.setItems(
                    FXCollections
                            .observableArrayList(
                                    service
                                            .listarMedicamentos(
                                                    idFarmaciaActual,
                                                    true
                                            )
                            )
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar los medicamentos. "
                    + mensaje(e)
            );
        }
    }


    private void recargarLotes() {

        if (idFarmaciaActual == null) {
            return;
        }


        try {

            tablaLotes.setItems(
                    FXCollections
                            .observableArrayList(
                                    service
                                            .listarLotes(
                                                    idFarmaciaActual
                                            )
                            )
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    "No fue posible cargar los lotes. "
                    + mensaje(e)
            );
        }
    }


    private void crearMedicamento() {

        try {

            Optional<MedicamentoGestion> resultado =
                    mostrarDialogoMedicamento(
                            null
                    );


            if (resultado.isEmpty()) {
                return;
            }


            service.crearMedicamento(
                    idFarmaciaActual,
                    resultado.get()
            );


            recargarTodo();


            AlertUtil.mostrarExito(
                    "Medicamento creado correctamente."
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    mensaje(e)
            );
        }
    }


    private void editarMedicamento() {

        MedicamentoGestion seleccionado =
                tablaMedicamentos
                        .getSelectionModel()
                        .getSelectedItem();


        if (seleccionado == null) {

            AlertUtil.mostrarAdvertencia(
                    "Seleccione un medicamento."
            );

            return;
        }


        try {

            Optional<MedicamentoGestion> resultado =
                    mostrarDialogoMedicamento(
                            seleccionado
                    );


            if (resultado.isEmpty()) {
                return;
            }


            service.actualizarMedicamento(
                    idFarmaciaActual,
                    resultado.get()
            );


            recargarTodo();


            AlertUtil.mostrarExito(
                    "Medicamento actualizado correctamente."
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    mensaje(e)
            );
        }
    }


    private void alternarEstadoMedicamento() {

        MedicamentoGestion seleccionado =
                tablaMedicamentos
                        .getSelectionModel()
                        .getSelectedItem();


        if (seleccionado == null) {

            AlertUtil.mostrarAdvertencia(
                    "Seleccione un medicamento."
            );

            return;
        }


        String nuevoEstado =
                seleccionado.estaActivo()
                        ? "INACTIVO"
                        : "ACTIVO";


        try {

            service.cambiarEstadoMedicamento(
                    idFarmaciaActual,
                    seleccionado
                            .getIdMedicamento(),
                    nuevoEstado
            );


            recargarTodo();


            AlertUtil.mostrarExito(
                    "Estado del medicamento actualizado a "
                    + nuevoEstado
                    + "."
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    mensaje(e)
            );
        }
    }


    private void crearLote() {

        try {

            List<MedicamentoGestion> medicamentos =
                    service.listarMedicamentos(
                            idFarmaciaActual,
                            false
                    );


            if (medicamentos.isEmpty()) {

                AlertUtil.mostrarAdvertencia(
                        "La farmacia no tiene medicamentos activos."
                );

                return;
            }


            Optional<LoteFormData> datos =
                    mostrarDialogoLote(
                            null,
                            medicamentos
                    );


            if (datos.isEmpty()) {
                return;
            }


            LoteFormData d =
                    datos.get();


            service.crearLote(
                    idFarmaciaActual,

                    Math.toIntExact(
                            d.medicamento()
                                    .getIdMedicamento()
                    ),

                    d.numeroLote(),

                    d.cantidad(),

                    d.fechaVencimiento(),

                    d.ubicacion()
                            .getIdUbicacion()
            );


            recargarTodo();


            AlertUtil.mostrarExito(
                    "Lote creado correctamente."
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    mensaje(e)
            );
        }
    }


    private void editarLote() {

        LoteGestion seleccionado =
                tablaLotes
                        .getSelectionModel()
                        .getSelectedItem();


        if (seleccionado == null) {

            AlertUtil.mostrarAdvertencia(
                    "Seleccione un lote."
            );

            return;
        }


        try {

            List<MedicamentoGestion> medicamentos =
                    service.listarMedicamentos(
                            idFarmaciaActual,
                            true
                    );


            Optional<LoteFormData> datos =
                    mostrarDialogoLote(
                            seleccionado,
                            medicamentos
                    );


            if (datos.isEmpty()) {
                return;
            }


            LoteFormData d =
                    datos.get();


            service.actualizarLote(
                    idFarmaciaActual,

                    seleccionado
                            .getIdLote(),

                    d.numeroLote(),

                    d.cantidad(),

                    d.fechaVencimiento(),

                    d.ubicacion()
                            .getIdUbicacion()
            );


            recargarTodo();


            AlertUtil.mostrarExito(
                    "Lote actualizado correctamente."
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    mensaje(e)
            );
        }
    }


    private void alternarEstadoLote() {

        LoteGestion seleccionado =
                tablaLotes
                        .getSelectionModel()
                        .getSelectedItem();


        if (seleccionado == null) {

            AlertUtil.mostrarAdvertencia(
                    "Seleccione un lote."
            );

            return;
        }


        String nuevoEstado =
                seleccionado.estaRetenido()
                        ? "DISPONIBLE"
                        : "RETENIDO";


        try {

            service.cambiarEstadoLote(
                    idFarmaciaActual,

                    seleccionado
                            .getIdLote(),

                    nuevoEstado
            );


            recargarTodo();


            AlertUtil.mostrarExito(
                    "Estado del lote actualizado a "
                    + nuevoEstado
                    + "."
            );

        } catch (Exception e) {

            AlertUtil.mostrarError(
                    mensaje(e)
            );
        }
    }


    private Optional<MedicamentoGestion>
            mostrarDialogoMedicamento(
                    MedicamentoGestion actual
            ) throws SQLException {

        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                actual == null
                        ? "Nuevo medicamento"
                        : "Editar medicamento"
        );

        dialog.initOwner(
                stage
        );


        TextField invima =
                new TextField(
                        actual == null
                                ? ""
                                : actual.getCodigoInvima()
                );

        TextField nombre =
                new TextField(
                        actual == null
                                ? ""
                                : actual.getNombreComercial()
                );

        TextField principio =
                new TextField(
                        actual == null
                                ? ""
                                : actual.getPrincipioActivo()
                );

        TextField concentracion =
                new TextField(
                        actual == null
                                ? ""
                                : nvl(
                                    actual.getConcentracion()
                                )
                );

        TextField forma =
                new TextField(
                        actual == null
                                ? ""
                                : nvl(
                                    actual.getFormaFarmaceutica()
                                )
                );

        TextField presentacion =
                new TextField(
                        actual == null
                                ? ""
                                : nvl(
                                    actual.getPresentacion()
                                )
                );


        ComboBox<String> categoria =
                new ComboBox<>(
                        FXCollections
                                .observableArrayList(
                                        service
                                                .listarCategorias()
                                )
                );


        if (actual != null) {

            categoria.setValue(
                    actual.getCategoria()
            );
        }


        TextField stockMinimo =
                new TextField(
                        actual == null
                                ? "0"
                                : String.valueOf(
                                    actual.getStockMinimo()
                                )
                );


        GridPane grid =
                new GridPane();

        grid.setHgap(10);

        grid.setVgap(10);

        grid.setPadding(
                new Insets(15)
        );


        grid.addRow(
                0,
                new Label(
                        "Código INVIMA:"
                ),
                invima
        );

        grid.addRow(
                1,
                new Label(
                        "Nombre comercial:"
                ),
                nombre
        );

        grid.addRow(
                2,
                new Label(
                        "Principio activo:"
                ),
                principio
        );

        grid.addRow(
                3,
                new Label(
                        "Concentración:"
                ),
                concentracion
        );

        grid.addRow(
                4,
                new Label(
                        "Forma farmacéutica:"
                ),
                forma
        );

        grid.addRow(
                5,
                new Label(
                        "Presentación:"
                ),
                presentacion
        );

        grid.addRow(
                6,
                new Label(
                        "Categoría:"
                ),
                categoria
        );

        grid.addRow(
                7,
                new Label(
                        "Stock mínimo:"
                ),
                stockMinimo
        );


        ButtonType guardar =
                new ButtonType(
                        "Guardar",
                        ButtonBar
                                .ButtonData
                                .OK_DONE
                );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        guardar,
                        ButtonType.CANCEL
                );


        dialog.getDialogPane()
                .setContent(
                        grid
                );


        Optional<ButtonType> respuesta =
                dialog.showAndWait();


        if (
                respuesta.isEmpty()
                || respuesta.get()
                   != guardar
        ) {

            return Optional.empty();
        }


        int minimo;


        try {

            minimo =
                    Integer.parseInt(
                            stockMinimo
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "El stock mínimo debe ser un entero válido."
            );
        }


        MedicamentoGestion resultado =
                new MedicamentoGestion(
                        actual == null
                                ? 0
                                : actual.getIdMedicamento(),

                        idFarmaciaActual,

                        invima
                                .getText()
                                .trim(),

                        nombre
                                .getText()
                                .trim(),

                        principio
                                .getText()
                                .trim(),

                        concentracion
                                .getText()
                                .trim(),

                        forma
                                .getText()
                                .trim(),

                        presentacion
                                .getText()
                                .trim(),

                        categoria.getValue(),

                        actual == null
                                ? 0
                                : actual.getStockTotal(),

                        minimo,

                        actual == null
                                ? "ACTIVO"
                                : actual.getEstado()
                );


        return Optional.of(
                resultado
        );
    }


    private Optional<LoteFormData>
            mostrarDialogoLote(
                    LoteGestion actual,
                    List<MedicamentoGestion> medicamentos
            ) throws SQLException {

        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                actual == null
                        ? "Nuevo lote"
                        : "Editar lote"
        );

        dialog.initOwner(
                stage
        );


        ComboBox<MedicamentoGestion> medicamento =
                new ComboBox<>(
                        FXCollections
                                .observableArrayList(
                                        medicamentos
                                )
                );

        medicamento.setPrefWidth(
                320
        );


        if (actual != null) {

            medicamentos
                    .stream()
                    .filter(
                            m ->
                                    m.getIdMedicamento()
                                    == actual.getIdMedicamento()
                    )
                    .findFirst()
                    .ifPresent(
                            medicamento::setValue
                    );


            /*
             * Después de crear un lote no se permite
             * moverlo a otro medicamento.
             */
            medicamento.setDisable(
                    true
            );
        }


        TextField numero =
                new TextField(
                        actual == null
                                ? ""
                                : actual.getNumeroLote()
                );


        TextField cantidad =
                new TextField(
                        actual == null
                                ? ""
                                : String.valueOf(
                                    actual.getCantidadActual()
                                )
                );


        DatePicker vencimiento =
                new DatePicker(
                        actual == null
                                ? LocalDate
                                    .now()
                                    .plusMonths(6)
                                : actual
                                    .getFechaVencimiento()
                );


        List<Ubicacion> ubicaciones =
                service
                        .listarUbicaciones();


        ComboBox<Ubicacion> ubicacion =
                new ComboBox<>(
                        FXCollections
                                .observableArrayList(
                                        ubicaciones
                                )
                );


        ubicaciones
                .stream()
                .filter(
                        u ->
                                actual != null
                                && u.getIdUbicacion()
                                   == actual
                                        .getIdUbicacion()
                )
                .findFirst()
                .ifPresent(
                        ubicacion::setValue
                );


        GridPane grid =
                new GridPane();

        grid.setHgap(10);

        grid.setVgap(10);

        grid.setPadding(
                new Insets(15)
        );


        grid.addRow(
                0,
                new Label(
                        "Medicamento:"
                ),
                medicamento
        );

        grid.addRow(
                1,
                new Label(
                        "Número de lote:"
                ),
                numero
        );

        grid.addRow(
                2,
                new Label(
                        "Cantidad:"
                ),
                cantidad
        );

        grid.addRow(
                3,
                new Label(
                        "Fecha vencimiento:"
                ),
                vencimiento
        );

        grid.addRow(
                4,
                new Label(
                        "Ubicación:"
                ),
                ubicacion
        );


        ButtonType guardar =
                new ButtonType(
                        "Guardar",
                        ButtonBar
                                .ButtonData
                                .OK_DONE
                );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        guardar,
                        ButtonType.CANCEL
                );


        dialog.getDialogPane()
                .setContent(
                        grid
                );


        Optional<ButtonType> respuesta =
                dialog.showAndWait();


        if (
                respuesta.isEmpty()
                || respuesta.get()
                   != guardar
        ) {

            return Optional.empty();
        }


        if (
                medicamento.getValue()
                == null
        ) {

            throw new IllegalArgumentException(
                    "Seleccione un medicamento."
            );
        }


        if (
                ubicacion.getValue()
                == null
        ) {

            throw new IllegalArgumentException(
                    "Seleccione una ubicación."
            );
        }


        int cantidadValor;


        try {

            cantidadValor =
                    Integer.parseInt(
                            cantidad
                                    .getText()
                                    .trim()
                    );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser un entero válido."
            );
        }


        return Optional.of(
                new LoteFormData(
                        medicamento.getValue(),

                        numero
                                .getText()
                                .trim(),

                        cantidadValor,

                        vencimiento
                                .getValue(),

                        ubicacion
                                .getValue()
                )
        );
    }


    private String nvl(
            String valor
    ) {

        return valor == null
                ? ""
                : valor;
    }


    private String mensaje(
            Exception e
    ) {

        return UserMessageResolver
                .resolve(e);
    }


    private record LoteFormData(
            MedicamentoGestion medicamento,
            String numeroLote,
            int cantidad,
            LocalDate fechaVencimiento,
            Ubicacion ubicacion
    ) {
    }
}
