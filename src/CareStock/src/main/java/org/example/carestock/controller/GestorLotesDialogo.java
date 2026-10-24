package org.example.carestock.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import org.example.carestock.model.Lote;
import org.example.carestock.service.ServicioCrudLotes;
import org.example.carestock.service.ServicioCrudLotes.MedicamentoOpcion;
import org.example.carestock.service.ServicioCrudLotes.UbicacionOpcion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Formularios CRUD de lotes integrados al dashboard.
 */
public final class GestorLotesDialogo {

    private final Window ventana;

    private final ServicioCrudLotes servicio =
            new ServicioCrudLotes();

    public GestorLotesDialogo(Window ventana) {
        this.ventana = ventana;
    }

    private void aviso(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.initOwner(ventana);
        alerta.setTitle("CareStock - Lotes");
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }

    private static int cantidad(TextField campo) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser un numero entero valido");
        }
    }

    private boolean formulario(Lote original) {

        boolean nuevo = original == null;

        List<MedicamentoOpcion> medicamentos;
        List<UbicacionOpcion> ubicaciones;

        try {
            medicamentos = nuevo
                    ? servicio.medicamentos()
                    : List.of();

            ubicaciones = servicio.ubicaciones();

        } catch (Exception ex) {
            aviso(Alert.AlertType.ERROR,
                    "No se pudieron cargar los datos",
                    ex.getMessage());
            return false;
        }

        if ((nuevo && medicamentos.isEmpty())
                || ubicaciones.isEmpty()) {

            aviso(Alert.AlertType.WARNING,
                    "Datos insuficientes",
                    "Se requiere un medicamento activo y " +
                    "una ubicacion registrada en Neon.");
            return false;
        }

        ComboBox<MedicamentoOpcion> cmbMed =
                new ComboBox<>();

        cmbMed.getItems().setAll(medicamentos);

        if (nuevo) {
            cmbMed.getSelectionModel().selectFirst();
        }

        cmbMed.setMaxWidth(Double.MAX_VALUE);

        ComboBox<UbicacionOpcion> cmbUbicacion =
                new ComboBox<>();

        cmbUbicacion.getItems().setAll(ubicaciones);

        if (nuevo) {
            cmbUbicacion.getSelectionModel().selectFirst();
        } else {
            cmbUbicacion.getItems().stream()
                    .filter(u -> original.getIdUbicacion() != null
                            && u.id() == original.getIdUbicacion())
                    .findFirst()
                    .ifPresent(cmbUbicacion::setValue);
        }

        cmbUbicacion.setMaxWidth(Double.MAX_VALUE);

        TextField txtNumero = new TextField(
                nuevo ? "" : original.getNumeroLote());

        txtNumero.setPromptText("Numero del lote");

        TextField txtCantidad = new TextField(
                nuevo ? "" :
                        Integer.toString(original.getCantidadActual()));

        txtCantidad.setDisable(!nuevo);
        txtCantidad.setPromptText("Unidades iniciales");

        DatePicker fecha = new DatePicker(
                nuevo
                    ? LocalDate.now().plusMonths(12)
                    : original.getFechaVencimiento());

        fecha.setMaxWidth(Double.MAX_VALUE);

        GridPane formulario = new GridPane();

        formulario.setHgap(12);
        formulario.setVgap(12);
        formulario.setPadding(new Insets(18));

        int fila = 0;

        if (nuevo) {
            formulario.addRow(fila++,
                    new Label("Medicamento"), cmbMed);
        } else {
            formulario.addRow(fila++,
                    new Label("Medicamento"),
                    new Label("ID " + original.getIdMedicamento()
                            + " (no modificable)"));
        }

        formulario.addRow(fila++,
                new Label("Numero de lote"), txtNumero);

        formulario.addRow(fila++,
                new Label("Cantidad"), txtCantidad);

        formulario.addRow(fila++,
                new Label("Vencimiento"), fecha);

        formulario.addRow(fila++,
                new Label("Ubicacion"), cmbUbicacion);

        formulario.add(
                new Label(nuevo
                        ? "El alta registra entrada y actualiza stock total."
                        : "La cantidad no se edita aqui: " +
                          "utiliza Ajustes de inventario."),
                0, fila, 2, 1);

        Dialog<ButtonType> dialogo = new Dialog<>();

        dialogo.initOwner(ventana);

        dialogo.setTitle(
                nuevo
                    ? "Crear lote"
                    : "Editar lote #" + original.getIdLote());

        dialogo.setHeaderText(nuevo
                ? "Registrar lote de medicamento"
                : "Modificar datos del lote (sin alterar existencias)");

        dialogo.getDialogPane().setContent(formulario);

        ButtonType confirmar = new ButtonType(
                nuevo ? "Crear lote" : "Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);

        dialogo.getDialogPane().getButtonTypes()
                .addAll(confirmar, ButtonType.CANCEL);

        while (true) {

            Optional<ButtonType> elegido =
                    dialogo.showAndWait();

            if (elegido.isEmpty()
                    || elegido.get() != confirmar) {
                return false;
            }

            try {
                UbicacionOpcion u =
                        cmbUbicacion.getValue();

                if (u == null) {
                    throw new IllegalArgumentException(
                            "Selecciona una ubicacion");
                }

                if (nuevo) {

                    MedicamentoOpcion m =
                            cmbMed.getValue();

                    if (m == null) {
                        throw new IllegalArgumentException(
                                "Selecciona un medicamento");
                    }

                    int id = servicio.crear(
                            m.id(),
                            txtNumero.getText(),
                            cantidad(txtCantidad),
                            fecha.getValue(),
                            u.id());

                    aviso(Alert.AlertType.INFORMATION,
                            "Lote creado",
                            "Se registro el lote #" + id +
                            " y su entrada de inventario.");

                } else {

                    servicio.editar(
                            original,
                            txtNumero.getText(),
                            fecha.getValue(),
                            u.id());

                    aviso(Alert.AlertType.INFORMATION,
                            "Lote actualizado",
                            "Datos guardados sin modificar existencias.");
                }

                return true;

            } catch (Exception ex) {

                aviso(Alert.AlertType.WARNING,
                        "No se aplicaron cambios",
                        ex.getMessage());
            }
        }
    }

    public boolean crear() {
        return formulario(null);
    }

    public boolean editar(Lote lote) {
        return formulario(lote);
    }

    public boolean retirar(Lote lote) {

        if (lote.getCantidadActual() > 0 && (lote.getFechaVencimiento() == null || lote.getFechaVencimiento().isAfter(LocalDate.now()))) {

            aviso(Alert.AlertType.WARNING,
                    "No se puede retirar",
                    "El lote tiene " + lote.getCantidadActual()
                    + " unidades. Ajusta primero el stock a cero "
                    + "con motivo auditado.");

            return false;
        }

        Alert pregunta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "El lote " + lote.getNumeroLote() +
                " quedara RETENIDO. No se borrara de Neon " +
                "ni se perdera su historial.",
                ButtonType.YES,
                ButtonType.NO);

        pregunta.initOwner(ventana);

        pregunta.setHeaderText(
                "Confirmar retiro logico del lote #"
                + lote.getIdLote());

        if (pregunta.showAndWait().orElse(ButtonType.NO)
                != ButtonType.YES) {
            return false;
        }

        try {

            servicio.retirar(lote);

            aviso(Alert.AlertType.INFORMATION,
                    "Lote retirado",
                    "Estado: RETENIDO. Historial conservado.");

            return true;

        } catch (Exception ex) {

            aviso(Alert.AlertType.ERROR,
                    "No se retiro el lote",
                    ex.getMessage());

            return false;
        }
    }
}
