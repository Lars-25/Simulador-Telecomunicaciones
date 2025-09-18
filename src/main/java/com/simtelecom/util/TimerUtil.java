package com.simtelecom.util;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utilidades para manejo de timers y animaciones
 */
public final class TimerUtil {

    private static final Map<String, Timer> SWING_TIMERS = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledFuture<?>> SCHEDULED_TASKS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(4);

    private TimerUtil() {
        throw new AssertionError("Esta clase no debe ser instanciada");
    }

    /**
     * Crea un timer de Swing para animaciones de UI
     * @param name Nombre único del timer
     * @param delay Delay en milisegundos
     * @param listener Action listener
     * @return Timer creado
     */
    public static Timer createSwingTimer(String name, int delay, ActionListener listener) {
        stopSwingTimer(name); // Detener timer existente si existe

        Timer timer = new Timer(delay, listener);
        SWING_TIMERS.put(name, timer);
        return timer;
    }

    /**
     * Inicia un timer de Swing
     * @param name Nombre del timer
     * @return true si se inició correctamente
     */
    public static boolean startSwingTimer(String name) {
        Timer timer = SWING_TIMERS.get(name);
        if (timer != null) {
            timer.start();
            return true;
        }
        return false;
    }

    /**
     * Detiene un timer de Swing
     * @param name Nombre del timer
     * @return true si se detuvo correctamente
     */
    public static boolean stopSwingTimer(String name) {
        Timer timer = SWING_TIMERS.get(name);
        if (timer != null) {
            timer.stop();
            return true;
        }
        return false;
    }

    /**
     * Remueve un timer de Swing
     * @param name Nombre del timer
     */
    public static void removeSwingTimer(String name) {
        Timer timer = SWING_TIMERS.remove(name);
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Programa una tarea para ejecutar después de un delay
     * @param name Nombre único de la tarea
     * @param task Tarea a ejecutar
     * @param delay Delay en milisegundos
     * @return ScheduledFuture de la tarea
     */
    public static ScheduledFuture<?> schedule(String name, Runnable task, long delay) {
        cancelScheduledTask(name); // Cancelar tarea existente si existe

        ScheduledFuture<?> future = SCHEDULER.schedule(task, delay, TimeUnit.MILLISECONDS);
        SCHEDULED_TASKS.put(name, future);
        return future;
    }

    /**
     * Programa una tarea para ejecutar repetidamente
     * @param name Nombre único de la tarea
     * @param task Tarea a ejecutar
     * @param initialDelay Delay inicial en milisegundos
     * @param period Período entre ejecuciones en milisegundos
     * @return ScheduledFuture de la tarea
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(String name, Runnable task,
                                                         long initialDelay, long period) {
        cancelScheduledTask(name); // Cancelar tarea existente si existe

        ScheduledFuture<?> future = SCHEDULER.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
        SCHEDULED_TASKS.put(name, future);
        return future;
    }

    /**
     * Programa una tarea para ejecutar con delay fijo entre ejecuciones
     * @param name Nombre único de la tarea
     * @param task Tarea a ejecutar
     * @param initialDelay Delay inicial en milisegundos
     * @param delay Delay entre ejecuciones en milisegundos
     * @return ScheduledFuture de la tarea
     */
    public static ScheduledFuture<?> scheduleWithFixedDelay(String name, Runnable task,
                                                            long initialDelay, long delay) {
        cancelScheduledTask(name); // Cancelar tarea existente si existe

        ScheduledFuture<?> future = SCHEDULER.scheduleWithFixedDelay(task, initialDelay, delay, TimeUnit.MILLISECONDS);
        SCHEDULED_TASKS.put(name, future);
        return future;
    }

    /**
     * Cancela una tarea programada
     * @param name Nombre de la tarea
     * @return true si se canceló correctamente
     */
    public static boolean cancelScheduledTask(String name) {
        ScheduledFuture<?> future = SCHEDULED_TASKS.remove(name);
        if (future != null) {
            return future.cancel(false);
        }
        return false;
    }

    /**
     * Ejecuta una tarea después de un delay usando el hilo EDT de Swing
     * @param task Tarea a ejecutar
     * @param delay Delay en milisegundos
     */
    public static void executeOnSwingThread(Runnable task, int delay) {
        Timer timer = new Timer(delay, e -> {
            task.run();
            ((Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Crea un timer para animación con interpolación
     * @param name Nombre del timer
     * @param duration Duración total en milisegundos
     * @param fps Frames por segundo
     * @param callback Callback que recibe el progreso (0.0-1.0)
     * @param onComplete Callback ejecutado al completar (opcional)
     * @return Timer creado
     */
    public static Timer createAnimationTimer(String name, int duration, int fps,
                                             ProgressCallback callback, Runnable onComplete) {
        stopSwingTimer(name);

        int delay = 1000 / fps;
        long startTime = System.currentTimeMillis();

        Timer timer = new Timer(delay, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / duration);

            callback.onProgress(progress);

            if (progress >= 1.0) {
                ((Timer)e.getSource()).stop();
                if (onComplete != null) {
                    onComplete.run();
                }
                SWING_TIMERS.remove(name);
            }
        });

        SWING_TIMERS.put(name, timer);
        return timer;
    }

    /**
     * Crea un timer para pulso (parpadeo)
     * @param name Nombre del timer
     * @param interval Intervalo del pulso en milisegundos
     * @param callback Callback que recibe el estado (true/false)
     * @return Timer creado
     */
    public static Timer createPulseTimer(String name, int interval, PulseCallback callback) {
        stopSwingTimer(name);

        boolean[] state = {false};

        Timer timer = new Timer(interval, e -> {
            state[0] = !state[0];
            callback.onPulse(state[0]);
        });

        SWING_TIMERS.put(name, timer);
        return timer;
    }

    /**
     * Obtiene el número de timers activos de Swing
     * @return Número de timers
     */
    public static int getActiveSwingTimersCount() {
        return (int) SWING_TIMERS.values().stream()
                .filter(Timer::isRunning)
                .count();
    }

    /**
     * Obtiene el número de tareas programadas activas
     * @return Número de tareas
     */
    public static int getActiveScheduledTasksCount() {
        return (int) SCHEDULED_TASKS.values().stream()
                .filter(f -> !f.isDone() && !f.isCancelled())
                .count();
    }

    /**
     * Detiene todos los timers y tareas
     */
    public static void stopAll() {
        // Detener todos los timers de Swing
        SWING_TIMERS.values().forEach(Timer::stop);
        SWING_TIMERS.clear();

        // Cancelar todas las tareas programadas
        SCHEDULED_TASKS.values().forEach(f -> f.cancel(false));
        SCHEDULED_TASKS.clear();
    }

    /**
     * Libera todos los recursos
     */
    public static void shutdown() {
        stopAll();
        SCHEDULER.shutdown();
        try {
            if (!SCHEDULER.awaitTermination(1, TimeUnit.SECONDS)) {
                SCHEDULER.shutdownNow();
            }
        } catch (InterruptedException e) {
            SCHEDULER.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Valida y ajusta un delay dentro de los límites permitidos
     * @param delay Delay a validar
     * @return Delay ajustado
     */
    public static int validateDelay(int delay) {
        return Math.max(Constants.MIN_ANIMATION_DELAY,
                Math.min(Constants.MAX_ANIMATION_DELAY, delay));
    }

    /**
     * Callback para progreso de animación
     */
    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(double progress);
    }

    /**
     * Callback para pulso
     */
    @FunctionalInterface
    public interface PulseCallback {
        void onPulse(boolean state);
    }
}