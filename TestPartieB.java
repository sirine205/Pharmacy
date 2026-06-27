package pharmacie;

import java.util.ArrayList;
import java.util.List;

public class TestPartieB {
	public static void main(String[] args) {
		MedChimique mc1 = new MedChimique("C1", "G1", 30.0, 111L, "X", 8);
		MedHemeopathique mh1 = new MedHemeopathique("H1", "G2", 20.0, 222L, "Plante");
		AppareilMed a1 = new AppareilMed("A1", 50.0);
		MedChimique mc2 = new MedChimique("C2", "G3", 10.0, 333L, "Y", 12);

		List<Vendable> liste = new ArrayList<>();
		liste.add(mc1);
		liste.add(mh1);
		liste.add(a1);
		liste.add(mc2);

		Pharmacie p = new Pharmacie();

		List<Vendable> tries = p.trierVendablesParPrix(liste);
		System.out.println("Tri par prix:");
		for (Vendable v : tries) {
			System.out.println(v.getNom() + " -> " + v.getPrix());
		}

		System.out.println("Noms:");
		System.out.println(p.nomsVendables(liste));

		double totalRemise10 = p.totalApresRemise(liste, 0.10);
		System.out.println("Total avec remise 10%: " + totalRemise10);

		System.out.println("Résumé types: " + p.resumeTypes(liste));
	}
}
