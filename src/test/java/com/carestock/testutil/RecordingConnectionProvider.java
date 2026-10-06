package com.carestock.testutil;

import com.carestock.config.ConnectionProvider;

import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Proveedor de conexión JDBC simulado para pruebas unitarias.
 *
 * Registra las operaciones transaccionales ejecutadas por los servicios
 * sin establecer una conexión real con PostgreSQL o Neon.
 */
public final class RecordingConnectionProvider
        implements ConnectionProvider {

    private boolean autoCommit = true;
    private boolean closed;

    private int connectionCount;
    private int commitCount;
    private int rollbackCount;

    @Override
    public Connection getConnection() {

        connectionCount++;

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{
                        Connection.class
                },
                (proxy, method, args) -> {

                    String methodName =
                            method.getName();

                    switch (methodName) {

                        case "getAutoCommit":
                            return autoCommit;

                        case "setAutoCommit":
                            autoCommit =
                                    (Boolean) args[0];

                            return null;

                        case "commit":
                            commitCount++;
                            return null;

                        case "rollback":
                            rollbackCount++;
                            return null;

                        case "close":
                            closed = true;
                            return null;

                        case "isClosed":
                            return closed;

                        case "isWrapperFor":
                            return false;

                        case "unwrap":
                            throw new UnsupportedOperationException(
                                    "unwrap no está soportado por la conexión de prueba."
                            );

                        case "toString":
                            return "RecordingConnection";

                        case "hashCode":
                            return System.identityHashCode(
                                    proxy
                            );

                        case "equals":
                            return proxy == args[0];

                        default:
                            throw new UnsupportedOperationException(
                                    "Método JDBC no soportado en la conexión de prueba: "
                                            + methodName
                            );
                    }
                }
        );
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public int getRollbackCount() {
        return rollbackCount;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public boolean isClosed() {
        return closed;
    }
}
