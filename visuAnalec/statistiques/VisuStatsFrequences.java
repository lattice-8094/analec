/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.statistiques;

import visuAnalec.texte.PanneauOccurrences;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import visuAnalec.*;
import visuAnalec.Message.*;
import visuAnalec.PanneauEditeur.*;
import visuAnalec.elements.*;
import visuAnalec.fichiers.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.util.GVisu.*;
import visuAnalec.vue.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuStatsFrequences extends JFrame
        implements VueListener, ActionListener, ListSelectionListener {
  private Vue vue;
  private ModeleStatsFrequences donneesFrequences = new ModeleStatsFrequences();
  private Class<? extends Element> classe;
  private String type;
  private String champEtudie;
  private String champFiltre;
  private String[] valeursChampFiltre;
  private TypeComboBox saisieTypeElements;
  private JComboBox saisieChampEtudie = new JComboBox();
  private JComboBox saisieChampFiltre = new JComboBox();
  private JList saisieValeursChampFiltre = new JList();
  private JTable tableau;
  private PanneauOccurrences panneauOccurrences;
  public VisuStatsFrequences(Container fenetre, Vue vue, PanneauEditeur pEditeur) {
    super("Fréquences");
    Point pos = new Point(fenetre.getX()+20, fenetre.getY()+50);
    Dimension dim = new Dimension(fenetre.getWidth(), fenetre.getHeight()-30);
    setLocation(pos);
    setSize(dim);
    this.vue = vue;
    vue.addEventListener(this);

    // Panneau des commandes
    Box panneauCommandes = Box.createVerticalBox();
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pSaisieType = new JPanel(new FlowLayout());
    pSaisieType.setBorder(new TitledBorder("Eléments"));
    saisieTypeElements = new TypeComboBox(Element.class);
    saisieTypeElements.setModele(vue.getTypesUnitesAVoir(), vue.getTypesRelationsAVoir(),
            vue.getTypesSchemasAVoir());
    saisieTypeElements.setEditable(false);
    saisieTypeElements.setMaximumRowCount(saisieTypeElements.getModel().getSize());
    saisieTypeElements.addActionListener(this);
    pSaisieType.add(saisieTypeElements, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieType);
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pSaisieChamp = new JPanel(new FlowLayout());
    pSaisieChamp.setBorder(new TitledBorder("Champ à analyser"));
    saisieChampEtudie.setEditable(false);
    saisieChampEtudie.addActionListener(this);
    pSaisieChamp.add(saisieChampEtudie, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieChamp);
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pSaisieFiltre = new JPanel(new BorderLayout());
    pSaisieFiltre.setBorder(new TitledBorder("Filtre"));
    saisieChampFiltre.setEditable(false);
    saisieChampFiltre.addActionListener(this);
    pSaisieFiltre.add(saisieChampFiltre, BorderLayout.NORTH);
    saisieValeursChampFiltre.addListSelectionListener(this);
    pSaisieFiltre.add(new JScrollPane(saisieValeursChampFiltre), BorderLayout.CENTER);
    panneauCommandes.add(pSaisieFiltre);
    panneauCommandes.add(Box.createVerticalGlue());
    // Panneau du tableau de résultats
    JPanel panneauAfficheTableau = new JPanel(new BorderLayout());
    panneauAfficheTableau.setBorder(new TitledBorder("Résultats"));
    tableau = new JTable(donneesFrequences);
    tableau.getSelectionModel().addListSelectionListener(this);
    JScrollPane scrollTableau = new JScrollPane(tableau);
    panneauAfficheTableau.add(scrollTableau, BorderLayout.CENTER);
    JPanel pExporterFrequences = new JPanel(new FlowLayout());
    JButton exporterFrequences = new JButton(new GAction("Exporter le tableau") {
      public void executer() {
        FichiersTableur.exporterTable(donneesFrequences);
      }
    });
    pExporterFrequences.add(exporterFrequences, CENTER_ALIGNMENT);
    panneauAfficheTableau.add(pExporterFrequences, BorderLayout.SOUTH);
//    panneauAfficheTableau.setPreferredSize(new Dimension(
//            Math.round((dim.width - panneauCommandes.getPreferredSize().width) / 2),
//            panneauCommandes.getPreferredSize().height));
    // Panneau de l'affichage graphique
    GBarres panneauAfficheBarres = new GBarres(donneesFrequences);
    JScrollPane scrollPanneauBarres = new JScrollPane(panneauAfficheBarres);
    scrollPanneauBarres.setBorder(new TitledBorder("Graphique"));
    scrollPanneauBarres.setPreferredSize(scrollTableau.getPreferredSize());
    donneesFrequences.addTableModelListener(panneauAfficheBarres);
    // Panneau de visualisation des occurrences
    panneauOccurrences = new PanneauOccurrences(vue, pEditeur);
    JScrollPane scrollPanneauOccurrences = new JScrollPane(panneauOccurrences);
    scrollPanneauOccurrences.setBorder(
            new TitledBorder("Visualisation des occurrences"));
    JButton exporterConcordances = new JButton(
            new GAction("Exporter les concordances") {
              public void executer() {
                FichiersTableur.exporterTable(panneauOccurrences.getModeleTableur());
              }
            });
    JPanel pExporterConcordances = new JPanel(new FlowLayout());
    pExporterConcordances.add(exporterConcordances, CENTER_ALIGNMENT);
    // cadre global de la fenêtre
    JPanel haut = new JPanel(new BorderLayout());
    haut.add(panneauCommandes, BorderLayout.WEST);
    haut.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauAfficheTableau, panneauAfficheBarres),
            BorderLayout.CENTER);
    JPanel bas = new JPanel(new BorderLayout());
    bas.add(scrollPanneauOccurrences, BorderLayout.CENTER);
    bas.add(pExporterConcordances, BorderLayout.SOUTH);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
    setContentPane(split);
    setVisible(true);
    split.setDividerLocation(2./3);
    // Fermeture de la fenêtre
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        fermerFenetre();
      }
    });
    // Initialisation
    saisieTypeElements.effSelection();
    afficherOccurrences();
  }
  private void fermerFenetre() {
    vue.removeEventListener(this);
    dispose();
  }
  private void initSaisieChampEtudie() {
    saisieChampEtudie.removeActionListener(this);
    saisieChampEtudie.removeAllItems();
    if (classe!=null) for (String ch : vue.getNomsChamps(classe, type))
        saisieChampEtudie.addItem(ch);
    saisieChampEtudie.setSelectedIndex(-1);
    champEtudie = null;
    saisieChampEtudie.addActionListener(this);
  }
  private void initSaisieChampFiltre() {
    saisieChampFiltre.removeActionListener(this);
    saisieChampFiltre.removeAllItems();
    saisieChampFiltre.addItem("<pas de filtre>");
    if (classe!=null) for (String ch : vue.getNomsChamps(classe, type))
        saisieChampFiltre.addItem(ch);
    saisieChampFiltre.setSelectedIndex(0);
    champFiltre = null;
    saisieChampFiltre.addActionListener(this);
  }
  private void initSaisieValeursChampFiltre() {
    saisieValeursChampFiltre.removeListSelectionListener(this);
    if (champFiltre==null) {
      saisieValeursChampFiltre.setListData(new String[0]);
    } else {
      saisieValeursChampFiltre.setListData(vue.getValeursChamp(classe, type, champFiltre));
    }
    saisieValeursChampFiltre.setSelectedIndex(-1);
    saisieValeursChampFiltre.addListSelectionListener(this);
  }
  void afficherOccurrences() {
    panneauOccurrences.init(classe, type, donneesFrequences.getListeElementsRowsSelected(tableau.getSelectedRows(), vue, classe,
            type, champEtudie, champFiltre, valeursChampFiltre));
  }
  void changerDonnees() {
    Object[] vals = saisieValeursChampFiltre.getSelectedValues();
    valeursChampFiltre = new String[vals.length];
    for (int i = 0; i<vals.length; i++) {
      valeursChampFiltre[i] = (String) vals[i];
    }
    donneesFrequences.setModele(vue, classe, type, champEtudie, champFiltre, valeursChampFiltre);
  }
  public void traiterEvent(Message evt) {
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        fermerFenetre();
        return;
      case NEW_CORPUS:  // impossible en principe : CLEAR_CORPUS a détruit la fenêtre
        throw new UnsupportedOperationException("Fenêtre stat présente lors d'un message NEW_CORPUS ???");
      case MODIF_TEXTE:
        afficherOccurrences();
        return;
      case MODIF_VUE:
        fermerFenetre();
        return;
      case MODIF_STRUCTURE:
        switch (((StructureEvent) evt).getModif()) {
          case AJOUT_VALEUR:
          case RENOM_VALEUR:
          case FUSION_VALEUR:
          case SUPPR_VALEUR:
          case VALEUR_PAR_DEFAUT:
          case AJOUT_FORME_UNITE:
            changerDonnees();
            return;
          case AJOUT_TYPE:  // à affiner : trop brutal
          case AJOUT_TYPE_ET_ELEMENTS:
          case RENOM_TYPE:
          case FUSION_TYPE:
          case SUPPR_TYPE:
          case AJOUT_PROP:
          case RENOM_PROP:
          case FUSION_PROP:
          case SUPPR_PROP:
            fermerFenetre();
            return;
          default:
            throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
        }
      case MODIF_ELEMENT:
        ElementEvent evtE = (ElementEvent) evt;
        if (evtE.getModif()!=TypeModifElement.BORNES_UNITE) changerDonnees();
        else afficherOccurrences();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }
  }
  public void actionPerformed(ActionEvent evt) {
    try {
      if (evt.getSource().equals(saisieTypeElements)) {
        classe = saisieTypeElements.getClasseSelection();
        type = saisieTypeElements.getTypeSelection();
        initSaisieChampEtudie();
        initSaisieChampFiltre();
        changerDonnees();
        return;
      }
      if (evt.getSource().equals(saisieChampEtudie)) {
        champEtudie = (String) saisieChampEtudie.getSelectedItem();
        changerDonnees();
        return;
      }
      if (evt.getSource().equals(saisieChampFiltre)) {
        int ind = saisieChampFiltre.getSelectedIndex();
        if (ind==0) champFiltre = null;
        else champFiltre = (String) saisieChampFiltre.getSelectedItem();
        initSaisieValeursChampFiltre();
        changerDonnees();
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.paramString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  public void valueChanged(ListSelectionEvent evt) {
    try {
      if (evt.getValueIsAdjusting()) return;
      if (evt.getSource().equals(saisieValeursChampFiltre)) {
        changerDonnees();
        return;
      }
      if (evt.getSource().equals(tableau.getSelectionModel())) {
        afficherOccurrences();
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.toString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
}
