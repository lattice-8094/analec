/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.vue;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.SimpleAttributeSet;
import visuAnalec.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuVue extends JFrame implements VueListener, ListSelectionListener, ChangeListener {
  static private VisuVue instance = null;
  private Vue vue;
  // Les données spécifiant la vue (échangées avec vue à l'initialisation et à la validation)
  private HashMap<Class<? extends Element>, HashMap<String, Boolean>> isTypesAVoir =
          new HashMap<Class<? extends Element>, HashMap<String, Boolean>>();
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir =
          new HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>>();
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir =
          new HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> niveauxChampsAVoir =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> positionsChampsAVoir =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> modifiabiliteChampsAVoir =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>>();
  private HashMap<Class<? extends Element>, HashMap<String, String>> champsId =
          new HashMap<Class<? extends Element>, HashMap<String, String>>();
  private HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> stylesElements =
          new HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> stylesValeursElements =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>>();  // le tableau des types à voir
  private JTable tableauTypes;
  private JTabbedPane panneauTabule;
  VisuVueExtraction visuVueExtraction;
  VisuVuePresentation visuVuePresentation;
  VisuVueColoriage visuVueColoriage;

  static public void newVisuVue(Container fenetre, Vue vue) {
    if (instance!=null) instance.fermerFenetre();
    instance = new VisuVue(fenetre, vue);
  }

  @SuppressWarnings("unchecked")
  private VisuVue(Container fenetre, Vue vue) {
    super("Gestion de la vue");
    Point locFenetre = new Point(fenetre.getX()+20, fenetre.getY()+50);
    Dimension dimFenetre = new Dimension(fenetre.getWidth(), fenetre.getHeight()-30);
    setLocation(locFenetre);
    setSize(dimFenetre);
    this.vue = vue;
    vue.addEventListener(this);
    // initialisation des données spécifiant la vue
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      isTypesAVoir.put(classe, new HashMap<String, Boolean>());
      for (String type : vue.getTypes(classe))
        isTypesAVoir.get(classe).put(type, false);
      for (String type : vue.getTypesAVoir(classe))
        isTypesAVoir.get(classe).put(type, true);
      champsAVoir.put(classe, new HashMap<String, ArrayList<String>>());
      adressesAVoir.put(classe, new HashMap<String, ArrayList<Adresse>>());
      positionsChampsAVoir.put(classe, new HashMap<String, HashMap<String, Integer>>());
      niveauxChampsAVoir.put(classe, new HashMap<String, HashMap<String, Integer>>());
      modifiabiliteChampsAVoir.put(classe, new HashMap<String, HashMap<String, Boolean>>());
      for (String type : vue.getTypes(classe)) {
        champsAVoir.get(classe).put(type, new ArrayList<String>());
        adressesAVoir.get(classe).put(type, new ArrayList<Adresse>());
        niveauxChampsAVoir.get(classe).put(type, new HashMap<String, Integer>());
        positionsChampsAVoir.get(classe).put(type, new HashMap<String, Integer>());
        modifiabiliteChampsAVoir.get(classe).put(type, new HashMap<String, Boolean>());
      }
      for (String type : vue.getTypesAVoir(classe)) {
        champsAVoir.get(classe).put(type, vue.getListeChamps(classe, type));
        for (String nom : vue.getNomsChamps(classe, type))
          adressesAVoir.get(classe).get(type).add(vue.getAdresseChamps(classe, type, nom));
      }
      champsId.put(classe, new HashMap<String, String>());
      for (String type : vue.getTypesAVoir(classe))
        if (vue.getChampId(classe, type)!=null)
          champsId.get(classe).put(type, vue.getChampId(classe, type));
      for (String type : vue.getTypesAVoir(classe)) {
        positionsChampsAVoir.get(classe).put(type, vue.getPositionsChampsAVoir(classe, type));
        niveauxChampsAVoir.get(classe).put(type, vue.getNiveauxChampsAVoir(classe, type));
        modifiabiliteChampsAVoir.get(classe).put(type, new HashMap<String, Boolean>());
        for (String champ : vue.getNomsChamps(classe, type))
          modifiabiliteChampsAVoir.get(classe).get(type).put(champ, vue.isChampModifiable(classe, type, champ));
      }
      stylesElements.put(classe, new HashMap<String, SimpleAttributeSet>());
      for (String type : vue.getTypesAVoir(classe))
        if (vue.getStyleElements(classe, type)!=null)
          stylesElements.get(classe).put(type, vue.getStyleElements(classe, type));
      stylesValeursElements.put(classe, new HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>());
      for (String type : vue.getTypesAVoir(classe))
        if (vue.getStyleValeursElements(classe, type)!=null)
          stylesValeursElements.get(classe).put(type, vue.getStyleValeursElements(classe, type));
    }
    // Tableau des types d'éléments à visualiser
    Box panneauTypes = Box.createVerticalBox();

    panneauTypes.add(Box.createVerticalGlue());
    ModeleTableauTypes donneesTypes = new ModeleTableauTypes();
    tableauTypes = new JTable(donneesTypes) {
      @Override
      public boolean isCellSelected(int row, int col) {
        if (col==1) {
          return false;
        }
        String classeType = ((ModeleTableauTypes) getModel()).classesTypes[row];
        return isRowSelected(row)&&isTypesAVoir.get(GVisu.getClasse(classeType)).get(GVisu.getType(classeType));
      }
    };

    tableauTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    tableauTypes.getSelectionModel().addListSelectionListener(this);

    JScrollPane scroll1 = new JScrollPane(tableauTypes);

    scroll1.setBorder(
            new TitledBorder("Types d'éléments à visualiser"));
    panneauTypes.add(scroll1);
    // les boutons de validation

    panneauTypes.add(Box.createVerticalGlue());
    JPanel pCommandesValidation = new JPanel(new FlowLayout());
    JButton valideModifs = new JButton(
            new GAction("Valider") {
              public void executer() {
                validerModifVue();
              }
            ;
    });

    JButton annuleModifs = new JButton(
            new GAction("Annuler") {
              public void executer() {
                annulerModifVue();
              }
            ;

    });
    pCommandesValidation.add(valideModifs, CENTER_ALIGNMENT);

    pCommandesValidation.add(annuleModifs, CENTER_ALIGNMENT);

    panneauTypes.add(pCommandesValidation);
    // Les panneaux tabulés Extraction, Sélection, Présentation, Coloriage
    panneauTabule = new JTabbedPane();

    panneauTabule.addChangeListener(
            this);
    visuVueExtraction = new VisuVueExtraction(this, vue, champsAVoir, adressesAVoir);

    panneauTabule.add(
            "Extraction de propriétés", visuVueExtraction);
    visuVuePresentation = new VisuVuePresentation(this, vue, champsAVoir, champsId,
            modifiabiliteChampsAVoir, niveauxChampsAVoir, positionsChampsAVoir);

    panneauTabule.add(
            "Présentation des champs", visuVuePresentation);
    visuVueColoriage = new VisuVueColoriage(this, vue, champsAVoir, adressesAVoir,
            stylesElements, stylesValeursElements);

    panneauTabule.add(
            "Coloriage d'éléments", visuVueColoriage);
    // cadre global de la fenêtre
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauTypes, panneauTabule);

    setContentPane(split);

    setVisible(
            true);
    split.setDividerLocation(
            0.25);
