package pharmacie;

public class ClientFidele {
	public long cin;
	String nom;
	String prenom;
	double credit = 10;
	
	
	public ClientFidele(long cIN, String nom, String prenom) {
		super();
		this.cin= cIN;
		this.nom = nom;
		this.prenom = prenom;
	}
	
	

	@Override
	public String toString() {
		return "nom=" + nom + ", prenom=" + prenom;
	}



	public double getCredit() {
		return credit;
	}



	public void setCredit(double credit) {
		this.credit = credit;
	}



	public String getNom() {
		return nom;
	}



	public void setNom(String nom) {
		this.nom = nom;
	}
	
	
	
	
	
	
}
