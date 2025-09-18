package com.simtelecom.model;

import com.simtelecom.util.BinaryUtil;

/**
 * Responsable de decodificar mensajes de formato binario a texto
 */
public class Decodificador extends ObservableModel {

    private boolean procesando;
    private String ultimoError;

    /**
     * Constructor del decodificador
     */
    public Decodificador() {
        this.procesando = false;
        this.ultimoError = null;
    }

    /**
     * Decodifica un mensaje de binario a texto
     * @param mensaje Mensaje a decodificar
     * @return true si la decodificación fue exitosa
     */
    public boolean decodificar(Mensaje mensaje) {
        if (mensaje == null) {
            setError("Mensaje no puede ser nulo");
            return false;
        }

        if (mensaje.getContenidoCodificado() == null || mensaje.getContenidoCodificado().isEmpty()) {
            setError("No hay contenido codificado para decodificar");
            return false;
        }

        if (procesando) {
            setError("Decodificador ya está procesando otro mensaje");
            return false;
        }

        try {
            procesando = true;
            ultimoError = null;

            notifyListeners(EventTypes.PROCESSING_STARTED, mensaje);

            // Simular tiempo de procesamiento
            Thread.sleep(100);

            // Verificar que el contenido es binario válido
            String contenidoCodificado = mensaje.getContenidoCodificado();
            if (!BinaryUtil.isBinaryString(contenidoCodificado)) {
                throw new IllegalArgumentException("El contenido no es una cadena binaria válida");
            }

            // Realizar la decodificación
            String textoDecodificado = BinaryUtil.binaryToText(contenidoCodificado);

            // Verificar que la decodificación coincide con el original
            if (!textoDecodificado.equals(mensaje.getContenidoOriginal())) {
                setError("El texto decodificado no coincide con el original");
                mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
                return false;
            }

            mensaje.setEstado(Mensaje.EstadoMensaje.DECODIFICADO);

            notifyListeners(EventTypes.MESSAGE_DECODED, mensaje);
            notifyListeners(EventTypes.PROCESSING_COMPLETED, mensaje);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setError("Decodificación interrumpida");
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } catch (Exception e) {
            setError("Error durante la decodificación: " + e.getMessage());
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } finally {
            procesando = false;
        }
    }

    /**
     * Decodifica un mensaje de forma asíncrona
     * @param mensaje Mensaje a decodificar
     * @param callback Callback a ejecutar cuando termine
     */
    public void decodificarAsync(Mensaje mensaje, Runnable callback) {
        Thread thread = new Thread(() -> {
            boolean exito = decodificar(mensaje);
            if (callback != null) {
                callback.run();
            }
        });
        thread.setName("Decodificador-Thread");
        thread.start();
    }

    /**
     * Verifica si el decodificador está procesando
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
     * Obtiene estadísticas del decodificador
     * @return String con información del estado
     */
    public String getEstadisticas() {
        return String.format("Decodificador{procesando=%s, tieneError=%s}",
                procesando, ultimoError != null);
    }
}
