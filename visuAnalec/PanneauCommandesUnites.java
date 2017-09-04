/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
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
public class PanneauCommandesUnites extends PanneauCommandes implements ActionListener {
  private PanneauChamps panneauChamps;
  public TypeComboBox saisieTypeUnites;
  private ElementComboBox saisieUnite;
  private JButton creeUnite;
  private JButton supprimeUnite;
  private JButton rectifieBorne;
  private int[] bornesSelectTexte;

  /*
   * CREATION PAR LE PANNEAU EDITEUR
   */
  PanneauCommandesUnites(Vue vue, PanneauAffTexte pTexte, PanneauChamps pChamps) {
    super(vue, pTexte);
    JToolBar barre1 = new JToolBar("Gestion des unités");
    barre1.setPreferredSize(new Dimension(0, 22));
//    barre1.setBackground(PanneauTexte.getCouleurFond1());
    barre1.setAlignmentX(0.0f);
    add(barre1);
    panneauChamps = pChamps;
    saisieTypeUnites = new TypeComboBox(Unite.class);
    saisieTypeUnites.addActionListener(this);
    saisieUnite = new ElementComboBox();
    saisieUnite.addActionListener(this);
    creeUnite = new JButton(new GAction("Créer") {
      public void executer() {
        creerUnite();
      }
    });
    supprimeUnite = new JButton(new GAction("Supprimer") {
      public void executer() {
        supprimerUnite();
      }
    });
    rectifieBorne = new JButton(new GAction("Rectifier une borne") {
      public void executer() {
        modifierBorne();
      }
    });
    barre1.addSeparator();
    barre1.add(new TitreBarre("Unité : ", PanneauTexte.getCouleurFond1()));
    barre1.addSeparator();
    barre1.add(saisieTypeUnites);
    barre1.add(saisieUnite);
    barre1.addSeparator();
    barre1.add(creeUnite);
    barre1.addSeparator();
    barre1.add(supprimeUnite);
    barre1.addSeparator();
    barre1.add(rectifieBorne);
  }

