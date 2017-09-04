/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.texte.PanneauOccurrences;
import visuAnalec.util.*;
import visuAnalec.util.Fichiers.*;

/**
 *
 * @author Bernard
 */
public class FichiersTableur {
  public static void importerExcel(Corpus corpus) {
    BufferedReader enonces = null;
    BufferedReader annot = null;
    File fichierCorpus;
    File fichierAnnot;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier des énoncés", Fichiers.TypeFichier.TABLEUR_ENONCES))==null)
      return;
    if ((fichierAnnot = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier des annotations", Fichiers.TypeFichier.TABLEUR_ANNOT))==null)
      return;
    try {
      enonces = new BufferedReader(new InputStreamReader(new FileInputStream(fichierCorpus), "ISO-8859-1"));
      annot = new BufferedReader(new InputStreamReader(new FileInputStream(fichierAnnot), "ISO-8859-1"));
      ExcelParser ep = new ExcelParser(corpus);
      corpus.setTexte(ep.parserEnonces(enonces, 2));
      ep.parserAnnot(annot, 2, "<*", "*>");
      corpus.newCorpusLu();
    } catch (IOException ex) {
      corpus.fermer();
      GMessages.impossibiliteAction("Erreur de lecture de fichier : \n"
              +ex.getLocalizedMessage());
      return;
    } finally {
      try {
        if (enonces!=null) enonces.close();
        if (annot!=null) annot.close();
      } catch (IOException ex) {
        GMessages.impossibiliteAction("Erreur de fermeture du fichier lu : \n"
                +ex.getLocalizedMessage());
      }
    }
  }
  public static void exporterTable(TableModel donnees) {
    PrintWriter ecrittableur = null;
    File fichierStats;
    GCodage codage = new GCodage();
    if ((fichierStats = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier ?", Fichiers.TypeFichier.TABLEUR_STATS,
            codage.getContenant()))==null) return;
    String code = codage.getCode();
    try {
      ecrittableur = new PrintWriter(fichierStats, code);
      ecrireTable(ecrittableur, donnees);
    } catch (FileNotFoundException ex) {
      GMessages.impossibiliteAction("Erreur d'écriture de fichier : \n"
              +"Codage de caractères non pris en charge : "+code);
    } catch (UnsupportedEncodingException ex) {
    } finally {
      if (ecrittableur!=null) ecrittableur.close();
    }
  }
  private static void ecrireTable(PrintWriter ecrittableur, TableModel donnees) {
    int nbcol = donnees.getColumnCount();
    for (int j = 0; j<nbcol; j++) {
      ecrittableur.print(donnees.getColumnName(j));
      ecrittableur.print(j==nbcol-1 ? '\n' : '\t');
    }
    int nblig = donnees.getRowCount();
    for (int i = 0; i<nblig; i++) for (int j = 0; j<nbcol; j++) {
        ecrittableur.print(donnees.getValueAt(i, j));
        ecrittableur.print(j==nbcol-1 ? '\n' : '\t');
      }

  }

  static class ExcelParser {
    Corpus corpus;
    HashMap<String, Unite> enonces;
    ExcelParser(Corpus corpus) {
      this.corpus = corpus;
      this.enonces = new HashMap<String, Unite>();
    }
    private String parserEnonces(BufferedReader lEnonces, int nbPropEnonces) throws IOException {
      String texte = "";
      String lEnonce;
      int nbcar = 0;
      while ((lEnonce = lEnonces.readLine())!=null) {
        if (lEnonce.trim().isEmpty()) continue;
        String[] chs = lEnonce.split("\t");
        if (chs.length!=2+nbPropEnonces) {
          throw new IOException("Format incorrect de fichier d'énoncés ligne : \n"+lEnonce);
        }
        String idEnonce = chs[0].trim();
        if (enonces.containsKey(idEnonce)) {
          throw new IOException("Identifant d'énoncés non unique  : "+idEnonce);
        }
        String texteEnonce = chs[1].trim();
        Unite enonce = new Unite("Enoncé", nbcar, nbcar += texteEnonce.length());
        for (int i = 0; i<nbPropEnonces; i++) {
          enonce.putProp("Propriété "+i, chs[i+2].trim());
        }
        enonces.put(idEnonce, enonce);
        enonce.putProp("id", idEnonce);
        corpus.addUniteLue(enonce);
        corpus.addFinParagraphe(nbcar);
        texte += texteEnonce;
      }
      return texte;
    }
    private void parserAnnot(BufferedReader lAnnots, int nbPropAnnots, String bGauche, String bDroite)
            throws IOException {
      String lAnnot;
      while ((lAnnot = lAnnots.readLine())!=null) {
        if (lAnnot.trim().isEmpty()) continue;
        String[] chs = lAnnot.split("\t", -1);
        if (chs.length!=2+nbPropAnnots) {
          throw new IOException("Format incorrect de fichier d'énoncés ligne : \n"+lAnnot);
        }
        String idEnonce = chs[0].trim();
        String texteEnonceAnnot = chs[1].trim();
        if (!enonces.containsKey(idEnonce)) {
          throw new IOException("Identifiant d'énoncés non trouvé : \n"+idEnonce);
        }
        int deb = texteEnonceAnnot.indexOf(bGauche);
        if (texteEnonceAnnot.substring(deb+bGauche.length()).indexOf(bGauche)>=0) {
          throw new IOException("Plusieurs bornes gauches dans : \n"+texteEnonceAnnot);
        }
        String texteEnonce = texteEnonceAnnot.replace(bGauche, "");
        int fin = texteEnonce.indexOf(bDroite);
        if (texteEnonce.substring(fin+bDroite.length()).indexOf(bDroite)>=0) {
          throw new IOException("Plusieurs bornes droites dans : \n"+texteEnonceAnnot);
        }
        if (fin<=deb) {
          throw new IOException("Borne droite précédant la borne gauche dans : \n"+texteEnonceAnnot);
        }
        texteEnonce = texteEnonce.replace(bDroite, "");
        Unite enonce = enonces.get(idEnonce);
        if (!texteEnonce.equals(corpus.getTexte().substring(enonce.getDeb(), enonce.getFin()))) {
          throw new IOException("Erreur d'énoncé identifié par "+idEnonce+" : \n"+texteEnonceAnnot
                  +"Enoncé dans le corpus : "+corpus.getTexte().substring(enonce.getDeb(), enonce.getFin()));
        }
        Unite marqueur = new Unite("Marqueur", enonce.getDeb()+deb, enonce.getDeb()+fin);
        for (int i = 0; i<nbPropAnnots; i++) {
          marqueur.putProp("Propriété "+i, chs[i+2].trim());
        }
        corpus.addUniteLue(marqueur);
        Relation rel = new Relation("Inclusion", marqueur, enonce);
        corpus.addRelationLue(rel);

      }
    }
  }
}
