# Análisis del flujo de ingreso de lote desde JavaFX hasta PostgreSQL

**Sprint:** 6
**Historia de Usuario:** HU.40
**Tarea:** #239
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 17 de septiembre de 2026

---

## 1. Objetivo

Identificar los puntos clave a documentar del flujo de ingreso de lote, 
desde la interfaz JavaFX hasta el almacenamiento en PostgreSQL.

---

## 2. Puntos clave identificados

| # | Punto clave | Descripción |
|---|-------------|-------------|
| 1 | Punto de entrada | El flujo inicia en `MainDashboardFX` cuando el 
usuario hace clic en "Agregar Medicamento". |
| 2 | Captura de datos | El formulario `FormularioMedicamentoDialog` 
captura los datos del nuevo lote. |
| 3 | Construcción del objeto | Se construye un objeto `Medicamento` con 
los datos ingresados. |
| 4 | Llamada al DAO | `MedicamentoDAO.insertar()` ejecuta el INSERT en 
PostgreSQL. |
| 5 | Conexión a BD | `DatabaseConfig.getConnection()` conecta a NeonDB 
con JDBC. |
| 6 | Inserción en BD | Se ejecuta `INSERT INTO MEDICAMENTOS` con los 
datos del lote. |
| 7 | Trigger de stock | El trigger `trg_actualizar_stock_total` actualiza 
el stock automáticamente. |
| 8 | Trazabilidad | El procedimiento `sp_registrar_nuevo_lote` registra 
el movimiento en `LOG_MOVIMIENTOS`. |

---

## 3. Componentes involucrados

| Capa | Componente |
|------|------------|
| Interfaz | `MainDashboardFX.java` |
| Interfaz | `FormularioMedicamentoDialog.java` |
| Modelo | `Medicamento.java` |
| Acceso a datos | `MedicamentoDAO.java` |
| Configuración | `DatabaseConfig.java` |
| Base de datos | `01_schema_carestock.sql` |
| Base de datos | `01_sp_ingreso_lotes.sql` |
| Base de datos | `02_functions_triggers.sql` |

---

## 4. Referencias

- HU.40 - Análisis del flujo de ingreso de lote
- Tarea #239 - Analizar el flujo de ingreso de lote desde JavaFX hasta 
PostgreSQL

---

**Fin del documento.**
