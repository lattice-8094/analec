/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

import java.awt.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import javax.swing.*;
import visuAnalec.util.*;
import visuAnalec.util.Fichiers.*;

/**
 *
 * @author Bernard
 */
public class FichiersGraphique {

  public static class CercleColore {

    public int cx, cy, r;
    public Color couleur;
  }

  public static interface GraphiqueExport {

    public Dimension initExport();

    public Rectangle getCadre();

    public CercleColore[] getCerclesColores();
  }

  public static void exporterGraphiqueSVG(GraphiqueExport donnees) {
    PrintWriter sortie = null;
    File fichierGraphique;
    if ((fichierGraphique = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier ?", Fichiers.TypeFichier.GRAPHIQUE_SVG)) == null) return;
    try {
      sortie = new PrintWriter(fichierGraphique, "UTF-8");
      ecrireSVG(sortie, donnees);
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier",
              JOptionPane.ERROR_MESSAGE);
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      if (sortie != null) sortie.close();
    }
  }

  public static void ecrireSVG(PrintWriter sortie, GraphiqueExport donnees) {
    sortie.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    sortie.println("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"");
    Dimension dim = donnees.initExport();
    sortie.println("x=\"0px\" y=\"0px\" width=\"" + dim.width
            + "px\" height=\"" + dim.height + "px\">");
    Rectangle cadre = donnees.getCadre();
    if (cadre != null)
      sortie.println("<rect x=\"" + cadre.x + "\" y=\"" + cadre.y
              + "\" fill=\"none\" stroke=\"#000000\" width=\"" + cadre.width
              + "\" height=\"" + cadre.height + "\"/>");
    CercleColore[] cercles = donnees.getCerclesColores();
    for (CercleColore cercle : cercles)
      sortie.println("<ellipse cx=\"" + cercle.cx + "\" cy=\""
              + cercle.cy + "\" fill=\"" + ecritCouleurSVG(cercle.couleur)
              + "\" stroke=\"#000000\" rx=\"" + cercle.r
              + "\" ry=\"" + cercle.r + "\"/>");

    sortie.println("</svg>");
  }

  private static String ecritCouleurSVG(Color c) {
    String res = "#";
    res += Integer.toHexString(c.getRGB()).substring(2);
    return res;
  }

  public static void exporterGraphiqueEMF(GraphiqueExport donnees) {
    FileChannel canal = null;
    FileOutputStream sortie = null;
    File fichierGraphique;
    if ((fichierGraphique = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier ?", Fichiers.TypeFichier.GRAPHIQUE_EMF)) == null) return;
    try {
      sortie = new FileOutputStream(fichierGraphique);
      canal = sortie.getChannel();
      ByteBuffer buff = ecrireEMF(donnees);
      canal.write(buff);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      if (canal != null)
        try {
          canal.close();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier",
                  JOptionPane.ERROR_MESSAGE);
        }
      if (sortie != null)
        try {
          sortie.close();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture de fichier",
                  JOptionPane.ERROR_MESSAGE);
        }
    }
  }

  private static ByteBuffer ecrireEMF(GraphiqueExport donnees) {
    ArrayList<Integer> dessin = new ArrayList<Integer>();
        Dimension dim = donnees.initExport();
    int nbrecords = 0;
    dessin.add(1); // header
    dessin.add(88); // taille du header 
    dessin.addAll(Arrays.asList(0, 0, dim.width, dim.height)); 
                                                   // taille du graphique en px 
    dessin.addAll(Arrays.asList(0, 0, dim.width*32, dim.height*32)); 
                                                  // taille en 0.01 mm (px * 32)
    dessin.add(0x464D4520); // signature (" EMF" à l'endroit!!) 
    dessin.add(0x00010000); // version 
    int indTailles = dessin.size();  // endroit où mettre les tailles 
    dessin.addAll(Arrays.asList(0, 0)); // taille du fichier, nb records : remplis à la fin
    dessin.add(2); // nbobjs : pen et brush
    dessin.addAll(Arrays.asList(0, 0));   //taille et offset de la description
    dessin.add(0); // nb d'entrees de palette 
    dessin.addAll(Arrays.asList(1024, 768, 320, 240)); // taille du device en px et en mm
    nbrecords++;
    dessin.addAll(Arrays.asList(0x11, 0xC, 0x8));  // mapmode : 0x8 (anisotropique)
    nbrecords++;
    dessin.addAll(Arrays.asList(0x12, 0xC, 0x1));  // bkmode : 0x1 (transparent)
    nbrecords++;
    dessin.addAll(Arrays.asList(0xA, 0x10, 0, 0));  // setWindowOrgEx: 0,0
    nbrecords++;
    dessin.addAll(Arrays.asList(0xC, 0x10, 0, 0));  // setViewPortOrgEx: 0,0
    nbrecords++;
    dessin.addAll(Arrays.asList(0x9, 0x10, dim.width, dim.height));  // setWindowExtEx
    nbrecords++; // setWindowExtEx: 0,0
    dessin.addAll(Arrays.asList(0xB, 0x10, dim.width, dim.height));  // setViewPortExtEx
    nbrecords++;
    dessin.addAll(Arrays.asList(0x27, 0x18, 1, 1, 0, 0));  // createBrushIndirect : 
    nbrecords++;                                        //1 (NoObj) 1 (BS_NULL) 0 0
    dessin.addAll(Arrays.asList(0x25, 0xC, 1));  // selectObject 1
    nbrecords++;
    dessin.addAll(Arrays.asList(0x5F, 0x34, 2, 0, 0, 0, 0, 0x12200, 0x4, 0, 0, 0, 0));
    // extCreatePen : 2 (NoObj) 0 0 0 0 (absence de DIB) 
    // 0x12200 (PS_GEOMETRIC, JOIN_MITER, ENDCAP_FLAT) 
    // 0x4 (largeur) 0 (BS_NULL) 0 (Color) 0 (BrushHatch) 0 (numStyleEntries)     
    nbrecords++;
    dessin.addAll(Arrays.asList(0x25, 0xC, 2));  // selectObject 2
    nbrecords++;
      Rectangle cadre = donnees.getCadre();
    if (cadre != null) {
  
    dessin.addAll(Arrays.asList(0x2B, 0x18, cadre.x, cadre.y, 
            cadre.x+cadre.width, cadre.y+cadre.height));  //  Rectangle
    nbrecords++;
    }
 CercleColore[] cercles = donnees.getCerclesColores();
   for (CercleColore cercle : cercles) {
//      sortie.println("<ellipse cx=\"" + cercle.cx + "\" cy=\""
//              + cercle.cy + "\" fill=\"" + ecritCouleurSVG(cercle.couleur)
//              + "\" stroke=\"#000000\" rx=\"" + cercle.r
//              + "\" ry=\"" + cercle.r + "\"/>");
    dessin.addAll(Arrays.asList(0x28, 0xC, 1));  // deleteObject 1
    nbrecords++;
    dessin.addAll(Arrays.asList(0x27, 0x18, 1, 0, ecritCouleurEMF(cercle.couleur), 0));  
    nbrecords++;      // createBrushIndirect : 1 (NoObj) 0 (BS_SOLID) Color (0BGR) 0
    dessin.addAll(Arrays.asList(0x25, 0xC, 1));  // selectObject 1
    nbrecords++;
    dessin.addAll(Arrays.asList(0x2A, 0x18, cercle.cx-cercle.r, cercle.cy-cercle.r,
            cercle.cx+cercle.r, cercle.cy+cercle.r));  //  Ellipse
    nbrecords++;
   }
    dessin.addAll(Arrays.asList(0xE, 0x14, 0, 0, 0x14));  //  EOF 0xE 
    nbrecords++;                                      //+ taille (20?) et offset palette
    dessin.set(indTailles++, dessin.size() * 4);
    dessin.set(indTailles, nbrecords);
    ByteBuffer buff = ByteBuffer.allocate(dessin.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
    buff.clear();
    for (int n : dessin) buff.putInt(n);
    buff.flip();
    return buff;
  }
    private static int ecritCouleurEMF(Color c) {
    return c.getRed() | (c.getGreen()<<8) | (c.getBlue()<<16);
  }


}
