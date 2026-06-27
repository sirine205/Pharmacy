package pharmacie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnexionBD {
    private static Connection connection;
    // Paramètres de connexion par défaut (à adapter si besoin)
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1"; 
    private static final String USER = "system";
    private static final String PASSWORD = "system";

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Chargement explicite du driver Oracle
                Class.forName("oracle.jdbc.OracleDriver"); 
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("connexion reussie");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Vérifie et crée les tables si elles n'existent pas (sans supprimer les données existantes)
     */
    public static void initializeDatabase() {
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("Impossible de se connecter à la base de données");
            return;
        }
        
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            
            // Vérifier si les tables existent déjà
            boolean tablesExist = false;
            try {
                stmt.executeQuery("SELECT 1 FROM medicament WHERE ROWNUM = 1");
                tablesExist = true;
                System.out.println("Les tables existent déjà - les données sont préservées");
            } catch (SQLException e) {
                // Les tables n'existent pas, on va les créer
                tablesExist = false;
            }
            
            if (!tablesExist) {
                System.out.println("Création des tables...");
                
                // Script SQL pour créer les tables (syntaxe Oracle) - SANS SUPPRIMER LES DONNÉES
                String[] sqlStatements = {
                    // Création des tables - Syntaxe Oracle avec IDENTITY columns (Oracle 12c+)
                    "CREATE TABLE medicament (" +
                      "code            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                      "num_serie       NUMBER(19) UNIQUE NOT NULL," +
                      "nom             VARCHAR2(50)," +
                      "genre           VARCHAR2(50)," +
                      "prix            NUMBER(10,2)," +
                      "date_expiration DATE," +
                      "quantite        NUMBER(10) DEFAULT 1" +
                    ")",
                    
                    "CREATE TABLE med_chimique (" +
                      "med_code    NUMBER PRIMARY KEY," +
                      "constituant VARCHAR2(100)," +
                      "age_min     NUMBER(10)," +
                      "CONSTRAINT fk_chim_med FOREIGN KEY (med_code) REFERENCES medicament(code) ON DELETE CASCADE" +
                    ")",
                    
                    "CREATE TABLE med_homeopathique (" +
                      "med_code    NUMBER PRIMARY KEY," +
                      "nom_plante  VARCHAR2(100)," +
                      "composition VARCHAR2(100)," +
                      "CONSTRAINT fk_homeo_med FOREIGN KEY (med_code) REFERENCES medicament(code) ON DELETE CASCADE" +
                    ")",
                    
                    "CREATE TABLE client (" +
                      "cin    NUMBER(19) PRIMARY KEY," +
                      "nom    VARCHAR2(50)," +
                      "prenom VARCHAR2(50)," +
                      "credit NUMBER(10,2) DEFAULT 10.0" +
                    ")",
                    
                    "CREATE TABLE appareil (" +
                      "id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                      "nom      VARCHAR2(50)," +
                      "prix     NUMBER(10,2)," +
                      "quantite NUMBER(10) DEFAULT 1" +
                    ")",
                    
                    // Création des index - Syntaxe Oracle (seulement si n'existent pas)
                    "CREATE INDEX idx_medicament_nom ON medicament(nom)",
                    "CREATE INDEX idx_appareil_nom ON appareil(nom)"
                };
                
                for (String sql : sqlStatements) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        // Ignorer les erreurs de table/index déjà existant (942, 955)
                        if (e.getErrorCode() != 942 && e.getErrorCode() != 955) {
                            System.err.println("Erreur lors de l'exécution: " + sql);
                            System.err.println("Erreur: " + e.getMessage());
                        }
                    }
                }
                
                System.out.println("Tables créées avec succès!");
            } else {
                System.out.println("Base de données déjà initialisée - données préservées\n");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Réinitialise complètement la base de données (supprime toutes les données)
     * À utiliser avec précaution - seulement pour les tests ou la réinitialisation complète
     */
    public static void resetDatabase() {
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("Impossible de se connecter à la base de données");
            return;
        }
        
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            
            System.out.println("ATTENTION: Suppression de toutes les tables et données...");
            
            // Suppression des tables existantes (si elles existent) - Syntaxe PL/SQL Oracle
            String[] dropStatements = {
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE med_homeopathique CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE med_chimique CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE appareil CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE client CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE medicament CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;"
            };
            
            for (String sql : dropStatements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    if (e.getErrorCode() != 942) {
                        System.err.println("Erreur lors de la suppression: " + e.getMessage());
                    }
                }
            }
            
            // Recréer les tables
            initializeDatabase();
            System.out.println("Base de données réinitialisée!");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réinitialisation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
