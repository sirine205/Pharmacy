package pharmacie;

public class MedChimique extends Medicament implements Vendable{
	String constituant;
	int ageMin;
	
	public MedChimique() {
		super();
		
	}
	
	public MedChimique(String nom, String genre, double prix, long numSerie, String composant, int age) {
		super(nom, genre, prix, numSerie);
		this.constituant=composant;
		this.ageMin=age;
	}
	public MedChimique(String nom, String genre, double prix, String composant, int age) {
		super(nom, genre, prix);
		this.constituant=composant;
		this.ageMin=age;
		
	}
	public MedChimique(String nom, String genre,String composant,int age) {
		super(nom, genre);
		this.constituant=composant;
		this.ageMin=age;
	}

	@Override
	public String toString() {
		return "MedChimique [constituant=" + constituant + ", ageMin=" + ageMin + ", toString()=" + super.toString()
				+ ", getPrix()=" + getPrix() + ", getNom()=" + getNom() + ", getGenre()=" + getGenre() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + "]";
	}

	@Override
	public double getTranche() {
		return this.prix * 0.8;
	}

	public String getConstituant() {
		return constituant;
	}

	public void setConstituant(String constituant) {
		this.constituant = constituant;
	}

	public int getAgeMin() {
		return ageMin;
	}

	public void setAgeMin(int ageMin) {
		this.ageMin = ageMin;
	}
	

	
	

	
	
	
}
