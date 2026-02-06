package train;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Visualisation graphique de la ligne de chemin de fer
 * Affiche les éléments (gares et sections) et les trains qui y circulent
 */
public class RailwayView extends JPanel {
    private static final int ELEMENT_WIDTH = 110;
    private static final int ELEMENT_HEIGHT = 70;
    private static final int TRAIN_SIZE = 35;
    private static final int PADDING = 15;
    
    private final Railway railway;
    private final Element[] elements;
    private final Map<String, Color> trainColors;
    private final Map<Element, java.util.List<Train>> trainPositions;
    
    // Couleurs modernes pour les trains
    private static final Color[] COLORS = {
        new Color(231, 76, 60),   // Rouge moderne
        new Color(52, 152, 219),  // Bleu moderne
        new Color(46, 204, 113),  // Vert moderne
        new Color(155, 89, 182),  // Violet moderne
        new Color(230, 126, 34),  // Orange moderne
        new Color(26, 188, 156)   // Turquoise moderne
    };
    
    private int colorIndex = 0;
    
    public RailwayView(Railway railway, Element[] elements) {
        this.railway = railway;
        this.elements = elements;
        this.trainColors = new HashMap<>();
        this.trainPositions = new HashMap<>();
        
        // Initialiser les positions vides pour chaque élément
        for (Element e : elements) {
            trainPositions.put(e, new java.util.ArrayList<>());
        }
        
        // Calculer la taille préférée
        int width = elements.length * (ELEMENT_WIDTH + PADDING) + PADDING;
        int height = ELEMENT_HEIGHT + 150;
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(240, 240, 240));  // Fond clair simple
    }
    
    /**
     * Enregistre un train avec une couleur unique
     */
    public synchronized void registerTrain(Train train) {
        if (!trainColors.containsKey(train.getName())) {
            trainColors.put(train.getName(), COLORS[colorIndex % COLORS.length]);
            colorIndex++;
        }
    }
    
    /**
     * Met à jour la position d'un train et redessine
     */
    public synchronized void updateTrainPosition(Train train, Element oldElement, Element newElement) {
        // Retirer le train de l'ancien élément
        if (oldElement != null && trainPositions.containsKey(oldElement)) {
            trainPositions.get(oldElement).remove(train);
        }
        
        // Ajouter le train au nouvel élément
        if (newElement != null && trainPositions.containsKey(newElement)) {
            if (!trainPositions.get(newElement).contains(train)) {
                trainPositions.get(newElement).add(train);
            }
        }
        
        // Redessiner
        repaint();
    }
    
    /**
     * Place initialement un train sur un élément
     */
    public synchronized void placeTrainInitially(Train train, Element element) {
        registerTrain(train);
        if (trainPositions.containsKey(element)) {
            trainPositions.get(element).add(train);
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Activer l'anti-aliasing pour un rendu lisse
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        int x = PADDING;
        int y = 30;
        
        // Dessiner chaque élément
        for (int i = 0; i < elements.length; i++) {
            Element element = elements[i];
            
            // Dessiner les connexions entre éléments
            if (i < elements.length - 1) {
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(x + ELEMENT_WIDTH, y + ELEMENT_HEIGHT / 2, 
                            x + ELEMENT_WIDTH + PADDING, y + ELEMENT_HEIGHT / 2);
                g2d.setStroke(new BasicStroke(1));
            }
            
            // Dessiner l'élément
            drawElement(g2d, element, x, y);
            
            // Dessiner les trains sur cet élément
            drawTrainsOnElement(g2d, element, x, y);
            
            x += ELEMENT_WIDTH + PADDING;
        }
        
        // Dessiner la légende
        drawLegend(g2d);
    }
    
    private void drawElement(Graphics2D g2d, Element element, int x, int y) {
        if (element instanceof Station) {
            // Gare - rectangle arrondi simple
            Station station = (Station) element;
            
            g2d.setColor(new Color(70, 70, 70));
            g2d.fillRoundRect(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT, 12, 12);
            g2d.setColor(new Color(50, 50, 50));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT, 12, 12);
            g2d.setStroke(new BasicStroke(1));
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            
            String name = element.toString();
            g2d.drawString(name, x + 8, y + 16);
            
            // Afficher le nombre de quais et trains
            int trainCount = trainPositions.get(element).size();
            g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2d.drawString("Quais: " + station.getSize(), x + 8, y + 32);
            g2d.drawString("Trains: " + trainCount + "/" + station.getSize(), x + 8, y + 48);
            
            // Indiquer si c'est une gare de croisement
            if (element instanceof IntermediateStation) {
                g2d.setColor(new Color(100, 200, 100));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2d.drawString("CROISEMENT", x + 8, y + 62);
            }
            
        } else {
            // Section - rectangle arrondi simple
            int trainCount = trainPositions.get(element).size();
            
            Color fillColor = trainCount > 0 ? new Color(220, 80, 80) : Color.WHITE;
            Color borderColor = trainCount > 0 ? new Color(180, 60, 60) : new Color(180, 180, 180);
            
            g2d.setColor(fillColor);
            g2d.fillRoundRect(x, y + 20, ELEMENT_WIDTH, ELEMENT_HEIGHT - 40, 10, 10);
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y + 20, ELEMENT_WIDTH, ELEMENT_HEIGHT - 40, 10, 10);
            g2d.setStroke(new BasicStroke(1));
            
            g2d.setColor(trainCount > 0 ? Color.WHITE : new Color(60, 60, 60));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            String name = element.toString();
            g2d.drawString(name, x + 8, y + ELEMENT_HEIGHT / 2 + 4);
        }
    }
    
    private synchronized void drawTrainsOnElement(Graphics2D g2d, Element element, int x, int y) {
        java.util.List<Train> trains = trainPositions.get(element);
        if (trains == null || trains.isEmpty()) return;
        
        int trainY = y + ELEMENT_HEIGHT + 10;
        int trainX = x;
        
        for (Train train : trains) {
            Color color = trainColors.getOrDefault(train.getName(), Color.GRAY);
            
            // Dessiner le train (rectangle arrondi simple)
            g2d.setColor(color);
            g2d.fillRoundRect(trainX, trainY, TRAIN_SIZE, TRAIN_SIZE, 8, 8);
            g2d.setColor(color.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(trainX, trainY, TRAIN_SIZE, TRAIN_SIZE, 8, 8);
            g2d.setStroke(new BasicStroke(1));
            
            // Nom du train
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            String name = train.getName();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = trainX + (TRAIN_SIZE - fm.stringWidth(name)) / 2;
            int textY = trainY + (TRAIN_SIZE + fm.getAscent()) / 2 - 2;
            g2d.drawString(name, textX, textY);
            
            // Direction avec flèche stylisée
            Direction dir = train.getPosition().getDirection();
            String dirStr = (dir == Direction.LR) ? "→" : "←";
            g2d.setColor(new Color(236, 240, 241));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString(dirStr, trainX + TRAIN_SIZE / 2 - 5, trainY + TRAIN_SIZE + 16);
            
            trainX += TRAIN_SIZE + 10;
        }
    }
    
    private void drawLegend(Graphics2D g2d) {
        int legendY = getHeight() - 20;
        int legendX = 10;
        
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("LÉGENDE:", legendX, legendY);
        
        legendX += 75;
        
        synchronized (this) {
            for (Map.Entry<String, Color> entry : trainColors.entrySet()) {
                g2d.setColor(entry.getValue());
                g2d.fillRoundRect(legendX, legendY - 12, 14, 14, 4, 4);
                g2d.setColor(new Color(60, 60, 60));
                g2d.drawString("T" + entry.getKey(), legendX + 18, legendY);
                legendX += 55;
            }
        }
        
        // Afficher le statut des trains sur les sections
        legendX += 15;
        int trainsLR = railway.getTrainsOnSectionsLR();
        int trainsRL = railway.getTrainsOnSectionsRL();
        
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawString("|", legendX, legendY);
        legendX += 15;
        
        g2d.setColor(new Color(80, 160, 80));
        if (trainsLR > 0) {
            g2d.drawString("→ " + trainsLR + " sur ligne", legendX, legendY);
        } else if (trainsRL > 0) {
            g2d.drawString("← " + trainsRL + " sur ligne", legendX, legendY);
        } else {
            g2d.setColor(new Color(120, 120, 120));
            g2d.drawString("Ligne libre", legendX, legendY);
        }
    }
}
