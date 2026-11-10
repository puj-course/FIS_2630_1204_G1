# CareStock - Navegacion contextual desde alertas

## 1. Identificacion

- Proyecto: CareStock
- Historia: HU.136
- Issue: #983
- Parent Issue: #980
- Rama: feature/hu136-navegacion-alertas-983
- Fecha: 03/11/2026

## 2. Objetivo

Permitir que el usuario acceda directamente desde una
alerta de inventario al producto o lote relacionado,
sin realizar una busqueda manual.

La navegacion debe conservar el contexto de la farmacia
activa y respetar las restricciones de seguridad.

## 3. Implementacion

Se incorporo navegacion contextual para dos tipos
de alertas:

### Stock minimo

- Identificacion mediante ID, categoria y codigo.
- Compatibilidad con Medicamentos, Aseo y Maternidad.
- Apertura de la consulta unificada de inventario.
- Seleccion del producto correspondiente.

### Vencimiento

- Identificacion por ID y numero de lote.
- Apertura de la pantalla de gestion de inventario.
- Seleccion del lote asociado a la alerta.

### Validaciones

- Comprobacion de la farmacia activa.
- Rechazo de referencias invalidas.
- Manejo de productos o lotes no disponibles.
- Conservacion de los controles de acceso existentes.

## 4. Componentes

- CareStockApp
- ControladorAlertas
- ConsultaInventarioController
- ControladorInventario
- ResolucionNavegacionAlertas
- alertas-view.fxml

## 5. Pruebas automatizadas

Clase:
ResolucionNavegacionAlertasTest

Casos incluidos:

1. Resolucion de stock minimo por categoria.
2. Identificacion del producto por ID y codigo.
3. Identificacion de lote por ID y numero.
4. Rechazo de navegacion entre farmacias.
5. Rechazo de alertas de vencimiento invalidas.

Resultado reportado:
5 pruebas exitosas, sin fallos ni errores.

## 6. Validacion funcional pendiente de cierre

- [ ] Abrir productos desde alertas de stock minimo.
- [ ] Abrir lotes desde alertas de vencimiento.
- [ ] Confirmar navegacion en las tres categorias.
- [ ] Comprobar manejo de registros inexistentes.
- [ ] Verificar segregacion entre farmacias.
- [ ] Confirmar permisos de cada rol.
- [ ] Verificar ejecucion con javafx:run.

## 7. Evidencias

Directorio:
docs/qa/evidence/

Resultados definitivos sujetos a la ejecucion
satisfactoria de Maven y a la validacion manual.

## 8. Conclusion

La implementacion introduce la resolucion contextual
de productos y lotes desde las alertas, reutilizando
las pantallas existentes de CareStock.

Las pruebas unitarias del componente de resolucion
fueron exitosas. La aprobacion funcional completa
requiere cerrar los casos manuales pendientes.
