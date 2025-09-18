package com.simtelecom.model;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

/**
 * Componente receptor que coordina la recepción, desencriptación y decodificación
 */
public class Receptor extends ObservableModel {

    private final Desencriptador desencriptador;
    private final Decodificador decodificador;
    private boolean procesando;
    private String ultimoError;
    private String identificador;
    private List<Mensaje> mensajesRecibidos;
    private Mensaje mensajeActual;

    /**
     * Constructor del receptor
     * @param identificador Identificador único del receptor
     */
    public Receptor(String identificador) {
        this.identificador = identificador != null ? identificador : "RECEPTOR-001";
        this.desencriptador = new Desencriptador();
        this.decodificador = new Decodificador();
        this.procesando = false;
        this.ultimoError = null;
        this.mensajesRecibidos = new ArrayList<>();

        // Configurar listeners para los componentes
        configurarListeners();
    }

    /**
     * Configura los listeners para los componentes internos
     */
    private void configurarListeners() {
        desencriptador.addListener((eventType, data) -> {
            // Reenviar eventos del desencriptador
            notifyListeners("RECEPTOR_" + eventType, data);
        });

        decodificador.addListener((eventType, data) -> {
            // Reenviar eventos del decodificador
            notifyListeners("RECEPTOR_" + eventType, data);
        });
    }

    /**
     * Configura el tipo de desencriptación (debe coincidir con el emisor)
     * @param tipo Tipo de encriptación usado
     * @param clave Clave de encriptación usada
     */
    public void configurarDesencriptacion(Encriptador.TipoEncriptacion tipo, String clave) {
        desencriptador.configurar(tipo, clave);
    }

    /**
     * Recibe y procesa un mensaje completo
     * @param mensaje Mensaje recibido del canal
     * @return CompletableFuture con el resultado del procesamiento
     */
    public CompletableFuture<Boolean> recibirMensaje(Mensaje mensaje) {
        return CompletableFuture.supplyAsync(() -> {
            if (procesando) {
                setError("Receptor ya está procesando otro mensaje");
                return false;
            }

            if (mensaje == null) {
                setError("No se puede procesar un mensaje nulo");
                return false;
            }

            try {
                procesando = true;
                ultimoError = null;
                mensajeActual = mensaje;

                notifyListeners(EventTypes.MESSAGE_RECEIVED, mensaje);

                // Paso 1: Desencriptar
                notifyListeners("RECEPTOR_DECRYPTING_START", mensaje);
                if (!desencriptador.desencriptar(mensaje)) {
                    setError("Error en la desencriptación: " + desencriptador.getUltimoError());
                    return false;
                }

                // Paso 2: Decodificar
                notifyListeners("RECEPTOR_DECODING_START", mensaje);
                if (!decodificador.decodificar(mensaje)) {
                    setError("Error en la decodificación: " + decodificador.getUltimoError());
                    return false;
                }

                // Marcar como completado
                mensaje.setEstado(Mensaje.EstadoMensaje.COMPLETADO);
                mensajesRecibidos.add(mensaje);

                notifyListeners("RECEPTOR_MESSAGE_PROCESSED", mensaje);
                notifyListeners(EventTypes.PROCESSING_COMPLETED, mensaje);

                return true;

            } catch (Exception e) {
                setError("Error inesperado durante la recepción: " + e.getMessage());
                if (mensaje != null) {
                    mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
                }
                return false;

            } finally {
                procesando = false;
                mensajeActual = null;
            }
        });
    }

    /**
     * Recibe un mensaje paso a paso con callbacks
     * @param mensaje Mensaje a procesar
     * @param onStep Callback ejecutado en cada paso
     * @param onComplete Callback ejecutado al completar
     */
    public void recibirMensajePasos(Mensaje mensaje,
                                    StepCallback onStep,
                                    CompleteCallback onComplete) {

        Thread thread = new Thread(() -> {
            try {
                boolean exito = true;
                mensajeActual = mensaje;

                // Paso 1: Desencriptación
                if (onStep != null) onStep.onStep("DECRYPTING", mensaje);
                if (!desencriptador.desencriptar(mensaje)) {
                    exito = false;
                    setError("Error en desencriptación");
                }

                if (exito) {
                    // Paso 2: Decodificación
                    if (onStep != null) onStep.onStep("DECODING", mensaje);
                    if (!decodificador.decodificar(mensaje)) {
                        exito = false;
                        setError("Error en decodificación");
                    }
                }

                if (exito) {
                    mensaje.setEstado(Mensaje.EstadoMensaje.COMPLETADO);
                    mensajesRecibidos.add(mensaje);
                }

                if (onComplete != null) {
                    onComplete.onComplete(exito, mensaje);
                }

            } catch (Exception e) {
                setError("Error inesperado: " + e.getMessage());
                if (onComplete != null) {
                    onComplete.onComplete(false, mensaje);
                }
            } finally {
                mensajeActual = null;
            }
        });

        thread.setName("Receptor-" + identificador + "-Thread");
        thread.start();
    }

