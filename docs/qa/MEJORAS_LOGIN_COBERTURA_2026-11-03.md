# CareStock - Mejoras de Login y Cobertura

## 1. Identificacion

- Proyecto: CareStock
- Fecha: 03/11/2026
- Rama: feature/hu136-navegacion-alertas-983
- Herramientas: JUnit 5, Mockito, JaCoCo, Maven
- Tecnologia: Java 21 y JavaFX 21

## 2. Objetivo

Mejorar la experiencia de inicio de sesion,
fortalecer la validacion de credenciales y
aumentar la cobertura de las clases criticas.

## 3. Mejoras funcionales

- Rediseno visual del formulario.
- Mayor contraste y jerarquia visual.
- Identidad grafica unificada de CareStock.
- Validacion de correo y contrasena.
- Visualizacion y ocultamiento de contrasena.
- Opcion de recordar exclusivamente el correo.
- Animacion de carga durante la autenticacion.
- Ejecucion asincrona mediante JavaFX Task.
- Mensajes de error claros y persistentes.
- Estados visuales de botones.

## 4. Correcciones realizadas

- Declaracion del modulo java.prefs.
- Manejo de excepciones comprobadas de sesion.
- Correccion de mensajes ocultados al limpiar clave.
- Inyeccion de dependencias para pruebas del login.
- Pruebas de JDBC simulado sin acceder a Neon.
- Preparacion del ciclo de vida JavaFX en JUnit.

## 5. Pruebas implementadas

### LoginControllerFxTest

12 pruebas del controlador JavaFX:

- Estado inicial del formulario.
- Validacion de campos vacios.
- Correccion de errores de entrada.
- Mostrar y ocultar contrasena.
- Recuperacion del correo recordado.
- Error de acceso a preferencias.
- Autenticacion correcta y animacion de carga.
- Inicio sin recordar correo.
- Credenciales incorrectas.
- Fallo inesperado de autenticacion.
- Error de inicializacion de sesion.
- Error al guardar preferencias.

### SesionUsuarioCoberturaAdicionalTest

5 pruebas adicionales:

- Inicio con farmacia asignada.
- Manejo de farmacia nula.
- Usuario inactivo.
- Excepciones JDBC.
- Rechazo de usuario invalido.

### AuthenticationServiceCoberturaAdicionalTest

2 pruebas parametrizadas de costos BCrypt invalidos.

Tambien se implementaron pruebas para:

- ValidacionFormularioLogin.
- Casos limite de correo y contrasena.
- Seguridad de AuthenticationService.
- Estructura y contrato del FXML.

## 6. Resultados JaCoCo

| Clase | Lineas | Ramas | Metodos |
|---|---:|---:|---:|
| LoginController | 85.19% | 73.91% | 70.37% |
| SesionUsuario | 100.00% | 100.00% | 100.00% |
| AuthenticationService | 100.00% | 94.44% | 100.00% |
| ValidacionFormularioLogin | 100.00% | 100.00% | 100.00% |

LoginController paso de 0.00% a 85.19%
de cobertura de lineas.

## 7. Regresion

Ultima ejecucion completa reportada:

- Pruebas: 578
- Fallos de prueba: 0
- Errores de prueba: 0
- Omitidas: 0

Observacion: esa ejecucion registro un fallo
posterior de arranque de fork en Maven Surefire.
El BUILD SUCCESS definitivo debe verificarse
en una nueva ejecucion de clean verify.

## 8. Seguridad

Las pruebas utilizan Mockito para simular
dependencias externas y consultas JDBC.

No se utilizaron cuentas reales de farmacia.

La funcionalidad Recordar mi correo no persiste
contrasenas y no reemplaza la autenticacion BCrypt.

## 9. Pendientes de mejora

- Revisar las 24 lineas restantes de LoginController.
- Ampliar cobertura de ramas cuando aporte valor.
- Revisar advertencia de modulo automatico jBCrypt.
- Conservar pruebas visuales en futuros cambios.

## 10. Conclusion

Se fortalecio la autenticacion de CareStock
mediante pruebas unitarias, funcionales JavaFX
y simulaciones de dependencias.

Tres componentes alcanzaron 100% de cobertura
de lineas, y LoginController alcanzo 85.19%.

Las pruebas permitieron identificar y corregir
un defecto real en la presentacion de errores.

El avance mejora la mantenibilidad, seguridad
y confiabilidad del inicio de sesion.
