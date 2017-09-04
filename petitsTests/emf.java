/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petitsTests;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 *
 * @author Bernard
 */
public class emf {

  public static void main(String[] args) throws IOException {
   FileChannel canal = null;
      FileOutputStream sortie = null;
    File fichierGraphique = new File("testjava.emf");
      sortie = new FileOutputStream(fichierGraphique);
      canal = sortie.getChannel();
      ByteBuffer buff = ecrireEMF("");
      
    canal.write(buff);

         canal.close();
        sortie.close();
  }
    private static ByteBuffer ecrireEMF(String donnees) {
    ArrayList<Integer> dessin = new ArrayList<Integer>();
    int nbrecords = 0;
    int nbobjs = 0;
    dessin.add(1); // header
    dessin.add(88); // taille du header 
    dessin.addAll(Arrays.asList(0, 0, 500, 400)); // taille du graphique en px 
    dessin.addAll(Arrays.asList(0, 0, 15000, 12000)); // puis en 0.01 mm (px * 31)
    dessin.add(0x464D4520); // signature (" EMF" à l'endroit!!) 
    dessin.add(0x00010000); // version 
    int indTailles = dessin.size();  // endroit où mettre les tailles 
    dessin.addAll(Arrays.asList(0, 0, 0)); // taille du fichier, nb records, nbobjs
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
    dessin.addAll(Arrays.asList(0x9, 0x10, 500, 400));  // setWindowExtEx: 0,0
    nbrecords++;
    dessin.addAll(Arrays.asList(0xB, 0x10, 500, 400));  // setViewPortExtEx: 0,0
    nbrecords++;
    dessin.addAll(Arrays.asList(0x27, 0x18, 1, 1, 0, 0));  // createBrushIndirect : 
    nbrecords++;                                        //1 (NoObj) 1 (BS_NULL) 0 0
    nbobjs++;
    dessin.addAll(Arrays.asList(0x25, 0xC, 1));  // selectObject 1
    nbrecords++;
    dessin.addAll(Arrays.asList(0x5F, 0x34, 2, 0, 0, 0, 0, 0x12200, 0x4, 0, 0, 0, 0));
    // extCreatePen : 2 (NoObj) 0 0 0 0 (absence de DIB) 
    // 0x12200 (PS_GEOMETRIC, JOIN_MITER, ENDCAP_FLAT) 
    // 0x4 (largeur) 0 (BS_NULL) 0 (Color) 0 (BrushHatch) 0 (numStyleEntries)     
    nbrecords++;
    nbobjs++;
    dessin.addAll(Arrays.asList(0x25, 0xC, 2));  // selectObject 2
    nbrecords++;
    dessin.addAll(Arrays.asList(0x2B, 0x18, 100, 100, 400, 300));  //  Rectangle
    nbrecords++;
    // + setPolyFillMode : 0x13 0xC 0x2 (winding mode)
    dessin.addAll(Arrays.asList(0x28, 0xC, 1));  // deleteObject 1
     nbrecords++;
    dessin.addAll(Arrays.asList(0x27, 0x18, 1, 0, 0xFF0000, 0));  // createBrushIndirect : 
    nbrecords++;                            //1 (NoObj) 0 (BS_SOLID) 0xFF (Color 0BGR) 0
    dessin.addAll(Arrays.asList(0x25, 0xC, 1));  // selectObject 1
    nbrecords++;
   dessin.addAll(Arrays.asList(0x2A, 0x18, 200, 150, 250, 200));  //  Ellipse
    nbrecords++;

    dessin.addAll(Arrays.asList(0x28, 0xC, 1));  // deleteObject 1
     nbrecords++;
    dessin.addAll(Arrays.asList(0x27, 0x18, 1, 0, 0xFFFF00, 0));  // createBrushIndirect : 
    nbrecords++;                            //1 (NoObj) 0 (BS_SOLID) 0xFF (Color 0BGR) 0
    dessin.addAll(Arrays.asList(0x25, 0xC, 1));  // selectObject 1
    nbrecords++;
   dessin.addAll(Arrays.asList(0x2A, 0x18, 300, 250, 310, 260));  //  Ellipse
    nbrecords++;
// >>> Ellipse :  0x2A 0x18 left top right bottom (coord viewport)   
    // + setPolyFillMode : 0x13 0xC 0x2    
    // + createBrushIndirect : 0x27 0x18 1 0 0xFFFF 0
    // + selectObject : 0x25 0xC, 0x1  
    // + deleteObject : 0x28 0xC 0x3
    // + setMiterLimit : 0x3A 0xC 0x4
    // + beginPath 0x3B 0x8
    // + moveToEx 0x1B 0x10 0x640 0xFFFFF7E2
    // + polyBezierTo16 0x58 0x4C
    // + closeFigure 0x3D 0x8
    // + endPath 0x3C 0x8
    // + strokeAndFillPath 0x3F 0x18

    // + setPolyFillMode : 0x13 0xC 0x2    
    // + createBrushIndirect : 0x27 0x18 3 0 0xFF0000 0
    // + selectObject : 0x25 0xC, 0x3  
    // + deleteObject : 0x28 0xC 0x1
    // + setMiterLimit : 0x3A 0xC 0x4
    // + beginPath 0x3B 0x8
    // + moveToEx 0x1B 0x10 0x258 0xFFFFFEA6  >>>>360
    // + polyBezierTo16 0x58 0x4C
    // + closeFigure 0x3D 0x8
    // + endPath 0x3C 0x8
    // + strokeAndFillPath 0x3F 0x18
    // + deleteObject : 0x28 0xC 0x2
    // + deleteObject : 0x28 0xC 0x3

    dessin.addAll(Arrays.asList(0xE, 0x14, 0, 0, 0x14));  //  EOF 0xE 
    nbrecords++;                                      //+ taille (20?) et offset palette
    dessin.set(indTailles++, dessin.size()*4);
    dessin.set(indTailles++, nbrecords);
    dessin.set(indTailles, nbobjs);
ByteBuffer buff = ByteBuffer.allocate(dessin.size()*4).order(ByteOrder.LITTLE_ENDIAN);
buff.clear();
for(int n: dessin) buff.putInt(n);
buff.flip();
return buff;
  }
}
