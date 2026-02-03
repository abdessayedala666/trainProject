package train;

/**
 * Représentation d'un train. Un train est caractérisé par deux valeurs :
 * <ol>
 * <li>
 * Son nom pour l'affichage.
 * </li>
 * <li>
 * La position qu'il occupe dans le circuit (un élément avec une direction) :
 * classe {@link Position}.
 * </li>
 * </ol>
 * 
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr>
 * @author Mayte segarra <mt.segarra@imt-atlantique.fr>
 *         Test if the first element of a train is a station
 * @author Philippe Tanguy <philippe.tanguy@imt-atlantique.fr>
 * @version 0.3
 */

public class Train implements Runnable {
	private final String name;  // Nom du train pour l'identification
	private Position pos;  // Position actuelle du train (élément + direction)
	private final Railway railway;  // Référence à la ligne ferroviaire
	static final int DELAY_MS = 1000;  // Délai entre chaque mouvement (en millisecondes)

	public Train(String name, Position p, Railway railway) throws BadPositionForTrainException {
		// Vérification des paramètres non nuls
		if (name == null || p == null || railway == null)
			throw new NullPointerException();

		// Un train doit obligatoirement démarrer dans une gare
		if (!(p.getPos() instanceof Station))
			throw new BadPositionForTrainException(name);

		this.name = name;
		this.pos = p.clone();
		this.railway = railway;
		
		// Enregistrer le train dans la gare initiale et mettre à jour l'occupation
		railway.placeTrainAtStation(this, (Station) p.getPos());
	}

	public String getName() {
		return this.name;
	}

	public void setPosition(Position p) {
		this.pos = p;
	}

	public Position getPosition() {
		return this.pos;
	}

	/**
	 * Méthode exécutée par le thread du train
	 * Le train se déplace continuellement sur la ligne en respectant les règles de circulation
	 */
	@Override
	public void run() {
		System.out.println(this + " démarre");
		while (true) {
			try {
				// Attendre un peu avant de se déplacer (simulation du temps de trajet)
				Thread.sleep(DELAY_MS);
				
				// Se déplacer vers l'élément suivant (avec synchronisation pour éviter les collisions)
				railway.move(this);
				
				// Afficher la nouvelle position
				System.out.println(this);
				
			} catch (InterruptedException e) {
				// Le train a été interrompu, on arrête proprement
				System.out.println(this + " interrompu");
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("Train[");
		result.append(this.name);
		result.append("]");
		result.append(" is on ");
		result.append(this.pos);
		return result.toString();
	}
}
