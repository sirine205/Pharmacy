package pharmacie;
import java.util.ArrayList;
import java.util.Arrays;


public class Etagere {
	int nombreMedicament;
	Medicament[] tabMedicament;
	ArrayList<Medicament> listeMedicament;
	int nbEffectif=0;
	
	
	public Etagere(int N) {
		nombreMedicament = N;
		tabMedicament = new Medicament[N];
		listeMedicament = new ArrayList<>();
	}
	
	public int nombreMedicamentsTab() {
		return this.nbEffectif;
	}
	
	public void ajoutMedicamentTab(Medicament m1) {
		if(nbEffectif < nombreMedicament) {
			tabMedicament[nbEffectif]= m1;
			nbEffectif++;
		}else {
			System.out.println("etagere pleine");
		}
	}
	
	public Medicament getMedicamentTab(int position) {
	    if (position < 1 || position > this.nbEffectif) {
	        System.out.println("position invalide");
	        return null;
	    }

	    int i = 0;
	    while (i < nbEffectif) {
	        if (i == position - 1) { 
	            return tabMedicament[i];
	        }
	        i++;
	    }

	    return null;
	}
	
	public int rechercheMedicamentTab(String nom,String genre) {
		int i=0;
		while(i<nbEffectif) {
			if(tabMedicament[i].nom.equals(nom) && tabMedicament[i].genre.equals(genre)) {
				return (i+1);
			}
			i++;
		}
		return 0;
	}
	
	public Medicament enleverMedicamentTab(int position) {
		
		if (position < 1 || position> nbEffectif ) {
			return null;
			
		}
		
		Medicament m = tabMedicament[position-1];
			
		for(int i=position ; i<nbEffectif; i++) {
			tabMedicament[i-1]=tabMedicament[i];
		}
		
		nbEffectif--;
		
		return m;
	}
	
	public Medicament enleverMedicamentTab(String nom , String genre) {
		int i = this.rechercheMedicamentTab(nom, genre) - 1;
			if (i < 0 ) {
				System.out.println("Médicament introuvable !");
				return null;
	    }
			Medicament m = tabMedicament[i];
	
			for(int j=i ; j<nbEffectif-1; j++) {
				tabMedicament[j]=tabMedicament[j+1];
			}
	
			nbEffectif--;
	
			return m;
	}
	@Override
	public String toString() {
	    String texte = "Contenu de l’étagère(tableau) :\n";

	    if (this.nombreMedicament == 0) {
	        texte += "Aucun médicament.\n";
	    } else {
	        int i = 0;
	        while (i < this.nbEffectif) {
	            texte += (i + 1) + ". " + tabMedicament[i] + "\n";
	            i++;
	        }
	    }

	    return texte;
	}



public int nombreMedicamentsListe() {
	return listeMedicament.size();
}

public void ajoutMedicamentListe(Medicament m1) {
	if(listeMedicament.size() < this.nombreMedicament) {
		listeMedicament.add(m1);
	}
	else {
        System.out.println("étagère pleine !");
    }

}

public Medicament getMedicamentListe(int position) {
	if (position < 1 || position > listeMedicament.size()) {
        System.out.println("position invalide");
        return null;
    }
	int i = 0;
    while (i < listeMedicament.size()) {
        if (i == position - 1) {
            return listeMedicament.get(i);
        }
        i++;
    }
    return null;
}

public int rechercheMedicamentListe(String nom, String genre) {
    int i = 0;
    while (i < listeMedicament.size()) {
        Medicament m = listeMedicament.get(i);
        if (m.getNom().equalsIgnoreCase(nom) &&
            m.getGenre().equalsIgnoreCase(genre)) {
            return i + 1;
        }
        i++;
    }
    return 0;
}
public Medicament enleverMedicamentListe(int position) {
	
	if (position < 1 || position > listeMedicament.size()) {
        System.out.println("Position invalide !");
        return null;
    }

    int i = 0;
    while (i < listeMedicament.size()) {
        if (i == position - 1) {
            return listeMedicament.remove(i);
        }
        i++;
    }
    return null;
}

public Medicament enleverMedicamentListe(String nom, String genre) {
    int pos = this.rechercheMedicamentListe(nom, genre);
    if (pos == 0) {
        System.out.println("Médicament introuvable !");
        return null;
    }
    return this.getMedicamentListe(pos);
}

public String toString2() {
    String texte = "Contenu de l’étagère(liste) :\n";

    if (listeMedicament.isEmpty()) {
        texte += "Aucun médicament.\n";
    } else {
        int i = 0;
        while (i < listeMedicament.size()) {
            Medicament m = listeMedicament.get(i);

            if (m != null) { 
                texte += (i + 1) + ". " + m + "\n";
            }

            i++;
        }
    }

    return texte;
}



	
	
}
