package org.example.carestock.service;

import org.example.carestock.config.ConexionBaseDatos;
import org.example.carestock.model.Lote;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * CRUD seguro de lotes.
 * La eliminacion es logica para conservar trazabilidad.
 */
public final class ServicioCrudLotes {

    public record MedicamentoOpcion(int id, String nombre) {
        @Override
        public String toString() {
            return nombre + " (#" + id + ")";
        }
    }

    public record UbicacionOpcion(int id, String estante, String nivel) {
        @Override
        public String toString() {
            return estante + " / " + nivel + " (#" + id + ")";
        }
    }

    private record Actor(int usuario, int farmacia) {}

    static boolean rolGestor(String rol) {
        String normalizado = rol == null ? "" : rol.trim();
        return "ADMINISTRADOR".equalsIgnoreCase(normalizado)
                || "SUPER_ADMIN".equalsIgnoreCase(normalizado)
                || "FARMACEUTICO".equalsIgnoreCase(normalizado);
    }

    private static Actor actor() {
        if (!SesionUsuario.estaActiva()) {
            throw new IllegalStateException("Sesion no iniciada");
        }

        var s = SesionUsuario.actual();

        if (!rolGestor(s.rol())
                || s.idFarmacia() == null
                || s.idFarmacia() <= 0) {
            throw new SecurityException(
                    "Solo administradores con farmacia pueden gestionar lotes");
        }

        return new Actor(s.id(), s.idFarmacia());
    }

