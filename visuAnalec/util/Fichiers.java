/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.util;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

/**
 *
 * @author Bernard
 */
public abstract class Fichiers {
  public enum TypeFichier {
    DOC_ANALEC("Document Analec", "ec"),
    CORPUS_ANALEC("Fichiers Analec", "ec"),
    STRUCTURE("Structure Analec", "ecs"),
    VUE("Vue Analec", "ecv"),
    TABLEUR_STATS("Tableur format texte (séparateur : tabulation)", "txt"),
    GRAPHIQUE_SVG("Graphique vectoriel format svg", "svg"),
    GRAPHIQUE_EMF("Graphique vectoriel format emf", "emf"),
    GLOZZ_CORPUS("Corpus Glozz", "ac"),
    GLOZZ_ANNOT("Annotations Glozz", "aa"),
    GLOZZ_MODELE("Modèle Glozz", "aam"),
    SYNTEX("Résultats Syntex", "anasynt"),
    TABLEUR_ENONCES("Table des énoncés", "txt"),
    TABLEUR_ANNOT("Table des annotations", "txt"),
    TEI("Corpus format TEI", "xml"),
    RHAPS("Corpus Rhapsodie format tabulaire", "tabular"),
    CONLL("Document format CoNLL", "out"),
    TEXTE("Texte brut", "txt"),
    TEXTE_RTF("Texte format RTF", "rtf"),
    TEXTE_HTML("Texte format HTML", "html");
    String ext;
    String nom;
    TypeFichier(String nom, String ext) {
      this.nom = nom;
      this.ext = ext;
    }
    public String getNom() {
      return nom;
    }
    public String getExtension() {
      return ext;
    }
    public boolean importCorpus() {
      return EnumSet.of(TypeFichier.TEXTE, TypeFichier.GLOZZ_CORPUS, TypeFichier.SYNTEX).contains(this);
    }
    public boolean resultats() {
      return EnumSet.of(TypeFichier.TABLEUR_STATS, TypeFichier.GRAPHIQUE_SVG, TypeFichier.GRAPHIQUE_EMF).contains(this);
    }
  }

  public enum Commande {
    OUVRIR("Ouvrir"),
    ENREGISTRER("Enregistrer");
    String label;
    Commande(String label) {
      this.label = label;
    }
    public String getLabel() {
      return label;
    }
  }

