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
public class PanneauCommandesSchemas extends PanneauCommandes implements ActionListener {
  private PanneauChamps panneauChamps;
  private TypeComboBox saisieTypeSchemas;
  private ElementComboBox saisieSchema;
  private TypeComboBox saisieTypeElt;
  private ElementComboBox saisieElt;
  private JButton creeSchema;
  private JButton supprimeSchema;
  private JButton ajouteElement;
  private JButton oteElement;
  private JButton creeUniteElt;
  private int[] bornesSelectTexte;
  /*
  
   * CREATION PAR LE PANNEAU EDITEUR
   */
  PanneauCommandesSchemas(Vue vue, PanneauAffTexte pTexte, PanneauChamps pChamps) {
    super(vue, pTexte);
    JToolBar barre1 = new JToolBar();
    barre1.setPreferredSize(new Dimension(0, 22));
//    barre1.setBackground(PanneauTexte.getCouleurFond1());
    barre1.setAlignmentX(0.0f);
    //    barre1.setFloatable(false);
    add(barre1);
    JToolBar barre2 = new JToolBar();
    barre2.setPreferredSize(new Dimension(0, 22));
//    barre2.setBackground(PanneauTexte.getCouleurFond2());
    barre2.setAlignmentX(0.0f);
//     barre2.setFloatable(false);
    add(barre2);
    panneauChamps = pChamps;
    saisieTypeSchemas = new TypeComboBox(Schema.class);
    saisieTypeSchemas.addActionListener(this);
    saisieSchema = new ElementComboBox();
    saisieSchema.addActionListener(this);
    saisieTypeElt = new TypeComboBox(Element.class);
    saisieTypeElt.addActionListener(this);
    saisieElt = new ElementComboBox();
    saisieElt.addActionListener(this);
    creeSchema = new JButton(new GAction("Créer") {
      public void executer() {
        creerSchema();
      }
    });
    supprimeSchema = new JButton(new GAction("Supprimer") {
      public void executer() {
        supprimerSchema();
      }
    });
    ajouteElement = new JButton(new GAction("Ajouter") {
      public void executer() {
        ajouterElement();
      }
    });
    oteElement = new JButton(new GAction("Oter") {
      public void executer() {
        oterElement();
      }
    });
    creeUniteElt = new JButton(new GAction("Créer") {
      public void executer() {
        creerUniteElt();
      }
    });
    barre1.addSeparator();
    barre1.add(new TitreBarre("Schéma : ", PanneauTexte.getCouleurFond1()));
    barre1.addSeparator();
    barre1.add(saisieTypeSchemas);
    barre1.add(saisieSchema);
    barre1.addSeparator();
    barre1.add(creeSchema);
    barre1.addSeparator();
    barre1.add(supprimeSchema);
    barre2.addSeparator();
    barre2.add(new TitreBarre("Elément : ", PanneauTexte.getCouleurFond2()));
    barre2.addSeparator();
    barre2.add(saisieTypeElt);
    barre2.add(saisieElt);
    barre2.addSeparator();
    barre2.add(ajouteElement);
    barre2.add(oteElement);
    barre2.add(creeUniteElt);
  }

