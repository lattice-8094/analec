/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.chaines;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import visuAnalec.*;
import visuAnalec.Message.*;
import visuAnalec.elements.*;
import visuAnalec.texte.*;
import visuAnalec.texte.PanneauTexte.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.util.GVisu.*;
import visuAnalec.vue.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuChaines extends JFrame implements VueListener, ActionListener {
  private static int[] seuilsValsNbElts = {3, 10};
  private static Color[] couleursNbElts = {Color.WHITE, GVisu.getCouleurSansValeur(),
    GVisu.getCouleur(0), GVisu.getCouleur(3), GVisu.getCouleur(1)};
  private static String[] labelsNbElements = {
    "pas de chaîne associée",
    "chaîne d'un seul chaînon",
    "chaîne de 2-"+Integer.toString(seuilsValsNbElts[0])+" chaînons",
    "chaîne de "+Integer.toString(seuilsValsNbElts[0]+1)+"-"
    +Integer.toString(seuilsValsNbElts[1])+"  chaînons",
    "chaîne de plus de "+Integer.toString(seuilsValsNbElts[1])+" chaînons"
  };
  private Vue vue;
  private ModeleChaines donneesChaines;
  private String typeChaines;
  private String typeUnites;
  private String[] valsAffichees;
  private TypeComboBox saisieTypeChaines;
  private TypeComboBox saisieTypeUnites;
  private Val0ComboBox saisieChampAffiche;
  private JComboBox saisieTailleLigne;
  private JList legendeChampAffiche;
  private JPopupMenu popupChamp = new JPopupMenu();
  private GChaines panneauGraphique;
  private PanneauAffChainesTexte panneauTexte;
  private PanneauEditeur panneauEditeur;
  public VisuChaines(Container fenetre, Vue vue, PanneauEditeur pEditeur) {
    super("Représentation des chaînes");
    Point pos = new Point(fenetre.getX()+20, fenetre.getY()+20);
    Dimension dim = new Dimension(fenetre.getWidth(), fenetre.getHeight());
    setLocation(pos);
    setSize(dim);
    this.vue = vue;
    vue.addEventListener(this);
    this.panneauEditeur = pEditeur;
    saisieTypeChaines = new TypeComboBox(Schema.class);
    saisieTypeChaines.setModele(vue.getTypesSchemasAVoir());
    saisieTypeChaines.addActionListener(this);
    saisieTypeUnites = new TypeComboBox(Unite.class);
    saisieTypeUnites.setModele(vue.getTypesUnitesAVoir());
    saisieTypeUnites.addActionListener(this);
    saisieTailleLigne = new JComboBox(
            new String[]{"<tous>", "10", "20", "30", "40", "50"});
    saisieTailleLigne.setEditable(true);
   // saisieTailleLigne.setMaximumSize(getPreferredSize());
    saisieTailleLigne.addActionListener(this);
    saisieTypeChaines.setModele(vue.getTypesSchemasAVoir());
    saisieChampAffiche = new Val0ComboBox("Nombre de chaînons");
    saisieChampAffiche.addActionListener(this);
    // Panneau de présentation du graphique
    JPanel pGraphique = new JPanel(new BorderLayout());
    Box commandes = Box.createHorizontalBox();
    commandes.add(Box.createHorizontalGlue());
    commandes.add(new JLabel("Chaînes : "));
    commandes.add(saisieTypeChaines);
    commandes.add(Box.createHorizontalGlue());
    commandes.add(new JLabel("Unites : "));
    commandes.add(saisieTypeUnites);
    commandes.add(Box.createHorizontalGlue());
    commandes.add(new JLabel("Nb de chainons par ligne : "));
    commandes.add(saisieTailleLigne);
    commandes.add(Box.createHorizontalGlue());
    commandes.add(new JLabel("Champ à afficher : "));
    commandes.add(saisieChampAffiche);
    pGraphique.add(commandes, BorderLayout.SOUTH);
    panneauGraphique = new GChaines(this);
    JScrollPane scrollGraphique = new JScrollPane(panneauGraphique);
    pGraphique.add(scrollGraphique, BorderLayout.CENTER);
    panneauTexte = new PanneauAffChainesTexte(vue.getCorpus(), this);
    panneauTexte.setTexteVisu();
    JScrollPane scrollTexte = new JScrollPane(panneauTexte);
    legendeChampAffiche = new JList();
    legendeChampAffiche.setCellRenderer(new LegendeCellRenderer());
    legendeChampAffiche.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        check(e);
      }
      public void mouseReleased(MouseEvent e) {
        check(e);
      }
      public void check(MouseEvent e) {
        if (e.isPopupTrigger()) {//if the event shows the menu
          popupChampInit(legendeChampAffiche.locationToIndex(e.getPoint()));
          popupChamp.show(legendeChampAffiche, e.getX(), e.getY());
        }
      }
    });
    JScrollPane scrollLegende = new JScrollPane(legendeChampAffiche);
    JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTexte, scrollLegende);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pGraphique, split1);
    getContentPane().add(split);
    setVisible(true);
    split.setDividerLocation(1./3);
    split1.setDividerLocation(3./4);
    // Fermeture de la fenêtre
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        fermerFenetre();
      }
    });
    // Initialisation
    saisieTailleLigne.setSelectedIndex(0);
    saisieTailleLigne.setMaximumSize(getPreferredSize());
    donneesChaines = new ModeleChaines(vue, panneauGraphique);
    if (saisieTypeChaines.nbItems()==1) {
      saisieTypeChaines.setSelection(0);
      if (saisieTypeUnites.nbItems()==1) saisieTypeUnites.setSelection(0);
      else saisieTypeUnites.effSelection();
    } else {
      saisieTypeChaines.effSelection();
      saisieTypeUnites.effSelection();
    }
  }
  void afficherOccurrences(Unite unite, Schema chaine) {
    if (chaine!=null)
      panneauTexte.montrerUnite(unite, TypeMarquage.SELECT1_2,
              donneesChaines.getUnitesChaine(chaine), TypeMarquage.SELECT1);
    else
      panneauTexte.montrerUnite(unite, TypeMarquage.SELECT2);

  }
  private void fermerFenetre() {
    vue.removeEventListener(this);
    dispose();
  }
  public void traiterEvent(Message evt) { // très grossier : à améliorer
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        fermerFenetre();
        return;
      case NEW_CORPUS:  // impossible en principe : CLEAR_CORPUS a détruit la fenêtre
        throw new UnsupportedOperationException("Fenêtre chaines présente lors d'un message NEW_CORPUS ???");
      case MODIF_TEXTE:
        fermerFenetre();
        return;
      case MODIF_VUE:
        fermerFenetre();
        return;
      case MODIF_STRUCTURE:
        fermerFenetre();
        return;
      case MODIF_ELEMENT:
//        ElementEvent evtE = (ElementEvent) evt;
//        if (evtE.getModif() != TypeModifElement.BORNES_UNITE) fermerFenetre();
        fermerFenetre();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }
  }
  public void traiterAffTexteDoubleClic(Unite unit) {
    Schema chaine;
    if ((chaine = donneesChaines.getChaine(unit))!=null)
      panneauEditeur.editerUniteChaine(unit, chaine);
    else
      panneauEditeur.editerUniteTypeChaine(unit, typeChaines);
  }
  public void traiterAffTexteClicVide() {
    panneauTexte.colorerUnites(donneesChaines.getUnites(), TypeMarquage.FOND1);
  }
  public void actionPerformed(ActionEvent evt) {
    try {
      if (evt.getSource().equals(saisieTypeChaines)
              ||evt.getSource().equals(saisieTypeUnites)) {
        if (saisieTypeChaines.pasdeSelection()||saisieTypeUnites.pasdeSelection()) {
          initVide();
          return;
        }
        try {
          init();
        } catch (UnsupportedOperationException ex) {
          GMessages.impossibiliteAction("Erreur de type : "+ex.getMessage());
          initVide();
          return;
        }
        return;
      }
      if (evt.getSource().equals(saisieChampAffiche)) {
        if (saisieChampAffiche.pasdeSelection()) initCouleursNbElts();
        else initCouleursChamp(saisieChampAffiche.getSelection());
        return;
      }
      if (evt.getSource().equals(saisieTailleLigne)) {
        int taille = saisieTailleLigne.getSelectedIndex()==0 ? Integer.MAX_VALUE :
                Integer.parseInt((String)saisieTailleLigne.getSelectedItem());
        panneauGraphique.setNbChainonsParLigne(taille);
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.paramString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  private void initVide() {
    typeChaines = null;
    typeUnites = null;
    donneesChaines.initVide();
    saisieChampAffiche.setModeleVide();
    panneauTexte.pasDeMarquage();
    panneauTexte.setCurseur(0);
    legendeChampAffiche.setListData(new LegendeLabel[0]);
  }
  private void init() throws UnsupportedOperationException {
    typeChaines = saisieTypeChaines.getTypeSelection();
    typeUnites = saisieTypeUnites.getTypeSelection();
    donneesChaines.init(typeChaines, typeUnites, seuilsValsNbElts);
    initCouleursNbElts();
    saisieChampAffiche.removeActionListener(this);
    saisieChampAffiche.setModele(vue.getNomsChamps(Unite.class, typeUnites));
    saisieChampAffiche.addActionListener(this);
    panneauTexte.colorerUnites(donneesChaines.getUnites(), TypeMarquage.FOND1);
  }
  private void initCouleursNbElts() {
    donneesChaines.setChampAfficheNbElts(couleursNbElts);
    legendeChampAffiche.setListData(creerLegendesNbElements());
  }
  private LegendeLabel[] creerLegendesNbElements() {
    LegendeLabel[] legendes = new LegendeLabel[couleursNbElts.length];
    for (int i = 0; i<legendes.length; i++)
      legendes[i] = new LegendeLabel(labelsNbElements[i],
              donneesChaines.isMasqueeValNbElements(i) ? null : couleursNbElts[i]);
    return legendes;
  }
  private void initCouleursChamp(String champ) {
    valsAffichees = vue.getValeursChamp(Unite.class, typeUnites, champ);
    HashMap<String, Color> couleursValeursChamp = new HashMap<String, Color>();
    for (int i = 0, nocolor = 0; i<valsAffichees.length; i++)
      if (!donneesChaines.isMasqueeValChamp(champ, valsAffichees[i]))
        couleursValeursChamp.put(valsAffichees[i], GVisu.getCouleur(nocolor++));
    if (!donneesChaines.isMasqueeValNulleChamp(champ))
      couleursValeursChamp.put("", GVisu.getCouleurSansValeur());
    donneesChaines.setChampAffiche(champ, couleursValeursChamp);
    legendeChampAffiche.setListData(creerLegendesChamp(valsAffichees,
            couleursValeursChamp));
  }
  private LegendeLabel[] creerLegendesChamp(String[] valsAffichees,
          HashMap<String, Color> couleursValeursChamp) {
    LegendeLabel[] legendes = new LegendeLabel[valsAffichees.length+1];
    for (int i = 0; i<valsAffichees.length; i++)
      legendes[i] = new LegendeLabel(valsAffichees[i],
              couleursValeursChamp.get(valsAffichees[i]));
    legendes[valsAffichees.length] = new LegendeLabel("<aucune valeur>",
            couleursValeursChamp.get(""));
    return legendes;
  }
  private void popupChampInit(final int index) {
    final boolean masquee;
    popupChamp.removeAll();
    if (saisieChampAffiche.pasdeSelection()) {
      masquee = donneesChaines.isMasqueeValNbElements(index);
      popupChamp.add(new GAction(masquee ? "Montrer cette valeur" : 
              "Masquer cette valeur") {
        public void executer() {
          modifierStatutValNbElements(masquee, index, false);
        }
      });
      popupChamp.add(new GAction(masquee ? "Montrer toutes les valeurs" : 
              "Ne montrer que cette valeur") {
        public void executer() {
          modifierStatutValNbElements(masquee, index, true);
        }
      });
    } else if (index==valsAffichees.length) {
      masquee = donneesChaines.isMasqueeValNulleChamp(saisieChampAffiche.getSelection());
      popupChamp.add(new GAction(masquee ? "Montrer cette valeur" : 
              "Masquer cette valeur") {
        public void executer() {
          modifierStatutValNulleChamp(masquee, false);
        }
      });
     popupChamp.add(new GAction(masquee ? "Montrer toutes les valeurs" : 
              "Ne montrer que cette valeur") {
        public void executer() {
          modifierStatutValNulleChamp(masquee, true);
        }
      });
    } else {
      masquee = donneesChaines.isMasqueeValChamp(saisieChampAffiche.getSelection(),
              valsAffichees[index]);
      popupChamp.add(new GAction(masquee ? "Montrer cette valeur" : 
              "Masquer cette valeur") {
        public void executer() {
          modifierStatutValChamp(masquee, index, false);
        }
      });
      popupChamp.add(new GAction(masquee ? "Montrer toutes les valeurs" : 
              "Ne montrer que cette valeur") {
        public void executer() {
          modifierStatutValChamp(masquee, index, true);
        }
      });
    }
  }
  private void modifierStatutValChamp(boolean masquee, int index, boolean tous) {
    if(!tous) donneesChaines.masquerValChamp(!masquee, 
            saisieChampAffiche.getSelection(), valsAffichees[index]);
    else if(masquee) donneesChaines.demasquerToutesValsChamp( 
            saisieChampAffiche.getSelection());
    else donneesChaines.masquerAutresValsChamp(typeUnites, 
            saisieChampAffiche.getSelection(), valsAffichees[index]);
    donneesChaines.modifMasqueVals(typeChaines, typeUnites);
    initCouleursChamp(saisieChampAffiche.getSelection());
    panneauTexte.colorerUnites(donneesChaines.getUnites(), TypeMarquage.FOND1);
  }
  private void modifierStatutValNulleChamp(boolean masquee, boolean tous) {
    if(!tous) donneesChaines.masquerValNulleChamp(!masquee, saisieChampAffiche.getSelection());
   else if(masquee) donneesChaines.demasquerToutesValsChamp( 
            saisieChampAffiche.getSelection());
    else donneesChaines.masquerValsNonNullesChamp(typeUnites, 
            saisieChampAffiche.getSelection());
    donneesChaines.modifMasqueVals(typeChaines, typeUnites);
    initCouleursChamp(saisieChampAffiche.getSelection());
    panneauTexte.colorerUnites(donneesChaines.getUnites(), TypeMarquage.FOND1);
  }
  private void modifierStatutValNbElements(boolean masquee, int index, boolean tous) {
    if(!tous) donneesChaines.masquerValNbElements(!masquee, index);
   else if(masquee) donneesChaines.demasquerToutesValsNbElts( 
            saisieChampAffiche.getSelection());
    else donneesChaines.masquerAutresValsNbElts( 
            saisieChampAffiche.getSelection(), index);
    donneesChaines.modifMasqueVals(typeChaines, typeUnites);
    initCouleursNbElts();
    panneauTexte.colorerUnites(donneesChaines.getUnites(), TypeMarquage.FOND1);
  }
}