//    visuVueExtraction.agencerPanneaux();
//
    // initialisation de la sélection d'un premier type à voir (si possible)


    for (int i = 0;
            i<donneesTypes.getRowCount();
            i++) {
      if ((Boolean) donneesTypes.getValueAt(i, 1)) {
        tableauTypes.setRowSelectionInterval(i, i);
        tableauTypes.setColumnSelectionInterval(0, 0);
        break;
      }
    }
    // Fermeture de la fenêtre

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    addWindowListener(
            new WindowAdapter() {
              @Override
              public void windowClosing(WindowEvent evt) {
                confirmerFermerFenetre();
              }
            });
  }

  public void stateChanged(ChangeEvent evt) {
    if (((JTabbedPane) evt.getSource())!=panneauTabule) return;
    try {
      switch (panneauTabule.getSelectedIndex()) {
        case 0:
          return;
        case 1:
          visuVuePresentation.reinitialiser();
          return;
        case 2:
          visuVueColoriage.reinitialiser();
          return;
        default:
          throw new IndexOutOfBoundsException();
      }
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  private void validerModifVue() {
    vue.modifierVueParSaisie(isTypesAVoir, adressesAVoir, champsAVoir, champsId, modifiabiliteChampsAVoir,
            positionsChampsAVoir, niveauxChampsAVoir, stylesElements, stylesValeursElements);
  }

  private void annulerModifVue() {
    fermerFenetre();
  }

  void ajouterChamp(Class<? extends Element> classe, String type, Adresse ad) {
    String champ = Vue.trouveNomChampParDefaut(ad, champsAVoir.get(classe).get(type));
    champsAVoir.get(classe).get(type).add(champ);
    adressesAVoir.get(classe).get(type).add(ad);
    modifiabiliteChampsAVoir.get(classe).get(type).put(champ, ad.getLongueur()==1);
    positionsChampsAVoir.get(classe).get(type).put(champ, 0);
    niveauxChampsAVoir.get(classe).get(type).put(champ, 0);
  }

  void renommerChamp(Class<? extends Element> classe, String type, int rg, String newNom) {
    String oldNom = champsAVoir.get(classe).get(type).get(rg);
    champsAVoir.get(classe).get(type).set(rg, newNom);
    if (champsId.get(classe).containsKey(type)&&oldNom.equals(champsId.get(classe).get(type))) {
      champsId.get(classe).put(type, newNom);
    }
    modifiabiliteChampsAVoir.get(classe).get(type).put(newNom,
            modifiabiliteChampsAVoir.get(classe).get(type).get(oldNom));
    modifiabiliteChampsAVoir.get(classe).get(type).remove(oldNom);
    positionsChampsAVoir.get(classe).get(type).put(newNom,
            positionsChampsAVoir.get(classe).get(type).get(oldNom));
    positionsChampsAVoir.get(classe).get(type).remove(oldNom);
    niveauxChampsAVoir.get(classe).get(type).put(newNom,
            niveauxChampsAVoir.get(classe).get(type).get(oldNom));
    niveauxChampsAVoir.get(classe).get(type).remove(oldNom);
    if (stylesValeursElements.get(classe).containsKey(type)
            &&stylesValeursElements.get(classe).get(type).containsKey(oldNom)) {
      stylesValeursElements.get(classe).get(type).
              put(newNom, stylesValeursElements.get(classe).get(type).get(oldNom));
      stylesValeursElements.get(classe).get(type).remove(oldNom);
    }
  }

  void supprimerChamp(Class<? extends Element> classe, String type, int rg) {
    String oldNom = champsAVoir.get(classe).get(type).get(rg);
    champsAVoir.get(classe).get(type).remove(rg);
    adressesAVoir.get(classe).get(type).remove(rg);
    if (champsId.get(classe).containsKey(type)&&oldNom.equals(champsId.get(classe).get(type))) {
      champsId.get(classe).remove(type);
      modifiabiliteChampsAVoir.get(classe).get(type).remove(oldNom);
      positionsChampsAVoir.get(classe).get(type).remove(oldNom);
      niveauxChampsAVoir.get(classe).get(type).remove(oldNom);
      if (stylesValeursElements.get(classe).containsKey(type)
              &&stylesValeursElements.get(classe).get(type).containsKey(oldNom)) {
        stylesValeursElements.get(classe).get(type).remove(oldNom);
      }
    }
  }
  String getPropChamp(Class<? extends Element> classe, String type, String champ) {
    ArrayList<String> champs = champsAVoir.get(classe).get(type);
    if(champs==null) return "";
    int ind = champs.indexOf(champ);
    if(ind<0) return "";
     String prop = adressesAVoir.get(classe).get(type).get(ind).prop;
     if(prop==null) return "";
     return prop;
   
  }

  public void valueChanged(ListSelectionEvent evt) {
    if ((ListSelectionModel) evt.getSource()!=tableauTypes.getSelectionModel())
      return;
    try {
      if (evt.getValueIsAdjusting()) {
        return;
      }
      int row = ((ListSelectionModel) evt.getSource()).getMinSelectionIndex();
      if (row<0) {
        visuVueExtraction.setTypeSelected(null, null);
        visuVuePresentation.setTypeSelected(null, null);
        visuVueColoriage.setTypeSelected(null, null);
      } else {
        String classeType = ((ModeleTableauTypes) tableauTypes.getModel()).classesTypes[row];
        Class<? extends Element> classeSelected = GVisu.getClasse(classeType);
        String typeSelected = GVisu.getType(classeType);
        if (isTypesAVoir.get(classeSelected).get(typeSelected)) {
          visuVueExtraction.setTypeSelected(classeSelected, typeSelected);
          visuVuePresentation.setTypeSelected(classeSelected, typeSelected);
          visuVueColoriage.setTypeSelected(classeSelected, typeSelected);
        } else {
          visuVueExtraction.setTypeSelected(null, null);
          visuVuePresentation.setTypeSelected(null, null);
          visuVueColoriage.setTypeSelected(null, null);
        }
      }
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  public void traiterEvent(Message evt) {
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        fermerFenetre();
        return;
      case NEW_CORPUS:  // impossible en principe : CLEAR_CORPUS a détruit la fenêtre
        throw new UnsupportedOperationException("Fenêtre vue présente lors d'un message NEW_CORPUS ???");
      case MODIF_TEXTE:
        return;
      case MODIF_VUE:
        fermerFenetre();  // ferme même si la modif vient d'ici
        return;
      case MODIF_STRUCTURE:
        fermerFenetre();
        return;
      case MODIF_ELEMENT:
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }
  }

  private void confirmerFermerFenetre() {
    try {
      int rep = JOptionPane.showOptionDialog(this, "Valider les modifications ?",
              "Fermeture de la fenêtre \"Structure des annotations\"", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Oui", "Non", "Annuler"}, "Non");
      if (rep==2) {
        return;
      }
      if (rep==0) {
        validerModifVue();
      }
      annulerModifVue();
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  private void fermerFenetre() {
    vue.removeEventListener(this);
    dispose();


  }

  private class ModeleTableauTypes extends AbstractTableModel {
    String[] classesTypes;

    private ModeleTableauTypes() {
      super();
      classesTypes = GVisu.getClassesTypes(vue.getTypes(Unite.class), vue.getTypes(Relation.class), vue.getTypes(Schema.class));
    }

    public int getColumnCount() {
      return 2;
    }

    public int getRowCount() {
      return classesTypes.length;
    }

    public Object getValueAt(int row, int col) {
      String classeType = classesTypes[row];
      return col==0 ? classeType : isTypesAVoir.get(GVisu.getClasse(classeType)).get(GVisu.getType(classeType));
    }

    @Override
    public String getColumnName(int col) {
      return col==0 ? "Types d'éléments" : "à voir";
    }

    @Override
    public Class getColumnClass(int col) {
      return col==0 ? String.class : Boolean.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
      return col==1;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
      if (col==1) {
        String classeType = classesTypes[row];
        isTypesAVoir.get(GVisu.getClasse(classeType)).put(GVisu.getType(classeType), (Boolean) aValue);
        tableauTypes.clearSelection();  // important : à faire même quand aValue est vrai
        // pour que l'événement "ListSelection change" soit émis dans tous les cas
        if ((Boolean) aValue) {
          tableauTypes.setRowSelectionInterval(row, row);
          tableauTypes.setColumnSelectionInterval(0, 0);
        }
      }
    }
  }
}
