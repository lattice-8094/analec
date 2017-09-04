/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.chaines;

import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import visuAnalec.elements.*;
import visuAnalec.texte.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public class ModeleChaines {
  static class Chainon {
    Unite unite;
    String texte;
    Schema chaine = null;
    int nbEltsChaine = 0;
    String idChaine = "";
    int noChaine = 0;
    Chainon(Unite unite, String texte) {
      this.unite = unite;
      this.texte = texte;
    }
  }
  private Vue vue;
  private GChaines panneauGraphique;
  private Unite[] listeUnites;
  private HashMap<Unite, Schema> chainesUnitesNonFiltrees =
          new HashMap<Unite, Schema>();
  private int[] seuilsValsNbElements = new int[0];
  private HashMap<Unite, Chainon> chainonsUnites = new HashMap<Unite, Chainon>();
  private HashMap<Schema, ArrayList<Unite>> unitesChaines =
          new HashMap<Schema, ArrayList<Unite>>();
  private HashMap<String, HashSet<String>> valsMasqueesChamp =
          new HashMap<String, HashSet<String>>();
  private HashSet<String> valNulleMasqueeChamp = new HashSet<String>();
  private boolean[] valsMasqueesNbElements = new boolean[0];
  public ModeleChaines(Vue vue, GChaines panneauGraphique) {
    this.vue = vue;
    this.panneauGraphique = panneauGraphique;
  }
  Schema getChaine(Unite unit) {
    return chainonsUnites.get(unit).chaine;
  }
  Unite[] getUnites() {
    return listeUnites;
  }
  Unite[] getUnitesChaine(Schema chaine) {
    ArrayList<Unite> units = unitesChaines.get(chaine);
    return units.toArray(new Unite[0]);
  }
  boolean isMasqueeValChamp(String champ, String val) {
    return valsMasqueesChamp.containsKey(champ)
            &&valsMasqueesChamp.get(champ).contains(val);
  }
  boolean isMasqueeValNulleChamp(String champ) {
    return valNulleMasqueeChamp.contains(champ);
  }
  boolean isMasqueeValNbElements(int i) {
    return valsMasqueesNbElements[i];
  }
  void demasquerToutesValsChamp(String champ) {
    valsMasqueesChamp.get(champ).clear();
    valNulleMasqueeChamp.remove(champ);
  }
  void demasquerToutesValsNbElts(String champ) {
    for (int i = 0; i<valsMasqueesNbElements.length; i++) {
      valsMasqueesNbElements[i] = false;  
    }
  }
 void masquerAutresValsChamp(String typeunite, String champ, String val) {
    for(String v: vue.getValeursChamp(Unite.class, typeunite, champ)) 
      valsMasqueesChamp.get(champ).add(v);
     valsMasqueesChamp.get(champ).remove(val);
     valNulleMasqueeChamp.add(champ);
  }
 void masquerValChamp(boolean yes, String champ, String val) {
    if (yes) valsMasqueesChamp.get(champ).add(val);
    else valsMasqueesChamp.get(champ).remove(val);
  }
 void masquerValsNonNullesChamp(String typeunite, String champ) {
    for(String v: vue.getValeursChamp(Unite.class, typeunite, champ)) 
      valsMasqueesChamp.get(champ).add(v);
     valNulleMasqueeChamp.remove(champ);
  }
  void masquerValNulleChamp(boolean yes, String champ) {
    if (yes) valNulleMasqueeChamp.add(champ);
    else valNulleMasqueeChamp.remove(champ);
  }
  void masquerAutresValsNbElts(String champ, int i0) {
    for (int i = 0; i<valsMasqueesNbElements.length; i++) {
      valsMasqueesNbElements[i] = i!=i0;  
    }
  }
  void masquerValNbElements(boolean yes, int i) {
    valsMasqueesNbElements[i] = yes;
  }
  void init(String typeChaines, String typeUnites, int[] seuils)
          throws UnsupportedOperationException {
    seuilsValsNbElements = seuils;
    String[] champs = vue.getNomsChamps(Unite.class, typeUnites);
    valsMasqueesChamp.clear();
    for (String champ : champs)
      valsMasqueesChamp.put(champ, new HashSet<String>());
    valNulleMasqueeChamp.clear();
    valsMasqueesNbElements = new boolean[seuils.length+3];
    calculerChainesUnitesNonFiltrees(typeChaines, typeUnites);
    calculerDonnees(typeChaines, typeUnites);
  }
  private void calculerChainesUnitesNonFiltrees(String typeChaines,
          String typeUnites) throws UnsupportedOperationException {
    chainesUnitesNonFiltrees.clear();
    for (Schema chaine : vue.getSchemasAVoir(typeChaines))
      for (Element elt : chaine.getContenu()) {
        if (elt.getClass()==Unite.class&&elt.getType().equals(typeUnites)) {
          if (chainesUnitesNonFiltrees.containsKey(elt)) {
            throw new UnsupportedOperationException("L'unité "+vue.getIdElement(elt)
                    +" appartient à deux chaînes : "+vue.getIdElement(chaine)+" et "
                    +vue.getIdElement(chainesUnitesNonFiltrees.get((Unite) elt)));
          } else
            chainesUnitesNonFiltrees.put((Unite) elt, chaine);
        }
      }
  }
  void modifMasqueVals(String typeChaines, String typeUnites) {
    calculerDonnees(typeChaines, typeUnites);
  }
  private void filtrerListeUnites(String typeUnites, String typeChaines) {
    Unite[] unites = vue.getUnitesAVoir(typeUnites);
    Schema[] chaines = vue.getSchemasAVoir(typeChaines);
    unitesChaines.clear();
    for (Schema chaine : chaines) unitesChaines.put(chaine, new ArrayList<Unite>());
    ArrayList<Integer> indFiltrees = new ArrayList<Integer>();
    String[] champs = vue.getNomsChamps(Unite.class, typeUnites);
    boolean agarder;
    for (int i = 0; i<unites.length; i++) {
      agarder = true;
      for (String champ : champs) {
        String val = vue.getValeurChamp(unites[i], champ);
        if (val.isEmpty()) {
          if (valNulleMasqueeChamp.contains(champ)) {
            agarder = false;
            break;
          }
        } else {
          if (valsMasqueesChamp.get(champ).contains(val)) {
            agarder = false;
            break;
          }
        }
      }
      if (agarder) {
        indFiltrees.add(i);
        if (chainesUnitesNonFiltrees.containsKey(unites[i])) {
          Schema chaine = chainesUnitesNonFiltrees.get(unites[i]);
          unitesChaines.get(chaine).add(unites[i]);
        }
      }
    }
    ArrayList<Integer> indIndFiltrees = new ArrayList<Integer>();
    for(int k=0; k<indFiltrees.size(); k++) {
      Unite unit = unites[indFiltrees.get(k)];
      if(chainesUnitesNonFiltrees.containsKey(unit)) {
        if(!isMasqueeValNbElements(calculerValNbElements(
                unitesChaines.get(chainesUnitesNonFiltrees.get(unit)).size())))
          indIndFiltrees.add(k);
      } else {
        if(!isMasqueeValNbElements(0)) indIndFiltrees.add(k);
      }      
    }
    listeUnites = new Unite[indIndFiltrees.size()];
    for (int k = 0; k<listeUnites.length; k++) {
      listeUnites[k] = unites[indFiltrees.get(indIndFiltrees.get(k))];
    }
        for (Schema chaine : chaines) {
          int nb = unitesChaines.get(chaine).size();
          if(nb==0||isMasqueeValNbElements(calculerValNbElements(nb)))
            unitesChaines.remove(chaine);
        }
  }
  private void calculerDonnees(String typeChaines, String typeUnites) {
     filtrerListeUnites(typeUnites, typeChaines);
    Chainon[] chainons = new Chainon[listeUnites.length];
    for (int i = 0; i<listeUnites.length; i++) {
      Unite unit = listeUnites[i];
      chainons[i] = new Chainon(unit, vue.getTexteUnite(unit));
      if(chainesUnitesNonFiltrees.containsKey(unit)) {
        Schema chaine = chainesUnitesNonFiltrees.get(unit);
        chainons[i].chaine = chaine;
        chainons[i].nbEltsChaine = unitesChaines.get(chaine).size();
        chainons[i].idChaine = vue.getIdElement(chaine);
      } 
      chainonsUnites.put(listeUnites[i], chainons[i]);
    }
    HashMap<Schema, Integer> noChaine = new HashMap<Schema, Integer>();
    int no = 0;
    for (Schema chaine : vue.getSchemasAVoir(typeChaines)) {
      if(unitesChaines.containsKey(chaine)) {
        int nbElts = unitesChaines.get(chaine).size(); 
       noChaine.put(chaine, nbElts>1 ? ++no : 0);
    }
    }
    for (Unite unit : listeUnites) {
      Chainon chainon = chainonsUnites.get(unit);
      if (chainon.chaine!=null) chainon.noChaine = noChaine.get(chainon.chaine);
    }
    int[] noPar = vue.getCorpus().calculerNoParagraphe(listeUnites);
    panneauGraphique.setDonnees(chainons, noPar);
  }
  void initVide() {
    listeUnites = new Unite[0];
    chainesUnitesNonFiltrees.clear();
    seuilsValsNbElements = new int[0];
    chainonsUnites.clear();
    unitesChaines.clear();
    panneauGraphique.setDonnees(new Chainon[0], new int[0]);
    panneauGraphique.setCouleurs(new Color[0]);
    valNulleMasqueeChamp.clear();
    valsMasqueesChamp.clear();
    valsMasqueesNbElements = new boolean[0];
  }
  void setChampAffiche(String champ, HashMap<String, Color> couleursValeursChamp) {
    panneauGraphique.setCouleurs(colorerChamp(champ, couleursValeursChamp));
  }
  void setChampAfficheNbElts(Color[] couleurs) {
    panneauGraphique.setCouleurs(colorerNbElts(couleurs));
  }
  private Color[] colorerNbElts(Color[] couleurs) {
    Color[] couls = new Color[listeUnites.length];
    for (int i = 0; i<listeUnites.length; i++) {
      Chainon ch = chainonsUnites.get(listeUnites[i]);
      if (ch.chaine==null) couls[i] = couleurs[0];
      else {
        int nbElts = ch.nbEltsChaine;
        couls[i] = couleurs[calculerValNbElements(nbElts)];
      }
    }
    return couls;
  }
  private Color[] colorerChamp(String champ, HashMap<String, Color> couleursValeursChamp) {
    Color[] couls = new Color[listeUnites.length];
    for (int i = 0; i<listeUnites.length; i++) {
      Chainon ch = chainonsUnites.get(listeUnites[i]);
      couls[i] = couleursValeursChamp.get(vue.getValeurChamp(ch.unite, champ));
    }
    return couls;
  }
  private int calculerValNbElements(int nbElts) {
    return nbElts==1 ? 1 : nbElts<=seuilsValsNbElements[0] ? 2 : 
            nbElts<=seuilsValsNbElements[1] ? 3 : 4;
  }
}
