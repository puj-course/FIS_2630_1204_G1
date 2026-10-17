
package org.example.carestock.facade;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.config.ConexionBD;
import org.example.carestock.dao.*;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.*;
import org.example.carestock.proxy.ConsultaInventario;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.service.RegistroLoteService;
import org.example.carestock.service.FarmacovigilanciaService;
import org.example.carestock.service.ControlVencimientosService;
import org.example.carestock.session.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Inventario {

    private final int idUsuario;

    private final AseoDAO aseoDAO = new AseoDAO();
    private final MaternidadDAO maternidadDAO = new MaternidadDAO();

    private final MedicamentoDAO medicamentoDAO =
            new MedicamentoDAOImpl();

    private final MedicamentoCatalogoDAO medicamentoCatalogoDAO =
            new MedicamentoCatalogoDAO();

    private final LoteDAO loteDAO = new LoteDAOImpl();

    private final ConsultaInventario consulta =
            InventarioConsultas.compartida();

    private final RegistroLoteService registroLoteService =
            new RegistroLoteService();

    private final FarmacovigilanciaService farmacovigilanciaService =
            new FarmacovigilanciaService();

    private final ControlVencimientosService controlVencimientosService =
            new ControlVencimientosService();

    public Inventario(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() <= 0) {
            throw new IllegalArgumentException(
                    "Se requiere un usuario autenticado"
            );
        }

        idUsuario = usuario.getIdUsuario();
    }

    private int farmaciaActual() throws Exception {
        if (SesionUsuario.actual().id() != idUsuario) {
            throw new ReglaNegocioException(
                    "La sesion ha cambiado. Abra nuevamente la vista."
            );
        }

        String sql = """
            SELECT u.id_farmacia
            FROM usuarios u
            INNER JOIN farmacias f ON f.id_farmacia = u.id_farmacia
            WHERE u.id_usuario = ?
              AND UPPER(TRIM(u.estado)) = 'ACTIVO'
              AND UPPER(TRIM(f.estado)) = 'ACTIVA'
            """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Usuario inactivo o sin farmacia activa asignada"
                    );
                }

                return rs.getInt("id_farmacia");
            }
        }
    }

    @FunctionalInterface
    private interface Escritura<T> {
        T ejecutar(int farmacia) throws Exception;
    }

    private <T> T escribir(Escritura<T> operacion)
            throws Exception {

        int farmacia = farmaciaActual();

        try {
            return operacion.ejecutar(farmacia);
        } finally {
            // Se descarta tambien ante errores.
            // Invalidar no significa que la operacion
            // haya sido exitosa.
            InventarioConsultas.invalidar();
        }
    }

    // ==================================================
    // ASEO
    // ==================================================

    public int registrarAseo(Aseo producto) throws Exception {
        return escribir(f ->
                aseoDAO.guardar(producto, f, idUsuario));
    }

    public RegistroCatalogo<Aseo> buscarAseo(int id)
            throws Exception {

        return aseoDAO.buscarPorId(id, farmaciaActual());
    }

    public List<RegistroCatalogo<Aseo>> listarAseo()
            throws Exception {

        return aseoDAO.listarPorFarmacia(farmaciaActual());
    }

    public boolean actualizarAseo(int id, Aseo producto)
            throws Exception {

        return escribir(f ->
                aseoDAO.actualizar(id, producto, f, idUsuario));
    }

    public boolean desactivarAseo(int id) throws Exception {
        return escribir(f -> {

            var existente = aseoDAO.buscarPorId(id, f);

            if (existente == null) {
                return false;
            }

            if (existente.producto().getStock() > 0) {
                throw new ReglaNegocioException(
                        "No se puede desactivar un producto con stock."
                );
            }

            return aseoDAO.desactivar(id, f, idUsuario);
        });
    }

    // ==================================================
    // MATERNIDAD
    // ==================================================

    public int registrarMaternidad(Maternidad producto)
            throws Exception {

        return escribir(f ->
                maternidadDAO.guardar(producto, f, idUsuario));
    }

    public RegistroCatalogo<Maternidad> buscarMaternidad(int id)
            throws Exception {

        return maternidadDAO.buscarPorId(id, farmaciaActual());
    }

    public List<RegistroCatalogo<Maternidad>> listarMaternidad()
            throws Exception {

        return maternidadDAO.listarPorFarmacia(
                farmaciaActual()
        );
    }

    public boolean actualizarMaternidad(
            int id,
            Maternidad producto
    ) throws Exception {

        return escribir(f ->
                maternidadDAO.actualizar(
                        id, producto, f, idUsuario
                ));
    }

    public boolean desactivarMaternidad(int id)
            throws Exception {

        return escribir(f -> {

            var existente = maternidadDAO.buscarPorId(id, f);

            if (existente == null) {
                return false;
            }

            if (existente.producto().getStock() > 0) {
                throw new ReglaNegocioException(
                        "No se puede desactivar un producto con stock."
                );
            }

            return maternidadDAO.desactivar(
                    id, f, idUsuario
            );
        });
    }

    // ==================================================
    // MEDICAMENTOS
    // ==================================================

    public Medicamento registrarMedicamento(
            Medicamento producto
    ) throws Exception {

        return escribir(f -> {

            if (producto == null) {
                throw new IllegalArgumentException(
                        "El medicamento es obligatorio"
                );
            }

            producto.setIdFarmacia(f);
            producto.setIdUsuarioCreacion(idUsuario);
            producto.setStockTotal(0);

            if (producto.getEstado() == null
                    || producto.getEstado().isBlank()) {

                producto.setEstado("ACTIVO");
            }

            if (producto.getIdCategoria() == null
                    || producto.getIdCategoria() <= 0) {

                throw new ReglaNegocioException(
                        "Categoria obligatoria para medicamento"
                );
            }

            if (producto.getPrincipioActivo() == null
                    || producto.getPrincipioActivo().isBlank()) {

                throw new ReglaNegocioException(
                        "Principio activo obligatorio"
                );
            }

            MedicamentoCatalogoDAO.validarCampos(producto);

            medicamentoDAO.guardar(producto);

            return producto;
        });
    }

    public List<RegistroCatalogo<Medicamento>>
    listarMedicamentos() throws Exception {

        List<RegistroCatalogo<Medicamento>> resultado =
                new ArrayList<>();

        for (Medicamento m :
                medicamentoCatalogoDAO.listarPorFarmacia(
                        farmaciaActual()
                )) {

            resultado.add(new RegistroCatalogo<>(
                    m.getIdMedicamento(),
                    m,
                    m.getEstado()
            ));
        }

        return resultado;
    }

    public boolean actualizarMedicamento(
            int id,
            Medicamento producto
    ) throws Exception {

        return escribir(f ->
                medicamentoCatalogoDAO.actualizar(
                        id, producto, f, idUsuario
                ));
    }

    public boolean desactivarMedicamento(int id)
            throws Exception {

        return escribir(f ->
                medicamentoCatalogoDAO.desactivar(
                        id, f, idUsuario
                ));
    }

    // ==================================================
    // CATEGORIAS
    // ==================================================

    public Map<Integer, String> listarCategoriasMedicamento()
            throws Exception {

        farmaciaActual();
        return medicamentoDAO.listarCategorias();
    }

    public Map<Integer, String> categoriaPorMedicamento()
            throws Exception {

        Map<Integer, String> categorias =
                listarCategoriasMedicamento();

        Map<Integer, String> resultado =
                new HashMap<>();

        for (var registro : listarMedicamentos()) {

            Medicamento m = registro.producto();

            resultado.put(
                    m.getIdMedicamento(),
                    categorias.get(m.getIdCategoria())
            );
        }

        return resultado;
    }

    // ==================================================
    // LOTES Y DISPENSACION
    // ==================================================

    public List<Lote> listarLotes() throws Exception {
        int idFarmacia = farmaciaActual();

        int actualizados = controlVencimientosService
                .sincronizar(idFarmacia, idUsuario);

        if (actualizados > 0) {
            InventarioConsultas.invalidarFarmacia(idFarmacia);
        }

        return loteDAO.listarPorFarmacia(idFarmacia);
    }

    public Lote registrarLote(Lote lote) throws Exception {

        return escribir(f ->
                registroLoteService.registrarMedicamentoLote(
                        lote, idUsuario, f
                ));
    }

    public void dispensarMedicamento(
            int idLote,
            int cantidad
    ) throws Exception {

        escribir(f -> {

            farmacovigilanciaService.dispensarMedicamento(
                    idLote,
                    cantidad,
                    f,
                    idUsuario
            );

            return null;
        });
    }

    // ==================================================
    // CONTADORES DEL INVENTARIO UNIFICADO
    // CORRECCION DEL ERROR DEL MERGE
    // ==================================================

    /**
     * Obtiene el total de productos por categoria
     * de la farmacia activa.
     *
     * Reutiliza la consulta unificada con Caching Proxy.
     */
    public Map<String, Integer> contarPorCategoria()
            throws Exception {

        Map<String, Integer> conteo =
                new LinkedHashMap<>();

        conteo.put("Medicamento", 0);
        conteo.put("Aseo", 0);
        conteo.put("Maternidad", 0);

        for (ProductoInventario producto :
                listarInventarioUnificado()) {

            String categoria = producto.categoria();

            if (conteo.containsKey(categoria)) {
                conteo.merge(
                        categoria,
                        1,
                        Integer::sum
                );
            }
        }

        return conteo;
    }

    // ==================================================
    // CONSULTA UNIFICADA CON PATRON GOF PROXY
    // ==================================================

    /**
     * Recupera el inventario correspondiente
     * a la farmacia activa.
     */
    public List<ProductoInventario> listarInventarioUnificado()
            throws Exception {

        return consulta.listarPorFarmacia(
                farmaciaActual()
        );
    }

    /**
     * Fuerza una nueva consulta del inventario,
     * invalidando previamente la cache.
     */
    public List<ProductoInventario> actualizarInventarioUnificado()
            throws Exception {

        int farmacia = farmaciaActual();

        InventarioConsultas.invalidar();

        return consulta.listarPorFarmacia(farmacia);
    }
}
