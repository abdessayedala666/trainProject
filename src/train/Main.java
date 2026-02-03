package train;

import javax.swing.*;

/**
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr>
 */
public class Main {
	public static void main(String[] args) {
		// Création des gares terminales
		Station A = new Station("GareA", 3);
		Station D = new Station("GareD", 3);
		
		// Création d'une gare intermédiaire au milieu
		// INVARIANT DE SÛRETÉ : avec n places, maximum n+1 trains pour éviter l'interblocage
		// Avec 2 places, on peut avoir au maximum 3 trains
		IntermediateStation C = new IntermediateStation("GareC", 2);
		
		// Création des sections
		Section AB = new Section("AB");
		Section BC = new Section("BC");
		Section CD = new Section("CD");
		Section DE = new Section("DE");
		
		// Tableau des éléments pour la ligne : GareA -- AB -- BC -- GareC -- CD -- DE -- GareD
		Element[] elements = { A, AB, BC, C, CD, DE, D };
		
		// Création de la ligne
		Railway railway = new Railway(elements);
		
		// Création de l'interface graphique sur le thread Swing
		SwingUtilities.invokeLater(() -> {
			// Créer la fenêtre de visualisation
			RailwayFrame frame = new RailwayFrame(railway, elements);
			
			// Associer la vue à la ligne
			railway.setView(frame.getRailwayView());
			
			frame.log("Ligne de chemin de fer: " + railway);
			frame.log("Configuration: 2 gares terminales (3 places) + 1 gare intermédiaire (2 places)");
			frame.log("Invariant respecté: GareC a 2 places, donc max 3 trains (2+1)");
			frame.log("");
			
			try {
				// Création de 3 trains (respecte l'invariant : n+1 avec n=2 pour la gare intermédiaire)
				
				// Train 1 : part de GareA vers la droite
				Position p1 = new Position(A, Direction.LR);
				Train t1 = new Train("T1", p1, railway);
				frame.log("Train T1 créé à GareA, direction → droite");
				
				// Train 2 : part de GareA vers la droite
				Position p2 = new Position(A, Direction.LR);
				Train t2 = new Train("T2", p2, railway);
				frame.log("Train T2 créé à GareA, direction → droite");
				
				// Train 3 : part de GareD vers la gauche
				Position p3 = new Position(D, Direction.RL);
				Train t3 = new Train("T3", p3, railway);
				frame.log("Train T3 créé à GareD, direction ← gauche");
				
				frame.log("");
				frame.log("Démarrage de la simulation avec 3 trains...");
				frame.log("Les trains peuvent se croiser à la GareC (gare intermédiaire)");
				frame.log("(Les trains se déplacent toutes les secondes)");
				frame.log("");

				// Création et démarrage des threads pour chaque train
				Thread thread1 = new Thread(t1);
				Thread thread2 = new Thread(t2);
				Thread thread3 = new Thread(t3);

				thread1.start();
				thread2.start();
				thread3.start();

			} catch (BadPositionForTrainException e) {
				frame.log("ERREUR: " + e.getMessage());
			}
		});
	}
}
