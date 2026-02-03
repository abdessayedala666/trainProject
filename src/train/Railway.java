package train;

/**
 * Représentation d'un circuit constitué d'éléments de voie ferrée : gare ou
 * section de voie
 * 
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr>
 * @author Philippe Tanguy <philippe.tanguy@imt-atlantique.fr>
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Railway {
	private final Element[] elements;  // Tableau des éléments composant la ligne (gares et sections)
	private RailwayView view;  // Vue pour la visualisation graphique
	
	// === Variables pour la prévention de l'interblocage ===
	// Compteur de trains par segment et par direction
	// Un segment est défini par l'index de la gare de départ (vers la droite)
	// Clé: index du segment, Valeur: nombre de trains allant vers la droite
	private Map<Integer, Integer> trainsPerSegmentLR = new HashMap<>();
	// Clé: index du segment, Valeur: nombre de trains allant vers la gauche
	private Map<Integer, Integer> trainsPerSegmentRL = new HashMap<>();
	
	// Compteurs globaux pour l'affichage
	private int trainsOnSectionsLR = 0;
	private int trainsOnSectionsRL = 0;

	public Railway(Element[] elements) {
		if (elements == null)
			throw new NullPointerException();

		this.elements = elements;
		for (Element e : elements)
			e.setRailway(this);
	}

	/**
	 * Définit la vue pour la visualisation
	 */
	public void setView(RailwayView view) {
		this.view = view;
	}

	/**
	 * Retourne le tableau des éléments
	 */
	public Element[] getElements() {
		return elements;
	}

	/**
	 * Retourne l'index d'un élément dans la ligne
	 * @param element l'élément à chercher
	 * @return l'index de l'élément ou -1 s'il n'est pas trouvé
	 */
	private int getIndex(Element element) {
		return Arrays.asList(elements).indexOf(element);
	}

	/**
	 * Retourne l'élément suivant dans la direction donnée
	 * @param current l'élément actuel
	 * @param direction la direction du déplacement
	 * @return l'élément suivant ou null si on est au bout
	 */
	private Element getNextElement(Element current, Direction direction) {
		int index = getIndex(current);
		if (direction == Direction.LR) {
			if (index + 1 < elements.length) {
				return elements[index + 1];
			}
		} else {
			if (index - 1 >= 0) {
				return elements[index - 1];
			}
		}
		return null;
	}

	/**
	 * Détermine si la direction doit changer (aux extrémités de la ligne)
	 * @param nextElement l'élément suivant
	 * @param currentDirection la direction actuelle
	 * @return la nouvelle direction
	 */
	private Direction getNewDirection(Element nextElement, Direction currentDirection) {
		int index = getIndex(nextElement);
		if (index == elements.length - 1 && currentDirection == Direction.LR) {
			return Direction.RL;
		} else if (index == 0 && currentDirection == Direction.RL) {
			return Direction.LR;
		}
		return currentDirection;
	}

	/**
	 * Vérifie si un train peut entrer dans l'élément suivant
	 * C'est la condition d'attente pour la synchronisation
	 * @param nextElement l'élément où le train veut aller
	 * @return true si le train peut entrer
	 */
	private boolean canEnter(Element nextElement) {
		return nextElement.canAccept();
	}

	/**
	 * Retourne la gare de destination selon la direction
	 * Pour les gares intermédiaires, on cherche la prochaine gare dans la direction
	 * @param currentElement l'élément actuel (peut être une gare intermédiaire)
	 * @param direction la direction du train
	 * @return la gare de destination (terminale ou intermédiaire)
	 */
	private Station getDestinationStation(Element currentElement, Direction direction) {
		int startIndex = getIndex(currentElement);
		
		if (direction == Direction.LR) {
			// Chercher la prochaine gare vers la droite
			for (int i = startIndex + 1; i < elements.length; i++) {
				if (elements[i] instanceof Station) {
					return (Station) elements[i];
				}
			}
			// Si pas trouvée, retourner la dernière (ne devrait pas arriver)
			return (Station) elements[elements.length - 1];
		} else {
			// Chercher la prochaine gare vers la gauche
			for (int i = startIndex - 1; i >= 0; i--) {
				if (elements[i] instanceof Station) {
					return (Station) elements[i];
				}
			}
			// Si pas trouvée, retourner la première (ne devrait pas arriver)
			return (Station) elements[0];
		}
	}

	/**
	 * Retourne l'index du segment entre deux gares
	 * Un segment est identifié par l'index de la gare la plus à gauche
	 * @param station la gare de départ
	 * @param direction la direction du train
	 * @return l'index du segment
	 */
	private int getSegmentIndex(Element station, Direction direction) {
		int stationIndex = getIndex(station);
		if (direction == Direction.LR) {
			// Le segment est identifié par la gare de gauche
			return stationIndex;
		} else {
			// Trouver la gare précédente (à gauche) pour identifier le segment
			for (int i = stationIndex - 1; i >= 0; i--) {
				if (elements[i] instanceof Station) {
					return i;
				}
			}
			return 0;
		}
	}

	/**
	 * Vérifie s'il y a des trains en sens inverse sur le segment spécifié
	 * @param segmentIndex l'index du segment
	 * @param direction la direction du train qui veut partir
	 * @return true s'il n'y a pas de trains en sens inverse
	 */
	private boolean noOppositeTrainsOnSegment(int segmentIndex, Direction direction) {
		if (direction == Direction.LR) {
			// Vérifier s'il y a des trains allant vers la gauche sur ce segment
			return trainsPerSegmentRL.getOrDefault(segmentIndex, 0) == 0;
		} else {
			// Vérifier s'il y a des trains allant vers la droite sur ce segment
			return trainsPerSegmentLR.getOrDefault(segmentIndex, 0) == 0;
		}
	}

	/**
	 * Vérifie si un train peut quitter une gare pour entrer sur les sections
	 * Invariant de sûreté: 
	 * - un train ne peut pas entrer sur un segment si des trains circulent dans le sens opposé SUR CE SEGMENT
	 * - un train ne peut pas partir si la gare de destination est pleine
	 * @param currentElement la gare où se trouve le train
	 * @param direction la direction dans laquelle le train veut aller
	 * @return true si le train peut quitter la gare
	 */
	private boolean canLeaveStation(Element currentElement, Direction direction) {
		// Identifier le segment que le train veut emprunter
		int segmentIndex = getSegmentIndex(currentElement, direction);
		
		// Vérifier qu'aucun train ne circule en sens inverse SUR CE SEGMENT
		boolean noOppositeTrains = noOppositeTrainsOnSegment(segmentIndex, direction);
		
		// Vérifier que la gare de destination a des quais disponibles
		Station destination = getDestinationStation(currentElement, direction);
		boolean destinationHasSpace = destination.canAccept();
		
		return noOppositeTrains && destinationHasSpace;
	}

	/**
	 * Vérifie pourquoi un train ne peut pas quitter la gare (pour le message)
	 * @param currentElement la gare actuelle
	 * @param direction la direction du train
	 * @return le message expliquant pourquoi le train attend
	 */
	private String getWaitReason(Element currentElement, Direction direction) {
		int segmentIndex = getSegmentIndex(currentElement, direction);
		
		// Vérifier les trains en sens inverse sur ce segment
		if (!noOppositeTrainsOnSegment(segmentIndex, direction)) {
			return "trains en sens inverse sur le segment " + segmentIndex;
		}
		
		// Vérifier la gare de destination
		Station destination = getDestinationStation(currentElement, direction);
		if (!destination.canAccept()) {
			return "gare de destination " + destination + " pleine";
		}
		
		return "raison inconnue";
	}

	/**
	 * Enregistre l'entrée d'un train sur un segment
	 * @param station la gare de départ
	 * @param direction la direction du train (LR ou RL)
	 */
	private void enterSegment(Element station, Direction direction) {
		int segmentIndex = getSegmentIndex(station, direction);
		
		if (direction == Direction.LR) {
			trainsPerSegmentLR.put(segmentIndex, trainsPerSegmentLR.getOrDefault(segmentIndex, 0) + 1);
			trainsOnSectionsLR++;
		} else {
			trainsPerSegmentRL.put(segmentIndex, trainsPerSegmentRL.getOrDefault(segmentIndex, 0) + 1);
			trainsOnSectionsRL++;
		}
	}

	/**
	 * Enregistre la sortie d'un train d'un segment
	 * @param station la gare d'arrivée
	 * @param direction la direction du train (LR ou RL)
	 */
	private void leaveSegment(Element station, Direction direction) {
		int segmentIndex;
		if (direction == Direction.LR) {
			// On arrive à une gare, le segment est celui avant cette gare
			for (int i = getIndex(station) - 1; i >= 0; i--) {
				if (elements[i] instanceof Station) {
					segmentIndex = i;
					trainsPerSegmentLR.put(segmentIndex, Math.max(0, trainsPerSegmentLR.getOrDefault(segmentIndex, 0) - 1));
					break;
				}
			}
			trainsOnSectionsLR--;
		} else {
			// On arrive à une gare venant de la droite
			segmentIndex = getIndex(station);
			trainsPerSegmentRL.put(segmentIndex, Math.max(0, trainsPerSegmentRL.getOrDefault(segmentIndex, 0) - 1));
			trainsOnSectionsRL--;
		}
	}

	/**
	 * Méthode synchronisée pour déplacer un train vers l'élément suivant
	 * 
	 * Cette méthode implémente les règles de circulation et la prévention de l'interblocage :
	 * - Un train attend si l'élément suivant est occupé
	 * - Un train attend si des trains circulent en sens inverse SUR LE MÊME SEGMENT
	 * - Un train réserve une place à la gare de destination avant de partir
	 * - Les gares intermédiaires permettent de changer de segment (croisement)
	 * 
	 * @param train le train à déplacer
	 */
	public synchronized void move(Train train) {
		Position currentPos = train.getPosition();
		Element currentElement = currentPos.getPos();
		Direction currentDirection = currentPos.getDirection();

		// Calculer l'élément suivant
		Element nextElement = getNextElement(currentElement, currentDirection);
		
		if (nextElement == null) {
			// Ne devrait pas arriver car on change de direction aux extrémités
			return;
		}

		// Déterminer la nouvelle direction (peut changer aux extrémités)
		Direction newDirection = getNewDirection(nextElement, currentDirection);

		// CAS 1: Le train est dans une gare et veut entrer dans une section
		if (currentElement instanceof Station && nextElement instanceof Section) {
			// Attendre que:
			// 1. La section soit libre
			// 2. Aucun train ne circule dans le sens opposé (prévention deadlock) - sauf pour gare intermédiaire
			// 3. La gare de destination a des quais disponibles (non réservés)
			while (!canEnter(nextElement) || !canLeaveStation(currentElement, currentDirection)) {
				try {
					if (!canLeaveStation(currentElement, currentDirection)) {
						System.out.println(train + " attend en gare (" + getWaitReason(currentElement, currentDirection) + ")");
					} else {
						System.out.println(train + " attend pour entrer dans " + nextElement);
					}
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			
			// RÉSERVER une place à la gare de destination AVANT de partir
			Station destination = getDestinationStation(currentElement, currentDirection);
			destination.reserveSpot();
			System.out.println(train + " réserve une place à " + destination + 
					" (disponibles: " + destination.getAvailableSpots() + "/" + destination.getSize() + ")");
			
			// Quitter la gare
			currentElement.leave();
			// Entrer dans la section
			nextElement.enter();
			// Enregistrer le train sur le segment
			enterSegment(currentElement, currentDirection);
		}
		// CAS 2: Le train est dans une section et va vers une autre section
		else if (currentElement instanceof Section && nextElement instanceof Section) {
			// Attendre que la section suivante soit libre
			while (!canEnter(nextElement)) {
				try {
					System.out.println(train + " attend pour entrer dans " + nextElement);
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			
			// Quitter la section actuelle
			currentElement.leave();
			// Entrer dans la nouvelle section
			nextElement.enter();
			// Note: le train reste comptabilisé dans la même direction
		}
		// CAS 3: Le train est dans une section et entre dans une gare
		else if (currentElement instanceof Section && nextElement instanceof Station) {
			// Le train a déjà réservé sa place, pas besoin d'attendre
			// La réservation garantit qu'il y a une place pour lui
			Station arrivalStation = (Station) nextElement;
			
			// Quitter la section
			currentElement.leave();
			// Décompter le train du segment
			leaveSegment(nextElement, currentDirection);
			// Consommer la réservation (la transformer en occupation réelle)
			arrivalStation.consumeReservation();
			// Entrer dans la gare
			nextElement.enter();
			
			System.out.println(train + " arrive à " + arrivalStation + 
					" (occupés: " + arrivalStation.getTrainCount() + "/" + arrivalStation.getSize() + ")");
		}
		// CAS 4: Gare à gare (ne devrait pas arriver dans cette configuration)
		else {
			while (!canEnter(nextElement)) {	
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			currentElement.leave();
			nextElement.enter();
		}
		
		// Mettre à jour la position du train
		Position newPos = new Position(nextElement, newDirection);
		train.setPosition(newPos);

		// Mettre à jour la vue si elle existe
		if (view != null) {
			view.updateTrainPosition(train, currentElement, nextElement);
		}

		// Notifier tous les threads en attente qu'un changement a eu lieu
		notifyAll();
	}

	/**
	 * Retourne le nombre de trains sur les sections allant vers la droite
	 */
	public int getTrainsOnSectionsLR() {
		return trainsOnSectionsLR;
	}

	/**
	 * Retourne le nombre de trains sur les sections allant vers la gauche
	 */
	public int getTrainsOnSectionsRL() {
		return trainsOnSectionsRL;
	}

	/**
	 * Place un train sur un élément initial (doit être une gare)
	 * @param train le train à placer
	 * @param station la gare où placer le train
	 * @throws BadPositionForTrainException si la gare ne peut pas accueillir le train
	 */
	public synchronized void placeTrainAtStation(Train train, Station station) throws BadPositionForTrainException {
		if (!station.canAccept()) {
			throw new BadPositionForTrainException(train.getName() + " - la gare " + station + " est pleine");
		}
		station.enter();
		
		// Mettre à jour la vue si elle existe
		if (view != null) {
			view.placeTrainInitially(train, station);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Element e : this.elements) {
			if (first)
				first = false;
			else
				result.append("--");
			result.append(e);
		}
		return result.toString();
	}
}
