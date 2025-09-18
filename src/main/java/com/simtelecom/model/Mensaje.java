package com.simtelecom.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa un mensaje en el sistema de telecomunicaciones
 */
public class Mensaje {

    private final String contenidoOriginal;
    private String contenidoBinario;
    private String contenidoCodificado;
    private String contenidoEncriptado;
    private final LocalDateTime fechaCreacion;
    private EstadoMensaje estado;

    /**
     * Estados posibles de un mensaje durante su procesamiento
     */
    public enum EstadoMensaje {
        CREADO("Mensaje creado"),
        CODIFICADO("Mensaje codificado a binario"),
        ENCRIPTADO("Mensaje encriptado"),
        TRANSMITIENDO("Mensaje en transmisión"),
        RECIBIDO("Mensaje recibido"),
        DESENCRIPTADO("Mensaje desencriptado"),
        DECODIFICADO("Mensaje decodificado"),
        COMPLETADO("Proceso completado"),
        ERROR("Error en el procesamiento");

        private final String descripcion;

        EstadoMensaje(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    /**
     * Constructor para crear un nuevo mensaje
     * @param contenido Contenido original del mensaje
     */
    public Mensaje(String contenido) {
        if (contenido == null) {
            throw new IllegalArgumentException("El contenido del mensaje no puede ser nulo");
        }

        this.contenidoOriginal = contenido;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoMensaje.CREADO;
    }

    // Getters
    public String getContenidoOriginal() {
        return contenidoOriginal;
    }

    public String getContenidoBinario() {
        return contenidoBinario;
    }

    public String getContenidoCodificado() {
        return contenidoCodificado;
    }

    public String getContenidoEncriptado() {
        return contenidoEncriptado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoMensaje getEstado() {
        return estado;
    }

    // Setters con validación
    public void setContenidoBinario(String contenidoBinario) {
        this.contenidoBinario = contenidoBinario;
    }

    public void setContenidoCodificado(String contenidoCodificado) {
        this.contenidoCodificado = contenidoCodificado;
    }

    public void setContenidoEncriptado(String contenidoEncriptado) {
        this.contenidoEncriptado = contenidoEncriptado;
    }

    public void setEstado(EstadoMensaje estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        this.estado = estado;
    }

    /**
     * Obtiene el tamaño del mensaje original en bytes
     * @return Tamaño en bytes
     */
    public int getTamanioBytes() {
        return contenidoOriginal.getBytes().length;
    }

    /**
     * Obtiene el tamaño del mensaje binario en bits
     * @return Tamaño en bits
     */
    public int getTamanioBits() {
        return contenidoBinario != null ? contenidoBinario.length() : 0;
    }

    /**
     * Verifica si el mensaje está en un estado de error
     * @return true si hay error
     */
    public boolean tieneError() {
        return estado == EstadoMensaje.ERROR;
    }

    /**
     * Verifica si el procesamiento del mensaje está completo
     * @return true si está completado
     */
    public boolean estaCompleto() {
        return estado == EstadoMensaje.COMPLETADO;
    }

    /**
     * Crea una copia del mensaje para transmisión
     * @return Copia del mensaje
     */
    public Mensaje crearCopia() {
        Mensaje copia = new Mensaje(this.contenidoOriginal);
        copia.contenidoBinario = this.contenidoBinario;
        copia.contenidoCodificado = this.contenidoCodificado;
        copia.contenidoEncriptado = this.contenidoEncriptado;
        copia.estado = this.estado;
        return copia;
    }

    @Override
    public String toString() {
        return String.format("Mensaje{estado=%s, tamaño=%d bytes, fecha=%s}",
                estado, getTamanioBytes(), fechaCreacion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mensaje mensaje = (Mensaje) o;
        return Objects.equals(contenidoOriginal, mensaje.contenidoOriginal) &&
                Objects.equals(fechaCreacion, mensaje.fechaCreacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contenidoOriginal, fechaCreacion);
    }
}
