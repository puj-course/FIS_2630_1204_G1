# 🏛️ CareStock - Arquitectura y Diagramas de Sistema (Visión Futura)

Este documento detalla la arquitectura de software avanzada para **CareStock**, contemplando la evolución desde el prototipo actual en JavaFX monolítico hacia un ecosistema distribuido, escalable, con integración continua y alta disponibilidad.

---

## 1. Diagrama de Componentes (Component Diagram)

El sistema se estructura bajo una **Arquitectura de Microservicios Desacoplados**, separando la capa de presentación (Web/Móvil), los servicios de dominio de negocio y la persistencia de datos.

```mermaid
C4Context
    title Diagrama de Componentes - Ecosistema CareStock

    Person(usuario, "Usuario CareStock", "Personal médico, regentes de farmacia, administradores")

    System_Boundary(frontend, "Capa de Presentación") {
        Component(webApp, "Portal Web Admin", "React / Next.js", "Gestión general, dashboards y administración central")
        Component(mobileApp, "App Móvil / Pos", "Flutter", "Lectura de códigos de barras/QR, alertas en tiempo real y recepción rápida")
        Component(desktopApp, "Cliente Escritorio", "JavaFX / Native Client", "Módulo local de gestión rápida en farmacias")
    }

    System_Boundary(gateway_layer, "Capa de Integración") {
        Component(apiGateway, "API Gateway / Reverse Proxy", "Spring Cloud Gateway / NGINX", "Autenticación JWT, Rate Limiting, Enrutamiento de peticiones")
    }

    System_Boundary(microservices, "Capa de Servicios de Negocio") {
        Component(authService, "Auth & Identity Service", "Spring Boot", "Gestión de usuarios, roles (RBAC) y tokens OAuth2/JWT")
        Component(inventoryService, "Inventory Core Service", "Spring Boot / Java", "Gestión de stock, lotes, semaforización de vencimiento y catálogo INVIMA")
        Component(alertService, "Notification & Alert Service", "Node.js / Python", "Generación de alertas críticas, correos, avisos de desabastecimiento")
        Component(auditService, "Audit & Traceability Service", "Go / Java", "Registro inmutable de trazabilidad de medicamentos y movimientos")
    }

    System_Boundary(data_layer, "Capa de Datos y Mensajería") {
        ComponentDb(mainDb, "Base de Datos Relacional", "PostgreSQL (Neon DB)", "Almacenamiento relacional para inventarios, usuarios y registros INVIMA")
        ComponentDb(cacheDb, "Capa de Caché", "Redis", "Caché de sesiones, catálogo de medicamentos frecuente y rate limiting")
        ComponentQueue(eventBus, "Event Bus / Message Queue", "RabbitMQ / Apache Kafka", "Transmisión asíncrona de eventos (ej. StockCrítico, MedicamentoVencido)")
    }

    Rel(usuario, webApp, "Accede via HTTPS")
    Rel(usuario, mobileApp, "Accede via HTTPS")
    Rel(usuario, desktopApp, "Accede via HTTPS / TCP")

    Rel(webApp, apiGateway, "Peticiones REST / GraphQL")
    Rel(mobileApp, apiGateway, "Peticiones REST / gRPC")
    Rel(desktopApp, apiGateway, "Peticiones REST")

    Rel(apiGateway, authService, "Valida credenciales / JWT")
    Rel(apiGateway, inventoryService, "Enruta consultas/registros de stock")
    Rel(apiGateway, alertService, "Enruta peticiones de notificaciones")

    Rel(inventoryService, mainDb, "Lectura/Escritura JDBC")
    Rel(inventoryService, cacheDb, "Consultas rápidas de catálogo")
    Rel(inventoryService, eventBus, "Publica eventos de cambios en stock")

    Rel(eventBus, alertService, "Consume eventos de alertas de vencimiento")
    Rel(eventBus, auditService, "Consume eventos para auditoría de movimientos")

    Rel(auditService, mainDb, "Escribe logs de auditoría")
