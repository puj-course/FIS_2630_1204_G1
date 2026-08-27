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
## Funcionalidades Principales

- **Despacho Asistido (FEFO):** Priorización automática de salida para el lote más próximo a vencer (*First Expired, First Out*).
- **Control de Lotes y Vencimientos:** Registro detallado de fechas de caducidad con semaforización visual de alertas.
- **Búsqueda y Filtros Rápidos:** Búsqueda en tiempo real por nombre comercial o principio activo optimizada para atención en mostrador.
- **Alertas de Stock Mínimo:** Notificaciones automáticas para reabastecimiento oportuno.
- **Trazabilidad y Reportes:** Historial de movimientos listo para inspecciones normativas.
---

## Tecnologías Utilizadas
* **Frontend:** JavaFX / FXML
* **Backend:** Java (JDK 17+) – Spring Boot
* **Base de Datos:** Oracle Database (Containerizado en Docker + JetBrains DataGrip)
* **IA & Analítica:** Python 3.10+, Pandas, Scikit-learn
* **DevOps & Herramientas:** Docker, GitHub Actions, Git, Visual Studio Code / IntelliJ IDEA
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

Johan Cadena (Jsanti13) - Rol: Data Engineer
