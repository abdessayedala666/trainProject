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
    private static final int ELEMENT_WIDTH = 120;
    private static final int ELEMENT_HEIGHT = 80;
    private static final int TRAIN_SIZE = 30;
    private static final int PADDING = 20;
    
    private final Railway railway;
    private final Element[] elements;
    private final Map<String, Color> trainColors;
    private final Map<Element, java.util.List<Train>> trainPositions;
    
    // Couleurs pour les trains
    private static final Color[] COLORS = {
        new Color(231, 76, 60),   // Rouge
        new Color(46, 204, 113),  // Vert
        new Color(52, 152, 219),  // Bleu
        new Color(155, 89, 182),  // Violet
        new Color(241, 196, 15),  // Jaune
        new Color(230, 126, 34)   // Orange
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
        setBackground(Color.WHITE);
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = PADDING;
        int y = 50;
        
        // Dessiner chaque élément
        for (int i = 0; i < elements.length; i++) {
            Element element = elements[i];
            
            // Dessiner les connexions entre éléments
            if (i < elements.length - 1) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(x + ELEMENT_WIDTH, y + ELEMENT_HEIGHT / 2, 
                            x + ELEMENT_WIDTH + PADDING, y + ELEMENT_HEIGHT / 2);
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
            // Gare - rectangle avec bordures arrondies
            Station station = (Station) element;
            
            // Couleur différente pour les gares intermédiaires
            if (element instanceof IntermediateStation) {
                g2d.setColor(new Color(39, 174, 96));  // Vert pour gare intermédiaire
            } else {
                g2d.setColor(new Color(52, 73, 94));   // Bleu foncé pour gare terminale
            }
            
            g2d.fillRoundRect(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT, 15, 15);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            
            String name = element.toString();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (ELEMENT_WIDTH - fm.stringWidth(name)) / 2;
            g2d.drawString(name, textX, y + 25);
            
            // Afficher le nombre de quais et trains
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            String info = "Quais: " + station.getSize();
            textX = x + (ELEMENT_WIDTH - fm.stringWidth(info)) / 2;
            g2d.drawString(info, textX + 10, y + 45);
            
            int trainCount = trainPositions.get(element).size();
            String trainInfo = "Trains: " + trainCount + "/" + station.getSize();
            g2d.drawString(trainInfo, textX + 5, y + 60);
            
            // Indiquer si c'est une gare intermédiaire
            if (element instanceof IntermediateStation) {
                g2d.setFont(new Font("Arial", Font.ITALIC, 9));
                g2d.drawString("(croisement)", textX + 3, y + 73);
            }
            
        } else {
            // Section - rectangle simple
            int trainCount = trainPositions.get(element).size();
            
            // Couleur selon l'occupation
            if (trainCount > 0) {
                g2d.setColor(new Color(231, 76, 60)); // Rouge si occupé
            } else {
                g2d.setColor(new Color(149, 165, 166)); // Gris si libre
            }
            
            g2d.fillRect(x, y + 20, ELEMENT_WIDTH, ELEMENT_HEIGHT - 40);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            
            String name = element.toString();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (ELEMENT_WIDTH - fm.stringWidth(name)) / 2;
            g2d.drawString(name, textX, y + ELEMENT_HEIGHT / 2 + 5);
        }
    }
    
    private synchronized void drawTrainsOnElement(Graphics2D g2d, Element element, int x, int y) {
        java.util.List<Train> trains = trainPositions.get(element);
        if (trains == null || trains.isEmpty()) return;
        
        int trainY = y + ELEMENT_HEIGHT + 10;
        int trainX = x + (ELEMENT_WIDTH - (trains.size() * (TRAIN_SIZE + 5))) / 2;
        
        for (Train train : trains) {
            Color color = trainColors.getOrDefault(train.getName(), Color.GRAY);
            
            // Dessiner le train (cercle)
            g2d.setColor(color);
            g2d.fillOval(trainX, trainY, TRAIN_SIZE, TRAIN_SIZE);
            
            // Bordure
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(trainX, trainY, TRAIN_SIZE, TRAIN_SIZE);
            
            // Nom du train
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            String name = train.getName();
            int textX = trainX + (TRAIN_SIZE - fm.stringWidth(name)) / 2;
            int textY = trainY + TRAIN_SIZE / 2 + 4;
            g2d.drawString(name, textX, textY);
            
            // Flèche de direction
            Direction dir = train.getPosition().getDirection();
            g2d.setColor(Color.BLACK);
            int arrowY = trainY + TRAIN_SIZE + 5;
            int arrowX = trainX + TRAIN_SIZE / 2;
            if (dir == Direction.LR) {
                g2d.drawString("→", arrowX - 5, arrowY + 10);
            } else {
                g2d.drawString("←", arrowX - 5, arrowY + 10);
            }
            
            trainX += TRAIN_SIZE + 10;
        }
    }
    
    private void drawLegend(Graphics2D g2d) {
        int legendY = getHeight() - 30;
        int legendX = 10;
        
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Légende:", legendX, legendY);
        
        legendX += 70;
        
        synchronized (this) {
            for (Map.Entry<String, Color> entry : trainColors.entrySet()) {
                g2d.setColor(entry.getValue());
                g2d.fillOval(legendX, legendY - 12, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Train " + entry.getKey(), legendX + 20, legendY);
                legendX += 80;
            }
        }
        
        // Afficher le statut des trains sur les sections
        legendX += 30;
        int trainsLR = railway.getTrainsOnSectionsLR();
        int trainsRL = railway.getTrainsOnSectionsRL();
        
        g2d.setColor(Color.BLACK);
        g2d.drawString("|", legendX, legendY);
        legendX += 15;
        
        // Indicateur direction
        if (trainsLR > 0) {
            g2d.setColor(new Color(46, 204, 113)); // Vert
            g2d.drawString("→ " + trainsLR + " train(s) sur ligne", legendX, legendY);
        } else if (trainsRL > 0) {
            g2d.setColor(new Color(52, 152, 219)); // Bleu
            g2d.drawString("← " + trainsRL + " train(s) sur ligne", legendX, legendY);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.drawString("Ligne libre", legendX, legendY);
        }
    }
}
