/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package petitsTests;

import java.io.*;
import visuAnalec.util.Fichiers;
import visuAnalec.util.Fichiers.Commande;

/**
 *
 * @author Bernard
 */
public class corpusHashcode {
  public static void main(String[] strs) throws Exception {
    BufferedReader text = null;
    File fichierCorpus;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier corpus", Fichiers.TypeFichier.GLOZZ_CORPUS)) == null) return;
      text = new BufferedReader(new InputStreamReader(new FileInputStream(fichierCorpus), "UTF-8"));
String texte = text.readLine();
System.out.println(texte.hashCode());
  }

}
