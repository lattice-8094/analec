/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import visuAnalec.Message.*;
import visuAnalec.elements.*;
import visuAnalec.texte.*;
import visuAnalec.texte.PanneauTexte.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.vue.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class PanneauEditeur extends JPanel implements VueListener {
  private enum Commandes {
    GESTION_UNITES,
    GESTION_RELATIONS,
    GESTION_SCHEMAS,
    MODIF_TEXTE;
    private static Commandes getCommande(Class<? extends Element> classe) {
      if (classe==Unite.class) return GESTION_UNITES;
      if (classe==Relation.class) return GESTION_RELATIONS;
      if (classe==Schema.class) return GESTION_SCHEMAS;
      throw new UnsupportedOperationException("sous-classe non traitée");
    }
  }
  private Vue vue;
  private PanneauAffTexte panneauTexte;
  private JPanel panneauOccurrences;
  private PanneauChamps panneauChamps;
  private Commandes commandes = null;
  private HashMap<Commandes, PanneauCommandes> panneauxCommandes =
          new HashMap<Commandes, PanneauCommandes>();
  private HashMap<Commandes, String> titresPanneauxCommandes =
          new HashMap<Commandes, String>();
  private JDialog fenetreModifTexte;
  private PanneauModifTexte panneauModifTexte;
  private PanneauCommandesModifTexte panneauCommandesModifTexte;
  private String recherche = "";
  private Element elementCourant;

  public static class ElementMarque {
    public Element elt;
    public String id;
    public TypeMarquage marque;
    public ElementMarque(Element elt, TypeMarquage marque, String id) {
      this.elt = elt;
      this.id = id;
      this.marque = marque;
    }
  }

  public static abstract class PanneauCommandes extends Box {
    protected Vue vue;
    protected PanneauAffTexte panneauTexte;
    PanneauCommandes(Vue vue, PanneauAffTexte panneauTexte) {
      super(BoxLayout.Y_AXIS);
      this.vue = vue;
      this.panneauTexte = panneauTexte;
    }
    public Vue getVue() {
      return vue;
    }
    abstract public void reafficher();        // quand la vue n'a pas changé
    abstract public void reinitialiser();     // quand la vue a changé
    abstract public void elementSuivant(int sens);
    abstract public Element elementSelectionne();
    abstract public void editerElement(Element elt);
    abstract public void traiterElementEvent(ElementEvent evt);
    abstract public void traiterSourisEvent(PanneauAffTexte.SourisEvent evt);   // appelée par le panneau AffTexte
    abstract public ArrayList<ElementMarque> trouverElementsVises(Unite uniteVisee,
            TypeMarquage marqueUniteVisee, boolean avecId);  // appelée par le panneau AffTexte
  }
  public PanneauEditeur(Vue vue) {
    super(new BorderLayout());
    this.vue = vue;
    vue.addEventListener(this);
    setBorder(new TitledBorder(""));
    panneauOccurrences = new JPanel(new BorderLayout());
    panneauTexte = new PanneauAffTexte(vue.getCorpus());
    panneauOccurrences.add(new JScrollPane(panneauTexte), BorderLayout.CENTER);
    panneauChamps = new PanneauChamps(this, vue);
    add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, panneauOccurrences, panneauChamps), BorderLayout.CENTER);
    // Les différents panneaux de commandes 
    panneauxCommandes.put(Commandes.GESTION_UNITES,
            new PanneauCommandesUnites(vue, panneauTexte, panneauChamps));
    titresPanneauxCommandes.put(Commandes.GESTION_UNITES, "Gestion des unités");
    panneauxCommandes.put(Commandes.GESTION_RELATIONS,
            new PanneauCommandesRelations(vue, panneauTexte, panneauChamps));
    titresPanneauxCommandes.put(Commandes.GESTION_RELATIONS, "Gestion des relations");
    panneauxCommandes.put(Commandes.GESTION_SCHEMAS,
            new PanneauCommandesSchemas(vue, panneauTexte, panneauChamps));
    titresPanneauxCommandes.put(Commandes.GESTION_SCHEMAS, "Gestion des schémas");
    new GActionClavier(this, "suivant", "F1", "control DOWN") {
      public void executer() {
        elementSuivant(1);
      }
    };
    new GActionClavier(this, "précédent", "F2", "control UP") {
      public void executer() {
        elementSuivant(-1);
      }
    };
    new GActionClavier(this, "copier", "F3", "control M") {
      public void executer() {
        copierElement();
      }
    };
    new GActionClavier(this, "coller", "F4", "control R") {
      public void executer() {
        collerElement();
      }
    };
    // La fenêtre modale pour éditer le texte
    fenetreModifTexte = new JDialog((VisuMain) getTopLevelAncestor(), true);
    fenetreModifTexte.setUndecorated(true);
    fenetreModifTexte.getContentPane().setLayout(new BorderLayout());
    panneauModifTexte = new PanneauModifTexte(vue.getCorpus());
    fenetreModifTexte.getContentPane().add(new JScrollPane(panneauModifTexte), BorderLayout.CENTER);
    panneauCommandesModifTexte = new visuAnalec.texte.PanneauCommandesModifTexte(this, vue, panneauModifTexte);
    panneauModifTexte.setCommandesModifTexte(panneauCommandesModifTexte);
    fenetreModifTexte.getContentPane().add(panneauCommandesModifTexte, BorderLayout.SOUTH);
    fenetreModifTexte.setVisible(false);
    // Panneau initial : gestion des unités
    changerPanneauCommandes(Commandes.GESTION_UNITES);
    reinitCommandes();

  }
  void agencerPanneaux() {
    JSplitPane split = (JSplitPane) panneauOccurrences.getParent();
    split.setDividerLocation(2./3);
    revalidate();
    repaint();
  }
  private void changerPanneauCommandes(Commandes newCommandes) {
    if (commandes!=null)
      panneauOccurrences.remove(panneauxCommandes.get(commandes));
    commandes = newCommandes;
    panneauOccurrences.add(panneauxCommandes.get(commandes), BorderLayout.NORTH);
    ((TitledBorder) getBorder()).setTitle(titresPanneauxCommandes.get(commandes));
    panneauTexte.setCommandes(panneauxCommandes.get(commandes));
    revalidate();
    repaint();
  }
  private void reinitCommandes() {
    panneauxCommandes.get(commandes).reinitialiser();
  }
  private void elementSuivant(int sens) {
    panneauxCommandes.get(commandes).elementSuivant(sens);
  }
  private void copierElement() {
    Element elt = panneauxCommandes.get(commandes).elementSelectionne();
    if (elt!=null) elementCourant = elt;
  }
  private void collerElement() {
    if (elementCourant==null) return;
    Element elt = panneauxCommandes.get(commandes).elementSelectionne();
    if (elt==null) return;
    vue.copierValeurs(elementCourant, elt);
  }
  private void reinitCommandes(Element elt) {
    panneauxCommandes.get(commandes).reinitialiser();
    panneauxCommandes.get(commandes).editerElement(elt);
  }
  private void reaffCommandes() {
    panneauxCommandes.get(commandes).reafficher();
  }
  private void initNewCorpus() {
    panneauTexte.setTexteVisu();
    panneauTexte.initAscenseur();
    colorierTexte();
    changerPanneauCommandes(Commandes.GESTION_UNITES);
    reinitCommandes();
  }
  private void colorierTexte() {
    panneauTexte.setAttributsStandard(vue.getStyleTexte());
    panneauTexte.colorierUnites(vue.getUnitesAColorier());
  }
  void initRechercherTexte() {
    recherche = (String) JOptionPane.showInputDialog(this, "Recherche dans le texte",
            "Rechercher", JOptionPane.PLAIN_MESSAGE, null, null, recherche);
    if (recherche==null) recherche = "";
    if (recherche.isEmpty()) return;
    panneauTexte.rechercherTexte(recherche);
  }
  void rechercherSuivantTexte() {
    if (recherche.isEmpty()) return;
    panneauTexte.rechercherTexte(recherche);

  }
  void initModifierTexte() {
    ((TitledBorder) getBorder()).setTitle("Modification du texte");
    repaint();
    fenetreModifTexte.setSize(panneauOccurrences.getSize());
    fenetreModifTexte.setLocationRelativeTo(panneauOccurrences);
    panneauChamps.effacer();
    panneauModifTexte.init();
    fenetreModifTexte.setModal(false);
    fenetreModifTexte.setVisible(true);
    panneauModifTexte.ajusterAscenseur(panneauTexte);
    fenetreModifTexte.setVisible(false);
    fenetreModifTexte.setModal(true);
    fenetreModifTexte.setVisible(true);
  }
  public void finModifTexte() {
    fenetreModifTexte.setVisible(false);
    panneauTexte.ajusterAscenseur(panneauModifTexte);
    if (commandes==null) {
      changerPanneauCommandes(Commandes.GESTION_UNITES);
      reinitCommandes();
    } else {
      ((TitledBorder) getBorder()).setTitle(titresPanneauxCommandes.get(commandes));
      reaffCommandes();
    }
    repaint();
  }
  public void saisirStyleTexte(Vue vue) {
    VisuStyleTexte.newVisuStyleTexte(this, vue.getStyleTexte());
  }
  public void changerStyleTexte(SimpleAttributeSet att) {
    vue.setStyleTexte(att);
    panneauTexte.setAttributsStandard(att);
    panneauModifTexte.setAttributsStandard(att);
    reaffCommandes();
  }
  public Document getTexteFormate() {
    return panneauTexte.getDocument();
  }
  public void editerElement(Element elt) {
    // typiquement, appelée de l'extérieur par d'autres fenêtres
    changerPanneauCommandes(Commandes.getCommande(elt.getClass()));
    reinitCommandes(elt);
    ((VisuMain) getTopLevelAncestor()).premierPlan();
  }
  public void editerUniteChaine(Unite unit, Schema chaine) {
    // appelée de l'extérieur notamment par visuChaines)
    changerPanneauCommandes(Commandes.GESTION_SCHEMAS);
    reinitCommandes();
    ((PanneauCommandesSchemas) panneauxCommandes.get(commandes)).editerSchemaElement(chaine, unit);
    ((VisuMain) getTopLevelAncestor()).premierPlan();
  }
  public void editerUniteTypeChaine(Unite unit, String typechaine) {
    // appelée de l'extérieur notamment par visuChaines)
    changerPanneauCommandes(Commandes.GESTION_SCHEMAS);
    reinitCommandes();
    ((PanneauCommandesSchemas) panneauxCommandes.get(commandes)).editerElementTypeSchema(unit, typechaine);
    ((VisuMain) getTopLevelAncestor()).premierPlan();
  }
  void initGestionUnites() {
    if (vue.getTypesUnitesAVoir().length==0) {
      JOptionPane.showMessageDialog(getTopLevelAncestor(), "Aucun type d'unités n'est défini dans la vue actuelle",
              "Saisie d'unités impossible", JOptionPane.ERROR_MESSAGE);
      return;
    }
    changerPanneauCommandes(Commandes.GESTION_UNITES);
    reinitCommandes();
  }
  void initGestionRelations() {
    if (vue.getTypesRelationsAVoir().length==0) {
      JOptionPane.showMessageDialog(getTopLevelAncestor(), "Aucun type de relation n'est défini dans la vue actuelle",
              "Saisie de relations impossible", JOptionPane.ERROR_MESSAGE);
      return;
    }
    changerPanneauCommandes(Commandes.GESTION_RELATIONS);
    reinitCommandes();
  }
  void initGestionSchemas() {
    if (vue.getTypesSchemasAVoir().length==0) {
      JOptionPane.showMessageDialog(getTopLevelAncestor(), "Aucun type de schéma n'est défini dans la vue actuelle",
              "Saisie de schéma impossible", JOptionPane.ERROR_MESSAGE);
      return;
    }
    changerPanneauCommandes(Commandes.GESTION_SCHEMAS);
    reinitCommandes();
  }
  public void traiterVueEvent(VueEvent evt) {
    switch (evt.getModif()) {
      case EXTRACTION:
      case NEW_VUE:
      case VUE_DEFAUT:
        colorierTexte();
        reinitCommandes();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getModif()+" oublié dans un switch");
    }
  }
  public void traiterStructureEvent(StructureEvent evt) {
    switch (evt.getModif()) {
      case SUPPR_TYPE:
      case FUSION_TYPE:
      case RENOM_TYPE:
      case AJOUT_TYPE:
      case AJOUT_TYPE_ET_ELEMENTS:
        colorierTexte();
        reinitCommandes();
        return;
      case VALEUR_PAR_DEFAUT:
      case AJOUT_FORME_UNITE:
      case FUSION_PROP:
      case RENOM_PROP:
      case SUPPR_PROP:
      case AJOUT_PROP:
      case FUSION_VALEUR:
      case RENOM_VALEUR:
      case SUPPR_VALEUR:
      case AJOUT_VALEUR:
        colorierTexte();
        panneauChamps.reafficher();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getModif()+" oublié dans un switch");
    }
  }
  public void traiterEvent(Message evt) {
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        initNewCorpus();
        return;
      case NEW_CORPUS:
        initNewCorpus();
        return;
      case MODIF_TEXTE:
        panneauTexte.setTexteVisu();
        return;
      case MODIF_VUE:
        traiterVueEvent((VueEvent) evt);
        return;
      case MODIF_STRUCTURE:
        traiterStructureEvent((StructureEvent) evt);
        return;
      case MODIF_ELEMENT:
        colorierTexte();
        panneauxCommandes.get(commandes).traiterElementEvent((ElementEvent) evt);
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }

  }
}
