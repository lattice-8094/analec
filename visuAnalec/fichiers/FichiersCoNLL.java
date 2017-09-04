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
public abstract class FichiersCoNLL {
  static final String TYPE_UNITES = "Token";
  static final String TYPE_RELATION = "Dependency";
  static final String TYPE_RELATION_PROJ = "Projective Dependency";
  static final String PROP_LEMME = "Lemma";
  static final String PROP_C_POS = "C-POS";
  static final String PROP_POS = "POS";
  static final String PROP_TRAITS = "Features";
  static final String PROP_REL = "Function";
  public static void importerCoNLL(Corpus corpus) {
    BufferedReader in = null;
    File ficCoNLL;
    GCodage codage = new GCodage();
    if ((ficCoNLL = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier CoNLL", TypeFichier.CONLL))==null)
      return;
    try {
      in = new BufferedReader(new InputStreamReader(
              new FileInputStream(ficCoNLL), "UTF-8"));
      CoNLLParser parseur = new CoNLLParser(corpus);
      parseur.parser(in);
      corpus.newCorpusLu();
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (in!=null) in.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture du fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  static class CoNLLParser {
    private Corpus corpus;
    private String texte="";
    private ArrayList<String> noHeads = new ArrayList<String>();
    private ArrayList<String> depRels = new ArrayList<String>();
    private ArrayList<String> noPHeads = new ArrayList<String>();
    private ArrayList<String> pDepRels = new ArrayList<String>();
    private ArrayList<Unite> units = new ArrayList<Unite>();
    private HashMap<String, Integer> noTokenUnits = new HashMap<String, Integer>();
    CoNLLParser(Corpus corpus) {
      this.corpus = corpus;
    }
    private void parser(BufferedReader in) throws IOException {
      String ligne;
      int noligne = 0;
      while (true) {
        ligne = in.readLine();
        if (ligne==null) {
          finDocument(noligne);
          break;
        }
        noligne++;
        if (ligne.trim().isEmpty()) finPhrase(noligne);
        else parserToken(ligne, noligne);
      }
    }
    private void parserToken(String ligne, int noligne) throws IOException {
      String ch;
      String[] items = ligne.split("\\t");
      if (items.length!=10)
        throw new CoNLLException("Nombre de colonnes incorrect ("
                +items.length+")", noligne);
      Unite unit = new Unite();
      unit.setType(TYPE_UNITES);
      units.add(unit);
      // champ ID
      ch = items[0].replace('_', ' ').trim();
      if (ch.isEmpty())
        throw new CoNLLException("Pas trouvé de numéro de token", noligne);
      noTokenUnits.put(ch, units.size()-1);
      // champ FORM
      ch = items[1].replace('_', ' ').trim();
      if (ch.isEmpty())
        throw new CoNLLException("Pas trouvé de token", noligne);
      if (isPonct1(ch)) texte = texte.trim();
      unit.setDeb(texte.length());
      texte += ch;
      unit.setFin(texte.length());
      if (!isPonct2(ch)) texte += " ";
      // champ LEMMA    
      ch = items[2].replace('_', ' ').trim();
      if (!ch.isEmpty())
        unit.putProp(PROP_LEMME, ch);
      // champ CPOSTAG
      ch = items[3].replace('_', ' ').trim();
      if (ch.isEmpty())
        throw new CoNLLException("Pas trouvé de C-POS", noligne);
      unit.putProp(PROP_C_POS, ch);
      // champ POSTAG
      ch = items[4].replace('_', ' ').trim();
      if (ch.isEmpty())
        throw new CoNLLException("Pas trouvé de POS", noligne);
      unit.putProp(PROP_POS, ch);
      // champ FEATS
      ch = items[5].replace('_', ' ').trim();
      if (!ch.isEmpty()) {
        String[] chs = ch.split("\\|");
        String traits = "";
        for (String trait : chs) {
          String[] attval = trait.split("=");
          if (attval.length==2)
            unit.putProp(attval[0].trim(), attval[1].trim());
          else {
            if (!traits.isEmpty()) traits += '|';
            traits += trait;
          }
        }
        if (!traits.isEmpty())
          unit.putProp(PROP_TRAITS, traits);
      }
      // champ HEAD
      ch = items[6].replace('_', ' ').trim();
      if (ch.isEmpty())
        throw new CoNLLException("Pas trouvé de HEAD", noligne);
      noHeads.add(ch);
      // champ DEPREL
      ch = items[7].replace('_', ' ').trim();
      if (ch.isEmpty())
        throw new CoNLLException("Pas trouvé de DEP-REL", noligne);
      depRels.add(ch);
      // champ PHEAD
      ch = items[8].replace('_', ' ').trim();
      noPHeads.add(ch);
      // champ PDEPREL
      ch = items[9].replace('_', ' ').trim();
      pDepRels.add(ch);
    }
    static private boolean isPonct1(String ch) {
      if (ch.length()!=1) return false;
      return ".,;:)]}!?'".contains(ch);
    }
    static private boolean isPonct2(String ch) {
      if (ch.length()!=1) return false;
      return "([{'".contains(ch);
    }
    private void finPhrase(int noligne) throws CoNLLException {
      for (Unite unit : units) corpus.addUniteLue(unit);
      for (int i = 0; i<units.size(); i++) {
        Unite unit = units.get(i);
        String nohead = noHeads.get(i);
        String deprel = depRels.get(i);
        if (!nohead.equals("0")) {
          Integer k = noTokenUnits.get(nohead);
          if (k==null)
            throw new CoNLLException("Erreur de numéro de token de HEAD : "
                    +nohead+" sur l'une des lignes précédentes", noligne);
          Relation rel = new Relation(TYPE_RELATION, units.get(i), units.get(k));
          if (deprel.isEmpty())
            throw new CoNLLException("Pas trouvé de DEP-REL"
                    +" sur l'une des lignes précédentes", noligne);
          rel.putProp(PROP_REL, deprel);
          corpus.addRelationLue(rel);
          nohead = noPHeads.get(i);
          deprel = pDepRels.get(i);
          if (!nohead.isEmpty()&&!nohead.equals("0")) {
            k = noTokenUnits.get(nohead);
            if (k==null)
              throw new CoNLLException("Erreur de numéro de token de P-HEAD : "
                      +nohead+" sur l'une des lignes précédentes", noligne);
            rel = new Relation(TYPE_RELATION_PROJ, units.get(i), units.get(k));
            if (deprel.isEmpty())
              throw new CoNLLException("Pas trouvé de P-DEP-REL"
                      +" sur l'une des lignes précédentes", noligne);
            rel.putProp(PROP_REL, deprel);
            corpus.addRelationLue(rel);
          }
        }
      }
      units.clear();
      noTokenUnits.clear();
      noHeads.clear();
      depRels.clear();
      noPHeads.clear();
      pDepRels.clear();
    }
    private void finDocument(int noligne) throws IOException {
      finPhrase(noligne);
      texte = texte.trim();
      corpus.setTexte(texte);
      corpus.addFinParagraphe(texte.length());
    }

    static class CoNLLException extends IOException {
      public CoNLLException(String ex, int noligne) {
        super("Erreur de syntaxe ligne "+noligne+" :\n"+ex);
      }
    }
  }
}
