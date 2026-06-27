package pharmacie;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Pharmacie {
	
	public Pharmacie() {
		// Constructeur vide, la connexion est gérée statiquement via ConnexionBD
	}
	
	// --- Gestion des Médicaments ---

	public List<Medicament> getAllMedicaments() {
		List<Medicament> list = new ArrayList<>();//pour stoker les resultats 
		Connection conn = ConnexionBD.getConnection();//connexion a la base de donné 
		if (conn == null) {
			System.err.println("ERREUR: Connexion null dans getAllMedicaments");
			return list;
		}

		// Requête avec jointures (LEFT JOIN) pour récupérer tous les types
		String sql = "SELECT m.code, m.num_serie, m.nom, m.genre, m.prix, m.date_expiration, m.quantite, " +
		             "c.constituant, c.age_min, h.nom_plante, h.composition " +
		             "FROM medicament m " +
		             "LEFT JOIN med_chimique c ON m.code = c.med_code " +
		             "LEFT JOIN med_homeopathique h ON m.code = h.med_code " +
		             "ORDER BY m.code";
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int count = 0;
			int skipped = 0;
			while (rs.next()) {
				count++;
				Medicament m = null;
				String nom = rs.getString("nom");
				if (nom == null) {
					System.err.println("ATTENTION: Ligne " + count + " a un nom NULL, ignorée");
					skipped++;
					continue;
				}
				
				String genre = rs.getString("genre");
				double prix = rs.getDouble("prix");
				long numSerie = rs.getLong("num_serie");
				long code = rs.getLong("code");
				
				String constituant = rs.getString("constituant");
				Integer ageMin = null;
				try {
					Object ageMinObj = rs.getObject("age_min");
					if (ageMinObj != null) {
						ageMin = rs.getInt("age_min");
					}
				} catch (Exception e) {
					// Ignorer si null
				}
				String nomPlante = rs.getString("nom_plante");
				String composition = rs.getString("composition");
				
				// Détermination du type selon les colonnes non nulles
				if (constituant != null && !constituant.trim().isEmpty() && ageMin != null) {
					m = new MedChimique(nom, genre, prix, numSerie, constituant, ageMin);
//					System.out.println("-Récupéré MedChimique: " + nom + " (code: " + code + ", num_serie: " + numSerie + ")");
				} else if (nomPlante != null && !nomPlante.trim().isEmpty()) {
					m = new MedHemeopathique(nom, genre, prix, numSerie, nomPlante, composition != null ? composition : "");
//					System.out.println("-Récupéré MedHemeopathique: " + nom + " (code: " + code + ", num_serie: " + numSerie + ")");
				} else {
					// Cas d'un médicament générique ou erreur de données
					System.err.println("⚠ ATTENTION: Médicament " + nom + " (code: " + code + ", num_serie: " + numSerie + ") n'a ni constituant ni nom_plante");
					System.err.println("  - constituant: '" + constituant + "'");
					System.err.println("  - age_min: " + ageMin);
					System.err.println("  - nom_plante: '" + nomPlante + "'");
					System.err.println("  - composition: '" + composition + "'");
					skipped++;
					continue; 
				}
				
				m.code = code;
				if (rs.getDate("date_expiration") != null) {
					m.setDateExpiration(rs.getDate("date_expiration").toLocalDate());
				}
				list.add(m);
			}
//			System.out.println("=== méthode getAllMedicaments: " + count + " lignes lues, " + list.size() + " médicaments ajoutés, " + skipped + " ignorés ===");
		} catch (SQLException e) { 
			System.err.println("ERREUR lors de la récupération des médicaments: " + e.getMessage());
			System.err.println("Code SQL: " + e.getErrorCode());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public void ajouterMedicament(Medicament m) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		
		try {
			conn.setAutoCommit(false); // Transaction

			// 1. Vérifier si existe déjà par numéro de série pour incrémenter quantité
			String checkSql = "SELECT code, quantite FROM medicament WHERE num_serie = ?";
			PreparedStatement checkPs = conn.prepareStatement(checkSql);
			checkPs.setLong(1, m.numSerie);
			ResultSet rsCheck = checkPs.executeQuery();
			
			if (rsCheck.next()) {
				// Mise à jour quantité
				int qte = rsCheck.getInt("quantite");
				String updateSql = "UPDATE medicament SET quantite = ? WHERE code = ?";
				PreparedStatement updatePs = conn.prepareStatement(updateSql);
				updatePs.setInt(1, qte + 1);
				updatePs.setLong(2, rsCheck.getLong("code"));
				updatePs.executeUpdate();
				updatePs.close();
			} else {
				// 2. Insertion nouvelle - Pour Oracle, on insère puis on récupère la valeur
				System.out.println("Ajout d'un nouveau médicament: " + m.getNom() + " (num_serie: " + m.numSerie + ")");
				
				String sqlMedInsert = "INSERT INTO medicament (num_serie, nom, genre, prix, date_expiration, quantite) VALUES (?, ?, ?, ?, ?, ?)";
				PreparedStatement psMed = conn.prepareStatement(sqlMedInsert);
				psMed.setLong(1, m.numSerie);
				psMed.setString(2, m.getNom());
				psMed.setString(3, m.getGenre());
				psMed.setDouble(4, m.getPrix());
				if (m.getDateExpiration() != null) {
					psMed.setDate(5, Date.valueOf(m.getDateExpiration()));
				} else {
					psMed.setNull(5, java.sql.Types.DATE);
				}
				psMed.setInt(6, 1);
				int rowsInserted = psMed.executeUpdate();
				System.out.println("Lignes insérées dans medicament: " + rowsInserted);
				psMed.close();
				
				// Récupérer le code généré - Dans Oracle avec IDENTITY, on doit interroger la table
				// Utiliser une requête simple avec le num_serie unique
				long medCode = -1;
				PreparedStatement getCodePs = null;
				ResultSet rsCode = null;
				
				try {
					// Méthode 1: SELECT direct avec num_serie (devrait fonctionner dans la même transaction)
					String getCodeSql = "SELECT code FROM medicament WHERE num_serie = ?";
					getCodePs = conn.prepareStatement(getCodeSql);
					getCodePs.setLong(1, m.numSerie);
					rsCode = getCodePs.executeQuery();
					
					if (rsCode.next()) {
						medCode = rsCode.getLong("code");
//						System.out.println("- le code récupéré pour médicament " + m.getNom() + ": " + medCode);
					} else {
						// Si la première méthode échoue, essayer avec une requête plus large
						System.err.println("ATTENTION: Code non trouvé par num_serie, tentative alternative...");
						rsCode.close();
						getCodePs.close();
						
						// Méthode alternative : récupérer le dernier code inséré pour ce num_serie
						String getCodeSql2 = "SELECT code FROM medicament WHERE num_serie = ? ORDER BY code DESC FETCH FIRST 1 ROWS ONLY";
						getCodePs = conn.prepareStatement(getCodeSql2);
						getCodePs.setLong(1, m.numSerie);
						rsCode = getCodePs.executeQuery();
						
						if (rsCode.next()) {
							medCode = rsCode.getLong("code");
//							System.out.println("✓ Code récupéré (méthode alternative) pour médicament " + m.getNom() + ": " + medCode);
						} else {
							System.err.println("ERREUR: Aucun code trouvé après insertion du médicament " + m.getNom() + " avec num_serie " + m.numSerie);
							// Vérifier combien de médicaments existent avec ce num_serie
							String checkCountSql = "SELECT COUNT(*) as cnt FROM medicament WHERE num_serie = ?";
							PreparedStatement checkCountPs = conn.prepareStatement(checkCountSql);
							checkCountPs.setLong(1, m.numSerie);
							ResultSet checkRs = checkCountPs.executeQuery();
							if (checkRs.next()) {
								int cnt = checkRs.getInt("cnt");
								System.err.println("Nombre de médicaments avec num_serie " + m.numSerie + ": " + cnt);
							}
							checkRs.close();
							checkCountPs.close();
						}
					}
				} finally {
					if (rsCode != null && !rsCode.isClosed()) rsCode.close();
					if (getCodePs != null && !getCodePs.isClosed()) getCodePs.close();
				}
				
				if (medCode != -1) {
					try {
						if (m instanceof MedChimique) {
							MedChimique medChim = (MedChimique) m;
							String sqlChim = "INSERT INTO med_chimique (med_code, constituant, age_min) VALUES (?, ?, ?)";
							PreparedStatement psChim = conn.prepareStatement(sqlChim);
							psChim.setLong(1, medCode);
							psChim.setString(2, medChim.getConstituant());
							psChim.setInt(3, medChim.getAgeMin());
							int rowsChim = psChim.executeUpdate();
							psChim.close();
							System.out.println("- Médicament chimique ajouté - code: " + medCode + ", nom: " + m.getNom() + ", lignes: " + rowsChim+'\n');
						} else if (m instanceof MedHemeopathique) {
							MedHemeopathique medHomeo = (MedHemeopathique) m;
							String sqlHomeo = "INSERT INTO med_homeopathique (med_code, nom_plante, composition) VALUES (?, ?, ?)";
							PreparedStatement psHomeo = conn.prepareStatement(sqlHomeo);
							psHomeo.setLong(1, medCode);
							psHomeo.setString(2, medHomeo.getNomPlante());
							// Gérer le cas où composition peut être null
							if (medHomeo.getComposition() != null && !medHomeo.getComposition().isEmpty()) {
								psHomeo.setString(3, medHomeo.getComposition());
							} else {
								psHomeo.setNull(3, java.sql.Types.VARCHAR);
							}
							int rowsHomeo = psHomeo.executeUpdate();
							psHomeo.close();
							System.out.println("-Médicament homéopathique ajouté - code: " + medCode + ", nom: " + m.getNom() + ", lignes: " + rowsHomeo+'\n');
						} else {
							System.err.println("ERREUR: Type de médicament inconnu pour " + m.getNom() + " (classe: " + m.getClass().getName() + ")");
							throw new SQLException("Type de médicament inconnu: " + m.getClass().getName());
						}
					} catch (SQLException eInsert) {
						System.err.println("ERREUR lors de l'insertion dans la table spécifique pour " + m.getNom() + ": " + eInsert.getMessage());
						System.err.println("Code SQL: " + eInsert.getErrorCode());
						eInsert.printStackTrace();
						throw eInsert; // Relancer pour faire un rollback
					}
				} else {
					System.err.println("ERREUR CRITIQUE: Impossible de récupérer le code du médicament inséré " + m.getNom());
					throw new SQLException("Impossible de récupérer le code du médicament inséré");
				}
			}
			
			rsCheck.close();
			checkPs.close();
			
			conn.commit(); // Valider transaction
//			System.out.println("✓ Transaction commitée avec succès");
			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			try { 
				if (conn != null) {
					conn.rollback(); 
					conn.setAutoCommit(true);
				}
			} catch (SQLException ex) { 
				ex.printStackTrace(); 
			}
			System.err.println("Erreur lors de l'ajout du médicament: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void supprimerMedicament(String nomMedicament) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// On cherche le premier médicament avec ce nom qui a du stock
			String sql = "SELECT code, quantite FROM medicament WHERE nom = ? AND quantite > 0 FETCH FIRST 1 ROWS ONLY";
			ps = conn.prepareStatement(sql);
			ps.setString(1, nomMedicament);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				long code = rs.getLong("code");
				int qte = rs.getInt("quantite");
				
				if (qte > 1) {
					String update = "UPDATE medicament SET quantite = ? WHERE code = ?";
					PreparedStatement up = conn.prepareStatement(update);
					up.setInt(1, qte - 1);
					up.setLong(2, code);
					up.executeUpdate();
					up.close();
				} else {
					// Suppression physique (Cascade supprimera dans les tables filles)
					String delete = "DELETE FROM medicament WHERE code = ?";
					PreparedStatement del = conn.prepareStatement(delete);
					del.setLong(1, code);
					del.executeUpdate();
					del.close();
				}
			}
		} catch (SQLException e) { 
			System.err.println("Erreur lors de la suppression du médicament: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int nombreMedicaments() {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT NVL(SUM(quantite), 0) as total FROM medicament";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.next()) return rs.getInt("total");
		} catch (SQLException e) { 
			System.err.println("Erreur lors du comptage des médicaments: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	// --- Gestion des Clients ---

	public void ajouterClient(ClientFidele c) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		PreparedStatement psCheck = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			// Vérifier si le client existe déjà
			String checkSql = "SELECT cin FROM client WHERE cin = ?";
			psCheck = conn.prepareStatement(checkSql);
			psCheck.setLong(1, c.cin);
			rs = psCheck.executeQuery();
			
			if (rs.next()) {
				System.out.println("Le client avec CIN " + c.cin + " existe déjà. Mise à jour des informations.");
				// Mise à jour si existe déjà
				String updateSql = "UPDATE client SET nom = ?, prenom = ?, credit = ? WHERE cin = ?";
				ps = conn.prepareStatement(updateSql);
				ps.setString(1, c.getNom());
				ps.setString(2, c.prenom);
				ps.setDouble(3, c.getCredit());
				ps.setLong(4, c.cin);
				ps.executeUpdate();
			} else {
				// Insertion nouvelle
				String sql = "INSERT INTO client (cin, nom, prenom, credit) VALUES (?, ?, ?, ?)";
				ps = conn.prepareStatement(sql);
				ps.setLong(1, c.cin);
				ps.setString(2, c.getNom());
				ps.setString(3, c.prenom);
				ps.setDouble(4, c.getCredit());
				ps.executeUpdate();
			}
		} catch (SQLException e) { 
			System.err.println("Erreur lors de l'ajout du client: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (psCheck != null) psCheck.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void supprimerClient(long cin) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		PreparedStatement ps = null;
		try {
			String sql = "DELETE FROM client WHERE cin = ?";
			ps = conn.prepareStatement(sql);
			ps.setLong(1, cin);
			ps.executeUpdate();
		} catch (SQLException e) { 
			System.err.println("Erreur lors de la suppression du client: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public List<ClientFidele> getAllClients() {
		List<ClientFidele> list = new ArrayList<>();
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return list;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM client";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				ClientFidele c = new ClientFidele(rs.getLong("cin"), rs.getString("nom"), rs.getString("prenom"));
				c.setCredit(rs.getDouble("credit"));
				list.add(c);
			}
		} catch (SQLException e) { 
			System.err.println("Erreur lors de la récupération des clients: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	// --- Recherche avec Streams  ---

	public List<Medicament> rechercherParNom(String nom) {
		return getAllMedicaments().stream()
				.filter(m -> m.getNom().equalsIgnoreCase(nom))
				.collect(Collectors.toList());
	}
	
	public List<Medicament> rechercherParCategorie(String genre) {
		return getAllMedicaments().stream()
				.filter(m -> m.getGenre().equalsIgnoreCase(genre))
				.collect(Collectors.toList());
	}
	
	public List<Medicament> rechercherParPremieresLettres(String debut) {
		return getAllMedicaments().stream()
				.filter(m -> m.getNom().toLowerCase().startsWith(debut.toLowerCase()))
				.collect(Collectors.toList());
	}

	// --- Achats  ---

	public double achatMedicament(String nomMedicament, long cin) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return 0;
		
		PreparedStatement psMed = null;
		ResultSet rsMed = null;
		PreparedStatement psClient = null;
		ResultSet rsClient = null;
		
		try {
			conn.setAutoCommit(false); // Début de la transaction
			//  Vérif stock et prix
			String sqlMed = "SELECT code, prix, quantite FROM medicament WHERE nom = ? AND quantite > 0 FETCH FIRST 1 ROWS ONLY";
			psMed = conn.prepareStatement(sqlMed);
			psMed.setString(1, nomMedicament);
			rsMed = psMed.executeQuery();
			
			if (!rsMed.next()) {
				conn.rollback();
				conn.setAutoCommit(true);
				System.out.println("Médicament non disponible");
				return 0;
			}
			
			double prix = rsMed.getDouble("prix");
			
			// Décrémenter stock
			supprimerMedicament(nomMedicament); 
			
			// Gestion fidélité client
			String sqlClient = "SELECT credit FROM client WHERE cin = ?";
			psClient = conn.prepareStatement(sqlClient);
			psClient.setLong(1, cin);
			rsClient = psClient.executeQuery();
			
			double prixFinal = prix;
			
			if (rsClient.next()) {
				double cumul = rsClient.getDouble("credit");
				if (cumul + prix > 100) {
					// Remise 15% si cumul > 100
					prixFinal = prix * 0.85;
					String update = "UPDATE client SET credit = 0 WHERE cin = ?"; // Reset fidélité
					PreparedStatement up = conn.prepareStatement(update);
					up.setLong(1, cin);
					up.executeUpdate();
					up.close();
				} else {
					// Ajout au cumul
					String update = "UPDATE client SET credit = ? WHERE cin = ?";
					PreparedStatement up = conn.prepareStatement(update);
					up.setDouble(1, cumul + prix);
					up.setLong(2, cin);
					up.executeUpdate();
					up.close();
				}
			}
			
			conn.commit(); // Validation de la transaction
			conn.setAutoCommit(true);
			return prixFinal;
			
		} catch (SQLException e) { 
			try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
			System.err.println("Erreur lors de l'achat du médicament: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rsClient != null) rsClient.close();
				if (psClient != null) psClient.close();
				if (rsMed != null) rsMed.close();
				if (psMed != null) psMed.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	// Achat avec facilité de paiement (Crédit insuffisant exception)
	// Note: Dans l'énoncé, "facilités de paiement" est un peu vague.
	// Ici on garde la méthode signature existante qui lève une exception.
	public double achatMedicamentCredit(String nom, long cin, ClientFidele c) throws CreditInsuffisant {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return 0;
		
		// 1. Récupérer les infos nécessaires (Prix médicament et Crédit client) AVANT l'achat
		double creditActuel = 0;
		double prixEstime = 0;
		boolean medExiste = false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// Crédit client
			String sqlClient = "SELECT credit FROM client WHERE cin = ?";
			ps = conn.prepareStatement(sqlClient);
			ps.setLong(1, cin);
			rs = ps.executeQuery();
			if (rs.next()) {
				creditActuel = rs.getDouble("credit");
			} else {
				return 0; // Client introuvable
			}
			rs.close();
			ps.close();
			
			// Prix médicament
			String sqlMed = "SELECT prix FROM medicament WHERE nom = ? AND quantite > 0 FETCH FIRST 1 ROWS ONLY";
			ps = conn.prepareStatement(sqlMed);
			ps.setString(1, nom);
			rs = ps.executeQuery();
			if (rs.next()) {
				prixEstime = rs.getDouble("prix");
				medExiste = true;
			}
		} catch (SQLException e) {
			System.err.println("Erreur lors de la vérification pré-achat: " + e.getMessage());
			e.printStackTrace();
			return 0;
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (!medExiste) return 0;
		
		// 2. Calculer le prix final prévisionnel (Même logique que achatMedicament)
		double prixFinal = prixEstime;
		if (creditActuel + prixEstime > 100) {
			prixFinal = prixEstime * 0.85;
		}
		
		// 3. Vérifier le crédit AVANT d'effectuer l'achat (et donc la transaction)
		if (creditActuel < prixFinal) {
			throw new CreditInsuffisant("Crédit insuffisant pour l'achat. Crédit disponible: " + creditActuel + ", Prix à payer: " + prixFinal);
		}
		
		// 4. Si tout est bon, on appelle la méthode qui effectue la transaction
		return achatMedicament(nom, cin);
	}

	// --- Expiration (Consigne A.4) ---
	
	public void afficherEtSoldesMedicamentsExpirantBientot() {
		List<Medicament> all = getAllMedicaments();
		LocalDate now = LocalDate.now();
		LocalDate inOneMonth = now.plusMonths(1);
		
//		System.out.println("--- Traitement des médicaments expirant dans un mois ---");
		
		List<Medicament> aSoldes = all.stream()
			.filter(m -> m.getDateExpiration() != null && 
						 m.getDateExpiration().isAfter(now) && 
						 m.getDateExpiration().isBefore(inOneMonth))
			.collect(Collectors.toList());
			
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		
		for (Medicament m : aSoldes) {
			double nouveauPrix = m.getPrix() * 0.70; // Remise 30%
			System.out.println("Remise appliquée sur " + m.getNom() + " : " + m.getPrix() + " -> " + nouveauPrix);
			m.setPrix(nouveauPrix);
			
			PreparedStatement ps = null;
			try {
				String sql = "UPDATE medicament SET prix = ? WHERE code = ?";
				ps = conn.prepareStatement(sql);
				ps.setDouble(1, nouveauPrix);
				ps.setLong(2, m.code);
				ps.executeUpdate();
			} catch (SQLException e) { 
				System.err.println("Erreur lors de la mise à jour du prix: " + e.getMessage());
				e.printStackTrace(); 
			} finally {
				try {
					if (ps != null) ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// --- Gestion des Appareils Médicaux ---

	public List<AppareilMed> getAllAppareils() {
		List<AppareilMed> list = new ArrayList<>();
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return list;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM appareil";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				AppareilMed a = new AppareilMed(rs.getString("nom"), rs.getDouble("prix"), rs.getInt("quantite"));
				list.add(a);
			}
		} catch (SQLException e) { 
			System.err.println("Erreur lors de la récupération des appareils: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public void ajouterAppareil(AppareilMed a) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		PreparedStatement psCheck = null;
		ResultSet rs = null;
		try {
			// Check si existe
			String check = "SELECT id, quantite FROM appareil WHERE nom = ? FETCH FIRST 1 ROWS ONLY";
			psCheck = conn.prepareStatement(check);
			psCheck.setString(1, a.getNom());
			rs = psCheck.executeQuery();
			
			if (rs.next()) {
				int qte = rs.getInt("quantite");
				String update = "UPDATE appareil SET quantite = ? WHERE id = ?";
				PreparedStatement psUp = conn.prepareStatement(update);
				psUp.setInt(1, qte + 1);
				psUp.setLong(2, rs.getLong("id"));
				psUp.executeUpdate();
				psUp.close();
			} else {
				String sql = "INSERT INTO appareil (nom, prix, quantite) VALUES (?, ?, ?)";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, a.getNom());
				ps.setDouble(2, a.getPrix());
				ps.setInt(3, a.getQuantite());
				ps.executeUpdate();
				ps.close();
			}
		} catch (SQLException e) { 
			System.err.println("Erreur lors de l'ajout de l'appareil: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (psCheck != null) psCheck.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void supprimerAppareil(String nomAppareil) {
		Connection conn = ConnexionBD.getConnection();
		if (conn == null) return;
		PreparedStatement psCheck = null;
		ResultSet rs = null;
		try {
			String check = "SELECT id, quantite FROM appareil WHERE nom = ? AND quantite > 0";
			psCheck = conn.prepareStatement(check);
			psCheck.setString(1, nomAppareil);
			rs = psCheck.executeQuery();
			
			if (rs.next()) {
				int qte = rs.getInt("quantite");
				long id = rs.getLong("id");
				if (qte > 1) {
					String update = "UPDATE appareil SET quantite = ? WHERE id = ?";
					PreparedStatement psUp = conn.prepareStatement(update);
					psUp.setInt(1, qte - 1);
					psUp.setLong(2, id);
					psUp.executeUpdate();
					psUp.close();
				} else {
					String del = "DELETE FROM appareil WHERE id = ?";
					PreparedStatement psDel = conn.prepareStatement(del);
					psDel.setLong(1, id);
					psDel.executeUpdate();
					psDel.close();
				}
			}
		} catch (SQLException e) { 
			System.err.println("Erreur lors de la suppression de l'appareil: " + e.getMessage());
			e.printStackTrace(); 
		} finally {
			try {
				if (rs != null) rs.close();
				if (psCheck != null) psCheck.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// --- Achat Générique (Vendable) avec Facilités ---

	public double acheterVendable(Vendable v, long cin, boolean facilites) {
		// Identification du type et gestion du stock
		if (v instanceof Medicament) {
			// Utilise la logique existante pour décrémenter stock medicament
			// Mais attention, achatMedicament fait tout (stock + fidélité).
			// On va réutiliser achatMedicament mais gérer le retour selon facilités.
			
			double prixFinal = achatMedicament(v.getNom(), cin);
			if (prixFinal == 0) return 0; // Echec achat
			
			if (facilites) {
				// Si facilité, on paie la tranche
				// Note: getTranche() retourne une partie du prix *initial* (ex: prix * 0.8).
				// Si prixFinal (après remise fidélité) est différent, faut-il appliquer tranche sur prixFinal ?
				// Logique simple: Tranche sur le prix à payer.
				// Mais v.getTranche() est calculé sur v.prix (base).
				// On va supposer que la facilité s'applique sur le montant final à payer.
				// MAIS v.getTranche() est hardcodé.
				// On va utiliser v.getTranche() tel quel comme "Montant à payer maintenant".
				return v.getTranche();
			}
			return prixFinal;
			
		} else if (v instanceof AppareilMed) {
			// Gestion stock appareil
			Connection conn = ConnexionBD.getConnection();
			if (conn == null) return 0;
			PreparedStatement ps = null;
			ResultSet rs = null;
			PreparedStatement psClient = null;
			ResultSet rsClient = null;
			try {
				conn.setAutoCommit(false); // Début de la transaction
				String check = "SELECT id, prix, quantite FROM appareil WHERE nom = ? AND quantite > 0 FETCH FIRST 1 ROWS ONLY";
				ps = conn.prepareStatement(check);
				ps.setString(1, v.getNom());
				rs = ps.executeQuery();
				
				if (!rs.next()) {
					conn.rollback();
					conn.setAutoCommit(true);
					System.out.println("Appareil non disponible");
					return 0;
				}
				
				double prix = rs.getDouble("prix");
				
				// Décrémenter stock
				supprimerAppareil(v.getNom());
				
				// Gestion fidélité (Copier coller logique medicament)
				String sqlClient = "SELECT credit FROM client WHERE cin = ?";
				psClient = conn.prepareStatement(sqlClient);
				psClient.setLong(1, cin);
				rsClient = psClient.executeQuery();
				
				double prixFinal = prix;
				
				if (rsClient.next()) {
					double cumul = rsClient.getDouble("credit");
					if (cumul + prix > 100) {
						prixFinal = prix * 0.85; // Remise 15%
						String update = "UPDATE client SET credit = 0 WHERE cin = ?";
						PreparedStatement up = conn.prepareStatement(update);
						up.setLong(1, cin);
						up.executeUpdate();
						up.close();
					} else {
						String update = "UPDATE client SET credit = ? WHERE cin = ?";
						PreparedStatement up = conn.prepareStatement(update);
						up.setDouble(1, cumul + prix);
						up.setLong(2, cin);
						up.executeUpdate();
						up.close();
					}
				}
				
				conn.commit(); // Validation de la transaction
				conn.setAutoCommit(true);
				
				if (facilites) {
					return v.getTranche();
				}
				return prixFinal;
				
			} catch (SQLException e) { 
				try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
				System.err.println("Erreur lors de l'achat de l'appareil: " + e.getMessage());
				e.printStackTrace(); 
			} finally {
				try {
					if (rsClient != null) rsClient.close();
					if (psClient != null) psClient.close();
					if (rs != null) rs.close();
					if (ps != null) ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	
	// --- Méthodes de compatibilité (pour garder le code existant fonctionnel si besoin) ---
	
	public ArrayList<Medicament> getListeMedicament() {
		return new ArrayList<>(getAllMedicaments());
	}
	
	public ArrayList<ClientFidele> getListeClientFidele() {
		return new ArrayList<>(getAllClients());
	}

	// --- Méthodes pour TestVendable (Traitement de listes en mémoire via Streams) ---

	public double calculerPrixFidele(List<Vendable> liste) {
		return liste.stream()
				.mapToDouble(Vendable::getPrix)
				.sum();
	}

	public void listerMedicamentsHomeo(List<Vendable> liste) {
		System.out.println("--- Médicaments Homéopathiques ---");
		// Utilisation des Streams pour filtrer et afficher
		liste.stream()
			// 1. Filtrer pour ne garder que les objets de type MedHemeopathique
			.filter(v -> v instanceof MedHemeopathique)
			// 2. Pour chaque élément filtré, afficher son nom
			.forEach(v -> System.out.println(v.getNom()));
	}

	public List<String> medicamentsRisques(List<Vendable> liste) {
		// Retourne les noms des médicaments chimiques avec age minimum > 10 ans
		return liste.stream()
				// 1. Filtrer : garder uniquement les MedChimique
				.filter(v -> v instanceof MedChimique)
				// 2. Caster : convertir le type Vendable en MedChimique pour accéder à getAgeMin()
				.map(v -> (MedChimique) v)
				// 3. Filtrer : garder ceux dont l'âge minimum est > 10
				.filter(m -> m.getAgeMin() > 10)
				// 4. Transformer : ne garder que le nom (String)
				.map(Medicament::getNom)
				// 5. Collecter : mettre les résultats dans une liste
				.collect(Collectors.toList());
	}
	
	// --- Partie B (Version simple) ---
	
	public List<Vendable> trierVendablesParPrix(List<Vendable> liste) {
		return liste.stream()
				.sorted(Comparator.comparingDouble(Vendable::getPrix))
				.collect(Collectors.toList());
	}
	
	public List<String> nomsVendables(List<Vendable> liste) {
		return liste.stream()
				.map(Vendable::getNom)
				.collect(Collectors.toList());
	}
	
	public double totalApresRemise(List<Vendable> liste, double pourcentage) {
		double p = Math.max(0.0, Math.min(1.0, pourcentage));
		return liste.stream()
				.mapToDouble(v -> v.getPrix() * (1.0 - p))
				.sum();
	}
	
	public String resumeTypes(List<Vendable> liste) {
		long chimiques = liste.stream().filter(v -> v instanceof MedChimique).count();
		long homeos = liste.stream().filter(v -> v instanceof MedHemeopathique).count();
		long appareils = liste.stream().filter(v -> v instanceof AppareilMed).count();
		return "chimique=" + chimiques + ", homeo=" + homeos + ", appareil=" + appareils;
	}
}