    private static void validarActor(Connection con, Actor actor)
            throws SQLException {

        String sql = """
                SELECT u.id_farmacia, r.nombre_rol
                FROM usuarios u
                JOIN roles r ON r.id_rol = u.id_rol
                JOIN farmacias f ON f.id_farmacia = u.id_farmacia
                WHERE u.id_usuario = ?
                  AND UPPER(TRIM(u.estado)) = 'ACTIVO'
                  AND UPPER(TRIM(f.estado)) = 'ACTIVA'
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, actor.usuario());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()
                        || rs.getInt("id_farmacia") != actor.farmacia()
                        || !rolGestor(rs.getString("nombre_rol"))) {
                    throw new SecurityException(
                            "Usuario sin permiso o farmacia diferente");
                }
            }
        }
    }

    public List<MedicamentoOpcion> medicamentos() throws Exception {
        Actor a = actor();
        List<MedicamentoOpcion> lista = new ArrayList<>();

        try (Connection con = ConexionBaseDatos.getConexion()) {
            validarActor(con, a);

            try (PreparedStatement ps = con.prepareStatement("""
                    SELECT id_medicamento, nombre_comercial
                    FROM medicamentos
                    WHERE id_farmacia = ?
                      AND UPPER(estado) = 'ACTIVO'
                    ORDER BY nombre_comercial
                    """)) {

                ps.setInt(1, a.farmacia());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new MedicamentoOpcion(
                                rs.getInt(1), rs.getString(2)));
                    }
                }
            }
        }

        return lista;
    }

    public List<UbicacionOpcion> ubicaciones() throws Exception {
        Actor a = actor();
        List<UbicacionOpcion> lista = new ArrayList<>();

        try (Connection con = ConexionBaseDatos.getConexion()) {
            validarActor(con, a);

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_ubicacion, estante, nivel " +
                    "FROM ubicaciones ORDER BY estante, nivel");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(new UbicacionOpcion(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3)));
                }
            }
        }

        return lista;
    }

    static void validarNuevo(
            String numero,
            int cantidad,
            LocalDate fecha,
            int idUbicacion) {

        validarCampos(numero, fecha, idUbicacion);

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad inicial debe ser mayor que cero");
        }

        if (!fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "El nuevo lote debe tener vencimiento futuro");
        }
    }

    private static void validarCampos(
            String numero,
            LocalDate fecha,
            int ubicacion) {

        if (numero == null
                || numero.isBlank()
                || numero.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Numero de lote obligatorio (maximo 50 caracteres)");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("Fecha obligatoria");
        }

        if (ubicacion <= 0) {
            throw new IllegalArgumentException("Ubicacion obligatoria");
        }
    }

    static void validarRetiro(int stock, String estado) {
        validarRetiro(stock, estado, null);
    }

    static void validarRetiro(
            int stock,
            String estado,
            LocalDate vencimiento) {

        if ("RETENIDO".equalsIgnoreCase(estado)) {
            throw new IllegalStateException(
                    "El lote ya fue retirado");
        }

        boolean deshabilitado =
                "DESHABILITADO".equalsIgnoreCase(estado)
                || (vencimiento != null
                    && !vencimiento.isAfter(LocalDate.now()));

        if (stock != 0 && !deshabilitado) {
            throw new IllegalStateException(
                    "Solo se puede retirar un lote con stock positivo "
                    + "cuando esta DESHABILITADO por vencimiento.");
        }
    }

    private static void verificarUbicacion(
            Connection con,
            int idUbicacion) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM ubicaciones WHERE id_ubicacion = ?")) {

            ps.setInt(1, idUbicacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Ubicacion inexistente");
                }
            }
        }
    }

    private static void log(
            Connection con,
            int usuario,
            int lote,
            String tipo,
            int cantidad,
            String detalle) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement("""
                INSERT INTO log_movimientos
                    (id_usuario, id_lote, tipo_movimiento,
                     cantidad_afectada, detalle_cambio)
                VALUES (?, ?, ?, ?, ?)
                """)) {

            ps.setInt(1, usuario);
            ps.setInt(2, lote);
            ps.setString(3, tipo);
            ps.setInt(4, cantidad);
            ps.setString(5, detalle);
            ps.executeUpdate();
        }
    }

    /**
     * Reconciliacion del stock total.
     * Evita duplicar incrementos cuando existe un trigger.
     */
    private static void recalcular(
            Connection con,
            int idMed,
            int usuario,
            int farmacia) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement("""
                UPDATE medicamentos m
                SET stock_total = COALESCE((
                    SELECT SUM(l.cantidad_actual)
                    FROM lotes l
                    WHERE l.id_medicamento = m.id_medicamento
                      AND l.estado_lote = 'DISPONIBLE'
                      AND l.fecha_vencimiento > CURRENT_DATE
                ), 0),
                id_usuario_modificacion = ?,
                fecha_modificacion = NOW()
                WHERE m.id_medicamento = ?
                  AND m.id_farmacia = ?
                """)) {

            ps.setInt(1, usuario);
            ps.setInt(2, idMed);
            ps.setInt(3, farmacia);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se actualizo el stock total");
            }
        }
    }

    public int crear(
            int idMedicamento,
            String numero,
            int cantidad,
            LocalDate fecha,
            int idUbicacion) throws Exception {

        validarNuevo(numero, cantidad, fecha, idUbicacion);

        if (idMedicamento <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona un medicamento");
        }

        Actor a = actor();
        int idLote;

        try (Connection con = ConexionBaseDatos.getConexion()) {
            boolean anterior = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                validarActor(con, a);

                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT id_medicamento
                        FROM medicamentos
                        WHERE id_medicamento = ?
                          AND id_farmacia = ?
                          AND estado = 'ACTIVO'
                        FOR UPDATE
                        """)) {

                    ps.setInt(1, idMedicamento);
                    ps.setInt(2, a.farmacia());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SecurityException(
                                    "Medicamento no pertenece a la farmacia " +
                                    "o esta inactivo");
                        }
                    }
                }

                verificarUbicacion(con, idUbicacion);

                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO lotes
                            (numero_lote, id_medicamento,
                             cantidad_actual, fecha_vencimiento,
                             id_ubicacion, estado_lote, id_usuario)
                        VALUES (?, ?, ?, ?, ?, 'DISPONIBLE', ?)
                        RETURNING id_lote
                        """)) {

                    ps.setString(1, numero.trim());
                    ps.setInt(2, idMedicamento);
                    ps.setInt(3, cantidad);
                    ps.setDate(4, java.sql.Date.valueOf(fecha));
                    ps.setInt(5, idUbicacion);
                    ps.setInt(6, a.usuario());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se genero ID de lote");
                        }
                        idLote = rs.getInt(1);
                    }
                }

                recalcular(
                        con, idMedicamento, a.usuario(), a.farmacia());

                log(con, a.usuario(), idLote, "ENTRADA", cantidad,
                        "Registro inicial del lote " + numero.trim());

                con.commit();

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(anterior);
            }
        }

        InventarioConsultas.invalidarFarmacia(a.farmacia());
        return idLote;
    }

    private record EstadoLote(
            int medicamento,
            String numero,
            LocalDate fecha,
            Integer ubicacion,
            int cantidad,
            String estado) {}

    private static EstadoLote bloquearLote(
            Connection con,
            int idLote,
            int farmacia) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT l.id_medicamento, l.numero_lote,
                       l.fecha_vencimiento, l.id_ubicacion,
                       l.cantidad_actual, l.estado_lote
                FROM lotes l
                JOIN medicamentos m
                  ON m.id_medicamento = l.id_medicamento
                WHERE l.id_lote = ?
                  AND m.id_farmacia = ?
                FOR UPDATE OF l, m
                """)) {

            ps.setInt(1, idLote);
            ps.setInt(2, farmacia);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SecurityException(
                            "Lote inexistente o pertenece a otra farmacia");
                }

                int ubicacion = rs.getInt("id_ubicacion");
                Integer ubicacionFinal =
                        rs.wasNull() ? null : ubicacion;

                return new EstadoLote(
                        rs.getInt("id_medicamento"),
                        rs.getString("numero_lote"),
                        rs.getDate("fecha_vencimiento").toLocalDate(),
                        ubicacionFinal,
                        rs.getInt("cantidad_actual"),
                        rs.getString("estado_lote"));
            }
        }
    }

    /**
     * Rechaza una edicion sobre datos desactualizados.
     */
    private static void sinCambiosExternos(
            Lote original,
            EstadoLote db) {

        if (original.getIdMedicamento() != db.medicamento()
                || !Objects.equals(
                        original.getNumeroLote(), db.numero())
                || !Objects.equals(
                        original.getFechaVencimiento(), db.fecha())
                || !Objects.equals(
                        original.getIdUbicacion(), db.ubicacion())
                || original.getCantidadActual() != db.cantidad()
                || !Objects.equals(
                        original.getEstadoLote(), db.estado())) {

            throw new IllegalStateException(
                    "El lote cambio desde la ultima consulta. " +
                    "Actualiza la tabla.");
        }
    }

    public void editar(
            Lote original,
            String numero,
            LocalDate fecha,
            int idUbicacion) throws Exception {

        Objects.requireNonNull(original, "Selecciona un lote");
        validarCampos(numero, fecha, idUbicacion);

        if (!fecha.equals(original.getFechaVencimiento())
                && !fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La nueva fecha debe ser futura");
        }

        Actor a = actor();

        try (Connection con = ConexionBaseDatos.getConexion()) {
            boolean anterior = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                validarActor(con, a);

                EstadoLote db = bloquearLote(
                        con, original.getIdLote(), a.farmacia());

                sinCambiosExternos(original, db);

                if ("RETENIDO".equalsIgnoreCase(db.estado())) {
                    throw new IllegalStateException(
                            "No se puede editar un lote retirado");
                }

                verificarUbicacion(con, idUbicacion);

                if (numero.trim().equals(db.numero())
                        && fecha.equals(db.fecha())
                        && Objects.equals(
                                idUbicacion, db.ubicacion())) {
                    throw new IllegalArgumentException(
                            "No hay cambios para guardar");
                }

                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE lotes
                        SET numero_lote = ?,
                            fecha_vencimiento = ?,
                            id_ubicacion = ?,
                            id_usuario_modificacion = ?,
                            fecha_modificacion = NOW()
                        WHERE id_lote = ?
                        """)) {

                    ps.setString(1, numero.trim());
                    ps.setDate(2, java.sql.Date.valueOf(fecha));
                    ps.setInt(3, idUbicacion);
                    ps.setInt(4, a.usuario());
                    ps.setInt(5, original.getIdLote());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se actualizo el lote");
                    }
                }

                // Evento administrativo sin cambio de unidades.
                log(con, a.usuario(), original.getIdLote(),
                        "AJUSTE", 0,
                        "Edicion de datos del lote (sin modificar unidades)");

                con.commit();

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(anterior);
            }
        }

        InventarioConsultas.invalidarFarmacia(a.farmacia());
    }

    public void retirar(Lote original) throws Exception {
        Objects.requireNonNull(original, "Selecciona un lote");

        Actor a = actor();

        try (Connection con = ConexionBaseDatos.getConexion()) {
            boolean anterior = con.getAutoCommit();
            con.setAutoCommit(false);

            try {
                validarActor(con, a);

                EstadoLote db = bloquearLote(
                        con, original.getIdLote(), a.farmacia());

                sinCambiosExternos(original, db);
                validarRetiro(db.cantidad(), db.estado(), db.fecha());

                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE lotes
                        SET estado_lote = 'RETENIDO',
                            id_usuario_modificacion = ?,
                            fecha_modificacion = NOW()
                        WHERE id_lote = ?
                        """)) {

                    ps.setInt(1, a.usuario());
                    ps.setInt(2, original.getIdLote());

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se retiro el lote");
                    }
                }

                recalcular(con, db.medicamento(),
                        a.usuario(), a.farmacia());

                log(con, a.usuario(), original.getIdLote(),
                        "AJUSTE", 0,
                        "Retiro logico del lote. Historial conservado");

                con.commit();

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(anterior);
            }
        }

        InventarioConsultas.invalidarFarmacia(a.farmacia());
    }
}
