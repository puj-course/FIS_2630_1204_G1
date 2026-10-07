package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

/**
 * Modela los dispositivos médicos e insumos de la droguería/IPS (jeringas, gasas, guantes, etc.)
 * según la regulación INVIMA (Decreto 4725 de 2005).
 */
public class DispositivoMedico implements Validable {

    public enum ClasificacionRiesgo {
        CLASE_I,   // Bajo riesgo (ej. gasas, baja lenguas)
        CLASE_IIA, // Riesgo moderado (ej. jeringas, agujas, cánulas)
        CLASE_IIB, // Riesgo alto (ej. catéteres, preservativos)
        CLASE_III  // Muy alto riesgo (ej. implantes, marcapasos)
    }

    private String id;
    private String nombre;
    private String registroSanitario; // Registro INVIMA tipo DM-XXXXX
    private ClasificacionRiesgo riesgo;
    private boolean esEsteril;
    private boolean esReutilizable;
    private boolean tieneAlertaActiva;

    public DispositivoMedico() {}

    public DispositivoMedico(String id, String nombre, String registroSanitario,
                             ClasificacionRiesgo riesgo, boolean esEsteril, boolean esReutilizable) {
        this.id = id;
        this.nombre = nombre;
        this.registroSanitario = registroSanitario;
        this.riesgo = riesgo;
        this.esEsteril = esEsteril;
        this.esReutilizable = esReutilizable;
        this.tieneAlertaActiva = false;
    }

    @Override
    public void validar() throws ReglaNegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre del dispositivo médico es obligatorio.");
        }
        if (registroSanitario == null || registroSanitario.trim().isEmpty()) {
            throw new ReglaNegocioException("El dispositivo médico debe contar con Registro Sanitario INVIMA o Permiso de Comercialización.");
        }
        if (riesgo == null) {
            throw new ReglaNegocioException("Debe especificar la clasificación de riesgo del dispositivo según normativa INVIMA.");
        }
        if (tieneAlertaActiva) {
            throw new ReglaNegocioException("BLOQUEO SANITARIO: El dispositivo médico " + nombre + " presenta una alerta o retiro del mercado.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return !tieneAlertaActiva && registroSanitario != null && !registroSanitario.trim().isEmpty();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRegistroSanitario() { return registroSanitario; }
    public void setRegistroSanitario(String registroSanitario) { this.registroSanitario = registroSanitario; }

    public ClasificacionRiesgo getRiesgo() { return riesgo; }
    public void setRiesgo(ClasificacionRiesgo riesgo) { this.riesgo = riesgo; }

    public boolean isEsEsteril() { return esEsteril; }
    public void setEsEsteril(boolean esEsteril) { this.esEsteril = esEsteril; }

    public boolean isEsReutilizable() { return esReutilizable; }
    public void setEsReutilizable(boolean esReutilizable) { this.esReutilizable = esReutilizable; }

    public boolean isTieneAlertaActiva() { return tieneAlertaActiva; }
    public void setTieneAlertaActiva(boolean tieneAlertaActiva) { this.tieneAlertaActiva = tieneAlertaActiva; }
}