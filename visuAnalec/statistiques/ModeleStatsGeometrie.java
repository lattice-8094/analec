/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.statistiques;

/**
 *
 * @author Bernard
 */
import JamaPlus.*;
import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import visuAnalec.elements.*;
import visuAnalec.vue.*;

public class ModeleStatsGeometrie {
  public enum TypeEventDonneesGraphe {
    NEW_DONNEES,
    MODIF_DONNEES,
    CLEAR_DONNEES,
    NEW_COORDS,
    NEW_COULEURS,
    NEW_SELECT_PTS
  }

  public interface DonneesGrapheListener extends EventListener {
    void traiterEvent(DonneesGrapheEvent e);
  }

  public class DonneesGrapheEvent extends EventObject {
    private TypeEventDonneesGraphe type;
    DonneesGrapheEvent(Object source, TypeEventDonneesGraphe type) {
      super(source);
      this.type = type;
    }
    public TypeEventDonneesGraphe getType() {
      return type;
    }
  }
  private ArrayList<DonneesGrapheListener> listeCorrespondants = new ArrayList<DonneesGrapheListener>();
  void addEventListener(DonneesGrapheListener correspondant) {
    listeCorrespondants.add(correspondant);
  }
  void removeEventListener(DonneesGrapheListener correspondant) {
    listeCorrespondants.remove(correspondant);
  }
  private void fireDonneesGrapheEvent(DonneesGrapheEvent evt) {
    for (Object corresp : listeCorrespondants.toArray()) {
      ((DonneesGrapheListener) corresp).traiterEvent(evt);
    }
  }
  private Vue vue;
  private Class<? extends Element> classe;
  private String type;
  private String[] champsPertinents;
  private Element[] elementsRetenus;
  private Matrix Pts;
  private Color[] couleursPts;
  private ArrayList<Integer> nosPtsChoisis;
  public Class<? extends Element> getClasseElements() {
    return classe;
  }
  public String getTypeElements() {
    return type;
  }
  public Matrix getPts() {
    return Pts;
  }
  public Color[] getCouleurs() {
    return couleursPts;
  }
  public boolean[] getEtatsChoixtPts() {
    boolean[] etats = new boolean[elementsRetenus.length];
    for (int no : nosPtsChoisis) {
      etats[no] = true;
    }
    return etats;
  }
  public ArrayList<ArrayList<String>> getTexteIdElements(ArrayList<Integer> nos) {
    ArrayList<ArrayList<String>> textesIds = new ArrayList<ArrayList<String>>();
    ArrayList<String> textes = new ArrayList<String>();
    ArrayList<String> ids = new ArrayList<String>();
    for (int i = 0; i<nos.size(); i++) {
      Element elt = elementsRetenus[nos.get(i)];
      textes.add(vue.getTexteUnite(elt.getUnite0()));
      ids.add(vue.getIdElement(elt));
    }
    textesIds.add(textes);
    textesIds.add(ids);
    return textesIds;
  }
  public Element[] getListeElementsChoisis() {
    Element[] liste = new Element[nosPtsChoisis.size()];
    for (int i = 0; i<liste.length; i++) {
      liste[i] = elementsRetenus[nosPtsChoisis.get(i)];
    }
    Arrays.sort(liste);
    return liste;
  }
//  public Unite getDerniereUniteChoisie() {
//    if (nosPtsChoisis.isEmpty()) {
//      return null;
//    }
//    return unitesSelectionnees[nosPtsChoisis.get(nosPtsChoisis.size() - 1)];
//  }
  void initialise(Vue vue, Class<? extends Element> classe, String type) {
    this.vue = vue;
    this.type = type;
    this.classe = classe;
    if (classe==null) {
      champsPertinents = new String[0];
      elementsRetenus = new Element[0];
      Pts = null;
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.CLEAR_DONNEES));
      return;
    }
    champsPertinents = vue.getNomsChamps(classe, type);
    elementsRetenus = vue.getElementsAVoir(classe, type);
    if (calculerCoord()) {
      nosPtsChoisis = new ArrayList<Integer>();
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.NEW_DONNEES));
    } else {
      Pts = null;
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.CLEAR_DONNEES));
    }
  }
  void majChampsSelect(String[] champsSelect) {
    champsPertinents = champsSelect;
    if (champsSelect.length>0&&calculerCoord()) {
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.NEW_COORDS));
    } else {
      Pts = null;
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.CLEAR_DONNEES));
    }
  }
  void majChampAffiche(String champAffiche, HashMap<String, Color> couleurValeur) {
    couleursPts = new Color[elementsRetenus.length];
    for (int i = 0; i<elementsRetenus.length; i++) {
      couleursPts[i] = couleurValeur.get(vue.getValeurChamp(elementsRetenus[i], champAffiche));
    }
    fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.NEW_COULEURS));
  }
  boolean calculerCoord() {
//    Matrix mat = vue.getMatrice(classe, type, champsPertinents, elementsRetenus);
    SparseMatrix mat = vue.getMatriceCreuse(classe, type, champsPertinents, elementsRetenus);
    Matrix CP = mat.afc().getCP();
    if (CP==null) return false;
    if (CP.getColumnDimension()<2) {
      Pts = new Matrix(elementsRetenus.length, 2);
      if (CP.getColumnDimension()==1)
        Pts.setMatrix(0, elementsRetenus.length-1, 0, 0, CP);
    } else {
      Pts = CP.getMatrix(0, elementsRetenus.length-1, 0, 1);
    }
    return true;
  }
  void setPtsChoisis(ArrayList<Integer> nos) {
    nosPtsChoisis = nos;
    fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.NEW_SELECT_PTS));
  }
  void majPtsChoisis(ArrayList<Integer> nos) {
    for (Integer no : nos) {
      if (nosPtsChoisis.contains(no)) {
        nosPtsChoisis.remove(no);
      } else {
        nosPtsChoisis.add(no);
      }
    }
    fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.NEW_SELECT_PTS));
  }
  void supPtsChoisis() {
    ArrayList<Element> EltsSelects = new ArrayList<Element>(
            Arrays.asList(elementsRetenus));
    for (Integer no : nosPtsChoisis) {
      EltsSelects.remove(elementsRetenus[no]);
    }
    elementsRetenus = EltsSelects.toArray(new Element[0]);
    if (calculerCoord()) {
      nosPtsChoisis = new ArrayList<Integer>();
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.MODIF_DONNEES));
    } else {
      Pts = null;
      fireDonneesGrapheEvent(new DonneesGrapheEvent(this, TypeEventDonneesGraphe.CLEAR_DONNEES));
    }
  }
}
