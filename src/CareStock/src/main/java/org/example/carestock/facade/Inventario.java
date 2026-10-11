
package org.example.carestock.facade;

// ==========================================
// DATA TRANSFER OBJECTS
// ==========================================

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.DataTransferObject.RegistroCatalogo;

// ==========================================
// CONFIGURACION
// ==========================================

import org.example.carestock.config.ConexionBD;

// ==========================================
// DAO
// ==========================================

import org.example.carestock.dao.AseoDAO;
import org.example.carestock.dao.MaternidadDAO;
import org.example.carestock.dao.MedicamentoDAO;
import org.example.carestock.dao.MedicamentoDAOImpl;
import org.example.carestock.dao.MedicamentoCatalogoDAO;
import org.example.carestock.dao.LoteDAO;
import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.dao.ConsultaInventarioDAO;

// ==========================================
// EXCEPCIONES
// ==========================================

import org.example.carestock.exception.ReglaNegocioException;

// ==========================================
// MODELOS
// ==========================================

import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;
import org.example.carestock.model.Lote;
import org.example.carestock.model.Usuario;

// ==========================================
// SERVICIOS
// ==========================================

import org.example.carestock.service.RegistroLoteService;
import org.example.carestock.service.FarmacovigilanciaService;

// ==========================================
// JAVA
// ==========================================

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * FACHADA DE INVENTARIO - PATRON GOF FACADE
 *
 * Proporciona un punto de acceso simplificado
 * a las operaciones del inventario de CareStock.
 *
 * Integra:
 *
 * - Medicamentos
 * - Productos de aseo
 * - Productos de maternidad
 * - Lotes
 * - Dispensacion
 * - Consulta unificada de inventario
 *
 * Los controladores JavaFX no necesitan
 * interactuar directamente con los DAO.
 *
 * Las operaciones del catalogo estan disponibles
 * para todos los usuarios autenticados.
 *
 * Cada operacion respeta la farmacia asignada
 * al usuario.
 */
public class Inventario {

    // ==========================================
    // CONTEXTO DE USUARIO
    // ==========================================

    private final int idUsuario;

    // ==========================================
    // DEPENDENCIAS DAO
    // ==========================================

    private final AseoDAO aseoDAO =
            new AseoDAO();

    private final MaternidadDAO maternidadDAO =
            new MaternidadDAO();

    private final MedicamentoDAO medicamentoDAO =
            new MedicamentoDAOImpl();

    private final MedicamentoCatalogoDAO medicamentoCatalogoDAO =
            new MedicamentoCatalogoDAO();

    private final LoteDAO loteDAO =
            new LoteDAOImpl();

    private final ConsultaInventarioDAO consultaInventarioDAO =
            new ConsultaInventarioDAO();

    // ==========================================
    // DEPENDENCIAS SERVICE
    // ==========================================

    private final RegistroLoteService registroLoteService =
            new RegistroLoteService();

    private final FarmacovigilanciaService farmacovigilanciaService =
            new FarmacovigilanciaService();

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Inventario(Usuario usuarioAutenticado) {

        if (usuarioAutenticado == null ||
                usuarioAutenticado.getIdUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "Se requiere un usuario autenticado"
            );
        }

