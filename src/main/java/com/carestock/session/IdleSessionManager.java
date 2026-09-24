package com.carestock.session;

import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Temporizador global de inactividad de CareStock.
 *
 * Supervisa las ventanas activas de la aplicación y
 * reinicia el temporizador con actividad de mouse o teclado.
 */
public final class IdleSessionManager {

    private final PauseTransition idleTimer =
            new PauseTransition();

    private final Set<Scene> monitoredScenes =
            Collections.newSetFromMap(
                    new IdentityHashMap<>()
            );

    private final EventHandler<MouseEvent>
            mouseActivityHandler =
            event -> resetTimer();

    private final EventHandler<KeyEvent>
            keyboardActivityHandler =
            event -> resetTimer();

    private final ListChangeListener<Window>
            windowListener =
            change -> {

                while (change.next()) {

                    if (change.wasAdded()) {

                        for (
                                Window window
                                : change.getAddedSubList()
                        ) {

                            registerWindow(
                                    window
                            );
                        }
                    }

                    if (change.wasRemoved()) {

                        for (
                                Window window
                                : change.getRemoved()
                        ) {

                            unregisterWindow(
                                    window
                            );
                        }
                    }
                }
            };

    private Runnable timeoutAction;

    private int timeoutMinutes;

    private boolean monitoring;

    private IdleSessionManager() {
    }

    /**
     * Holder para inicialización diferida del singleton.
     */
    private static final class Holder {

        private static final IdleSessionManager INSTANCE =
                new IdleSessionManager();
    }

    public static IdleSessionManager getInstance() {

        return Holder.INSTANCE;
    }

    /**
     * Inicia la supervisión global de la sesión.
     */
    public void startMonitoring(
            int timeoutMinutes,
            Runnable timeoutAction
    ) {

        validarTimeoutMinutes(
                timeoutMinutes
        );

        if (timeoutAction == null) {

            throw new IllegalArgumentException(
                    "La acción de expiración no puede ser nula."
            );
        }

        stopMonitoring();

        this.timeoutMinutes =
                timeoutMinutes;

        this.timeoutAction =
                timeoutAction;

        this.monitoring =
                true;

        Window
                .getWindows()
                .addListener(
                        windowListener
                );

        for (
                Window window
                : new ArrayList<>(
                        Window.getWindows()
                )
        ) {

            registerWindow(
                    window
            );
        }

        idleTimer.setDuration(
                Duration.minutes(
                        timeoutMinutes
                )
        );

        idleTimer.setOnFinished(
                event -> handleTimeout()
        );

        idleTimer.playFromStart();
    }

    /**
     * Detiene el temporizador y elimina todos los
     * listeners instalados.
     */
    public void stopMonitoring() {

        idleTimer.stop();

        idleTimer.setOnFinished(
                null
        );

        Window
                .getWindows()
                .removeListener(
                        windowListener
                );

        for (
                Scene scene
                : new ArrayList<>(
                        monitoredScenes
                )
        ) {

            unregisterScene(
                    scene
            );
        }

        monitoredScenes.clear();

        timeoutAction =
                null;

        monitoring =
                false;
    }

    private void registerWindow(
            Window window
    ) {

        if (
                window == null
                || window.getScene() == null
        ) {

            return;
        }

        registerScene(
                window.getScene()
        );
    }

    private void unregisterWindow(
            Window window
    ) {

        if (
                window == null
                || window.getScene() == null
        ) {

            return;
        }

        unregisterScene(
                window.getScene()
        );
    }

    private void registerScene(
            Scene scene
    ) {

        if (
                scene == null
                || !monitoredScenes.add(scene)
        ) {

            return;
        }

        scene.addEventFilter(
                MouseEvent.MOUSE_MOVED,
                mouseActivityHandler
        );

        scene.addEventFilter(
                MouseEvent.MOUSE_CLICKED,
                mouseActivityHandler
        );

        scene.addEventFilter(
                MouseEvent.MOUSE_DRAGGED,
                mouseActivityHandler
        );

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                keyboardActivityHandler
        );
    }

    private void unregisterScene(
            Scene scene
    ) {

        if (
                scene == null
                || !monitoredScenes.remove(scene)
        ) {

            return;
        }

        scene.removeEventFilter(
                MouseEvent.MOUSE_MOVED,
                mouseActivityHandler
        );

        scene.removeEventFilter(
                MouseEvent.MOUSE_CLICKED,
                mouseActivityHandler
        );

        scene.removeEventFilter(
                MouseEvent.MOUSE_DRAGGED,
                mouseActivityHandler
        );

        scene.removeEventFilter(
                KeyEvent.KEY_PRESSED,
                keyboardActivityHandler
        );
    }

    private void resetTimer() {

        if (monitoring) {

            idleTimer.playFromStart();
        }
    }

    private void handleTimeout() {

        Runnable action =
                timeoutAction;

        stopMonitoring();

        if (action != null) {

            action.run();
        }
    }

    public boolean isMonitoring() {

        return monitoring;
    }

    public int getTimeoutMinutes() {

        return timeoutMinutes;
    }

    static void validarTimeoutMinutes(
            int timeoutMinutes
    ) {

        if (
                timeoutMinutes
                < PreferenciaSesionLimits.MIN
                || timeoutMinutes
                > PreferenciaSesionLimits.MAX
        ) {

            throw new IllegalArgumentException(
                    "El tiempo de inactividad debe estar "
                    + "entre "
                    + PreferenciaSesionLimits.MIN
                    + " y "
                    + PreferenciaSesionLimits.MAX
                    + " minutos."
            );
        }
    }

    /**
     * Valores duplicados deliberadamente como límites
     * técnicos del temporizador para mantener desacoplado
     * el administrador del modelo de persistencia.
     */
    private static final class PreferenciaSesionLimits {

        private static final int MIN = 1;

        private static final int MAX = 120;

        private PreferenciaSesionLimits() {
        }
    }
}
