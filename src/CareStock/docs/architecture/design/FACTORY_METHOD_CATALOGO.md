# Factory Method en CareStock

El patron creacional Factory Method reemplaza Builder en el catalogo.

## Participantes

- Creator: FabricaProducto<T extends Validable>
- Factory Method: crearInstancia()
- Concrete Creators: FabricaAseo, FabricaMaternidad y FabricaMedicamento
- Products: Aseo, Maternidad y Medicamento

ControladorCatalogo delega la construccion a las fabricas.
AseoDao y MaternidadDao utilizan las fabricas al leer PostgreSQL.

La fabrica crea y valida objetos en memoria.
Inventario (Facade) conserva la autorizacion y la farmacia activa.
Los DAO siguen encargados de la persistencia.

Verificacion: ./mvnw -Djavafx.platform=win clean verify
