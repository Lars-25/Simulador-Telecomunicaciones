package com.simtelecom;

import com.simtelecom.controller.SimulationController;
import com.simtelecom.view.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación de simulación de telecomunicaciones
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                SimulationController controller = new SimulationController();
                MainFrame mainFrame = new MainFrame(controller);
                mainFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error al iniciar la aplicación: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}