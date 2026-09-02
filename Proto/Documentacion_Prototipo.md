==============================================================================
GUIA COMPLETA DE DESARROLLO Y DESPLIEGUE - CARESTOCK JAVAFX & NEON DB
===============================================================================

Este archivo contiene la documentacion integral, especificaciones tecnicas,
comandos de ejecucion, scripts de base de datos y el codigo fuente del prototipo.

-------------------------------------------------------------------------------
1. ESTRUCTURA DEL PROYECTO
-------------------------------------------------------------------------------

FIS_2630_1204_G1/
├── Proto/
│   ├── bin/                          # Clases compiladas (.class)
│   └── src/
│       └── com/
│           └── carestock/
│               ├── config/
│               │   └── DatabaseConfig.java
│               ├── dao/
│               │   └── MedicamentoDAO.java
│               ├── model/
│               │   └── Medicamento.java
│               └── view/
│                   ├── FormularioMedicamentoDialog.java
│                   └── MainDashboardFX.java
├── postgresql-driver.jar             # Driver JDBC de PostgreSQL
└── DOCUMENTACION_CARESTOCK.txt

-------------------------------------------------------------------------------
2. CONFIGURACION DE BASE DE DATOS (NEON DB)
-------------------------------------------------------------------------------

-- Tabla CATEGORIAS
CREATE TABLE IF NOT EXISTS CATEGORIAS (
    id_categoria SERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla MEDICAMENTOS
CREATE TABLE IF NOT EXISTS MEDICAMENTOS (
    id_medicamento SERIAL PRIMARY KEY,
    codigo_invima VARCHAR(50) NOT NULL UNIQUE,
    nombre_comercial VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150) NOT NULL,
    concentracion VARCHAR(50),
    id_categoria INT REFERENCES CATEGORIAS(id_categoria),
    stock_total INT DEFAULT 0,
    stock_minimo INT DEFAULT 0
);

-- Insercion de categoria por defecto
INSERT INTO CATEGORIAS (nombre_categoria) 
VALUES ('General') 
ON CONFLICT (nombre_categoria) DO NOTHING;

-------------------------------------------------------------------------------
3. CODIGO FUENTE DEL SISTEMA
-------------------------------------------------------------------------------

A. Modelo: Medicamento.java
Ubicacion: Proto/src/com/carestock/model/Medicamento.java

package com.carestock.model;

public class Medicamento {
    private Long idMedicamento;
    private String codigoInvima;
    private String nombreComercial;
    private String principioActivo;
    private String concentracion;
    private String categoria;
    private Integer stockTotal;
    private Integer stockMinimo;

    public Medicamento() {}

    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial, 
                       String principioActivo, String concentracion, String categoria, 
                       Integer stockTotal, Integer stockMinimo) {
        this.idMedicamento = idMedicamento;
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.concentracion = concentracion;
        this.categoria = categoria;
        this.stockTotal = stockTotal;
        this.stockMinimo = stockMinimo;
    }

    public Long getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(Long idMedicamento) { this.idMedicamento = idMedicamento; }

    public String getCodigoInvima() { return codigoInvima; }
    public void setCodigoInvima(String codigoInvima) { this.codigoInvima = codigoInvima; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }

    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getStockTotal() { return stockTotal; }
    public void setStockTotal(Integer stockTotal) { this.stockTotal = stockTotal; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
}


B. Configuracion JDBC: DatabaseConfig.java
Ubicacion: Proto/src/com/carestock/config/DatabaseConfig.java

package com.carestock.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL = "jdbc:postgresql://ep-rough-field-a46l07i6-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASS = "npg_6SroEaZInY9v";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL JDBC no encontrado.");
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}


C. Capa de Datos: MedicamentoDAO.java
Ubicacion: Proto/src/com/carestock/dao/MedicamentoDAO.java

