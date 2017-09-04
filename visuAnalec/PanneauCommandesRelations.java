/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import visuAnalec.Message.*;
import visuAnalec.PanneauEditeur.*;
import visuAnalec.elements.*;
import visuAnalec.texte.*;
import visuAnalec.texte.PanneauAffTexte.*;
import visuAnalec.texte.PanneauTexte.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.util.GVisu.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public class PanneauCommandesRelations extends PanneauCommandes implements ActionListener {
  private PanneauChamps panneauChamps;
  private TypeComboBox saisieTypeRelations;
  private ElementComboBox saisieRelation;
  private TypeComboBox saisieTypeElt1;
  private ElementComboBox saisieElt1;
  private TypeComboBox saisieTypeElt2;
  private ElementComboBox saisieElt2;
  private JButton creeRelation;
  private JButton supprimeRelation;
  private JButton creeUniteElt1;
  private JButton creeUniteElt2;
  private boolean uniteCreeElt1 = true; // pour la création d'unités en cours de route
  private int[] bornesSelectTexte;
  /*
  
   * CREATION PAR LE PANNEAU EDITEUR
   */
  PanneauCommandesRelations(Vue vue, PanneauAffTexte pTexte, PanneauChamps pChamps) {
    super(vue, pTexte);
    JToolBar barre1 = new JToolBar();
    barre1.setPreferredSize(new Dimension(0, 22));
    barre1.setAlignmentX(0.0f);
    //    barre1.setFloatable(false);
    add(barre1);
    JToolBar barre2 = new JToolBar();
//    barre2.setBackground(PanneauTexte.getCouleurFond2());
    barre2.setPreferredSize(new Dimension(0, 22));
    barre2.setAlignmentX(0.0f);
//     barre2.setFloatable(false);
    add(barre2);
    JToolBar barre3 = new JToolBar();
    barre3.setPreferredSize(new Dimension(0, 22));
    barre3.setAlignmentX(0.0f);
//    barre3.setBackground(PanneauTexte.getCouleurFond2());
//     barre3.setFloatable(false);
    add(barre3);

    panneauChamps = pChamps;
    saisieTypeRelations = new TypeComboBox(Relation.class);
    saisieTypeRelations.addActionListener(this);
    saisieRelation = new ElementComboBox();
    saisieRelation.addActionListener(this);
    saisieTypeElt1 = new TypeComboBox(Element.class);
    saisieTypeElt1.addActionListener(this);
    saisieElt1 = new ElementComboBox();
    saisieElt1.addActionListener(this);
    saisieTypeElt2 = new TypeComboBox(Element.class);
    saisieTypeElt2.addActionListener(this);
    saisieElt2 = new ElementComboBox();
    saisieElt2.addActionListener(this);
    creeRelation = new JButton(new GAction("Créer") {
      public void executer() {
        creerRelation();
      }
    });
    supprimeRelation = new JButton(new GAction("Supprimer") {
      public void executer() {
        supprimerRelation();
      }
    });
    creeUniteElt1 = new JButton(new GAction("Créer") {
      public void executer() {
        creerUniteElt1();
      }
    });
    creeUniteElt2 = new JButton(new GAction("Créer") {
      public void executer() {
        creerUniteElt2();
      }
    });
    barre1.addSeparator();
    barre1.add(new TitreBarre("  Relation :  ", PanneauTexte.getCouleurFond1()));
    barre1.addSeparator();
    barre1.add(saisieTypeRelations);
    barre1.add(saisieRelation);
    barre1.addSeparator();
    barre1.add(creeRelation);
    barre1.addSeparator();
    barre1.add(supprimeRelation);
    barre2.addSeparator();
    barre2.add(new TitreBarre("Elément 1 : ", PanneauTexte.getCouleurFond2()));
    barre2.addSeparator();
    barre2.add(saisieTypeElt1);
    barre2.add(saisieElt1);
    barre2.addSeparator();
    barre2.add(creeUniteElt1);
    barre3.addSeparator();
    barre3.add(new TitreBarre("Elément 2 : ", PanneauTexte.getCouleurFond3()));
    barre3.addSeparator();
    barre3.add(saisieTypeElt2);
    barre3.add(saisieElt2);
    barre3.addSeparator();
    barre3.add(creeUniteElt2);
  }

  /*
   * COMMANDES UTILISEES PAR LE PANNEAU EDITEUR
   */
  public void reinitialiser() {
    // typiquement, appelé par le panneau éditeur à la suite d'une modif de vue ou de structure
    saisieTypeRelations.removeActionListener(this);
    saisieTypeRelations.setModele(vue.getTypesRelationsAVoir());
    saisieTypeRelations.effSelection();
    saisieTypeRelations.addActionListener(this);
    saisieTypeElt1.removeActionListener(this);
    saisieTypeElt1.setModele(vue.getTypes(Unite.class), vue.getTypes(Relation.class), vue.getTypes(Schema.class));
    saisieTypeElt1.effSelection();
    saisieTypeElt1.addActionListener(this);
    saisieTypeElt2.removeActionListener(this);
    saisieTypeElt2.setModele(vue.getTypes(Unite.class), vue.getTypes(Relation.class), vue.getTypes(Schema.class));
    saisieTypeElt2.effSelection();
    saisieTypeElt2.addActionListener(this);
    initSaisieRelation();
    initSaisieElt1();
    initSaisieElt2();
    etatSelectRien();
    revalidate();
    repaint();
  }
  public void reafficher() {
    // potentiellement appelé par le panneau éditeur ?
    // appelé aussi par  traiterElementEvent
    if (saisieRelation.pasdeSelection()) {
      initSaisieRelation();
      if (saisieElt1.pasdeSelection()) {
        initSaisieElt1();
        if (saisieElt2.pasdeSelection()) {
          initSaisieElt2();
          etatSelectRien();
        } else {
          Element elt2Courant = saisieElt2.getSelection();
          initSaisieElt2(elt2Courant);
          etatSelectElt2(elt2Courant);
        }
      } else {
        Element elt1Courant = saisieElt1.getSelection();
        initSaisieElt1(elt1Courant);
        if (saisieElt2.pasdeSelection()) {
          initSaisieElt2();
          etatSelectElt1(elt1Courant);

        } else {
          Element elt2Courant = saisieElt2.getSelection();
          initSaisieElt2(elt2Courant);
          etatSelectElts(elt1Courant, elt2Courant);
        }
      }
    } else {
      Relation relCourant = (Relation) saisieRelation.getSelection();
      initSaisieRelation(relCourant);
      initSaisieElt1(relCourant.getElt1());
      initSaisieElt2(relCourant.getElt2());
      etatSelectRel(relCourant);
    }
  }
  public void elementSuivant(int sens) {
    if (sens>0) saisieRelation.gotoSuivant();
    else saisieRelation.gotoPrecedent();
  }
 public Element elementSelectionne() {
    if (saisieRelation.pasdeSelection()) return null;
    return saisieRelation.getSelection();
  }
  public void editerElement(Element elt) {
    // appelé par le panneauEditeur : elt est forcément une relation
    if (elt.getClass()!=Relation.class)
      throw new UnsupportedOperationException("Erreur : classe incorrecte");
    editerRelation((Relation) elt);
  }
  public void traiterElementEvent(ElementEvent evt) {
    switch (evt.getModif()) {
      case AJOUT_RELATION:
        Relation rel = (Relation) evt.getElement();
        editerRelation(rel);
        return;
      case SUP_RELATION:
        if (evt.getElement()==saisieRelation.getSelection()) {
          initSaisieRelation();
          initSaisieElt1();
          initSaisieElt2();
          etatSelectRien();
        } else {
          reafficher();
        }
        return;
      case AJOUT_UNITE:
        Unite unit = (Unite) evt.getElement();
        boolean chgEtat = false;
        if (!saisieTypeElt1.pasdeSelection()&&saisieTypeElt1.getClasseSelection()==Unite.class
                &&saisieTypeElt1.getTypeSelection().equals(unit.getType())) {
          initSaisieElt1(uniteCreeElt1 ? unit : null);
          chgEtat = true;
        }
        if (!saisieTypeElt2.pasdeSelection()&&saisieTypeElt2.getClasseSelection()==Unite.class
                &&saisieTypeElt2.getTypeSelection().equals(unit.getType())) {
          initSaisieElt2(uniteCreeElt1 ? null : unit);
          chgEtat = true;
        }
        if (chgEtat) {
          if (uniteCreeElt1) etatSelectElt1(unit);
          else etatSelectElt2(unit);
          uniteCreeElt1 = true;
        }
        return;
      case SUP_UNITE:
      case BORNES_UNITE:
      case AJOUT_SCHEMA:
      case MODIF_SCHEMA:
      case SUP_SCHEMA:
      case MODIF_VALEUR:
        reafficher();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getModif()+" oublié dans un switch");
    }
  }
  public void editerRelation(Relation rel) {
    // appelé  par traiterElementEvent, 
    // et aussi directement par le panneau éditeur quand il est sollicité par d'autres fenêtres
    if (!rel.getType().equals(saisieTypeRelations.getTypeSelection())) {
      saisieTypeRelations.removeActionListener(this);
      saisieTypeRelations.setSelection(rel.getType());
      saisieTypeRelations.addActionListener(this);
    }
    initSaisieRelation(rel);
    saisieRelation.setSelection(rel);
  }
  public ArrayList<ElementMarque> trouverElementsVises(Unite uniteVisee, TypeMarquage marqueUniteVisee,
          boolean avecId) {
    ArrayList<ElementMarque> eltsMarques = new ArrayList<ElementMarque>();
    ArrayList<Element> eltsVises;
    Element eltVise;
    switch (marqueUniteVisee) {
      case FOND1:
        eltsVises = Element.trouverElementsParUniteSousjacente(saisieRelation.getElements(), uniteVisee);
        for (Element elt : eltsVises)
          eltsMarques.add(new ElementMarque(
                  elt, marqueUniteVisee, avecId ? saisieRelation.getIdElement(elt) : null));
        return eltsMarques;
      case FOND2:
        eltsVises = Element.trouverElementsParUniteSousjacente(saisieElt1.getElements(), uniteVisee);
        for (Element elt : eltsVises)
          eltsMarques.add(new ElementMarque(
                  elt, marqueUniteVisee, avecId ? saisieElt1.getIdElement(elt) : null));
        return eltsMarques;
      case FOND3:
        eltsVises = Element.trouverElementsParUniteSousjacente(saisieElt2.getElements(), uniteVisee);
        for (Element elt : eltsVises)
          eltsMarques.add(new ElementMarque(
                  elt, marqueUniteVisee, avecId ? saisieElt2.getIdElement(elt) : null));
        return eltsMarques;
      case SELECT2:
      case SELECT1_2:
        eltVise = saisieElt1.getSelection();
        eltsMarques.add(new ElementMarque(
                eltVise, marqueUniteVisee, avecId ? saisieElt1.getIdElement(eltVise) : null));
        return eltsMarques;
      case SELECT3:
      case SELECT1_3:
        eltVise = saisieElt2.getSelection();
        eltsMarques.add(new ElementMarque(
                eltVise, marqueUniteVisee, avecId ? saisieElt2.getIdElement(eltVise) : null));
        return eltsMarques;
      default:
        throw new UnsupportedOperationException("Marquage non prévu : "+marqueUniteVisee);

    }
  }

  /*
   * ACTIONS DE L'UTILISATEUR : CHANGEMENTS D'ETAT
   */
  public void actionPerformed(ActionEvent evt) {
    try {
      if (evt.getSource()==saisieTypeRelations) {
        initSaisieRelation();
        if (saisieElt1.pasdeSelection()) {
          if (saisieElt2.pasdeSelection()) etatSelectRien();
          else etatSelectElt2(saisieElt2.getSelection());
        } else {
          if (saisieElt2.pasdeSelection())
            etatSelectElt1(saisieElt1.getSelection());
          else
            checkEtatSelectElts(saisieElt1.getSelection(), saisieElt2.getSelection());
        }
        return;
      }
      if (evt.getSource()==saisieTypeElt1) {
        forceEffSelectRel();
        initSaisieElt1();
        if (saisieElt2.pasdeSelection()) etatSelectRien();
        else etatSelectElt2(saisieElt2.getSelection());
        return;
      }
      if (evt.getSource()==saisieTypeElt2) {
        forceEffSelectRel();
        initSaisieElt2();
        if (saisieElt1.pasdeSelection()) etatSelectRien();
        else etatSelectElt1(saisieElt1.getSelection());
        return;
      }
      if (evt.getSource()==saisieRelation) {
        if (saisieRelation.pasdeSelection()) {
          if (saisieElt1.pasdeSelection()) {
            if (saisieElt2.pasdeSelection()) etatSelectRien();
            else etatSelectElt2(saisieElt2.getSelection());
          } else {
            if (saisieElt2.pasdeSelection())
              etatSelectElt1(saisieElt1.getSelection());
            else
              etatSelectElts(saisieElt1.getSelection(), saisieElt2.getSelection());
          }
        } else {
          forceEtatSelectRel((Relation) saisieRelation.getSelection());
        }
        return;
      }
      if (evt.getSource()==saisieElt1) {
        if (!saisieRelation.pasdeSelection()) {
          saisieRelation.removeActionListener(this);
          saisieRelation.effSelection();
          saisieRelation.addActionListener(this);
          saisieElt2.removeActionListener(this);
          saisieElt2.effSelection();
          saisieElt2.addActionListener(this);
        }
        if (saisieElt1.pasdeSelection()) {
          if (saisieElt2.pasdeSelection()) etatSelectRien();
          else etatSelectElt2(saisieElt2.getSelection());
        } else {
          if (saisieElt2.pasdeSelection())
            etatSelectElt1(saisieElt1.getSelection());
          else
            checkEtatSelectElts(saisieElt1.getSelection(), saisieElt2.getSelection());
        }
        return;
      }
      if (evt.getSource()==saisieElt2) {
        if (!saisieRelation.pasdeSelection()) {
          saisieRelation.removeActionListener(this);
          saisieRelation.effSelection();
          saisieRelation.addActionListener(this);
          saisieElt1.removeActionListener(this);
          saisieElt1.effSelection();
          saisieElt1.addActionListener(this);
        }
        if (saisieElt2.pasdeSelection()) {
          if (saisieElt1.pasdeSelection()) etatSelectRien();
          else etatSelectElt1(saisieElt1.getSelection());
        } else {
          if (saisieElt1.pasdeSelection())
            etatSelectElt2(saisieElt2.getSelection());
          else
            checkEtatSelectElts(saisieElt1.getSelection(), saisieElt2.getSelection());
        }
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.paramString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  @Override
  public void traiterSourisEvent(SourisEvent evt) {
    switch (evt.getTypeEvent()) {
      case SELECT_RIEN:
        saisieElt1.removeActionListener(this);
        saisieElt1.effSelection();
        saisieElt1.addActionListener(this);
        saisieElt2.removeActionListener(this);
        saisieElt2.effSelection();
        saisieElt2.addActionListener(this);
        saisieRelation.removeActionListener(this);
        saisieRelation.effSelection();
        saisieRelation.addActionListener(this);
        etatSelectRien();
        return;
      case SELECT_ELEMENT:
        switch (evt.getMarquageElementVise()) {
          case FOND1:
            saisieRelation.setSelection(evt.getElementVise());
            return;
          case FOND2:
            saisieElt1.setSelection(evt.getElementVise());
            return;
          case FOND3:
            saisieElt2.setSelection(evt.getElementVise());
            return;
          case SELECT2:
          case SELECT1_2:
            forceEffSelectElt2();
            return;
          case SELECT3:
          case SELECT1_3:
            forceEffSelectElt1();
            return;
        }
        return;
      case SELECT_TEXTE:
        if (!saisieElt1.pasdeSelection()&&!saisieElt2.pasdeSelection()) {
          saisieElt1.removeActionListener(this);
          saisieElt1.effSelection();
          saisieElt1.addActionListener(this);
          saisieElt2.removeActionListener(this);
          saisieElt2.effSelection();
          saisieElt2.addActionListener(this);
          saisieRelation.removeActionListener(this);
          saisieRelation.effSelection();
          saisieRelation.addActionListener(this);
        }
        bornesSelectTexte = evt.getBornesSelectionTexte();
        etatSelectTexte();
        return;
    }
    throw new UnsupportedOperationException("Evénement non prévu : "+evt.getTypeEvent());
  }
  /*
   * ACTIONS DE L'UTILISATEUR : BOUTONS
   */
  private void creerRelation() {
    vue.getCorpus().addRelationSaisie((String) saisieTypeRelations.getTypeSelection(),
            saisieElt1.getSelection(), saisieElt2.getSelection());
  }
  private void supprimerRelation() {
    if (!GMessages.confirmationAction("Supprimer la relation sélectionnée ?"))
      return;
    vue.getCorpus().supRelation((Relation) saisieRelation.getSelection());
  }
  private void creerUniteElt1() {
    if (vue.getCorpus().checkPresenceUnite(saisieTypeElt1.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1])
            &&!GMessages.confirmationAction("Dupliquer cette unité  ?")) return;
    uniteCreeElt1 = true;
    vue.getCorpus().addUniteSaisie(saisieTypeElt1.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1]);
  }
  private void creerUniteElt2() {
    if (vue.getCorpus().checkPresenceUnite(saisieTypeElt2.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1])
            &&!GMessages.confirmationAction("Dupliquer cette unité  ?")) return;
    uniteCreeElt1 = false;
    vue.getCorpus().addUniteSaisie(saisieTypeElt2.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1]);
  }

  /* 
   * GESTION INTERNE
   */
  private void etatSelectRien() {   // rien n'est sélectionné
    if (saisieTypeRelations.pasdeSelection()) {
      if (saisieTypeElt1.pasdeSelection()) {
        if (saisieTypeElt2.pasdeSelection()) panneauTexte.pasDeMarquage();
        else
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
      } else {
        if (saisieTypeElt2.pasdeSelection())
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2);
        else
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2,
                  Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
      }
    } else {
      if (saisieTypeElt1.pasdeSelection()) {
        if (saisieTypeElt2.pasdeSelection())
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(saisieRelation.getElements()), TypeMarquage.FOND1);
        else
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(filtreElt2((Relation[]) saisieRelation.getElements())), TypeMarquage.FOND1,
                  Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
      } else {
        if (saisieTypeElt2.pasdeSelection())
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(filtreElt1((Relation[]) saisieRelation.getElements())), TypeMarquage.FOND1,
                  Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2);
        else
          panneauTexte.saisir(null,
                  Element.getUnitesSousjacentes(filtreElts((Relation[]) saisieRelation.getElements())), TypeMarquage.FOND1,
                  Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2,
                  Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
      }
    }
    panneauChamps.effacer();
    creeRelation.setEnabled(false);
    supprimeRelation.setEnabled(false);
    creeUniteElt1.setEnabled(false);
    creeUniteElt2.setEnabled(false);
  }
  private void etatSelectTexte() {   // du texte est sélectionné
    if (saisieElt1.pasdeSelection()) {
      if (saisieElt2.pasdeSelection()) panneauTexte.pasDeMarquage();
      else
        panneauTexte.saisir(null,
                saisieElt2.getSelection().getUnitesSousjacentes(), TypeMarquage.SELECT3);
    } else {
      if (saisieElt2.pasdeSelection())
        panneauTexte.saisir(null,
                saisieElt1.getSelection().getUnitesSousjacentes(), TypeMarquage.SELECT2);
      else // au moins un des 2 elt n'est pas sélectionné
        throw new UnsupportedOperationException("Cas non prévu");
    }
    panneauChamps.effacer();
    creeRelation.setEnabled(false);
    supprimeRelation.setEnabled(false);
    if (!saisieTypeElt1.pasdeSelection()&&saisieTypeElt1.getClasseSelection()==Unite.class
            &&saisieElt1.pasdeSelection())
      creeUniteElt1.setEnabled(true);
    if (!saisieTypeElt2.pasdeSelection()&&saisieTypeElt2.getClasseSelection()==Unite.class
            &&saisieElt2.pasdeSelection())
      creeUniteElt2.setEnabled(true);
  }
  private void etatSelectElt1(Element elt1) { // seul Elt1 a été choisi
    if (saisieTypeElt2.pasdeSelection()) {
      if (saisieTypeRelations.pasdeSelection())
        panneauTexte.saisir(elt1.getUnite0(), elt1.getUnitesSousjacentes(), TypeMarquage.SELECT2);
      else
        panneauTexte.saisir(elt1.getUnite0(), elt1.getUnitesSousjacentes(), TypeMarquage.SELECT2,
                Element.getUnitesSousjacentes(filtreElt1((Relation[]) saisieRelation.getElements(), elt1)),
                TypeMarquage.FOND1);
    } else {
      if (saisieTypeRelations.pasdeSelection())
        panneauTexte.saisir(elt1.getUnite0(), elt1.getUnitesSousjacentes(), TypeMarquage.SELECT2,
                Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
      else
        panneauTexte.saisir(elt1.getUnite0(), elt1.getUnitesSousjacentes(), TypeMarquage.SELECT2,
                Element.getUnitesSousjacentes(filtreElt1(filtreElt2((Relation[]) saisieRelation.getElements()), elt1)),
                TypeMarquage.FOND1,
                Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
    }
    panneauChamps.afficher(elt1);
    creeRelation.setEnabled(false);
    supprimeRelation.setEnabled(false);
    creeUniteElt1.setEnabled(false);
    creeUniteElt2.setEnabled(false);
  }
  private void etatSelectElt2(Element elt2) {  // seul Elt2 a été choisi
    if (saisieTypeElt1.pasdeSelection()) {
      if (saisieTypeRelations.pasdeSelection())
        panneauTexte.saisir(elt2.getUnite0(), elt2.getUnitesSousjacentes(), TypeMarquage.SELECT3);
      else
        panneauTexte.saisir(elt2.getUnite0(), elt2.getUnitesSousjacentes(), TypeMarquage.SELECT3,
                Element.getUnitesSousjacentes(filtreElt2((Relation[]) saisieRelation.getElements(), elt2)),
                TypeMarquage.FOND1);
    } else {
      if (saisieTypeRelations.pasdeSelection())
        panneauTexte.saisir(elt2.getUnite0(), elt2.getUnitesSousjacentes(), TypeMarquage.SELECT3,
                Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2);
      else
        panneauTexte.saisir(elt2.getUnite0(), elt2.getUnitesSousjacentes(), TypeMarquage.SELECT3,
                Element.getUnitesSousjacentes(filtreElt2(filtreElt1((Relation[]) saisieRelation.getElements()), elt2)),
                TypeMarquage.FOND1,
                Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2);
    }
    panneauChamps.afficher(elt2);
    creeRelation.setEnabled(false);
    supprimeRelation.setEnabled(false);
    creeUniteElt1.setEnabled(false);
    creeUniteElt2.setEnabled(false);
  }
  private void etatSelectElts(Element elt1, Element elt2) { // Elt1 et Elt2 ont été choisis et ils ne forment pas encore de relation
    panneauTexte.saisir(elt1.getUnite0(), elt1.getUnitesSousjacentes(), TypeMarquage.SELECT2,
            elt2.getUnitesSousjacentes(), TypeMarquage.SELECT3);
    panneauChamps.effacer();
    if (!saisieTypeRelations.pasdeSelection()) {
      creeRelation.setEnabled(true);
//      Boutons.setDefaut(creeRelation, true);
    }
    supprimeRelation.setEnabled(false);
    creeUniteElt1.setEnabled(false);
    creeUniteElt2.setEnabled(false);
  }
  private void etatSelectRel(Relation rel) {  // une relation est sélectionnée
    panneauTexte.saisir(rel.getElt1().getUnite0(),
            rel.getElt1().getUnitesSousjacentes(), TypeMarquage.SELECT1_2,
            rel.getElt2().getUnitesSousjacentes(), TypeMarquage.SELECT1_3);
//            Element.getUnitesSousjacentes(saisieElt1.getElements()), TypeMarquage.FOND2,
//            Element.getUnitesSousjacentes(saisieElt2.getElements()), TypeMarquage.FOND3);
    panneauChamps.afficher(rel);
    creeRelation.setEnabled(false);
    supprimeRelation.setEnabled(true);
    creeUniteElt1.setEnabled(false);
    creeUniteElt2.setEnabled(false);
  }
  private Relation[] filtreElt1(Relation[] relations) {
    Class classe = saisieTypeElt1.getClasseSelection();
    String type = saisieTypeElt1.getTypeSelection();
    ArrayList<Integer> indsAOter = new ArrayList<Integer>();
    for (int i = 0; i<relations.length; i++) {
      Element elt1 = relations[i].getElt1();
      if (elt1.getClass()!=classe||!elt1.getType().equals(type))
        indsAOter.add(i);
    }
    return Tableaux.oterRelations(relations, indsAOter);
  }
  private Relation[] filtreElt1(Relation[] relations, Element elt1) {
    ArrayList<Relation> rels = new ArrayList<Relation>();
    for (Relation rel : relations) if (elt1==rel.getElt1()) rels.add(rel);
    return rels.toArray(new Relation[0]);
  }
  private Relation[] filtreElt2(Relation[] relations) {
    Class classe = saisieTypeElt2.getClasseSelection();
    String type = saisieTypeElt2.getTypeSelection();
    ArrayList<Integer> indsAOter = new ArrayList<Integer>();
    for (int i = 0; i<relations.length; i++) {
      Element elt2 = relations[i].getElt2();
      if (elt2.getClass()!=classe||!elt2.getType().equals(type))
        indsAOter.add(i);
    }
    return Tableaux.oterRelations(relations, indsAOter);
  }
  private Relation[] filtreElt2(Relation[] relations, Element elt2) {
    ArrayList<Relation> rels = new ArrayList<Relation>();
    for (Relation rel : relations) if (elt2==rel.getElt2()) rels.add(rel);
    return rels.toArray(new Relation[0]);
  }
  private Relation[] filtreElts(Relation[] relations) {
    Class classe1 = saisieTypeElt1.getClasseSelection();
    String type1 = saisieTypeElt1.getTypeSelection();
    Class classe2 = saisieTypeElt2.getClasseSelection();
    String type2 = saisieTypeElt2.getTypeSelection();
    ArrayList<Integer> indsAOter = new ArrayList<Integer>();
    for (int i = 0; i<relations.length; i++) {
      Element elt1 = relations[i].getElt1();
      Element elt2 = relations[i].getElt2();
      if (elt1.getClass()!=classe1||!elt1.getType().equals(type1)
              ||elt2.getClass()!=classe2||!elt2.getType().equals(type2))
        indsAOter.add(i);
    }
    return Tableaux.oterRelations(relations, indsAOter);
  }
  private void checkEtatSelectElts(Element elt1, Element elt2) {
    if (!saisieTypeRelations.pasdeSelection())
      for (Relation rel : (Relation[]) saisieRelation.getElements()) {
        if (rel.getElt1()==elt1&&rel.getElt2()==elt2) {
          saisieRelation.setSelection(rel);
          return;
        }
      }
    etatSelectElts(elt1, elt2);
  }
  private void forceEtatSelectRel(Relation rel) {
    if (saisieTypeElt1.pasdeSelection()||saisieTypeElt1.getClasseSelection()!=rel.getElt1().getClass()
            ||!saisieTypeElt1.getTypeSelection().equals(rel.getElt1().getType())) {
      saisieTypeElt1.removeActionListener(this);
      saisieTypeElt1.setSelection(rel.getElt1().getClass(), rel.getElt1().getType());
      saisieTypeElt1.addActionListener(this);
    }
    initSaisieElt1(rel.getElt1());
    if (saisieTypeElt2.pasdeSelection()||saisieTypeElt2.getClasseSelection()!=rel.getElt2().getClass()
            ||!saisieTypeElt2.getTypeSelection().equals(rel.getElt2().getType())) {
      saisieTypeElt2.removeActionListener(this);
      saisieTypeElt2.setSelection(rel.getElt2().getClass(), rel.getElt2().getType());
      saisieTypeElt2.addActionListener(this);
    }
    initSaisieElt2(rel.getElt2());
    etatSelectRel(rel);
  }
  private void forceEffSelectElt1() {
    if (saisieElt1.pasdeSelection()) return;
    if (!saisieRelation.pasdeSelection()) {
      saisieRelation.removeActionListener(this);
      saisieRelation.effSelection();
      saisieRelation.addActionListener(this);
    }
    saisieElt1.effSelection();
  }
  private void forceEffSelectElt2() {
    if (saisieElt2.pasdeSelection()) return;
    if (!saisieRelation.pasdeSelection()) {
      saisieRelation.removeActionListener(this);
      saisieRelation.effSelection();
      saisieRelation.addActionListener(this);
    }
    saisieElt2.effSelection();
  }
  private void forceEffSelectRel() {
    if (saisieRelation.pasdeSelection()) return;
    saisieRelation.removeActionListener(this);
    saisieRelation.effSelection();
    saisieRelation.addActionListener(this);
  }
  private void initSaisieRelation(Relation rel) {
    saisieRelation.removeActionListener(this);
    if (saisieTypeRelations.pasdeSelection()) saisieRelation.setModeleVide();
    else {
      String type0 = saisieTypeRelations.getTypeSelection();
      Relation[] relations = vue.getRelationsAVoir(type0);
      saisieRelation.setModele(relations, vue.getIdRelations(type0, relations));
    }
    if (rel==null) saisieRelation.effSelection();
    else saisieRelation.setSelection(rel);
    saisieRelation.addActionListener(this);
    saisieRelation.revalidate();
  }
  private void initSaisieRelation() {
    initSaisieRelation(null);
  }
  private void initSaisieElt1(Element elt1) {
    saisieElt1.removeActionListener(this);
    if (saisieTypeElt1.pasdeSelection()) saisieElt1.setModeleVide();
    else {
      Class<? extends Element> classe = saisieTypeElt1.getClasseSelection();
      String type = saisieTypeElt1.getTypeSelection();
      saisieElt1.setModele(vue.getElements(classe, type), vue.getIdElements(classe, type));
    }
    if (elt1==null) saisieElt1.effSelection();
    else saisieElt1.setSelection(elt1);
    saisieElt1.addActionListener(this);
    saisieElt1.revalidate();
  }
  private void initSaisieElt1() {
    initSaisieElt1(null);
  }
  private void initSaisieElt2(Element elt2) {
    saisieElt2.removeActionListener(this);
    if (saisieTypeElt2.pasdeSelection()) saisieElt2.setModeleVide();
    else {
      Class<? extends Element> classe = saisieTypeElt2.getClasseSelection();
      String type = saisieTypeElt2.getTypeSelection();
      saisieElt2.setModele(vue.getElements(classe, type), vue.getIdElements(classe, type));
    }

    if (elt2==null) saisieElt2.effSelection();
    else saisieElt2.setSelection(elt2);
    saisieElt2.addActionListener(this);
    saisieElt2.revalidate();
  }
  private void initSaisieElt2() {
    initSaisieElt2(null);
  }
}