package org.example.carestock.service;

import org.example.carestock.dao.AlertasInventarioDao;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.session.SesionUsuario;

import java.util.List;
import java.util.Objects;

public final class ServicioAlertasInventario {

    public record Resumen(int criticas, int advertencias, int stockMinimo, int vencimiento) {

        public int total() {
            return criticas + advertencias;
        }
    }

    private final AlertasInventarioDao dao;

    public ServicioAlertasInventario() {
        this(new AlertasInventarioDao());
    }

    ServicioAlertasInventario(AlertasInventarioDao dao) {
        this.dao = Objects.requireNonNull(dao, "El DAO de alertas es obligatorio");
    }

    public List<AlertaInventario> listar(int idFarmacia) throws Exception {
        if (idFarmacia <= 0) {
            throw new IllegalArgumentException("Farmacia inválida");
        }

        exigirSesion(idFarmacia);
        List<AlertaInventario> alertas = dao.listarPorFarmacia(idFarmacia);
        exigirSesion(idFarmacia);

        return alertas;
    }

    public static Resumen resumir(List<AlertaInventario> alertas) {
        int criticas = 0;
        int advertencias = 0;
        int stock = 0;
        int vencimiento = 0;

        for (AlertaInventario alerta : alertas) {
            if (alerta.severidad() == Severidad.CRITICA) {
                criticas++;
            } else {
                advertencias++;
            }
            if (alerta.tipoAlerta() == TipoAlerta.STOCK_MINIMO) {
                stock++;
            } else {
                vencimiento++;
            }
        }

        return new Resumen(criticas, advertencias, stock, vencimiento);
    }

    public static List<AlertaInventario> filtrar(
            List<AlertaInventario> alertas,
            TipoAlerta tipo,
            Categoria categoria,
            Severidad severidad
    ) {
        return alertas.stream()
                .filter(a -> tipo == null || a.tipoAlerta() == tipo)
                .filter(a -> categoria == null || a.categoria() == categoria)
                .filter(a -> severidad == null || a.severidad() == severidad)
                .toList();
    }

    static void exigirSesion(int idFarmacia) throws ReglaNegocioException {
        if (!SesionUsuario.estaActiva()) {
            throw new ReglaNegocioException("Debe iniciar sesión.");
        }

        Integer farmaciaSesion = SesionUsuario.actual().idFarmacia();
        if (!Objects.equals(farmaciaSesion, idFarmacia)) {
            throw new ReglaNegocioException(
                    "La farmacia no corresponde a la sesión activa.");
        }
    }
}
