/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import visuAnalec.elements.*;
import visuAnalec.util.GMessages.*;

/**
 *
 * @author Bernard
 */
public abstract class GVisu {
  public static class TitreBarre extends JLabel {
    public TitreBarre(String titre) {
      super(titre);
    }
    public TitreBarre(String titre, Color couleur) {
      super(titre);
      setOpaque(true);
      setBackground(couleur);
    }
  }

  public static abstract class Boutons {
    public static void setDefaut(JButton bouton, boolean oui) {
      ((JFrame) bouton.getTopLevelAncestor()).getRootPane().setDefaultButton(oui ? bouton : null);
      if (oui) bouton.requestFocusInWindow();
    }
  }
  public static String styleIdPopup(String texte, String id) {
    if (texte.length()>35)
      texte = texte.substring(0, 18)+" [...] "+texte.substring(texte.length()-8);
    return "<html>"+id+" (<span style=\"font-style:italic \">"+texte+"</span>)";
  }
  public static String styleIdTooltip(ArrayList<String> textes, ArrayList<String> ids) {
    String tooltip = "<html>";
    for (int i = 0; i<textes.size(); i++) {
      String texte = textes.get(i);
      if (texte.length()>35)
        texte = texte.substring(0, 18)+" [...] "+texte.substring(texte.length()-8);
      tooltip += "<p>"+ids.get(i)+" (<span style=\"font-style:italic \">"+texte+"</span>)";
    }
    return tooltip;
  }
  public static String styleDef(String nom) {
    return "<html><span style=\"color:gray\"> &lt <span style=\"font-style:italic \">"
            +nom+"</span> &gt";
  }
  private final static Font STYLE_COMBO = Font.decode("Arial plain 12");

  public enum SensSpinnerBinaire {
    SUIVANT, PRECEDENT
  }

  public static class ModeleSpinnerBinaire extends AbstractSpinnerModel {
    private SensSpinnerBinaire sens;
    public Object getValue() {
      return sens;
    }
    public void setValue(Object value) {
      sens = (SensSpinnerBinaire) value;
      fireStateChanged();
    }
    public Object getNextValue() {
      return SensSpinnerBinaire.PRECEDENT;
    }
    public Object getPreviousValue() {
      return SensSpinnerBinaire.SUIVANT;
    }
  }
  private static Dimension dimZero = new Dimension(20, 20);
  private static Insets insetsZero = new Insets(0, 0, 0, 0);

