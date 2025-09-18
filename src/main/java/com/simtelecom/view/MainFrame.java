package com.simtelecom.view;

import com.simtelecom.controller.SimulationController;
import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación
 */
public class MainFrame extends JFrame {
    private final SimulationController controller;

    public MainFrame(SimulationController controller) {
        this.controller = controller;
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setTitle("Simulador de Telecomunicaciones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Crear paneles (los implementaremos después)
        JPanel panelEmisor = new JPanel();
        JPanel panelReceptor = new JPanel();
        JPanel panelCanal = new JPanel();
        JPanel panelControl = new JPanel();
        JPanel panelProceso = new JPanel();

        panelEmisor.setBorder(BorderFactory.createTitledBorder("Emisor"));
        panelReceptor.setBorder(BorderFactory.createTitledBorder("Receptor"));
        panelCanal.setBorder(BorderFactory.createTitledBorder("Canal"));
        panelControl.setBorder(BorderFactory.createTitledBorder("Control"));
        panelProceso.setBorder(BorderFactory.createTitledBorder("Proceso"));

        // Agregar paneles al frame
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.add(panelEmisor);
        topPanel.add(panelReceptor);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(panelCanal, BorderLayout.CENTER);
        centerPanel.add(panelProceso, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    private void setupLayout() {
        // Configuración adicional de layout
    }

    private void setupListeners() {
        // Configurar listeners del controlador
    }
}