/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.statistiques;

//import donnees.*;
import java.util.*;
import javax.swing.table.*;
import visuAnalec.elements.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public class ModeleStatsFrequences extends AbstractTableModel {
  private String[] nomsValeurs;
  private int[] nbOcc;
  private String[] nomsColonnes;
  private int totalOcc;
 public ModeleStatsFrequences() {
    super();
    initialise();
  }
private void initialise() {
    nomsValeurs = new String[0];
    nbOcc = new int[]{0};
    nomsColonnes = new String[]{"", "Nb d'occurrences", "Fréquence (%)"};
    totalOcc = 0;
  }
  public int getRowCount() {
    return nomsValeurs.length + 2;
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
        return row == nomsValeurs.length + 1 ? "TOTAL" : row == nomsValeurs.length ? "<aucune valeur>" : nomsValeurs[row];
      case 1:
        return row == nomsValeurs.length + 1 ? totalOcc : nbOcc[row];
      case 2:
        return row == nomsValeurs.length + 1 ? 100.0f : Math.round(nbOcc[row] * 10000.0f / totalOcc) / 100.0f;
      default:
        return null;
    }
  }
  Element[] getListeElementsRowsSelected(int[] rows, Vue vue, Class<? extends Element> classe, String type,
          String champCible, String champFiltre, String[] valeursChampFiltre) {
    if (rows.length == 0 || classe==null) return (new Unite[0]);
    ArrayList<String> vals = new ArrayList<String>();
    for (int row : rows) {
      if (row == nomsValeurs.length + 1) {
        return vue.getDoubleSelectionOccurrences(classe, type, null, vals.toArray(new String[0]), 
                champFiltre, valeursChampFiltre);
      }
      vals.add(row == nomsValeurs.length ? "" : nomsValeurs[row]);
    }
    return vue.getDoubleSelectionOccurrences(classe, type, champCible, vals.toArray(new String[0]), 
            champFiltre, valeursChampFiltre);
  }
  private void setModeleVide() {
    initialise();
    fireTableDataChanged();
  }
  void setModele(Vue vue, Class<? extends Element> classe, String type, String champCible,
          String champFiltre, String[] valeursChampFiltre) {
    if (classe==null || champCible == null || vue.getNbElementsAVoir(classe, type) == 0) {
      setModeleVide();
      return;
    }
    nomsValeurs = vue.getValeursChamp(classe, type, champCible);
    nbOcc = new int[nomsValeurs.length + 1];
    HashMap<String, Integer> compte =
            vue.compterOccurrencesFiltrees(classe, type, champCible, champFiltre, valeursChampFiltre);
    totalOcc = 0;
    for (int row = 0; row < nomsValeurs.length + 1; row++) {
      nbOcc[row] = compte.get(row == nomsValeurs.length ? "" : nomsValeurs[row]);
      totalOcc += nbOcc[row];
    }
    fireTableDataChanged();
  }
}

