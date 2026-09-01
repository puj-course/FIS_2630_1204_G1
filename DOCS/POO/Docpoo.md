# 📐 Diagrama de Clases del Sistema CareStock

**Issue:** [#41 - Elaboración del Diagrama de Clases del Sistema CareStock](https://github.com/puj-course/FIS_2630_1204_G1/issues/41)  
**Autor:** Mateo Salazar Bogotá (Product Owner)  
**Dominio:** Sistema SaaS de Gestión de Inventarios Médico-Farmacéuticos  

---

## 🎯 Objetivo y Alcance

Este documento define la arquitectura orientada a objetos para el sistema **CareStock**, derivándose directamente del modelo de base de datos relacional (PostgreSQL / NeonDB) y satisfaciendo los requerimientos de las historias de usuario clave (gestión de stock, despacho FEFO y alertas de vencimiento).

---

## 📊 Diagrama de Clases UML (Mermaid)
<p align="center">
  <img src="https://kroki.io/mermaid/svg/eNp1VNtu2zAMfc9XCFqAFU76tF6DIFvWd-zQpQsGY2nSjhZ1J8pJ22X_fbSdrE6yZ02-yEMeHw5J-rE0jR6x4M2f6O_4s6SStZ0iG4w-xP1y-Q45g0v7-n62-7u_3R9-H0b3f_XjS-L6YmD6vU-fFymV7hT6xY1vJmsqG6_6f_eHw2m2dK7l4x5cndm-HnF1V3s11_7y9_X142P39jM2o3e96z5e-eG6h4l96d3I9R6eLg1e_x3w48Q0l-602D5N-dMdf_d2f9w__j6NlM3x8mN13F4KkX90Dk30lWz_k0K-e8i-lW23kym3a-3t_p39Hj719_3-2_9pP-_f_2j7b0O6d38N9b2R-v2IymJ_Sfp02-1y_1T7wF23lO2t7o_uV7_3l519f-zffm5_7B7p_l--9D7s" alt="Diagrama de Clases CareStock" width="100%">
</p>

<details>
  <summary><b>💻 Ver Código Fuente del Diagrama</b></summary>
  <br>

```mermaid
classDiagram
    direction TB

    class Rol {
        -Integer idRol
        -String nombreRol
        -String descripcion
        +Rol(idRol: Integer, nombreRol: String)
        +getIdRol() Integer
        +getNombreRol() String
    }

    class Usuario {
        -Integer idUsuario
        -String nombreCompleto
        -String email
        -String passwordHash
        -Rol rol
        +Usuario(nombreCompleto: String, email: String, passwordHash: String, rol: Rol)
        +autenticar(password: String) Boolean
        +cambiarPassword(nuevaPassword: String) Void
        +getRol() Rol
    }

    class Proveedor {
        -Integer idProveedor
        -String nit
        -String razonSocial
        -String telefono
        -String email
        +Proveedor(nit: String, razonSocial: String)
        +actualizarContacto(telefono: String, email: String) Void
        +getDatosProveedor() String
    }

    class Categoria {
        -Integer idCategoria
        -String nombreCategoria
        -String descripcion
        +Categoria(nombreCategoria: String, descripcion: String)
        +getNombreCategoria() String
    }

    class Ubicacion {
        -Integer idUbicacion
        -String estante
        -String nivel
        -String descripcion
        +Ubicacion(estante: String, nivel: String)
        +getUbicacionFormateada() String
    }

    class Producto {
        <<Abstract>>
        -Integer idProducto
        -String codigoInvima
        -String nombreComercial
        -String principioActivo
        -String concentracion
        -Integer stockMinimo
        -Integer stockTotal
        -String estado
        -Categoria categoria
        -List lotes
        +Producto(codigoInvima: String, nombreComercial: String, stockMinimo: Integer)
        +actualizarStockTotal() Void
        +calcularStockCritico() Boolean
        +agregarLote(lote: Lote) Void
        +getLotes() List
    }

    class Medicamento {
        -String formaFarmaceutica
        -Boolean requiereReceta
        +Medicamento(codigoInvima: String, nombreComercial: String, formaFarmaceutica: String)
        +validarFormaFarmaceutica() Boolean
    }

    class Lote {
        -Integer idLote
        -String numeroLote
        -Integer cantidadActual
        -Date fechaVencimiento
        -Ubicacion ubicacion
        -Proveedor proveedor
        +Lote(numeroLote: String, cantidadInicial: Integer, fechaVencimiento: Date, ubicacion: Ubicacion)
        +crearLote(numeroLote: String, cantidad: Integer, fechaVenc: Date) Lote
        +descontarStock(cantidad: Integer) Boolean
        +esProximoAVencer(diasLimite: Integer) Boolean
        +estaVencido() Boolean
    }

    class Alerta {
        -Integer idAlerta
        -String tipoAlerta
        -String mensaje
        -Date fechaGeneracion
        -String estado
        -Producto producto
        -Lote lote
        +Alerta(tipoAlerta: String, mensaje: String)
        +evaluarVencimientos(lote: Lote) Alerta
        +evaluarStockMinimo(producto: Producto) Alerta
        +marcarComoResuelta() Void
    }

    class Movimiento {
        -Integer idMovimiento
        -String tipoMovimiento
        -Integer cantidad
        -Date fechaMovimiento
        -String observacion
        -Usuario usuario
        -Lote lote
        +Movimiento(tipoMovimiento: String, cantidad: Integer, usuario: Usuario, lote: Lote)
        +despacharFEFO(producto: Producto, cantidadRequerida: Integer) List
        +registrarEntrada(lote: Lote, cantidad: Integer) Void
    }

    Producto <|-- Medicamento : Herencia
    Usuario "1" *-- "1" Rol : Tiene
    Producto "1" *-- "1" Categoria : Pertenece a
    Producto "1" *-- "0..*" Lote : Contiene
    Lote "0..*" -- "1" Ubicacion : Almacenado en
    Lote "0..*" -- "0..1" Proveedor : Suministrado por
    Movimiento "0..*" -- "1" Usuario : Realizado por
    Movimiento "0..*" -- "1" Lote : Aplica a
    Alerta "0..*" -- "0..1" Producto : Refiere a
    Alerta "0..*" -- "0..1" Lote : Refiere a
