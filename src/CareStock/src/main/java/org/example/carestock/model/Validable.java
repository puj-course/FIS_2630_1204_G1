package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public interface Validable {
    /**
     * nuestro metodo que revisa si alguna clase tiene un dato mal
     * arroja la excepcion personalizada que se implementa en cada clase
     * y se detiene la operacion
     * @throws ReglaNegocioException
     */
    void validar() throws ReglaNegocioException;

    /**
     * Para cualquier insumo medico dentro de carestock dira si es o no
     * apto para dispensar
     * @return
     */
    boolean esAptoParaDispensar();
}