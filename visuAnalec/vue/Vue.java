/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.vue;

import JamaPlus.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.SimpleAttributeSet;
import visuAnalec.*;
import visuAnalec.util.*;
import visuAnalec.Message.*;
import visuAnalec.elements.*;
import visuAnalec.donnees.*;
import visuAnalec.donnees.Corpus.*;
import visuAnalec.texte.PanneauTexte;
import visuAnalec.texte.PanneauTexte.*;

/**
 *
 * @author Bernard
 */
public class Vue implements CorpusListener {
  public static interface VueListener extends EventListener {
    void traiterEvent(Message e);     // relaie les messages de Corpus + envoie ses propres messages (VueEvent)
  }

  public static class Adresse implements Serializable {
    ArrayList<Jalon> chemin;
    String prop;
    Adresse(Adresse adresse, String prop) {
      this.prop = prop;
      this.chemin = new ArrayList<Jalon>(adresse.chemin);
    }
    Adresse(Adresse adresse, Jalon jalon) {
      this.prop = null;
      ArrayList<Jalon> chemin1 = new ArrayList<Jalon>(adresse.chemin);
      chemin1.add(jalon);
      this.chemin = chemin1;
    }
    Adresse(Adresse adresse) {
      this.prop = adresse.prop;
      this.chemin = new ArrayList<Jalon>();
      for (Jalon jalon : adresse.chemin) this.chemin.add(new Jalon(jalon));
    }
    Adresse(Jalon jalon, String prop) {
      this.prop = prop;
      chemin = new ArrayList<Jalon>(Arrays.asList(jalon));
    }
    Adresse(Jalon jalon) {
      this.prop = null;
      chemin = new ArrayList<Jalon>(Arrays.asList(jalon));
    }
    int getLongueur() {
      return chemin.size();
    }
    Jalon getLastJalon() {
      return chemin.get(chemin.size()-1);
    }
    void renommer(Class<? extends Element> classe, String oldType, String newType) {
      for (Jalon jalon : chemin)
        if (jalon.classe==classe&&jalon.type.equals(oldType))
          jalon.type = newType;
    }
    boolean contient(Class<? extends Element> classe, String type) {
      for (Jalon jalon : chemin) {
        if (jalon.classe==classe&&jalon.type.equals(type)) return true;
      }
      return false;
    }
    @Override
    public String toString() {
      String nom = "";
      for (Jalon jalon : chemin) {
        nom += jalon;
      }
      nom += ": "+prop;
      return nom;
    }
  }

