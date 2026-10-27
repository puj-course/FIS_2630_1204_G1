# Issue #460 - Validación de segregación de inventario

## Objetivo

Validar que el inventario de CareStock se encuentre segregado
por farmacia y que el contexto de la sesión determine
exclusivamente qué inventario puede ser consultado.

## Estrategia de prueba

Se utilizan tres farmacias independientes:

- Farmacia A: medicamentos A1 y A2.
- Farmacia B: medicamentos B1 y B2.
- Farmacia C: inventario vacío.

La prueba de integración establece diferentes usuarios en
UserSession y obtiene el id_farmacia mediante SessionContext.

Posteriormente se consulta MedicamentoDAO utilizando únicamente
el id_farmacia proveniente de la sesión.

## Flujo validado

Usuario
-> UserSession
-> SessionContext
-> id_farmacia
-> MedicamentoDAO
-> PostgreSQL
-> inventario de la farmacia activa

## Resultados esperados

### Usuario A

Visualiza:

- A1
- A2

No visualiza:

- B1
- B2

### Usuario B

Visualiza:

- B1
- B2

No visualiza:

- A1
- A2

### Farmacia C

La consulta devuelve una lista vacía.

En JavaFX se presenta:

Inventario vacío para esta sede

### Sin sesión

SessionContext bloquea la operación antes de realizar
la consulta al DAO.

### Usuario sin farmacia

No se permite realizar una consulta global de inventario.

## Seguridad

La consulta utilizada por MedicamentoDAO incluye:

WHERE m.id_farmacia = ?

Por lo tanto, la segregación ocurre en PostgreSQL y no
únicamente mediante filtros visuales en JavaFX.

Los errores JDBC son transformados en mensajes controlados
por UserMessageResolver y no deben revelar SQL, cadenas JDBC,
usuarios o credenciales.
cat > docs/qa/HU-460-SEGREGACION-INVENTARIO.md <<'EOF'
# Issue #460 - Validación de segregación de inventario

## Objetivo

Validar que el inventario de CareStock se encuentre segregado
por farmacia y que el contexto de la sesión determine
exclusivamente qué inventario puede ser consultado.

## Estrategia de prueba

Se utilizan tres farmacias independientes:

- Farmacia A: medicamentos A1 y A2.
- Farmacia B: medicamentos B1 y B2.
- Farmacia C: inventario vacío.

La prueba de integración establece diferentes usuarios en
UserSession y obtiene el id_farmacia mediante SessionContext.

Posteriormente se consulta MedicamentoDAO utilizando únicamente
el id_farmacia proveniente de la sesión.

## Flujo validado

Usuario
-> UserSession
-> SessionContext
-> id_farmacia
-> MedicamentoDAO
-> PostgreSQL
-> inventario de la farmacia activa

## Resultados esperados

### Usuario A

Visualiza:

- A1
- A2

No visualiza:

- B1
- B2

### Usuario B

Visualiza:

- B1
- B2

No visualiza:

- A1
- A2

### Farmacia C

La consulta devuelve una lista vacía.

En JavaFX se presenta:

Inventario vacío para esta sede

### Sin sesión

SessionContext bloquea la operación antes de realizar
la consulta al DAO.

### Usuario sin farmacia

No se permite realizar una consulta global de inventario.

## Seguridad

La consulta utilizada por MedicamentoDAO incluye:

WHERE m.id_farmacia = ?

Por lo tanto, la segregación ocurre en PostgreSQL y no
únicamente mediante filtros visuales en JavaFX.

Los errores JDBC son transformados en mensajes controlados
por UserMessageResolver y no deben revelar SQL, cadenas JDBC,
usuarios o credenciales.

## Pruebas relacionadas

- InventarioSegregacionFarmaciasIT
- MedicamentoDAOFarmaciaIT
- UsuarioDAOFarmaciaIT
- InventarioCrudServiceScopeTest
- SessionContextFarmaciaTest
- SessionContextTest
- UserMessageResolverTest

## Evidencia

La evidencia automática se encuentra en:

docs/qa/evidence/issue-460-segregacion-inventario.txt