  public static class ElementComboBox extends Box implements ActionListener, ChangeListener {
    private ArrayList<ActionListener> listeCorrespondants = new ArrayList<ActionListener>();
    public JComboBox combo;
    private JButton zero;
    private JSpinner spin;
    public Element[] elements;  // liste triée
    public ElementComboBox() {
      super(BoxLayout.X_AXIS);
      zero = new JButton(new GAction("0") {
        public void executer() {
          effSelection();
        }
      });
      zero.setMargin(insetsZero);
      zero.setPreferredSize(dimZero);
      spin = new JSpinner(new ModeleSpinnerBinaire());
      spin.setEditor(new JLabel());
      spin.addChangeListener(this);
      combo = new JComboBox();
      combo.setEditable(false);
      combo.setFont(STYLE_COMBO);
      combo.addActionListener(this);
      add(combo);
      add(spin);
      add(zero);
      setModeleVide();
    }
    public void addActionListener(ActionListener correspondant) {
      listeCorrespondants.add(correspondant);
    }
    public void removeActionListener(ActionListener correspondant) {
      listeCorrespondants.remove(correspondant);
    }
    public void actionPerformed(ActionEvent evt) {
      try {
        evt.setSource(this);
        for (Object corresp : listeCorrespondants.toArray()) {
          ((ActionListener) corresp).actionPerformed(evt);
        }
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
    public void setModeleVide() {
      combo.setModel(new DefaultComboBoxModel(new String[]{styleDef("identifiant")}));
      elements = new Element[0];
      setMaximumSize(getPreferredSize());
    }
    public void setModele(Element[] elts, String[] ids) {
      // elts doit être trié et en correspondance avec ids
      combo.setModel(new DefaultComboBoxModel(Tableaux.insererStringEnTete(ids, styleDef("identifiant"))));
      elements = elts;
      setMaximumSize(getPreferredSize());
    }
    public Element[] getElements() {
      return elements;
    }
    public void gotoPrecedent() {
      int index;
      if ((index = combo.getSelectedIndex())==0) index = combo.getItemCount();
      combo.setSelectedIndex(index-1);
    }
    public void gotoSuivant() {
      int index = combo.getSelectedIndex()+1;
      if (index==combo.getItemCount()) index = 0;
      combo.setSelectedIndex(index);
    }
    public boolean pasdeSelection() {
      return combo.getSelectedIndex()<1;
    }
    public Element getSelection() {
      if (combo.getSelectedIndex()>0)
        return elements[combo.getSelectedIndex()-1];
      return null;
    }
    public void effSelection() {
      combo.setSelectedIndex(0);
    }
    public void setSelection(Element elt) {
      int no = Arrays.binarySearch(elements, elt);
      if (no<0)
        throw new ArrayIndexOutOfBoundsException("Element à sélectionner absent");
      combo.setSelectedIndex(no+1);
    }
    public String getIdElement(Element elt) {
      int no = Arrays.binarySearch(elements, elt);
      if (no<0)
        throw new ArrayIndexOutOfBoundsException("Element recherché absent");
      return ((String) combo.getItemAt(no+1));

    }
    public void stateChanged(ChangeEvent e) {
      try {
        switch ((SensSpinnerBinaire) spin.getValue()) {
          case SUIVANT:
            gotoSuivant();
            return;
          case PRECEDENT:
            gotoPrecedent();
            return;
        }
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
  }

  public static class Val0ComboBox extends JComboBox {
    private String[] vals;
    private String val0;
    public Val0ComboBox(String val0) {
      super();
      this.val0 = val0;
      setEditable(false);
      setFont(STYLE_COMBO);
      setModeleVide();
    }
    public void setModeleVide() {
      setModel(new DefaultComboBoxModel(new String[]{styleDef(val0)}));
      vals = new String[0];
      setMaximumSize(getPreferredSize());
    }
    public void setModele(String[] types) {
      setModel(new DefaultComboBoxModel(Tableaux.insererStringEnTete(types, styleDef(val0))));
      this.vals = types;
      setMaximumSize(getPreferredSize());
    }
    public boolean pasdeSelection() {
      return getSelectedIndex()<1;
    }
    public int nbItems() {
      return vals.length;
    }
    public String getSelection() {
      if (getSelectedIndex()>0) return vals[getSelectedIndex()-1];
      return null;
    }
    public void effSelection() {
      setSelectedIndex(0);
    }
    public void setSelection(String type) {
      setSelectedItem(type);
    }
    public void setSelection(int i) {
      setSelectedIndex(i+1);
    }
  }
  public static String getNomClasse(Class<? extends Element> classe) {
    if (classe==Element.class) return "élément";
    else if (classe==Unite.class) return "unité";
    else if (classe==Relation.class) return "relation";
    else if (classe==Schema.class) return "schéma";
    else
      throw new UnsupportedOperationException("Sous-classe inconnue pour getNomClasse()");
  }
  public static String getNomDeClasse(Class<? extends Element> classe) {
    if (classe==Element.class) return "d'élément";
    else if (classe==Unite.class) return "d'unité";
    else if (classe==Relation.class) return "de relation";
    else if (classe==Schema.class) return "de schéma";
    else
      throw new UnsupportedOperationException("Sous-classe inconnue pour getNomTypedeClasse()");
  }
  public static String getNomDeLaClasse(Element elt) {
    Class<? extends Element> classe = elt.getClass();
    if (classe==Unite.class) return "de l'unité";
    else if (classe==Relation.class) return "de la relation";
    else if (classe==Schema.class) return "du schéma";
    else
      throw new UnsupportedOperationException("Sous-classe inconnue pour getNomTypedeClasse()");
  }
  public static Class<? extends Element> getClasse(String classeType) {
    if (classeType==null) return null;
    if (classeType.startsWith("REL-")) return Relation.class;
    if (classeType.startsWith("SCH-")) return Schema.class;
    return Unite.class;
  }
  public static String getType(String classeType) {
    if (classeType==null) return null;
    if (classeType.startsWith("REL-")) return classeType.substring(4);
    if (classeType.startsWith("SCH-")) return classeType.substring(4);
    return classeType;
  }
  private static String getClasseType(Class<? extends Element> classe, String type) {
    if (classe==Unite.class) return type;
    else if (classe==Relation.class) return "REL-"+type;
    else if (classe==Schema.class) return "SCH-"+type;
    else
      throw new UnsupportedOperationException("Sous-classe inconnue pour getClasseType()");
  }
  public static String[] getClassesTypes(String[] typesUnit, String[] typesRel, String[] typesSch) {
    String[] types = Arrays.copyOf(typesUnit, typesUnit.length+typesRel.length+typesSch.length);
    for (int i = 0; i<typesRel.length; i++)
      types[i+typesUnit.length] = "REL-"+typesRel[i];
    for (int i = 0; i<typesSch.length; i++)
      types[i+typesUnit.length+typesRel.length] = "SCH-"+typesSch[i];
    return types;
  }

  public static class TypeComboBox extends Val0ComboBox {
    private Class<? extends Element> classe;
    public TypeComboBox(Class<? extends Element> classe) {
      super("type "+getNomDeClasse(classe));
      this.classe = classe;
    }
    @Override
    public void setModele(String[] types) {
      if (this.classe==Element.class)
        throw new UnsupportedOperationException("pas pour la classe Element");
      super.setModele(types);
    }
    public void setModele(String[] typesUnites, String[] typesRelations, String[] typesSchemas) {
      if (this.classe!=Element.class)
        throw new UnsupportedOperationException("uniquement pour la classe Element");
      super.setModele(getClassesTypes(typesUnites, typesRelations, typesSchemas));
    }
    public String getTypeSelection() {
      String classetype = getSelection();
      if (classetype==null) return null;
      if (classe==Element.class) return getType(classetype);
      return classetype;
    }
    public Class<? extends Element> getClasseSelection() {
      String classetype = getSelection();
      if (classetype==null) return null;
      if (classe==Element.class) return getClasse(classetype);
      return classe;
    }
    public void setSelection(Class<? extends Element> classe, String type) {
      if (this.classe!=Element.class)
        throw new UnsupportedOperationException("uniquement pour la classe Element");
      super.setSelection(getClasseType(classe, type));
    }
    @Override
    public void setSelection(String type) {
      if (this.classe==Element.class)
        throw new UnsupportedOperationException("pas pour la classe Element");
      super.setSelection(type);
    }
  }
  private final static Color[] couleursEtNoirEtBlanc = new Color[]{Color.BLACK, Color.WHITE, Color.YELLOW,
    Color.RED, Color.BLUE, Color.ORANGE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.PINK
  };
  private final static Color[] couleurs = new Color[]{Color.YELLOW, Color.RED, Color.BLUE,
    Color.ORANGE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.PINK
  };
  public static Color[] getCouleursEtNoirEtBlanc() {
    return couleursEtNoirEtBlanc;
  }
  public static Color getCouleur(int i) {
    Color c = couleurs[i%couleurs.length];
    for (int n = 0; n<i/couleurs.length; n++) {
      float[] rgb = c.getRGBColorComponents(null);
      c = new Color(0.499f+rgb[0]/3, 0.499f+rgb[1]/3, 0.499f+rgb[2]/3);
    }
    return c;
  }
  public static Color getCouleurSansValeur() {
    return Color.LIGHT_GRAY;
  }

  public static class DisqueCouleurIcone implements Icon {
    private Color couleur;
    int rayon;
    int marge;
    public DisqueCouleurIcone(Color couleur, int rayon, int marge) {
      this.couleur = couleur;
      this.rayon = rayon;
      this.marge = marge;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (couleur==null) {
        g.setColor(Color.BLACK);
        g.drawLine(x+3*marge, y+3*marge, x+2*rayon-marge, y+2*rayon-marge);
        g.drawLine(x+3*marge, y+2*rayon-marge, x+2*rayon-marge, y+3*marge);
      } else {
        g.setColor(couleur);
        g.fillOval(x+marge, y+marge, 2*rayon, 2*rayon);
        g.setColor(Color.BLACK);
        g.drawOval(x+marge, y+marge, 2*rayon, 2*rayon);
      }
    }
    public int getIconWidth() {
      return 2*rayon+2*marge;
    }
    public int getIconHeight() {
      return 2*rayon+2*marge;
    }
  }

  public static class LegendeLabel {
    String texte;
    Icon icone;
    public LegendeLabel(String texte, Color couleur) {
      this.texte = texte;
      this.icone = new DisqueCouleurIcone(couleur, 6, 2);
    }
  }

  public static class LegendeCellRenderer extends JLabel implements ListCellRenderer {
    public LegendeCellRenderer() {
      setIconTextGap(12);
    }
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
      LegendeLabel label = (LegendeLabel) value;
      setText(label.texte);
      setIcon(label.icone);
      return this;
    }
  }

  public static class RectangleCouleurIcone implements Icon {
    private Color couleur;
    private int largeur;
    private int hauteur;
    private int marge;
    private boolean select;
    public RectangleCouleurIcone(Color couleur, int largeur, int hauteur, int marge, boolean select) {
      this.couleur = couleur;
      this.largeur = largeur;
      this.hauteur = hauteur;
      this.marge = marge;
      this.select = select;
    }
    void setCouleur(Color c) {
      couleur = c;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(couleur);
      g.fillRect(x+marge, y+marge, largeur, hauteur);
      if (select) {
        g.setColor(Color.BLACK);
        g.drawRect(x+marge, y+marge, largeur, hauteur);
      }
    }
    public int getIconWidth() {
      return largeur+2*marge;
    }
    public int getIconHeight() {
      return hauteur+2*marge;
    }
  }

  public static class CouleurCellRenderer extends JLabel implements ListCellRenderer, TableCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
      if (index==list.getModel().getSize()-1||index<0) {
        setText(index<0 ? "" : styleDef("autre"));
        setIcon(null);
      } else {
        setText(null);
        setIcon(new RectangleCouleurIcone((Color) value, 40, 10, 2, isSelected));
      }
      return this;
    }
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      setText(null);
      setIcon(new RectangleCouleurIcone((Color) value, 80, 10, 2, true));
      return this;
    }
  }

