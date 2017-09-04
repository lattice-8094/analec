/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

import java.io.*;
import javax.swing.*;
import visuAnalec.donnees.*;
import visuAnalec.util.*;
import visuAnalec.util.Fichiers.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public abstract class FichiersJava {
  public static void ouvrirCorpusVue(Corpus corpus, Vue vue, String nomfichier) {
    File fichierCorpus;
    if (nomfichier.isEmpty()) {
      if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR,
              "Choix du document", Fichiers.TypeFichier.DOC_ANALEC))==null) return;
    } else {
      if ((fichierCorpus = Fichiers.trouverFichier(Fichiers.TypeFichier.DOC_ANALEC, nomfichier))==null) {
        JOptionPane.showMessageDialog(null, "Fichier "+nomfichier+" inexistant dans le répertoire "
                +Fichiers.getRepertoireCourant(Fichiers.TypeFichier.DOC_ANALEC).getPath(), "Erreur de lecture de fichier",
                JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    if(!ouvrirCorpus(corpus, fichierCorpus, false)) return;
    File fichierVue = Fichiers.trouverFichier(Fichiers.TypeFichier.VUE, fichierCorpus);
    if (fichierVue==null) return;
    ouvrirVue(vue, fichierVue);
  }

public static boolean ouvrirCorpus(Corpus corpus, File fichierCorpus, boolean batch) {
     ObjectInputStream ois = null;
     try {
      ois = new ObjectInputStream(new FileInputStream(fichierCorpus));
      corpus.lireJava(ois, batch);
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le document "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (InvalidClassException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le document "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (ClassCastException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le document "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return false;
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return false;
    } finally {
      try {
        if (ois!=null) ois.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      return true;
    }
}
  public static void ouvrirStructure(Corpus corpus) {
    File fichierStructure;
    if ((fichierStructure = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix de la structure", Fichiers.TypeFichier.STRUCTURE))==null) return;
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(fichierStructure));
      Structure structureLue = corpus.lireStructureJava(ois);
      corpus.fusionnerStructureLue(structureLue);
      corpus.newCorpusLu();
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire la structure "+fichierStructure.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ecs", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (InvalidClassException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire la structure "+fichierStructure.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ecs", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ClassCastException ex) {
      JOptionPane.showMessageDialog(null, "Impossible de lire la structure "+fichierStructure.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ecs", JOptionPane.ERROR_MESSAGE);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (ois!=null) ois.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public static void ouvrirVue(Vue vue, File fichierVue) {
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(fichierVue));
      if (!vue.lireJava(ois)) {
        JOptionPane.showMessageDialog(null, "Impossible d'utiliser la vue "+fichierVue.getName()
                +" : vue incompatible avec le document courant", "Erreur de lecture de fichier .ecv", JOptionPane.ERROR_MESSAGE);
        if (ois!=null) ois.close();
        return;
      }
    } catch (InvalidClassException ex) {
      JOptionPane.showMessageDialog(null, "Impossible de lire la vue "+fichierVue.getName()
              +" : versions d'Analec incompatibles ", "Erreur d'ouverture de fichier .ecv", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ClassNotFoundException ex) {
      JOptionPane.showMessageDialog(null, "Impossible de lire la vue "+fichierVue.getName()
              +" : versions d'Analec incompatibles ", "Erreur d'ouverture de fichier .ecv", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ClassCastException ex) {
      JOptionPane.showMessageDialog(null, "Impossible de lire la vue "+fichierVue.getName()
              +" : versions d'Analec incompatibles ", "Erreur d'ouverture de fichier .ecv", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (ois!=null) ois.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public static void ouvrirVue(Vue vue) {
    File fichierVue;
    if ((fichierVue = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix de la vue", Fichiers.TypeFichier.VUE))==null) return;
    ouvrirVue(vue, fichierVue);
  }

  public static void fusionnerAnnot(Corpus corpus) {
    File fichierCorpus;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier à fusionner", Fichiers.TypeFichier.DOC_ANALEC))==null)
      return;
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(fichierCorpus));
      corpus.fusionnerAnnotJava(ois);
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ClassCastException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (InvalidClassException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (ois!=null) ois.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }

  }

  public static void concatenerDocuments(Corpus corpus) {
    File fichierCorpus;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier à concaténer", Fichiers.TypeFichier.DOC_ANALEC))==null)
      return;
    concatener(fichierCorpus, corpus);
    corpus.newCorpusLu();
  }

  public static void concatenerCorpus(final Corpus corpus) {
    File rep;
    if ((rep = Fichiers.choisirRepertoire("Choix du répertoire à concaténer"))
            ==null) return;
    final File[] fics = Fichiers.listerFichiers(rep, Fichiers.TypeFichier.DOC_ANALEC);
    final GMessages.GProgression progression =
            new GMessages.GProgression(fics.length, "Concaténation", "fichiers");
    Thread traitement = new Thread() {
      @Override
      public void run() {
        for (int i = 0; i<fics.length; i++) {
          if (progression.isCanceled()) break;
          progression.setNbTraites(i+1);
          concatener(fics[i], corpus);
        }
        corpus.newCorpusLu();
      }
    };
    traitement.start();
  }

  public static void extraireTypeCorpus(final String type, final Corpus corpus) {
    File rep;
    if ((rep = Fichiers.choisirRepertoire("Choix du répertoire à traiter"))
            ==null) return;
   final  File[] fics = Fichiers.listerFichiers(rep, Fichiers.TypeFichier.DOC_ANALEC);
    final GMessages.GProgression progression =
            new GMessages.GProgression(fics.length, "Extraction", "fichiers");
  Thread traitement = new Thread() {
      @Override
      public void run() {
        for (int i = 0; i<fics.length; i++) {
          if (progression.isCanceled()) break;
          progression.setNbTraites(i+1);
      extraireType(fics[i], Fichiers.extraireNom(fics[i]), type, corpus);        
        }
        corpus.newCorpusLu();
      }
    };
    traitement.start();
  }

  private static void extraireType(File fichierCorpus, String nomDocument,
          String type, Corpus corpus) {
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(fichierCorpus));
      corpus.extraireEtConcatenerCorpusJava(ois, nomDocument, type);
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ClassCastException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (InvalidClassException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (ois!=null) ois.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static void concatener(File fichierCorpus, Corpus corpus) {
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new FileInputStream(fichierCorpus));
      corpus.concatenerCorpusJava(ois);
    } catch (ClassNotFoundException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ClassCastException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (InvalidClassException e) {
      JOptionPane.showMessageDialog(null, "Impossible de lire le corpus "+fichierCorpus.getName()
              +" : versions d'Analec incompatibles ",
              "Erreur d'ouverture de fichier .ec", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (ois!=null) ois.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public static boolean enregistrerCorpus(Corpus corpus, File fichier) {
    if (fichier!=null) return ecrireCorpus(corpus, fichier);
    return false;
  }

  public static boolean enregistrerCorpusSous(Corpus corpus) {
    File fichier;
    if ((fichier = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier corpus", Fichiers.TypeFichier.DOC_ANALEC))==null) return false;
    return ecrireCorpus(corpus, fichier);
  }

  public static void enregistrerStructure(Corpus corpus) {
    File fichier;
    if ((fichier = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier structure", Fichiers.TypeFichier.STRUCTURE))==null) return;
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(new FileOutputStream(fichier));
      corpus.ecrireStructureJava(oos);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (oos!=null) oos.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier écrit", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static boolean ecrireCorpus(Corpus corpus, File fichier) {
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(new FileOutputStream(fichier));
      corpus.ecrireJava(oos);
        return true;
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
         if (oos!=null) try {
        oos.close();
      } catch (IOException ex1) {
      }
     return false;
    
    }
  }

  public static boolean enregistrerVue(Vue vue) {
    File fichier;
    if ((fichier = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier vue", Fichiers.TypeFichier.VUE))==null) return false;
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(new FileOutputStream(fichier));
      vue.ecrireJava(oos);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return false;
    } finally {
      try {
        if (oos!=null) oos.close();
        return true;
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier écrit", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
  }
}
