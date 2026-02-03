package train;

/**
 * Représentation d'une section de voie ferrée. C'est une sous-classe de la
 * classe {@link Element}.
 * Une section ne peut contenir qu'un seul train à la fois.
 *
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr>
 * @author Philippe Tanguy <philippe.tanguy@imt-atlantique.fr>
 */
public class Section extends Element {
	private int trainCount = 0;  // Nombre de trains sur cette section (0 ou 1 maximum)
	
	public Section(String name) {
		super(name);
	}

	/**
	 * Vérifie si un train peut entrer dans cette section
	 * Une section ne peut contenir qu'un seul train à la fois
	 * @return true si la section est vide, false sinon
	 */
	@Override
	public boolean canAccept() {
		return trainCount == 0;
	}

	/**
	 * Enregistre l'entrée d'un train dans cette section
	 * Incrémente le compteur à 1
	 */
	@Override
	public void enter() {
		trainCount = 1;
	}

	/**
	 * Enregistre la sortie d'un train de cette section
	 * Remet le compteur à 0
	 */
	@Override
	public void leave() {
		trainCount = 0;
	}

	@Override
	public int getTrainCount() {
		return trainCount;
	}
}
