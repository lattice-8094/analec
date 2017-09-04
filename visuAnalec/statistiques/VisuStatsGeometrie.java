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
import javax.swing.table.*;
import visuAnalec.*;
import visuAnalec.Message.*;
import visuAnalec.elements.*;
import visuAnalec.fichiers.*;
import visuAnalec.statistiques.ModeleStatsGeometrie.*;
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
public class VisuStatsGeometrie extends JFrame implements VueListener, ActionListener, DonneesGrapheListener {
  private Vue vue;
  private ModeleStatsGeometrie donneesGraphe = new ModeleStatsGeometrie();
  private GPlanAFC panneauAfficheGraphe;
  private Class<? extends Element> classe;
  private String type;
  private String[] listeChampsSelectionnes;
  private String champAffiche;
  private TypeComboBox saisieTypeElements;
  private JList saisieChampsSelectionnes = new JList();
  private JComboBox saisieChampAffiche = new JComboBox();
  private JList legendeChampAffiche = new JList();
  private PanneauOccurrences panneauOccurrences;
  public VisuStatsGeometrie(Container fenetre, Vue vue, PanneauEditeur pEditeur) {
    super("Représentations géométriques");
    Point pos = new Point(fenetre.getX()+20, fenetre.getY()+20);
    Dimension dim = new Dimension(fenetre.getWidth(), fenetre.getHeight());
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
    saisieTypeElements.addActionListener(this);
    pSaisieType.add(saisieTypeElements, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieType);
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pSaisieSelection = new JPanel(new FlowLayout());
    pSaisieSelection.setBorder(new TitledBorder("Champs pertinents"));
    pSaisieSelection.add(saisieChampsSelectionnes, CENTER_ALIGNMENT);
    panneauCommandes.add(pSaisieSelection);
    panneauCommandes.add(Box.createVerticalGlue());
    JPanel pAfficherGraphe = new JPanel(new FlowLayout());
    JButton afficherGraphe = new JButton(new GAction("Mettre à jour le graphe") {
      public void executer() {
        Object[] valSelect = saisieChampsSelectionnes.getSelectedValues();
        String[] chs = new String[valSelect.length];
        for (int i = 0; i<valSelect.length; i++) {
          chs[i] = (String) valSelect[i];
        }
        donneesGraphe.majChampsSelect(chs);
      }
    });
    pAfficherGraphe.add(afficherGraphe, CENTER_ALIGNMENT);
    panneauCommandes.add(pAfficherGraphe);
    panneauCommandes.add(Box.createVerticalGlue());

    // Panneau du graphe
    JPanel panneauAfficheGraphique = new JPanel(new BorderLayout());
    panneauAfficheGraphique.setBorder(new TitledBorder("Graphique"));
    panneauAfficheGraphique.setPreferredSize(new Dimension(
            Math.round((getWidth()-panneauCommandes.getPreferredSize().width)/2),
            panneauCommandes.getPreferredSize().height));
    panneauAfficheGraphe = new GPlanAFC(donneesGraphe);
    JScrollPane scroll1 = new JScrollPane(panneauAfficheGraphe);
    panneauAfficheGraphique.add(scroll1, BorderLayout.CENTER);
    JPanel pExporterGraphique = new JPanel(new FlowLayout());
    JButton exporterGraphiqueSVG = new JButton(
            new GAction("<html> Exporter le graphique<br>en format xml (.svg)") {
              public void executer() {
                FichiersGraphique.exporterGraphiqueSVG(panneauAfficheGraphe);
              }
            });
    JButton exporterGraphiqueEMF = new JButton(
            new GAction("<html> Exporter le graphique<br>vers MS-Office (.emf)") {
              public void executer() {
                FichiersGraphique.exporterGraphiqueEMF(panneauAfficheGraphe);
              }
            });
    //  exporterGraphiqueEMF.setText("<htlm> Exporter le graphique<br>vers MS-Office (.emf)");
    pExporterGraphique.add(exporterGraphiqueSVG, CENTER_ALIGNMENT);
    pExporterGraphique.add(exporterGraphiqueEMF, CENTER_ALIGNMENT);
    panneauAfficheGraphique.add(pExporterGraphique, BorderLayout.SOUTH);

    // Panneau champ affiché 
    Box panneauChampAffiche = Box.createVerticalBox();
    panneauChampAffiche.add(Box.createVerticalGlue());
    JPanel pChampAffiche = new JPanel(new BorderLayout());
    pChampAffiche.setBorder(new TitledBorder("Champ à afficher"));
    saisieChampAffiche.setEditable(false);
    saisieChampAffiche.addActionListener(this);
    pChampAffiche.add(saisieChampAffiche, BorderLayout.NORTH);
    legendeChampAffiche.setCellRenderer(new LegendeCellRenderer());
    pChampAffiche.add(legendeChampAffiche, BorderLayout.CENTER);
    panneauChampAffiche.add(pChampAffiche);

    // Panneau valeurs typiques 
    JPanel panneauValTypiques = new JPanel(new BorderLayout());
    panneauValTypiques.setBorder(new TitledBorder("Valeurs typiques"));
    JLabel affNbElements = new JLabel("", JLabel.CENTER);
    panneauValTypiques.add(affNbElements, BorderLayout.NORTH);
    ModeleValTypiques donneesValTypiques =
            new ModeleValTypiques(vue, donneesGraphe, affNbElements);
    donneesGraphe.addEventListener(donneesValTypiques);
    JTable tableauValTypiques = new JTable(donneesValTypiques);
    JScrollPane scrollChampAffiche = new JScrollPane(tableauValTypiques);
    panneauValTypiques.add(scrollChampAffiche, BorderLayout.CENTER);

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

    donneesGraphe.addEventListener(this);  // pour lancer afficheOccurrences

    // cadre global de la fenêtre
    JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panneauChampAffiche, panneauValTypiques);
    JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panneauAfficheGraphique, split1);
    JSplitPane split3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panneauCommandes, split2);
    JPanel bas = new JPanel(new BorderLayout());
    bas.add(scrollPanneauOccurrences, BorderLayout.CENTER);
    bas.add(pExporterConcordances, BorderLayout.SOUTH);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            split3, bas);
    getContentPane().add(split);
    setVisible(true);
    split3.setDividerLocation(0.15);
    split2.setDividerLocation(0.4);
    split1.setDividerLocation(0.3);
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
  }
  private void fermerFenetre() {
    vue.removeEventListener(this);
    dispose();
  }
  void initSaisieChampsSelectionnes() {
    if (classe==null) {
      listeChampsSelectionnes = new String[0];
      saisieChampsSelectionnes.setListData(listeChampsSelectionnes);
    } else {
      listeChampsSelectionnes = vue.getNomsChamps(classe, type);
      saisieChampsSelectionnes.setListData(listeChampsSelectionnes);
      saisieChampsSelectionnes.addSelectionInterval(0, listeChampsSelectionnes.length-1);
    }
  }
  void initSaisieChampAffiche() {
    saisieChampAffiche.removeAllItems();
    if (classe!=null) {
      for (String ch : vue.getNomsChamps(classe, type)) {
        saisieChampAffiche.addItem(ch);
      }
    }
    saisieChampAffiche.setSelectedIndex(-1);
    champAffiche = null;
    initLegendeChampAffiche();
  }
  void initLegendeChampAffiche() {
    legendeChampAffiche.setListData(new LegendeLabel[0]);
    HashMap<String, Color> couleurValeur = new HashMap<String, Color>();
    couleurValeur.put("", GVisu.getCouleurSansValeur());
    donneesGraphe.majChampAffiche(champAffiche, couleurValeur);
  }
  void majCouleursChampAffiche() {
    String[] vals = classe==null ? new String[0] : vue.getValeursChamp(classe, type, champAffiche);
    HashMap<String, Color> couleurValeur = new HashMap<String, Color>();
    LegendeLabel[] legende = new LegendeLabel[vals.length+1];
    for (int i = 0; i<vals.length; i++) {
      legende[i] = new LegendeLabel(vals[i], GVisu.getCouleur(i));
      couleurValeur.put(vals[i], GVisu.getCouleur(i));
    }
    legende[vals.length] = new LegendeLabel("<aucune valeur>", GVisu.getCouleurSansValeur());
    couleurValeur.put("", GVisu.getCouleurSansValeur());
    legendeChampAffiche.setListData(legende);
    donneesGraphe.majChampAffiche(champAffiche, couleurValeur);
  }
  public void traiterEvent(Message evt) {
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        fermerFenetre();
        return;
      case NEW_CORPUS:  // impossible en principe : CLEAR_CORPUS a détruit la fenêtre
        throw new UnsupportedOperationException("Fenêtre stat présente lors d'un message NEW_CORPUS ???");
      case MODIF_TEXTE:
        return;
      case MODIF_VUE:
        fermerFenetre();
        return;
      case MODIF_STRUCTURE:
        fermerFenetre();
        return;
      case MODIF_ELEMENT:
        ElementEvent evtE = (ElementEvent) evt;
        if (evtE.getModif()!=TypeModifElement.BORNES_UNITE) {
          fermerFenetre();
        }
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
        donneesGraphe.initialise(vue, classe, type);
        initSaisieChampsSelectionnes();
        initSaisieChampAffiche();
        return;
      }
      if (evt.getSource().equals(saisieChampAffiche)) {
        champAffiche = (String) saisieChampAffiche.getSelectedItem();
        majCouleursChampAffiche();
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "
              +evt.paramString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  public void traiterEvent(DonneesGrapheEvent evt) {
    switch (evt.getType()) {
      case NEW_DONNEES:
      case CLEAR_DONNEES:
        panneauOccurrences.initVide();
        return;
      case MODIF_DONNEES:
        panneauOccurrences.initVide();
        majCouleursChampAffiche();
        return;
      case NEW_SELECT_PTS:
        panneauOccurrences.init(classe, type,
                donneesGraphe.getListeElementsChoisis());
        return;
      case NEW_COORDS:
      case NEW_COULEURS:
        return;
      default:
        throw new UnsupportedOperationException("cas "+evt.getType()
                +" non traité dans un switch");
    }
  }

  static class ModeleValTypiques extends AbstractTableModel
          implements DonneesGrapheListener {
    String[] nomsColonnes;
    Vue.TripletValTypique[] triplets;
    ModeleStatsGeometrie donnees;
    Vue vue;
    JLabel affNbElts;
    public ModeleValTypiques(Vue vue, ModeleStatsGeometrie donnees, JLabel affNbElts) {
      super();
      this.donnees = donnees;
      this.vue = vue;
      this.affNbElts = affNbElts;
      initialise();
    }
    private void initialise() {
      nomsColonnes = new String[]{"Champ", "Valeur", "Pourcentage"};
      setModeleVide();
    }
    private void setModeleVide() {
      triplets = new Vue.TripletValTypique[0];
      affNbElts.setText("Pas d'élément sélectionné");
      fireTableDataChanged();
    }
    private void setModele(Class<? extends Element> classe, String type, Element[] elts) {
      triplets = vue.getValTypiques(classe, type, elts);
      affNbElts.setText(elts.length==1 ? "Un seul élément sélectionné"
              : elts.length+" éléments sélectionnés");
      fireTableDataChanged();
    }
    public int getRowCount() {
      return triplets.length;
    }
    public int getColumnCount() {
      return 3;
    }
    @Override
    public String getColumnName(int col) {
      return nomsColonnes[col];
    }
    public Object getValueAt(int row, int col) {
      switch (col) {
        case 0:
          return triplets[row].champ;
        case 1:
          return triplets[row].valeur;
        case 2:
          return Math.round(triplets[row].pourcentage*10000)/100.0f;
        default:
          return null;
      }
    }
    public void traiterEvent(DonneesGrapheEvent evt) {
      switch (evt.getType()) {
        case NEW_DONNEES:
        case CLEAR_DONNEES:
        case MODIF_DONNEES:
          setModeleVide();
          return;
        case NEW_SELECT_PTS:
          setModele(donnees.getClasseElements(), donnees.getTypeElements(),
                  donnees.getListeElementsChoisis());
          return;
        case NEW_COORDS:
        case NEW_COULEURS:
          return;
        default:
          throw new UnsupportedOperationException("cas "+evt.getType()+" non traité dans un switch");
      }
    }
  }
}
