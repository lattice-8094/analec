/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.texte;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import visuAnalec.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public class PanneauOccurrences extends JTable implements ListSelectionListener {
  ModeleTableauOcc modele;
  ModeleTableur modeleTableur;
  Vue vue;
  PanneauEditeur pEditeur;
  Element[] listElt;
  final static int EMPAN = 80;  // paramètre à saisir (un jour)
  public PanneauOccurrences(Vue vue, PanneauEditeur pEditeur) {
    super();
    modele = new ModeleTableauOcc();
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setModel(modele);
    modeleTableur = new ModeleTableur();
    getSelectionModel().addListSelectionListener(this);
    this.vue = vue;
    this.pEditeur = pEditeur;
  }
  public TableModel getModeleTableur() {
    return modeleTableur;
  }
  public void initVide() {
    listElt = new Element[0];
    getTableHeader().setVisible(false);
    modele.clear();
    modeleTableur.clear();
  }
  public void init(Class<? extends Element> classe, String type, Element[] listElt) {
    this.listElt = listElt;
    if (listElt.length==0) {
      initVide();
      return;
    }
    getTableHeader().setVisible(true);
    String[] ids = vue.getIdListeElements(classe, type, listElt);
    String[][] concordances = vue.getCorpus().getConcordances(listElt, EMPAN);
    String[] champs = vue.getNomsChamps(classe, type);
    String[][] valeurs = vue.getValeursChampsListeElements(champs, listElt);
    modele.setModele(ids, concordances);
    modeleTableur.setModele(ids, concordances, champs, valeurs);
    getColumnModel().getColumn(0).setPreferredWidth(150);
    getColumnModel().getColumn(1).setMinWidth(1200);
  }
  @Override
  public void valueChanged(ListSelectionEvent evt) {
    try {
      if (evt.getValueIsAdjusting()) return;
      if (evt.getSource().equals(getSelectionModel())) {
        int row = getSelectedRow();
        if (row>=0) pEditeur.editerElement(listElt[row]);
        return;
      }
      throw new UnsupportedOperationException("Evénement non prévu : "+evt.toString());
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  private static class ModeleTableauOcc extends AbstractTableModel {
    final static String STYLE_HTML = "<html>";
    final static String STYLE_DEB_SELECT =
            "<span style=\"font-weight:bold; background-color:#FFFF80\">";
    final static String STYLE_FIN_SELECT = "</span>";
    String[] ids;
    String[] extraits;
    public ModeleTableauOcc() {
      super();
      clear();
    }
    public int getRowCount() {
      return ids.length;
    }
    public int getColumnCount() {
      return 2;
    }
    @Override
    public String getColumnName(int columnIndex) {
      return columnIndex==0 ? "Identifiants" : "Occurrences";
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
      return columnIndex==0 ? ids[rowIndex] : extraits[rowIndex];

    }
    private void clear() {
      ids = new String[0];
    }
    private void setModele(String[] ids, String[][] concordances) {
      this.ids = ids;
      extraits = new String[ids.length];
      for (int i = 0; i<ids.length; i++)
        extraits[i] = formerExtrait(concordances[i]);
      fireTableDataChanged();
    }
    private String formerExtrait(String[] concordance) {
      return STYLE_HTML+concordance[0]+STYLE_DEB_SELECT+concordance[1]
              +STYLE_FIN_SELECT+concordance[2];
    }
  }

  private static class ModeleTableur extends AbstractTableModel {
    String[] ids;
    String[][] concordances;
    String[] champs;
    String[][] valeurs;
    public ModeleTableur() {
      super();
      clear();
    }
    public int getRowCount() {
      return ids.length;
    }
    public int getColumnCount() {
      return 4+champs.length;
    }
    @Override
    public String getColumnName(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return "Identifiants";
        case 1:
          return "Contexte gauche";
        case 2:
          return "Elément";
        case 3:
          return "Contexte droit";
        default:
          return champs[columnIndex-4];
      }
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return ids[rowIndex];
        case 1:
        case 2:
        case 3:
          return concordances[rowIndex][columnIndex-1];
        default:
          return valeurs[rowIndex][columnIndex-4];
      }

    }
    private void clear() {
      champs = ids = new String[0];
    }
    private void setModele(String[] ids, String[][] concordances,
            String[] champs, String[][] valeurs) {
      this.ids = ids;
      this.concordances = concordances;
      this.champs = champs;
      this.valeurs = valeurs;
      fireTableDataChanged();
    }
  }
}
