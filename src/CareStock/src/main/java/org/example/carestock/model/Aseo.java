package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Aseo implements Validable {

    private int idAseo;
    private Integer idFarmacia;
    private Integer idUsuarioCreacion;
    private String estado = "ACTIVO";
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;

    // Atributos específicos de aseo
    private String tipoAseo;
    private boolean biodegradable;
    private String componentesActivos;

    public Aseo() {
    }

    // =========================================
    // REGLAS DE NEGOCIO - VALIDABLE
    // =========================================

    @Override
    public void validar() throws ReglaNegocioException {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException(
                    "El codigo del producto de aseo es obligatorio."
            );
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException(
                    "El nombre del producto de aseo es obligatorio."
            );
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException(
                    "La descripcion del producto de aseo es obligatoria."
            );
        }
        if (!Double.isFinite(precio) || precio < 0) {
            throw new ReglaNegocioException(
                    "El precio del producto de aseo debe ser valido y no negativo."
            );
        }
        if (estado == null ||
                !("ACTIVO".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado))) {
            throw new ReglaNegocioException("El estado debe ser ACTIVO o INACTIVO.");
        }
        if (stock < 0) {
            throw new ReglaNegocioException(
                    "El stock del producto de aseo no puede ser negativo."
            );
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        try {
            validar();
            return stock > 0 && "ACTIVO".equalsIgnoreCase(estado);
        } catch (ReglaNegocioException e) {
            return false;
        }
    }

    // =========================================
    // GETTERS Y SETTERS
    // =========================================

    public int getIdAseo() {
        return idAseo;
    }

    public void setIdAseo(int idAseo) {
        this.idAseo = idAseo;
    }

    public Integer getIdFarmacia() {
        return idFarmacia;
    }

    public void setIdFarmacia(Integer idFarmacia) {
        this.idFarmacia = idFarmacia;
    }

    public Integer getIdUsuarioCreacion() {
        return idUsuarioCreacion;
    }

    public void setIdUsuarioCreacion(Integer idUsuarioCreacion) {
        this.idUsuarioCreacion = idUsuarioCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getTipoAseo() {
        return tipoAseo;
    }

    public void setTipoAseo(String tipoAseo) {
        this.tipoAseo = tipoAseo;
    }

    public boolean isBiodegradable() {
        return biodegradable;
    }

    public void setBiodegradable(boolean biodegradable) {
        this.biodegradable = biodegradable;
    }

    public String getComponentesActivos() {
        return componentesActivos;
    }

    public void setComponentesActivos(String componentesActivos) {
        this.componentesActivos = componentesActivos;
    }
}