  public static class FiltreExt implements java.io.FileFilter {
    private TypeFichier type;
    public FiltreExt(TypeFichier type) {
      this.type = type;
    }
    public boolean accept(File fic) {
      return type.getExtension().equals(getExtension(fic));
    }
  }
  // Variables de classe
  private static Preferences pref = Preferences.userNodeForPackage(Fichiers.class);
  private static File repertoireCourantCorpus = new File(pref.get("repertoire", System.getProperty("user.dir")));
  private static EnumMap<TypeFichier, File> fichiersCourants = new EnumMap<TypeFichier, File>(TypeFichier.class);
  static {
    String nomfic = pref.get("nomCorpus", "");
    if (!nomfic.isEmpty())
      fichiersCourants.put(TypeFichier.DOC_ANALEC, new File(repertoireCourantCorpus, nomfic+'.'+TypeFichier.DOC_ANALEC.getExtension()));
  }
  public static File trouverFichier(TypeFichier type, String nomFichier) {
    File fichier = new File(repertoireCourantCorpus, nomFichier);
    if (!getExtension(fichier).equals(type.getExtension()))
      fichier = changerExtension(fichier, type.getExtension());
    if (!fichier.exists()) return null;
    majFichiersCourants(type, fichier);
    return fichier;
  }
  public static File trouverFichier(TypeFichier type, File oldfichier) {
    File fichier = changerExtension(oldfichier, type.getExtension());
    if (!fichier.exists()) return null;
    majFichiersCourants(type, fichier);
    return fichier;
  }
  public static File creerFichier(File rep, TypeFichier type, File oldfichier) {
    return ajouterExtension(new File(rep, extraireNom(oldfichier)), type.getExtension());
  }
  public static File creerFichier(File rep, TypeFichier type, String nom) {
    return ajouterExtension(new File(rep, nom), type.getExtension());
  }
  public static File getRepertoireCourant(TypeFichier type) {
    if (fichiersCourants.containsKey(type))
      return fichiersCourants.get(type).getParentFile();
    return repertoireCourantCorpus;
  }
  public static File getFichierCourant(TypeFichier type) {
    if (fichiersCourants.containsKey(type)) return fichiersCourants.get(type);
    return null;
  }
  public static String getNomFichierCourant(TypeFichier type) {
    if (fichiersCourants.containsKey(type))
      return extraireNom(fichiersCourants.get(type));
    return "";
  }
  public static void removeFichierCourant(TypeFichier type) {
    fichiersCourants.remove(type);
  }
  private static void majFichiersCourants(TypeFichier type, File fichier) {
    fichiersCourants.put(type, fichier);
    if (type==TypeFichier.GLOZZ_CORPUS) {
      fichiersCourants.put(TypeFichier.GLOZZ_ANNOT, changerExtension(fichier, TypeFichier.GLOZZ_ANNOT.ext));
      fichiersCourants.put(TypeFichier.GLOZZ_MODELE, changerExtension(fichier, TypeFichier.GLOZZ_MODELE.ext));
    }
    if (type==TypeFichier.DOC_ANALEC) {
      repertoireCourantCorpus = fichier.getParentFile();
      pref.put("repertoire", repertoireCourantCorpus.getPath());
      pref.put("nomCorpus", extraireNom(fichier));
    }

  }
  // Utilitaires de manipulation de noms de fichiers
  private static File ajouterExtension(File fichier, String extension) {
    String nom = fichier.getName();
    return new File(fichier.getParentFile(), nom+"."+extension);
  }
  private static File changerExtension(File fichier, String extension) {
    String nom = fichier.getName();
    int ind = nom.lastIndexOf('.');
    if (ind>=0) nom = nom.substring(0, ind);
    return new File(fichier.getParentFile(), nom+"."+extension);
  }
  public static String extraireNom(File fichier) {
    if (fichier==null) return null;
    String nom = fichier.getName();
    int ind = nom.lastIndexOf('.');
    if (ind<0) return nom;
    else return nom.substring(0, ind);
  }
  private static String getExtension(File fichier) {
    String nom = fichier.getName();
    int ind = nom.lastIndexOf('.');
    if (ind>=0) return nom.substring(ind+1);
    return "";
  }
  public static File[] listerFichiers(File rep, TypeFichier type) {
    return rep.listFiles(new FiltreExt(type));
  }
  public static boolean existeDebutFichier(File rep, TypeFichier type, 
          String debut) {
    File[] fics = listerFichiers(rep, type);
    for (int i = 0; i<fics.length; i++) 
      if(extraireNom(fics[i]).startsWith(debut)) return true;
    return false;
  }
  // Choix des fichiers
  public static File choisirFichier(Commande commande, String titre, TypeFichier type) {
    return choisirFichier(commande, titre, type, null);
  }
  public static File choisirFichier(Commande commande, String titre, TypeFichier type, JComponent complement) {
    JFileChooser choixFichier = new JFileChooser();
    choixFichier.resetChoosableFileFilters();
    choixFichier.setFileFilter(new FileNameExtensionFilter(type.getNom(), type.getExtension()));
    choixFichier.setDialogTitle(titre);
    if (complement!=null) choixFichier.setAccessory(complement);
    choixFichier.setCurrentDirectory(getRepertoireCourant(type));
    File fichierPropose;
    if (type.resultats()) {
      String nom = extraireNom(getFichierCourant(TypeFichier.DOC_ANALEC));
      if (nom==null) fichierPropose = null;
      else fichierPropose = new File(getRepertoireCourant(type), nom+"_res");
    } else {
      fichierPropose = getFichierCourant(type);
    }
    if (fichierPropose!=null) {
      if (commande==Commande.OUVRIR&&!fichierPropose.exists()) {
        fichierPropose = new File(getRepertoireCourant(type), "*."+type.getExtension());
      }
      choixFichier.setSelectedFile(fichierPropose);
    }
    int reponse = choixFichier.showDialog(null, commande.getLabel());
    if (reponse!=JFileChooser.APPROVE_OPTION) return null;
    File fichierChoisi = choixFichier.getSelectedFile();
    if (commande==Commande.ENREGISTRER) {
      if (!getExtension(fichierChoisi).equals(type.getExtension()))
        fichierChoisi = ajouterExtension(fichierChoisi, type.getExtension());
      if (fichierChoisi.exists()) {
        int rep = JOptionPane.showOptionDialog(null, "Remplacer "+fichierChoisi.getName()+" ?",
                "Fichier existant", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{"Remplacer", "Annuler"}, "Remplacer");
        if (rep==1) return null;
      }
    }
    majFichiersCourants(type, fichierChoisi);
    if (commande==Commande.OUVRIR&&type.importCorpus())
      fichiersCourants.remove(TypeFichier.DOC_ANALEC);
    return fichierChoisi;
  }
  public static File choisirRepertoire(String titre) {
    JFileChooser choixRep = new JFileChooser();
    choixRep.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    choixRep.setAcceptAllFileFilterUsed(false);
    choixRep.setFileFilter(new FileNameExtensionFilter("Répertoire", "*"));
    choixRep.setDialogTitle(titre);
    choixRep.setCurrentDirectory(repertoireCourantCorpus);
    int reponse = choixRep.showDialog(null, "Sélectionner");
    if (reponse!=JFileChooser.APPROVE_OPTION) return null;
    return choixRep.getSelectedFile();
  }
  public static File[] choisirCorpus(String titre, TypeFichier type) {
    JFileChooser choixFichier = new JFileChooser();
    FilesystemFilter filtre = new FilesystemFilter(type.getExtension(), "", false);
    choixFichier.addPropertyChangeListener(new SelectFics(choixFichier, filtre));
    choixFichier.resetChoosableFileFilters();
    choixFichier.setFileFilter(new FileNameExtensionFilter(
            type.getNom(), type.getExtension()));
    choixFichier.setDialogTitle(titre);
    choixFichier.setMultiSelectionEnabled(true);
    File rep = getRepertoireCourant(type);
    choixFichier.setCurrentDirectory(rep);
    int reponse = choixFichier.showDialog(null, "Choisir");
    if (reponse!=JFileChooser.APPROVE_OPTION) return new File[0];
    File[] fics = choixFichier.getSelectedFiles();
    if (fics==null||fics.length==0) return new File[0];
    majFichiersCourants(TypeFichier.CORPUS_ANALEC, fics[0]);
    return fics;
  }

