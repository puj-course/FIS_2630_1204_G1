package org.example.carestock.service;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.proxy.ConsultaInventario;
import org.example.carestock.proxy.InventarioConsultas;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * HU.137 / #989: consulta las necesidades de reposicion sin cambiar inventario.
 *
 * <p>La vista de alertas vigente es la autoridad para el umbral, la existencia y
 * la severidad (las tres categorias). El proxy de inventario suministra detalles
 * del catalogo y solo complementa un codigo faltante; al tener cache con TTL,
 * nunca sustituye los valores de stock y minimo de la alerta.</p>
 *
 * <p>Un minimo persistido igual a cero NO se interpreta como un minimo inventado:
 * la base actual utiliza 0 por defecto y no distingue "sin configurar" de 0
 * explicito. Por compatibilidad con la vista, un producto agotado con minimo 0
 * aparece critico con faltante 0. Los productos con existencias positivas
 * y minimo 0 no generan reposicion.</p>
 */
public final class ServicioPlanificacionReposicion {

    private record ClaveProducto(Categoria categoria, int idProducto) { }

    private static final Comparator<RegistroReposicion> ORDEN = Comparator
            .comparing((RegistroReposicion registro) -> registro.severidad() != Severidad.CRITICA)
            .thenComparing(RegistroReposicion::categoria)
            .thenComparing(RegistroReposicion::nombre, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(RegistroReposicion::idProducto);

    private final ServicioAlertasInventario servicioAlertas;
    private final ConsultaInventario consultaInventario;

    public ServicioPlanificacionReposicion() {
        this(new ServicioAlertasInventario(), InventarioConsultas.compartida());
    }

    ServicioPlanificacionReposicion(
            ServicioAlertasInventario servicioAlertas,
            ConsultaInventario consultaInventario
    ) {
        this.servicioAlertas = Objects.requireNonNull(servicioAlertas, "El servicio de alertas es obligatorio");
        this.consultaInventario = Objects.requireNonNull(consultaInventario, "La consulta de inventario es obligatoria");
    }

    public List<RegistroReposicion> listarPorFarmacia(int idFarmacia) throws Exception {
        if (idFarmacia <= 0) {
            throw new IllegalArgumentException("Farmacia invalida");
        }
        ServicioAlertasInventario.exigirSesion(idFarmacia);

        // La vista es la fuente de verdad de los stocks y de las reglas de alerta.
        List<AlertaInventario> alertas = servicioAlertas.listar(idFarmacia);
        List<ProductoInventario> inventario = consultaInventario.listarPorFarmacia(idFarmacia);
        ServicioAlertasInventario.exigirSesion(idFarmacia);

        Map<ClaveProducto, ProductoInventario> catalogo = new LinkedHashMap<>();
        for (ProductoInventario producto : inventario) {
            Categoria categoria = convertirCategoria(producto.categoria());
            if (categoria != null) {
                catalogo.putIfAbsent(new ClaveProducto(categoria, producto.id()), producto);
            }
        }

        Map<ClaveProducto, RegistroReposicion> necesidades = new LinkedHashMap<>();
        for (AlertaInventario alerta : ServicioAlertasInventario.filtrar(
                alertas, TipoAlerta.STOCK_MINIMO, null, null)) {
            Integer minimo = alerta.umbral();
            if (minimo == null) {
                // Sin umbral conocido: no se asigna ningun numero ficticio.
                continue;
            }
            if (minimo < 0) {
                throw new IllegalStateException("Stock minimo negativo en alertas");
            }
            if (alerta.existencias() > minimo) {
                // Defensa ante datos desactualizados o inconsistentes.
                continue;
            }

            ClaveProducto clave = new ClaveProducto(alerta.categoria(), alerta.idReferencia());
            ProductoInventario producto = catalogo.get(clave);
            String codigo = alerta.codigo();
            if ((codigo == null || codigo.isBlank()) && producto != null) {
                codigo = producto.codigo();
            }

            int faltante = Math.max(0, minimo - alerta.existencias());
            Severidad severidad = alerta.existencias() == 0
                    ? Severidad.CRITICA : Severidad.ADVERTENCIA;
            RegistroReposicion registro = new RegistroReposicion(
                    alerta.categoria(), alerta.idReferencia(), codigo, alerta.producto(),
                    alerta.existencias(), minimo, faltante, severidad);
            necesidades.putIfAbsent(clave, registro);
        }

        ServicioAlertasInventario.exigirSesion(idFarmacia);
        return necesidades.values().stream().sorted(ORDEN).toList();
    }

    private static Categoria convertirCategoria(String valor) {
        if (valor == null) {
            return null;
        }
        return switch (valor.trim().toUpperCase(Locale.ROOT)) {
            case "MEDICAMENTO" -> Categoria.MEDICAMENTO;
            case "ASEO" -> Categoria.ASEO;
            case "MATERNIDAD" -> Categoria.MATERNIDAD;
            default -> null;
        };
    }
}
