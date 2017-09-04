/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.texte;

import java.awt.event.*;
import java.util.*;
import visuAnalec.chaines.*;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;

/**
 *
 * @author Bernard
 */
public class PanneauAffChainesTexte extends PanneauTexte {
  private ListenerSourisTexte sourisListener;
  private VisuChaines visuChaines;
  private Marquage marquage;
  public PanneauAffChainesTexte(Corpus corpus, VisuChaines visuChaines) {
    super(corpus);
    setEditable(false);
    this.visuChaines = visuChaines;
    setSourisListener(visuChaines);
    marquage = new Marquage(this);
  }
  protected void setSourisListener(VisuChaines vChaines) {
    sourisListener = new ListenerSourisTexte(this, vChaines);
    addMouseListener(sourisListener);
  }
  public void pasDeMarquage() {
    marquerUnites(marquage, new TypeMarquage[0], null);
  }
  public void montrerUnite(Unite unit, TypeMarquage typeSelectUnite) {
    TypeMarquage[] typesMarq = new TypeMarquage[]{typeSelectUnite};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(typeSelectUnite, new Unite[]{unit});
    marquerUnites(marquage, typesMarq, unites);
    placerAscenseur(unit);
  }
  public void montrerUnite(Unite unit, TypeMarquage typeSelectUnite, Unite[] unitsChaine,
          TypeMarquage typeSelectChaine) {
    TypeMarquage[] typesMarq = new TypeMarquage[]{typeSelectUnite, typeSelectChaine};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(typeSelectUnite, new Unite[]{unit});
    unites.put(typeSelectChaine, unitsChaine);
    marquerUnites(marquage, typesMarq, unites);
    placerAscenseur(unit);
  }
  public void colorerUnites(Unite[] units, TypeMarquage typeColorChaines) {
    TypeMarquage[] typesMarq = new TypeMarquage[]{typeColorChaines};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(typeColorChaines, units);
    marquerUnites(marquage, typesMarq, unites);
  }

  protected static class ListenerSourisTexte extends MouseAdapter {
    private PanneauAffChainesTexte panneauTexte;
    private VisuChaines visuChaines;
    public ListenerSourisTexte(PanneauAffChainesTexte pTexte, VisuChaines vChaines) {
      this.panneauTexte = pTexte;
      this.visuChaines = vChaines;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
 try {
   Unite uniteVisee = panneauTexte.getUniteVisee(panneauTexte.marquage, e.getPoint());
      if (uniteVisee == null)  visuChaines.traiterAffTexteClicVide();
      else  if (e.getClickCount() == 2) visuChaines.traiterAffTexteDoubleClic(uniteVisee);
     } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
   }
  }
}
