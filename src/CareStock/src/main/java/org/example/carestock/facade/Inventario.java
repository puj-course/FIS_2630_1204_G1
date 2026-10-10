package org.example.carestock.facade;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.dao.AseoDAO;
import org.example.carestock.dao.MaternidadDAO;
import org.example.carestock.dao.MedicamentoDAO;
import org.example.carestock.dao.MedicamentoDAOImpl;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;
import org.example.carestock.model.Lote;
import org.example.carestock.model.Usuario;
import org.example.carestock.service.RegistroLoteService;
import org.example.carestock.service.FarmacovigilanciaService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Fachada GoF: los controladores no requieren conocer los DAO concretos.
 * NOTA: conectar a una sesion autenticada y comprobar permisos por rol
 * antes de habilitar escrituras desde JavaFX.
 */
public class Inventario {

    private final int idUsuario;
    private final AseoDAO aseoDAO = new AseoDAO();
    private final MaternidadDAO maternidadDAO = new MaternidadDAO();
    private final MedicamentoDAO medicamentoDAO = new MedicamentoDAOImpl();
    private final RegistroLoteService registroLoteService = new RegistroLoteService();
    private final FarmacovigilanciaService farmacovigilanciaService =
            new FarmacovigilanciaService();

    public Inventario(Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Se requiere un usuario autenticado");
        }
        this.idUsuario = usuarioAutenticado.getIdUsuario();
    }

    /** La farmacia se consulta a BD, no se recibe desde un formulario. */
    private int farmaciaActual() throws Exception {
        String sql = "SELECT u.id_farmacia FROM usuarios u "
                + "JOIN farmacias f ON f.id_farmacia = u.id_farmacia "
                + "WHERE u.id_usuario = ? AND UPPER(u.estado) = 'ACTIVO' "
                + "AND UPPER(f.estado) = 'ACTIVA'";
        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Usuario inactivo o sin farmacia activa asignada");
                }
                return rs.getInt("id_farmacia");
            }
        }
    }

    public int registrarAseo(Aseo producto) throws Exception {
        return aseoDAO.guardar(producto, farmaciaActual(), idUsuario);
    }

    public int registrarMaternidad(Maternidad producto) throws Exception {
        return maternidadDAO.guardar(producto, farmaciaActual(), idUsuario);
    }

    public RegistroCatalogo<Aseo> buscarAseo(int idAseo) throws Exception {
        return aseoDAO.buscarPorId(idAseo, farmaciaActual());
    }

    public RegistroCatalogo<Maternidad> buscarMaternidad(int idMaternidad)
            throws Exception {
        return maternidadDAO.buscarPorId(idMaternidad, farmaciaActual());
    }

    public List<RegistroCatalogo<Aseo>> listarAseo() throws Exception {
        return aseoDAO.listarPorFarmacia(farmaciaActual());
    }

    public List<RegistroCatalogo<Maternidad>> listarMaternidad() throws Exception {
        return maternidadDAO.listarPorFarmacia(farmaciaActual());
    }

    public boolean actualizarAseo(int idAseo, Aseo producto) throws Exception {
        return aseoDAO.actualizar(idAseo, producto, farmaciaActual(), idUsuario);
    }

    public boolean actualizarMaternidad(int idMaternidad,
                                        Maternidad producto) throws Exception {
        return maternidadDAO.actualizar(
                idMaternidad, producto, farmaciaActual(), idUsuario);
    }

    public boolean desactivarAseo(int idAseo) throws Exception {
        return aseoDAO.desactivar(idAseo, farmaciaActual(), idUsuario);
    }

    public boolean desactivarMaternidad(int idMaternidad) throws Exception {
        return maternidadDAO.desactivar(idMaternidad, farmaciaActual(), idUsuario);
    }

    public Medicamento registrarMedicamento(Medicamento producto) throws Exception {
        if (producto == null) {
            throw new IllegalArgumentException("El medicamento es obligatorio");
        }
        producto.setIdFarmacia(farmaciaActual());
        producto.setIdUsuarioCreacion(idUsuario);
        producto.setStockTotal(0);
        if (producto.getEstado() == null || producto.getEstado().isBlank()) {
            producto.setEstado("ACTIVO");
        }
        if (producto.getIdCategoria() == null || producto.getIdCategoria() <= 0) {
            throw new ReglaNegocioException("Categoria obligatoria para medicamento");
        }
        if (producto.getPrincipioActivo() == null ||
                producto.getPrincipioActivo().isBlank()) {
            throw new ReglaNegocioException("Principio activo obligatorio");
        }
        producto.validar();
        medicamentoDAO.guardar(producto);
        return producto;
    }

    /** Los lotes de aseo y maternidad requieren una migracion posterior. */
    public Lote registrarLote(Lote lote) throws Exception {
        return registroLoteService.registrarMedicamentoLote(
                lote, idUsuario, farmaciaActual());
    }

    public void dispensarMedicamento(int idLote, int cantidad) throws Exception {
        farmacovigilanciaService.dispensarMedicamento(
                idLote, cantidad, farmaciaActual(), idUsuario);
    }
}
