package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Medicamento implements Validable {

    private int idMedicamento;
    private String codigoInvima;
    private String nombreComercial;
    private String principioActivo;
    private String concentracion;
    private String formaFarmaceutica;
    private Integer idCategoria;
    private int stockMinimo;
    private int stockTotal;
    private String estado;
    private Integer idFarmacia;
    private String presentacion;
    private Integer idUsuarioCreacion;
    private Integer idUsuarioModificacion;
    private LocalDateTime fechaModificacion;
    // Precio opcional en registros historicos; en nuevos registros se captura desde JavaFX.
    private BigDecimal precio;

    public Medicamento() {}

    public Medicamento(String codigoInvima, String nombreComercial, String principioActivo,
                       String concentracion, String formaFarmaceutica, Integer idCategoria,
                       int stockMinimo, int stockTotal, String estado, Integer idFarmacia,
                       String presentacion, Integer idUsuarioCreacion) {
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.concentracion = concentracion;
        this.formaFarmaceutica = formaFarmaceutica;
        this.idCategoria = idCategoria;
        this.stockMinimo = stockMinimo;
        this.stockTotal = stockTotal;
        this.estado = estado;
        this.idFarmacia = idFarmacia;
        this.presentacion = presentacion;
        this.idUsuarioCreacion = idUsuarioCreacion;
    }

    @Override
    public void validar() throws ReglaNegocioException {
        if (nombreComercial == null || nombreComercial.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre comercial del medicamento es obligatorio.");
        }
        if (codigoInvima == null || codigoInvima.trim().isEmpty()) {
            throw new ReglaNegocioException("El código/registro INVIMA es obligatorio.");
        }
        if (precio != null && (precio.signum() < 0 || precio.scale() > 2 ||
                precio.precision() - precio.scale() > 10)) {
            throw new ReglaNegocioException(
                    "El precio debe ser positivo o cero, con maximo dos decimales y 10 digitos enteros.");
        }
        if (stockMinimo < 0 || stockTotal < 0) {
            throw new ReglaNegocioException("Los valores de inventario (stock) no pueden ser negativos.");
        }
        if ("BLOQUEADO".equalsIgnoreCase(estado)) {
            throw new ReglaNegocioException("BLOQUEO SANITARIO: El medicamento " + nombreComercial + " se encuentra bloqueado.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return "ACTIVO".equalsIgnoreCase(estado)
                && stockTotal > 0
                && codigoInvima != null
                && !codigoInvima.trim().isEmpty();
    }


    /**
     * Punto de entrada para inicializar el Builder desde el cliente.
     */
    public static MedicamentoBuilder builder() {
        return new MedicamentoBuilder();
    }

    public static class MedicamentoBuilder {
        private final Medicamento medicamento;

        public MedicamentoBuilder() {
            this.medicamento = new Medicamento();
        }

        public MedicamentoBuilder idMedicamento(int idMedicamento) {
            medicamento.setIdMedicamento(idMedicamento);
            return this;
        }

        public MedicamentoBuilder codigoInvima(String codigoInvima) {
            medicamento.setCodigoInvima(codigoInvima);
            return this;
        }

        public MedicamentoBuilder nombreComercial(String nombreComercial) {
            medicamento.setNombreComercial(nombreComercial);
            return this;
        }

        public MedicamentoBuilder principioActivo(String principioActivo) {
            medicamento.setPrincipioActivo(principioActivo);
            return this;
        }

        public MedicamentoBuilder concentracion(String concentracion) {
            medicamento.setConcentracion(concentracion);
            return this;
        }

        public MedicamentoBuilder formaFarmaceutica(String formaFarmaceutica) {
            medicamento.setFormaFarmaceutica(formaFarmaceutica);
            return this;
        }

        public MedicamentoBuilder idCategoria(Integer idCategoria) {
            medicamento.setIdCategoria(idCategoria);
            return this;
        }

        public MedicamentoBuilder stockMinimo(int stockMinimo) {
            medicamento.setStockMinimo(stockMinimo);
            return this;
        }

        public MedicamentoBuilder stockTotal(int stockTotal) {
            medicamento.setStockTotal(stockTotal);
            return this;
        }

        public MedicamentoBuilder estado(String estado) {
            medicamento.setEstado(estado);
            return this;
        }

        public MedicamentoBuilder idFarmacia(Integer idFarmacia) {
            medicamento.setIdFarmacia(idFarmacia);
            return this;
        }

        public MedicamentoBuilder presentacion(String presentacion) {
            medicamento.setPresentacion(presentacion);
            return this;
        }

        public MedicamentoBuilder precio(BigDecimal precio) {
            medicamento.setPrecio(precio);
            return this;
        }

        public MedicamentoBuilder idUsuarioCreacion(Integer idUsuarioCreacion) {
            medicamento.setIdUsuarioCreacion(idUsuarioCreacion);
            return this;
        }

        /**
         * Método final que construye y retorna la instancia de Medicamento validada.
         */
        public Medicamento build() {
            return this.medicamento;
        }
    }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(int idMedicamento) { this.idMedicamento = idMedicamento; }

    public String getCodigoInvima() { return codigoInvima; }
    public void setCodigoInvima(String codigoInvima) { this.codigoInvima = codigoInvima; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }

    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }

    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public int getStockTotal() { return stockTotal; }
    public void setStockTotal(int stockTotal) { this.stockTotal = stockTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIdFarmacia() { return idFarmacia; }
    public void setIdFarmacia(Integer idFarmacia) { this.idFarmacia = idFarmacia; }

    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

    public Integer getIdUsuarioCreacion() { return idUsuarioCreacion; }
    public void setIdUsuarioCreacion(Integer idUsuarioCreacion) { this.idUsuarioCreacion = idUsuarioCreacion; }

    public Integer getIdUsuarioModificacion() { return idUsuarioModificacion; }
    public void setIdUsuarioModificacion(Integer idUsuarioModificacion) { this.idUsuarioModificacion = idUsuarioModificacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}