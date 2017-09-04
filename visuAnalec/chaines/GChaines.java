/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.chaines;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import visuAnalec.chaines.ModeleChaines.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.util.GVisu.*;

/**
 *
 * @author Bernard
 */
public class GChaines extends JPanel {
  final static Font FONTE_ID = Font.decode("Arial bold 10");
  final static int RAYON_DISQUES = 6;
  final static int MARGE_DISQUES = 2;
  final static int LARGEUR_INTER_CHAINONS = 10;
  final static int MARGE_DEBUT_LIGNE = 15;
  final static int HAUTEUR_LIGNES = 80;
  final static int HAUTEUR_SEGMENT = 40;
  final static int HAUTEUR_CHAINONS = 60;
  final static String SYMBOLE_SEGMENT = "§";
//    final static int hauteurinfLien = 25;
//    final static int deltahauteurLien = 5;
//    final static int deltasupArcs = 95;
//    final static int hauteurinfArcs = 5;
//  final static int HAUTEUR_PANNEAU = 120;
  final static String PAS_DE_NUMERO = "~";

  static class GChainon extends JButton {
    VisuChaines visu;
    Chainon chainon;
    public GChainon(Chainon ch, VisuChaines vis) {
      super();
      this.visu = vis;
      this.chainon = ch;
      Icon d = new DisqueCouleurIcone(Color.WHITE, RAYON_DISQUES, MARGE_DISQUES);
      setAction(new GAction(null, d) {
        public void executer() {
          visu.afficherOccurrences(chainon.unite, chainon.chaine);
        }
      });
      setSize(2*(RAYON_DISQUES+MARGE_DISQUES), 2*(RAYON_DISQUES+MARGE_DISQUES));
      setBorderPainted(false);
      String t2 = "<html><div style=\"font-size:11pt\"><span style=\"font-style:italic\">"+chainon.texte
              +"</span><p>"+chainon.idChaine+"<p>"+chainon.nbEltsChaine+" occurences </div>";
      setToolTipText(t2);
      setBackground(Color.WHITE);
      setFocusPainted(false);
    }
    @Override
    public JToolTip createToolTip() {
      JToolTip jt = new JToolTip();
      jt.setBackground(Color.WHITE);
      return jt;
    }
    void changerCouleur(Color couleur) {
      setIcon(new DisqueCouleurIcone(couleur, RAYON_DISQUES, MARGE_DISQUES));
    }
  }

  static class GNoChainon extends JLabel {
    public GNoChainon(int noChaine) {
      super(noChaine==0 ? PAS_DE_NUMERO : Integer.toString(noChaine));
      setFont(FONTE_ID);
      setSize(getPreferredSize());
    }
    void localiser(Point chainonPosition, Dimension chainonTaille) {
      int x = chainonPosition.x+chainonTaille.width/2-getSize().width/2;
      int y = chainonPosition.y-getSize().height;
      setLocation(x, y);
    }
  }

  private enum TypeSegment {
    COMPLET("", ""),
    DEBUT("", "-"),
    MILIEU("-", "-"),
    FIN("-", "");
    String marqueAvant, marqueApres;
    private TypeSegment(String avant, String apres) {
      marqueAvant = avant;
      marqueApres = apres;
    }
  }