  /*
   * COMMANDES UTILISEES PAR LE PANNEAU EDITEUR
   */
  public void reinitialiser() {
    // typiquement, appelé par le panneau éditeur à la suite d'une modif de vue ou de structure
    saisieTypeSchemas.removeActionListener(this);
    saisieTypeSchemas.setModele(vue.getTypesSchemasAVoir());
    saisieTypeSchemas.effSelection();
    saisieTypeSchemas.addActionListener(this);
    saisieTypeElt.removeActionListener(this);
    saisieTypeElt.setModele(vue.getTypes(Unite.class), vue.getTypes(Relation.class), vue.getTypes(Schema.class));
    saisieTypeElt.effSelection();
    saisieTypeElt.addActionListener(this);
    initSaisieSchema();
    initSaisieElt();
    etatSelectRien();
    revalidate();
    repaint();
  }
  public void reafficher() {
    // potentiellement appelé par le panneau éditeur ?
    // appelé aussi par  traiterElementEvent
    if (saisieSchema.pasdeSelection()) {
      initSaisieSchema();
      if (saisieElt.pasdeSelection()) {
        initSaisieElt();
        etatSelectRien();
      } else {
        Element eltCourant = saisieElt.getSelection();
        initSaisieElt(eltCourant);
        etatSelectElt(eltCourant);
      }
    } else {
      Schema schCourant = (Schema) saisieSchema.getSelection();
      initSaisieSchema(schCourant);
      if (saisieElt.pasdeSelection()) {
        initSaisieElt();
        etatSelectSchema(schCourant);
      } else {
        Element eltCourant = saisieElt.getSelection();
        initSaisieElt(eltCourant);
        etatSelectSchemaElt(schCourant, eltCourant);
      }
    }
  }
   public void elementSuivant(int sens) {
    if (sens>0) saisieSchema.gotoSuivant();
    else saisieSchema.gotoPrecedent();
  }
 public Element elementSelectionne() {
    if (saisieSchema.pasdeSelection()) return null;
    return saisieSchema.getSelection();
  }
  public void editerElement(Element elt) {
    // appelé par le panneauEditeur : elt est forcément un schéma
    if (elt.getClass() != Schema.class) throw new UnsupportedOperationException("Erreur : classe incorrecte");
    editerSchema((Schema) elt);
  }
  public void traiterElementEvent(ElementEvent evt) {
    switch (evt.getModif()) {
      case AJOUT_SCHEMA:
      case MODIF_SCHEMA:
        Schema sch = (Schema) evt.getElement();
        editerSchema(sch);
        return;
      case SUP_SCHEMA:
        if (evt.getElement() == saisieSchema.getSelection()) {
          initSaisieSchema();
          initSaisieElt();
          etatSelectRien();
        } else {
          reafficher();
        }
        return;
      case AJOUT_UNITE:
        if (saisieTypeElt.getClasseSelection() == Unite.class ||
                evt.getElement().getType().equals(saisieTypeElt.getTypeSelection())) {
          initSaisieElt(evt.getElement());
        }
        reafficher();
        return;
      case SUP_UNITE:
      case SUP_RELATION:
      case BORNES_UNITE:
      case AJOUT_RELATION:
      case MODIF_VALEUR:
        reafficher();
        return;
      default:
        throw new UnsupportedOperationException("Cas " + evt.getModif() + " oublié dans un switch");
    }
  }
  public void editerSchema(Schema sch) {
    // appelé  par traiterElementEvent, 
    // et aussi directement par le panneau éditeur quand il est sollicité par d'autres fenêtres
    if (!sch.getType().equals(saisieTypeSchemas.getTypeSelection())) {
      saisieTypeSchemas.removeActionListener(this);
      saisieTypeSchemas.setSelection(sch.getType());
      saisieTypeSchemas.addActionListener(this);
    }
    initSaisieSchema(sch);
    saisieSchema.setSelection(sch);
  }
  public void editerSchemaElement(Schema sch, Element elt) {
    // appelé  par le panneau éditeur  sollicité par VisuChaines 
    if (!sch.getType().equals(saisieTypeSchemas.getTypeSelection())) {
      saisieTypeSchemas.removeActionListener(this);
      saisieTypeSchemas.setSelection(sch.getType());
      saisieTypeSchemas.addActionListener(this);
    }
    initSaisieSchema(sch);
    forceElt(elt);
  }
 public void editerElementTypeSchema(Element elt, String typesch) {
    // appelé  par le panneau éditeur  sollicité par VisuChaines 
    if (!typesch.equals(saisieTypeSchemas.getTypeSelection())) {
      saisieTypeSchemas.removeActionListener(this);
      saisieTypeSchemas.setSelection(typesch);
      saisieTypeSchemas.addActionListener(this);
    }
    initSaisieSchema();
    forceElt(elt);
  }
  public ArrayList<ElementMarque> trouverElementsVises(Unite uniteVisee, TypeMarquage marqueUniteVisee,
          boolean avecId) {
    ArrayList<ElementMarque> eltsMarques = new ArrayList<ElementMarque>();
    ArrayList<Element> eltsVises;
    Element eltVise;
    switch (marqueUniteVisee) {
      case FOND1:
        eltsVises = Element.trouverElementsParUniteSousjacente(saisieSchema.getElements(), uniteVisee);
        for (Element elt : eltsVises) eltsMarques.add(new ElementMarque(
                  elt, marqueUniteVisee, avecId ? saisieSchema.getIdElement(elt) : null));
        return eltsMarques;
      case FOND1_2:
      case FOND2:
        eltsVises = Element.trouverElementsParUniteSousjacente(saisieElt.getElements(), uniteVisee);
        for (Element elt : eltsVises) eltsMarques.add(new ElementMarque(
                  elt, marqueUniteVisee, avecId ? saisieElt.getIdElement(elt) : null));
        return eltsMarques;
      case SELECT1:
        eltVise = saisieSchema.getSelection();
        eltsMarques.add(new ElementMarque(
                  eltVise, marqueUniteVisee, avecId ? saisieSchema.getIdElement(eltVise) : null));
        return eltsMarques;
      case SELECT1_2:
      case SELECT2:
        eltVise = saisieElt.getSelection();
        eltsMarques.add(new ElementMarque(
                eltVise, marqueUniteVisee, avecId ? saisieElt.getIdElement(eltVise) : null));
        return eltsMarques;       
      default:
        throw new UnsupportedOperationException("Marquage non prévu : " + marqueUniteVisee);

    }
  }
  /*
   * ACTIONS DE L'UTILISATEUR : CHANGEMENTS D'ETAT
   */
  public void actionPerformed(ActionEvent evt) {
    try {
      if (evt.getSource() == saisieTypeSchemas) {
        initSaisieSchema();
        if (saisieElt.pasdeSelection()) etatSelectRien();
        else etatSelectElt(saisieElt.getSelection());
        return;
      }
      if (evt.getSource() == saisieTypeElt) {
        initSaisieElt();
        if (saisieSchema.pasdeSelection()) etatSelectRien();
        else etatSelectSchema((Schema) saisieSchema.getSelection());
        return;
      }
      if (evt.getSource() == saisieSchema) {
        if (saisieSchema.pasdeSelection()) {
          if (saisieElt.pasdeSelection()) etatSelectRien();
          else etatSelectElt(saisieElt.getSelection());
        } else {
          if (saisieElt.pasdeSelection()) etatSelectSchema((Schema) saisieSchema.getSelection());
          else etatSelectSchemaElt((Schema) saisieSchema.getSelection(), saisieElt.getSelection());
        }
        return;
      }
      if (evt.getSource() == saisieElt) {
        if (saisieElt.pasdeSelection()) {
          if (saisieSchema.pasdeSelection()) etatSelectRien();
          else etatSelectSchema((Schema) saisieSchema.getSelection());
        } else {
          if (saisieSchema.pasdeSelection()) etatSelectElt(saisieElt.getSelection());
          else etatSelectSchemaElt((Schema) saisieSchema.getSelection(), saisieElt.getSelection());
        }
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : " + evt.paramString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  @Override
  public void traiterSourisEvent(SourisEvent evt) {
    switch (evt.getTypeEvent()) {
      case SELECT_RIEN:
        saisieElt.removeActionListener(this);
        saisieElt.effSelection();
        saisieElt.addActionListener(this);
        saisieSchema.effSelection();
        return;
      case SELECT_ELEMENT:
        switch (evt.getMarquageElementVise()) {
          case SELECT1:
            forceElt(evt.getElementVise());
            return;
          case SELECT2:
          case SELECT1_2:
            saisieSchema.effSelection();
            return;
          case FOND1:
            saisieSchema.setSelection(evt.getElementVise());
            return;
          case FOND2:
          case FOND1_2:
            saisieElt.setSelection(evt.getElementVise());
            return;
          default:
            throw new UnsupportedOperationException("Marquage non prévu : " + evt.getTypeEvent());
        }
      case SELECT_TEXTE:
        saisieElt.removeActionListener(this);
        saisieElt.effSelection();
        saisieElt.addActionListener(this);
        saisieSchema.removeActionListener(this);
        saisieSchema.effSelection();
        saisieSchema.addActionListener(this);
        bornesSelectTexte = evt.getBornesSelectionTexte();
        etatSelectTexte();
        return;
    }
    throw new UnsupportedOperationException("Evénement non prévu : " + evt.getTypeEvent());
  }

  /*
   * ACTIONS DE L'UTILISATEUR : BOUTONS
   */
  private void creerSchema() {
    vue.getCorpus().addSchemaSaisi(saisieTypeSchemas.getTypeSelection(), saisieElt.getSelection());
  }
  private void creerUniteElt() {
    if (vue.getCorpus().checkPresenceUnite(saisieTypeElt.getTypeSelection(), bornesSelectTexte[0], bornesSelectTexte[1]) &&
            !GMessages.confirmationAction("Dupliquer cette unité  ?")) return;
    vue.getCorpus().addUniteSaisie(saisieTypeElt.getTypeSelection(),  bornesSelectTexte[0], bornesSelectTexte[1]);
  }
  private void supprimerSchema() {
    if (!GMessages.confirmationAction("Supprimer le schéma sélectionné ?")) return;
    vue.getCorpus().supSchema((Schema) saisieSchema.getSelection());
  }
  private void ajouterElement() {
    vue.getCorpus().ajouterElementSchemaSaisi(saisieElt.getSelection(), (Schema) saisieSchema.getSelection());
  }
  private void oterElement() {
    vue.getCorpus().oterElementSchema(saisieElt.getSelection(), (Schema) saisieSchema.getSelection());
  }

  /* 
   * GESTION INTERNE
   */
  private void etatSelectRien() { // ni Sch ni Elt n'ont été saisis
    if (saisieTypeElt.pasdeSelection()) {
      if (saisieTypeSchemas.pasdeSelection()) panneauTexte.pasDeMarquage();
      else panneauTexte.saisir(null,
                Element.getUnitesSousjacentes(saisieSchema.getElements()), TypeMarquage.FOND1);
    } else {
      if (saisieTypeSchemas.pasdeSelection()) panneauTexte.saisir(null,
                Element.getUnitesSousjacentes(saisieElt.getElements()), TypeMarquage.FOND2);
      else panneauTexte.saisir(null,
                Element.getUnitesSousjacentes(saisieSchema.getElements()), TypeMarquage.FOND1,
                Element.getUnitesSousjacentes(saisieElt.getElements()), TypeMarquage.FOND2);
    }
    panneauChamps.effacer();
    creeSchema.setEnabled(false);
    supprimeSchema.setEnabled(false);
    ajouteElement.setEnabled(false);
    oteElement.setEnabled(false);
    creeUniteElt.setEnabled(false);
  }
  private void etatSelectTexte() { // du texte a été sélectionné
    if (saisieTypeElt.pasdeSelection()) {
      if (saisieTypeSchemas.pasdeSelection()) panneauTexte.pasDeMarquage();
      else panneauTexte.saisir(null,
                Element.getUnitesSousjacentes(saisieSchema.getElements()), TypeMarquage.FOND1);
    } else {
      if (saisieTypeSchemas.pasdeSelection()) panneauTexte.saisir(null,
                Element.getUnitesSousjacentes(saisieElt.getElements()), TypeMarquage.FOND2);
      else panneauTexte.saisir(null,
                Element.getUnitesSousjacentes(saisieSchema.getElements()), TypeMarquage.FOND1,
                Element.getUnitesSousjacentes(saisieElt.getElements()), TypeMarquage.FOND2);
    }
    panneauChamps.effacer();
    creeSchema.setEnabled(false);
    supprimeSchema.setEnabled(false);
    ajouteElement.setEnabled(false);
    oteElement.setEnabled(false);
    if (!saisieTypeElt.pasdeSelection() && saisieTypeElt.getClasseSelection() == Unite.class)
      creeUniteElt.setEnabled(true);
  }
  private void etatSelectElt(Element elt) {  // seul Elt a été choisi 
    if (saisieTypeSchemas.pasdeSelection()) panneauTexte.saisir(elt.getUnite0(),
              elt.getUnitesSousjacentes(), TypeMarquage.SELECT2);
    else panneauTexte.saisir(elt.getUnite0(), elt.getUnitesSousjacentes(), TypeMarquage.SELECT2,
              Element.getUnitesSousjacentes(saisieSchema.getElements()), TypeMarquage.FOND1);
    panneauChamps.afficher(elt);
    if (!saisieTypeSchemas.pasdeSelection()) {
      creeSchema.setEnabled(true);
      Boutons.setDefaut(creeSchema, true);
    }
    supprimeSchema.setEnabled(false);
    ajouteElement.setEnabled(false);
    oteElement.setEnabled(false);
    creeUniteElt.setEnabled(false);
  }
  private void etatSelectSchema(Schema sch) { // seul Sch a été choisi
    if (saisieTypeElt.pasdeSelection()) panneauTexte.saisir(sch.getUnite0(),
              sch.getUnitesSousjacentes(), TypeMarquage.SELECT1);
    else panneauTexte.saisir(sch.getUnite0(),
              sch.getUnitesSousjacentes(), TypeMarquage.SELECT1,
              Element.getUnitesSousjacentes(filtreSch(saisieElt.getElements(), (Schema[])saisieSchema.getElements())),
              TypeMarquage.FOND1_2,
              Element.getUnitesSousjacentes(saisieElt.getElements()), TypeMarquage.FOND2);
    panneauChamps.afficher(sch);
    creeSchema.setEnabled(false);
    supprimeSchema.setEnabled(true);
    ajouteElement.setEnabled(false);
    oteElement.setEnabled(false);
    creeUniteElt.setEnabled(false);
  }
  private void etatSelectSchemaElt(Schema sch, Element elt) {
    if (sch.contient(elt)) etatSelectSchemaEltInterne(sch, elt);
    else etatSelectSchemaEltExterne(sch, elt);
  }
  private void etatSelectSchemaEltInterne(Schema sch, Element elt) {
    panneauTexte.saisir(elt.getUnite0(),
            elt.getUnitesSousjacentes(), TypeMarquage.SELECT1_2,
            sch.getUnitesSousjacentes(), TypeMarquage.SELECT1);
    panneauChamps.afficher(elt);
    creeSchema.setEnabled(false);
    ajouteElement.setEnabled(false);
    if (sch.getContenu().size() > 1) {
      oteElement.setEnabled(true);
      supprimeSchema.setEnabled(false);
    } else {
      oteElement.setEnabled(false);
      supprimeSchema.setEnabled(true);
    }
    creeUniteElt.setEnabled(false);
  }
  private void etatSelectSchemaEltExterne(Schema sch, Element elt) {
    panneauTexte.saisir(elt.getUnite0(),
            sch.getUnitesSousjacentes(), TypeMarquage.SELECT1,
            elt.getUnitesSousjacentes(), TypeMarquage.SELECT2);
    panneauChamps.afficher(sch);
    creeSchema.setEnabled(false);
    supprimeSchema.setEnabled(false);
    ajouteElement.setEnabled(true);
    oteElement.setEnabled(false);
    creeUniteElt.setEnabled(false);
  }
  private void forceElt(Element elt) {
    if (saisieTypeElt.getClasseSelection() != elt.getClass() || !elt.getType().equals(saisieTypeElt.getTypeSelection())) {
      saisieTypeElt.removeActionListener(this);
      saisieTypeElt.setSelection(elt.getClass(), elt.getType());
      saisieTypeElt.addActionListener(this);
      initSaisieElt();
    }
    saisieElt.setSelection(elt);
  }
  private Element[] filtreSch(Element[] elts, Schema[] schs) {
    ArrayList<Element> elts1 = new ArrayList<Element>();
    for (Element elt : elts) for(Schema sch: schs) if(sch.getContenu().contains(elt)) {
      elts1.add(elt);
      break;
    }
    return elts1.toArray(new Element[0]);
  }
  private void initSaisieSchema(Schema sch) {
    saisieSchema.removeActionListener(this);
    if (saisieTypeSchemas.pasdeSelection()) saisieSchema.setModeleVide();
    else {
      String type0 = saisieTypeSchemas.getTypeSelection();
      Schema[] schemas = vue.getSchemasAVoir(type0);
      saisieSchema.setModele(schemas, vue.getIdSchemas(type0, schemas));
    }
    if (sch == null) saisieSchema.effSelection();
    else saisieSchema.setSelection(sch);
    saisieSchema.addActionListener(this);
    saisieSchema.revalidate();
  }
  private void initSaisieSchema() {
    initSaisieSchema(null);
  }
  private void initSaisieElt(Element elt) {
    saisieElt.removeActionListener(this);
    if (saisieTypeElt.pasdeSelection()) saisieElt.setModeleVide();
    else {
      Class<? extends Element> classe = saisieTypeElt.getClasseSelection();
      String type = saisieTypeElt.getTypeSelection();
      saisieElt.setModele(vue.getElements(classe, type), vue.getIdElements(classe, type));
    }
    if (elt == null) saisieElt.effSelection();
    else saisieElt.setSelection(elt);
    saisieElt.addActionListener(this);
    saisieElt.revalidate();
  }
  private void initSaisieElt() {
    initSaisieElt(null);
  }
}