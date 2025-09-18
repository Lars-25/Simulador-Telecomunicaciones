package com.simtelecom.model;

import java.util.Base64;

/**
 * Responsable de desencriptar mensajes usando los mismos algoritmos del Encriptador
 */
public class Desencriptador extends ObservableModel {

    private boolean procesando;
    private String ultimoError;
    private Encriptador.TipoEncriptacion tipoEncriptacion;
    private String clave;

    /**
     * Constructor del desencriptador
     */
    public Desencriptador() {
        this.procesando = false;
        this.ultimoError = null;
        this.tipoEncriptacion = Encriptador.TipoEncriptacion.NONE;
        this.clave = "";
    }

    /**
     * Establece el tipo de encriptación y la clave (debe coincidir con el encriptador)
     * @param tipo Tipo de encriptación usado
     * @param clave Clave usada en la encriptación
     */
    public void configurar(Encriptador.TipoEncriptacion tipo, String clave) {
        this.tipoEncriptacion = tipo != null ? tipo : Encriptador.TipoEncriptacion.NONE;
        this.clave = clave != null ? clave : "";
    }

    /**
     * Desencripta un mensaje según la configuración actual
     * @param mensaje Mensaje a desencriptar
     * @return true si la desencriptación fue exitosa
     */
    public boolean desencriptar(Mensaje mensaje) {
        if (mensaje == null) {
            setError("Mensaje no puede ser nulo");
            return false;
        }

        if (mensaje.getContenidoEncriptado() == null || mensaje.getContenidoEncriptado().isEmpty()) {
            setError("No hay contenido encriptado para desencriptar");
            return false;
        }

        if (procesando) {
            setError("Desencriptador ya está procesando otro mensaje");
            return false;
        }

        try {
            procesando = true;
            ultimoError = null;

            notifyListeners(EventTypes.PROCESSING_STARTED, mensaje);

            // Simular tiempo de procesamiento
            Thread.sleep(150);

            String contenidoDesencriptado;
            String contenidoEncriptado = mensaje.getContenidoEncriptado();

            switch (tipoEncriptacion) {
                case NONE:
                    contenidoDesencriptado = contenidoEncriptado;
                    break;
                case CAESAR:
                    contenidoDesencriptado = desencriptarCesar(contenidoEncriptado);
                    break;
                case XOR:
                    contenidoDesencriptado = desencriptarXOR(contenidoEncriptado);
                    break;
                case BASE64:
                    contenidoDesencriptado = desencriptarBase64(contenidoEncriptado);
                    break;
                default:
                    throw new IllegalStateException("Tipo de encriptación no soportado: " + tipoEncriptacion);
            }

            // Restaurar el contenido codificado original
            mensaje.setContenidoCodificado(contenidoDesencriptado);
            mensaje.setEstado(Mensaje.EstadoMensaje.DESENCRIPTADO);

            notifyListeners(EventTypes.MESSAGE_DECRYPTED, mensaje);
            notifyListeners(EventTypes.PROCESSING_COMPLETED, mensaje);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setError("Desencriptación interrumpida");
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } catch (Exception e) {
            setError("Error durante la desencriptación: " + e.getMessage());
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } finally {
            procesando = false;
        }
    }

    /**
     * Desencriptación César (inversa de la encriptación)
     */
    private String desencriptarCesar(String contenido) {
        if (clave.isEmpty()) {
            return contenido;
        }

        int desplazamiento;
        try {
            desplazamiento = Integer.parseInt(clave) % 2;
        } catch (NumberFormatException e) {
            desplazamiento = 1;
        }

        if (desplazamiento == 0) {
            return contenido;
        }

        // Invertir bits de vuelta (misma operación que encriptar)
        StringBuilder resultado = new StringBuilder();
        for (char c : contenido.toCharArray()) {
            resultado.append(c == '0' ? '1' : '0');
        }
        return resultado.toString();
    }

    /**
     * Desencriptación XOR (misma operación que encriptar)
     */
    private String desencriptarXOR(String contenido) {
        if (clave.isEmpty()) {
            return contenido;
        }

        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < contenido.length(); i++) {
            char bitMensaje = contenido.charAt(i);
            char bitClave = clave.charAt(i % clave.length());

            // XOR es su propia inversa
            if (bitMensaje == bitClave) {
                resultado.append('0');
            } else {
                resultado.append('1');
            }
        }
        return resultado.toString();
    }

    /**
     * Decodificación Base64
     */
    private String desencriptarBase64(String contenido) {
        byte[] decodedBytes = Base64.getDecoder().decode(contenido);
        return new String(decodedBytes);
    }

    // Getters
    public boolean estaProcesando() {
        return procesando;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public Encriptador.TipoEncriptacion getTipoEncriptacion() {
        return tipoEncriptacion;
    }

    public String getClave() {
        return clave;
    }

    /**
     * Establece un error y notifica a los listeners
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
}
