package com.simtelecom;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("FILE.txt");
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            // Convertir el contenido completo a bytes UTF-8
            byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);

            int byteCount = 0;
            StringBuilder byteString = new StringBuilder();
            StringBuilder binaryString = new StringBuilder();
            StringBuilder charString = new StringBuilder();

            for (byte b : bytes) {
                int unsignedByte = b & 0xFF;
                byteCount++;

                byteString.append("[");
                byteString.append(String.format("%02x", unsignedByte));
                byteString.append("]");

                String binaryByte = String.format("%8s", Integer.toBinaryString(unsignedByte)).replace(' ', '0');
                binaryString.append("[");
                binaryString.append(binaryByte);
                binaryString.append("]");

                if (unsignedByte >= 32 && unsignedByte <= 126) {
                    charString.append((char) unsignedByte);
                } else {
                    charString.append(".");
                }

                if (byteCount % 8 == 0) {
                    System.out.printf("%08x  %s%n", byteCount - 8, byteString);
                    System.out.printf("%08x  %s%n", byteCount - 8, binaryString);
                    System.out.printf("%08x  %s%n%n", byteCount - 8, charString);

                    byteString.setLength(0);
                    binaryString.setLength(0);
                    charString.setLength(0);
                }
            }

            if (byteCount % 8 != 0) {
                System.out.printf("%08x  %s%n", byteCount - (byteCount % 8), byteString);
                System.out.printf("%08x  %s%n", byteCount - (byteCount % 8), binaryString);
                System.out.printf("%08x  %s%n", byteCount - (byteCount % 8), charString);
            }

            // Mostrar el contenido completo decodificado como UTF-8
            System.out.println("\nTexto completo decodificado:");
            System.out.println(content.toString());

        } catch (IOException e) {
            System.out.println("Error al leer el archivo");
            e.printStackTrace();
        }
    }
}