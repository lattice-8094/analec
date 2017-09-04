/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.texte;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import visuAnalec.PanneauEditeur.*;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.texte.PanneauAffTexte.ListenerSourisTexte.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import static visuAnalec.texte.PanneauAffTexte.SourisEvent.*;

/**
 *
 * @author Bernard
 */
public class PanneauAffTexte extends PanneauTexte {
  private PanneauCommandes panneauCommandes;
  private ListenerSourisTexte sourisListener;
  protected Marquage marquage = new Marquage(this);
  public PanneauAffTexte(Corpus corpus) {
    super(corpus);
    coloriage = new Coloriage(this);
    setEditable(false);
    setSourisListener();
  }
  @Override
  public String getToolTipText() {
    Point pt = getMousePosition();
    if (pt == null) return "";
    Unite[] unitesVisees = getUnitesVisees(marquage, pt);
    if (unitesVisees.length == 0) return "";
    ArrayList<String> textes = new ArrayList<String>();
    ArrayList<String> idsElementsVises = new ArrayList<String>();
    for (Unite unit : unitesVisees) {
      String texte = getTexteUnite(unit);
      for (ElementMarque eltMarque : panneauCommandes.trouverElementsVises(unit,
              marquage.getMarquage(unit), true)) {
        idsElementsVises.add(eltMarque.id);
        textes.add(texte);
      }
    }
    return GVisu.styleIdTooltip(textes, idsElementsVises);
  }
  protected void setSourisListener() {
    sourisListener = new ListenerSourisTexte(this);
    addMouseListener(sourisListener);
  }
  public void setCommandes(PanneauCommandes panneauCommandes) {
    this.panneauCommandes = panneauCommandes;
    sourisListener.setCommandes(panneauCommandes);
  }
  private void deselecterUnite(Unite unite) {  // utilisé uniquement pour rectifier une borne
    int debUnite = indexToPosition(unite.getDeb());
    int finUnite = indexToPosition(unite.getFin());
    doc.setCharacterAttributes(debUnite, finUnite - debUnite, AttributsStandard, true);
  }
  private void selecterUnite(Unite unite) {   // utilisé uniquement pour rectifier une borne
    int debUnite = indexToPosition(unite.getDeb());
    int finUnite = indexToPosition(unite.getFin());
    doc.setCharacterAttributes(debUnite, finUnite - debUnite, SELECTION_1, true);
  }
  public void pasDeMarquage() {
    marquerUnites(marquage, new TypeMarquage[0], null);
    sourisListener.setEtat(EtatClic.SAISIE);
  }
  public void saisir(Unite amontrer, Unite[] lUnites, TypeMarquage type) {
    TypeMarquage[] typesMarq = {type};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(type, lUnites);
    marquerUnites(marquage, typesMarq, unites);
    if (amontrer != null) placerAscenseur(amontrer);
    sourisListener.setEtat(EtatClic.SAISIE);
  }
  public void saisir(Unite amontrer, Unite[] lUnites1, TypeMarquage type1, Unite[] lUnites2, TypeMarquage type2) {
    TypeMarquage[] typesMarq = {type1, type2};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(type1, lUnites1);
    unites.put(type2, lUnites2);
    marquerUnites(marquage, typesMarq, unites);
    if (amontrer != null) placerAscenseur(amontrer);
    sourisListener.setEtat(EtatClic.SAISIE);
  }
  public void saisir(Unite amontrer, Unite[] lUnites1, TypeMarquage type1,
          Unite[] lUnites2, TypeMarquage type2, Unite[] lUnites3, TypeMarquage type3) {
    TypeMarquage[] typesMarq = {type1, type2, type3};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(type1, lUnites1);
    unites.put(type2, lUnites2);
    unites.put(type3, lUnites3);
    marquerUnites(marquage, typesMarq, unites);
    if (amontrer != null) placerAscenseur(amontrer);
    sourisListener.setEtat(EtatClic.SAISIE);
  }
  public void saisir(Unite amontrer, Unite[] lUnites1, TypeMarquage type1, Unite[] lUnites2, TypeMarquage type2,
          Unite[] lUnites3, TypeMarquage type3, Unite[] lUnites4, TypeMarquage type4) {
    TypeMarquage[] typesMarq = {type1, type2, type3, type4};
    EnumMap<TypeMarquage, Unite[]> unites = new EnumMap<TypeMarquage, Unite[]>(TypeMarquage.class);
    unites.put(type1, lUnites1);
    unites.put(type2, lUnites2);
    unites.put(type3, lUnites3);
    unites.put(type4, lUnites4);
    marquerUnites(marquage, typesMarq, unites);
    if (amontrer != null) placerAscenseur(amontrer);
    sourisListener.setEtat(EtatClic.SAISIE);
  }
  public void rectifierBorneUnite(Unite unite) {
    sourisListener.etatChtBornes(unite);
  }
  void changerBornes(Unite unite, int borneGauche, int borneDroite) {
    corpus.modifBornesUnite(unite, borneGauche, borneDroite);
  }

