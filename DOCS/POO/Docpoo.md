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
