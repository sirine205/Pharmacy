package pharmacie;

import java.time.LocalDate;

public abstract class Medicament {
	 long code;
	 String nom;
	 String genre;
	 double prix;
	 static long nombre = 0;
	 long numSerie=0;
	 LocalDate dateExpiration;
	
	public Medicament(String nom, String genre) {
		super();
		nombre++;
		this.code = nombre;
		this.nom = nom;
		this.genre = genre;
		this.prix = 0;
		this.numSerie = 0;
	}
	
	public Medicament(String nom, String genre, double prix) {
		super();
		nombre++;
		this.code = nombre;
		this.nom = nom;
		this.genre = genre;
		this.prix = prix ;
	}
	public Medicament(String nom, String genre, double prix, long numSerie) {
		super();
		nombre++;
		this.code = nombre;
		this.nom = nom;
		this.genre = genre;
		this.prix = prix ;
		this.numSerie= numSerie;
	}

	public Medicament() {
		nombre++;
		this.code = nombre;
		this.nom = "";
		this.genre = "";
		this.prix = 0;
		this.numSerie = 0;
	}
	
	@Override
	public String toString() {
		return "Medicament [code=" + code + ", nom=" + nom + ", genre=" + genre + ", prix=" + prix + "]";
	}
	
	public int compare_nom(Medicament m1) {
		return this.nom.compareTo(m1.nom);
	}
	
	static public int compare_nom(Medicament m1,Medicament m2) {
		return m1.nom.compareTo(m2.nom);
	}
	
	
	static public int compare_prix(Medicament m1,Medicament m2) {
		return (Double.compare(m1.prix, m2.prix));
	}
	
	public int compare_prix(Medicament m1) {
		return (Double.compare(prix,m1.prix));
	}

	public double getPrix() {
		return prix;
	}

	public void setPrix(double prix) {
		this.prix = prix;
	}

	public String getNom() {
		return nom;
	}

	public String getGenre() {
		return genre;
	}

	public LocalDate getDateExpiration() {
		return dateExpiration;
	}

	public void setDateExpiration(LocalDate dateExpiration) {
		this.dateExpiration = dateExpiration;
	}
	
	
	
	
}
