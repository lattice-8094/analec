/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import visuAnalec.PanneauEditeur;
import visuAnalec.donnees.*;
import visuAnalec.util.*;
import visuAnalec.util.Fichiers.*;

/**
 *
 * @author Bernard
 */
public abstract class FichiersTexte {
  public static void importerTexte(Corpus corpus) {
    BufferedReader fluxIn = null;
    File fichier;
    GCodage codage = new GCodage();
    if ((fichier = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du corpus", Fichiers.TypeFichier.TEXTE, codage.getContenant())) == null) return;
    String code = codage.getCode();
    try {
      fluxIn = new BufferedReader(new InputStreamReader(new FileInputStream(fichier), code));
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, "Codage de caractères non pris en charge : " + code,
              "Erreur d'ouverture de fichier texte", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'ouverture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      corpus.lireTexte(fluxIn);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (fluxIn != null) fluxIn.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier lu", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
  }
  public static void exporterTexteRTF(PanneauEditeur pEditeur) {
    FileOutputStream fluxOut = null;
    FiltreCorrectionRTFOutputStream filtre = null;
    File fichierCorpus;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier texte", Fichiers.TypeFichier.TEXTE_RTF)) == null) return;
    try {
      fluxOut = new FileOutputStream(fichierCorpus);
      filtre = new FiltreCorrectionRTFOutputStream(fluxOut);
      Document doc = pEditeur.getTexteFormate();
      RTFEditorKit editeur = new RTFEditorKit();
      editeur.write(filtre, doc, 0, doc.getLength());
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (BadLocationException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      if (fluxOut != null) try {
          fluxOut.close();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier", JOptionPane.ERROR_MESSAGE);
        }
    }
  }

  private static class FiltreCorrectionRTFOutputStream extends FilterOutputStream {
    // Note: Windows versions of Word have never supported \cbN, 
    //but it can be emulated by the control word sequence 
    // \chshdng0\chcbpatN.
    private final static char[] CodeCouleurFondcorrect = "\\chshdng0\\chcbpat".toCharArray();
    private final static char[] CodeUnicodecorrect = "\\'".toCharArray();
    private int etat;
    private int[] buffer;
    private int pt = 0;
    private FiltreCorrectionRTFOutputStream(OutputStream out) {
      super(out);
      pt = etat = 0;
      buffer = new int[10];
    }
    @Override
    public void write(int car) throws IOException {
      switch (etat) {
        case 0:
          if (car == '\\') {
            buffer[pt++] = car;
            etat = 1;
          } else out.write(car);
          return;
        case 1:
          if (car == 'c') {  // debut d'une couleur de fond : etats 2, 3, 4
            buffer[pt++] = car;
            etat = 2;
          } else if (car == 'u') {  // début d'un caractère unicode non décodé : etats 5, 6
            buffer[pt++] = car;
            etat = 5;
          } else raz(car);
          return;
        case 2:
          if (car == 'b') {
            buffer[pt++] = car;
            etat = 3;

          } else raz(car);
          return;
        case 3:
          if (car >= '0' && car <= '9') {
            buffer[pt++] = car;
            etat = 4;
          } else raz(car);
          return;
        case 4:
          if (car >= '0' && car <= '9') buffer[pt++] = car;
          else correctionCouleurFond(car);
          return;
        case 5:
          if (car >= '0' && car <= '9') {
            buffer[pt++] = car;
            etat = 6;
          } else raz(car);
          return;
        case 6:
          if (car >= '0' && car <= '9') buffer[pt++] = car;
          else if (car == ' ') etat = 7;
          else raz(car);
          return;
        case 7:
          if (car == '?') correctionUnicode();
          else raz(car);
          return;
        default:
          throw new UnsupportedOperationException("Etat " + etat + " non prévu");
      }
    }
    private void raz(int b) throws IOException {
      for (int pt0 = 0; pt0 < pt; pt0++) out.write(buffer[pt0]);
      pt = etat = 0;
      write(b);
    }
    private void correctionCouleurFond(int b) throws IOException {
      for (int c : CodeCouleurFondcorrect) out.write(c);
      for (int pt0 = 3; pt0 < pt; pt0++) out.write(buffer[pt0]);
      pt = etat = 0;
      write(b);
    }
    private void correctionUnicode() throws IOException {
      for (int c : CodeUnicodecorrect) out.write(c);
      int n = 0;
      for (int pt0 = 2; pt0 < pt; pt0++) n = 10 * n + (buffer[pt0] - '0');
      String nhex = Integer.toHexString(n % 256);
      if (nhex.length() == 1) out.write('0');
      out.write(nhex.charAt(0));
      if (nhex.length() == 2) out.write(nhex.charAt(1));
      pt = etat = 0;
    }
  }
  public static void exporterTexteHTML(PanneauEditeur pEditeur) {
    FileWriter fluxOut = null;
//   FiltreCorrectionRTFOutputStream filtre = null;
    File fichierCorpus;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier texte", Fichiers.TypeFichier.TEXTE_HTML)) == null) return;
    try {
      fluxOut = new FileWriter(fichierCorpus);
      Document doc = pEditeur.getTexteFormate();
      HTMLWriterCorrige htmlWriter = new HTMLWriterCorrige(fluxOut, (StyledDocument) doc);
      htmlWriter.write();
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (BadLocationException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'encodage de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      if (fluxOut != null)
        try {
          fluxOut.close();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier", JOptionPane.ERROR_MESSAGE);
        }
    }
  }

  private static class HTMLWriterCorrige extends AbstractWriter {
    public HTMLWriterCorrige(Writer w, StyledDocument doc) {
      super(w, doc);
    }
    @Override
    protected void write() throws IOException, BadLocationException {
      write("<html>\n");
      ElementIterator it = getElementIterator();
      Element elt;
      it.first();  // le premier (la racine) n'a pas l'info intéressante)
      if((elt = it.next()) != null) writeTagBody(elt.getAttributes());
      while(elt!=null) {
        if (elt.isLeaf()) {
          writeSpan(elt.getAttributes(), getText(elt));
        }
        elt = it.next();
      }       
      write("</body>\n");
      write("</html>\n");
    }
    private void writeTagBody(AttributeSet attr) throws IOException {
      write("<body style=\" ");
      write("font-family: " + StyleConstants.getFontFamily(attr));
      write("; font-size: " + StyleConstants.getFontSize(attr) + "pt");
      write("; font-style: " + (StyleConstants.isItalic(attr) ? "italic" : "normal"));
      write("; font-weight: " + (StyleConstants.isBold(attr) ? "bold" : "normal"));
      write("; text-decoration: " + (StyleConstants.isUnderline(attr) ? "underline" : "none"));
      write("; color: ");
      writeColor(StyleConstants.getForeground(attr));
      write("; background-color: ");
      writeColor(StyleConstants.getBackground(attr));
      write("\">\n");
    }
    private void writeSpan(AttributeSet attr, String texte) throws IOException {
      write("<span style=\" ");
      write("; font-style: " + (StyleConstants.isItalic(attr) ? "italic" : ""));
      write("; font-weight: " + (StyleConstants.isBold(attr) ? "bold" : ""));
      write("; text-decoration: " + (StyleConstants.isUnderline(attr) ? "underline" : ""));
      write("; color: ");
      writeColor(StyleConstants.getForeground(attr));
      write("; background-color: ");
      writeColor(StyleConstants.getBackground(attr));
      write("\">");
      for(int i=0; i<texte.length(); i++) writeCar(texte.charAt(i));
      write("</span>\n");
    }
    private void writeColor(Color couleur) throws IOException {
      write('#');
      String str = Integer.toHexString(couleur.getRed() % 256);
      if (str.length() == 1) write('0');
      write(str.charAt(0));
      if (str.length() == 2) write(str.charAt(1));
      str = Integer.toHexString(couleur.getGreen() % 256);
      if (str.length() == 1) write('0');
      write(str.charAt(0));
      if (str.length() == 2) write(str.charAt(1));
      str = Integer.toHexString(couleur.getBlue() % 256);
      if (str.length() == 1) write('0');
      write(str.charAt(0));
      if (str.length() == 2) write(str.charAt(1));
    }

  private void writeCar(char car) throws IOException {
    switch(car) {
      case '\n': write("<p>"); return;
      case '"': write("&quot;");return;
      case'<': write("&lt;");return;
      case'>': write("&gt;");return;
      default:
        if(car>128) write("&#"+(car%256)+";");
        else write(car);
    }   
   }   
  }
}
