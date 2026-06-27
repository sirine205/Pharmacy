package pharmacie;

public class TestEtagere {
	public static void main(String[] args) {
		Medicament m1=new MedChimique("Efferalgan", "Paracetamol", 12.5, "Paracetamol", 12);
		Medicament m2=new MedChimique("Clamoxyl", "Antibiotique", 10.5, "Amoxicilline", 12);
		Medicament m3=new MedChimique("Clamoxyl", "Antibiotique", 10.5, "Amoxicilline", 12);
		Etagere etagere=new Etagere(100);
		
		etagere.ajoutMedicamentTab(m1);
		etagere.ajoutMedicamentListe(m1);
		etagere.ajoutMedicamentTab(m2);
		etagere.ajoutMedicamentListe(m2);
		etagere.ajoutMedicamentTab(m3);
		etagere.ajoutMedicamentListe(m3);
		
		System.out.println("Nombre d'élements dans le tableau");
		System.out.println(etagere.nombreMedicamentsTab());
		System.out.println("Nombre d'élements dans la liste");
		System.out.println(etagere.nombreMedicamentsListe());
		
		etagere.enleverMedicamentListe("Efferalgan", "Paracetamol");
		etagere.enleverMedicamentTab("Clamoxyl", "Antibiotique");
		
				System.out.println("Nombre d'élements dans le tableau");
				System.out.println(etagere.nombreMedicamentsTab());
				System.out.println("Nombre d'élements dans la liste");
				System.out.println(etagere.nombreMedicamentsListe());

		
		System.out.println(etagere.toString());
		System.out.println(etagere.toString2());
		
		
	}

}
