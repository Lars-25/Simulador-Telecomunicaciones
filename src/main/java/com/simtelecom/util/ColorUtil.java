package com.simtelecom.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidades para manejo de colores en la interfaz
 */
public final class ColorUtil {

    private static final Map<String, Color> COLOR_CACHE = new HashMap<>();

    static {
        // Inicializar colores predefinidos
        COLOR_CACHE.put(Constants.COLOR_BIT_0, hexToColor(Constants.COLOR_BIT_0));
        COLOR_CACHE.put(Constants.COLOR_BIT_1, hexToColor(Constants.COLOR_BIT_1));
        COLOR_CACHE.put(Constants.COLOR_PROCESSING, hexToColor(Constants.COLOR_PROCESSING));
        COLOR_CACHE.put(Constants.COLOR_ERROR, hexToColor(Constants.COLOR_ERROR));
        COLOR_CACHE.put(Constants.COLOR_SUCCESS, hexToColor(Constants.COLOR_SUCCESS));
    }

    private ColorUtil() {
        throw new AssertionError("Esta clase no debe ser instanciada");
    }

    /**
     * Convierte un string hexadecimal a Color
     * @param hex String hexadecimal (con o sin #)
     * @return Color correspondiente
     */
    public static Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return Color.BLACK;
        }

        // Remover # si existe
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            return Color.decode("#" + hex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    /**
     * Obtiene un color desde el caché o lo crea
     * @param colorKey Clave del color (puede ser hex o nombre)
     * @return Color correspondiente
     */
    public static Color getColor(String colorKey) {
        if (colorKey == null) {
            return Color.BLACK;
        }

        return COLOR_CACHE.computeIfAbsent(colorKey, ColorUtil::hexToColor);
    }

    /**
     * Obtiene el color para un bit específico
     * @param bit Carácter del bit ('0' o '1')
     * @return Color correspondiente
     */
    public static Color getColorForBit(char bit) {
        return bit == '0' ? getColor(Constants.COLOR_BIT_0) : getColor(Constants.COLOR_BIT_1);
    }

    /**
     * Obtiene el color para el estado de procesamiento
     * @return Color de procesamiento
     */
    public static Color getProcessingColor() {
        return getColor(Constants.COLOR_PROCESSING);
    }

    /**
     * Obtiene el color para errores
     * @return Color de error
     */
    public static Color getErrorColor() {
        return getColor(Constants.COLOR_ERROR);
    }

    /**
     * Obtiene el color para éxito
     * @return Color de éxito
     */
    public static Color getSuccessColor() {
        return getColor(Constants.COLOR_SUCCESS);
    }

    /**
     * Crea una versión más clara del color
     * @param color Color base
     * @param factor Factor de claridad (0.0-1.0)
     * @return Color más claro
     */
    public static Color lighten(Color color, float factor) {
        if (color == null) return Color.WHITE;

        factor = Math.max(0.0f, Math.min(1.0f, factor));

        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));

        return new Color(r, g, b, color.getAlpha());
    }

    /**
     * Crea una versión más oscura del color
     * @param color Color base
     * @param factor Factor de oscuridad (0.0-1.0)
     * @return Color más oscuro
     */
    public static Color darken(Color color, float factor) {
        if (color == null) return Color.BLACK;

        factor = Math.max(0.0f, Math.min(1.0f, factor));

        int r = (int)(color.getRed() * (1 - factor));
        int g = (int)(color.getGreen() * (1 - factor));
        int b = (int)(color.getBlue() * (1 - factor));

        return new Color(r, g, b, color.getAlpha());
    }

    /**
     * Crea un color con transparencia
     * @param color Color base
     * @param alpha Valor alpha (0-255)
     * @return Color con transparencia
     */
    public static Color withAlpha(Color color, int alpha) {
        if (color == null) return new Color(0, 0, 0, alpha);

        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Convierte un color a su representación hexadecimal
     * @param color Color a convertir
     * @return String hexadecimal
     */
    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        return String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Obtiene un color interpolado entre dos colores
     * @param color1 Color inicial
     * @param color2 Color final
     * @param ratio Ratio de interpolación (0.0-1.0)
     * @return Color interpolado
     */
    public static Color interpolate(Color color1, Color color2, float ratio) {
        if (color1 == null) return color2;
        if (color2 == null) return color1;

        ratio = Math.max(0.0f, Math.min(1.0f, ratio));

        int r = (int)(color1.getRed() + (color2.getRed() - color1.getRed()) * ratio);
        int g = (int)(color1.getGreen() + (color2.getGreen() - color1.getGreen()) * ratio);
        int b = (int)(color1.getBlue() + (color2.getBlue() - color1.getBlue()) * ratio);
        int a = (int)(color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * ratio);

        return new Color(r, g, b, a);
    }

    /**
     * Genera un color aleatorio
     * @return Color aleatorio
     */
    public static Color randomColor() {
        return new Color(
                (int)(Math.random() * 256),
                (int)(Math.random() * 256),
                (int)(Math.random() * 256)
        );
    }

    /**
     * Verifica si un color es oscuro
     * @param color Color a verificar
     * @return true si es oscuro
     */
    public static boolean isDark(Color color) {
        if (color == null) return true;

        // Calcular luminancia usando la fórmula estándar
        double luminance = (0.299 * color.getRed() +
                0.587 * color.getGreen() +
                0.114 * color.getBlue()) / 255.0;

        return luminance < 0.5;
    }

    /**
     * Obtiene el color de contraste apropiado (blanco o negro)
     * @param backgroundColor Color de fondo
     * @return Color de contraste
     */
    public static Color getContrastColor(Color backgroundColor) {
        return isDark(backgroundColor) ? Color.WHITE : Color.BLACK;
    }
}