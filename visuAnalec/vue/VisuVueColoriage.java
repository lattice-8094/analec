/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.vue;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuVueColoriage extends JPanel implements TreeWillExpandListener {
  private VisuVue visuVue;
  private int indexTab;
  private Vue vue;
  private JTable tableauValeursAColorier;
  private JTree arbreValeursAChoisir;
  private JPopupMenuValeur popupMenuValeur;
  private Class<? extends Element> classeSelected;
  private String typeSelected;
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> stylesElements;
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> stylesValeursElements;
  private JPanel panneauValeursChoisies;
  private JPanel panneauValeursAChoisir;
  VisuVueColoriage(VisuVue visuVue, Vue vue,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir,
          HashMap<Class<? extends Element>, HashMap<String, SimpleAttributeSet>> stylesElements,
          HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashMap<String, SimpleAttributeSet>>>> stylesValeursElements) {
    super(new BorderLayout());
    this.visuVue = visuVue;
    this.vue = vue;
    this.champsAVoir = champsAVoir;
    this.adressesAVoir = adressesAVoir;
    this.stylesElements = stylesElements;
    this.stylesValeursElements = stylesValeursElements;
    classeSelected = null;
    typeSelected = null;
//    // Panneau des valeurs choisies à colorier
    panneauValeursChoisies = new JPanel();
    // Le tableau des valeurs à colorier
    ModeleTableauValeursAColorier donneesValeursAColorier = new ModeleTableauValeursAColorier();
    tableauValeursAColorier = new JTable(donneesValeursAColorier);
    GVisu.CouleurTableEditor couleurcombo =
            new GVisu.CouleurTableEditor(GVisu.getCouleursEtNoirEtBlanc());
    tableauValeursAColorier.setDefaultEditor(Color.class, couleurcombo);
    tableauValeursAColorier.setDefaultRenderer(Color.class, new GVisu.CouleurCellRenderer());
    popupMenuValeur = new JPopupMenuValeur();
    popupMenuValeur.add(new ActionSupprimerValeur());

    MouseListener popupListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }
      private void maybeShowPopup(MouseEvent e) {
        try {
          if (e.isPopupTrigger()) {
            int rg = popupMenuValeur.rang;
            popupMenuValeur.rang = tableauValeursAColorier.rowAtPoint(e.getPoint());
            ((JMenuItem) (popupMenuValeur.getSubElements()[0])).setText("supprimer ce coloriage");
            popupMenuValeur.show(e.getComponent(), e.getX(), e.getY());
          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    };
    tableauValeursAColorier.addMouseListener(popupListener);
    tableauValeursAColorier.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        try {
          if (e.getKeyCode()==KeyEvent.VK_DELETE&&tableauValeursAColorier.getSelectedRowCount()==1) {
            ((ModeleTableauValeursAColorier) tableauValeursAColorier.getModel()).supprimerRangee(
                    tableauValeursAColorier.getSelectedRow());
          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    });
    JScrollPane scroll2 = new JScrollPane(tableauValeursAColorier);
    scroll2.setBorder(new TitledBorder("Elements à colorier"));
    panneauValeursChoisies.add(scroll2, BorderLayout.CENTER);
    // Panneau des valeurs à choisir
    panneauValeursAChoisir = new JPanel(new BorderLayout());
    // L'arbre des champs à extraire
    ModeleArbreValeursAChoisir donneesValeursAChoisir =
            new ModeleArbreValeursAChoisir(vue, champsAVoir, adressesAVoir);
    arbreValeursAChoisir = new JTree(donneesValeursAChoisir);
    arbreValeursAChoisir.addTreeWillExpandListener(this);
    MouseListener ml2 = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        try {
          TreePath selPath = arbreValeursAChoisir.getPathForLocation(e.getX(), e.getY());
          if (selPath!=null&&e.getClickCount()==2) {
            NoeudValeurAChoisir noeud = (NoeudValeurAChoisir) selPath.getLastPathComponent();
            if (noeud.getAllowsChildren()) return;
            else ajouterValeur(noeud.champ, noeud.valeur);
          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    };
    arbreValeursAChoisir.addMouseListener(ml2);
    JScrollPane scroll3 = new JScrollPane(arbreValeursAChoisir);
    scroll3.setBorder(new TitledBorder("Choix de nouvelles valeurs à colorier"));
    panneauValeursAChoisir.add(scroll3, BorderLayout.CENTER);
    // Dimensionnement 
    add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauValeursChoisies, panneauValeursAChoisir),
            BorderLayout.CENTER);
  }
//    void agencerPanneaux() {
//        JSplitPane split = (JSplitPane) panneauValeursChoisies.getParent();
//    split.setDividerLocation(2. / 3);
//    revalidate();
//    repaint();
//    }
  public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    NoeudValeurAChoisir noeud = (NoeudValeurAChoisir) event.getPath().getLastPathComponent();
    if (noeud.isLeaf()) {
      ModeleArbreValeursAChoisir donnees = (ModeleArbreValeursAChoisir) arbreValeursAChoisir.getModel();
      donnees.setSousArbre(noeud);
    }
  }
  public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    return;
  }
  void setTypeSelected(Class<? extends Element> classe, String type) {
    classeSelected = classe;
    typeSelected = type;
    ((ModeleTableauValeursAColorier) tableauValeursAColorier.getModel()).reinitialiser();
    ((ModeleArbreValeursAChoisir) arbreValeursAChoisir.getModel()).setModele(classeSelected, typeSelected);
  }
  private void ajouterValeur(String champ, String valeur) {
    if (champ.isEmpty())
      ((ModeleTableauValeursAColorier) tableauValeursAColorier.getModel()).ajouterTousAColorier();
    else
      ((ModeleTableauValeursAColorier) tableauValeursAColorier.getModel()).ajouterValeurAColorier(champ, valeur);
  }
  void reinitialiser() {
    ((ModeleTableauValeursAColorier) tableauValeursAColorier.getModel()).reinitialiser();
    ((ModeleArbreValeursAChoisir) arbreValeursAChoisir.getModel()).setModele(classeSelected, typeSelected);
  }
  private static final String[] nomsColonnes = new String[]{
    "Champ",
    "Valeur",
    "<html><span style=\"font-weight:900 \">Gras</span>",
    "<html><span style=\"font-style:italic \">Italique</span>",
    "<html><span style=\"text-decoration:underline \">Souligné</span>",
    "<html><span style=\"color:red \">Couleur</span>",
    "<html><span style=\"background-color:yellow \">Fond</span>",};
  private static final Class[] classesColonnes = new Class[]{
    String.class, String.class, Boolean.class, Boolean.class, Boolean.class, Color.class, Color.class
  };

  private class ModeleTableauValeursAColorier extends AbstractTableModel {
    private ArrayList<String> champs = new ArrayList<String>();
    private ArrayList<String> valeurs = new ArrayList<String>();
    private ArrayList<Boolean> gras = new ArrayList<Boolean>();
    private ArrayList<Boolean> italiques = new ArrayList<Boolean>();
    private ArrayList<Boolean> soulignes = new ArrayList<Boolean>();
    private ArrayList<Color> couleurs = new ArrayList<Color>();
    private ArrayList<Color> fonds = new ArrayList<Color>();
    private void reinitialiser() {
      viderTableau();
      AttributeSet att;
      if (typeSelected!=null) {
        if (stylesElements.get(classeSelected).containsKey(typeSelected)) {
          att = stylesElements.get(classeSelected).get(typeSelected);
          ajouterRangee("", "", att);
        }
        if (stylesValeursElements.get(classeSelected).containsKey(typeSelected))
          for (String champ : stylesValeursElements.get(classeSelected).get(typeSelected).keySet())
            for (String valeur : stylesValeursElements.get(classeSelected).get(typeSelected).get(champ).keySet()) {
              att = stylesValeursElements.get(classeSelected).get(typeSelected).get(champ).get(valeur);
              ajouterRangee(champ, valeur, att);
            }
        fireTableDataChanged();
      }
    }
    private void viderTableau() {
      champs.clear();
      valeurs.clear();
      gras.clear();
      italiques.clear();
      soulignes.clear();
      couleurs.clear();
      fonds.clear();
    }
    private void ajouterTousAColorier() {
      if (!champs.isEmpty()&&champs.get(0).isEmpty()) return;
      stylesElements.get(classeSelected).put(typeSelected, new SimpleAttributeSet());
      insererRangee(0, "", "", vue.getStyleTexte());
      fireTableDataChanged();
    }
    private void ajouterValeurAColorier(String champ, String valeur) {
      for (int i = 0; i<champs.size(); i++)
        if (champs.get(i).equals(champ)&&valeurs.get(i).equals(valeur)) return;
      if (!stylesValeursElements.get(classeSelected).containsKey(typeSelected))
        stylesValeursElements.get(classeSelected).put(typeSelected, new HashMap<String, HashMap<String, SimpleAttributeSet>>());
      if (!stylesValeursElements.get(classeSelected).get(typeSelected).containsKey(champ))
        stylesValeursElements.get(classeSelected).get(typeSelected).put(champ, new HashMap<String, SimpleAttributeSet>());
      stylesValeursElements.get(classeSelected).get(typeSelected).get(champ).put(valeur, new SimpleAttributeSet());
      ajouterRangee(champ, valeur, vue.getStyleTexte());
      fireTableDataChanged();
    }
    private void ajouterRangee(String champ, String valeur, AttributeSet att) {
      insererRangee(champs.size(), champ, valeur, att);
    }
    private void insererRangee(int row, String champ, String valeur, AttributeSet att) {
      SimpleAttributeSet att0 = vue.getStyleTexte();
      champs.add(row, champ);
      valeurs.add(row, valeur);
      if (att.isDefined(StyleConstants.Bold))
        gras.add(row, StyleConstants.isBold(att));
      else gras.add(row, StyleConstants.isBold(att0));
      if (att.isDefined(StyleConstants.Italic))
        italiques.add(row, StyleConstants.isItalic(att));
      else italiques.add(row, StyleConstants.isItalic(att0));
      if (att.isDefined(StyleConstants.Underline))
        soulignes.add(row, StyleConstants.isUnderline(att));
      else soulignes.add(row, StyleConstants.isUnderline(att0));
      if (att.isDefined(StyleConstants.Foreground))
        couleurs.add(row, StyleConstants.getForeground(att));
      else couleurs.add(row, StyleConstants.getForeground(att0));
      if (att.isDefined(StyleConstants.Background))
        fonds.add(row, StyleConstants.getBackground(att));
      else fonds.add(row, StyleConstants.getBackground(att0));
      fireTableDataChanged();
    }
    void supprimerRangee(int row) {
      if (champs.get(row).isEmpty())
        stylesElements.get(classeSelected).remove(typeSelected);
      else
        stylesValeursElements.get(classeSelected).get(typeSelected).get(champs.get(row)).remove(valeurs.get(row));
      champs.remove(row);
      valeurs.remove(row);
      gras.remove(row);
      italiques.remove(row);
      soulignes.remove(row);
      couleurs.remove(row);
      fonds.remove(row);

      fireTableDataChanged();
    }
    public int getColumnCount() {
      return nomsColonnes.length;
    }
    public int getRowCount() {
      return (valeurs.size());
    }
    public Object getValueAt(int row, int col) {
      switch (col) {
        case 0:
          if (champs.get(row).isEmpty()) return GVisu.styleDef("tous");
          return champs.get(row);
        case 1:
          if (champs.get(row).isEmpty()) return GVisu.styleDef("toutes");
          return valeurs.get(row);
        case 2:
          return gras.get(row);
        case 3:
          return italiques.get(row);
        case 4:
          return soulignes.get(row);
        case 5:
          return couleurs.get(row);
        case 6:
          return fonds.get(row);
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
      return col>1;
    }
    @Override
    public void setValueAt(Object aValue, int row, int col) {
      Boolean valbool;
      Color valcoul;
      SimpleAttributeSet att = champs.get(row).isEmpty() ? stylesElements.get(classeSelected).get(typeSelected) : stylesValeursElements.get(classeSelected).get(typeSelected).get(champs.get(row)).get(valeurs.get(row));
      SimpleAttributeSet att0 = vue.getStyleTexte();
      switch (col) {
        case 2:
          valbool = (Boolean) aValue;
          gras.set(row, valbool);
          if (StyleConstants.isBold(att0)==valbool)
            att.removeAttribute(StyleConstants.Bold);
          else StyleConstants.setBold(att, valbool);
          break;
        case 3:
          valbool = (Boolean) aValue;
          italiques.set(row, valbool);
          if (StyleConstants.isItalic(att0)==valbool)
            att.removeAttribute(StyleConstants.Italic);
          else StyleConstants.setItalic(att, valbool);
          break;
        case 4:
          valbool = (Boolean) aValue;
          soulignes.set(row, valbool);
          if (StyleConstants.isUnderline(att0)==valbool)
            att.removeAttribute(StyleConstants.Underline);
          else StyleConstants.setUnderline(att, valbool);
          break;
        case 5:
          valcoul = (Color) aValue;
          couleurs.set(row, valcoul);
          if (StyleConstants.getForeground(att0)==valcoul)
            att.removeAttribute(StyleConstants.Foreground);
          else StyleConstants.setForeground(att, valcoul);
          break;
        case 6:
          valcoul = (Color) aValue;
          fonds.set(row, valcoul);
          if (StyleConstants.getBackground(att0)==valcoul)
            att.removeAttribute(StyleConstants.Background);
          else StyleConstants.setBackground(att, valcoul);
          break;
        default:
          throw new IndexOutOfBoundsException("col = "+col);
      }
      fireTableCellUpdated(row, col);
    }
  }

  private static class JPopupMenuValeur extends JPopupMenu {
    int rang = -1;
  }

  private class ActionSupprimerValeur extends AbstractAction {
    ActionSupprimerValeur() {
      super("supprimer le champ");
    }
    public void actionPerformed(ActionEvent e) {
      try {
        int rg = ((JPopupMenuValeur) ((JMenuItem) e.getSource()).getParent()).rang;
        ((ModeleTableauValeursAColorier) tableauValeursAColorier.getModel()).supprimerRangee(rg);
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
  }

  private static class ModeleArbreValeursAChoisir extends DefaultTreeModel {
    HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champs;
    HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adresses;
    Vue vue;
    ModeleArbreValeursAChoisir(Vue vue, HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAvoir,
            HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir) {
      super(null, true);
      this.champs = champsAvoir;
      this.adresses = adressesAVoir;
      this.vue = vue;
    }
    void setModele(Class<? extends Element> classeRacine, String typeRacine) {
      if (typeRacine==null) {
        setRoot(null);
      } else {
        NoeudValeurAChoisir racine = new NoeudValeurAChoisir(
                classeRacine, typeRacine, null, null);
        setRoot(racine);
        setSousArbre(racine);
      }
      reload();
    }
    void setSousArbre(NoeudValeurAChoisir noeud) {
      if (noeud.champ==null) {
        noeud.add(new NoeudValeurAChoisir(noeud.classe, noeud.type, "", GVisu.styleDef("tous les éléments")));
        for (String champ : champs.get(noeud.classe).get(noeud.type))
          noeud.add(new NoeudValeurAChoisir(noeud.classe, noeud.type, champ, null));
      } else {
        int nochamp = champs.get(noeud.classe).get(noeud.type).indexOf(noeud.champ);
        for (String valeur : vue.getValeursProp(adresses.get(noeud.classe).get(noeud.type).get(nochamp)))
          noeud.add(new NoeudValeurAChoisir(noeud.classe, noeud.type, noeud.champ, valeur));
      }
    }
  }

  private static class NoeudValeurAChoisir extends DefaultMutableTreeNode {
    Class<? extends Element> classe;
    String type;
    String champ;
    String valeur;
    NoeudValeurAChoisir(Class<? extends Element> classe, String type, String champ, String valeur) {
      super();
      this.classe = classe;
      this.type = type;
      this.champ = champ;
      this.valeur = valeur;
      setAllowsChildren(valeur==null);

    }
    @Override
    public String toString() {
      if (champ==null) return type;
      if (valeur==null) return champ;
      return valeur;
    }
  }
}
