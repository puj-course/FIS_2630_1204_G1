# Plantilla del README del proyecto 2630

# CARE STOCK

## Descripción
es una plataforma SaaS integral orientada a la gestión, trazabilidad y optimización de inventarios en droguerías independientes e Instituciones Prestadoras de Salud (IPS) en Colombia. 
El sistema aborda directamente la problemática de las pérdidas operativas por vencimiento de medicamentos y el riesgo de sanciones regulatorias. A través de alertas en tiempo real, automatización de trazabilidad y analítica de datos, **CareStock** garantiza el cumplimiento estricto de la **Resolución 1403 de 2007** de la normativa colombiana de salud.

---
## Equipo del Proyecto (Grupo 1)
| Nombre | Rol | GitHub / Perfil |
|--------|-----|-----------------|
| **Mateo Salazar Bogotá** | Product Owner | [@Mateosalazar543](https://github.com/Mateosalazar543) |
| **Laura Sofía Ortiz Gómez** | Scrum Master | [@Lau1216](https://github.com/Lau1216) |
| **Valentina Carrillo Peñuela** | Diseñadora UI/UX | [@valcarrpe](https://github.com/valcarrpe) |
| **Alejandro Rodríguez Molina** | Desarrollador Backend | [@alejormolina3](https://github.com/alejormolina3) |
| **Santiago Cadena Goyeneche** | Ingeniero de Datos | [@Jsanti13](https://github.com/Jsanti13) |

---

## Funcionalidades Núcleo (MVP - Sprint 1)
1. **Búsqueda en Tiempo Real:** Filtros por nombre comercial, principio activo y autocompletado en mostrador.
2. **Carga y Gestión de Lotes:** Registro de lotes del fabricante y control estricto de fechas de vencimiento.
3. **Despacho Asistido (FEFO):** Sugerencia automática de salida para el lote más próximo a vencer (*First Expired, First Out*).
4. **Panel Semafórico de Vencimientos:** Indicadores visuales de estado (Normal, Alerta de Stock Bajo y Crítico/Vencimiento).
5. **Alertas de Stock Mínimo:** Notificaciones para reabastecimiento oportuno.
6. **Autocompletado y Filtros Rápidos:** Interfaz ágil diseñada para la atención rápida en mostrador.
7. **Reportes y Auditoría:** Historial trazable de movimientos y salidas para inspección normativa.

---

## Tecnologías Utilizadas
* **Frontend:** JavaFX / FXML
* **Backend:** Java (JDK 17+) – Spring Boot
* **Base de Datos:** Oracle Database (Containerizado en Docker + JetBrains DataGrip)
* **IA & Analítica:** Python 3.10+, Pandas, Scikit-learn
* **DevOps & Herramientas:** Docker, GitHub Actions, Git, Visual Studio Code / IntelliJ IDEA

---

## Sistema de Diseño (UI/UX)
El frontend implementa la paleta cromática **Pastel Farmacéutico** bajo la regla 60-30-10 para reducir la fatiga visual en la operación diaria de mostrador:
* `Dominante (60%)`: `#FDFBF7` (Fondo principal / Lienzo)
* `Secundario (30%)`: `#A7DBD8` (Estructura, barras laterales y navegación)
* `Acento (10%)`: `#B7A6E0` (Botones primarios e interacción)
* `Alerta Stock Bajo`: `#F6CEBE` (Indicador Preventivo)
* `Alerta Crítica / Vencimiento`: `#F0ABA8` (Indicador de Riesgo)

---

## Estructura del Repositorio
```text
FIS_2630_1204_G1/
├── app/                      # Código fuente principal de la aplicación (Java/JavaFX)
├── conf/                     # Archivos de configuración del sistema y conexiones
├── DOCS/                     # Documentación técnica, diagramas y especificaciones
├── Proto/                    # Prototipos visuales y mockups UI/UX
├── BOILERPLATE_template.md   # Guía de estructura base
├── LICENSE                   # Licencia del proyecto
└── README.md                 # Documento principal del repositorio

## Instalación y Ejecución
**Requisitos**
- Docker y Docker Compose
- Git
- Java 17+
- Python 3.10+

## Clonar el repositorio
```text
git clone https://github.com/puj-course/FIS_2630_1204_G1
cd proyecto
```

## Ejecución con Docker
```text
docker-compose up --build
```

## Ejecución de pruebas
```text
docker-compose run backend mvn test
docker-compose run ai-model pytest
```

---

## Contexto Académico
- **Asignatura:** Fundamentos de Ingeniería de Software
- **Docente:** Luis Gabriel Moreno Sandoval, PhD
- **Contacto:** morenoluis@javeriana.edu.co

---

## Contacto

**Estudiante 1**  
Estudiante de Ingeniería de Sistemas, Pontificia Universidad Javeriana  
📧 Carrillo-Valentina@javeriana.edu.co  

**Estudiante 2**  
Estudiante de Ingeniería en Sistemas, Pontificia Universidad Javeriana  
📧 lauras.ortizg@javeriana.edu.co  

**Estudiante 3**  
Estudiante de Ingeniería en Sistemas, Pontificia Universidad Javeriana  
📧 alejandrorodriguezm@javeriana.edu.co

**Estudiante 4**  
Estudiante de Ingeniería en Sistemas, Pontificia Universidad Javeriana  
📧 cadenag.js@javeriana.edu.co

**Estudiante 5**  
Estudiante de Ingeniería en Sistemas, Pontificia Universidad Javeriana  
📧 mateo.salazarb@javeriana.edu.co

--- 

## Licencia
Proyecto desarrollado con fines académicos.
