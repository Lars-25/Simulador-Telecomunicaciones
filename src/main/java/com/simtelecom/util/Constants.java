package com.simtelecom.util;

public final class Constants {

    // Configuración de UI
    public static final int MAIN_WINDOW_WIDTH = 1200;
    public static final int MAIN_WINDOW_HEIGHT = 800;
    public static final int PANEL_MIN_WIDTH = 300;
    public static final int PANEL_MIN_HEIGHT = 200;

    // Configuración de animación
    public static final int DEFAULT_ANIMATION_DELAY = 500; // ms
    public static final int MIN_ANIMATION_DELAY = 100;
    public static final int MAX_ANIMATION_DELAY = 2000;

    // Configuración de simulación
    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_ENCRYPTION = "NONE";

    // Colores para visualización
    public static final String COLOR_BIT_0 = "#FF6B6B";
    public static final String COLOR_BIT_1 = "#4ECDC4";
    public static final String COLOR_PROCESSING = "#45B7D1";
    public static final String COLOR_ERROR = "#FF4757";
    public static final String COLOR_SUCCESS = "#26de81";

    // Rutas de recursos
    public static final String ICONS_PATH = "/icons/";
    public static final String CONFIG_PATH = "/config/";

    // Archivos de configuración
    public static final String SIMULATION_CONFIG = "simulation.properties";

    // Constructor privado para evitar instanciación
    private Constants() {
        throw new AssertionError("Esta clase no debe ser instanciada");
    }
}