/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.donnees;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import visuAnalec.*;
import visuAnalec.Message.*;
import visuAnalec.donnees.Corpus.*;
import visuAnalec.vue.Vue.*;
import visuAnalec.elements.*;
import visuAnalec.util.GMessages;
import visuAnalec.util.GMessages.*;

/**
 *
 * @author Bernard
 */
public class VisuStructure extends JFrame implements CorpusListener {
  static private VisuStructure instance = null;
  private Corpus corpus;
  private Structure structure;
  private JScrollPane panneauUnites, panneauRelations, panneauSchemas;
  private HashMap<Class<? extends Element>, ModeleArbreProp> donneesClasse =
          new HashMap<Class<? extends Element>, ModeleArbreProp>();
  static public VisuStructure newVisuStructure(Container fenetre, Corpus corpus) {
    if (instance==null) instance = new VisuStructure(fenetre, corpus);
    return instance;
  }
  private VisuStructure(Container fenetre, Corpus corps) {
    super("Structure des annotations");
    Point locFenetre = new Point(fenetre.getX()+20, fenetre.getY()+50);
    Dimension dimFenetre = new Dimension(fenetre.getWidth(), fenetre.getHeight()-30);
    setLocation(locFenetre);
    setSize(dimFenetre);
    this.corpus = corps;
    this.structure = corps.getStructure();
    corps.addEventListener(this);
    // Panneau des types d'unités
    ModeleArbreProp donneesPropUnites = new ModeleArbreProp(structure, Unite.class);
    JTree arbrePropUnites = new JTree(donneesPropUnites);
    donneesClasse.put(Unite.class, donneesPropUnites);
    arbrePropUnites.setRootVisible(true);
    arbrePropUnites.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    panneauUnites = new JScrollPane(arbrePropUnites);
    panneauUnites.setBorder(new TitledBorder("Unités"));
    // Panneau des types de relations
    ModeleArbreProp donneesPropRelations = new ModeleArbreProp(structure, Relation.class);
    JTree arbrePropRelations = new JTree(donneesPropRelations);
    donneesClasse.put(Relation.class, donneesPropRelations);
    arbrePropRelations.setRootVisible(true);
    arbrePropRelations.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    panneauRelations = new JScrollPane(arbrePropRelations);
    panneauRelations.setBorder(new TitledBorder("Relations"));
    // Panneau des types de schémas
    ModeleArbreProp donneesPropSchemas = new ModeleArbreProp(structure, Schema.class);
    JTree arbrePropSchemas = new JTree(donneesPropSchemas);
    donneesClasse.put(Schema.class, donneesPropSchemas);
    arbrePropSchemas.setRootVisible(true);
    arbrePropSchemas.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    panneauSchemas = new JScrollPane(arbrePropSchemas);
    panneauSchemas.setBorder(new TitledBorder("Schémas"));

    // cadre global de la fenêtre
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panneauUnites,
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panneauRelations, panneauSchemas));
    getContentPane().add(split);

    // Gestion des menus contextuels
    final JPopupMenuStructure popupMenuStructure = new JPopupMenuStructure();

    MouseListener popupListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        try {
          if (e.isPopupTrigger()) showPopup(e);
          else if (e.getClickCount()==2) {
            JTree arbre = (JTree) e.getComponent();
            TreePath item = arbre.getPathForLocation(e.getX(), e.getY());
            if (item==null) return;
            if (item.getPathCount()!=4) return;
            ModeleArbreProp donnees = (ModeleArbreProp) arbre.getModel();
            Class<? extends Element> classe = donnees.classe;
            DefaultMutableTreeNode noeudType = (DefaultMutableTreeNode) item.getPathComponent(1);
            DefaultMutableTreeNode noeudProp = (DefaultMutableTreeNode) item.getPathComponent(2);
            DefaultMutableTreeNode noeudVal = (DefaultMutableTreeNode) item.getPathComponent(3);
            String type = (String) noeudType.getUserObject();
            String prop = (String) noeudProp.getUserObject();
            String val = (String) noeudVal.getUserObject();
            if (isDefaut(val)) {
              corpus.modifierValParDefaut(classe, type, prop, "");
              //             noeudVal.setUserObject(oteDefaut(val));
            } else {
              corpus.modifierValParDefaut(classe, type, prop, val);
//              for (Enumeration noeuds = noeudProp.children(); noeuds.hasMoreElements();) {
//                DefaultMutableTreeNode noeud1 = (DefaultMutableTreeNode) noeuds.nextElement();
//                String val1 = (String) noeud1.getUserObject();
//                if (isDefaut(val1)) {
//                  noeud1.setUserObject(oteDefaut(val1));
//                  donnees.nodeChanged(noeudVal);
//                  break;
//                }
//              }
////              noeudVal.setUserObject(placeDefaut(val));
              //             corpus.newValeurDefaut(classe, type, prop, val);
            }
            //          donnees.nodeChanged(noeudVal);

          }
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        try {
          if (e.isPopupTrigger()) showPopup(e);
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
      private void showPopup(MouseEvent e) {
        JTree arbre = (JTree) e.getComponent();
        TreePath item = arbre.getPathForLocation(e.getX(), e.getY());
        if (item==null) return;
        popupMenuStructure.init(arbre, item);
        popupMenuStructure.show(e.getComponent(), e.getX(), e.getY());
      }
    };

    arbrePropUnites.addMouseListener(popupListener);
    arbrePropRelations.addMouseListener(popupListener);
    arbrePropSchemas.addMouseListener(popupListener);
    // Fermeture de la fenêtre
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        fermerFenetre();
      }
    });

  }
  public void agencer() {
    ((JSplitPane) panneauUnites.getParent()).setDividerLocation(1./3);
    ((JSplitPane) panneauRelations.getParent()).setDividerLocation(1./3);
  }
  private void fermerFenetre() {
    corpus.removeEventListener(this);
    dispose();
    instance = null;
  }
  public void traiterEvent(Message evt) {
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        fermerFenetre();
        return;
      case NEW_CORPUS:  // impossible en principe : CLEAR_CORPUS a détruit la fenêtre
        throw new UnsupportedOperationException("Fenêtre stat présente lors d'un message NEW_CORPUS ???");
      case MODIF_STRUCTURE:
        StructureEvent evtS = (StructureEvent) evt;
        ModeleArbreProp donnees = donneesClasse.get(evtS.getClasse());
        switch (evtS.getModif()) { // pour les modifs ayant lieu en dehors de VisuStructure 
          case AJOUT_TYPE:
            donnees.insererNewType(evtS.getArg(0));
            return;
          case AJOUT_PROP:
            donnees.insererNewProp(evtS.getArg(0), evtS.getArg(1));
            return;
          case AJOUT_VALEUR:
            donnees.insererNewValeur(evtS.getArg(0), evtS.getArg(1), evtS.getArg(2));
            return;
          case VALEUR_PAR_DEFAUT:
            donnees.modifierValDefaut(evtS.getArg(0), evtS.getArg(1), evtS.getArg(2));
            return;
          default:
            return;
        }
      case MODIF_ELEMENT:
      case MODIF_TEXTE:
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }
  }
  static boolean isDefaut(String val) {
    return val.startsWith("=>");
  }
  static String placeDefaut(String val) {
    return isDefaut(val) ? val : "=>"+val;
  }
  static String oteDefaut(String val) {
    return isDefaut(val) ? val.substring(2) : val;
  }

  private class JPopupMenuStructure extends JPopupMenu {
    void init(JTree arbre, TreePath item) {
      ModeleArbreProp donnees = (ModeleArbreProp) arbre.getModel();
      removeAll();
      int niveau = item.getPathCount();
      if (niveau<4) {
        add(new ActionAjouter(donnees.classe, arbre, donnees, item));
      }
      if (niveau>1) {
        if (niveau!=3||!donnees.classe.equals(Unite.class)||!Structure.FORME.equals(
                ((DefaultMutableTreeNode) item.getLastPathComponent()).getUserObject()))
          add(new ActionRenommer(donnees.classe, donnees, item));
        add(new ActionSupprimer(donnees.classe, donnees, item));
      }
      if (niveau==2&&donnees.classe.equals(Unite.class)
              &&!donnees.hasForme((DefaultMutableTreeNode) item.getLastPathComponent()))
        add(new ActionAjouterForme(arbre, donnees, item));
      if (niveau==3)
        add(new ActionDispatcher(donnees.classe, arbre, donnees, item));
    }
  }
  static String[] commandesAjout = new String[]{
    "Ajouter un type",
    "Ajouter une propriété",
    "Ajouter une valeur"
  };

  private class ActionAjouter extends GAction {
    TreePath item;
    Class<? extends Element> classe;
    JTree arbre;
    ModeleArbreProp donnees;
    ActionAjouter(Class<? extends Element> classe, JTree arbre, ModeleArbreProp donnees, TreePath item) {
      super(commandesAjout[item.getPathCount()-1]);
      this.item = item;
      this.classe = classe;
      this.donnees = donnees;
      this.arbre = arbre;
    }
    public void executer() {
      corpus.removeEventListener(VisuStructure.this);
      DefaultMutableTreeNode noeud, noeud1, noeud2;
      noeud = (DefaultMutableTreeNode) item.getLastPathComponent();
      switch (item.getPathCount()) {
        case 1: // racine : ajout d'un type
          String newType =
                  corpus.saisirAjouterType(VisuStructure.this, classe);
          if (newType!=null) {
            noeud1 = new DefaultMutableTreeNode(newType, true);
            donnees.insertNodeInto(noeud1, noeud, 0);
            arbre.makeVisible(item.pathByAddingChild(noeud1));
          }
          break;
        case 2: // type : ajout d'une prop
          String newProp =
                  corpus.saisirAjouterProp(VisuStructure.this, classe,
                  (String) noeud.getUserObject());
          if (newProp!=null) {
            noeud1 = new DefaultMutableTreeNode(newProp, true);
            donnees.insertNodeInto(noeud1, noeud, 0);
            arbre.makeVisible(item.pathByAddingChild(noeud1));
          }
          break;
        case 3: // prop : ajout d'une valeur
          noeud1 = (DefaultMutableTreeNode) item.getPathComponent(1);
          String newVal = corpus.saisirAjouterVal(VisuStructure.this, classe,
                  (String) noeud1.getUserObject(), (String) noeud.getUserObject());
          if (newVal!=null) {
            noeud2 = new DefaultMutableTreeNode(newVal, false);
            donnees.insertNodeInto(noeud2, noeud, 0);
            arbre.makeVisible(item.pathByAddingChild(noeud1));
          }
          break;
        default:
          throw new UnsupportedOperationException("Cas d'ajout non prévu ");
      }
      corpus.addEventListener(VisuStructure.this);
    }
  }
  static String[] commandesRenom = new String[]{
    "Renommer le type",
    "Renommer la propriété",
    "Renommer la valeur"
  };

  private class ActionRenommer extends GAction {
    TreePath item;
    Class<? extends Element> classe;
    ModeleArbreProp donnees;
    ActionRenommer(Class<? extends Element> classe, ModeleArbreProp donnees, TreePath item) {
      super(commandesRenom[item.getPathCount()-2]);
      this.item = item;
      this.classe = classe;
      this.donnees = donnees;
    }
    @SuppressWarnings("unchecked")
    public void executer() {
      corpus.removeEventListener(VisuStructure.this);
      DefaultMutableTreeNode noeud, noeud0, noeud1, noeud2;
      noeud = (DefaultMutableTreeNode) item.getLastPathComponent();
      switch (item.getPathCount()) {
        case 2: // type
          String newType = corpus.saisirRenommerType(VisuStructure.this, classe,
                  (String) noeud.getUserObject());
          if (newType!=null) {
            DefaultMutableTreeNode ntypefusion = null;
            noeud0 = (DefaultMutableTreeNode) item.getPathComponent(0);
            for (Enumeration<DefaultMutableTreeNode> etype = noeud0.children(); etype.hasMoreElements();) {
              DefaultMutableTreeNode ntype = etype.nextElement();
              String type0 = (String) ntype.getUserObject();
              if (newType.equals(type0)) {
                ntypefusion = ntype;
                break;
              }
            }
            if (ntypefusion!=null)
              donnees.fusionnerType(noeud, ntypefusion);
            else {
              noeud.setUserObject(newType);
              donnees.nodeChanged(noeud);
            }
          }
          break;
        case 3: // prop
          noeud1 = (DefaultMutableTreeNode) item.getPathComponent(1);
          String newProp = corpus.saisirRenommerProp(VisuStructure.this, classe,
                  (String) noeud1.getUserObject(), (String) noeud.getUserObject());
          if (newProp!=null) {
            DefaultMutableTreeNode npropfusion = null;
            for (Enumeration<DefaultMutableTreeNode> eprop = noeud1.children(); eprop.hasMoreElements();) {
              DefaultMutableTreeNode nprop = eprop.nextElement();
              String prop0 = (String) nprop.getUserObject();
              if (newProp.equals(prop0)) {
                npropfusion = nprop;
                break;
              }
            }
            if (npropfusion!=null)
              donnees.fusionnerProp(noeud1, noeud, npropfusion);
            else {
              noeud.setUserObject(newProp);
              donnees.nodeChanged(noeud);
            }
          }
          break;
        case 4: // val
          noeud1 = (DefaultMutableTreeNode) item.getPathComponent(1);
          noeud2 = (DefaultMutableTreeNode) item.getPathComponent(2);
          String val = (String) noeud.getUserObject();
          boolean isDefaut = isDefaut(val);
          if (isDefaut) val = oteDefaut(val);
          String newVal = corpus.saisirRenommerVal(VisuStructure.this, classe,
                  (String) noeud1.getUserObject(), (String) noeud2.getUserObject(), val);
          if (newVal!=null) {
            for (Enumeration<DefaultMutableTreeNode> eval = noeud2.children(); eval.hasMoreElements();) {
              DefaultMutableTreeNode nval = eval.nextElement();
              String val0 = (String) nval.getUserObject();
              if (newVal.equals(isDefaut(val0) ? oteDefaut(val0) : val0)) {
                isDefaut = isDefaut||isDefaut(val0);
                donnees.removeNodeFromParent(nval);
                break;
              }
            }
            if (isDefaut) newVal = placeDefaut(newVal);
            noeud.setUserObject(newVal);
            donnees.nodeChanged(noeud);
          }
          break;
        default:
          throw new UnsupportedOperationException("Cas de renommage non prévu ");
      }
      corpus.addEventListener(VisuStructure.this);
    }
  }
  static String[] commandesSuppr = new String[]{
    "Supprimer le type",
    "Supprimer la propriété",
    "Supprimer la valeur"
  };

  private class ActionSupprimer extends GAction {
    TreePath item;
    Class<? extends Element> classe;
    ModeleArbreProp donnees;
    ActionSupprimer(Class<? extends Element> classe, ModeleArbreProp donnees, TreePath item) {
      super(commandesSuppr[item.getPathCount()-2]);
      this.item = item;
      this.classe = classe;
      this.donnees = donnees;
    }
    @SuppressWarnings("unchecked")
    public void executer() {
      corpus.removeEventListener(VisuStructure.this);
      DefaultMutableTreeNode noeud, noeud1, noeud2;
      noeud = (DefaultMutableTreeNode) item.getLastPathComponent();
      switch (item.getPathCount()) {
        case 1: // racine
          return;
        case 2: // type
          if (corpus.saisirSupprimerType(VisuStructure.this, classe,
                  (String) noeud.getUserObject())) {
            donnees.removeNodeFromParent(noeud);
          }
          break;
        case 3: // prop
          noeud1 = (DefaultMutableTreeNode) item.getPathComponent(1);
          if (corpus.saisirSupprimerProp(VisuStructure.this, classe,
                  (String) noeud1.getUserObject(), (String) noeud.getUserObject())) {
            donnees.removeNodeFromParent(noeud);
          }
          ;
          break;
        case 4: // val
          noeud1 = (DefaultMutableTreeNode) item.getPathComponent(1);
          noeud2 = (DefaultMutableTreeNode) item.getPathComponent(2);
          String val = (String) noeud.getUserObject();
          if (isDefaut(val)) val = oteDefaut(val);
          if (corpus.saisirSupprimerVal(VisuStructure.this, classe,
                  (String) noeud1.getUserObject(), (String) noeud2.getUserObject(), val)) {
            donnees.removeNodeFromParent(noeud);
          }
          break;
        default:
          throw new UnsupportedOperationException("Cas de suppression non prévu ");
      }
      corpus.addEventListener(VisuStructure.this);
    }
  }

  private class ActionAjouterForme extends GAction {
    TreePath item;
    Class<? extends Element> classe;
    JTree arbre;
    ModeleArbreProp donnees;
    ActionAjouterForme(JTree arbre, ModeleArbreProp donnees, TreePath item) {
      super("Ajouter la forme de surface");
      this.item = item;
      this.donnees = donnees;
      this.arbre = arbre;
    }
    @SuppressWarnings("unchecked")
    public void executer() {
      corpus.removeEventListener(VisuStructure.this);
      DefaultMutableTreeNode noeud, noeud1;
      noeud = (DefaultMutableTreeNode) item.getLastPathComponent();
      String type = (String) noeud.getUserObject();
      corpus.ajouterFormeUnite(type);
      noeud1 = new DefaultMutableTreeNode(Structure.FORME, true);
      donnees.insertNodeInto(noeud1, noeud, 0);
      arbre.makeVisible(item.pathByAddingChild(noeud1));
      corpus.addEventListener(VisuStructure.this);
      corpus.instancierFormeUnite(type);

    }
  }

  private class ActionDispatcher extends GAction {
    TreePath item;
    Class<? extends Element> classe;
    JTree arbre;
    ModeleArbreProp donnees;
    ActionDispatcher(Class<? extends Element> classe, JTree arbre,
            ModeleArbreProp donnees, TreePath item) {
      super("Transformer en types distincts");
      this.item = item;
      this.classe = classe;
      this.donnees = donnees;
      this.arbre = arbre;
    }
    @SuppressWarnings("unchecked")
    public void executer() {
      corpus.removeEventListener(VisuStructure.this);
      DefaultMutableTreeNode noeudprop = (DefaultMutableTreeNode) item.getLastPathComponent();
      int nbval = noeudprop.getChildCount();
      String[] vals = new String[nbval];
      Enumeration<DefaultMutableTreeNode> eval = noeudprop.children();
      for (int i = 0; i<nbval; i++)
        vals[i] = (String) eval.nextElement().getUserObject();
      DefaultMutableTreeNode noeudtype = (DefaultMutableTreeNode) item.getPathComponent(1);
      int nbprop = noeudtype.getChildCount();
      String[] props = new String[nbprop-1];
      String[][] propsvals = new String[nbprop-1][];
      Enumeration<DefaultMutableTreeNode> eprop = noeudtype.children();
      for (int i = 0; eprop.hasMoreElements(); i++) {
        DefaultMutableTreeNode nprop = eprop.nextElement();
        if (nprop!=noeudprop) {
          props[i] = (String) nprop.getUserObject();
          propsvals[i] = new String[nprop.getChildCount()];
          Enumeration<DefaultMutableTreeNode> epropval = nprop.children();
          for (int j = 0; epropval.hasMoreElements(); j++)
            propsvals[i][j] = (String) epropval.nextElement().getUserObject();
        } else i--;
      }
      if (corpus.saisirDispatcherProp(VisuStructure.this, classe,
              (String) noeudtype.getUserObject(),
              (String) noeudprop.getUserObject(), vals, props, propsvals))
        donnees.dispatcherProp(noeudtype, noeudprop, vals, props, propsvals);
      corpus.addEventListener(VisuStructure.this);
    }
  }

  private static class ModeleArbreProp extends DefaultTreeModel {
    Structure structure;
    Class<? extends Element> classe;
    ModeleArbreProp(Structure structure, Class<? extends Element> classe) {
      super(null, true);
      this.structure = structure;
      this.classe = classe;
      setModele();
    }
    private void setModele() {
      DefaultMutableTreeNode racine = new DefaultMutableTreeNode("TYPES :", true);
      setRoot(racine);
      String[] types = structure.getTypes(classe).toArray(new String[0]);
      Arrays.sort(types);
      for (String type : types) {
        DefaultMutableTreeNode ntype =
                new DefaultMutableTreeNode(type, true);
        racine.add(ntype);
        String[] props =
                structure.getNomsProps(classe, type).toArray(new String[0]);
        Arrays.sort(props);
        for (String prop : props) {
          DefaultMutableTreeNode nprop =
                  new DefaultMutableTreeNode(prop, true);
          ntype.add(nprop);
          String[] vals =
                  structure.getValeursProp(classe, type, prop).toArray(new String[0]);
          Arrays.sort(vals);
          for (String val : vals) {
            if (val.equals(structure.getValeurParDefaut(classe, type, prop)))
              val = placeDefaut(val);
            DefaultMutableTreeNode nval =
                    new DefaultMutableTreeNode(val, false);
            nprop.add(nval);
          }
        }
      }
      reload();
    }
    @SuppressWarnings("unchecked")
    void insererNewType(String type) {
      DefaultMutableTreeNode racine = (DefaultMutableTreeNode) getRoot();
      insertNodeInto(new DefaultMutableTreeNode(type, true), racine, 0);
    }
    @SuppressWarnings("unchecked")
    void insererNewProp(String type, String prop) {
      DefaultMutableTreeNode racine = (DefaultMutableTreeNode) getRoot();
      DefaultMutableTreeNode ntype = null;
      for (Enumeration<DefaultMutableTreeNode> etype = racine.children();
              etype.hasMoreElements();) {
        ntype = etype.nextElement();
        if (type.equals(ntype.getUserObject())) break;
      }
      insertNodeInto(new DefaultMutableTreeNode(prop, true), ntype, 0);
    }
    @SuppressWarnings("unchecked")
    void insererNewValeur(String type, String prop, String valeur) {
      DefaultMutableTreeNode racine = (DefaultMutableTreeNode) getRoot();
      DefaultMutableTreeNode ntype = null;
      DefaultMutableTreeNode nprop = null;
      for (Enumeration<DefaultMutableTreeNode> etype = racine.children();
              etype.hasMoreElements();) {
        ntype = etype.nextElement();
        if (type.equals(ntype.getUserObject())) {
          for (Enumeration<DefaultMutableTreeNode> eprop = ntype.children(); eprop.hasMoreElements();) {
            nprop = eprop.nextElement();
            if (prop.equals(nprop.getUserObject())) break;
          }
          break;
        }
      }
      insertNodeInto(new DefaultMutableTreeNode(valeur, false), nprop, 0);
    }
    @SuppressWarnings("unchecked")
    void modifierValDefaut(String type, String prop, String valeur) {
      DefaultMutableTreeNode racine = (DefaultMutableTreeNode) getRoot();
      DefaultMutableTreeNode ntype = null;
      DefaultMutableTreeNode nprop = null;
      DefaultMutableTreeNode nval = null;
      for (Enumeration<DefaultMutableTreeNode> etype = racine.children(); etype.hasMoreElements();) {
        ntype = etype.nextElement();
        if (type.equals(ntype.getUserObject())) {
          for (Enumeration<DefaultMutableTreeNode> eprop = ntype.children(); eprop.hasMoreElements();) {
            nprop = eprop.nextElement();
            if (prop.equals(nprop.getUserObject())) break;
          }
          break;
        }
      }
      for (Enumeration<DefaultMutableTreeNode> eval = nprop.children(); eval.hasMoreElements();) {
        nval = eval.nextElement();
        String val = (String) nval.getUserObject();
        if (isDefaut(val)) {
          nval.setUserObject(oteDefaut(val));
          nodeChanged(nval);
        }
        if (val.equals(valeur)) {
          nval.setUserObject(placeDefaut(val));
          nodeChanged(nval);
        }

      }
    }
    void fusionnerType(DefaultMutableTreeNode noeudafusionner,
            DefaultMutableTreeNode noeudagarder) {
      removeNodeFromParent(noeudafusionner);
      noeudagarder.removeAllChildren();
      String type = (String) noeudagarder.getUserObject();
      String[] props = structure.getNomsProps(classe, type).toArray(new String[0]);
      Arrays.sort(props);
      for (String prop : props) {
        DefaultMutableTreeNode nprop = new DefaultMutableTreeNode(prop, true);
        noeudagarder.add(nprop);
        String[] vals = structure.getValeursProp(classe, type, prop).toArray(new String[0]);
        Arrays.sort(vals);
        for (String val : vals) {
          if (val.equals(structure.getValeurParDefaut(classe, type, prop)))
            val = placeDefaut(val);
          DefaultMutableTreeNode nval = new DefaultMutableTreeNode(val, false);
          nprop.add(nval);
        }
      }
      nodeStructureChanged(noeudagarder);
    }
    void fusionnerProp(DefaultMutableTreeNode noeudtype, DefaultMutableTreeNode noeudafusionner,
            DefaultMutableTreeNode noeudagarder) {
      removeNodeFromParent(noeudafusionner);
      noeudagarder.removeAllChildren();
      String type = (String) noeudtype.getUserObject();
      String prop = (String) noeudagarder.getUserObject();
      String[] vals = structure.getValeursProp(classe, type, prop).toArray(new String[0]);
      Arrays.sort(vals);
      for (String val : vals) {
        if (val.equals(structure.getValeurParDefaut(classe, type, prop)))
          val = placeDefaut(val);
        DefaultMutableTreeNode nval = new DefaultMutableTreeNode(val, false);
        noeudagarder.add(nval);
      }
      nodeStructureChanged(noeudagarder);
    }
    void dispatcherProp(DefaultMutableTreeNode noeudtype,
            DefaultMutableTreeNode noeudprop, String[] newtypes, String[] props,
            String[][] propsvals) {
      removeNodeFromParent(noeudtype);
      removeNodeFromParent(noeudprop);
      DefaultMutableTreeNode racine = (DefaultMutableTreeNode) getRoot();
      DefaultMutableTreeNode ntype = null;
      DefaultMutableTreeNode nprop = null;
      for (String type : newtypes) {
        insertNodeInto(ntype = new DefaultMutableTreeNode(type, true), racine, 0);
        for (int i = 0; i<props.length; i++) {
          insertNodeInto(nprop = new DefaultMutableTreeNode(props[i], true),
                  ntype, i);
          for (int j = 0; j<propsvals[i].length; j++)
            insertNodeInto(new DefaultMutableTreeNode(oteDefaut(propsvals[i][j]),
                    false), nprop, j);

        }

      }
    }
    @SuppressWarnings("unchecked")
    boolean hasForme(DefaultMutableTreeNode noeudtype) {
      for (Enumeration<DefaultMutableTreeNode> eprop = noeudtype.children();
              eprop.hasMoreElements();) {
        DefaultMutableTreeNode nprop = eprop.nextElement();
        if (Structure.FORME.equals(nprop.getUserObject())) return true;
      }

      return false;
    }
  }
}
