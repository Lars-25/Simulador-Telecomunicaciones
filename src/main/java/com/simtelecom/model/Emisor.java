package com.simtelecom.model;

import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente emisor que coordina la codificación y encriptación de mensajes
 */
public class Emisor extends ObservableModel {

    private final Codificador codificador;
    private final Encriptador encriptador;
    private Canal canal;
    private boolean procesando;
    private String ultimoError;
    private String identificador;
    private List<Mensaje> mensajesEnviados;
    private Mensaje mensajeActual;

    public Emisor(String identificador) {
        this.identificador = identificador != null ? identificador : "EMISOR-001";
        this.codificador = new Codificador();
        this.encriptador = new Encriptador();
        this.procesando = false;
        this.ultimoError = null;
        this.mensajesEnviados = new ArrayList<>();

        configurarListeners();
    }

    private void configurarListeners() {
        codificador.addListener((eventType, data) -> {
            notifyListeners("EMISOR_" + eventType, data);
        });

        encriptador.addListener((eventType, data) -> {
            notifyListeners("EMISOR_" + eventType, data);
        });
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public void configurarEncriptacion(Encriptador.TipoEncriptacion tipo, String clave) {
        encriptador.configurar(tipo, clave);
    }

    public CompletableFuture<Boolean> enviarMensaje(String contenido) {
        return CompletableFuture.supplyAsync(() -> {
            if (procesando) {
                setError("Emisor ya está procesando otro mensaje");
                return false;
            }

            if (contenido == null || contenido.isEmpty()) {
                setError("El contenido del mensaje no puede estar vacío");
                return false;
            }

            try {
                procesando = true;
                ultimoError = null;

                Mensaje mensaje = new Mensaje(contenido);
                mensajeActual = mensaje;

                notifyListeners(EventTypes.MESSAGE_CREATED, mensaje);

                // Paso 1: Codificar
                if (!codificador.codificar(mensaje)) {
                    setError("Error en codificación: " + codificador.getUltimoError());
                    return false;
                }

                // Paso 2: Encriptar
                if (!encriptador.encriptar(mensaje)) {
                    setError("Error en encriptación: " + encriptador.getUltimoError());
                    return false;
                }

                // Paso 3: Transmitir a través del canal
                if (canal == null) {
                    setError("No hay canal configurado para transmitir");
                    return false;
                }

                boolean exitoTransmision = canal.transmitir(mensaje);
                if (!exitoTransmision) {
                    setError("Error en transmisión: " + canal.getUltimoError());
                    return false;
                }

                mensajesEnviados.add(mensaje);
                notifyListeners("EMISOR_MESSAGE_SENT", mensaje);
                return true;

            } catch (Exception e) {
                setError("Error inesperado: " + e.getMessage());
                return false;
            } finally {
                procesando = false;
                mensajeActual = null;
            }
        });
    }

    public void enviarMensajePasos(String contenido, Receptor.StepCallback onStep, Receptor.CompleteCallback onComplete) {
        new Thread(() -> {
            try {
                boolean exito = true;
                Mensaje mensaje = new Mensaje(contenido);
                mensajeActual = mensaje;

                notifyListeners(EventTypes.MESSAGE_CREATED, mensaje);

                // Paso 1: Codificación
                if (onStep != null) onStep.onStep("ENCODING", mensaje);
                if (!codificador.codificar(mensaje)) {
                    exito = false;
                    setError("Error en codificación");
                }

                if (exito) {
                    // Paso 2: Encriptación
                    if (onStep != null) onStep.onStep("ENCRYPTING", mensaje);
                    if (!encriptador.encriptar(mensaje)) {
                        exito = false;
                        setError("Error en encriptación");
                    }
                }

                if (exito && canal != null) {
                    // Paso 3: Transmisión
                    if (onStep != null) onStep.onStep("TRANSMITTING", mensaje);
                    if (!canal.transmitir(mensaje)) {
                        exito = false;
                        setError("Error en transmisión");
                    }
                }

                if (exito) {
                    mensajesEnviados.add(mensaje);
                }

                if (onComplete != null) {
                    onComplete.onComplete(exito, mensaje);
                }

            } catch (Exception e) {
                setError("Error inesperado: " + e.getMessage());
                if (onComplete != null) {
                    onComplete.onComplete(false, null);
                }
            } finally {
                mensajeActual = null;
            }
        }).start();
    }

    // Getters y métodos auxiliares
    public boolean estaProcesando() { return procesando; }
    public String getUltimoError() { return ultimoError; }
    public List<Mensaje> getMensajesEnviados() { return new ArrayList<>(mensajesEnviados); }
    public int getContadorMensajes() { return mensajesEnviados.size(); }

    private void setError(String error) {
        this.ultimoError = error;
        notifyListeners(EventTypes.ERROR_OCCURRED, error);
    }
}