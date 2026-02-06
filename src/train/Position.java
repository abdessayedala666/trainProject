package train;

/**
 * Représentation de la position d'un train dans le circuit. Une position
 * est caractérisée par deux valeurs :

 * L'élément où se positionne le train : une gare (classe {@link Station})
 * ou une section de voie ferrée (classe {@link Section}).

 * La direction qu'il prend (enumération {@link Direction}) : de gauche à
 * droite ou de droite à gauche.

 * 
 * @author Fabien Dagnat <fabien.dagnat@imt-atlantique.fr> Modifié par Mayte
 *         Segarra
 * @author Philippe Tanguy <philippe.tanguy@imt-atlantique.fr>
 * 
 * @version 0.3
 */
public class Position implements Cloneable {
	private final Direction direction;  // Direction du train (LR ou RL)
	private final Element pos;  // Élément où se trouve le train (gare ou section)

	/**
	 * Constructeur d'une position
	 * @param elt l'élément où se trouve le train
	 * @param d la direction du train
	 */
	public Position(Element elt, Direction d) {
		if (elt == null || d == null)
			throw new NullPointerException();

		this.pos = elt;
		this.direction = d;
	}

	@Override
	public Position clone() {
		try {
			return (Position) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Element getPos() {
		return pos;
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(this.pos.toString());
		result.append(" going ");
		result.append(this.direction);
		return result.toString();
	}
}
