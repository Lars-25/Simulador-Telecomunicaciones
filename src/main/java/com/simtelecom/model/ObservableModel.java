package com.simtelecom.model;

import com.simtelecom.view.ModelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Clase base para modelos observables que notifican cambios a los listeners
 */
public abstract class ObservableModel {

    // Usar CopyOnWriteArrayList para thread-safety en las notificaciones
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Agrega un listener para ser notificado de cambios
     * @param listener Listener a agregar
     */
    public void addListener(ModelListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remueve un listener
     * @param listener Listener a remover
     */
    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica a todos los listeners sobre un cambio en el modelo
     * @param eventType Tipo de evento que ocurrió
     * @param data Datos adicionales del evento (puede ser null)
     */
    protected void notifyListeners(String eventType, Object data) {
        for (ModelListener listener : listeners) {
            try {
                listener.onModelChanged(eventType, data);
            } catch (Exception e) {
                // Log del error pero continúa notificando a otros listeners
                System.err.println("Error notificando listener: " + e.getMessage());
            }
        }
    }

    /**
     * Notifica sobre un cambio sin datos adicionales
     * @param eventType Tipo de evento
     */
    protected void notifyListeners(String eventType) {
        notifyListeners(eventType, null);
    }

    /**
     * Obtiene el número de listeners registrados
     * @return Número de listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Remueve todos los listeners
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Constantes para tipos de eventos comunes
     */
    public static class EventTypes {
        public static final String MODEL_UPDATED = "MODEL_UPDATED";
        public static final String PROCESSING_STARTED = "PROCESSING_STARTED";
        public static final String PROCESSING_COMPLETED = "PROCESSING_COMPLETED";
        public static final String ERROR_OCCURRED = "ERROR_OCCURRED";
        public static final String MESSAGE_CREATED = "MESSAGE_CREATED";
        public static final String MESSAGE_ENCODED = "MESSAGE_ENCODED";
        public static final String MESSAGE_ENCRYPTED = "MESSAGE_ENCRYPTED";
        public static final String MESSAGE_TRANSMITTED = "MESSAGE_TRANSMITTED";
        public static final String MESSAGE_RECEIVED = "MESSAGE_RECEIVED";
        public static final String MESSAGE_DECRYPTED = "MESSAGE_DECRYPTED";
        public static final String MESSAGE_DECODED = "MESSAGE_DECODED";

        private EventTypes() {
            throw new AssertionError("Esta clase no debe ser instanciada");
        }
    }
}