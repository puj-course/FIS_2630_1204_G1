# Clase ProductoDirector

## Descripción

La clase `ProductoDirector` es el **Director** del patrón Builder. Su rol es orquestar la construcción de productos usando un Builder concreto, sin necesidad de conocer los detalles internos de cómo se construyen.

---

## Rol en el patrón Builder

| Rol | Clase |
|-----|-------|
| **Builder** | `Producto` (interfaz) |
| **ConcreteBuilder** | `MedicamentoBuilder`, `CosmeticoBuilder`, `DispositivoMedicoBuilder` |
| **Director** | `ProductoDirector` |
| **Product** | `Medicamento`, `Cosmetico`, `DispositivoMedico` |

---

## Atributos

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| `builder` | `Producto` | El builder concreto que se usará para construir el producto. |

---

## Métodos

### `ProductoDirector(Producto builder)`

Constructor que recibe un builder concreto.

### `changeBuilder(Producto builder)`

Permite cambiar el builder concreto en tiempo de ejecución.

### `make(String type)`

Construye un producto según el tipo especificado:

- **"estandar"**: Construye un producto con código `PROD-001`, nombre "Producto Comercial Estándar", precio 25000.0 y stock 50.
- **"premium"**: Construye un producto con código `PROD-999`, nombre "Producto de Alta Gama", precio 120000.0 y stock 10.

---

## Ejemplo de uso

```java
ProductoDirector director = new ProductoDirector(new MedicamentoBuilder());

// Crear producto estándar
director.make("estandar");

// Cambiar a otro builder
director.changeBuilder(new CosmeticoBuilder());

// Crear producto premium
director.make("premium");
