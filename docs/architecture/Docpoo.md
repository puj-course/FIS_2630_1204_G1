# 📐 Documentación técnica del diagrama de clases — CareStock

> **Referencia:** [Issue #41 — Elaboración del Diagrama de Clases del Sistema CareStock](https://github.com/puj-course/FIS_2630_1204_G1/issues/41)  
> **Autor de la documentación original:** Mateo Salazar Bogotá — Product Owner  
> **Fecha de revisión documental:** 14 de octubre de 2026  
> **Tipo de artefacto:** Diseño estructural orientado a objetos (UML)  
> **Estado:** Documentación ampliada a partir del diagrama aportado; **pendiente de contraste final con el código fuente de la rama vigente**.

## 1. Propósito

Este documento describe y justifica la arquitectura orientada a objetos de **CareStock**, un sistema de gestión de inventarios médico-farmacéuticos con interfaz JavaFX y persistencia relacional mediante JDBC sobre PostgreSQL/Neon. Su propósito es explicar **qué responsabilidad tiene cada paquete**, **cómo colaboran sus clases**, **qué decisiones de diseño sustentan la solución** y **cómo comprobar que los patrones y las reglas de negocio funcionan**.

Este documento y el archivo `Carestock.svg` deben mantenerse en la misma carpeta para que la imagen se visualice correctamente en GitHub. El documento complementa, pero no reemplaza, el código fuente, las pruebas automatizadas, la documentación de base de datos ni los diagramas de comportamiento. En especial, la existencia de una operación o relación en el diagrama **no demuestra por sí sola que esté implementada y probada**.

### 1.1 Alcance funcional representado

- Autenticación, contexto de sesión y asociación del usuario con una farmacia.
- Consulta y administración del catálogo de **medicamentos, productos de aseo y maternidad**.
- Registro de lotes, gestión de existencias y operaciones de dispensación.
- Consulta unificada del inventario por farmacia y filtrado para la interfaz.
- Persistencia mediante DAO y gestión centralizada de conexiones JDBC.
- Optimización de lecturas mediante **Proxy con caché** y cachés auxiliares para vistas.
- Construcción de modelos mediante **Builder** y orquestación de casos de uso mediante **Facade**.

También figuran tipos como `Cosmetico`, `DispositivoMedico` y `AlertaInvima`. Su presencia en UML **no debe interpretarse automáticamente como evidencia de que tienen formularios o flujos productivos habilitados**. El propio diagrama marca `AlertaInvima` como modelo histórico/de pruebas.

### 1.2 Criterios y límites

**Fuente primaria:** SVG UML suministrado junto con la documentación inicial. La arquitectura y las firmas aquí descritas reflejan ese archivo, no una inspección independiente del repositorio. Cuando se especifica una garantía que el diagrama no puede demostrar (seguridad, transacciones, concurrencia, FEFO), se indica como **criterio de validación** y no como hecho comprobado.

**Precisión terminológica:** la evidencia disponible permite describir a CareStock como aplicación de escritorio JavaFX con base de datos remota. La denominación «SaaS» del documento original requiere demostrar un modelo de prestación del servicio que no se puede inferir solo del diagrama.

---

## 2. Diagrama de clases UML completo

[![Diagrama UML de clases de CareStock](./Carestock.svg)](./Carestock.svg)

**[🔍 Abrir el SVG completo para ampliar y examinar las relaciones](./Carestock.svg)**

**Figura 1.** Vista estructural global del sistema, organizada por paquetes.
