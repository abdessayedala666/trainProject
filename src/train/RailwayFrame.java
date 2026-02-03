package train;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale pour la visualisation de la ligne de chemin de fer
 */
public class RailwayFrame extends JFrame {
    private final RailwayView railwayView;
    private final JTextArea logArea;
    
    public RailwayFrame(Railway railway, Element[] elements) {
        super("Simulation de Ligne de Chemin de Fer");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panneau de titre
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(44, 62, 80));
        JLabel titleLabel = new JLabel(" Simulation de Trains ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Vue de la ligne
        railwayView = new RailwayView(railway, elements);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(railwayView, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        // Zone de log
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Journal des événements"));
        add(scrollPane, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public RailwayView getRailwayView() {
        return railwayView;
    }
    
    /**
     * Ajoute un message au journal
     */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll vers le bas
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
