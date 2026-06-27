package pharmacie;

public class AppareilMed implements Vendable{
	String nom;
	double prix;
	int quantite = 1;
	
	public AppareilMed(String nom, double prix) {
		super();
		this.nom = nom;
		this.prix = prix;
	}
	
	public AppareilMed(String nom, double prix, int quantite) {
		super();
		this.nom = nom;
		this.prix = prix;
		this.quantite = quantite;
	}

	@Override
	public double getTranche() {
		
		return prix/3;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public double getPrix() {
		return prix;
	}

	public void setPrix(double prix) {
		this.prix = prix;
	}
	
	public int getQuantite() {
		return quantite;
	}

	public void setQuantite(int quantite) {
		this.quantite = quantite;
	}

	@Override
	public String toString() {
		return "AppareilMed [nom=" + nom + ", prix=" + prix + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}
	
	
}
