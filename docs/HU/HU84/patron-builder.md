# Patrón Builder en CareStock

## 1. Interfaz `Producto`

La interfaz `Producto` define el contrato que deben cumplir todos los builders del sistema. Es la base del patrón Builder en CareStock.

### Métodos

| Método | Descripción |
|--------|-------------|
| `void reset()` | Reinicia el builder para construir un nuevo producto desde cero. |
| `Producto setCodigo(String codigo)` | Establece el código del producto. |
| `Producto setNombre(String nombre)` | Establece el nombre del producto. |
| `Producto setDescripcion(String descripcion)` | Establece la descripción del producto. |
| `Producto setPrecio(double precio)` | Establece el precio del producto. |
| `Producto setStock(int stock)` | Establece el stock del producto. |

### Propósito

Permitir que los builders concretos (`MedicamentoBuilder`, `CosmeticoBuilder`, `DispositivoMedicoBuilder`) implementen los mismos métodos y puedan ser usados de forma intercambiable por el `ProductoDirector`.

### Código

```java
public interface Producto {
    void reset();
    Producto setCodigo(String codigo);
    Producto setNombre(String nombre);
    Producto setDescripcion(String descripcion);
    Producto setPrecio(double precio);
    Producto setStock(int stock);
}
