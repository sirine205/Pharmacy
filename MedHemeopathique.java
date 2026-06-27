package pharmacie;

public class MedHemeopathique extends Medicament implements Vendable{
	String nomPlante;
	String composition;
	
	public MedHemeopathique() {
		super();
		
	}
	public MedHemeopathique(String nom, String genre, double prix, long numSerie, String nomPlante, String composition) {
		super(nom, genre, prix, numSerie);
		this.nomPlante=nomPlante;
		this.composition=composition;
		
	}
	public MedHemeopathique(String nom, String genre, double prix, long numSerie, String nomPlante) {
		super(nom, genre, prix);
		this.nomPlante=nomPlante;
	}
	public MedHemeopathique(String nom, String genre,String nomPlante,String composition) {
		super(nom, genre);
		this.nomPlante=nomPlante;
		this.composition=composition;
	}
	
	@Override
	public String toString() {
		return "MedHemeopathique [nomPlante=" + nomPlante + ", composition=" + composition + ", toString()="
				+ super.toString() + ", getPrix()=" + getPrix() + ", getNom()=" + getNom() + ", getGenre()="
				+ getGenre() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
	}
	@Override
	public double getTranche() {
		return this.prix * 0.9;
	}
	
	public String getNomPlante() {
		return nomPlante;
	}

	public void setNomPlante(String nomPlante) {
		this.nomPlante = nomPlante;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}
	
}
