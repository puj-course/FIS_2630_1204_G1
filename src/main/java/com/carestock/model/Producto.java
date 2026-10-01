package com.carestock.model;

/**
 * Interfaz Producto en CARESTOCK.
 * Contrato base para los productos gestionados por CareStock.
 *
 * Define las operaciones comunes que debe proporcionar cualquier
 * tipo de producto del sistema, sin imponer una implementación (Por que eso debe hacer una interfaz)
 * concreta ni almacenar estado.
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