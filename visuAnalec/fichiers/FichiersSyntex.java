/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

import java.io.*;
import java.util.*;
import javax.swing.*;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.util.Fichiers.*;

/**
 *
 * @author Bernard
 */
public class FichiersSyntex {
  public static void importerSyntex(Corpus corpus) {
    BufferedReader analyse = null;
    File fichierCorpus;
    GCodage codage = new GCodage();
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR, "Choix du fichier de résultats d'analyse syntaxique",
            Fichiers.TypeFichier.SYNTEX, codage.getContenant())) == null) return;
    String code = codage.getCode();
    int cadence = 1; 
    try {
      analyse = new BufferedReader(new InputStreamReader(new FileInputStream(fichierCorpus), code));
      SyntexParser sp = new SyntexParser(corpus);
      sp.parser(analyse, cadence);
      corpus.newCorpusLu();
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (analyse != null) analyse.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture du fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  static class SyntexParser {
    Corpus corpus;
    String texte;
    SyntexParser(Corpus corpus) {
      this.corpus = corpus;
      texte = "";
    }
    private void parser(BufferedReader anasynt, int cadence) throws IOException {
      String ligne;
      int natLigne = 0;
      int nophr = 0;
      Schema phr = null;
      String txt = null;
      while ((ligne = anasynt.readLine()) != null) {
        if (ligne.trim().isEmpty()) continue;
        switch (natLigne) {
          case 0:
            phr = parserSEQ(ligne);
            break;
          case 1:
            txt = parserTXT(ligne);
            texte += txt;
            corpus.addFinParagraphe(texte.length());
            break;
          case 2:
            if (++nophr % cadence == 0) {
              parserETIQ(ligne, phr, texte.length() - txt.length());
            }
            break;
        }
        natLigne = ++natLigne % 3;
      }
      if (natLigne > 0) throw new IOException("Fin de fichier prématurée");
      corpus.setTexte(texte);
    }
    private Schema parserSEQ(String ligne) throws IOException {
      if (!ligne.startsWith("<SEQ id=")) throw new IOException("SEQ attendu ligne :\n" + ligne);
      Schema phr = new Schema("Phrase");
      phr.putProp("id", ligne.substring(8, ligne.indexOf(';')));
      return phr;
    }
    private String parserTXT(String ligne) throws IOException {
      if (!ligne.startsWith("<TXT>")) throw new IOException("TXT attendu ligne :\n" + ligne);
      return supBlancs(ligne.substring(5));
    }
    private String supBlancs(String txt) {
      return txt.replaceAll("([\\'\\(\\-\\{\\[]) ", "$1").replaceAll(" ([\\.\\,\\-\\)\\%\\}\\]])", "$1");
    }
    private void parserETIQ(String ligne, Schema phr, int offset) throws IOException {
      if (!ligne.startsWith("<ETIQ>")) throw new IOException("ETIQ attendu ligne :\n" + ligne);
      String[] etiqs = ligne.substring(6).split("\t");
      Unite[] units = new Unite[etiqs.length];
      for (int i = 0; i < units.length; i++) units[i] = new Unite();
      ArrayList<Relation> rels = new ArrayList<Relation>();
      for (int i = 0; i < units.length; i++) {
        String[] data = etiqs[i].split("\\|", -1);
        if (data.length < 5) throw new IOException("Nombre de champs insuffisant pour " + etiqs[i]);
        if (Integer.parseInt(data[3]) != i + 1) throw new IOException("Problème de numérotation pour " + etiqs[i]);
        data[2] = supBlancs(data[2]);
        while (texte.charAt(offset) == ' ') offset++;
        if (!texte.startsWith(data[2], offset)) throw new IOException("Attendu : " + data[2] +
                  " au début de la chaîne\n" + texte.substring(offset));
        units[i].setType(typeSyntex(data[0]));
        units[i].setDeb(offset);
        units[i].setFin(offset += data[2].length());
        units[i].putProp("Catégorie Syntex", data[0]);
        units[i].putProp("Lemme", data[1]);
        if (data[4].isEmpty()) continue;
        String[] ascs = data[4].split(",");
        for (String asc : ascs) {
          String[] dataAsc = asc.split(";");
          int noAsc = Integer.parseInt(dataAsc[1]);
          if (noAsc <= 0 || noAsc > units.length)
            throw new IOException("Problème de numérotation pour l'ascendant de " + etiqs[i]);
          Relation rel = new Relation("Dépendance", units[i], units[noAsc - 1]);
          rel.putProp("Fonction syntex", dataAsc[0]);
          rels.add(rel);
        }
      }
      for (Unite unit : units) {
        corpus.addUniteLue(unit);
        phr.ajouter(unit);
      }
      for (Relation rel : rels) {  
        corpus.addRelationLue(rel);
        phr.ajouter(rel);
      }
      corpus.addSchemaLu(phr);
    }
    private String typeSyntex(String catSyntex) throws IOException {
      if (catSyntex.startsWith("Adj")) return "Adjectif";
      if (catSyntex.startsWith("Adv")) return "Adverbe";
      if (catSyntex.startsWith("CCoord") || catSyntex.startsWith("TypoCoord")) return "Coordonnant";
      if (catSyntex.equals("CSub")) return "Subordonnant";
      if (catSyntex.startsWith("Det")) return "Déterminant";
      if (catSyntex.equals("Elim")) return "Interjection";
      if (catSyntex.startsWith("Nom") || catSyntex.equals("NUM")) return "Nom";
      if (catSyntex.startsWith("Pp") || catSyntex.startsWith("V")) return "Verbe";
      if (catSyntex.startsWith("Prep")) return "Préposition";
      if (catSyntex.equals("Pro")) return "Pronom";
      if (catSyntex.equals("ProRel")) return "Relatif";
      if (catSyntex.equals("Typo")) return "Ponctuation";
      throw new IOException("Catégorie syntex non reconnue : " + catSyntex);
    }
  }
}
  
