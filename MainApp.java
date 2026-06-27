package pharmacie;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class MainApp extends Application {
	private final Pharmacie pharmacie = new Pharmacie();
	private final TableView<Medicament> medsTable = new TableView<>();
	private final TableView<ClientFidele> clientsTable = new TableView<>();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		// Initialiser la base de données au démarrage
		ConnexionBD.initializeDatabase();

		TabPane tabs = new TabPane();
		tabs.getTabs().add(tabAccueil());
		tabs.getTabs().add(tabMedicaments());
		tabs.getTabs().add(tabClients());
		Scene scene = new Scene(tabs, 900, 600);
		stage.setTitle("Pharmacie");
		stage.setScene(scene);
		stage.show();
	}

	private Tab tabAccueil() {
		Tab t = new Tab("Accueil");
		t.setClosable(false);
		Label medsCount = new Label();
		Label clientsCount = new Label();
		Button refresh = new Button("Rafraîchir");
		refresh.setOnAction(e -> {
			medsCount.setText("Médicaments: " + pharmacie.nombreMedicaments());
			clientsCount.setText("Clients: " + pharmacie.getAllClients().size());
		});
		VBox box = new VBox(12, new Label("Bienvenue"), medsCount, clientsCount, refresh);
		box.setPadding(new Insets(16));
		t.setContent(box);
		return t;
	}

	private Tab tabMedicaments() {
		Tab t = new Tab("Médicaments");
		t.setClosable(false);

		TextField nom = new TextField();
		nom.setPromptText("Nom");
		TextField genre = new TextField();
		genre.setPromptText("Catégorie");
		TextField prix = new TextField();
		prix.setPromptText("Prix");
		TextField numSerie = new TextField();
		numSerie.setPromptText("Numéro de série");
		DatePicker dateExpiration = new DatePicker();
		dateExpiration.setPromptText("Date d'expiration");
		ToggleGroup typeGroup = new ToggleGroup();
		RadioButton rbChim = new RadioButton("Chimique");
		RadioButton rbHomeo = new RadioButton("Homéopathique");
		rbChim.setToggleGroup(typeGroup);
		rbHomeo.setToggleGroup(typeGroup);
		rbChim.setSelected(true);
		TextField constituant = new TextField();
		constituant.setPromptText("Constituant");
		TextField ageMin = new TextField();
		ageMin.setPromptText("Âge min");
		TextField nomPlante = new TextField();
		nomPlante.setPromptText("Nom plante");
		TextField composition = new TextField();
		composition.setPromptText("Composition");

		Button ajouter = new Button("Ajouter");
		ajouter.setOnAction(e -> {
			try {
				if (nom.getText().isEmpty() || genre.getText().isEmpty() || prix.getText().isEmpty()
						|| numSerie.getText().isEmpty()) {
					throw new InputValidationException("Champs requis manquants");
				}
				double p = Double.parseDouble(prix.getText());
				long ns = Long.parseLong(numSerie.getText());
				Medicament m;
				if (rbChim.isSelected()) {
					if (constituant.getText().isEmpty() || ageMin.getText().isEmpty())
						throw new InputValidationException("Champs chimique manquants");
					int a = Integer.parseInt(ageMin.getText());
					m = new MedChimique(nom.getText(), genre.getText(), p, ns, constituant.getText(), a);
				} else {
					if (nomPlante.getText().isEmpty())
						throw new InputValidationException("Nom plante requis");
					m = new MedHemeopathique(nom.getText(), genre.getText(), p, ns, nomPlante.getText(),
							composition.getText());
				}
				// Définir la date d'expiration si elle a été saisie
				if (dateExpiration.getValue() != null) {
					m.setDateExpiration(dateExpiration.getValue());
				}
				pharmacie.ajouterMedicament(m);
				// Rafraîchir la liste après ajout
				List<Medicament> liste = pharmacie.getAllMedicaments();
				System.out.println("Nombre de médicaments récupérés après ajout: " + liste.size());
				chargerMedicaments(liste);
				// Vider les champs après ajout réussi
				nom.clear();
				genre.clear();
				prix.clear();
				numSerie.clear();
				dateExpiration.setValue(null);
				constituant.clear();
				ageMin.clear();
				nomPlante.clear();
				composition.clear();
				info("Ajout effectué - " + liste.size() + " médicament(s) en stock");
			} catch (NumberFormatException ex) {
				erreur("Prix/numéro/âge doivent être numériques");
			} catch (InputValidationException ex) {
				erreur(ex.getMessage());
			} catch (Exception ex) {
				erreur("Erreur: " + ex.getMessage());
			}
		});

		Button supprimer = new Button("Supprimer par nom");
		supprimer.setOnAction(e -> {
			try {
				if (nom.getText().isEmpty())
					throw new InputValidationException("Nom requis");
				pharmacie.supprimerMedicament(nom.getText());
				chargerMedicaments(pharmacie.getAllMedicaments());
				info("Suppression effectuée");
			} catch (InputValidationException ex) {
				erreur(ex.getMessage());
			} catch (Exception ex) {
				erreur("Erreur: " + ex.getMessage());
			}
		});

		TextField rechNom = new TextField();
		rechNom.setPromptText("Rechercher par nom");
		Button btnRechNom = new Button("Rechercher");
		btnRechNom.setOnAction(e -> chargerMedicaments(pharmacie.rechercherParNom(rechNom.getText())));

		TextField rechCat = new TextField();
		rechCat.setPromptText("Rechercher par catégorie");
		Button btnRechCat = new Button("Rechercher");
		btnRechCat.setOnAction(e -> chargerMedicaments(pharmacie.rechercherParCategorie(rechCat.getText())));

		TextField rechDeb = new TextField();
		rechDeb.setPromptText("Premières lettres");
		Button btnRechDeb = new Button("Rechercher");
		btnRechDeb.setOnAction(e -> chargerMedicaments(pharmacie.rechercherParPremieresLettres(rechDeb.getText())));

		Button lister = new Button("Lister tout");
		lister.setOnAction(e -> chargerMedicaments(pharmacie.getAllMedicaments()));

		TableColumn<Medicament, String> colNom = new TableColumn<>("Nom");
		colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
		TableColumn<Medicament, String> colGenre = new TableColumn<>("Catégorie");
		colGenre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGenre()));
		TableColumn<Medicament, String> colPrix = new TableColumn<>("Prix");
		colPrix.setCellValueFactory(
				c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getPrix())));
		TableColumn<Medicament, String> colDate = new TableColumn<>("Expiration");
		colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
				c.getValue().getDateExpiration() != null ? c.getValue().getDateExpiration().toString() : ""));
		medsTable.getColumns().setAll(colNom, colGenre, colPrix, colDate);

		rbChim.setOnAction(e -> {
			constituant.setDisable(false);
			ageMin.setDisable(false);
			nomPlante.setDisable(true);
			composition.setDisable(true);
		});
		rbHomeo.setOnAction(e -> {
			constituant.setDisable(true);
			ageMin.setDisable(true);
			nomPlante.setDisable(false);
			composition.setDisable(false);
		});
		rbChim.fire();

		HBox typeBox = new HBox(8, rbChim, rbHomeo);
		HBox chimBox = new HBox(8, constituant, ageMin);
		HBox homeoBox = new HBox(8, nomPlante, composition);
		HBox actions = new HBox(8, ajouter, supprimer, lister);
		HBox recherches = new HBox(8, rechNom, btnRechNom, rechCat, btnRechCat, rechDeb, btnRechDeb);
		VBox form = new VBox(10, nom, genre, prix, numSerie, dateExpiration, typeBox, chimBox, homeoBox, actions,
				recherches,
				medsTable);
		form.setPadding(new Insets(12));
		t.setContent(form);
		return t;
	}

	private Tab tabClients() {
		Tab t = new Tab("Clients");
		t.setClosable(false);

		TextField cin = new TextField();
		cin.setPromptText("CIN");
		TextField nom = new TextField();
		nom.setPromptText("Nom");
		TextField prenom = new TextField();
		prenom.setPromptText("Prénom");

		Button ajouter = new Button("Ajouter");
		ajouter.setOnAction(e -> {
			try {
				if (cin.getText().isEmpty() || nom.getText().isEmpty() || prenom.getText().isEmpty())
					throw new InputValidationException("Champs requis manquants");
				long c = Long.parseLong(cin.getText());
				ClientFidele cl = new ClientFidele(c, nom.getText(), prenom.getText());
				pharmacie.ajouterClient(cl);
				chargerClients(pharmacie.getAllClients());
				info("Ajout effectué");
			} catch (NumberFormatException ex) {
				erreur("CIN doit être numérique");
			} catch (InputValidationException ex) {
				erreur(ex.getMessage());
			} catch (Exception ex) {
				erreur("Erreur: " + ex.getMessage());
			}
		});

		Button supprimer = new Button("Supprimer");
		supprimer.setOnAction(e -> {
			try {
				if (cin.getText().isEmpty())
					throw new InputValidationException("CIN requis");
				long c = Long.parseLong(cin.getText());
				pharmacie.supprimerClient(c);
				chargerClients(pharmacie.getAllClients());
				info("Suppression effectuée");
			} catch (NumberFormatException ex) {
				erreur("CIN doit être numérique");
			} catch (InputValidationException ex) {
				erreur(ex.getMessage());
			} catch (Exception ex) {
				erreur("Erreur: " + ex.getMessage());
			}
		});

		Button lister = new Button("Lister tout");
		lister.setOnAction(e -> chargerClients(pharmacie.getAllClients()));

		TableColumn<ClientFidele, String> colNom = new TableColumn<>("Nom");
		colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
		TableColumn<ClientFidele, String> colPrenom = new TableColumn<>("Prénom");
		colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().prenom));
		TableColumn<ClientFidele, String> colCredit = new TableColumn<>("Crédit");
		colCredit.setCellValueFactory(
				c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getCredit())));
		clientsTable.getColumns().setAll(colNom, colPrenom, colCredit);

		HBox actions = new HBox(8, ajouter, supprimer, lister);
		VBox form = new VBox(10, cin, nom, prenom, actions, clientsTable);
		form.setPadding(new Insets(12));
		t.setContent(form);
		return t;
	}

	private void chargerMedicaments(List<Medicament> data) {
		ObservableList<Medicament> obs = FXCollections.observableArrayList(data);
		medsTable.setItems(obs);
	}

	private void chargerClients(List<ClientFidele> data) {
		ObservableList<ClientFidele> obs = FXCollections.observableArrayList(data);
		clientsTable.setItems(obs);
	}

	private void erreur(String message) {
		Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
		a.showAndWait();
	}

	private void info(String message) {
		Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
		a.showAndWait();
	}
}