package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.Medicamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<Medicamento> obtenerTodos() {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT m.id_medicamento, m.codigo_invima, m.nombre_comercial, " +
                     "m.principio_activo, m.concentracion, c.nombre_categoria AS categoria, " +
                     "m.stock_total, m.stock_minimo " +
                     "FROM MEDICAMENTOS m " +
                     "LEFT JOIN CATEGORIAS c ON m.id_categoria = c.id_categoria " +
                     "ORDER BY m.id_medicamento ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Medicamento m = new Medicamento(
                    rs.getLong("id_medicamento"),
                    rs.getString("codigo_invima"),
                    rs.getString("nombre_comercial"),
                    rs.getString("principio_activo"),
                    rs.getString("concentracion"),
                    rs.getString("categoria"),
                    rs.getInt("stock_total"),
                    rs.getInt("stock_minimo")
                );
                lista.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar Neon DB: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public int obtenerTotalUnidadesStock() {
        String sql = "SELECT COALESCE(SUM(stock_total), 0) FROM MEDICAMENTOS";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener total unidades: " + e.getMessage());
        }
        return 0;
    }

    public int obtenerAlertasCriticas() {
        String sql = "SELECT COUNT(*) FROM MEDICAMENTOS WHERE stock_total <= stock_minimo";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al obtener alertas criticas: " + e.getMessage());
        }
        return 0;
    }

    public boolean guardar(Medicamento m) {
        return insertar(m);
    }

    public boolean agregarMedicamento(Medicamento m) {
        return insertar(m);
    }

    private int obtenerIdCategoriaPorNombre(Connection conn, String nombreCategoria) throws SQLException {
        if (nombreCategoria == null || nombreCategoria.trim().isEmpty()) {
            return 1;
        }
        String sqlSelect = "SELECT id_categoria FROM CATEGORIAS WHERE UPPER(nombre_categoria) = UPPER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setString(1, nombreCategoria.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria");
                }
            }
        }
        return 1;
    }

    public boolean insertar(Medicamento m) {
        String sql = "INSERT INTO MEDICAMENTOS (codigo_invima, nombre_comercial, principio_activo, concentracion, id_categoria, stock_total, stock_minimo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            int idCat = obtenerIdCategoriaPorNombre(conn, m.getCategoria());

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, m.getCodigoInvima());
                stmt.setString(2, m.getNombreComercial());
                stmt.setString(3, m.getPrincipioActivo());
                stmt.setString(4, m.getConcentracion() != null ? m.getConcentracion() : "N/A");
                stmt.setInt(5, idCat);
                stmt.setInt(6, m.getStockTotal() != null ? m.getStockTotal() : 0);
                stmt.setInt(7, m.getStockMinimo() != null ? m.getStockMinimo() : 0);

                int filasAfectadas = stmt.executeUpdate();
                return filasAfectadas > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar en Neon DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}


D. Dialogo Modal: FormularioMedicamentoDialog.java
Ubicacion: Proto/src/com/carestock/view/FormularioMedicamentoDialog.java

package com.carestock.view;

