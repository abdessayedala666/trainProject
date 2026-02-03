package train;

/**
 * Représentation d'une gare. C'est une sous-classe de la classe {@link Element}.
 * Une gare est caractérisée par un nom et un nombre de quais (donc de trains
 * qu'elle est susceptible d'accueillir à un instant donné).
 * 
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr>
 * @author Philippe Tanguy <philippe.tanguy@imt-atlantique.fr>
 */
public class Station extends Element {
	private final int size;  // Nombre de quais (capacité maximale)
	private int trainCount = 0;  // Nombre de trains actuellement présents en gare
	private int reservedSpots = 0;  // Nombre de places réservées par des trains en route vers cette gare

	public Station(String name, int size) {
		super(name);
		if(name == null || size <= 0)
			throw new NullPointerException();
		this.size = size;
	}

	/**
	 * Retourne le nombre de quais de la gare
	 * @return le nombre de quais
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Retourne le nombre de places disponibles (non occupées et non réservées)
	 * @return le nombre de places libres
	 */
	public int getAvailableSpots() {
		return size - trainCount - reservedSpots;
	}

	/**
	 * Réserve une place pour un train en route vers cette gare
	 */
	public void reserveSpot() {
		reservedSpots++;
	}

	/**
	 * Libère une réservation quand le train arrive (la réservation devient occupation)
	 */
	public void consumeReservation() {
		if (reservedSpots > 0) {
			reservedSpots--;
		}
	}

	@Override
	public boolean canAccept() {
		// Une gare peut accepter un train s'il y a des places non occupées ET non réservées
		return getAvailableSpots() > 0;
	}

	@Override
	public void enter() {
		trainCount++;
	}

	@Override
	public void leave() {
		if (trainCount > 0) {
			trainCount--;
		}
	}

	@Override
	public int getTrainCount() {
		return trainCount;
	}

	/**
	 * Retourne le nombre de réservations en cours
	 * @return le nombre de places réservées
	 */
	public int getReservedSpots() {
		return reservedSpots;
	}
	
	/**
	 * Indique si c'est une gare terminale (aux extrémités de la ligne)
	 * @return true car une Station standard est considérée comme terminale
	 */
	public boolean isTerminal() {
		return true;
	}
}