        this.idUsuario =
                usuarioAutenticado.getIdUsuario();
    }

    // ==========================================
    // FARMACIA ACTIVA
    // ==========================================

    /**
     * Obtiene la farmacia asignada al usuario.
     *
     * La farmacia se consulta directamente
     * en PostgreSQL.
     *
     * No se recibe desde los formularios JavaFX.
     */
    private int farmaciaActual() throws Exception {

        String sql = """
                SELECT u.id_farmacia
                FROM usuarios u
                INNER JOIN farmacias f
                    ON f.id_farmacia = u.id_farmacia
                WHERE u.id_usuario = ?
                  AND UPPER(TRIM(u.estado)) = 'ACTIVO'
                  AND UPPER(TRIM(f.estado)) = 'ACTIVA'
                """;

        try (Connection conexion =
                     ConexionBD.getConexion();

             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new ReglaNegocioException(
                            "Usuario inactivo o sin farmacia "
                                    + "activa asignada"
                    );
                }

                return rs.getInt("id_farmacia");
            }
        }
    }

    // ==========================================
    // CRUD DE ASEO
    // ==========================================

    /**
     * Registrar producto de aseo.
     */
    public int registrarAseo(Aseo producto)
            throws Exception {

        return aseoDAO.guardar(
                producto,
                farmaciaActual(),
                idUsuario
        );
    }

    /**
     * Buscar producto de aseo.
     */
    public RegistroCatalogo<Aseo> buscarAseo(
            int idAseo
    ) throws Exception {

        return aseoDAO.buscarPorId(
                idAseo,
                farmaciaActual()
        );
    }

    /**
     * Listar productos de aseo
     * de la farmacia activa.
     */
    public List<RegistroCatalogo<Aseo>> listarAseo()
            throws Exception {

        return aseoDAO.listarPorFarmacia(
                farmaciaActual()
        );
    }

    /**
     * Actualizar producto de aseo.
     */
    public boolean actualizarAseo(
            int idAseo,
            Aseo producto
    ) throws Exception {

        return aseoDAO.actualizar(
                idAseo,
                producto,
                farmaciaActual(),
                idUsuario
        );
    }

    /**
     * Desactivar producto de aseo.
     */
    public boolean desactivarAseo(
            int idAseo
    ) throws Exception {

        RegistroCatalogo<Aseo> existente =
                buscarAseo(idAseo);

        if (existente == null) {
            return false;
        }

        if (existente.producto().getStock() > 0) {

            throw new ReglaNegocioException(
                    "No se puede desactivar un producto "
                            + "con stock. Regularice las existencias."
            );
        }

        return aseoDAO.desactivar(
                idAseo,
                farmaciaActual(),
                idUsuario
        );
    }

    // ==========================================
    // CRUD DE MATERNIDAD
    // ==========================================

    /**
     * Registrar producto de maternidad.
     */
    public int registrarMaternidad(
            Maternidad producto
    ) throws Exception {

        return maternidadDAO.guardar(
                producto,
                farmaciaActual(),
                idUsuario
        );
    }

    /**
     * Buscar producto de maternidad.
     */
    public RegistroCatalogo<Maternidad> buscarMaternidad(
            int idMaternidad
    ) throws Exception {

        return maternidadDAO.buscarPorId(
                idMaternidad,
                farmaciaActual()
        );
    }

    /**
     * Listar productos de maternidad.
     */
    public List<RegistroCatalogo<Maternidad>> listarMaternidad()
            throws Exception {

        return maternidadDAO.listarPorFarmacia(
                farmaciaActual()
        );
    }

    /**
     * Actualizar producto de maternidad.
     */
    public boolean actualizarMaternidad(
            int idMaternidad,
            Maternidad producto
    ) throws Exception {

        return maternidadDAO.actualizar(
                idMaternidad,
                producto,
                farmaciaActual(),
                idUsuario
        );
    }

    /**
     * Desactivar producto de maternidad.
     */
    public boolean desactivarMaternidad(
            int idMaternidad
    ) throws Exception {

        RegistroCatalogo<Maternidad> existente =
                buscarMaternidad(idMaternidad);

        if (existente == null) {
            return false;
        }

        if (existente.producto().getStock() > 0) {

            throw new ReglaNegocioException(
                    "No se puede desactivar un producto "
                            + "con stock. Regularice las existencias."
            );
        }

        return maternidadDAO.desactivar(
                idMaternidad,
                farmaciaActual(),
                idUsuario
        );
    }

    // ==========================================
    // CRUD DE MEDICAMENTOS
    // ==========================================

    /**
     * Registrar medicamento.
     *
     * El medicamento se vincula automaticamente
     * a la farmacia del usuario autenticado.
     *
     * El stock inicial es cero.
     */
    public Medicamento registrarMedicamento(
            Medicamento producto
    ) throws Exception {

        if (producto == null) {

            throw new IllegalArgumentException(
                    "El medicamento es obligatorio"
            );
        }

        producto.setIdFarmacia(
                farmaciaActual()
        );

        producto.setIdUsuarioCreacion(
                idUsuario
        );

        producto.setStockTotal(0);

        if (producto.getEstado() == null ||
                producto.getEstado().isBlank()) {

            producto.setEstado("ACTIVO");
        }

        if (producto.getIdCategoria() == null ||
                producto.getIdCategoria() <= 0) {

            throw new ReglaNegocioException(
                    "Categoria obligatoria para medicamento"
            );
        }

        if (producto.getPrincipioActivo() == null ||
                producto.getPrincipioActivo().isBlank()) {

            throw new ReglaNegocioException(
                    "Principio activo obligatorio"
            );
        }

        MedicamentoCatalogoDAO.validarCampos(
                producto
        );

        medicamentoDAO.guardar(producto);

        return producto;
    }

    //Método contarPorCategoria
    
    public Map<String, Integer> contarPorCategoria() throws Exception {
    return medicamentoDAO.contarPorCategoria();
    }

    /**
     * Listar medicamentos de la farmacia
     * asignada al usuario.
     */
    public List<RegistroCatalogo<Medicamento>>
    listarMedicamentos() throws Exception {

        List<RegistroCatalogo<Medicamento>> registros =
                new ArrayList<>();

        List<Medicamento> medicamentos =
                medicamentoCatalogoDAO.listarPorFarmacia(
                        farmaciaActual()
                );

        for (Medicamento medicamento : medicamentos) {

            registros.add(
                    new RegistroCatalogo<>(
                            medicamento.getIdMedicamento(),
                            medicamento,
                            medicamento.getEstado()
                    )
            );
        }

        return registros;
    }

    /**
     * Actualizar medicamento.
     */
    public boolean actualizarMedicamento(
            int idMedicamento,
            Medicamento producto
    ) throws Exception {

        return medicamentoCatalogoDAO.actualizar(
                idMedicamento,
                producto,
                farmaciaActual(),
                idUsuario
        );
    }

    /**
     * Desactivar medicamento.
     */
    public boolean desactivarMedicamento(
            int idMedicamento
    ) throws Exception {

        return medicamentoCatalogoDAO.desactivar(
                idMedicamento,
                farmaciaActual(),
                idUsuario
        );
    }

    // ==========================================
    // CATEGORIAS DE MEDICAMENTOS
    // ==========================================

    /**
     * Listar categorias farmacologicas.
     */
    public Map<Integer, String> listarCategoriasMedicamento()
            throws Exception {

        farmaciaActual();

        return medicamentoDAO.listarCategorias();
    }

    /**
     * Obtener la categoria de cada medicamento
     * perteneciente a la farmacia activa.
     */
    public Map<Integer, String> categoriaPorMedicamento()
            throws Exception {

        Map<Integer, String> categorias =
                listarCategoriasMedicamento();

        Map<Integer, String> resultado =
                new HashMap<>();

        for (RegistroCatalogo<Medicamento> registro
                : listarMedicamentos()) {

            Medicamento medicamento =
                    registro.producto();

            resultado.put(
                    medicamento.getIdMedicamento(),
                    categorias.get(
                            medicamento.getIdCategoria()
                    )
            );
        }

        return resultado;
    }

    // ==========================================
    // GESTION DE LOTES
    // ==========================================

    /**
     * Listar lotes exclusivamente
     * de la farmacia autenticada.
     */
    public List<Lote> listarLotes()
            throws Exception {

        return loteDAO.listarPorFarmacia(
                farmaciaActual()
        );
    }

    /**
     * Registrar lote de medicamento.
     *
     * Los lotes de aseo y maternidad
     * requieren una migracion posterior.
     */
    public Lote registrarLote(
            Lote lote
    ) throws Exception {

        return registroLoteService
                .registrarMedicamentoLote(
                        lote,
                        idUsuario,
                        farmaciaActual()
                );
    }

    /**
     * Dispensar medicamento.
     *
     * El servicio verifica que el lote
     * pertenece a la farmacia autenticada.
     */
    public void dispensarMedicamento(
            int idLote,
            int cantidad
    ) throws Exception {

        farmacovigilanciaService
                .dispensarMedicamento(
                        idLote,
                        cantidad,
                        farmaciaActual(),
                        idUsuario
                );
    }

    // ==========================================
    // CONSULTA UNIFICADA DE INVENTARIO
    // HISTORIA DE USUARIO
    // ==========================================

    /**
     * Consulta unificada del inventario.
     *
     * Permite obtener:
     *
     * - Medicamentos
     * - Productos de aseo
     * - Productos de maternidad
     *
     * La consulta devuelve exclusivamente
     * los productos vinculados a la farmacia
     * del usuario autenticado.
     *
     * La informacion se obtiene mediante
     * ConsultaInventarioDAO.
     *
     * Patron de diseño: Facade GoF.
     *
     * @return Lista unificada de productos.
     * @throws Exception Si falla la consulta.
     */
    public List<ProductoInventario>
    listarInventarioUnificado()
            throws Exception {

        // Validar usuario y obtener farmacia.
        int idFarmacia = farmaciaActual();

        // Consultar las tres categorias.
        return consultaInventarioDAO
                .listarPorFarmacia(idFarmacia);
    }
}
