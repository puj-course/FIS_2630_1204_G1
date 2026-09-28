# Cifrado de Contraseñas en CareStock

**Fecha:** 25 de septiembre de 2026

---

## 1. Introducción

Este documento describe cómo CareStock protege las contraseñas de los 
usuarios del sistema. El objetivo es que cualquier persona del equipo, 
o un auditor externo, pueda entender el mecanismo de seguridad sin 
necesidad de leer el código fuente directamente.

---

## 2. ¿Qué es un hash y por qué no es lo mismo que "encriptar"?

Cifrar (encriptar) una contraseña implica que, con la clave correcta, 
se puede revertir el proceso y recuperar la contraseña original. Un 
**hash**, en cambio, es una transformación de un solo sentido: no 
existe ninguna clave ni procedimiento que permita convertir un hash de 
vuelta a la contraseña original.

CareStock **nunca** guarda ni transmite la contraseña en texto plano. 
Lo único que se almacena en la base de datos es el resultado de 
aplicarle un hash a la contraseña.

---

## 3. Algoritmo utilizado: bcrypt

CareStock utiliza **bcrypt** para el hash de contraseñas, implementado 
mediante la extensión `pgcrypto` de PostgreSQL. Bcrypt fue diseñado 
específicamente para contraseñas: es deliberadamente lento de calcular 
(a diferencia de algoritmos como MD5 o SHA-1), lo que dificulta los 
ataques de fuerza bruta incluso si un atacante obtuviera acceso a la 
base de datos.

Cada hash generado por bcrypt incluye internamente un valor aleatorio 
(*salt*), por lo que dos usuarios con la misma contraseña nunca 
tendrán el mismo valor almacenado en `password_hash`.

---

## 4. Dónde vive esta lógica

El cifrado y la verificación de contraseñas se implementan directamente 
en la base de datos, como funciones de PostgreSQL:

| Función | Responsabilidad |
|---------|------------------|
| `fn_crear_usuario` | Recibe la contraseña en texto plano, valida que tenga al menos 8 caracteres, la cifra con bcrypt (`crypt(password, gen_salt('bf'))`) y la guarda en `usuarios.password_hash`. |
| `fn_autenticar_usuario` | Recibe el correo y la contraseña ingresados, y compara el hash generado a partir de la contraseña contra el hash almacenado, sin descifrar nunca el valor guardado. |

Del lado de JavaFX, la pantalla `CrearUsuarioFX` invoca `fn_crear_usuario` 
a través de `UsuarioDAO.crear()`, agregando una validación adicional de 
longitud mínima en la interfaz para dar retroalimentación inmediata al 
usuario antes de consultar la base de datos.

---

## 5. Garantías de seguridad

- La contraseña en texto plano **nunca** se almacena en ninguna tabla.
- La contraseña en texto plano **nunca** se imprime en logs de consola 
ni en mensajes de error mostrados al usuario.
- La verificación de credenciales se hace comparando hashes, no 
comparando texto plano contra texto plano.
- La longitud mínima de contraseña (8 caracteres) se valida tanto en 
la interfaz como en la base de datos, como doble capa de protección.

---

**Fin del documento.**
