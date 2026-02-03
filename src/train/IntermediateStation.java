package train;

/**
 * Représentation d'une gare intermédiaire. C'est une sous-classe de {@link Station}.
 * Une gare intermédiaire permet à des trains de se croiser au milieu de la ligne.
 * 
 * INVARIANT DE SÛRETÉ pour éviter l'interblocage :
 * Si une gare intermédiaire a n places, le nombre total de trains dans le système
 * ne doit pas dépasser n + 1 trains.
 * 
 * Explication : Avec n places et n+2 trains ou plus, tous les trains peuvent se bloquer :
 * - n trains occupent la gare intermédiaire
 * - 1 train attend d'y entrer depuis chaque côté
 * - Personne ne peut avancer → interblocage
 * 
 * Solution : Limiter à n+1 trains maximum garantit qu'au moins un train peut toujours
 * entrer dans la gare et libérer de l'espace.
 * 
 * @author Votre nom
 */
public class IntermediateStation extends Station {
	
	/**
	 * Constructeur d'une gare intermédiaire
	 * @param name le nom de la gare
	 * @param size le nombre de quais (places) disponibles
	 */
	public IntermediateStation(String name, int size) {
		super(name, size);
	}
	
	/**
	 * Indique si c'est une gare terminale (aux extrémités de la ligne)
	 * @return false car c'est une gare intermédiaire
	 */
	public boolean isTerminal() {
		return false;
	}
}
