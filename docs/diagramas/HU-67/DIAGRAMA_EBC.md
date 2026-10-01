# HU-67 — Diagrama de Análisis ECB

## Historia de Usuario

**Como** usuario del sistema  
**Quiero** que la tabla principal de inventario muestre únicamente los productos de mi sede  
**Para** garantizar la segregación de datos y evitar alteraciones accidentales en el stock de otras farmacias.

---

## Actores

Los actores involucrados directamente en esta historia son:

- Administrador.
- Farmacéutico.

Ambos representan usuarios autenticados que poseen una farmacia asignada dentro de CareStock.

---

## Diagrama ECB

<pre>
┌─────────────────────┐        ┌─────────────────────┐        ┌────────────────────────┐        ┌─────────────────────┐
│        ACTOR        │        │      BOUNDARY       │        │        CONTROL         │        │       ENTITY        │
└─────────────────────┘        └─────────────────────┘        └────────────────────────┘        └─────────────────────┘


       👤
  Administrador
       │
       ├──────────────────────►  │◯
       │                         InventarioView
       │                         &lt;&lt;boundary&gt;&gt;
       │                              │
       │                              │ cargarInventario()
       │                              ▼
       │                                                    ◉
       │                                            InventarioController
       │                                                &lt;&lt;control&gt;&gt;
       │                                                    │
       │                                                    ├──────────────────────────► ◯
       │                                                    │                             CurrentUser
       │                                                    │                             &lt;&lt;entity&gt;&gt;
       │                                                    │                                 │
       │                                                    │                                 │ idFarmacia
       │                                                    │                                 ▼
       │                                                    ├──────────────────────────► ◯
       │                                                    │                             Farmacia
       │                                                    │                             &lt;&lt;entity&gt;&gt;
       │                                                    │
       │                                                    ├──────────────────────────► ◯
       │                                                    │                             Lote
       │                                                    │                             &lt;&lt;entity&gt;&gt;
       │                                                    │                                 │
       │                                                    │                                 ▼
       │                                                    └──────────────────────────► ◯
       │                                                                                  Medicamento
       │                                                                                  &lt;&lt;entity&gt;&gt;
       │
       │
       👤
  Farmacéutico
       │
       └──────────────────────►  │◯
                                 InventarioView
                                 &lt;&lt;boundary&gt;&gt;


                                 ◄────────────────────
                                  inventario filtrado
                                 ────────────────────►
                                      Usuario
</pre>

---

## Responsabilidades ECB

### Boundary — `InventarioView`

Representa la interfaz mediante la cual el usuario consulta el inventario.

Sus responsabilidades son:

- cargar automáticamente el inventario al abrir la vista;
- solicitar al controlador la información correspondiente;
- mostrar únicamente los registros pertenecientes a la farmacia asignada al usuario;
- mostrar el mensaje `Inventario vacío para esta sede` cuando no existan registros.

La vista no determina manualmente la farmacia ni realiza directamente el filtrado de los datos.

---

### Control — `InventarioController`

Representa el componente encargado de coordinar el caso de uso.

Sus responsabilidades son:

1. recibir la solicitud desde `InventarioView`;
2. obtener el usuario autenticado;
3. recuperar su `idFarmacia`;
4. validar que exista una farmacia asignada;
5. consultar el inventario correspondiente a esa farmacia;
6. recuperar los lotes y medicamentos asociados;
7. retornar únicamente los registros autorizados a la vista.

---

### Entity — `CurrentUser`

Representa el contexto del usuario autenticado.

Para esta historia de usuario proporciona principalmente:

```text
idUsuario
rol
idFarmacia
