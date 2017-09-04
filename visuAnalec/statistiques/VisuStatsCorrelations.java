/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.statistiques;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import visuAnalec.*;
import visuAnalec.Message.*;
import visuAnalec.elements.*;
import visuAnalec.fichiers.*;
import visuAnalec.texte.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.util.GVisu.*;
import visuAnalec.vue.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuStatsCorrelations extends JFrame
        implements VueListener, ActionListener, ListSelectionListener {
  private Vue vue;
  private ModeleStatsCorrelations donneesCorrelations = new ModeleStatsCorrelations();
  private Class<? extends Element> classe;
  private String type;
  private String champ1;
  private String champ2;
  private TypeComboBox saisieTypeElements;
  private JComboBox saisieChamp1 = new JComboBox();
  private JComboBox saisieChamp2 = new JComboBox();
  private JTable tableau;
  private PanneauOccurrences panneauOccurrences;
  public VisuStatsCorrelations(Container fenetre, Vue vue, PanneauEditeur pEditeur) {
    super("Corrélations");
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
    saisieTypeElements.setMaximumRowCount(saisieTypeElements.getModel().getSize());
    saisieTypeElements.setEditable(false);
    saisieTypeElements.addActionListener(this);
    pSaisieType.add(saisieTypeElements, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieType);
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pSaisieChamp1 = new JPanel(new FlowLayout());
    pSaisieChamp1.setBorder(new TitledBorder("Champ 1 (horizontal)"));
    saisieChamp1.setEditable(false);
    saisieChamp1.addActionListener(this);
    pSaisieChamp1.add(saisieChamp1, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieChamp1);
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pSaisieChamp2 = new JPanel(new FlowLayout());
    pSaisieChamp2.setBorder(new TitledBorder("Champ 2 (vertical)"));
    saisieChamp2.setEditable(false);
    saisieChamp2.addActionListener(this);
    pSaisieChamp2.add(saisieChamp2, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieChamp2);
    panneauCommandes.add(Box.createVerticalGlue());
    // Panneau du tableau de résultats
    JPanel panneauAfficheTableau = new JPanel(new BorderLayout());
    panneauAfficheTableau.setBorder(new TitledBorder("Résultats"));
    tableau = new JTable(donneesCorrelations);
    tableau.setDefaultRenderer(Number.class, new CorrelationsRenderer(donneesCorrelations));
    tableau.setDefaultRenderer(Object.class, new CorrelationsRenderer(donneesCorrelations));
    tableau.setName("tableauCorrelations");
    tableau.setCellSelectionEnabled(true);
    tableau.setRowHeight(40);
    tableau.getTableHeader().setPreferredSize(new Dimension(tableau.getPreferredSize().width, 40));
    tableau.getSelectionModel().addListSelectionListener(this);
    JScrollPane scroll1 = new JScrollPane(tableau);
    panneauAfficheTableau.add(scroll1, BorderLayout.CENTER);
    JPanel pExporterCorrelations = new JPanel(new FlowLayout());
    JButton exporterCorrelations = new JButton(new GAction("Exporter le tableau") {
      public void executer() {
        FichiersTableur.exporterTable(donneesCorrelations);
      }
    });
    pExporterCorrelations.add(exporterCorrelations, CENTER_ALIGNMENT);
    panneauAfficheTableau.add(pExporterCorrelations, BorderLayout.SOUTH);
    panneauAfficheTableau.setPreferredSize(new Dimension(
            getWidth()-panneauCommandes.getPreferredSize().width,
            panneauCommandes.getPreferredSize().height));
    // Panneau de visualisation des occurrences
    panneauOccurrences = new PanneauOccurrences(vue, pEditeur);
    JScrollPane scrollPanneauOccurrences = new JScrollPane(panneauOccurrences);
    scrollPanneauOccurrences.setBorder(new TitledBorder("Visualisation des occurrences"));
    JButton exporterConcordances = new JButton(
            new GAction("Exporter les concordances") {
              public void executer() {
                FichiersTableur.exporterTable(panneauOccurrences.getModeleTableur());
              }
            });
    JPanel pExporterConcordances = new JPanel(new FlowLayout());
    pExporterConcordances.add(exporterConcordances, CENTER_ALIGNMENT);
    // cadre global de la fenêtre
    JPanel bas = new JPanel(new BorderLayout());
    bas.add(scrollPanneauOccurrences, BorderLayout.CENTER);
    bas.add(pExporterConcordances, BorderLayout.SOUTH);
    JSplitPane haut = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panneauCommandes, panneauAfficheTableau);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
    getContentPane().add(split);
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
  private void initSaisieChamps() {
    for (JComboBox saisieChamp : new JComboBox[]{saisieChamp1, saisieChamp2}) {
      saisieChamp.removeActionListener(this);
      saisieChamp.removeAllItems();
      if (classe!=null) for (String ch : vue.getNomsChamps(classe, type))
          saisieChamp.addItem(ch);
      saisieChamp.setSelectedIndex(-1);
      saisieChamp.addActionListener(this);
    }
    champ1 = champ2 = null;
  }
  void afficherOccurrences() {
    ArrayList<Integer> rows = new ArrayList<Integer>();
    ArrayList<Integer> cols = new ArrayList<Integer>();
    for (int row : tableau.getSelectedRows()) {
      for (int col : tableau.getSelectedColumns()) {
        if (tableau.isCellSelected(row, col)) {
          rows.add(row);
          cols.add(col);
        }
      }
    }
    panneauOccurrences.init(classe, type,
            donneesCorrelations.getListeElementsSelected(rows.toArray(new Integer[0]),
            cols.toArray(new Integer[0]), vue, classe, type, champ1, champ2));
  }
  void changerDonnees() {
    donneesCorrelations.setModele(vue, classe, type, champ1, champ2);
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
          case AJOUT_FORME_UNITE:
          case VALEUR_PAR_DEFAUT:
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
        initSaisieChamps();
        changerDonnees();
        return;
      }
      if (evt.getSource().equals(saisieChamp1)) {
        champ1 = (String) saisieChamp1.getSelectedItem();
        changerDonnees();
        return;
      }
      if (evt.getSource().equals(saisieChamp2)) {
        champ2 = (String) saisieChamp2.getSelectedItem();
        changerDonnees();
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.toString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  public void valueChanged(ListSelectionEvent evt) {
    try {
      if (evt.getValueIsAdjusting())
        return;
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
