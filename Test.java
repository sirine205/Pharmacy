package pharmacie;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;


public class Test {
	
	public static void main(String[] args) {
		Medicament m1 = new MedChimique("Aralgan","Paracetemol", 10.0, "Paracetamol", 12);
		Medicament m2 = new MedChimique("Effarlagon","Paracetamol", 12.0, "Paracetamol", 12);
		System.out.println(m1.compare_nom(m2));
		System.out.println(Medicament.compare_nom(m1,m2));
		m1.setPrix(70);
		m2.setPrix(30);
		System.out.println(m1.compare_prix(m2));
		
		Medicament [] tab_medocs = new Medicament[3];
		
		for(int i=0;i<3;i++) {
			Scanner saisie= new Scanner(System.in);
			
			System.out.println("entrer le nom ");
			String n = saisie.nextLine();
			
			System.out.println("entrer le genre ");
			String g = saisie.nextLine();
			
			System.out.println("entrer le prix ");
			double p = saisie.nextDouble();
			
			tab_medocs[i] = new MedChimique(n,g,p, "Unknown", 0);
		}
		
		System.out.println("le tableau avant le tri ");
		
		
		for(Medicament m : tab_medocs){
			System.out.println(m);
		}
		
		System.out.println("le tableau apres le tri ");
		
		for(int i =0 ; i<tab_medocs.length-1;i++) {
			int min = i;
			
			for(int j = i+1;j<tab_medocs.length;j++) {
				if (tab_medocs[j].compare_prix(tab_medocs[i])<0) {
					min = j;
				}
			}
			
			if(min != i) {
				Medicament temp = tab_medocs[i];
		        tab_medocs[i] = tab_medocs[min];
		        tab_medocs[min] = temp;
			}
		}
		
		Arrays.sort(tab_medocs, new Comparator<Medicament>() {

			@Override
			public int compare(Medicament o1, Medicament o2) {
				return Medicament.compare_prix(o1, o2);
			}
			
			
		});
		
		for(Medicament m : tab_medocs){
			System.out.println(m);
		}
		
	}
}
