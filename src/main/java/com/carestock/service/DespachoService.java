package com.carestock.service;

import com.carestock.dao.LoteDAO;
import com.carestock.model.DespachoLote;
import com.carestock.session.SessionContext;

import java.sql.SQLException;

/**
 * Servicio encargado de realizar operaciones de despacho.
 *
 * El usuario responsable nunca se recibe desde la interfaz.
 * Siempre se obtiene desde SessionContext.
 */
public class DespachoService {

    private final LoteDAO loteDAO;
    private final SessionContext sessionContext;

    public DespachoService() {
        this(
                new LoteDAO(),
                new SessionContext()
        );
    }

    public DespachoService(
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

    public void despachar(
            int idLote,
            int cantidad
    ) throws SQLException {

        /*
         * El usuario nunca se recibe como parámetro.
         * Se extrae únicamente de la sesión autenticada.
         */
        int idUsuario =
                sessionContext.requireAuthenticatedUserId();

        DespachoLote despacho =
                new DespachoLote(
                        idLote,
                        cantidad,
                        idUsuario
                );

        loteDAO.despacharLote(despacho);
    }
}
