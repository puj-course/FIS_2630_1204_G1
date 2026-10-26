package org.example.carestock.sms;

import org.example.carestock.config.ConexionBaseDatos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public final class MigracionSms {

    private MigracionSms() {
    }

    public static void main(String[] args) throws Exception {

        if (!"SI".equalsIgnoreCase(
                System.getenv("SMS_CONFIRM_MIGRATION")
        )) {
            throw new IllegalStateException(
                    "Confirme SMS_CONFIRM_MIGRATION=SI."
            );
        }

        Path ruta = Path.of(
                "..", "..",
                "scripts", "migrations",
                "885_sms_alertas.sql"
        );

        String contenido = Files.readString(ruta);

        try (Connection conexion =
                     ConexionBaseDatos.getConexion()) {

            conexion.setAutoCommit(false);

            try (Statement st = conexion.createStatement()) {

                for (String sentencia : contenido.split(";")) {

                    if (!sentencia.isBlank()) {
                        st.execute(sentencia);
                    }
                }

                conexion.commit();

                System.out.println(
                        "Migracion SMS ejecutada correctamente."
                );

            } catch (Exception ex) {
                conexion.rollback();
                throw ex;
            }

        } finally {
            ConexionBaseDatos.cerrarPool();
        }
    }
}