import com.carestock.model.Medicamento;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class FormularioMedicamentoDialog extends Dialog<Medicamento> {

    private TextField txtInvima = new TextField();
    private TextField txtNombre = new TextField();
    private TextField txtPrincipio = new TextField();
    private TextField txtStockInicial = new TextField();
    private TextField txtStockMinimo = new TextField();

    public FormularioMedicamentoDialog() {
        setTitle("CareStock - Agregar Nuevo Medicamento");
        setHeaderText("Ingrese los datos del nuevo medicamento.\nSiga los formatos de ejemplo mostrados en cada campo.");

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        txtInvima.setPromptText("Ej: INVIMA-2024M-057");
        txtNombre.setPromptText("Ej: Colchicina 0.5mg");
        txtPrincipio.setPromptText("Ej: Colchicina");
        txtStockInicial.setPromptText("Ej: 50");
        txtStockMinimo.setPromptText("Ej: 10");

        grid.add(new Label("Codigo INVIMA:"), 0, 0);
        grid.add(txtInvima, 1, 0);

        grid.add(new Label("Nombre Comercial:"), 0, 1);
        grid.add(txtNombre, 1, 1);

        grid.add(new Label("Principio Activo:"), 0, 2);
        grid.add(txtPrincipio, 1, 2);

        grid.add(new Label("Stock Inicial:"), 0, 3);
        grid.add(txtStockInicial, 1, 3);

        grid.add(new Label("Stock Minimo:"), 0, 4);
        grid.add(txtStockMinimo, 1, 4);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                int stock = 0;
                int min = 0;
                try {
                    stock = Integer.parseInt(txtStockInicial.getText().trim());
                } catch (Exception ignored) {}
                try {
                    min = Integer.parseInt(txtStockMinimo.getText().trim());
                } catch (Exception ignored) {}

                return new Medicamento(
                    null,
                    txtInvima.getText().trim(),
                    txtNombre.getText().trim(),
                    txtPrincipio.getText().trim(),
                    "N/A",
                    "General",
                    stock,
                    min
                );
            }
            return null;
        });
    }
}


E. Vista Principal: MainDashboardFX.java
Ubicacion: Proto/src/com/carestock/view/MainDashboardFX.java

package com.carestock.view;

