package com.simtelecom.controller;

import com.simtelecom.model.*;
import com.simtelecom.view.ModelListener;
import com.simtelecom.util.Constants;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

/**
 * Controlador principal que coordina toda la simulación de telecomunicaciones
 */
public class SimulationController extends ObservableModel {

    private final Emisor emisor;
    private final Receptor receptor;
    private final Canal canal;
    private final List<ModelListener> viewListeners;

    private boolean simulacionActiva;
    private boolean modoPasoAPaso;
    private String ultimoError;
    private int mensajesEnviados;
    private int mensajesRecibidos;
    private long tiempoInicioSimulacion;

    /**
     * Estados de la simulación
     */
    public enum EstadoSimulacion {
        DETENIDA("Simulación detenida"),
        CONFIGURANDO("Configurando parámetros"),
        EJECUTANDO("Simulación en ejecución"),
        PAUSADA("Simulación pausada"),
        COMPLETADA("Simulación completada"),
        ERROR("Error en la simulación");

        private final String descripcion;

        EstadoSimulacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private EstadoSimulacion estadoActual;

    /**
     * Constructor del controlador de simulación
     */
    public SimulationController() {
        this.emisor = new Emisor("EMISOR-PRINCIPAL");
        this.receptor = new Receptor("RECEPTOR-PRINCIPAL");
        this.canal = new Canal();
        this.viewListeners = new ArrayList<>();

        this.simulacionActiva = false;
        this.modoPasoAPaso = false;
        this.ultimoError = null;
        this.mensajesEnviados = 0;
        this.mensajesRecibidos = 0;
        this.estadoActual = EstadoSimulacion.DETENIDA;

        configurarComponentes();
    }

    /**
     * Configura los listeners de los componentes
     */
    private void configurarComponentes() {
        // Conectar emisor con canal
        emisor.setCanal(canal);

        // Configurar listeners del emisor
        emisor.addListener((eventType, data) -> {
            handleEmisorEvent(eventType, data);
        });

        // Configurar listeners del receptor
        receptor.addListener((eventType, data) -> {
            handleReceptorEvent(eventType, data);
        });

        // Configurar listeners del canal
        canal.addListener((eventType, data) -> {
            handleCanalEvent(eventType, data);
        });
    }

    /**
     * Maneja eventos del emisor
     */
    private void handleEmisorEvent(String eventType, Object data) {
        notifyListeners("CONTROLLER_" + eventType, data);

        if (eventType.equals("EMISOR_MESSAGE_SENT") && data instanceof Mensaje) {
            mensajesEnviados++;

            // Auto-recibir el mensaje en el receptor
            Mensaje mensaje = (Mensaje) data;
            if (!modoPasoAPaso) {
                receptor.recibirMensaje(mensaje);
            }
        }
    }

    /**
     * Maneja eventos del receptor
     */
    private void handleReceptorEvent(String eventType, Object data) {
        notifyListeners("CONTROLLER_" + eventType, data);

        if (eventType.equals("RECEPTOR_MESSAGE_PROCESSED")) {
            mensajesRecibidos++;
        }
    }

    /**
     * Maneja eventos del canal
     */
    private void handleCanalEvent(String eventType, Object data) {
        notifyListeners("CONTROLLER_" + eventType, data);
    }

    /**
     * Configura los parámetros de encriptación
     * @param tipo Tipo de encriptación
     * @param clave Clave de encriptación
     */
    public void configurarEncriptacion(Encriptador.TipoEncriptacion tipo, String clave) {
        emisor.configurarEncriptacion(tipo, clave);
        receptor.configurarDesencriptacion(tipo, clave);

        notifyListeners("ENCRYPTION_CONFIGURED", tipo);
    }

