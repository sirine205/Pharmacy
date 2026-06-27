package pharmacie;

import java.time.LocalDate;
import java.util.List;

public class TestComplet {
	public static void main(String[] args) {
		System.out.println("===========================================================");
		System.out.println("   TEST COMPLET DES FONCTIONNALITÉS DU PROJET PHARMACIE");
		System.out.println("===========================================================\n");

		// 0. Initialisation de la base de données
		ConnexionBD.initializeDatabase();
		Pharmacie p = new Pharmacie();
		
		// --- 1. AJOUTER VENDABLES ET CLIENTS ---
		System.out.println("----------1) TEST AJOUT CLIENTS ET VENDABLES DANS LA BASE-----------\n");
		
		// Ajout d'un client fidèle
		ClientFidele c1 = new ClientFidele(111111L, "Testeur", "Ali");
		p.ajouterClient(c1);
		System.out.println("Client ajouté\n");

		// Ajout de vendables variés
		// a) Médicament Chimique (Normal)
		MedChimique m1 = new MedChimique("Doliprane", "Chimique", 10.0, 1001L, "Paracetamol", 12);
		m1.setDateExpiration(LocalDate.now().plusMonths(6)); // Expire dans 6 mois (pas de solde)
		p.ajouterMedicament(m1);
		
		// b) Médicament Homéopathique (Va expirer bientôt pour le test 4)
		MedHemeopathique m2 = new MedHemeopathique("Arnica", "Homeo", 20.0, 1002L, "Arnica Montana", "Granules");
		m2.setDateExpiration(LocalDate.now().plusDays(15)); // Expire dans 15 jours (< 1 mois -> Solde attendu)
		p.ajouterMedicament(m2);
		
		// c) Appareil Médical
		AppareilMed a1 = new AppareilMed("Tensiometre", 120.0, 5);
		p.ajouterAppareil(a1);
		AppareilMed a2 = new AppareilMed("Thermometre", 50.0, 10);
		p.ajouterAppareil(a2);
		AppareilMed a3 = new AppareilMed("Oxymetre", 80.0, 8);
		p.ajouterAppareil(a3);
		
		System.out.println("\nVendables ajoutés: Doliprane, Arnica, Tensiometre, Thermometre, Oxymetre\n");

		
		// --- 2. RECHERCHE AVEC STREAMS ---
		System.out.println("\n------------2) TEST RECHERCHE-------------\n");
		
		System.out.println("- Recherche par nom 'Doliprane':");
		List<Medicament> resNom = p.rechercherParNom("Doliprane");
		if (resNom.isEmpty()) System.out.println("  Aucun résultat.\n");
		resNom.forEach(m -> System.out.println("  Trouvé: " + m.getNom() + ", Prix: " + m.getPrix()));
		
		System.out.println("\n- Recherche par catégorie 'Homeo':");
		List<Medicament> resCat = p.rechercherParCategorie("Homeo");
		if (resCat.isEmpty()) System.out.println("  Aucun résultat.\n");
		resCat.forEach(m -> System.out.println("  Trouvé: " + m.getNom()));
		
		System.out.println("\n- Recherche par premières lettres 'A':");
		List<Medicament> resDeb = p.rechercherParPremieresLettres("A");
		if (resDeb.isEmpty()) System.out.println("  Aucun résultat.\n");
		resDeb.forEach(m -> System.out.println("  Trouvé: " + m.getNom()));
		System.out.println();

		
		// --- 3. ACHAT AVEC FACILITÉS ---
		System.out.println("\n----------3) TEST ACHAT AVEC FACILITÉS DE PAIEMENT-----------\n");
		
		// Achat d'un appareil (Tensiometre 120.0) avec facilités
		// Tranche appareil = prix / 3 = 40.0
		System.out.println("Achat Tensiometre (Prix: 120.0) par Ali avec facilités (Tranche = Prix/3)...");
		
		// Note: acheterVendable utilise le nom de l'objet passé pour chercher en base
		double montantPaye = p.acheterVendable(a1, 111111L, true);
		
		System.out.println("Montant payé : " + montantPaye);
		
		// Vérification stock
		List<AppareilMed> apps = p.getAllAppareils();
		for(AppareilMed a : apps) {
			if(a.getNom().equals("Tensiometre")) {
				System.out.println("Stock Tensiometre restant : " + a.getQuantite());
			}
		}
		System.out.println();

		
		// --- 4. EXPIRATION ET REMISE ---
		System.out.println("\n-----------4) la liste des médicaments qui expirent dans un mois et une remise de 30% sur le prix initial-----------\n");
		p.afficherEtSoldesMedicamentsExpirantBientot();
		System.out.println();

		
		// --- SUPPRESSION (NETTOYAGE) ---
		System.out.println("----- TEST SUPPRESSION ---------");
		p.supprimerMedicament("Doliprane");
		System.out.println("- Doliprane supprimé");
		p.supprimerMedicament("Arnica");
		System.out.println("- Arnica supprimé");
		p.supprimerAppareil("Tensiometre");
		System.out.println("- Tensiometre supprimé");
		p.supprimerAppareil("Thermometre");
		System.out.println("- Thermometre supprimé");
		p.supprimerAppareil("Oxymetre");
		System.out.println("- Oxymetre supprimé");
		p.supprimerClient(111111L);
		System.out.println("- Client Ali supprimé");
		
		System.out.println("\n=== FIN DU TEST  ===");
	}
}