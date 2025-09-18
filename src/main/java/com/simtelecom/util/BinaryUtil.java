package com.simtelecom.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades para conversiones binarias y manipulación de bits
 */
public final class BinaryUtil {

    private BinaryUtil() {
        throw new AssertionError("Esta clase no debe ser instanciada");
    }

    /**
     * Convierte un texto a representación binaria
     * @param text Texto a convertir
     * @return String con la representación binaria
     */
    public static String textToBinary(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder binary = new StringBuilder();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            binary.append(String.format("%8s", Integer.toBinaryString(b & 0xFF))
                    .replace(' ', '0'));
        }

        return binary.toString();
    }

    /**
     * Convierte una representación binaria a texto
     * @param binary String binario
     * @return Texto decodificado
     */
    public static String binaryToText(String binary) {
        if (binary == null || binary.length() % 8 != 0) {
            throw new IllegalArgumentException("Binary string must be multiple of 8 bits");
        }

        StringBuilder text = new StringBuilder();

        for (int i = 0; i < binary.length(); i += 8) {
            String byteString = binary.substring(i, i + 8);
            int byteValue = Integer.parseInt(byteString, 2);
            text.append((char) byteValue);
        }

        return text.toString();
    }

    /**
     * Convierte un byte a su representación hexadecimal
     * @param b Byte a convertir
     * @return String hexadecimal (2 caracteres)
     */
    public static String byteToHex(byte b) {
        return String.format("%02x", b & 0xFF);
    }

    /**
     * Convierte un texto a lista de bits individuales
     * @param text Texto a convertir
     * @return Lista de caracteres '0' y '1'
     */
    public static List<Character> textToBitList(String text) {
        String binary = textToBinary(text);
        List<Character> bits = new ArrayList<>();

        for (char c : binary.toCharArray()) {
            bits.add(c);
        }

        return bits;
    }

    /**
     * Formatea una cadena binaria para visualización
     * @param binary Cadena binaria
     * @return Cadena formateada con espacios cada 8 bits
     */
    public static String formatBinaryString(String binary) {
        if (binary == null || binary.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < binary.length(); i++) {
            if (i > 0 && i % 8 == 0) {
                formatted.append(" ");
            }
            formatted.append(binary.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Verifica si una cadena contiene solo caracteres binarios
     * @param str Cadena a verificar
     * @return true si solo contiene '0' y '1'
     */
    public static boolean isBinaryString(String str) {
        return str != null && str.matches("[01]*");
    }
}