    /**
     * Obtiene el último mensaje recibido
     * @return Último mensaje o null si no hay mensajes
     */
    public Mensaje getUltimoMensaje() {
        if (mensajesRecibidos.isEmpty()) {
            return null;
        }
        return mensajesRecibidos.get(mensajesRecibidos.size() - 1);
    }

    /**
     * Obtiene todos los mensajes recibidos
     * @return Lista de mensajes recibidos
     */
    public List<Mensaje> getMensajesRecibidos() {
        return new ArrayList<>(mensajesRecibidos);
    }

    /**
     * Obtiene el número de mensajes recibidos exitosamente
     * @return Contador de mensajes
     */
    public int getContadorMensajes() {
        return mensajesRecibidos.size();
    }

    /**
     * Obtiene el número de mensajes con errores
     * @return Contador de errores
     */
    public int getContadorErrores() {
        return (int) mensajesRecibidos.stream()
                .filter(Mensaje::tieneError)
                .count();
    }

    /**
     * Calcula la tasa de éxito de recepción
     * @return Porcentaje de éxito (0-100)
     */
    public double getTasaExito() {
        if (mensajesRecibidos.isEmpty()) {
            return 0.0;
        }

        long exitosos = mensajesRecibidos.stream()
                .filter(m -> !m.tieneError())
                .count();

        return (double) exitosos / mensajesRecibidos.size() * 100.0;
    }

    /**
     * Obtiene el estado actual del receptor
     * @return Información del estado
     */
    public EstadoReceptor getEstado() {
        return new EstadoReceptor(
                identificador,
                procesando,
                ultimoError,
                mensajesRecibidos.size(),
                getContadorErrores(),
                getTasaExito(),
                desencriptador.getTipoEncriptacion()
        );
    }

    /**
     * Limpia el historial de mensajes recibidos
     */
    public void limpiarHistorial() {
        mensajesRecibidos.clear();
        notifyListeners("RECEPTOR_HISTORY_CLEARED", null);
    }

    // Getters
    public boolean estaProcesando() {
        return procesando;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Mensaje getMensajeActual() {
        return mensajeActual;
    }

    public Desencriptador getDesencriptador() {
        return desencriptador;
    }

    public Decodificador getDecodificador() {
        return decodificador;
    }

    /**
     * Establece un error y notifica
     */
    private void setError(String error) {
        this.ultimoError = error;
        notifyListeners(EventTypes.ERROR_OCCURRED, error);
    }

    /**
     * Limpia el último error
     */
    public void limpiarError() {
        this.ultimoError = null;
        desencriptador.limpiarError();
        decodificador.limpiarError();
    }

    /**
     * Callback para notificación de pasos
     */
    @FunctionalInterface
    public interface StepCallback {
        void onStep(String stepName, Mensaje mensaje);
    }

    /**
     * Callback para completar proceso
     */
    @FunctionalInterface
    public interface CompleteCallback {
        void onComplete(boolean success, Mensaje mensaje);
    }

    /**
     * Clase para representar el estado del receptor
     */
    public static class EstadoReceptor {
        private final String identificador;
        private final boolean procesando;
        private final String error;
        private final int mensajesRecibidos;
        private final int mensajesConError;
        private final double tasaExito;
        private final Encriptador.TipoEncriptacion tipoEncriptacion;

        public EstadoReceptor(String identificador, boolean procesando, String error,
                              int mensajesRecibidos, int mensajesConError, double tasaExito,
                              Encriptador.TipoEncriptacion tipoEncriptacion) {
            this.identificador = identificador;
            this.procesando = procesando;
            this.error = error;
            this.mensajesRecibidos = mensajesRecibidos;
            this.mensajesConError = mensajesConError;
            this.tasaExito = tasaExito;
            this.tipoEncriptacion = tipoEncriptacion;
        }

        // Getters
        public String getIdentificador() { return identificador; }
        public boolean estaProcesando() { return procesando; }
        public String getError() { return error; }
        public int getMensajesRecibidos() { return mensajesRecibidos; }
        public int getMensajesConError() { return mensajesConError; }
        public double getTasaExito() { return tasaExito; }
        public Encriptador.TipoEncriptacion getTipoEncriptacion() { return tipoEncriptacion; }

        @Override
        public String toString() {
            return String.format("EstadoReceptor{id=%s, procesando=%s, mensajes=%d, éxito=%.1f%%}",
                    identificador, procesando, mensajesRecibidos, tasaExito);
        }
    }
}
