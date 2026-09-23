package com.carestock.service;

import com.carestock.dao.LoteDAO;
import com.carestock.model.Lote;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.session.SessionContext;
import com.carestock.utils.IngresoLoteValidator;

import java.sql.SQLException;
import java.time.LocalDate;

public class IngresoLoteService {

    private final LoteDAO loteDAO;
    private final SessionContext sessionContext;

    public IngresoLoteService() {
        this(
                new LoteDAO(),
                new SessionContext()
        );
    }

    public IngresoLoteService(
            LoteDAO loteDAO,
            SessionContext sessionContext
    ) {

        if (loteDAO == null) {
            throw new IllegalArgumentException(
                    "LoteDAO no puede ser nulo."
            );
        }

        if (sessionContext == null) {
            throw new IllegalArgumentException(
                    "SessionContext no puede ser nulo."
            );
        }

        this.loteDAO = loteDAO;
        this.sessionContext = sessionContext;
    }

    public void registrar(
            Medicamento medicamento,
            String numeroLote,
            String cantidadStr,
            LocalDate fechaVencimiento,
            Ubicacion ubicacion
    ) throws SQLException {

        /*
         * El usuario responsable se obtiene exclusivamente
         * desde la sesión autenticada.
         *
         * Ningún ID de usuario puede venir desde la UI.
         */
        int idUsuario =
                sessionContext.requireAuthenticatedUserId();

        String error = IngresoLoteValidator.validar(
                medicamento,
                numeroLote,
                cantidadStr,
                fechaVencimiento,
                ubicacion
        );

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        int cantidad =
                Integer.parseInt(cantidadStr.trim());

        /*
         * El usuario queda fijado en el objeto que
         * será enviado a persistencia.
         */
        Lote lote = new Lote(
                numeroLote.trim(),
                Math.toIntExact(
                        medicamento.getIdMedicamento()
                ),
                cantidad,
                fechaVencimiento,
                ubicacion.getIdUbicacion(),
                idUsuario
        );

        loteDAO.registrarNuevoLote(lote);
    }
}
