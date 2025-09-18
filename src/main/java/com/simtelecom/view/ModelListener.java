package com.simtelecom.view;

/**
 * Interface para objetos que escuchan cambios en el modelo
 */
@FunctionalInterface
public interface ModelListener {

    /**
     * Método llamado cuando ocurre un cambio en el modelo
     * @param eventType Tipo de evento que ocurrió
     * @param data Datos adicionales relacionados con el evento (puede ser null)
     */
    void onModelChanged(String eventType, Object data);

    /**
     * Método por defecto para manejar eventos sin datos adicionales
     * @param eventType Tipo de evento
     */
    default void onModelChanged(String eventType) {
        onModelChanged(eventType, null);
    }
}