  public static class SourisEvent {
    public enum TypeEvent {
      SELECT_TEXTE, SELECT_RIEN, SELECT_ELEMENT
    }
    private TypeEvent type;
    private Element elementVise;
    private TypeMarquage marquageElementVise;
    private int[] bornesSelectionTexte;
    protected SourisEvent(TypeEvent type) {
      this(type, null, null, null);
    }
    protected SourisEvent(TypeEvent type, Element elt, TypeMarquage marque) {
      this(type, elt, marque, null);
    }
    protected SourisEvent(TypeEvent type, int[] bornes) {
      this(type, null, null, bornes);
    }
    protected SourisEvent(TypeEvent type, Element elt, TypeMarquage marque, int[] bornes) {
      this.type = type;
      marquageElementVise = marque;
      bornesSelectionTexte = bornes;
      elementVise = elt;
    }
    public TypeEvent getTypeEvent() {
      return type;
    }
    public TypeMarquage getMarquageElementVise() {
      return marquageElementVise;
    }
    public int[] getBornesSelectionTexte() {
      return bornesSelectionTexte;
    }
    public Element getElementVise() {
      return elementVise;
    }
  }

  protected static class ListenerSourisTexte extends MouseAdapter {
    enum EtatClic {
      INACTIF,
      SAISIE,
      BORNES,
    }
    private EtatClic etatClic;
    private Unite uniteABorner;
    private PanneauAffTexte panneauTexte;
    private PanneauCommandes panneauCommandes;
    private JPopupMenuUnites popupMenuUnites;
    public ListenerSourisTexte(PanneauAffTexte pTexte) {
      panneauTexte = pTexte;
      popupMenuUnites = new JPopupMenuUnites(pTexte);
    }
    void setCommandes(PanneauCommandes commandes) {
      panneauCommandes = commandes;
      popupMenuUnites.setCommandes(commandes);
    }
    void setEtat(EtatClic etat) {
      this.etatClic = etat;
    }
    void etatChtBornes(Unite uniteABorner) {
      etatClic = EtatClic.BORNES;
      this.uniteABorner = uniteABorner;
      int deb = panneauTexte.indexToPosition(uniteABorner.getDeb());
      int fin = panneauTexte.indexToPosition(uniteABorner.getFin());
      panneauTexte.doc.setCharacterAttributes(deb, fin - deb, BORNAGE, true);
    }
    @Override
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) mousePopup(e);
    }
    @Override
    public void mouseReleased(MouseEvent e) {
      try {
        if (e.isPopupTrigger()) {
          mousePopup(e);
          return;
        }
        switch (etatClic) {
          case INACTIF:
          case BORNES:
            return;
          case SAISIE:
            if (panneauTexte.getSelectedText() != null) panneauCommandes.traiterSourisEvent(new SourisEvent(
                      TypeEvent.SELECT_TEXTE, panneauTexte.getIndSelection()));
            return;
          default:
            throw new UnsupportedOperationException("Cas non prévu d'étatClic");
        }
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
      try {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        if (e.getClickCount() > 1) return;
        switch (etatClic) {
          case INACTIF:  // peut-être inutile ??
            return;
          case SAISIE:
            Unite uniteVisee = panneauTexte.getUniteVisee(panneauTexte.marquage, e.getPoint());
            if (uniteVisee == null) {
              panneauCommandes.traiterSourisEvent(new SourisEvent(TypeEvent.SELECT_RIEN));
              return;
            }
            ElementMarque eltM = panneauCommandes.trouverElementsVises(uniteVisee,
                    panneauTexte.marquage.getMarquage(uniteVisee), false).get(0);
            panneauCommandes.traiterSourisEvent(new SourisEvent(TypeEvent.SELECT_ELEMENT,
                    eltM.elt, eltM.marque));
            return;
          case BORNES: // à revoir
            int olddeb = uniteABorner.getDeb();
            int oldfin = uniteABorner.getFin();
            int newdeb = olddeb;
            int newfin = oldfin;
            int newborne = panneauTexte.positionToIndex(panneauTexte.viewToModel(e.getPoint()));
            if (2 * newborne < olddeb + oldfin) newdeb = newborne;
            else newfin = newborne;
            panneauTexte.deselecterUnite(uniteABorner);
            panneauTexte.changerBornes(uniteABorner, newdeb, newfin);
            panneauTexte.selecterUnite(uniteABorner);
            etatClic = EtatClic.SAISIE;
            return;
          default:
            throw new UnsupportedOperationException("Cas non prévu d'état d'AffTexte");
        }
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
    public void mousePopup(MouseEvent e) {
      Unite[] unitesVisees = panneauTexte.getUnitesVisees(panneauTexte.marquage, e.getPoint());
      if (unitesVisees.length > 0) {
        popupMenuUnites.reinitialiser(unitesVisees);
        popupMenuUnites.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  private static class JPopupMenuUnites extends JPopupMenu {
    private PanneauAffTexte panneauTexte;
    private PanneauCommandes panneauCommandes;
    private ArrayList<ElementMarque> elementsMarques = new ArrayList<ElementMarque>();
    private Element[] elementsVises;
    private String[] idsElementsVises;
    private TypeMarquage[] marquesElementsVises;
    public JPopupMenuUnites(PanneauAffTexte panneauTexte) {
      super();
      this.panneauTexte = panneauTexte;
    }
    void setCommandes(PanneauCommandes commandes) {
      panneauCommandes = commandes;
    }
    void reinitialiser(Unite[] unitesVisees) {
      removeAll();
      elementsMarques.clear();
      elementsVises = new Element[unitesVisees.length];
      idsElementsVises = new String[unitesVisees.length];
      marquesElementsVises = new TypeMarquage[unitesVisees.length];
      int no = 0;
      ArrayList<ElementMarque> eltsMARajouter = new ArrayList<ElementMarque>();
      ArrayList<String> descrsARajouter = new ArrayList<String>();
      for (Unite unit : unitesVisees) {
        String texte = panneauTexte.getTexteUnite(unit);
        ArrayList<TypeMarquage> typesM = panneauTexte.marquage.getMarquages(unit);
        for (ElementMarque eltM : panneauCommandes.trouverElementsVises(unit, typesM.get(0), true)) {
          elementsMarques.add(eltM);
          String descr = GVisu.styleIdPopup(texte, eltM.id);
          add(new ActionSelectionUnites(descr, no++));
        }
        for (TypeMarquage typeM : typesM.subList(1, typesM.size()))
          for (ElementMarque eltM : panneauCommandes.trouverElementsVises(unit, typeM, true)) {
            eltsMARajouter.add(eltM);
            descrsARajouter.add(GVisu.styleIdPopup(texte, eltM.id));
          }
      }
      for (int i = 0; i < eltsMARajouter.size(); i++) {
        elementsMarques.add(eltsMARajouter.get(i));
        add(new ActionSelectionUnites(descrsARajouter.get(i), no++));
      }
    }

    private class ActionSelectionUnites extends GAction {
      int no;
      private ActionSelectionUnites(String descr, int no) {
        super(descr);
        this.no = no;
      }
      public void executer() {
        panneauCommandes.traiterSourisEvent(new SourisEvent(TypeEvent.SELECT_ELEMENT,
                elementsMarques.get(no).elt, elementsMarques.get(no).marque));
      }
    }
  }
}

      

