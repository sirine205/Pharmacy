package pharmacie;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class TestPharmacie {
	public static void main(String[] args) {
		// Initialiser la base de données avant les tests
		ConnexionBD.initializeDatabase();
		
		Medicament m1=new MedChimique("Efferalgan", "Paracetamol", 25.5,123456L, "Paracetamol", 12);
		Medicament m2=new MedChimique("Efferalgan", "Paracetamol", 25.5,123456L, "Paracetamol", 12);
		Medicament m3=new MedChimique("Clamoxyl", "Antibiotique", 30.5,456789L, "Amoxicilline", 12);
		Medicament m4=new MedChimique("Clamoxyl", "Antibiotique", 30.5,456789L, "Amoxicilline", 12);
		Medicament m5=new MedChimique("Clamoxyl", "Antibiotique", 30.5,456789L, "Amoxicilline", 12);
		
		Pharmacie pharmacie=new Pharmacie();
		
		pharmacie.ajouterMedicament(m1);
		pharmacie.ajouterMedicament(m2);
		pharmacie.ajouterMedicament(m3);
		pharmacie.ajouterMedicament(m4);
		pharmacie.ajouterMedicament(m5);
		
		ClientFidele clientFidele0= new ClientFidele(1234785L, "Foudhaili", "Mohamed Aziz");
		ClientFidele clientFidele1= new ClientFidele(1234850L, "Moussa", "Nour");
		ClientFidele clientFidele2= new ClientFidele(1247856L, "Nasri", "Safa");
		
		pharmacie.ajouterClient(clientFidele0);
		pharmacie.ajouterClient(clientFidele1);
		pharmacie.ajouterClient(clientFidele2);
		
		List<Medicament> l = pharmacie.getAllMedicaments();
		
		int nb = (int) l.stream()
			    .filter(m -> m.getNom().startsWith("C") && m.getPrix() > 20.0)
			    .count();
		
		System.out.println("le nombre de médicament est : "+nb);
		
		
		List <ClientFidele> c = pharmacie.getAllClients();
		
		
		List <ClientFidele> client = c.stream().
									sorted(Comparator.comparing(ClientFidele::getNom)).
									collect(Collectors.toList());
		
		System.out.println("liste de clients triée par ordre alphabétique du nom \n"+ client);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
