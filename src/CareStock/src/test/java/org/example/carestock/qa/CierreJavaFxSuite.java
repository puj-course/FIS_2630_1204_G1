package org.example.carestock.qa;

import javafx.application.Platform;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Libera JavaFX al finalizar la suite de pruebas.
 * Evita cerrarlo entre diferentes clases JUnit.
 */
public final class CierreJavaFxSuite
        implements LauncherSessionListener {

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        try {
            Platform.exit();
        } catch (IllegalStateException ignored) {
            // JavaFX no estaba inicializado.
        }
    }
}
