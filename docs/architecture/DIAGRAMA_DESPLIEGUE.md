# 🚀 CareStock - Diagrama y Especificación de Despliegue (Visión Futura)

Este documento describe la arquitectura de infraestructura y despliegue físico/lógico para la evolución a futuro de **CareStock**, diseñada para soportar alta disponibilidad, escalabilidad horizontal y redundancia en entornos hospitalarios y farmacéuticos.

---

## 1. Diagrama de Despliegue (Deployment Diagram)

El sistema utiliza una infraestructura basada en **contenedores (Docker)** orquestados en la nube, desacoplando la capa de clientes, los nodos de cómputo y los servicios de bases de datos administradas.

```mermaid
flowchart TB
    subgraph CLIENT_ZONE [" Client Zone (Dispositivos) "]
        direction LR
        C1["📱 App Móvil / POS (Flutter)"]
        C2["💻 App Escritorio / Web (JavaFX / React)"]
    end

    subgraph EDGE_ZONE [" Edge & Security Layer (Cloudflare) "]
        WAF["🛡️ WAF / DDoS Protection"]
        CDN["⚡ Content Delivery Network (CDN)"]
    end

    subgraph CLUSTER [" K8s Cluster / Cloud Compute (AWS / GCP) "]
        subgraph INGRESS_LAYER [" Ingress Layer "]
            ING["🔀 NGINX Ingress Controller / Load Balancer"]
        end

        subgraph MICROSERVICES [" Microservices Layer (Pods) "]
            GW["🚪 API Gateway Pod"]
            MS1["📦 Inventory Core Service Pod (Réplica A)"]
            MS2["📦 Inventory Core Service Pod (Réplica B)"]
            MS3["🔔 Notification & Alerts Service Pod"]
            MS4["🛡️ Auth & RBAC Service Pod"]
        end

        subgraph CACHE_LAYER [" In-Memory Cache "]
            REDIS[("⚡ Redis Cluster (Caché & Sesiones)")]
        end
    end

    subgraph DB_ZONE [" Cloud Managed Services (DBaaS & External) "]
        NEON[("🐘 Neon Serverless PostgreSQL\n(Base de Datos Principal)")]
        INVIMA["🏛️ API Registro Sanitario INVIMA\n(Servicio Externo)"]
    end

    %% Conexiones
    C1 -->|HTTPS / TLS 1.3| WAF
    C2 -->|HTTPS / TLS 1.3| WAF
    WAF --> CDN
    CDN -->|TCP / HTTP2| ING

    ING -->|Balanceo de carga| GW

    GW -->|gRPC / REST| MS4
    GW -->|gRPC / REST| MS1
    GW -->|gRPC / REST| MS2
    GW -->|gRPC / REST| MS3

    MS1 -->|Conexión JDBC Pool| NEON
    MS2 -->|Conexión JDBC Pool| NEON
    MS1 <-->|Lectura/Escritura rápida| REDIS
    MS2 <-->|Lectura/Escritura rápida| REDIS

    MS3 -->|Consultas de sincronización| INVIMA
