# Plantilla del README del proyecto 2630

# CARE STOCK

## Descripción
es una plataforma SaaS integral orientada a la gestión, trazabilidad y optimización de inventarios en droguerías independientes e Instituciones Prestadoras de Salud (IPS) en Colombia. 
El sistema aborda directamente la problemática de las pérdidas operativas por vencimiento de medicamentos y el riesgo de sanciones regulatorias. A través de alertas en tiempo real, automatización de trazabilidad y analítica de datos, **CareStock** garantiza el cumplimiento estricto de la **Resolución 1403 de 2007** de la normativa colombiana de salud.

---

## Equipo del Proyecto
| Nombre        | Rol                   | GitHub / Perfil |
|--------------|-----------------------|-----------------|
| Mateo Salazar| Product Owner         | github.com/Mateosalazar543 |
| Valentina Carrillo P| Diseñadora UI       | github.com/valcarrpe |
| Alejandro Rodriguez | Desarrollador Backend | github.com/alejormolina3 |
| Santiago Cadena | Ingeniero De Datos             | github.com/Jsanti13|
| Laura Sofía Ortiz | Scrum Máster       | github.com/Lau1216 |

---

## Tecnologías Utilizadas
- **Frontend:** JavaFX
- **Backend:** Java – Spring Boot
- **Base de Datos:** PostgreSQL
- **IA / Data Science:** Python, Pandas, Scikit-learn
- **DevOps:** GitHub Actions, Docker, SonarQube
- **Control de versiones:** Git

---

## Estructura del Proyecto
```text
project-name/
├── app/
│   ├── index.js
│   │   └── Punto de entrada principal de la aplicación.
│   ├── package.json
│   │   └── Define las dependencias, scripts y configuración del proyecto.
│   ├── routes/
│   │   ├── index.js
│   │   └── Define las rutas o endpoints principales de la aplicación.
│   ├── controllers/
│   │   ├── userController.js
│   │   └── Contiene la lógica encargada de procesar las solicitudes.
│   └── services/
│       ├── userService.js
│       └── Contiene la lógica de negocio y servicios reutilizables.
│
├── conf/
│   ├── config.json
│   │   └── Contiene parámetros generales de configuración.
│   ├── database.js
│   │   └── Configura la conexión con la base de datos.
│   └── environment.example
│       └── Ejemplo de las variables de entorno necesarias para ejecutar el proyecto.
│
├── docs/
│   ├── architecture.md
│   │   └── Describe la arquitectura general del sistema.
│   ├── api.md
│   │   └── Documenta los endpoints, parámetros y respuestas de la API.
│   ├── installation.md
│   │   └── Explica cómo instalar y configurar el proyecto.
│   └── user_guide.md
│       └── Guía básica para el uso de la aplicación.
│
├── scripts/
│   ├── setup.sh
│   │   └── Automatiza la instalación y configuración inicial del proyecto.
│   ├── start.sh
│   │   └── Permite iniciar la aplicación.
│   ├── test.sh
│   │   └── Ejecuta las pruebas automatizadas.
│   └── deploy.sh
│       └── Automatiza tareas relacionadas con el despliegue.
│
├── src/
│   ├── models/
│   │   ├── user.js
│   │   └── Define las estructuras o modelos de datos del sistema.
│   ├── utils/
│   │   ├── helpers.js
│   │   └── Contiene funciones auxiliares reutilizables.
│   ├── middleware/
│   │   ├── auth.js
│   │   └── Contiene funciones que se ejecutan antes o después de una solicitud.
│   └── tests/
│       ├── user.test.js
│       └── Contiene pruebas unitarias o de integración del proyecto.
│
├── temp/
│   ├── .gitkeep
│   │   └── Permite conservar la carpeta vacía dentro del repositorio.
│   ├── example.tmp
│   │   └── Ejemplo de archivo temporal generado durante la ejecución.
│   └── uploads/
│       └── Carpeta destinada a almacenar archivos temporales cargados por usuarios.
│
├── BOILERPLATE_template.md
│   └── Documento que explica la estructura base y cómo utilizar este boilerplate.
│
├── CONTRIBUTING.md
│   └── Define las normas y recomendaciones para contribuir al proyecto.
│
├── LICENSE
│   └── Especifica la licencia bajo la cual se distribuye el proyecto.
│
├── README.md
│   └── Documento principal con la descripción, instalación, uso y características del proyecto.
│
├── .gitignore
│   └── Define los archivos y carpetas que Git no debe versionar.
│
└── .env.example
    └── Plantilla de las variables de entorno necesarias para ejecutar la aplicación.
```

---

## Instalación y Ejecución
**Requisitos**
- Docker y Docker Compose
- Git
- Java 17+
- Python 3.10+

## Clonar el repositorio
```text
git clone https://github.com/organizacion/proyecto.git
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