  static class GSegment extends JLabel {
    int xmin, xmax;
    int ybas, yhaut;
    int no;
    TypeSegment type;
    GSegment(int xmin, int xmax, int ybas, int yhaut, int no, TypeSegment type) {
      super(type.marqueAvant+SYMBOLE_SEGMENT+Integer.toString(no)+type.marqueApres);
      setFont(FONTE_ID);
      this.xmin = xmin;
      this.xmax = xmax;
      this.ybas = ybas;
      this.yhaut = yhaut;
      this.type = type;
      setSize(getPreferredSize());
    }
    void localiser() {
      setLocation((xmin+xmax)/2-getSize().width/2, yhaut-getSize().height);
    }
    void dessinerTraits(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics;
      g.setColor(Color.BLACK);
      Point p0 = new Point(xmin, ybas);
      Point p1 = new Point(xmin, yhaut);
      Point p2 = new Point(xmax, yhaut);
      Point p3 = new Point(xmax, ybas);
      switch (type) {
        case COMPLET:
          g.draw(new Line2D.Float(p0, p1));
          g.draw(new Line2D.Float(p1, p2));
          g.draw(new Line2D.Float(p2, p3));
          break;
        case DEBUT:
          g.draw(new Line2D.Float(p0, p1));
          g.draw(new Line2D.Float(p1, p2));
          break;
        case FIN:
          g.draw(new Line2D.Float(p1, p2));
          g.draw(new Line2D.Float(p2, p3));
          break;
        case MILIEU:
          g.draw(new Line2D.Float(p1, p2));
          break;
      }
    }
  }
  /*
   * Champs de la classeGChaines
   */
  VisuChaines visu;
  Chainon[] chainons = new Chainon[0];
  int[] noSegments = new int[0];
  Color[] couleurs = new Color[0];
  GChainon[] gChainons;
  ArrayList<GSegment> gSegments = new ArrayList<GSegment>();
  private int nbChainonsParLigne;
  GChaines(VisuChaines vc) {
    super((LayoutManager) null);
    setBackground(Color.WHITE);
    this.visu = vc;
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        visu.traiterAffTexteClicVide();
      }
    });
  }
  void setNbChainonsParLigne(int nb) {
    nbChainonsParLigne = nb;
    afficher();
    colorer();
  }
  void setDonnees(Chainon[] chainons, int[] noSegments) {
    this.chainons = chainons;
    this.noSegments = noSegments;
    afficher();
  }
  private void afficher() {
    effacerTout();
    if (chainons.length==0) return;
    gChainons = new GChainon[chainons.length];
    int xCourant = MARGE_DEBUT_LIGNE+LARGEUR_INTER_CHAINONS;
    int yCourant = 0;
    GNoChainon gNoChainon;
    int xminSeg = MARGE_DEBUT_LIGNE+LARGEUR_INTER_CHAINONS/2+2;
    GSegment gseg;
    int xmaxSeg = 0;
    int xmaxligne = 0;
    for (int i = 0; i<chainons.length; i++) {
      gChainons[i] = new GChainon(chainons[i], visu);
      add(gChainons[i]);
      gChainons[i].setLocation(xCourant, yCourant+HAUTEUR_CHAINONS);
      add(gNoChainon = new GNoChainon(chainons[i].noChaine));
      gNoChainon.localiser(gChainons[i].getLocation(), gChainons[i].getSize());
      xCourant += gChainons[i].getWidth()+LARGEUR_INTER_CHAINONS;
      xmaxligne = Math.max(xCourant, xmaxligne);
      xmaxSeg = xCourant-LARGEUR_INTER_CHAINONS/2-2;
      if (i+1==chainons.length||noSegments[i+1]>noSegments[i]) {
        // fin de paragraphe, donc on trace un segment terminal  
        if (xminSeg>0) {  // le paragraphe a commencé sur cette ligne
          add(gseg = new GSegment(xminSeg, xmaxSeg, yCourant+HAUTEUR_CHAINONS,
                  yCourant+HAUTEUR_SEGMENT, noSegments[i], TypeSegment.COMPLET));
        } else {  // le paragraphe a commencé avant cette ligne
          xminSeg = MARGE_DEBUT_LIGNE+LARGEUR_INTER_CHAINONS/2+2;
          add(gseg = new GSegment(xminSeg, xmaxSeg, yCourant+HAUTEUR_CHAINONS,
                  yCourant+HAUTEUR_SEGMENT, noSegments[i], TypeSegment.FIN));
        }
        gseg.localiser();
        gSegments.add(gseg);
        xminSeg = xmaxSeg+4;
      }
      if (i+1!=chainons.length&&(i+1)%nbChainonsParLigne==0) {  // on va changer de ligne
        if (xminSeg>xmaxSeg) {  // ça tombe bien : on change aussi de segment
          xminSeg = MARGE_DEBUT_LIGNE+LARGEUR_INTER_CHAINONS/2+2;
        } else { // ca tombe pas bien : il faut tracer un segment non terminal
          if (xminSeg>0) { // le paragraphe a commencé sur cette ligne
            add(gseg = new GSegment(xminSeg, xmaxSeg, yCourant+HAUTEUR_CHAINONS,
                    yCourant+HAUTEUR_SEGMENT, noSegments[i], TypeSegment.DEBUT));
          } else {  // le paragraphe a commencé avant cette ligne
            xminSeg = MARGE_DEBUT_LIGNE+LARGEUR_INTER_CHAINONS/2+2;
            add(gseg = new GSegment(xminSeg, xmaxSeg, yCourant+HAUTEUR_CHAINONS,
                    yCourant+HAUTEUR_SEGMENT, noSegments[i], TypeSegment.MILIEU));
          }
          gseg.localiser();
          gSegments.add(gseg);
          xminSeg = -1;
        }
        xCourant = MARGE_DEBUT_LIGNE+LARGEUR_INTER_CHAINONS;
        yCourant += HAUTEUR_LIGNES;
      }
    }
    setPreferredSize(new Dimension(xmaxligne+30, yCourant+HAUTEUR_LIGNES+30));
  }
  void setCouleurs(Color[] couleurs) {
    this.couleurs = couleurs;
    colorer();
  }
  private void colorer() {
    for (int i = 0; i<gChainons.length; i++) {
      gChainons[i].changerCouleur(couleurs[i]);
    }
    revalidate();
    repaint();
  }
  private void effacerTout() {
    removeAll();
    gChainons = new GChainon[0];
    gSegments.clear();
    repaint();
  }
  private void effacer(Graphics graphics) {
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, getWidth(), getHeight());
  }
  @Override
  protected void paintComponent(Graphics graphics) {
    effacer(graphics);
    for (GSegment gs : gSegments) gs.dessinerTraits(graphics);
  }
}
