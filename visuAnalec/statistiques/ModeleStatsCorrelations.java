/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.statistiques;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import visuAnalec.elements.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public class ModeleStatsCorrelations extends AbstractTableModel {
  private String[] valeursChamp2;
  private String[] valeursChamp1;
  private int[][] nbOcc;
  private int[] totauxChamp1;
  private int[] totauxChamp2;
  private int totalTotaux;
  public ModeleStatsCorrelations() {
    super();
    initialise();
  }
  private void initialise() {
    valeursChamp2 = new String[0];
    valeursChamp1 = new String[0];
    nbOcc = new int[1][1];
    totauxChamp1 = new int[1];
    totauxChamp2 = new int[1];
    totalTotaux = 0;
  }
  public int getRowCount() {
    return valeursChamp2.length + 2;
  }
  public int getColumnCount() {
    return valeursChamp1.length + 3;
  }
  @Override
  public String getColumnName(int col) {
    return col == 0 ? "" : col == valeursChamp1.length + 2 ? "TOTAL" : col == valeursChamp1.length + 1 ? "<aucune valeur>" : valeursChamp1[col - 1];
  }
  public Object getValueAt(int row, int col) {
    if (col == 0) {
      return row == valeursChamp2.length + 1 ? "TOTAL" : row == valeursChamp2.length ? "<aucune valeur>" : valeursChamp2[row];
    } else if (col == valeursChamp1.length + 2) {
      return row == valeursChamp2.length + 1 ? totalTotaux : totauxChamp1[row];
    } else {
      return row == valeursChamp2.length + 1 ? totauxChamp2[col - 1] : nbOcc[row][col - 1];
    }
  }
  Element[] getListeElementsSelected(Integer[] rows, Integer[] cols, Vue vue, Class<? extends Element> classe, 
          String type, String champ1, String champ2) {
    HashSet<Element> liste = new HashSet<Element>();
    for (int i = 0; i < rows.length; i++) {
      if(cols[i]==0) continue;
      if (rows[i] == valeursChamp2.length + 1) {
        if (cols[i] == valeursChamp1.length + 2) {
          return vue.getElementsAVoir(classe, type);
        } else {
          String[] val1 = cols[i] == valeursChamp1.length + 1 ? new String[]{""} : new String[]{valeursChamp1[cols[i] - 1]};
          liste.addAll(Arrays.asList(vue.getDoubleSelectionOccurrences(classe, type,champ1, val1, null, null)));
        }
      } else {
        String[] val2 = rows[i] == valeursChamp2.length ? new String[]{""} : new String[]{valeursChamp2[rows[i]]};
        if (cols[i] == valeursChamp1.length + 2) {
          liste.addAll(Arrays.asList(vue.getDoubleSelectionOccurrences(classe, type, champ2, val2, null, null)));
        } else {
          String[] val1 = cols[i] == valeursChamp1.length + 1 ? new String[]{""} : new String[]{valeursChamp1[cols[i] - 1]};
          liste.addAll(Arrays.asList(vue.getDoubleSelectionOccurrences(classe, type, champ2, val2, champ1, val1)));

        }
      }
    }
    Element[] tab = liste.toArray(new Element[0]);
    Arrays.sort(tab);
    return tab;
  }
  void setModele(Vue vue, Class<? extends Element> classe, String type, String champ1, String champ2) {
    if (classe == null) {
      setModeleVide();
      return;
    }
    valeursChamp1 = champ1 == null ? new String[0] : vue.getValeursChamp(classe, type, champ1);
    valeursChamp2 = champ2 == null ? new String[0] : vue.getValeursChamp(classe, type, champ2);
    nbOcc = new int[valeursChamp2.length + 1][valeursChamp1.length + 1];
    totauxChamp1 = new int[valeursChamp2.length + 1];
    totauxChamp2 = new int[valeursChamp1.length + 1];
    HashMap<String, HashMap<String, Integer>> compte =
            vue.compterOccurrencesCorrelees(classe, type, champ1, champ2);
    totalTotaux = 0;
    for (int col = 0; col < valeursChamp1.length + 1; col++) {
      HashMap<String, Integer> hm = compte.get(col == valeursChamp1.length ? "" : valeursChamp1[col]);
      for (int row = 0; row < valeursChamp2.length + 1; row++) {
        nbOcc[row][col] = hm.get(row == valeursChamp2.length ? "" : valeursChamp2[row]);
        totauxChamp2[col] += nbOcc[row][col];
        totauxChamp1[row] += nbOcc[row][col];
        totalTotaux += nbOcc[row][col];
      }
    }
    fireTableStructureChanged();
  }
  private void setModeleVide() {
    initialise();
    fireTableDataChanged();
  }
}

class CorrelationsRenderer extends DefaultTableCellRenderer {
  ModeleStatsCorrelations donnees;
  static Color GRIS_CLAIR = new Color(0.95f, 0.95f, 0.95f);
  static Color BLEU_CLAIR = new Color(0.9f, 0.9f, 1.0f);
  public CorrelationsRenderer(ModeleStatsCorrelations donnees) {
    this.donnees = donnees;
  }
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int col) {
    int colTotal = donnees.getColumnCount() - 1;
    int rowTotal = donnees.getRowCount() - 1;
    int totalTotaux = (Integer) donnees.getValueAt(rowTotal, colTotal);
    setBackground(Color.WHITE);
    setHorizontalTextPosition(JLabel.CENTER);
    if (col == colTotal || row == rowTotal) {
      setBackground(BLEU_CLAIR);
    } else if (col == 0) {
      setBackground(GRIS_CLAIR);
      setHorizontalTextPosition(JLabel.LEFT);
    } else if (totalTotaux > 0) {
      int c = (Integer) donnees.getValueAt(row, col);
      float m = ((Integer) donnees.getValueAt(row, colTotal)) * ((Integer) donnees.getValueAt(rowTotal, col)) /
              totalTotaux * 1.0f;
      if (c / m > 1.0f) {
        float corr = Math.min(c / m - 1.0f, 2.0f) * 0.4f;
        setBackground(new Color(1.0f, 1.0f - corr, 1.0f - corr));
      }
    }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
  }
}


