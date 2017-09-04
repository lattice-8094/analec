/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petitsTests;

import JamaPlus.Matrix;
import JamaPlus.SingularValueDecomposition;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.plaf.*;

/**
 *
 * @author Bernard
 */
public class EllipseDemo extends JComponent {

  private EllipsePainter painter;
  private Matrix Pts;

  EllipseDemo(double[][] pts) {
    super();
    Pts = new Matrix(pts);
    setUI(painter = new EllipsePainter());
    painter.setEllipse(Pts);
    repaint();
  }

  private static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("EllipseDemo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    EllipseDemo demo = new EllipseDemo(new double[][]{{30, -40}, {100, -20},
                 {30, -20}, {60, 20}, {100, 20}, {-50, 10} });
    frame.setContentPane(demo);

    //Display the window.
    frame.setSize(450, 260);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        createAndShowGUI();
      }
    });
  }

  public static class Ellipse {

    double xcentre, ycentre;
    double grandAxe, petitAxe;
    double xrotation, yrotation;

    public Ellipse(double xc, double yc, double ga, double pa, double xr, double yr) {
      xcentre = xc;
      ycentre = yc;
      petitAxe = pa;
      grandAxe = ga;
      xrotation = xr;
      yrotation = yr;
    }
  }

  static class EllipsePainter extends ComponentUI {

    private final static int marge = 20;
    private final static int bord = marge - 1;
    private final static int rayon = 3;
    static EllipsePainter ellipseUI = new EllipsePainter();
    private double xRange, yRange;
    private Ellipse ellipse;
    private double[] xPts, yPts;
    private int nbPts;
    private boolean[] isDedans;
//    private Color[] couleurs;
//    private boolean[] etatPts;
//    private Rectangle[] rects;
//

    void setEllipse(Matrix Pts) {
      nbPts = Pts.getRowDimension();
      isDedans = new boolean[nbPts]; 
      double xmin = Pts.minColumn(0);
      double ymin = Pts.minColumn(1);
      xRange = Pts.maxColumn(0) - xmin;
      yRange = Pts.maxColumn(1) - ymin;
      xPts = new double[nbPts];
      yPts = new double[nbPts];
      for (int i = 0; i < nbPts; i++) {
        xPts[i] = Pts.get(i, 0) - xmin;
        yPts[i] = Pts.get(i, 1) - ymin;
      }
      Matrix PtsEntoures = Pts.getMatrix(0, nbPts -2, 0, 1);
      SingularValueDecomposition svd = new SingularValueDecomposition(PtsEntoures.columnCovariance());
      Matrix C = PtsEntoures.columnMean();
      double xcentre = C.get(0, 0);
      double ycentre = C.get(0, 1);
      Matrix U = svd.getU();
      double xrotation = U.get(0, 0);
      double yrotation = U.get(0, 1);
      double[] s = svd.getSingularValues();
      Matrix OPts = Pts.firstLineMinus(C).times(U);
      double[] d2Pts = new double[nbPts];
      double maxd2Pts = 0;
      for (int i = 0; i < nbPts; i++) {
        d2Pts[i] = (OPts.get(i, 0) * OPts.get(i, 0)) / s[0]
                + (OPts.get(i, 1) * OPts.get(i, 1)) / s[1];
        if(i<nbPts-1) maxd2Pts = Math.max(maxd2Pts, d2Pts[i]);
      }
      for (int i = 0; i < nbPts; i++) {
        isDedans[i] = d2Pts[i] <= maxd2Pts;
      }
      double delta = 0.01;
      double r = Math.sqrt(maxd2Pts)+delta;
      System.out.println(r);
      double grandaxe = r *Math.sqrt(s[0]);
      double petitaxe = r * Math.sqrt(s[1]);
      ellipse = new Ellipse(xcentre - xmin, ycentre - ymin,
              grandaxe, petitaxe, xrotation, yrotation);

    }
//
//    void clearPts() {
//        nbPts = 0;
//        xRange = 0;
//        yRange = 0;
//        xPts = new double[0];
//        yPts = new double[0];
//        rects = new Rectangle[0];
//    }
//
//    void setEtatPts(boolean[] etats) {
//        etatPts = etats;
//    }
//
//    void setCouleurs(Color[] couleurs) {
//        this.couleurs = couleurs;
//    }
//
//    ArrayList<Integer> getPts(Point pt) {
//        ArrayList<Integer> nos = new ArrayList<Integer>();
//        for (int no = 0; no < nbPts; no++) {
//            if (rects[no].contains(pt)) {
//                nos.add(no);
//            }
//        }
//        return nos;
//    }

    @Override
    public void paint(Graphics g0, JComponent c) {
      if (nbPts == 0) return;
      Graphics2D g = (Graphics2D) g0;
      double echelle;
      int x0, y0, xrg, yrg, x, y;
      Dimension dim = c.getSize();
      double echx = (dim.width - 2 * marge) / xRange;
      double echy = (dim.height - 2 * marge) / yRange;
      if (echx < echy) {
        echelle = echx;
        xrg = (int) Math.round(xRange * echelle);
        yrg = (int) Math.round(yRange * echelle);
        x0 = marge;
        y0 = (int) Math.round((dim.height - yrg) / 2.0);
      } else {
        echelle = echy;
        xrg = (int) Math.round(xRange * echelle);
        yrg = (int) Math.round(yRange * echelle);
        x0 = (int) Math.round((dim.width - xrg) / 2.0);
        y0 = marge;
      }
//      System.out.println(x0 + " " + y0 + " " + xrg + " " + yrg);
      g.drawRect(x0 - bord, y0 - bord, xrg + 2 * bord, yrg + 2 * bord);
      Ellipse2D.Double ell = new Ellipse2D.Double(
              x0 + ellipse.xcentre * echelle - ellipse.grandAxe * echelle,
              y0 + ellipse.ycentre * echelle - ellipse.petitAxe * echelle,
              2 * ellipse.grandAxe * echelle, 2 * ellipse.petitAxe * echelle);
      g.draw(AffineTransform.getRotateInstance(
              ellipse.xrotation, ellipse.yrotation,
              x0 + ellipse.xcentre * echelle,
              y0 + ellipse.ycentre * echelle).createTransformedShape(ell));
      for (int i = 0; i < nbPts; i++) {
        x = x0 + (int) Math.round(echelle * xPts[i]);
        y = y0 + (int) Math.round(echelle * yPts[i]);
            if(isDedans[i]) g.setColor(Color.RED);
            else g.setColor(Color.BLUE);
//            rects[i] = new Rectangle(x - rayon, y - rayon, 2 * rayon, 2 * rayon);
        g.fillOval(x - rayon - 1, y - rayon - 1, 2 * rayon + 2, 2 * rayon + 2);
//            g.setStroke(etatPts[i] ? ligneEpaisse : ligneFine);
//            g.setColor(Color.BLACK);
        int epais = 0;
        g.drawOval(x - (rayon + epais), y - (rayon + epais), 2 * (rayon + epais), 2 * (rayon + epais));
      }
//
    }

    public static ComponentUI createUI(JComponent c) {
      return ellipseUI;
    }
  }
}
