
## Segundo lote de ampliacion de cobertura

Se incorporaron pruebas adicionales para:

- Precios validos y limites decimales.
- Rechazo de precios negativos y formatos invalidos.
- Carga de formularios de Aseo.
- Carga de formularios de Maternidad.
- Carga de formularios de Medicamentos.
- Validacion de autorizaciones desde la consulta.
- Rechazo de dispensaciones invalidas.
- Dispensacion exitosa mediante fachada simulada.

Se emplean JUnit 5, Mockito y JavaFX.

No se realizan operaciones sobre Neon.

La cobertura se valida localmente con JaCoCo
y posteriormente en SonarCloud.
