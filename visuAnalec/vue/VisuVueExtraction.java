package visuAnalec.vue;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.*;
import javax.swing.tree.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class VisuVueExtraction extends JPanel implements TreeWillExpandListener {
  private VisuVue visuVue;
  private Vue vue;
  private JTable tableauChampsAVoir;
  private JTree arbreChampsAExtraire;
  private JPopupMenuChamp popupMenuChamp;
  private Class<? extends Element> classeSelected;
  private String typeSelected;
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir;
  private HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir;
  private JPanel panneauChampsExtraits;
  private JPanel panneauAExtraire;
  VisuVueExtraction(VisuVue visuVue, Vue vue,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<String>>> champsAVoir,
          HashMap<Class<? extends Element>, HashMap<String, ArrayList<Adresse>>> adressesAVoir) {
    super(new BorderLayout());
    this.visuVue = visuVue;
    this.vue = vue;
    this.champsAVoir = champsAVoir;
    this.adressesAVoir = adressesAVoir;
    classeSelected = null;
    typeSelected = null;
//    // Panneau des champs extraits à visualiser
  panneauChampsExtraits =new JPanel(new BorderLayout());
    // Le tableau des champs à visualiser
    ModeleTableauChampsAVoir donneesChampsAVoir = new ModeleTableauChampsAVoir();
    tableauChampsAVoir = new JTable(donneesChampsAVoir);
    popupMenuChamp = new JPopupMenuChamp();
    popupMenuChamp.add(new ActionSupprimerChamp());

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
            popupMenuChamp.rang = tableauChampsAVoir.rowAtPoint(e.getPoint());
            ((JMenuItem) (popupMenuChamp.getSubElements()[0])).setText("supprimer le champ \"" +
                    tableauChampsAVoir.getValueAt(popupMenuChamp.rang, 0) + "\"");
            popupMenuChamp.show(e.getComponent(), e.getX(), e.getY());
          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    };
    tableauChampsAVoir.addMouseListener(popupListener);
    tableauChampsAVoir.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        try {
          if (e.getKeyCode() == KeyEvent.VK_DELETE && tableauChampsAVoir.getSelectedRowCount() == 1) {
            ((ModeleTableauChampsAVoir) tableauChampsAVoir.getModel()).supprimerChamp(tableauChampsAVoir.getSelectedRow());
          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    });
    JScrollPane scroll2 = new JScrollPane(tableauChampsAVoir);
    scroll2.setBorder(new TitledBorder("Propriétés à visualiser"));
    panneauChampsExtraits.add(scroll2, BorderLayout.CENTER);

    // Panneau des champs à extraire
    panneauAExtraire = new JPanel(new BorderLayout());
    ModeleArbreChampsAExtraire donneesChampsAExtraire = new ModeleArbreChampsAExtraire(vue);
    arbreChampsAExtraire = new JTree(donneesChampsAExtraire);
    arbreChampsAExtraire.addTreeWillExpandListener(this);
    MouseListener ml2 = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        try {
          TreePath selPath = arbreChampsAExtraire.getPathForLocation(e.getX(), e.getY());
          if (selPath != null && e.getClickCount() == 2) {
            NoeudChampAExtraire noeud = (NoeudChampAExtraire) selPath.getLastPathComponent();
            if (noeud.getAllowsChildren()) return;
            else ajouterChamp(noeud.getAdresse());
          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    };
    arbreChampsAExtraire.addMouseListener(ml2);
    JScrollPane scroll3 = new JScrollPane(arbreChampsAExtraire);
    scroll3.setBorder(new TitledBorder("Extraction de nouvelles propriétés à visualiser"));
    panneauAExtraire.add(scroll3, BorderLayout.CENTER);
      // Dimensionnement 
    add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panneauChampsExtraits, panneauAExtraire),
            BorderLayout.CENTER);
  }
