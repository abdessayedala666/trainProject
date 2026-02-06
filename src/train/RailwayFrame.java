package train;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale pour la visualisation de la ligne de chemin de fer
 */
public class RailwayFrame extends JFrame {
    private final RailwayView railwayView;
    
    public RailwayFrame(Railway railway, Element[] elements) {
        super("TRAINS");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Titre simple
        JLabel titleLabel = new JLabel("SIMULATION TRAINS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Vue de la ligne
        railwayView = new RailwayView(railway, elements);
        add(railwayView, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public RailwayView getRailwayView() {
        return railwayView;
    }
    
    /**
     * Ajoute un message au journal (désactivé)
     */
    public void log(String message) {
        // Log désactivé
    }
}