  public static class CouleurComboBoxEditor implements ComboBoxEditor {
    CouleurComboBox comboBox;
    RectangleCouleurIcone icone;
    JLabel editeur;
    Color couleur;
    public CouleurComboBoxEditor(CouleurComboBox cb) {
      comboBox = cb;
      editeur = new JLabel(icone = new RectangleCouleurIcone(null, 80, 10, 2, true));
    }
    public Component getEditorComponent() {
      return editeur;
    }
    private void init(Color couleur) {
      this.couleur = couleur;
      icone.setCouleur(couleur);
    }
    public void setItem(Object anObject) {
      if (anObject==null) {
        Color c = JColorChooser.showDialog(editeur, "Choix de la couleur", couleur);
        if (c!=null) couleur = c;
      } else couleur = (Color) anObject;
      icone.setCouleur(couleur);
      comboBox.setSelectedItem(couleur);
    }
    public Object getItem() {
      return couleur;
    }
    public void selectAll() {
    }
    public void addActionListener(ActionListener l) {
    }
    public void removeActionListener(ActionListener l) {
    }
  }

  public static class CouleurComboBox extends JComboBox {
    public CouleurComboBox(Color[] couleurs) {
      super(couleurs);
      addItem(null);
      setRenderer(new CouleurCellRenderer());
      setEditor(new CouleurComboBoxEditor(this));
      setMaximumRowCount(couleurs.length+1);
    }
  }

  public static class CouleurTableEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private CouleurComboBox comboBox;
    private Color couleur;
    public CouleurTableEditor(Color[] couleurs) {
      comboBox = new CouleurComboBox(couleurs);
      comboBox.setEditable(true);
      comboBox.addActionListener(this);
    }
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      couleur = (Color) value;
      comboBox.getEditor().setItem(value);
      return comboBox;
    }
    public Object getCellEditorValue() {
      return couleur;
    }
    public void actionPerformed(ActionEvent e) {
      couleur = (Color) comboBox.getSelectedItem();
      fireEditingStopped();
    }
  }
}
