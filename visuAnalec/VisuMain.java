package visuAnalec;

/**
 *
 * @author bernard
 */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import javax.swing.table.TableModel;
import visuAnalec.Message.*;
import visuAnalec.chaines.*;
import visuAnalec.donnees.*;
import visuAnalec.fichiers.*;
import visuAnalec.statistiques.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.vue.*;
import visuAnalec.vue.Vue.*;

@SuppressWarnings("serial")
public class VisuMain extends JFrame implements VueListener {
  private Corpus corpus;
  private Vue vue;
  private PanneauEditeur panneauEditeur;
  private String nomCorpus = "";
  private String nomVue = "";
  private static String[] args;
  VisuMain() {
    super();
    corpus = new Corpus();
    vue = new Vue(corpus);
    panneauEditeur = new PanneauEditeur(vue);
    vue.addEventListener(this);
    // Cadre global de la fenêtre
    setContentPane(panneauEditeur);
    // Menus
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    // Menu corpus
    JMenu corpusMenu = new JMenu("Documents");
    corpusMenu.setMnemonic(KeyEvent.VK_D);
    menuBar.add(corpusMenu);
    corpusMenu.add(new GAction("Ouvrir...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersJava.ouvrirCorpusVue(corpus, vue, "");
        //       System.out.println(corpus.getStructure());
      }
    });
    JMenuItem item;
    corpusMenu.add(item = new JMenuItem(new GAction("Enregistrer") {
      public void executer() {
        if (corpus.isVide()) return;
        if (!nomCorpus.isEmpty()) {
          File fichier = Fichiers.getFichierCourant(Fichiers.TypeFichier.DOC_ANALEC);
          if (FichiersJava.enregistrerCorpus(corpus, fichier)) setTitre();
        } else if (FichiersJava.enregistrerCorpusSous(corpus)) {
          nomCorpus = Fichiers.getNomFichierCourant(Fichiers.TypeFichier.DOC_ANALEC);
          setTitre();
        }
      }
    }));
    corpusMenu.add(new GAction("Enregistrer sous...") {
      public void executer() {
        if (FichiersJava.enregistrerCorpusSous(corpus)) {
          nomCorpus = Fichiers.getNomFichierCourant(Fichiers.TypeFichier.DOC_ANALEC);
          setTitre();
        }
      }
    });
    corpusMenu.add(new GAction("Fermer") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
      }
    });
    corpusMenu.insertSeparator(corpusMenu.getMenuComponentCount());
    corpusMenu.add(new GAction("Fusionner des annotations") {
      public void executer() {
        if (!sauvegarde()) return;
        FichiersJava.fusionnerAnnot(corpus);
      }
    });
    corpusMenu.add(new GAction("Concaténer deux documents") {
      public void executer() {
        if (!sauvegarde()) return;
        FichiersJava.concatenerDocuments(corpus);
      }
    });
    corpusMenu.add(new GAction("Concaténer un corpus") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersJava.concatenerCorpus(corpus);
      }
    });
    corpusMenu.add(new GAction("Extraire un type d'un corpus") {
      public void executer() {
        if (!sauvegarde()) return;
        String type = JOptionPane.showInputDialog("Nom du type d'unité à extraire");
        if (type==null) return;
        corpus.fermer();
        FichiersJava.extraireTypeCorpus(type, corpus);
      }
    });
    corpusMenu.insertSeparator(corpusMenu.getMenuComponentCount());
    corpusMenu.add(new GAction("Importer du texte brut...") {
      public void executer() {
        if (!sauvegarde()) return;
        int rep = 0;
        if (!corpus.getStructure().isVide()) {
          rep = JOptionPane.showOptionDialog(null, "Utiliser la structure actuelle pour le nouveau document ?",
                  "Structure déjà présente", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                  null, new Object[]{"Oui", "Non", "Annuler"}, "Oui");
          if (rep==2) return;
        }
        if (rep==0) corpus.fermerConserverStructure();
        else corpus.fermer();
        FichiersTexte.importerTexte(corpus);
      }
    });
    corpusMenu.add(new GAction("Importer des données Excel...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersTableur.importerExcel(corpus);
      }
    });
    corpusMenu.add(new GAction("Importer des données Syntex...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersSyntex.importerSyntex(corpus);
      }
    });
    corpusMenu.add(new GAction("Importer des données Glozz...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersGlozz.importerGlozz(corpus);
      }
    });
    corpusMenu.add(new GAction("Exporter des données Glozz...") {
      public void executer() {
        FichiersGlozz.exporterGlozz(corpus);
      }
    });
    corpusMenu.add(new GAction("Convertir un corpus Glozz...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersGlozz.convertirCorpusGlozz(corpus);
      }
    });
   corpusMenu.add(new GAction("Exporter un corpus TEI...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersTEI.exporterCorpusTEI(corpus);
      }
    });
   corpusMenu.add(new GAction("Convertir un corpus TEI...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersTEI.convertirCorpusTEI(corpus);
     }
    });
   corpusMenu.add(new GAction("Importer un document CoNLL...") {
      public void executer() {
        if (!sauvegarde()) return;
        corpus.fermer();
        FichiersCoNLL.importerCoNLL(corpus);
     }
    });
    corpusMenu.insertSeparator(corpusMenu.getMenuComponentCount());
    corpusMenu.add(new GAction("Quitter") {
      public void executer() {
        fermerFenetre();
      }
    });

    // Menu structure
    JMenu structureMenu = new JMenu("Structure");
    structureMenu.setMnemonic(KeyEvent.VK_S);
    menuBar.add(structureMenu);
    structureMenu.add(new GAction("Gestion de la structure...") {
      public void executer() {
        VisuStructure visuStructure = VisuStructure.newVisuStructure(getContentPane(), corpus);
        visuStructure.setExtendedState(NORMAL);
        visuStructure.setVisible(true);
        visuStructure.agencer();
      }
    });
    structureMenu.add(new GAction("Ouvrir...") {
      public void executer() {
        if (!fusionStructure()) return;
        FichiersJava.ouvrirStructure(corpus);
      }
    });
    structureMenu.add(item = new JMenuItem(new GAction("Enregistrer...") {
      public void executer() {
        FichiersJava.enregistrerStructure(corpus);
        setTitre();
      }
    }));
    structureMenu.add(new GAction("Importer un modèle Glozz...") {
      public void executer() {
        if (!fusionStructure()) return;
        FichiersGlozz.importerModeleGlozz(corpus);
        //       System.out.println(corpus.getStructure());
      }
    });
    structureMenu.add(new GAction("Exporter un modèle Glozz...") {
      public void executer() {
        FichiersGlozz.exporterModeleGlozz(corpus.getStructure());
      }
    });

    // Menu vue
    JMenu vueMenu = new JMenu("Vue");
    vueMenu.setMnemonic(KeyEvent.VK_V);
    menuBar.add(vueMenu);
    vueMenu.add(new GAction("Gestion de la vue...") {
      public void executer() {
        VisuVue.newVisuVue(getContentPane(), vue);
      }
    });
    vueMenu.add(new GAction("Ouvrir (.ecv)...") {
      public void executer() {
        FichiersJava.ouvrirVue(vue);
      }
    });
    vueMenu.add(new GAction("Enregistrer (.ecv)...") {
      public void executer() {
        if (FichiersJava.enregistrerVue(vue)) {
          nomVue = Fichiers.getNomFichierCourant(Fichiers.TypeFichier.VUE);
          setTitre();
        }
      }
    });
    vueMenu.add(new GAction("Rétablir la vue par défaut") {
      public void executer() {
        nomVue = "";
        vue.retablirVueParDefaut();
      }
    });

    // Menu texte
    JMenu texteMenu = new JMenu("Texte");
    texteMenu.setMnemonic(KeyEvent.VK_T);
    menuBar.add(texteMenu);
    texteMenu.add(new GAction("Modifier le texte") {
      public void executer() {
        panneauEditeur.initModifierTexte();
      }
    });
    texteMenu.insertSeparator(texteMenu.getMenuComponentCount());
    texteMenu.add(item = new JMenuItem(new GAction("Rechercher...") {
      public void executer() {
        panneauEditeur.initRechercherTexte();
      }
    }));
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
    texteMenu.add(item = new JMenuItem(new GAction("Suivant") {
      public void executer() {
        panneauEditeur.rechercherSuivantTexte();
      }
    }));
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
    texteMenu.insertSeparator(texteMenu.getMenuComponentCount());
    texteMenu.add(new GAction("Style du document...") {
      public void executer() {
        panneauEditeur.saisirStyleTexte(vue);
      }
    });
    texteMenu.add(new GAction("Exporter format RTF...") {
      public void executer() {
        FichiersTexte.exporterTexteRTF(panneauEditeur);
      }
    });
    texteMenu.add(new GAction("Exporter format HTML...") {
      public void executer() {
        FichiersTexte.exporterTexteHTML(panneauEditeur);
      }
    });


    // Menu unités
    JMenu unitesMenu = new JMenu("Unités");
    unitesMenu.setMnemonic(KeyEvent.VK_U);
    menuBar.add(unitesMenu);
    unitesMenu.add(new GAction("Gestion des unités") {
      public void executer() {
        panneauEditeur.initGestionUnites();
      }
    });
    unitesMenu.add(new GAction("Export des unités") {
      public void executer() {
        String type;
        String[] types = vue.getTypesUnitesAVoir();
        if(types==null||types.length==0) return;
        if(types.length==1) type = types[0];
        else type = (String) JOptionPane.showInputDialog(VisuMain.this, 
       "Type des unités à exporter", "Choix du type d'unités",
       JOptionPane.QUESTION_MESSAGE, null, types, "");
        if(type==null) return;
        type = type.trim();
        if(type.isEmpty()) return;       
        TableModel donnees = vue.tableauUnites(type);
        if(donnees!=null)  FichiersTableur.exporterTable(donnees);
      }
    });
    unitesMenu.insertSeparator(unitesMenu.getMenuComponentCount());
    unitesMenu.add(new GAction("Création d'unités typographiques") {
      public void executer() {
        GMessages.pasImplemente();
      }
    });
    unitesMenu.add(new GAction("Création d'unités de constituance") {
      public void executer() {
        GMessages.pasImplemente();
      }
    });

    // Menu relations
    JMenu relationsMenu = new JMenu("Relations");
    relationsMenu.setMnemonic(KeyEvent.VK_R);
    menuBar.add(relationsMenu);
    relationsMenu.add(new GAction("Gestion des relations") {
      public void executer() {
        panneauEditeur.initGestionRelations();
      }
    });
    relationsMenu.add(new GAction("Visualisation d'arborescences") {
      public void executer() {
        GMessages.pasImplemente();
      }
    });
    relationsMenu.add(new GAction("Saisie d'arborescences") {
      public void executer() {
        GMessages.pasImplemente();
      }
    });
    relationsMenu.add(new GAction("Création d'arborescences") {
      public void executer() {
        GMessages.pasImplemente();
      }
    });

    // Menu schémas
    JMenu schemasMenu = new JMenu("Schémas");
    schemasMenu.setMnemonic(KeyEvent.VK_H);
    menuBar.add(schemasMenu);
    schemasMenu.add(new GAction("Gestion des schémas") {
      public void executer() {
        panneauEditeur.initGestionSchemas();
      }
    });
    schemasMenu.add(new GAction("Visualisation de chaînes") {
      public void executer() {
        new VisuChaines(getContentPane(), vue, panneauEditeur);
      }
    });

    // Menu règles
    JMenu reglesMenu = new JMenu("Règles");
    reglesMenu.setMnemonic(KeyEvent.VK_G);
    menuBar.add(reglesMenu);
    
    // Menu Stats
    JMenu statsMenu = new JMenu("Statistiques");
    statsMenu.setMnemonic(KeyEvent.VK_A);
    menuBar.add(statsMenu);
    statsMenu.add(new GAction("Fréquences...") {
      public void executer() {
        new VisuStatsFrequences(getContentPane(), vue, panneauEditeur);
      }
    });
    statsMenu.add(new GAction("Corrélations...") {
      public void executer() {
        new VisuStatsCorrelations(getContentPane(), vue, panneauEditeur);
      }
    });
    statsMenu.add(new GAction("Représentations géométriques...") {
      @SuppressWarnings("ResultOfObjectAllocationIgnored")
      public void executer() {
        new VisuStatsGeometrie(getContentPane(), vue, panneauEditeur);
      }
    });
    // Fermeture de la fenêtre
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        fermerFenetre();
      }
    });

  }
  public void premierPlan() {
    if ((getExtendedState()&Frame.ICONIFIED)>0) {
      setExtendedState(getExtendedState()&~Frame.ICONIFIED);
    }
    toFront();

  }
  private void fermerFenetre() {
    if (sauvegarde()) System.exit(0);
  }
  boolean sauvegarde() {
    if (!corpus.isModifie()) return true;
    int rep = JOptionPane.showOptionDialog(this, "Sauvegarder le document en cours ?",
            "Document modifié", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new Object[]{"Oui", "Non", "Annuler"}, "Non");
    if (rep==2) {
      return false;
    }
    if (rep==0) {
      FichiersJava.enregistrerCorpusSous(corpus);
    }
    return true;
  }
  boolean fusionStructure() {
    if (corpus.getStructure().isVide()) return true;
    int rep = JOptionPane.showOptionDialog(this, "La nouvelle structure va être fusionnée avec la structure courante",
            "Structure déjà présente", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new Object[]{"Poursuivre", "Annuler"}, "Poursuivre");
    return rep==0;
  }
  public void traiterEvent(Message evt) {
    switch (evt.getType()) {
      case CLEAR_CORPUS:
        nomCorpus = "";
        Fichiers.removeFichierCourant(Fichiers.TypeFichier.DOC_ANALEC);
        setTitre();
        return;
      case NEW_CORPUS:
        nomCorpus = Fichiers.getNomFichierCourant(Fichiers.TypeFichier.DOC_ANALEC);
        setTitre();
        return;
      case MODIF_VUE:
        switch (((VueEvent) evt).getModif()) {
          case VUE_DEFAUT:
            nomVue = "";
            setTitre();
            return;
          case NEW_VUE:
            nomVue = Fichiers.getNomFichierCourant(Fichiers.TypeFichier.VUE);
            setTitre();
            return;
          case EXTRACTION:
            setTitre();
            return;
          default:
            throw new UnsupportedOperationException("Cas "+((VueEvent) evt).getModif()+" oublié dans un switch");
        }
      case MODIF_TEXTE:
      case MODIF_STRUCTURE:
      case MODIF_ELEMENT:
        setTitre();
        return;
      default:
        throw new UnsupportedOperationException("Cas "+evt.getType()+" oublié dans un switch");
    }
  }
  private void setTitre() {
    String titre = "Analec 1.5";
    if (!nomCorpus.isEmpty()) {
      titre += " : "+nomCorpus;
      if (corpus.isModifie()) titre += "*";
      if (nomVue.isEmpty()) {
        if (vue.isModifiee()) titre += " - vue modifiée*";
        else titre += " - vue par défaut";
      } else {
        titre += " - vue : "+nomVue;
        if (vue.isModifiee()) titre += "*";
      }
    }
    setTitle(titre);
  }
  public static void main(String[] args) {
    VisuMain.args = args;
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          // Fenêtre
          lancerVisuAnalec();
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    });
  }
  public static void lancerVisuAnalec() throws IOException, ClassNotFoundException {
    // Fenêtre
    VisuMain fenetre = new VisuMain();
    fenetre.setTitre();
    Rectangle rmax = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    fenetre.setLocation(50, 50);
    fenetre.setSize(rmax.width-100, rmax.height-100);
    fenetre.setVisible(true);
    fenetre.panneauEditeur.agencerPanneaux();
    fenetre.traiterArguments(args);
//    System.out.println(fenetre.corpus.getStructure().toString());
//   new GMessages.GProgression(fenetre);
  }
  private void traiterArguments(String[] args) throws IOException, ClassNotFoundException {
    if (args.length==0) return;
    FichiersJava.ouvrirCorpusVue(corpus, vue, args[0]);
  }
}