  static class FilesystemFilter extends javax.swing.filechooser.FileFilter
          implements FilenameFilter {
    private String[] extensions;
    private String description;
    private boolean accepteReps;
    public FilesystemFilter(String extension, String description,
            boolean accepteReps) {
      this(new String[]{extension}, description, accepteReps);
    }
    public FilesystemFilter(String[] extensions, String description,
            final boolean accepteReps) {
      this.extensions = extensions;
      this.description = description;
      this.accepteReps = accepteReps;
    }
    public boolean accept(File rep, String nom) {
      return accept(new File(rep, nom));
    }
    public boolean accept(final File fic) {
      if (fic.isDirectory()&&accepteReps) return true;
      for (int i = 0; i<this.extensions.length; i++)
        if (fic.getName().endsWith(this.extensions[i])) return true;
      return false;
    }
    public String getDescription() {
      return this.description;
    }
 }
 public static class SelectFics implements PropertyChangeListener{
    JFileChooser choix;
    FilesystemFilter filtre;
    SelectFics(JFileChooser choix, FilesystemFilter filtre) {
      this.choix = choix;
      this.filtre = filtre;
    }
    @Override
       public void propertyChange(PropertyChangeEvent e) {
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
           choix.setSelectedFiles(choix.getCurrentDirectory().listFiles(filtre));
        }
    }
  }

  public static class GCodage {
    JPanel contenant;
    JComboBox saisieCodage;
    public GCodage() {
      saisieCodage = new JComboBox(new String[]{
                "Microsoft (windows-1252)",
                "UTF-8 (unicode 8 bits)",
                "UTF-16 (unicode 16 bits)",
                "ISO-8859-1 (latin-1)",
                "<autre (à saisir)>"
              });
      saisieCodage.setEditable(true);
      saisieCodage.setSelectedIndex(0);
      contenant = new JPanel(new BorderLayout());
      contenant.setBorder(new TitledBorder("Codage des caractères"));
      contenant.add(saisieCodage, BorderLayout.NORTH);
      contenant.add(new JPanel(), BorderLayout.CENTER);
    }
    public JComponent getContenant() {
      return contenant;
    }
    public String getCode() {
      switch (saisieCodage.getSelectedIndex()) {
        case 0:
          return "windows-1252";
        case 1:
          return "UTF-8";
        case 2:
          return "UTF-16";
        case 3:
          return "ISO-8859-1";
        default:
          return (String) saisieCodage.getSelectedItem();
      }
    }
  }
}
