package com.simtelecom.model;

import java.util.Base64;

/**
 * Responsable de encriptar mensajes usando diferentes algoritmos
 */
public class Encriptador extends ObservableModel {

    /**
     * Tipos de encriptación disponibles
     */
    public enum TipoEncriptacion {
        NONE("Sin encriptación"),
        CAESAR("Cifrado César"),
        XOR("Cifrado XOR"),
        BASE64("Codificación Base64");

        private final String descripcion;

        TipoEncriptacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private boolean procesando;
    private String ultimoError;
    private TipoEncriptacion tipoEncriptacion;
    private String clave;

    /**
     * Constructor del encriptador
     */
    public Encriptador() {
        this.procesando = false;
        this.ultimoError = null;
        this.tipoEncriptacion = TipoEncriptacion.NONE;
        this.clave = "";
    }

    /**
     * Establece el tipo de encriptación y la clave
     * @param tipo Tipo de encriptación
     * @param clave Clave para la encriptación (puede ser null para algunos tipos)
     */
    public void configurar(TipoEncriptacion tipo, String clave) {
        this.tipoEncriptacion = tipo != null ? tipo : TipoEncriptacion.NONE;
        this.clave = clave != null ? clave : "";
    }

    /**
     * Encripta un mensaje según la configuración actual
     * @param mensaje Mensaje a encriptar
     * @return true si la encriptación fue exitosa
     */
    public boolean encriptar(Mensaje mensaje) {
        if (mensaje == null) {
            setError("Mensaje no puede ser nulo");
            return false;
        }

        if (mensaje.getContenidoCodificado() == null || mensaje.getContenidoCodificado().isEmpty()) {
            setError("No hay contenido codificado para encriptar");
            return false;
        }

        if (procesando) {
            setError("Encriptador ya está procesando otro mensaje");
            return false;
        }

        try {
            procesando = true;
            ultimoError = null;

            notifyListeners(EventTypes.PROCESSING_STARTED, mensaje);

            // Simular tiempo de procesamiento
            Thread.sleep(150);

            String contenidoEncriptado;
            String contenidoCodificado = mensaje.getContenidoCodificado();

            switch (tipoEncriptacion) {
                case NONE:
                    contenidoEncriptado = contenidoCodificado;
                    break;
                case CAESAR:
                    contenidoEncriptado = encriptarCesar(contenidoCodificado);
                    break;
                case XOR:
                    contenidoEncriptado = encriptarXOR(contenidoCodificado);
                    break;
                case BASE64:
                    contenidoEncriptado = encriptarBase64(contenidoCodificado);
                    break;
                default:
                    throw new IllegalStateException("Tipo de encriptación no soportado: " + tipoEncriptacion);
            }

            mensaje.setContenidoEncriptado(contenidoEncriptado);
            mensaje.setEstado(Mensaje.EstadoMensaje.ENCRIPTADO);

            notifyListeners(EventTypes.MESSAGE_ENCRYPTED, mensaje);
            notifyListeners(EventTypes.PROCESSING_COMPLETED, mensaje);

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setError("Encriptación interrumpida");
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } catch (Exception e) {
            setError("Error durante la encriptación: " + e.getMessage());
            mensaje.setEstado(Mensaje.EstadoMensaje.ERROR);
            return false;

        } finally {
            procesando = false;
        }
    }

    /**
     * Encriptación César simple para cadenas binarias
     */
    private String encriptarCesar(String contenido) {
        if (clave.isEmpty()) {
            return contenido; // Sin desplazamiento
        }

        int desplazamiento;
        try {
            desplazamiento = Integer.parseInt(clave) % 2; // Solo 0 o 1 para binarios
        } catch (NumberFormatException e) {
            desplazamiento = 1; // Valor por defecto
        }

        if (desplazamiento == 0) {
            return contenido;
        }

        // Invertir bits (0->1, 1->0)
        StringBuilder resultado = new StringBuilder();
        for (char c : contenido.toCharArray()) {
            resultado.append(c == '0' ? '1' : '0');
        }
        return resultado.toString();
    }

    /**
     * Encriptación XOR con clave
     */
    private String encriptarXOR(String contenido) {
        if (clave.isEmpty()) {
            return contenido;
        }

        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < contenido.length(); i++) {
            char bitMensaje = contenido.charAt(i);
            char bitClave = clave.charAt(i % clave.length());

            // XOR entre caracteres binarios
            if (bitMensaje == bitClave) {
                resultado.append('0');
            } else {
                resultado.append('1');
            }
        }
        return resultado.toString();
    }

    /**
     * Codificación Base64 del contenido binario
     */
    private String encriptarBase64(String contenido) {
        return Base64.getEncoder().encodeToString(contenido.getBytes());
    }

    // Getters
    public boolean estaProcesando() {
        return procesando;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public TipoEncriptacion getTipoEncriptacion() {
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
