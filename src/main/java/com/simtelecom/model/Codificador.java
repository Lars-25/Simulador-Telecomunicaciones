package com.simtelecom.model;
import com.simtelecom.util.BinaryUtil;

/**
 * Responsable de codificar mensajes de texto a formato binario
 */
public class Codificador extends ObservableModel {

    private boolean procesando;
    private String ultimoError;

    /**
     * Constructor del codificador
     */
    public Codificador() {
        this.procesando = false;
        this.ultimoError = null;
    }

    /**
     * Codifica un mensaje de texto a binario
     * @param mensaje Mensaje a codificar
     * @return true si la codificación fue exitosa
     */
    public boolean codificar(Mensaje mensaje) {
        if (mensaje == null) {
            setError("Mensaje no puede ser nulo");
            return false;
        }

        if (procesando) {
            setError("Codificador ya está procesando otro mensaje");
            return false;
        }

        try {
            procesando = true;
            ultimoError = null;

            notifyListeners(EventTypes.PROCESSING_STARTED, mensaje);

            // Simular tiempo de procesamiento (opcional para animaciones)
            Thread.sleep(100);

            // Realizar la codificación
            String contenidoBinario = BinaryUtil.textToBinary(mensaje.getContenidoOriginal());
            mensaje.setContenidoBinario(contenidoBinario);
            mensaje.setContenidoCodificado(contenidoBinario); // Por ahora, mismo contenido
            mensaje.setEstado(Mensaje.EstadoMensaje.CODIFICADO);

            notifyListeners(EventTypes.MESSAGE_ENCODED, mensaje);
            notifyListeners(EventTypes.PROCESSING_COMPLETED, mensaje);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setError("Codificación interrumpida");
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } catch (Exception e) {
            setError("Error durante la codificación: " + e.getMessage());
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } finally {
            procesando = false;
        }
    }

    /**
     * Codifica un mensaje de forma asíncrona
     * @param mensaje Mensaje a codificar
     * @param callback Callback a ejecutar cuando termine (puede ser null)
     */
    public void codificarAsync(Mensaje mensaje, Runnable callback) {
        Thread thread = new Thread(() -> {
            boolean exito = codificar(mensaje);
            if (callback != null) {
                callback.run();
            }
        });
        thread.setName("Codificador-Thread");
        thread.start();
    }

    /**
     * Verifica si el codificador está procesando
     * @return true si está procesando
     */
    public boolean estaProcesando() {
        return procesando;
    }

    /**
     * Obtiene el último error ocurrido
     * @return Descripción del último error o null si no hay errores
     */
    public String getUltimoError() {
        return ultimoError;
    }

    /**
     * Establece un error y notifica a los listeners
     * @param error Descripción del error
     */
    private void setError(String error) {
        this.ultimoError = error;
        notifyListeners(EventTypes.ERROR_OCCURRED, error);
    }

    /**
     * Limpia el último error registrado
     */
    public void limpiarError() {
        this.ultimoError = null;
    }

    /**
     * Obtiene estadísticas del codificador
     * @return String con información del estado
     */
    public String getEstadisticas() {
        return String.format("Codificador{procesando=%s, tieneError=%s}",
                procesando, ultimoError != null);
    }
}