  public static class Jalon implements Serializable {
    public enum Origine {
      ELT_0, ELT_1, ELT_2, REL_SORTANTE, REL_ENTRANTE, SCH_CONTENANT
    }
    /**
     * L'origine d'un jalon indique d'où ce jalon provient :
     * ELT_0 est  un jalon de classe quelconque qui est toujours le premier jalon d'une adresse complète. 
     * ELT_1 et ELT_2 sont des jalons de classe quelconque qui proviennent d'un jalon précédent de classe Relation  
     * REL_S et REL_E sont des jalons de classe Relation qui proviennent d'un jalon précédent de classe quelconque
     * SCH_C est un jalon de classe Schema qui provient d'un jalon précédent de classe quelconque
     */
    public Origine origine;
    public Class<? extends Element> classe;
    public String type;
    Jalon(Origine origine, Class<? extends Element> classe, String type) {
      this.classe = classe;
      this.origine = origine;
      this.type = type;
    }
    Jalon(Jalon jalon) {
      this.classe = jalon.classe;
      this.origine = jalon.origine;
      this.type = jalon.type;
    }
    @Override
    public String toString() {
      switch (origine) {
        case SCH_CONTENANT:
          return "{"+type;
        case REL_ENTRANTE:
          return "<"+type+"<";
        case REL_SORTANTE:
          return ">"+type+">";
        case ELT_0:
          return type;
        case ELT_1:
          return type;
        case ELT_2:
          return type;
        default:
          return "ERREUR!!";
      }
    }
  }
  /** 
   * Champs de la classe Vue :
   * Données spécifiant la vue: 
   *   - typesA voir, champs, adresses, modifiables
   *   - champsId
   * (ce sont les seuls champs à sauvegarder et à recharger) 
   * Le champ unitesAVoir est un raccourci pour éviter de retrier chaque fois les données correspondantes de Corpus
   */
  private ArrayList<VueListener> listeCorrespondants = new ArrayList<VueListener>();
  private Corpus corpus;
  private boolean modifiee = false;
  ;
  // types d'éléments à voir pour chaque classe
  private HashMap<Class<? extends Element>, String[]> typesAVoir =
          new HashMap<Class<? extends Element>, String[]>();
  // champs en ordre (niveau et position) pour chaque type d'éléments à voir pour chaque classe 
  private HashMap<Class<? extends Element>, HashMap<String, String[][]>> champspos =
          new HashMap<Class<? extends Element>, HashMap<String, String[][]>>();
  // adresse pour chaque champ pour chaque  type d'élément à voir pour chaque classe
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Adresse>>> adresses =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Adresse>>>();
  // modifiabilité pour chaque champ pour chaque  type d'élément à voir pour chaque classe
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> modifiables =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>>();
  // champ choisi comme identifiant (s'il existe) pour chaque type de chaque classe
  private HashMap<Class<? extends Element>, HashMap<String, String>> champsId =
          new HashMap<Class<? extends Element>, HashMap<String, String>>();
// style pour tout le texte
  private SimpleAttributeSet styleTexte;
  // style (s'il existe) pour chaque type de chaque classe
  private HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> stylesElements =
          new HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>>();
  // style (s'il existe) pour chaque valeur de chaque champ de chaque type de chaque classe
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> stylesValeursElements =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>>();
  // unites par type d'unites à voir
  private HashMap<String, Unite[]> unitesAVoir = new HashMap<String, Unite[]>();
  /* Constructeur de la classe Vue */
  @SuppressWarnings("unchecked")
  public Vue(Corpus corpus) {
    this.corpus = corpus;
    corpus.addEventListener(this);
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      typesAVoir.put(classe, new String[0]);
      champspos.put(classe, new HashMap<String, String[][]>());
      adresses.put(classe, new HashMap<String, HashMap<String, Adresse>>());
      modifiables.put(classe, new HashMap<String, HashMap<String, Boolean>>());
      champsId.put(classe, new HashMap<String, String>());
      stylesElements.put(classe, new HashMap<String, SimpleAttributeSet>());
      stylesValeursElements.put(classe, new HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>());
    }
    this.styleTexte = new SimpleAttributeSet(PanneauTexte.STANDARD_DEFAUT);
  }
  public void addEventListener(VueListener correspondant) {
    listeCorrespondants.add(correspondant);
  }
  public void removeEventListener(VueListener correspondant) {
    listeCorrespondants.remove(correspondant);
  }
  private void fireMessage(Message evt) {
    for (Object corresp : listeCorrespondants.toArray()) {
      ((VueListener) corresp).traiterEvent(evt);
    }
  }
  public boolean isModifiee() {
    return modifiee;
  }
  public void ecrireJava(ObjectOutputStream oos) throws IOException {
    oos.writeObject(typesAVoir);
    oos.writeObject(champspos);
    oos.writeObject(adresses);
    oos.writeObject(modifiables);
    oos.writeObject(champsId);
    oos.writeObject(styleTexte);
    oos.writeObject(stylesElements);
    oos.writeObject(stylesValeursElements);
    modifiee = false;
  }
  @SuppressWarnings("unchecked")
  public boolean lireJava(ObjectInputStream ois) throws IOException, ClassNotFoundException, ClassCastException {
    HashMap<Class<? extends Element>, String[]> typesAVoirLus =
            (HashMap<Class<? extends Element>, String[]>) ois.readObject();
    HashMap<Class<? extends Element>, HashMap<String, String[][]>> champsposLus =
            (HashMap<Class<? extends Element>, HashMap<String, String[][]>>) ois.readObject();
    HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Adresse>>> adressesLues =
            (HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Adresse>>>) ois.readObject();
    HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> modifablesLus =
            (HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>>) ois.readObject();
    HashMap<Class<? extends Element>, HashMap<String, String>> champsIdLus =
            (HashMap<Class<? extends Element>, HashMap<String, String>>) ois.readObject();
    SimpleAttributeSet styleTexteLu = (SimpleAttributeSet) ois.readObject();
    HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> stylesElementsLus =
            (HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>>) ois.readObject();
    HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> stylesValeursElementsLus =
            (HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>>) ois.readObject();
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      for (String type : typesAVoirLus.get(classe)) {
        if (!corpus.getStructure().getTypes(classe).contains(type))
          return false;
      }
    }
    // En prévision d'une compatibilité ascendante
    boolean suite = false;
    try {
      suite = ois.readBoolean();
    } catch (EOFException eOFException) {
    }
    if (suite) {
      System.out.println("Il y a d'autres objets dans le .ecv");
    }
    typesAVoir = typesAVoirLus;
    champspos = champsposLus;
    adresses = adressesLues;
    modifiables = modifablesLus;
    champsId = champsIdLus;
    styleTexte = styleTexteLu;
    stylesElements = stylesElementsLus;
    stylesValeursElements = stylesValeursElementsLus;
    unitesAVoir.clear();
    modifiee = false;
    for (String type : typesAVoir.get(Unite.class)) majUnitesAVoir(type);
    fireMessage(new VueEvent(this, TypeModifVue.NEW_VUE));
    return true;
  }
  public void traiterEvent(Message evt) { // Messages de corpus
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        initVueParDefaut();
        fireMessage(evt);
        return;
      case NEW_CORPUS:
        initVueParDefaut();
        fireMessage(evt);
        return;
      case MODIF_TEXTE:
        fireMessage(evt);
        return;
      case MODIF_ELEMENT:
        ElementEvent evtE = (ElementEvent) evt;
        switch (evtE.getModif()) {
          case AJOUT_UNITE:
            if (!unitesAVoir.containsKey(evtE.getElement().getType())) return;
            majUnitesAVoir(evtE.getElement().getType());
            fireMessage(evt);
            return;
          case SUP_UNITE:
            if (!unitesAVoir.containsKey(evtE.getElement().getType())) return;
            majUnitesAVoir(evtE.getElement().getType());
            fireMessage(evt);
            return;
          case AJOUT_RELATION:
          case AJOUT_SCHEMA:
          case SUP_RELATION:
          case SUP_SCHEMA:
          case BORNES_UNITE:
          case MODIF_SCHEMA:
          case MODIF_VALEUR:
            fireMessage(evt);
            return;
          default:
            throw new UnsupportedOperationException("Cas "+evtE.getModif()+" oublié dans un switch");
        }
      case MODIF_STRUCTURE:
        StructureEvent evtS = (StructureEvent) evt;
        switch (evtS.getModif()) {
          case FUSION_TYPE:
            fusionnerType(evtS.getClasse(), evtS.getArg(0), evtS.getArg(1));
            fireMessage(evt);
            return;
          case RENOM_TYPE:
            renommerType(evtS.getClasse(), evtS.getArg(0), evtS.getArg(1));
            fireMessage(evt);
            return;
          case AJOUT_TYPE:
            ajouterType(evtS.getClasse(), evtS.getArg());
            fireMessage(evt);
            return;
          case AJOUT_TYPE_ET_ELEMENTS:
            ajouterType(evtS.getClasse(), evtS.getArg());
            if (evtS.getClasse()==Unite.class&&unitesAVoir.containsKey(evtS.getArg()))
              majUnitesAVoir(evtS.getArg());
            fireMessage(evt);
            return;
          case SUPPR_TYPE:
            supprimerType(evtS.getClasse(), evtS.getArg());
            fireMessage(evt);
            return;
          case AJOUT_PROP:
            ajouterProp(evtS.getClasse(), evtS.getArg(0), evtS.getArg(1));
            fireMessage(evt);
            return;
          case AJOUT_FORME_UNITE:
            ajouterFormeUnite(evtS.getArg(0));
            fireMessage(evt);
            return;
          case FUSION_PROP:
            fusionnerProp(evtS.getClasse(), evtS.getArg(0), evtS.getArg(1), evtS.getArg(2));
            fireMessage(evt);
            return;
          case RENOM_PROP:
            renommerProp(evtS.getClasse(), evtS.getArg(0), evtS.getArg(1), evtS.getArg(2));
            fireMessage(evt);
            return;
          case SUPPR_PROP:
            supprimerProp(evtS.getClasse(), evtS.getArg(0), evtS.getArg(1));
            fireMessage(evt);
            return;
          case RENOM_VALEUR:
          case FUSION_VALEUR:
          case SUPPR_VALEUR:
          case AJOUT_VALEUR:
            fireMessage(evt);
            return;
          case VALEUR_PAR_DEFAUT:
            fireMessage(evt);
            return;
          default:
            throw new UnsupportedOperationException("Cas "+evtS.getModif()+" oublié dans un switch");
        }
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }
  }
  @SuppressWarnings("unchecked")
  private void initVueParDefaut() {
    unitesAVoir.clear();
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      String[] types = getTypes(classe);
      Arrays.sort(types);
      typesAVoir.put(classe, types);
      champspos.get(classe).clear();
      adresses.get(classe).clear();
      champsId.get(classe).clear();
      styleTexte = new SimpleAttributeSet(PanneauTexte.STANDARD_DEFAUT);
      stylesElements.get(classe).clear();
      stylesValeursElements.get(classe).clear();
      for (String type : typesAVoir.get(classe)) {
        Jalon jalon0 = new Jalon(Jalon.Origine.ELT_0, classe, type);
        String[] chps = getNomsProps(new Adresse(jalon0));
        champspos.get(classe).put(type, new String[1][]);
        champspos.get(classe).get(type)[0] = chps;
        adresses.get(classe).put(type, new HashMap<String, Adresse>());
        modifiables.get(classe).put(type, new HashMap<String, Boolean>());
        for (String chp : chps) {
          adresses.get(classe).get(type).put(chp, new Adresse(jalon0, chp));
          modifiables.get(classe).get(type).put(chp, true);
        }
        if (classe==Unite.class) majUnitesAVoir(type);
      }
    }
    modifiee = false;
    fireMessage(new VueEvent(this, TypeModifVue.VUE_DEFAUT));

  }
  private void majUnitesAVoir(String typeUnite) {
    Unite[] unts = corpus.getUnites(typeUnite).toArray(new Unite[0]);
    Arrays.sort(unts);
    unitesAVoir.put(typeUnite, unts);
  }
  public void retablirVueParDefaut() {
    initVueParDefaut();
  }
  public void setStyleTexte(SimpleAttributeSet att) {
    styleTexte = att;
  }
  public SimpleAttributeSet getStyleTexte() {
    return styleTexte;
  }
  public void modifierVueParSaisie(
          HashMap<Class<? extends Element>, HashMap<String, Boolean>> newIsTypesAVoir,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> newAdressesAVoir,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> newChampsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, String>> newChampsId,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> newModifables,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> newPositionsChamps,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> newNiveauxChamps,
          HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> newStylesElements,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> newStylesValeursElements) {
    setTypesAVoir(newIsTypesAVoir);
    setChampsAdresses(newChampsAVoir, newAdressesAVoir, newPositionsChamps, newNiveauxChamps);
    setChampsModifiables(newChampsAVoir, newModifables);
    setChampsId(newChampsId);
    setStylesElements(newStylesElements);
    setStylesValeursElements(newStylesValeursElements);
    unitesAVoir.clear();
    for (String typeUnite : typesAVoir.get(Unite.class))
      majUnitesAVoir(typeUnite);
    modifiee = true;
    fireMessage(new VueEvent(this, TypeModifVue.EXTRACTION));
  }
  @SuppressWarnings("unchecked")
  private void setTypesAVoir(HashMap<Class<? extends Element>, HashMap<String, Boolean>> isAVoir) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      ArrayList<String> types = new ArrayList<String>();
      for (String type : getTypes(classe)) if (isAVoir.get(classe).get(type))
          types.add(type);
      typesAVoir.put(classe, types.toArray(new String[0]));
    }
  }
  @SuppressWarnings("unchecked")
  private void setChampsAdresses(
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> positionsChamps,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> niveauxChamps) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      adresses.get(classe).clear();
      for (int i = 0; i<typesAVoir.get(classe).length; i++) {
        String type = typesAVoir.get(classe)[i];
        ArrayList<String> chps = champsAVoir.get(classe).get(type);
        adresses.get(classe).put(type, new HashMap<String, Adresse>());
        for (int j = 0; j<chps.size(); j++)
          adresses.get(classe).get(type).put(chps.get(j), adressesAVoir.get(classe).get(type).get(j));
        champspos.get(classe).put(type, ordonnerChamps(chps, niveauxChamps.get(classe).get(type),
                positionsChamps.get(classe).get(type)));
      }
    }
  }
  private String[][] insererChamp(String[][] chpsenordre, String newchp) {
    if (chpsenordre.length==0) {
      String[][] chps = new String[1][1];
      chps[0][0] = newchp;
      return chps;
    }
    String[] chs1 = new String[chpsenordre[0].length+1];
    chs1[0] = newchp;
    System.arraycopy(chpsenordre[0], 0, chs1, 1, chpsenordre[0].length);
    chpsenordre[0] = chs1;
    return chpsenordre;
  }
  private String[][] ordonnerChamps(ArrayList<String> chps, HashMap<String, Integer> nivx, HashMap<String, Integer> poss) {
    HashMap<Integer, HashMap<Integer, ArrayList<String>>> chpstries =
            new HashMap<Integer, HashMap<Integer, ArrayList<String>>>();
    for (String ch : chps) {
      int niv = nivx.get(ch);
      if (!chpstries.containsKey(niv))
        chpstries.put(niv, new HashMap<Integer, ArrayList<String>>());
      int pos = poss.get(ch);
      if (!chpstries.get(niv).containsKey(pos))
        chpstries.get(niv).put(pos, new ArrayList<String>());
      chpstries.get(niv).get(pos).add(ch);
    }
    Integer[] niveaux = chpstries.keySet().toArray(new Integer[0]);
    Arrays.sort(niveaux);
    String[][] chpsenordre = new String[niveaux.length][];
    int niveauenordre = 0;
    for (int niv : niveaux) {
      Integer[] positions = chpstries.get(niv).keySet().toArray(new Integer[0]);
      Arrays.sort(positions);
      int nbch = 0;
      for (int pos : positions) nbch += chpstries.get(niv).get(pos).size();
      chpsenordre[niveauenordre] = new String[nbch];
      int positionenordre = 0;
      for (int pos : positions) {
        String[] chs = chpstries.get(niv).get(pos).toArray(new String[0]);
        Arrays.sort(chs);
        for (String ch : chs)
          chpsenordre[niveauenordre][positionenordre++] = ch;
      }
      niveauenordre++;
    }
    return chpsenordre;
  }
  @SuppressWarnings("unchecked")
  private void setChampsModifiables(HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> newChampsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> newModifables) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      modifiables.get(classe).clear();
      for (int i = 0; i<typesAVoir.get(classe).length; i++) {
        String type = typesAVoir.get(classe)[i];
        modifiables.get(classe).put(type, new HashMap<String, Boolean>());
        for (String chp : newChampsAVoir.get(classe).get(type))
          modifiables.get(classe).get(type).put(chp, newModifables.get(classe).get(type).get(chp));
      }
    }
  }
  @SuppressWarnings("unchecked")
  private void setChampsId(HashMap<Class<? extends Element>, HashMap<String, String>> newChampsId) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      champsId.get(classe).clear();
      champsId.get(classe).putAll(newChampsId.get(classe));
    }
  }
  @SuppressWarnings("unchecked")
  private void setStylesElements(HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> newStylesElements) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      stylesElements.get(classe).clear();
      stylesElements.get(classe).putAll(newStylesElements.get(classe));
    }
  }
  @SuppressWarnings("unchecked")
  private void setStylesValeursElements(HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> newStylesValeursElements) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      stylesValeursElements.get(classe).clear();
      stylesValeursElements.get(classe).putAll(newStylesValeursElements.get(classe));
    }
  }
  public Corpus getCorpus() {
    return corpus;
  }
  public String[] getTypes(Class<? extends Element> classe) {
    String[] types = corpus.getStructure().getTypes(classe).toArray(new String[0]);
    Arrays.sort(types);
    return types;
  }
  @SuppressWarnings("unchecked")
  public HashMap<Class<? extends Element>, HashMap<String, Boolean>> isTypesAVoir() {
    HashMap<Class<? extends Element>, HashMap<String, Boolean>> isAVoir =
            new HashMap<Class<? extends Element>, HashMap<String, Boolean>>();
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      isAVoir.put(classe, new HashMap<String, Boolean>());
      for (String type : getTypes(classe)) isAVoir.get(classe).put(type, false);
      for (String type : typesAVoir.get(classe))
        isAVoir.get(classe).put(type, true);
    }
    return isAVoir;
  }
  String[] getTypesAVoir(Class<? extends Element> classe) {
    return typesAVoir.get(classe);
  }
  public String[] getTypesUnitesAVoir() {
    return getTypesAVoir(Unite.class);
  }
  public String[] getTypesRelationsAVoir() {
    return getTypesAVoir(Relation.class);
  }
  public String[] getTypesSchemasAVoir() {
    return getTypesAVoir(Schema.class);
  }
  public String getChampId(Class<? extends Element> classe, String type) {
    return champsId.get(classe).get(type);
  }
  public SimpleAttributeSet getStyleElements(Class<? extends Element> classe, String type) {
    return stylesElements.get(classe).get(type);
  }
  public HashMap<String, HashMap<String, SimpleAttributeSet>> getStyleValeursElements(
          Class<? extends Element> classe, String type) {
    return stylesValeursElements.get(classe).get(type);
  }
  public String[][] getChamps(Class<? extends Element> classe, String type) {
    if (champspos.get(classe).containsKey(type)) {
      return champspos.get(classe).get(type);
    } else {
      return new String[0][];
    }
  }
  public String[] getNomsChamps(Class<? extends Element> classe, String type) {
    return getListeChamps(classe, type).toArray(new String[0]);
  }
  ArrayList<String> getListeChamps(Class<? extends Element> classe, String type) {
    // utilisé aussi par VisuVue
    ArrayList<String> lchps = new ArrayList<String>();
    if (champspos.get(classe).containsKey(type))
      for (String[] champsniveau : champspos.get(classe).get(type))
        lchps.addAll(Arrays.asList(champsniveau));
    return lchps;
  }
  static String trouveNomChampParDefaut(Adresse ad, ArrayList<String> chps) {
    String nom;
    if (ad.chemin.size()==1) {
      nom = ad.prop;
      while (chps.contains(nom)) nom += "-0";
      return nom;
    }
    for (int i = 1; true; i++) {
      nom = ad.prop+"-"+i;
      if (!chps.contains(nom)) {
        return nom;
      }
    }
  }
  String[] getNomsProps(Adresse adresse) {
    String[] props;
    Jalon jalon = adresse.getLastJalon();
    props = corpus.getStructure().getNomsProps(jalon.classe, jalon.type).toArray(new String[0]);
    Arrays.sort(props);
    return props;
  }
  String[] getValeursProp(Adresse adresse) {
    String[] vals;
    Jalon jalon = adresse.getLastJalon();
    vals = corpus.getStructure().getValeursProp(jalon.classe, jalon.type, adresse.prop).toArray(new String[0]);
    Arrays.sort(vals);
    return vals;
  }
  @SuppressWarnings("unchecked")
  Jalon[] getNextJalonsPotentiels(Adresse adresse) {
    ArrayList<Jalon> nextJalons = new ArrayList<Jalon>();
    Jalon jalon = adresse.getLastJalon();
    switch (jalon.origine) {
      case REL_ENTRANTE:
        for (Class<? extends Element> cl : new Class[]{Unite.class, Relation.class, Schema.class}) {
          for (String typeRel : getTypes(cl)) {
            nextJalons.add(new Jalon(Jalon.Origine.ELT_1, cl, typeRel));
          }
        }
        break;
      case REL_SORTANTE:
        for (Class<? extends Element> cl : new Class[]{Unite.class, Relation.class, Schema.class}) {
          for (String typeRel : getTypes(cl)) {
            nextJalons.add(new Jalon(Jalon.Origine.ELT_2, cl, typeRel));
          }
        }
        break;
      default:
        for (String typeRel : getTypes(Relation.class)) {
          nextJalons.add(new Jalon(Jalon.Origine.REL_SORTANTE, Relation.class, typeRel));
        }
        for (String typeRel : getTypes(Relation.class)) {
          nextJalons.add(new Jalon(Jalon.Origine.REL_ENTRANTE, Relation.class, typeRel));
        }
        for (String typeSch : getTypes(Schema.class)) {
          nextJalons.add(new Jalon(Jalon.Origine.SCH_CONTENANT, Schema.class, typeSch));
        }
        break;
    }
    return nextJalons.toArray(new Jalon[0]);
  }
  Adresse getAdresseChamps(Class<? extends Element> classe, String type, String nomChamp) {
    if (adresses.get(classe).containsKey(type)) {
      return adresses.get(classe).get(type).get(nomChamp);
    } else {
      return null;
    }
  }
  public boolean isChampModifiable(Class<? extends Element> classe, String type, String champ) {
    return modifiables.get(classe).get(type).get(champ);
  }
  public HashMap<String, Integer> getPositionsChampsAVoir(Class<? extends Element> classe, String type) {
    HashMap<String, Integer> positions = new HashMap<String, Integer>();
    if (champspos.get(classe).containsKey(type)) {
      for (String[] champsniveau : champspos.get(classe).get(type)) {
        int i = 0;
        for (String ch : champsniveau) positions.put(ch, i++);
      }
    }
    return positions;
  }
  public HashMap<String, Integer> getNiveauxChampsAVoir(Class<? extends Element> classe, String type) {
    HashMap<String, Integer> niveaux = new HashMap<String, Integer>();
    if (champspos.get(classe).containsKey(type)) {
      int i = 0;
      for (String[] champsniveau : champspos.get(classe).get(type)) {
        for (String ch : champsniveau) niveaux.put(ch, i);
        i++;
      }
    }
    return niveaux;
  }
  public String[] getValeursChamp(Class<? extends Element> classe, String type, String champ) {
    Adresse adresse = getAdresseChamps(classe, type, champ);
    if (adresse==null) {
      return new String[0];
    }
    return getValeursProp(adresse);
  }
  public String getValeurChamp(Element element, String champ) {
    Adresse adresse = getAdresseChamps(element.getClass(), element.getType(), champ);
    if (adresse==null) {
      return "";
    }
    Element elt = element;
    for (Jalon jalon : adresse.chemin) {
      if ((elt = elt.getElementSuivant(jalon))==null) return "";
    }
    return elt.getProp(adresse.prop);
  }
  public boolean setValeurChamp(Element element, String champ, String newval) {
    Adresse adresse = getAdresseChamps(element.getClass(), element.getType(), champ);
    Element elt = element;
    for (Jalon jalon : adresse.chemin) {
      elt = elt.getElementSuivant(jalon);
      if (elt==null) return false;
    }
    corpus.setValeurPropElement(elt, adresse.prop, newval);
    return true;
  }
  public void copierValeurs(Element eltSource, Element eltCible) {
    Class<? extends Element> classe = eltSource.getClass();
    if (eltCible.getClass()!=classe) return;
    String type = eltSource.getType();
    if (!eltCible.getType().equals(type)) return;
    for (String champ : getListeChamps(classe, type))
      if (isChampModifiable(classe, type, champ))
        setValeurChamp(eltCible, champ, getValeurChamp(eltSource, champ));
  }
  private void ajouterType(Class<? extends Element> classe, String newType) {

//      ArrayList<String> typs = new ArrayList<String>(Arrays.asList(typesUnitesAVoir));
//      typs.add(newType);
//      typesUnitesAVoir = typs.toArray(new String[0]);
//      Arrays.sort(typesUnitesAVoir);
    typesAVoir.put(classe, Tableaux.insererString(typesAVoir.get(classe), newType));
    champspos.get(classe).put(newType, new String[0][]);
    adresses.get(classe).put(newType, new HashMap<String, Adresse>());
    if (classe==Unite.class) unitesAVoir.put(newType, new Unite[0]);
    modifiables.get(classe).put(newType, new HashMap<String, Boolean>());
  }
  private void renommerType(Class<? extends Element> classe, String oldType, String newType) {
    for (Class<? extends Element> cl : adresses.keySet())
      for (String type : adresses.get(cl).keySet())
        for (String chp : adresses.get(cl).get(type).keySet()) {
          adresses.get(cl).get(type).get(chp).renommer(classe, oldType, newType);
        }
    int ind = Arrays.binarySearch(typesAVoir.get(classe), oldType);
    if (ind>=0) {
      typesAVoir.get(classe)[ind] = newType;
      Arrays.sort(typesAVoir.get(classe));
      champspos.get(classe).put(newType, champspos.get(classe).get(oldType));
      champspos.get(classe).remove(oldType);
      adresses.get(classe).put(newType, adresses.get(classe).get(oldType));
      adresses.get(classe).remove(oldType);
      if (champsId.get(classe).containsKey(oldType)) {
        champsId.get(classe).put(newType, champsId.get(classe).get(oldType));
        champsId.get(classe).remove(oldType);
      }
      modifiables.get(classe).put(newType, modifiables.get(classe).get(oldType));
      if (classe==Unite.class) {
        unitesAVoir.put(newType, unitesAVoir.get(oldType));
        unitesAVoir.remove(oldType);
      }
      if (stylesElements.get(classe).containsKey(oldType)) {
        stylesElements.get(classe).put(newType, stylesElements.get(classe).get(oldType));
        stylesElements.get(classe).remove(oldType);
      }
      if (stylesValeursElements.get(classe).containsKey(oldType)) {
        stylesValeursElements.get(classe).put(newType, stylesValeursElements.get(classe).get(oldType));
        stylesValeursElements.get(classe).remove(oldType);
      }
    }
  }
  private void fusionnerType(Class<? extends Element> classe, String oldType, String newType) {
    int indold = Arrays.binarySearch(typesAVoir.get(classe), oldType);
    if (indold>=0) {
      int indnew = Arrays.binarySearch(typesAVoir.get(classe), newType);
      if (indnew>=0) {
        ArrayList<String> chpsnewtype = getListeChamps(classe, newType);
        HashMap<String, Integer> nivxnewtype = getNiveauxChampsAVoir(classe, newType);
        HashMap<String, Integer> possnewtype = getPositionsChampsAVoir(classe, newType);
        for (String chpoldtype : getListeChamps(classe, oldType)) {
          Adresse adroldtype = adresses.get(classe).get(oldType).get(chpoldtype);
          String nomchamp = trouveNomChampParDefaut(adroldtype, chpsnewtype);
          adresses.get(classe).get(newType).put(nomchamp, adroldtype);
          chpsnewtype.add(nomchamp);
          nivxnewtype.put(nomchamp, getNiveauxChampsAVoir(classe, oldType).get(chpoldtype));
          possnewtype.put(nomchamp, getPositionsChampsAVoir(classe, oldType).get(chpoldtype));
          modifiables.get(classe).get(newType).put(nomchamp, modifiables.get(classe).get(oldType).get(chpoldtype));
          if (stylesValeursElements.get(classe).containsKey(oldType)&&stylesValeursElements.get(classe).get(oldType).containsKey(chpoldtype)) {
            if (!stylesValeursElements.get(classe).containsKey(newType))
              stylesValeursElements.get(classe).put(newType, new HashMap<String, HashMap<String, SimpleAttributeSet>>());
            stylesValeursElements.get(classe).get(newType).put(nomchamp,
                    stylesValeursElements.get(classe).get(oldType).get(chpoldtype));
          }
        }
        champspos.get(classe).put(newType, ordonnerChamps(chpsnewtype, nivxnewtype, possnewtype));
      }
      typesAVoir.put(classe, Tableaux.oterString(typesAVoir.get(classe), indold));
      champspos.get(classe).remove(oldType);
      adresses.get(classe).remove(oldType);
      champsId.get(classe).remove(oldType);
      modifiables.get(classe).remove(oldType);
      stylesElements.get(classe).remove(oldType);
      stylesValeursElements.get(classe).remove(oldType);
      if (classe==Unite.class) {
        majUnitesAVoir(newType);
        unitesAVoir.remove(oldType);
      }
    }
    for (Class<? extends Element> cl : typesAVoir.keySet())
      for (String typ : typesAVoir.get(cl)) {
        ArrayList<String> chpsASup = new ArrayList<String>();
        ArrayList<String> chps = getListeChamps(cl, typ);
        HashMap<String, Integer> nivx = getNiveauxChampsAVoir(cl, typ);
        HashMap<String, Integer> poss = getPositionsChampsAVoir(cl, typ);
        for (String ch : chps) {
          Adresse ad = adresses.get(cl).get(typ).get(ch);
          if (ad.contient(classe, oldType)) {
            Adresse ad1 = new Adresse(ad);
            ad1.renommer(classe, oldType, newType);
            boolean dejala = false;
            for (String ch1 : chps)
              if (adresses.get(cl).get(typ).get(ch1).toString().equals(ad1.toString())) {
                dejala = true;
                break;
              }
            if (!dejala) adresses.get(cl).get(typ).put(ch, ad1);
            else {
              chpsASup.add(ch);
              if (ch.equals(champsId.get(cl).get(typ)))
                champsId.get(cl).remove(typ);
              modifiables.get(cl).get(typ).remove(ch);
              if (stylesValeursElements.get(cl).containsKey(typ))
                stylesValeursElements.get(cl).get(typ).remove(ch);
              nivx.remove(ch);
              poss.remove(ch);
            }
          }
        }
        chps.removeAll(chpsASup);
        for (String ch : chpsASup) adresses.get(cl).get(typ).remove(ch);
        champspos.get(cl).put(typ, ordonnerChamps(chps, nivx, poss));
      }
  }
  private void supprimerType(Class<? extends Element> classe, String type) {
    int ind = Arrays.binarySearch(typesAVoir.get(classe), type);
    if (ind>=0) {
      typesAVoir.put(classe, Tableaux.oterString(typesAVoir.get(classe), ind));
      champspos.get(classe).remove(type);
      adresses.get(classe).remove(type);
      if (classe==Unite.class) unitesAVoir.remove(type);
    }
    champsId.get(classe).remove(type);
    modifiables.get(classe).remove(type);
    stylesElements.get(classe).remove(type);
    stylesValeursElements.get(classe).remove(type);
    for (Class<? extends Element> cl : typesAVoir.keySet())
      for (String typ : typesAVoir.get(cl)) {
        ArrayList<String> chpsASup = new ArrayList<String>();
        ArrayList<String> chps = getListeChamps(cl, typ);
        HashMap<String, Integer> nivx = getNiveauxChampsAVoir(cl, typ);
        HashMap<String, Integer> poss = getPositionsChampsAVoir(cl, typ);
        for (String ch : chps)
          if (adresses.get(cl).get(typ).get(ch).contient(classe, type)) {
            chpsASup.add(ch);
            adresses.get(cl).get(typ).remove(ch);
            if (ch.equals(champsId.get(cl).get(typ)))
              champsId.get(cl).remove(typ);
            modifiables.get(cl).get(typ).remove(ch);
            if (stylesValeursElements.get(cl).containsKey(typ))
              stylesValeursElements.get(cl).get(typ).remove(ch);
            nivx.remove(ch);
            poss.remove(ch);
          }
        chps.removeAll(chpsASup);
        champspos.get(cl).put(typ, ordonnerChamps(chps, nivx, poss));
      }
  }
  private void ajouterProp(Class<? extends Element> classe, String type, String prop) {
    if (Arrays.binarySearch(typesAVoir.get(classe), type)>=0) {
      Adresse ad = new Adresse(new Jalon(Jalon.Origine.ELT_0, classe, type), prop);
      String newchp = trouveNomChampParDefaut(ad, getListeChamps(classe, type));
      champspos.get(classe).put(type, insererChamp(champspos.get(classe).get(type), newchp));
      adresses.get(classe).get(type).put(newchp, ad);
      modifiables.get(classe).get(type).put(newchp, true);
    }
  }
  private void ajouterFormeUnite(String type) {
    if (Arrays.binarySearch(typesAVoir.get(Unite.class), type)>=0) {
      Adresse ad = new Adresse(new Jalon(Jalon.Origine.ELT_0, Unite.class, type), Structure.FORME);
      champspos.get(Unite.class).put(type, insererChamp(champspos.get(Unite.class).get(type), Structure.FORME));
      adresses.get(Unite.class).get(type).put(Structure.FORME, ad);
      modifiables.get(Unite.class).get(type).put(Structure.FORME, false);
    }
  }
  private void renommerProp(Class<? extends Element> classe, String type, String oldNom, String newNom) {
    // on ne change que les adresses : on ne renomme les champs que dans certains cas (cf. renommerChamp appelé à la fin)
    for (Class<? extends Element> cl : adresses.keySet())
      for (String typ : adresses.get(cl).keySet())
        for (String chp : adresses.get(cl).get(typ).keySet()) {
          if (adresses.get(cl).get(typ).get(chp).getLastJalon().classe==classe
                  &&type.equals(adresses.get(cl).get(typ).get(chp).getLastJalon().type)
                  &&oldNom.equals(adresses.get(cl).get(typ).get(chp).prop)) {
            adresses.get(cl).get(typ).get(chp).prop = newNom;
          }
        }
    renommerChamp(classe, type, oldNom, newNom);
  }
  private void renommerChamp(Class<? extends Element> classe, String type, String oldNom, String newNom) {
    // renomme aussi le champ quand la correspondance prop<=>champ est directe (notamment dans la vue par défaut)
    if (!champspos.get(classe).containsKey(type))
      throw new IllegalArgumentException();
    String[][] chps = champspos.get(classe).get(type);
    if (!adresses.get(classe).get(type).containsKey(oldNom))
      throw new IllegalArgumentException();
    if (adresses.get(classe).get(type).get(oldNom).getLongueur()>1) return;
    if (!adresses.get(classe).get(type).get(oldNom).prop.equals(oldNom)) return;
    boolean fait = false;
    for (int niv = 0; niv<chps.length; niv++) {
      for (int pos = 0; pos<chps[niv].length; pos++)
        if (chps[niv][pos].equals(oldNom)) {
          chps[niv][pos] = newNom;
          fait = true;
          break;
        }
      if (fait) break;
    }
    if (!fait) throw new IllegalArgumentException();
    adresses.get(classe).get(type).put(newNom, adresses.get(classe).get(type).get(oldNom));
    adresses.get(classe).get(type).remove(oldNom);
    modifiables.get(classe).get(type).put(newNom, modifiables.get(classe).get(type).get(oldNom));
    modifiables.get(classe).get(type).remove(oldNom);
    if (champsId.get(classe).containsKey(type)&&champsId.get(classe).get(type).equals(oldNom))
      champsId.get(classe).put(type, newNom);
    if (stylesValeursElements.get(classe).containsKey(type)
            &&stylesValeursElements.get(classe).get(type).containsKey(oldNom)) {
      stylesValeursElements.get(classe).get(type).put(newNom,
              stylesValeursElements.get(classe).get(type).get(oldNom));
      stylesValeursElements.get(classe).get(type).remove(oldNom);
    }
  }
  private void fusionnerProp(Class<? extends Element> classe, String type, String oldNom, String newNom) {
    // pour le moment, on se contente de supprimer tous les champs qui utilisent l'ancienne prop
    // c'est un peu grossier...
    supprimerProp(classe, type, oldNom);
  }
  private void supprimerProp(Class<? extends Element> classe, String type, String prop) {
    // on parcourt tous les champs et les adresses correspondantes
    for (Class<? extends Element> cl : typesAVoir.keySet())
      for (String typ : typesAVoir.get(cl)) {
        ArrayList<String> chpsASup = new ArrayList<String>();
        ArrayList<String> chps = getListeChamps(cl, typ);
        HashMap<String, Integer> nivx = getNiveauxChampsAVoir(cl, typ);
        HashMap<String, Integer> poss = getPositionsChampsAVoir(cl, typ);
        for (String ch : chps) {
          Adresse ad = adresses.get(cl).get(typ).get(ch);
          if (ad.getLastJalon().classe==classe&&type.equals(ad.getLastJalon().type)&&prop.equals(ad.prop)) {
            chpsASup.add(ch);
            adresses.get(cl).get(typ).remove(ch);
            modifiables.get(cl).get(typ).remove(ch);
            if (ch.equals(champsId.get(cl).get(typ)))
              champsId.get(cl).remove(typ);
            if (stylesValeursElements.get(cl).containsKey(typ))
              stylesValeursElements.get(cl).get(typ).remove(ch);
            nivx.remove(ch);
            poss.remove(ch);
          }
        }
        chps.removeAll(chpsASup);
        champspos.get(cl).put(typ, ordonnerChamps(chps, nivx, poss));
      }
  }
  public Unite[] getUnites(String type) {
    Unite[] units = corpus.getUnites(type).toArray(new Unite[0]);
    Arrays.sort(units);
    return units;
  }
  public Unite[] getUnitesAVoir(String type) {
    if (unitesAVoir.get(type)==null) {
      return new Unite[0];
    }
    return unitesAVoir.get(type);
  }
  public Relation[] getRelations(String type) {
    Relation[] rels = corpus.getRelations(type).toArray(new Relation[0]);
    Arrays.sort(rels);
    return rels;
  }
  public Relation[] getRelationsAVoir(String type) {
    // Pour l'instant, toutes les relations de ce type, mais ça peut changer avec la mise en place des sélections
    // nécessitera peut-être la mise en place d'une variable relationsAVoir (comme pour les unités) 
    Relation[] rels = corpus.getRelations(type).toArray(new Relation[0]);
    Arrays.sort(rels);
    return rels;
  }
  public Schema[] getSchemas(String type) {
    Schema[] schs = corpus.getSchemas(type).toArray(new Schema[0]);
    Arrays.sort(schs);
    return schs;
  }
  public Schema[] getSchemasAVoir(String type) {
    // Pour l'instant, tous les schémas de ce type, mais ça peut changer avec la mise en place des sélections
    // nécessitera peut-être la mise en place d'une variable schemasAVoir (comme pour les unités) 
    Schema[] schs = corpus.getSchemas(type).toArray(new Schema[0]);
    Arrays.sort(schs);
    return schs;
  }
  public Element[] getElements(Class<? extends Element> classe, String type) {
    if (classe==Unite.class) return getUnites(type);
    if (classe==Relation.class) return getRelations(type);
    if (classe==Schema.class) return getSchemas(type);
    throw new UnsupportedOperationException("sous-classe non traitée");
  }
  public Element[] getElementsAVoir(Class<? extends Element> classe, String type) {
    if (classe==Unite.class) return getUnitesAVoir(type);
    if (classe==Relation.class) return getRelationsAVoir(type);
    if (classe==Schema.class) return getSchemasAVoir(type);
    throw new UnsupportedOperationException("sous-classe non traitée");
  }
  public String getTexteUnite(Unite unit) {  // utilisé par ModeleChaines et par ModeleStatsGeometrie - à revoir : pas très efficace
    return corpus.getTexteUnite(unit);
  }
  public String getIdElement(Element elt) {  // utilisé  par PanneauOccurrences
    String type = elt.getType();
    Class<? extends Element> classe = elt.getClass();
    String champId = getChampId(classe, type);
    String id;
    if (champId!=null) {
      id = getValeurChamp(elt, champId);
      if (!id.isEmpty()) return id;
    }
    Element[] listeElts = getElements(classe, type);
    if (listeElts==null) return "";
    int no = Arrays.binarySearch(listeElts, elt);
    if (no<0) return "";
    return type+"-"+Integer.toString(no+1);
  }
  private String[] getIdElements(Class<? extends Element> classe, String type,
          Element[] eltsCorpus, Element[] eltsATraiter) {
    // eltsATraiter doit être  trié ; s'il est null, on prend toutes les éléments du corpus de ce type 
    String champId = getChampId(classe, type);
    String[] ids = new String[eltsATraiter==null ? eltsCorpus.length : eltsATraiter.length];
    String id;
    int i = 0;
    for (int no = 0; no<eltsCorpus.length; no++) {
      if (eltsATraiter==null||eltsATraiter[i]==eltsCorpus[no]) {
        id = champId==null ? "" : getValeurChamp(eltsCorpus[no], champId);
        ids[i++] = id.isEmpty() ? type+"-"+Integer.toString(no+1) : id;
      }
    }
    return ids;
  }
  public String[] getIdUnites(String type) {  // toutes les unités du corpus de ce type 
    return getIdElements(Unite.class, type, getUnites(type), null);
  }
  public String[] getIdUnites(String type, Unite[] unites) {    // unites doit être  trié 
    return getIdElements(Unite.class, type, getUnites(type), unites);
  }
  public String[] getIdRelations(String type) {  // toutes les relations du corpus de ce type 
    return getIdElements(Relation.class, type, getRelations(type), null);
  }
  public String[] getIdRelations(String type, Relation[] relations) {   // relations doit être  trié 
    return getIdElements(Relation.class, type, getRelations(type), relations);
  }
  public String[] getIdSchemas(String type) {  // tous les schémas du corpus de ce type 
    return getIdElements(Schema.class, type, getSchemas(type), null);
  }
  public String[] getIdSchemas(String type, Schema[] schemas) {    // schemas doit être  trié 
    return getIdElements(Schema.class, type, getSchemas(type), schemas);
  }
  public String[] getIdElements(Class<? extends Element> classe, String type) {
    if (classe==Relation.class) return getIdRelations(type);
    if (classe==Schema.class) return getIdSchemas(type);
    return getIdUnites(type);
  }
  /**
   * Fonction pour le coloriage
   */
  public UniteColoree[] getUnitesAColorier() {
    ArrayList<UniteColoree> unites = new ArrayList<UniteColoree>();
    for (Class<? extends Element> classe : stylesElements.keySet())
      for (String type : stylesElements.get(classe).keySet()) {
        for (Unite unit : Element.getUnitesSousjacentes(getElementsAVoir(classe, type)))
          unites.add(new UniteColoree(unit, stylesElements.get(classe).get(type)));
      }
    for (Class<? extends Element> classe : stylesValeursElements.keySet())
      for (String type : stylesValeursElements.get(classe).keySet())
        for (String champ : stylesValeursElements.get(classe).get(type).keySet())
          for (String valeur : stylesValeursElements.get(classe).get(type).get(champ).keySet()) {
            for (Unite unit : Element.getUnitesSousjacentes(getElementsAVoirValeurChamp(classe, type, champ, valeur)))
              unites.add(new UniteColoree(unit, stylesValeursElements.get(classe).get(type).get(champ).get(valeur)));
          }
    return unites.toArray(new UniteColoree[0]);
  }
  public Element[] getElementsAVoirValeurChamp(Class<? extends Element> classe,
          String type, String champ, String valeur) {
    ArrayList<Element> elts = new ArrayList<Element>();
    for (Element elt : getElementsAVoir(classe, type))
      if (valeur.equals(getValeurChamp(elt, champ))) elts.add(elt);
    return elts.toArray(new Element[0]);
  }
  /**
   * Fonctions de comptage pour les stats
   */
  public int getNbElementsAVoir(Class<? extends Element> classe, String type) {
    return getElementsAVoir(classe, type).length;
  }
  public HashMap<String, Integer> compterOccurrencesFiltrees(Class<? extends Element> classe,
          String type, String champCible, String champFiltre, String[] valeursChampFiltre) {
    HashMap<String, Integer> compte = initCompte(classe, type, champCible);
    for (Element elt : getElementsAVoir(classe, type)) {
      String valCible = getValeurChamp(elt, champCible);
      if (champFiltre==null) {
        compte.put(valCible, compte.get(valCible)+1);
      } else {
        String valfiltre = getValeurChamp(elt, champFiltre);
        for (Object valchfiltre : valeursChampFiltre) {
          if (valfiltre.equals(valchfiltre)) {
            compte.put(valCible, compte.get(valCible)+1);
            break;
          }
        }
      }
    }
    return compte;
  }
  public HashMap<String, HashMap<String, Integer>> compterOccurrencesCorrelees(
          Class<? extends Element> classe, String type, String champ1, String champ2) {
    HashMap<String, HashMap<String, Integer>> compte = new HashMap<String, HashMap<String, Integer>>();
    for (String val : getValeursChamp(classe, type, champ1)) {
      compte.put(val, initCompte(classe, type, champ2));
    }
    compte.put("", initCompte(classe, type, champ2));
    for (Element elt : getElementsAVoir(classe, type)) {
      String val1 = getValeurChamp(elt, champ1);
      String val2 = getValeurChamp(elt, champ2);
      compte.get(val1).put(val2, compte.get(val1).get(val2)+1);
    }
    return compte;
  }
  private HashMap<String, Integer> initCompte(Class<? extends Element> classe, String type, String champ) {
    HashMap<String, Integer> hm = new HashMap<String, Integer>();
    for (String val : getValeursChamp(classe, type, champ)) {
      hm.put(val, 0);
    }
    hm.put("", 0);
    return hm;
  }
  public Element[] getDoubleSelectionOccurrences(Class<? extends Element> classe, String type,
          String champ1, String[] valeurs1, String champ2, String[] valeurs2) {
    ArrayList<Element> aliste = new ArrayList<Element>();
    for (Element elt : getElementsAVoir(classe, type)) {
      if (verifierContrainteFiltre(elt, champ1, valeurs1)&&verifierContrainteFiltre(elt, champ2, valeurs2)) {
        aliste.add(elt);
      }
    }
    Element[] res = aliste.toArray(new Element[0]);
    Arrays.sort(res);
    return res;
  }
  private boolean verifierContrainteFiltre(Element elt, String champ, String[] valeurs) {
    if (champ==null) {
      return true;
    }
    String valeur = getValeurChamp(elt, champ);
    for (String val : valeurs) {
      if (val.equals(valeur)) {
        return true;
      }
    }
    return false;
  }
  public Matrix getMatrice(Class<? extends Element> classe, String type, String[] champsSelect, Element[] eltsSelect) {
    HashMap<String, HashMap<String, Integer>> numeros = new HashMap<String, HashMap<String, Integer>>();
    int nb = 0;
    for (String champ : champsSelect) {
      numeros.put(champ, new HashMap<String, Integer>());
      numeros.get(champ).put("", nb++);
      for (String val : getValeursChamp(classe, type, champ)) {
        numeros.get(champ).put(val, nb++);
      }
    }
    double[][] matrice = new double[eltsSelect.length][nb];
    for (int i = 0; i<eltsSelect.length; i++) {
      for (String champ : champsSelect) {
        matrice[i][numeros.get(champ).get(getValeurChamp(eltsSelect[i], champ))] = 1.;
      }
    }
    return new Matrix(matrice, eltsSelect.length, nb);
  }
  public SparseMatrix getMatriceCreuse(Class<? extends Element> classe, String type,
          String[] champsSelect, Element[] eltsSelect) {
    HashMap<String, HashMap<String, Integer>> numeros =
            new HashMap<String, HashMap<String, Integer>>();
    int nb = 0;
    for (String champ : champsSelect) {
      numeros.put(champ, new HashMap<String, Integer>());
      numeros.get(champ).put("", nb++);
      for (String val : getValeursChamp(classe, type, champ))
        numeros.get(champ).put(val, nb++);
    }
//    double[][] matrice = new double[eltsSelect.length][nb];
    ArrayList<Integer> rows = new ArrayList<Integer>();
    ArrayList<Integer> cols = new ArrayList<Integer>();
    for (int i = 0; i<eltsSelect.length; i++)
      for (String champ : champsSelect) {
        rows.add(i);
        cols.add(numeros.get(champ).get(getValeurChamp(eltsSelect[i], champ)));
      }
    return new SparseMatrix(eltsSelect.length, nb, rows, cols, 1);
  }

  public static class TripletValTypique {
    public String champ;
    public String valeur;
    public float pourcentage;
    public TripletValTypique(String champ, String valeur, float pourcentage) {
      this.champ = champ;
      this.valeur = valeur;
      this.pourcentage = pourcentage;
    }
  }
  public TripletValTypique[] getValTypiques(Class<? extends Element> classe,
          String type, Element[] elts) {
    ArrayList<String> chps = new ArrayList<String>();
    ArrayList<String> vals = new ArrayList<String>();
    ArrayList<Float> pcts = new ArrayList<Float>();
    HashMap<String, Integer> compte = new HashMap<String, Integer>();
    int total = elts.length;
    for (String champ : getListeChamps(classe, type)) {
      compte.clear();
      for (Element elt : elts) {
        String val = getValeurChamp(elt, champ);
        if (val.isEmpty()) continue;
        if (compte.containsKey(val)) compte.put(val, compte.get(val)+1);
        else compte.put(val, 1);
      }
      for (String val : compte.keySet()) if (2*compte.get(val)>total) {
          chps.add(champ);
          vals.add(val);
          pcts.add(((float) compte.get(val))/total);
          break;
        }
    }
    TripletValTypique[] triplets = new TripletValTypique[chps.size()];
    for (int i = 0; i<chps.size(); i++)
      triplets[i] = new TripletValTypique(chps.get(i), vals.get(i), pcts.get(i));
    return triplets;
  }
  public String[] getIdListeElements(Class<? extends Element> classe,
          String type, Element[] elts) {
    return getIdElements(classe, type, elts, null);
  }
  public String[][] getValeursChampsListeElements(String[] champs, Element[] elts) {
    String[][] valeurs = new String[elts.length][champs.length];
    for (int i = 0; i<elts.length; i++) for (int j = 0; j<champs.length; j++)
        valeurs[i][j] = getValeurChamp(elts[i], champs[j]);
    return valeurs;
  }
  public TableModel tableauUnites(String type) {
    if (getUnitesAVoir(type).length==0) return null;
    return new TableUnites(type, this);
  }

  static public class TableUnites extends AbstractTableModel {
    String type;
    Vue vue;
    Unite[] unites;
    String[] champs;
    TableUnites(String type, Vue vue) {
      super();
      this.type = type;
      this.vue = vue;
    }
    public int getRowCount() {
      return vue.getUnitesAVoir(type).length;
    }
    public int getColumnCount() {
      return vue.getNomsChamps(Unite.class, type).length+3;
    }
    @Override
    public String getColumnName(int col) {
      switch (col) {
        case 0:
          return "#Id";
        case 1:
          return "From";
        case 2:
          return "To";
        default:
          return vue.getNomsChamps(Unite.class, type)[col-3];
      }
    }
    public Object getValueAt(int row, int col) {
      switch (col) {
        case 0:
          return row+1;
        case 1:
          return vue.getUnitesAVoir(type)[row].getDeb();
        case 2:
          return vue.getUnitesAVoir(type)[row].getFin();
        default:
          return vue.getValeurChamp(
                  vue.getUnitesAVoir(type)[row],
                  vue.getNomsChamps(Unite.class, type)[col-3]);
      }
    }
  }
}