import com.carestock.dao.MedicamentoDAO;
import com.carestock.model.Medicamento;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class MainDashboardFX extends Application {

    private TableView<Medicamento> tablaInventario = new TableView<>();
    private ObservableList<Medicamento> listaMedicamentos = FXCollections.observableArrayList();
    private FilteredList<Medicamento> listaFiltrada = new FilteredList<>(listaMedicamentos, p -> true);

    private MedicamentoDAO medicamentoDAO = new MedicamentoDAO();

    private Label lblTotalStock = new Label("0");
    private Label lblProximosVencer = new Label("0");
    private Label lblAlertasCriticas = new Label("0");

    private Button btnFiltrarCriticos;
    private boolean filtrandoCriticos = false;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #F8F9FA;");

        HBox topbar = buildTopbar();
        HBox metricCards = buildMetricCards();
        VBox tableContainer = buildTableSection();

        mainContent.getChildren().addAll(topbar, metricCards, tableContainer);
        root.setCenter(mainContent);

        cargarDatosDesdeBD();

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("CareStock - Modulo de Inventarios (Conectado a Neon DB)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cargarDatosDesdeBD() {
        try {
            List<Medicamento> desdeBD = medicamentoDAO.obtenerTodos();
            listaMedicamentos.setAll(desdeBD);

            int totalStock = medicamentoDAO.obtenerTotalUnidadesStock();
            int alertas = medicamentoDAO.obtenerAlertasCriticas();

            lblTotalStock.setText(String.format("%,d", totalStock));
            lblProximosVencer.setText("0");
            lblAlertasCriticas.setText(String.valueOf(alertas));

        } catch (Exception e) {
            System.err.println("Error al cargar datos desde Neon DB: " + e.getMessage());
        }
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #A3D9D2;");

        Label logo = new Label("CareStock");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1C313A;");

        Button btnDashboard = new Button("Dashboard");
        btnDashboard.setMaxWidth(Double.MAX_VALUE);
        btnDashboard.setStyle("-fx-background-color: #B39DDB; -fx-text-fill: white; -fx-font-weight: bold;");

        sidebar.getChildren().addAll(logo, new Separator(), btnDashboard);
        return sidebar;
    }

    private HBox buildTopbar() {
        HBox topbar = new HBox();
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Dashboard general");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox botonesAccion = new VBox(8);
        botonesAccion.setAlignment(Pos.CENTER_RIGHT);

        Button btnAgregar = new Button("+ Agregar Medicamento");
        btnAgregar.setStyle("-fx-background-color: #A3D9D2; -fx-font-weight: bold;");
        btnAgregar.setOnAction(e -> abrirFormularioAgregar());

        btnFiltrarCriticos = new Button("[!] Ver Alertas Criticas");
        btnFiltrarCriticos.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-weight: bold;");
        btnFiltrarCriticos.setOnAction(e -> alternarFiltroCriticos());

        botonesAccion.getChildren().addAll(btnAgregar, btnFiltrarCriticos);

        topbar.getChildren().addAll(title, spacer, botonesAccion);
        return topbar;
    }

    private void alternarFiltroCriticos() {
        filtrandoCriticos = !filtrandoCriticos;

        if (filtrandoCriticos) {
            listaFiltrada.setPredicate(m -> 
                m.getStockTotal() != null && m.getStockMinimo() != null && m.getStockTotal() <= m.getStockMinimo()
            );
            btnFiltrarCriticos.setText("Ver Todos los Medicamentos");
            btnFiltrarCriticos.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold;");
        } else {
            listaFiltrada.setPredicate(p -> true);
            btnFiltrarCriticos.setText("[!] Ver Alertas Criticas");
            btnFiltrarCriticos.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-weight: bold;");
        }
    }

    private HBox buildMetricCards() {
        HBox container = new HBox(15);
        container.getChildren().addAll(
            createCard(lblTotalStock, "Unidades en stock"),
            createCard(lblProximosVencer, "Proximos a vencer"),
            createCard(lblAlertasCriticas, "Alertas criticas")
        );
        return container;
    }

    private VBox createCard(Label numLabel, String label) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        numLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label subText = new Label(label);
        subText.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

        card.getChildren().addAll(numLabel, subText);
        return card;
    }

    private VBox buildTableSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label lblSection = new Label("Inventario en Neon DB");
        lblSection.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableColumn<Medicamento, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idMedicamento"));
        colId.setPrefWidth(50);

        TableColumn<Medicamento, String> colInvima = new TableColumn<>("INVIMA");
        colInvima.setCellValueFactory(new PropertyValueFactory<>("codigoInvima"));

        TableColumn<Medicamento, String> colNombre = new TableColumn<>("NOMBRE");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreComercial"));

        TableColumn<Medicamento, String> colPrincipio = new TableColumn<>("PRINCIPIO");
        colPrincipio.setCellValueFactory(new PropertyValueFactory<>("principioActivo"));

        TableColumn<Medicamento, String> colCategoria = new TableColumn<>("CATEGORIA");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        TableColumn<Medicamento, Integer> colStock = new TableColumn<>("STOCK");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockTotal"));

        TableColumn<Medicamento, Integer> colStockMin = new TableColumn<>("STOCK MIN");
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        tablaInventario.getColumns().clear();
        tablaInventario.getColumns().addAll(colId, colInvima, colNombre, colPrincipio, colCategoria, colStock, colStockMin);

        tablaInventario.setItems(listaFiltrada);
        tablaInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        section.getChildren().addAll(lblSection, tablaInventario);
        return section;
    }

    private void abrirFormularioAgregar() {
        FormularioMedicamentoDialog dialog = new FormularioMedicamentoDialog();
        Optional<Medicamento> result = dialog.showAndWait();
        result.ifPresent(medicamento -> {
            boolean exito = medicamentoDAO.guardar(medicamento);
            if (exito) {
                cargarDatosDesdeBD();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error al guardar el medicamento en Neon DB.");
                alert.showAndWait();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

-------------------------------------------------------------------------------
4. INSTRUCCIONES DE COMPILACION, EJECUCION Y GIT
-------------------------------------------------------------------------------

A. Compilar el proyecto:
javac --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml -d Proto/bin -cp "postgresql-driver.jar" $(find Proto/src -name "*.java")

B. Ejecutar la aplicacion:
java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml -cp "Proto/bin;postgresql-driver.jar" com.carestock.view.MainDashboardFX
