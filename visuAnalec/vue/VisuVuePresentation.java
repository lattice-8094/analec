/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.vue;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import visuAnalec.donnees.Structure;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuVuePresentation extends JPanel implements ActionListener {
  private VisuVue visuVue;
  private Vue vue;
  private JTable tableauChampsAVoir;
  private JComboBox saisieChampId;
  private Class<? extends Element> classeSelected;
  private String typeSelected;
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> modifiabiliteChampsAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> niveauxChampsAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> positionsChampsAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, String>> champsId;
  VisuVuePresentation(VisuVue visuVue, Vue vue,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, String>> champsId,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Boolean>>> modifiabiliteChampsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> niveauxChampsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, Integer>>> positionsChampsAVoir) {
    super(new BorderLayout());
    this.visuVue = visuVue;
    this.vue = vue;
    this.champsAVoir = champsAVoir;
    this.champsId = champsId;
    this.modifiabiliteChampsAVoir = modifiabiliteChampsAVoir;
    this.niveauxChampsAVoir = niveauxChampsAVoir;
    this.positionsChampsAVoir = positionsChampsAVoir;
    classeSelected = null;
    typeSelected = null;
    // Panneau du champ identifiant
    saisieChampId = new JComboBox();
    saisieChampId.setEditable(false);
    saisieChampId.addActionListener(this);
    JPanel panneauChampId = new JPanel();
    panneauChampId.add(new JLabel("Choix d'un champ identifiant"));
    panneauChampId.add(saisieChampId);
    add(panneauChampId, BorderLayout.NORTH);
    // Le tableau des champs à visualiser
    ModeleTableauChampsAVoir donneesChampsAVoir = new ModeleTableauChampsAVoir();
    tableauChampsAVoir = new JTable(donneesChampsAVoir);
    JScrollPane scroll2 = new JScrollPane(tableauChampsAVoir);
    JPanel panneauChampsAVoir = new JPanel();
    panneauChampsAVoir.add(scroll2);
    add(panneauChampsAVoir, BorderLayout.CENTER);
  }
  void setTypeSelected(Class<? extends Element> classe, String type) {
    classeSelected = classe;
    typeSelected = type;
    reinitialiser();
  }
  void reinitialiser() {
    ((ModeleTableauChampsAVoir) tableauChampsAVoir.getModel()).fireTableDataChanged();
    saisieChampId.removeActionListener(this);
    if (typeSelected==null) saisieChampId.setModel(new DefaultComboBoxModel());
    else {
      String[] champs = champsAVoir.get(classeSelected).get(typeSelected).toArray(new String[0]);
      champs = Tableaux.insererStringEnTete(champs, "<aucun>");
      saisieChampId.setModel(new DefaultComboBoxModel(champs));
      if (champsId.get(classeSelected).containsKey(typeSelected))
        saisieChampId.setSelectedItem(champsId.get(classeSelected).get(typeSelected));
      else saisieChampId.setSelectedIndex(0);
    }
    saisieChampId.addActionListener(this);
  }
  public void actionPerformed(ActionEvent evt) {
    try {
      if (evt.getSource()==saisieChampId) {
        if (saisieChampId.getSelectedIndex()==0)
          champsId.get(classeSelected).remove(typeSelected);
        else
          champsId.get(classeSelected).put(typeSelected, (String) saisieChampId.getSelectedItem());
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.paramString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  private static final String[] nomsColonnes = new String[]{
    "<html><span style=\"font-weight:900 \">Champ</span>",
    "<html><span style=\"font-weight:900 \">Niveau</span>",
    "<html><span style=\"font-weight:900 \">Position</span>",
    "<html><span style=\"font-weight:900 \">Modifiable ?</span>"
  };
  private static final Class[] classesColonnes = new Class[]{String.class, Integer.class, Integer.class, Boolean.class};

  private class ModeleTableauChampsAVoir extends AbstractTableModel {
    public int getColumnCount() {
      return nomsColonnes.length;
    }
    public int getRowCount() {
      return (typeSelected==null||!champsAVoir.get(classeSelected).containsKey(typeSelected)) ? 0 : champsAVoir.get(classeSelected).get(typeSelected).size();
    }
    public Object getValueAt(int row, int col) {
      String champ = champsAVoir.get(classeSelected).get(typeSelected).get(row);
      switch (col) {
        case 0:
          return champ;
        case 1:
          return niveauxChampsAVoir.get(classeSelected).get(typeSelected).get(champ);
        case 2:
          return positionsChampsAVoir.get(classeSelected).get(typeSelected).get(champ);
        case 3:
          return modifiabiliteChampsAVoir.get(classeSelected).get(typeSelected).get(champ);
        default:
          throw new IndexOutOfBoundsException();
      }
    }
    @Override
    public String getColumnName(int col) {
      return nomsColonnes[col];
    }
    @Override
    public Class getColumnClass(int col) {
      return classesColonnes[col];
    }
    @Override
    public boolean isCellEditable(int row, int col) {
      if (col==3&&classeSelected==Unite.class) {
        String champ = champsAVoir.get(classeSelected).get(typeSelected).get(row);
        String prop = visuVue.getPropChamp(classeSelected, typeSelected, champ);
        if (Structure.FORME.equals(prop)) return false;
      }
      return col>0;
    }
    @Override
    public void setValueAt(Object aValue, int row, int col) {
      String champ = champsAVoir.get(classeSelected).get(typeSelected).get(row);
      switch (col) {
        case 1:
          niveauxChampsAVoir.get(classeSelected).get(typeSelected).put(champ, (Integer) aValue);
          return;
        case 2:
          positionsChampsAVoir.get(classeSelected).get(typeSelected).put(champ, (Integer) aValue);
          return;
        case 3:
          modifiabiliteChampsAVoir.get(classeSelected).get(typeSelected).put(champ, (Boolean) aValue);
          return;
        default:
          throw new IndexOutOfBoundsException();
      }
    }
  }
}