  /*
   * COMMANDES UTILISEES PAR LE PANNEAU EDITEUR
   */
  public void reinitialiser() {
    // typiquement, appelé par le panneau éditeur à la suite d'une modif de vue ou de structure
    saisieTypeUnites.removeActionListener(this);
    saisieTypeUnites.setModele(vue.getTypesUnitesAVoir());
    saisieTypeUnites.effSelection();
    saisieTypeUnites.addActionListener(this);
    supprimeUnite.setEnabled(false);
    rectifieBorne.setEnabled(false);
    initSaisieUnite();
    etatSelectRien();
    revalidate();
    repaint();
  }
  public void reafficher() {
    // potentiellement appelé par le panneau éditeur ?
    // appelé aussi par  traiterElementEvent
    if (saisieUnite.pasdeSelection()) {
      initSaisieUnite();
      etatSelectRien();
    } else {
      Unite uniteCourante = (Unite) saisieUnite.getSelection();
      initSaisieUnite(uniteCourante);
      etatSelectUnite(uniteCourante);
    }
  }
  public void elementSuivant(int sens) {
    if (sens>0) saisieUnite.gotoSuivant();
    else saisieUnite.gotoPrecedent();
  }
  public Element elementSelectionne() {
    if (saisieUnite.pasdeSelection()) return null;
    return saisieUnite.getSelection();
  }
  public void editerElement(Element elt) {
    // appelé par le panneauEditeur : elt est forcément une unité
    if (elt.getClass()!=Unite.class)
      throw new UnsupportedOperationException("Erreur : classe incorrecte");
    editerUnite((Unite) elt);
  }
  public ArrayList<ElementMarque> trouverElementsVises(Unite uniteVisee, TypeMarquage marqueUniteVisee,
          boolean avecId) {
    ArrayList<ElementMarque> eltsMarques = new ArrayList<ElementMarque>();
    eltsMarques.add(new ElementMarque(uniteVisee, marqueUniteVisee,
            avecId ? saisieUnite.getIdElement(uniteVisee) : null));
    return eltsMarques;
  }
  public void traiterElementEvent(ElementEvent evt) {
    // relayé par le panneau éditeur 
    switch (evt.getModif()) {
      case AJOUT_UNITE:
        Unite unite = (Unite) evt.getElement();
        editerUnite(unite);
        return;
      case SUP_UNITE:
        if (evt.getElement()==saisieUnite.getSelection()) {
          initSaisieUnite();
          etatSelectRien();
        } else {
          reafficher();
        }
        return;
      case BORNES_UNITE:
        return;  // traité directement par le panneau Texte : à revoir ?
      case AJOUT_RELATION:
      case AJOUT_SCHEMA:
      case SUP_RELATION:
      case MODIF_SCHEMA:
      case SUP_SCHEMA:
      case MODIF_VALEUR:
        reafficher();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getModif()+" oublié dans un switch");
    }
  }
  void editerUnite(Unite unite) {
    // appelé  par traiterElementEvent, 
    // et aussi directement par le panneau éditeur quand il est sollicité par d'autres fenêtres
    if (!unite.getType().equals(saisieTypeUnites.getTypeSelection())) {
      saisieTypeUnites.removeActionListener(this);
      saisieTypeUnites.setSelection(unite.getType());
      saisieTypeUnites.addActionListener(this);
    }
    initSaisieUnite(unite);
    etatSelectUnite(unite);
  }
  /*
   * ACTIONS DE L'UTILISATEUR : CHANGEMENTS D'ETAT
   */
  public void actionPerformed(ActionEvent evt) {
    try {
      if (evt.getSource()==saisieTypeUnites) {
        initSaisieUnite();
        if (bornesSelectTexte==null) etatSelectRien();
        else {
          panneauTexte.getCaret().setSelectionVisible(true);
          etatSelectTexte();
        }
        return;
      }
      if (evt.getSource()==saisieUnite) {
        if (saisieUnite.pasdeSelection()) etatSelectRien();
        else etatSelectUnite((Unite) saisieUnite.getSelection());
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
        saisieUnite.effSelection();
        bornesSelectTexte = null;
        return;
      case SELECT_TEXTE:
        saisieUnite.removeActionListener(this);
        saisieUnite.effSelection();
        saisieUnite.addActionListener(this);
        bornesSelectTexte = evt.getBornesSelectionTexte();
        etatSelectTexte();
        return;
      case SELECT_ELEMENT:
        saisieUnite.setSelection(evt.getElementVise());
        bornesSelectTexte = null;
        return;
    }
    throw new UnsupportedOperationException("Evénement non prévu : "+evt.getTypeEvent());
  }
  /*
   * ACTIONS DE L'UTILISATEUR : BOUTONS
   */
  private void creerUnite() {
    if (vue.getCorpus().checkPresenceUnite(saisieTypeUnites.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1])
            &&!GMessages.confirmationAction("Dupliquer cette unité  ?")) return;
    vue.getCorpus().addUniteSaisie(saisieTypeUnites.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1]);
  }
  private void supprimerUnite() {
    if (!GMessages.confirmationAction("Supprimer l'unité sélectionnée ?"))
      return;
    vue.getCorpus().supUnite((Unite) saisieUnite.getSelection());
  }
  private void modifierBorne() {
    panneauTexte.rectifierBorneUnite((Unite) saisieUnite.getSelection());
  }
  /* 
   * GESTION INTERNE
   */
  private void etatSelectUnite(Unite unite) {  // une unité est sélectionnée
    panneauTexte.saisir(unite, new Unite[]{unite}, TypeMarquage.SELECT1);
    //           (Unite[]) saisieUnite.getElements(), TypeMarquage.FOND1);
    panneauChamps.afficher(unite);
    creeUnite.setEnabled(false);
    supprimeUnite.setEnabled(true);
    rectifieBorne.setEnabled(true);
  }
  private void etatSelectRien() {  // rien n'est sélectionné
    if (saisieTypeUnites.pasdeSelection()) panneauTexte.pasDeMarquage();
    else
      panneauTexte.saisir(null, (Unite[]) saisieUnite.getElements(), TypeMarquage.FOND1);
    panneauChamps.effacer();
    creeUnite.setEnabled(false);
    supprimeUnite.setEnabled(false);
    rectifieBorne.setEnabled(false);
  }
  private void etatSelectTexte() {    // du texte est sélectionné
    panneauTexte.pasDeMarquage();
//    if (saisieTypeUnites.pasdeSelection()) panneauTexte.pasDeMarquage();
//    else panneauTexte.saisir(null, (Unite[]) saisieUnite.getElements(), TypeMarquage.FOND1);
    panneauChamps.effacer();
    if (!saisieTypeUnites.pasdeSelection()) {
      creeUnite.setEnabled(true);
//      Boutons.setDefaut(creeUnite, true);
    }
    supprimeUnite.setEnabled(false);
    rectifieBorne.setEnabled(false);
  }
  private void initSaisieUnite(Unite unit) {
    saisieUnite.removeActionListener(this);
    if (saisieTypeUnites.pasdeSelection()) saisieUnite.setModeleVide();
    else {
      String type0 = saisieTypeUnites.getTypeSelection();
      Unite[] unites = vue.getUnitesAVoir(type0);
      saisieUnite.setModele(unites, vue.getIdUnites(type0, unites));
    }
    if (unit==null) saisieUnite.effSelection();
    else saisieUnite.setSelection(unit);
    saisieUnite.addActionListener(this);
    saisieUnite.revalidate();
  }
  private void initSaisieUnite() {
    initSaisieUnite(null);
  }
}