//  void agencerPanneaux() {
//        JSplitPane split = (JSplitPane) panneauChampsExtraits.getParent();
//    split.setDividerLocation(2. / 3);
//    revalidate();
//    repaint();
//  }
  public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    NoeudChampAExtraire noeud = (NoeudChampAExtraire) event.getPath().getLastPathComponent();
    if(noeud.isLeaf()) {
      ModeleArbreChampsAExtraire donnees = (ModeleArbreChampsAExtraire) arbreChampsAExtraire.getModel();
     donnees.setSousArbre(noeud);
    }
  }
  public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    return;
  }
  void setTypeSelected(Class<? extends Element> classe, String type) {
    classeSelected = classe;
    typeSelected = type;
    ((ModeleTableauChampsAVoir) tableauChampsAVoir.getModel()).fireTableDataChanged();
    ((ModeleArbreChampsAExtraire) arbreChampsAExtraire.getModel()).setModele(classeSelected, typeSelected);
  }
  private void ajouterChamp(Adresse ad) {
     for (Adresse ad0 : adressesAVoir.get(classeSelected).get(typeSelected)) {
      if (ad.toString().equals(ad0.toString())) {
        JOptionPane.showMessageDialog(tableauChampsAVoir.getTopLevelAncestor(),
                "Ajout impossible : le champ d'adresse  " + ad.toString() + " existe déjà",
                "Erreur d'ajout de champ", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    visuVue.ajouterChamp(classeSelected, typeSelected, ad);
    ((ModeleTableauChampsAVoir) tableauChampsAVoir.getModel()).fireTableDataChanged();
  }

  private class ModeleTableauChampsAVoir extends AbstractTableModel {
    public int getColumnCount() {
      return 2;
    }
    public int getRowCount() {
      return (typeSelected == null || !champsAVoir.get(classeSelected).containsKey(typeSelected)) ? 0 : 
        champsAVoir.get(classeSelected).get(typeSelected).size();
    }
    public Object getValueAt(int row, int col) {
      return col == 0 ? champsAVoir.get(classeSelected).get(typeSelected).get(row) : 
        adressesAVoir.get(classeSelected).get(typeSelected).get(row).toString();
    }
    @Override
    public String getColumnName(int col) {
      return col == 0 ? "Noms abrégés des champs" : "Noms complets des propriétés";
    }
    @Override
    public boolean isCellEditable(int row, int col) {
      return col == 0;
    }
    @Override
    public void setValueAt(Object aValue, int row, int col) {
      if (col != 0)   throw new IndexOutOfBoundsException();
    visuVue.renommerChamp(classeSelected, typeSelected, row,  (String) aValue);
    }
    void supprimerChamp(int row) {
      visuVue.supprimerChamp(classeSelected, typeSelected,row);
      fireTableDataChanged();
    }
  }

  private static class JPopupMenuChamp extends JPopupMenu {
    int rang = -1;
  }

  private class ActionSupprimerChamp extends AbstractAction {
    ActionSupprimerChamp() {
      super("supprimer le champ");
    }
    public void actionPerformed(ActionEvent e) {
      try {
        int rg = ((JPopupMenuChamp) ((JMenuItem) e.getSource()).getParent()).rang;
        ((ModeleTableauChampsAVoir) tableauChampsAVoir.getModel()).supprimerChamp(rg);
      } catch (Throwable ex) {
        GMessages.erreurFatale(ex);
      }
    }
  }

  private static class ModeleArbreChampsAExtraire extends DefaultTreeModel {
    Vue vue;
    ModeleArbreChampsAExtraire(Vue vue) {
      super(null, true);
      this.vue = vue;
    }
    void setModele(Class<? extends Element> classeRacine, String typeRacine) {
      if (typeRacine == null) {
        setRoot(null);
      } else {
        NoeudChampAExtraire racine = new NoeudChampAExtraire(
                new Adresse(new Jalon(Jalon.Origine.ELT_0, classeRacine, typeRacine)));
        setRoot(racine);
        setSousArbre(racine);
      }
      reload();
    }
    void setSousArbre(NoeudChampAExtraire noeud) {
      Adresse adresse = noeud.getAdresse();
      for (String prop : vue.getNomsProps(adresse)) {
        noeud.add(new NoeudChampAExtraire(new Adresse(adresse, prop)));
      }
      Jalon[] jalons = vue.getNextJalonsPotentiels(adresse);
      for (Jalon jalon : jalons) {
        noeud.add(new NoeudChampAExtraire(new Adresse(adresse, jalon)));
      }
    }
  }

  private static class NoeudChampAExtraire extends DefaultMutableTreeNode {
    NoeudChampAExtraire(Adresse adresse) {
      super(adresse, adresse.prop == null);
    }
    Adresse getAdresse() {
      return (Adresse) getUserObject();
    }
    @Override
    public String toString() {
      if (getAdresse().prop == null) return getAdresse().getLastJalon().toString();
      return getAdresse().prop;
    }
  }
}