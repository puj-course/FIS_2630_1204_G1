package org.example.carestock.dao;

import org.example.carestock.model.Usuario;

/**
 * Interfaz que define las operaciones de acceso a datos para usuarios.
 * Aplicando el estilo arquitectónico en capas para desacoplar la lógica de autenticación de PostgreSQL.
 */
public interface UsuarioDAO {
    Usuario buscarPorEmail(String email) throws Exception;
}