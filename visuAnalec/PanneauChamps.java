/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.vue.*;

import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.GlazedLists;
/**
 *
 * @author Bernard
 */
public class PanneauChamps extends JPanel {
  ArrayList<GChamp> champs = new ArrayList<GChamp>();
  private Vue vue;
  private Element element = null;
  private Class<? extends Element> classe = null;
  private String type = null;
  private JPanel cadre;
  public PanneauChamps(PanneauEditeur pEdit, Vue vue) {
    super(new BorderLayout());
    setBorder(new TitledBorder("Champs"));
    cadre = new JPanel(new GridLayout(0, 1));
    JScrollPane scroll = new JScrollPane(cadre);
    add(scroll, BorderLayout.CENTER);
    this.vue = vue;
  }
  void reafficher() {
    if (element == null) return;
    type = null;
    afficher(element);
  }
  void afficher(Element elt) {
    this.element = elt;
    if (elt.getClass() != classe || !elt.getType().equals(type)) {
      type = elt.getType();
      classe = elt.getClass();
      afficherTouslesChamps();
    }
    ((TitledBorder) getBorder()).setTitle("Champs " + GVisu.getNomDeLaClasse(elt) + " :  \"" +
            vue.getIdElement(elt) + "\"");
    for (GChamp gc : champs) {
      gc.afficher(elt);
    }
    revalidate();
    repaint();
  }
  void effacer() {
    cadre.removeAll();
    type = null;
    element = null;
    ((TitledBorder) getBorder()).setTitle("Champs");
    revalidate();
    repaint();
  }
  private void afficherTouslesChamps() {
    cadre.removeAll();
    champs.clear();

    int niveau = 0;
    for (String[] champsniveau : vue.getChamps(classe, type)) {
      JPanel panneauniveau = new JPanel();
 //     panneauniveau.setBorder(new TitledBorder("Niveau " + ++niveau));
      for (String champ : champsniveau) {
        GChamp gc = new GChamp(vue, classe, type, champ);
        champs.add(gc);
        panneauniveau.add(gc);
      }
      cadre.add(panneauniveau);
    }
  }

  private static class GChamp extends JPanel implements ActionListener {
    JComboBox afficheValeurChamp;
    Vue vue;
    Class<? extends Element> classe;
    String type;
    String champ;
    Element element = null;
    GChamp(Vue vue, Class<? extends Element> classe, String type, String champ) {
      super();
      this.vue = vue;
      this.classe = classe;
      this.type = type;
      this.champ = champ;
      Box groupe = Box.createVerticalBox();
      Box titre = Box.createHorizontalBox();
      titre.add(Box.createHorizontalGlue());
      JLabel label = new JLabel(champ, JLabel.CENTER);
 //     label.setPreferredSize(new Dimension(50, 10));
      titre.add(label);
      titre.add(Box.createHorizontalGlue());
      groupe.add(titre);
      groupe.add(Box.createVerticalStrut(2));
      afficheValeurChamp = new JComboBox(vue.getValeursChamp(classe, type, champ));
      /*
      CP: JComboBox avec autocompletion. Dépend de la lib glazedlists
      (http://www.glazedlists.com/), (https://github.com/glazedlists/glazedlists)
      */
      AutoCompleteSupport support = AutoCompleteSupport.install( afficheValeurChamp, GlazedLists.eventListOf(vue.getValeursChamp(classe, type, champ)));
      afficheValeurChamp.setEditable(vue.isChampModifiable(classe, type, champ));
      afficheValeurChamp.addActionListener(this);
      groupe.add(afficheValeurChamp);
      add(groupe);
    }
    void afficher(Element elt) {
      this.element = elt;
      afficheValeurChamp.removeActionListener(this);
      String val = vue.getValeurChamp(elt, champ);
      if (val.isEmpty()) afficheValeurChamp.setSelectedIndex(-1);
      else afficheValeurChamp.setSelectedItem(val);
      afficheValeurChamp.addActionListener(this);
    }
    public void actionPerformed(ActionEvent evt) {
      try {
        if (!afficheValeurChamp.isEditable()) {
          JOptionPane.showMessageDialog(getTopLevelAncestor(), "Champ non modifiable pour ce type d'éléments",
                  "Modification impossible", JOptionPane.ERROR_MESSAGE);
          afficher(element);
          return;
        }
        String newval = ((String) afficheValeurChamp.getSelectedItem());
        if (newval == null) newval = "";
        else newval = newval.trim();
        if (!vue.setValeurChamp(element, champ, newval)) {
          JOptionPane.showMessageDialog(getTopLevelAncestor(), "Champ non défini pour cet élément",
                  "Saisie de valeur de champ impossible", JOptionPane.ERROR_MESSAGE);
          afficher(element);
        }
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
  }
}
