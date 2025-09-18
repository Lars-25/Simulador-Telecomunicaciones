package com.simtelecom.model;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * Simula un canal de comunicación con posibles interferencias y errores
 */
public class Canal extends ObservableModel {

    /**
     * Tipos de canal disponibles
     */
    public enum TipoCanal {
        IDEAL("Canal Ideal - Sin ruido"),
        RUIDOSO("Canal con Ruido"),
        INTERMITENTE("Canal Intermitente"),
        CON_PERDIDAS("Canal con Pérdidas");

        private final String descripcion;

        TipoCanal(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private boolean transmitiendo;
    private String ultimoError;
    private TipoCanal tipoCanal;
    private double probabilidadError;
    private double velocidadTransmision; // bits por segundo
    private final Random random;
    private List<Character> bitBuffer;
    private int bitsTransmitidos;
    private int erroresIntroducidos;

    /**
     * Constructor del canal
     */
    public Canal() {
        this.transmitiendo = false;
        this.ultimoError = null;
        this.tipoCanal = TipoCanal.IDEAL;
        this.probabilidadError = 0.0;
        this.velocidadTransmision = 1000.0; // 1000 bps por defecto
        this.random = new Random();
        this.bitBuffer = new ArrayList<>();
        this.bitsTransmitidos = 0;
        this.erroresIntroducidos = 0;
    }

    /**
     * Configura el canal
     * @param tipo Tipo de canal
     * @param probabilidadError Probabilidad de error (0.0 - 1.0)
     * @param velocidad Velocidad en bits por segundo
     */
    public void configurar(TipoCanal tipo, double probabilidadError, double velocidad) {
        this.tipoCanal = tipo != null ? tipo : TipoCanal.IDEAL;
        this.probabilidadError = Math.max(0.0, Math.min(1.0, probabilidadError));
        this.velocidadTransmision = Math.max(1.0, velocidad);

        // Ajustar probabilidad según el tipo de canal
        switch (this.tipoCanal) {
            case IDEAL:
                this.probabilidadError = 0.0;
                break;
            case RUIDOSO:
                this.probabilidadError = Math.max(this.probabilidadError, 0.01);
                break;
            case INTERMITENTE:
                this.probabilidadError = Math.max(this.probabilidadError, 0.05);
                break;
            case CON_PERDIDAS:
                this.probabilidadError = Math.max(this.probabilidadError, 0.1);
                break;
        }
    }

    /**
     * Transmite un mensaje a través del canal
     * @param mensaje Mensaje a transmitir
     * @return true si la transmisión fue exitosa
     */
    public boolean transmitir(Mensaje mensaje) {
        if (mensaje == null) {
            setError("Mensaje no puede ser nulo");
            return false;
        }

        if (mensaje.getContenidoEncriptado() == null || mensaje.getContenidoEncriptado().isEmpty()) {
            setError("No hay contenido encriptado para transmitir");
            return false;
        }

        if (transmitiendo) {
            setError("Canal ya está transmitiendo otro mensaje");
            return false;
        }

        try {
            transmitiendo = true;
            ultimoError = null;
            bitsTransmitidos = 0;
            erroresIntroducidos = 0;

            notifyListeners(EventTypes.PROCESSING_STARTED, mensaje);
            mensaje.setEstado(Mensaje.EstadoMensaje.TRANSMITIENDO);

            String contenidoOriginal = mensaje.getContenidoEncriptado();
            StringBuilder contenidoTransmitido = new StringBuilder();

            // Simular transmisión bit por bit
            for (int i = 0; i < contenidoOriginal.length(); i++) {
                char bitOriginal = contenidoOriginal.charAt(i);
                char bitTransmitido = aplicarEfectosCanal(bitOriginal);

                contenidoTransmitido.append(bitTransmitido);
                bitsTransmitidos++;

                if (bitOriginal != bitTransmitido) {
                    erroresIntroducidos++;
                }

                // Simular velocidad de transmisión
                if (velocidadTransmision < 10000) { // Solo para velocidades bajas
                    Thread.sleep((long)(1000.0 / velocidadTransmision));
                }

                // Notificar progreso cada cierto número de bits
                if (i % 8 == 0) {
                    notifyListeners("TRANSMISSION_PROGRESS",
                            (double)i / contenidoOriginal.length());
                }
            }

            // Actualizar el mensaje con el contenido posiblemente alterado
            mensaje.setContenidoEncriptado(contenidoTransmitido.toString());
            mensaje.setEstado(Mensaje.EstadoMensaje.RECIBIDO);

            notifyListeners(EventTypes.MESSAGE_TRANSMITTED, mensaje);
            notifyListeners(EventTypes.PROCESSING_COMPLETED, mensaje);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setError("Transmisión interrumpida");
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } catch (Exception e) {
            setError("Error durante la transmisión: " + e.getMessage());
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } finally {
            transmitiendo = false;
        }
    }

    /**
     * Aplica los efectos del canal a un bit individual
     * @param bitOriginal Bit original
     * @return Bit posiblemente alterado
     */
    private char aplicarEfectosCanal(char bitOriginal) {
        switch (tipoCanal) {
            case IDEAL:
                return bitOriginal;

            case RUIDOSO:
            case INTERMITENTE:
            case CON_PERDIDAS:
                if (random.nextDouble() < probabilidadError) {
                    // Invertir el bit (error de transmisión)
                    return bitOriginal == '0' ? '1' : '0';
                }
                return bitOriginal;

            default:
                return bitOriginal;
        }
    }

    /**
     * Transmite un mensaje de forma asíncrona
     * @param mensaje Mensaje a transmitir
     * @param callback Callback a ejecutar cuando termine
     */
    public void transmitirAsync(Mensaje mensaje, Runnable callback) {
        Thread thread = new Thread(() -> {
            boolean exito = transmitir(mensaje);
            if (callback != null) {
                callback.run();
            }
        });
        thread.setName("Canal-Thread");
        thread.start();
    }

    /**
     * Calcula la tasa de error de bits (BER)
     * @return BER como porcentaje
     */
    public double calcularBER() {
        if (bitsTransmitidos == 0) {
            return 0.0;
        }
        return (double)erroresIntroducidos / bitsTransmitidos * 100.0;
    }

    // Getters
    public boolean estaTransmitiendo() {
        return transmitiendo;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public TipoCanal getTipoCanal() {
        return tipoCanal;
    }

    public double getProbabilidadError() {
        return probabilidadError;
    }

    public double getVelocidadTransmision() {
        return velocidadTransmision;
    }

    public int getBitsTransmitidos() {
        return bitsTransmitidos;
    }

    public int getErroresIntroducidos() {
        return erroresIntroducidos;
    }

    /**
     * Obtiene estadísticas del canal
     * @return String con estadísticas
     */
    public String getEstadisticas() {
        return String.format("Canal{tipo=%s, BER=%.2f%%, bits=%d, errores=%d}",
                tipoCanal, calcularBER(), bitsTransmitidos, erroresIntroducidos);
    }

    /**
     * Reinicia las estadísticas del canal
     */
    public void reiniciarEstadisticas() {
        bitsTransmitidos = 0;
        erroresIntroducidos = 0;
        bitBuffer.clear();
    }

    /**
     * Establece un error y notifica a los listeners
     */
    private void setError(String error) {
        this.ultimoError = error;
        notifyListeners(EventTypes.ERROR_OCCURRED, error);
    }
}
