package com.carestock.service;

import com.carestock.dao.InventarioCrudDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.LoteGestion;
import com.carestock.model.MedicamentoGestion;
import com.carestock.model.Ubicacion;
import com.carestock.security.AccessControl;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventarioCrudService {

    private final InventarioCrudDAO dao;
    private final SessionContext sessionContext;


    public InventarioCrudService() {

        this(
                new InventarioCrudDAO(),
                new SessionContext()
        );
    }


    InventarioCrudService(
            InventarioCrudDAO dao,
            SessionContext sessionContext
    ) {

        this.dao = dao;
        this.sessionContext = sessionContext;
    }


    public int resolverFarmacia(
            Integer idFarmaciaSolicitada
    ) {

        AccessControl.requireRole(
                "SUPER_ADMIN",
                "ADMINISTRADOR"
        );

        UserSession.CurrentUser actor =
                sessionContext
                        .requireCurrentUser();

        if (!actor.tieneFarmaciaAsignada()) {

            throw new IllegalStateException(
                    SessionContext.ERROR_FARMACIA_REQUERIDA
            );
        }

        int idFarmaciaSesion =
                actor.getIdFarmacia();

        if (
                idFarmaciaSolicitada != null
                && !idFarmaciaSolicitada.equals(
                        idFarmaciaSesion
                )
        ) {

            throw new AccesoDenegadoException(
                    "No se puede gestionar inventario "
                    + "de una farmacia distinta a la sesión activa."
            );
        }

        return idFarmaciaSesion;
    }


    public List<MedicamentoGestion> listarMedicamentos(
            Integer idFarmaciaSolicitada,
            boolean incluirInactivos
    ) throws SQLException {

        return dao.listarMedicamentos(
                resolverFarmacia(
                        idFarmaciaSolicitada
                ),
                incluirInactivos
        );
    }


    public List<LoteGestion> listarLotes(
            Integer idFarmaciaSolicitada
    ) throws SQLException {

        return dao.listarLotes(
                resolverFarmacia(
                        idFarmaciaSolicitada
                )
        );
    }


    public List<String> listarCategorias()
            throws SQLException {

        AccessControl.requireRole(
                "SUPER_ADMIN",
                "ADMINISTRADOR"
        );

        return dao.listarCategorias();
    }


    public List<Ubicacion> listarUbicaciones()
            throws SQLException {

        AccessControl.requireRole(
                "SUPER_ADMIN",
                "ADMINISTRADOR"
        );

        return dao.listarUbicaciones();
    }


    public int crearMedicamento(
            Integer idFarmaciaSolicitada,
            MedicamentoGestion medicamento
    ) throws SQLException {

        validarMedicamento(
                medicamento
        );


        return dao.crearMedicamento(
                sessionContext
                        .requireAuthenticatedUserId(),

                resolverFarmacia(
                        idFarmaciaSolicitada
                ),

                medicamento
        );
    }


    public void actualizarMedicamento(
            Integer idFarmaciaSolicitada,
            MedicamentoGestion medicamento
    ) throws SQLException {

        if (
                medicamento == null
                || medicamento.getIdMedicamento() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Seleccione un medicamento válido."
            );
        }


        validarMedicamento(
                medicamento
        );


        dao.actualizarMedicamento(
                sessionContext
                        .requireAuthenticatedUserId(),

                resolverFarmacia(
                        idFarmaciaSolicitada
                ),

                medicamento
        );
    }


    public void cambiarEstadoMedicamento(
            Integer idFarmaciaSolicitada,
            long idMedicamento,
            String nuevoEstado
    ) throws SQLException {

        if (
                !"ACTIVO".equalsIgnoreCase(
                        nuevoEstado
                )
                && !"INACTIVO".equalsIgnoreCase(
                        nuevoEstado
                )
        ) {

            throw new IllegalArgumentException(
                    "Estado de medicamento inválido."
            );
        }


        dao.cambiarEstadoMedicamento(
                sessionContext
                        .requireAuthenticatedUserId(),

                resolverFarmacia(
                        idFarmaciaSolicitada
                ),

                idMedicamento,

                nuevoEstado
                        .toUpperCase()
        );
    }


    public int crearLote(
            Integer idFarmaciaSolicitada,
            int idMedicamento,
            String numeroLote,
            int cantidad,
            LocalDate fechaVencimiento,
            int idUbicacion
    ) throws SQLException {

        validarLote(
                numeroLote,
                cantidad,
                fechaVencimiento,
                idUbicacion,
                true
        );


        return dao.crearLote(
                sessionContext
                        .requireAuthenticatedUserId(),

                resolverFarmacia(
                        idFarmaciaSolicitada
                ),

                idMedicamento,
                numeroLote.trim(),
                cantidad,
                fechaVencimiento,
                idUbicacion
        );
    }


    public void actualizarLote(
            Integer idFarmaciaSolicitada,
            int idLote,
            String numeroLote,
            int cantidad,
            LocalDate fechaVencimiento,
            int idUbicacion
    ) throws SQLException {

        if (idLote <= 0) {

            throw new IllegalArgumentException(
                    "Seleccione un lote válido."
            );
        }


        validarLote(
                numeroLote,
                cantidad,
                fechaVencimiento,
                idUbicacion,
                false
        );


        dao.actualizarLote(
                sessionContext
                        .requireAuthenticatedUserId(),

                resolverFarmacia(
                        idFarmaciaSolicitada
                ),

                idLote,
                numeroLote.trim(),
                cantidad,
                fechaVencimiento,
                idUbicacion
        );
    }


    public void cambiarEstadoLote(
            Integer idFarmaciaSolicitada,
            int idLote,
            String nuevoEstado
    ) throws SQLException {

        if (
                !"RETENIDO".equalsIgnoreCase(
                        nuevoEstado
                )
                && !"DISPONIBLE".equalsIgnoreCase(
                        nuevoEstado
                )
        ) {

            throw new IllegalArgumentException(
                    "Solo se permite retener o reactivar un lote."
            );
        }


        dao.cambiarEstadoLote(
                sessionContext
                        .requireAuthenticatedUserId(),

                resolverFarmacia(
                        idFarmaciaSolicitada
                ),

                idLote,

                nuevoEstado
                        .toUpperCase()
        );
    }


    private void validarMedicamento(
            MedicamentoGestion medicamento
    ) {

        if (medicamento == null) {

            throw new IllegalArgumentException(
                    "El medicamento es obligatorio."
            );
        }


        if (vacio(
                medicamento.getCodigoInvima()
        )) {

            throw new IllegalArgumentException(
                    "El código INVIMA es obligatorio."
            );
        }


        if (vacio(
                medicamento.getNombreComercial()
        )) {

            throw new IllegalArgumentException(
                    "El nombre comercial es obligatorio."
            );
        }


        if (vacio(
                medicamento.getPrincipioActivo()
        )) {

            throw new IllegalArgumentException(
                    "El principio activo es obligatorio."
            );
        }


        if (vacio(
                medicamento.getCategoria()
        )) {

            throw new IllegalArgumentException(
                    "La categoría es obligatoria."
            );
        }


        if (
                medicamento.getStockMinimo()
                < 0
        ) {

            throw new IllegalArgumentException(
                    "El stock mínimo no puede ser negativo."
            );
        }
    }


    private void validarLote(
            String numeroLote,
            int cantidad,
            LocalDate fechaVencimiento,
            int idUbicacion,
            boolean creacion
    ) {

        if (vacio(numeroLote)) {

            throw new IllegalArgumentException(
                    "El número de lote es obligatorio."
            );
        }


        if (
                cantidad < 0
                || (
                    creacion
                    && cantidad == 0
                )
        ) {

            throw new IllegalArgumentException(
                    creacion
                            ? "La cantidad inicial debe ser mayor que cero."
                            : "La cantidad no puede ser negativa."
            );
        }


        if (fechaVencimiento == null) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento es obligatoria."
            );
        }


        if (
                creacion
                && !fechaVencimiento
                        .isAfter(
                                LocalDate.now()
                        )
        ) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser futura."
            );
        }


        if (idUbicacion <= 0) {

            throw new IllegalArgumentException(
                    "Seleccione una ubicación válida."
            );
        }
    }


    private boolean vacio(
            String valor
    ) {

        return valor == null
                || valor.isBlank();
    }
}