    /**
     * Configura los parámetros del canal
     * @param tipo Tipo de canal
     * @param probabilidadError Probabilidad de error
     * @param velocidad Velocidad de transmisión
     */
    public void configurarCanal(Canal.TipoCanal tipo, double probabilidadError, double velocidad) {
        canal.configurar(tipo, probabilidadError, velocidad);

        notifyListeners("CHANNEL_CONFIGURED", canal.getEstadisticas());
    }

    /**
     * Inicia una simulación completa
     * @param mensaje Mensaje a transmitir
     * @return CompletableFuture con el resultado
     */
    public CompletableFuture<Boolean> iniciarSimulacion(String mensaje) {
        if (simulacionActiva) {
            setError("Ya hay una simulación en progreso");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                simulacionActiva = true;
                estadoActual = EstadoSimulacion.EJECUTANDO;
                tiempoInicioSimulacion = System.currentTimeMillis();

                notifyListeners("SIMULATION_STARTED", mensaje);

                // Enviar mensaje
                boolean exitoEnvio = emisor.enviarMensaje(mensaje).get();
                if (!exitoEnvio) {
                    setError("Error al enviar mensaje: " + emisor.getUltimoError());
                    return false;
                }

                // El receptor se encarga automáticamente en handleEmisorEvent
                // si no está en modo paso a paso

                estadoActual = EstadoSimulacion.COMPLETADA;
                notifyListeners("SIMULATION_COMPLETED", getEstadisticasSimulacion());

                return true;

            } catch (Exception e) {
                setError("Error durante la simulación: " + e.getMessage());
                estadoActual = EstadoSimulacion.ERROR;
                return false;

            } finally {
                simulacionActiva = false;
            }
        });
    }

    /**
     * Inicia simulación paso a paso
     * @param mensaje Mensaje a transmitir
     */
    public void iniciarSimulacionPasoAPaso(String mensaje) {
        if (simulacionActiva) {
            setError("Ya hay una simulación en progreso");
            return;
        }

        modoPasoAPaso = true;
        simulacionActiva = true;
        estadoActual = EstadoSimulacion.EJECUTANDO;
        tiempoInicioSimulacion = System.currentTimeMillis();

        notifyListeners("SIMULATION_STEP_MODE_STARTED", mensaje);

        // Enviar mensaje paso a paso
        emisor.enviarMensajePasos(mensaje,
                (step, msg) -> {
                    notifyListeners("SIMULATION_STEP", new StepInfo(step, msg, "EMISOR"));
                },
                (success, msg) -> {
                    if (success) {
                        notifyListeners("EMISOR_COMPLETED", msg);
                    } else {
                        setError("Error en emisor: " + emisor.getUltimoError());
                    }
                }
        );
    }

    /**
     * Ejecuta el siguiente paso en modo paso a paso
     * @param mensaje Mensaje a procesar en el receptor
     */
    public void siguientePaso(Mensaje mensaje) {
        if (!modoPasoAPaso || !simulacionActiva) {
            setError("No está en modo paso a paso");
            return;
        }

        receptor.recibirMensajePasos(mensaje,
                (step, msg) -> {
                    notifyListeners("SIMULATION_STEP", new StepInfo(step, msg, "RECEPTOR"));
                },
                (success, msg) -> {
                    if (success) {
                        mensajesRecibidos++;
                        estadoActual = EstadoSimulacion.COMPLETADA;
                        notifyListeners("SIMULATION_COMPLETED", getEstadisticasSimulacion());
                    } else {
                        setError("Error en receptor: " + receptor.getUltimoError());
                        estadoActual = EstadoSimulacion.ERROR;
                    }

                    simulacionActiva = false;
                    modoPasoAPaso = false;
                }
        );
    }

    /**
     * Detiene la simulación actual
     */
    public void detenerSimulacion() {
        simulacionActiva = false;
        modoPasoAPaso = false;
        estadoActual = EstadoSimulacion.DETENIDA;

        notifyListeners("SIMULATION_STOPPED", null);
    }

    /**
     * Reinicia las estadísticas de la simulación
     */
    public void reiniciarEstadisticas() {
        mensajesEnviados = 0;
        mensajesRecibidos = 0;
        canal.reiniciarEstadisticas();
        receptor.limpiarHistorial();

        notifyListeners("STATISTICS_RESET", null);
    }

    /**
     * Obtiene las estadísticas actuales de la simulación
     * @return Objeto con estadísticas
     */
    public EstadisticasSimulacion getEstadisticasSimulacion() {
        long tiempoTranscurrido = simulacionActiva ?
                System.currentTimeMillis() - tiempoInicioSimulacion : 0;

        return new EstadisticasSimulacion(
                mensajesEnviados,
                mensajesRecibidos,
                canal.getBitsTransmitidos(),
                canal.getErroresIntroducidos(),
                canal.calcularBER(),
                receptor.getTasaExito(),
                tiempoTranscurrido,
                estadoActual
        );
    }

    // Getters
    public Emisor getEmisor() { return emisor; }
    public Receptor getReceptor() { return receptor; }
    public Canal getCanal() { return canal; }
    public boolean isSimulacionActiva() { return simulacionActiva; }
    public boolean isModoPasoAPaso() { return modoPasoAPaso; }
    public EstadoSimulacion getEstadoActual() { return estadoActual; }
    public String getUltimoError() { return ultimoError; }

    /**
     * Establece un error
     */
    private void setError(String error) {
        this.ultimoError = error;
        this.estadoActual = EstadoSimulacion.ERROR;
        notifyListeners(EventTypes.ERROR_OCCURRED, error);
    }

    /**
     * Información de paso en simulación paso a paso
     */
    public static class StepInfo {
        private final String stepName;
        private final Mensaje mensaje;
        private final String component;

        public StepInfo(String stepName, Mensaje mensaje, String component) {
            this.stepName = stepName;
            this.mensaje = mensaje;
            this.component = component;
        }

        public String getStepName() { return stepName; }
        public Mensaje getMensaje() { return mensaje; }
        public String getComponent() { return component; }
    }

    /**
     * Estadísticas de la simulación
     */
    public static class EstadisticasSimulacion {
        private final int mensajesEnviados;
        private final int mensajesRecibidos;
        private final int bitsTransmitidos;
        private final int erroresBits;
        private final double ber;
        private final double tasaExito;
        private final long tiempoEjecucion;
        private final EstadoSimulacion estado;

        public EstadisticasSimulacion(int mensajesEnviados, int mensajesRecibidos,
                                      int bitsTransmitidos, int erroresBits, double ber,
                                      double tasaExito, long tiempoEjecucion, EstadoSimulacion estado) {
            this.mensajesEnviados = mensajesEnviados;
            this.mensajesRecibidos = mensajesRecibidos;
            this.bitsTransmitidos = bitsTransmitidos;
            this.erroresBits = erroresBits;
            this.ber = ber;
            this.tasaExito = tasaExito;
            this.tiempoEjecucion = tiempoEjecucion;
            this.estado = estado;
        }

        // Getters
        public int getMensajesEnviados() { return mensajesEnviados; }
        public int getMensajesRecibidos() { return mensajesRecibidos; }
        public int getBitsTransmitidos() { return bitsTransmitidos; }
        public int getErroresBits() { return erroresBits; }
        public double getBer() { return ber; }
        public double getTasaExito() { return tasaExito; }
        public long getTiempoEjecucion() { return tiempoEjecucion; }
        public EstadoSimulacion getEstado() { return estado; }

        @Override
        public String toString() {
            return String.format("EstadisticasSimulacion{mensajes=%d/%d, bits=%d, BER=%.2f%%, éxito=%.1f%%, tiempo=%dms}",
                    mensajesRecibidos, mensajesEnviados, bitsTransmitidos, ber, tasaExito, tiempoEjecucion);
        }
    }
}
