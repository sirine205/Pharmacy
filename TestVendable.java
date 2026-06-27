package pharmacie;

import java.time.LocalDate;
import java.util.ArrayList;

public class TestVendable {
	
	public static void main(String[] args) {
		
		MedHemeopathique m1=new MedHemeopathique("A","AA",12.5,123456789L,"Acacia");
		MedHemeopathique m2=new MedHemeopathique("A","AA",12.5,123456789L,"Acacia");
		MedHemeopathique m3=new MedHemeopathique("B","BB",32.5,123456789L,"Verveine");
		
	    MedChimique m4=new MedChimique("C","CC", 4.5,456789123L,"ABC",12);
	    AppareilMed m5=new AppareilMed("App",123.5);
	    
	    ArrayList<Vendable> listeVendable=new ArrayList<Vendable>();
	    listeVendable.add(m1);
	    listeVendable.add(m2);
	    listeVendable.add(m3);
	    listeVendable.add(m4);
	    listeVendable.add(m5);
	    
	    Pharmacie pharmacie=new Pharmacie();
	    
	   
	    System.out.println(listeVendable);
	    
	    System.out.println("Prix Total: " + pharmacie.calculerPrixFidele(listeVendable));
	    
	    pharmacie.listerMedicamentsHomeo(listeVendable);
	   
	    m1.setDateExpiration(LocalDate.of(2025,11,30));
	    m2.setDateExpiration(LocalDate.of(2025,12,30));
	    m3.setDateExpiration(LocalDate.of(2026,12,30));
	    m4.setDateExpiration(LocalDate.of(2027,12,30));
	   
	    System.out.print("Médicaments Risqués: " + pharmacie.medicamentsRisques(listeVendable));
	    
	    
	}

}
