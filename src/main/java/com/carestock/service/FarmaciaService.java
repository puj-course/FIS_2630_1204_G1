package com.carestock.service;

import com.carestock.dao.FarmaciaDAO;
import com.carestock.model.Farmacia;
import com.carestock.model.ResumenFarmacia;
import com.carestock.security.AccessControl;
import com.carestock.session.SessionContext;

import java.sql.SQLException;
import java.util.List;

public class FarmaciaService {

    private final FarmaciaDAO farmaciaDAO;
    private final SessionContext sessionContext;


    public FarmaciaService() {

        this(
                new FarmaciaDAO(),
                new SessionContext()
        );
    }


    FarmaciaService(
            FarmaciaDAO farmaciaDAO,
            SessionContext sessionContext
    ) {

        this.farmaciaDAO =
                farmaciaDAO;

        this.sessionContext =
                sessionContext;
    }


    public Farmacia crearFarmacia(
            String codigo,
            String nombre
    ) throws SQLException {

        AccessControl.requireRole(
                "SUPER_ADMIN"
        );


        validarDatos(
                codigo,
                nombre
        );


        int idUsuarioActor =
                sessionContext
                        .requireAuthenticatedUserId();


        int idFarmacia =
                farmaciaDAO
                        .crearSegura(
                                idUsuarioActor,
                                codigo.trim(),
                                nombre.trim()
                        );


        Farmacia farmacia =
                farmaciaDAO
                        .buscarPorId(
                                idFarmacia
                        );


        if (farmacia == null) {

            throw new SQLException(
                    "La farmacia fue creada pero "
                    + "no pudo recuperarse posteriormente."
            );
        }


        return farmacia;
    }


    public List<Farmacia> listarActivas()
            throws SQLException {

        AccessControl.requireRole(
                "SUPER_ADMIN"
        );


        return farmaciaDAO
                .listarActivas();
    }

    public List<ResumenFarmacia> obtenerResumenGlobal() throws SQLException {
        AccessControl.requireRole("SUPER_ADMIN");
        return farmaciaDAO.obtenerResumenGlobal();
    }


    private void validarDatos(
            String codigo,
            String nombre
    ) {

        if (
                codigo == null
                || codigo.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El código de farmacia es obligatorio."
            );
        }


        if (
                nombre == null
                || nombre.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El nombre de farmacia es obligatorio."
            );
        }


        if (
                codigo.trim().length()
                > 40
        ) {

            throw new IllegalArgumentException(
                    "El código no puede superar 40 caracteres."
            );
        }


        if (
                nombre.trim().length()
                > 150
        ) {

            throw new IllegalArgumentException(
                    "El nombre no puede superar 150 caracteres."
            );
        }
    }
}
