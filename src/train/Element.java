package train;

/**
 * Cette classe abstraite est la représentation générique d'un élément de base
 * d'un
 * circuit, elle factorise les fonctionnalités communes des deux sous-classes :
 * l'entrée d'un train, sa sortie et l'appartenance au circuit.<br/>
 * Les deux sous-classes sont :
 * <ol>
 * <li>La représentation d'une gare : classe {@link Station}</li>
 * <li>La représentation d'une section de voie ferrée : classe
 * {@link Section}</li>
 * </ol>
 * 
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr>
 * @author Philippe Tanguy <philippe.tanguy@imt-atlantique.fr>
 */
public abstract class Element {
	private final String name;
	protected Railway railway;

	protected Element(String name) {
		if (name == null)
			throw new NullPointerException();

		this.name = name;
	}

	public void setRailway(Railway r) {
		if (r == null)
			throw new NullPointerException();

		this.railway = r;
	}	

	public Railway getRailway() {
		return this.railway;
	}

	/**
	 * Vérifie si un train peut entrer dans cet élément
	 * @return true si l'élément peut accueillir un train supplémentaire
	 */
	public abstract boolean canAccept();

	/**
	 * Enregistre l'entrée d'un train dans cet élément
	 */
	public abstract void enter();

	/**
	 * Enregistre la sortie d'un train de cet élément
	 */
	public abstract void leave();

	/**
	 * Retourne le nombre actuel de trains dans cet élément
	 * @return le nombre de trains présents
	 */
	public abstract int getTrainCount();

	@Override
	public String toString() {
		return this.name;
	}
}
