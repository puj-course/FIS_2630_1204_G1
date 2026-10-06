package com.carestock.model;
/**
 * Interfaz Producto.
 * base para los productos gestionados por CareStock.
 * Esto nos dice que operaciones si o si debe tener un producto dentro del sistema
 * si no pues perdimos y no sabemos es pero nada de POO
 */
public interface Producto {

    Long getIdMedicamento();
    void setIdMedicamento(Long idMedicamento);

    String getCodigoInvima();
    void setCodigoInvima(String codigoInvima);

    String getNombreComercial();
    void setNombreComercial(String nombreComercial);

    String getPrincipioActivo();
    void setPrincipioActivo(String principioActivo);

    String getConcentracion();
    void setConcentracion(String concentracion);

    String getCategoria();
    void setCategoria(String categoria);

    Integer getStockMinimo();
    void setStockMinimo(Integer stockMinimo);

    Integer getStockTotal();
    void setStockTotal(Integer stockTotal);

    String getEstado();
    void setEstado(String estado);
}