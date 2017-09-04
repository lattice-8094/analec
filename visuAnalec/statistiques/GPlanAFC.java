/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.statistiques;

import JamaPlus.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;
import visuAnalec.elements.*;
import visuAnalec.fichiers.FichiersGraphique.*;
import visuAnalec.statistiques.ModeleStatsGeometrie.*;
import visuAnalec.util.*;
import visuAnalec.util.GMessages.*;

/**
 *
 * @author Bernard
 */
public class GPlanAFC extends JComponent
        implements DonneesGrapheListener, MouseListener, GraphiqueExport {

  private static class Ellipse {

    double xcentre, ycentre;
    double grandAxe, petitAxe;
    double xrotation, yrotation;

    private Ellipse(double xc, double yc, double ga, double pa, double xr, double yr) {
      xcentre = xc;
      ycentre = yc;
      petitAxe = pa;
      grandAxe = ga;
      xrotation = xr;
      yrotation = yr;
    }
  }
  private final static int marge = 20;
  private final static int delta = 1;
  private final static int bord = marge - delta;
  private final static int rayon = 3;
  private final static int epaisseurSelection = 3;
  private final static double epsilon = 0.0001;
  private final static BasicStroke ligneEpaisse = new BasicStroke(epaisseurSelection);
  private final static BasicStroke ligneFine = new BasicStroke();
  private final static int dimExport = 500;
  private double xRange, yRange, xMin, yMin;
  private double[] xPts, yPts;
  private int nbPts;
  private Color[] couleurs;
  private boolean[] etatPts;
  private Rectangle[] rects;
  private Ellipse ellipse;
  private ModeleStatsGeometrie donnees;
  private JPopupMenu menu;
  private double echelleExport;  // pour exporter le graphique

  GPlanAFC(ModeleStatsGeometrie donnees) {
    super();
    this.donnees = donnees;
    donnees.addEventListener(this);
    addMouseListener(this);
    menu = new JPopupMenu();
    menu.add(new GAction("Tout déselectionner") {

      public void executer() {
        deselectionnerTout();
      }
    });
    menu.add(new GAction("Entourer la sélection") {

      public void executer() {
        entourerSelection();
      }
    });
    menu.add(new GAction("Supprimer les points") {

      public void executer() {
        supprimerPts();
      }
    });
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  @Override
  public JToolTip createToolTip() {
    JToolTip jt = new JToolTip();
//    jt.setBackground(JAUNE_TRES_CLAIR);
    return jt;
  }

  public void traiterEvent(DonneesGrapheEvent evt) {
    switch (evt.getType()) {
      case NEW_DONNEES:
      case MODIF_DONNEES:
        setPts(donnees.getPts());
        couleurs = new Color[donnees.getPts().getRowDimension()];
        Arrays.fill(couleurs, GVisu.getCouleurSansValeur());
        etatPts = donnees.getEtatsChoixtPts();
        clearEllipse();
        repaint();
        return;
      case NEW_COORDS:
        setPts(donnees.getPts());
        repaint();
        return;
      case CLEAR_DONNEES:
        clearPts();
        repaint();
        return;
      case NEW_COULEURS:
        couleurs = donnees.getCouleurs();
        repaint();
        return;
      case NEW_SELECT_PTS:
        etatPts = donnees.getEtatsChoixtPts();
        repaint();
        return;
      default:
        throw new UnsupportedOperationException("cas " + evt.getType() + " non traité");
    }
  }

  public void mouseClicked(MouseEvent e) {
    try {
      ArrayList<Integer> nos = getPts(getMousePosition());
      if (!nos.isEmpty() && e.getButton() == MouseEvent.BUTTON1) {
        clearEllipse();
        donnees.majPtsChoisis(nos);
      }
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  public void mousePressed(MouseEvent e) {
    try {
      if (e.isPopupTrigger()) {
        showPopup(e);
      }
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  public void mouseReleased(MouseEvent e) {
    try {
      if (e.isPopupTrigger()) {
        showPopup(e);
      }
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }

  private void showPopup(MouseEvent e) {
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  @Override
  public String getToolTipText() {
    Point pt = getMousePosition();
    if (pt == null) return "";
    ArrayList<Integer> nopts = getPts(pt);
    if (nopts.isEmpty()) return "";
    ArrayList<ArrayList<String>> textesEtIds = donnees.getTexteIdElements(nopts);
    return GVisu.styleIdTooltip(textesEtIds.get(0), textesEtIds.get(1));
  }

  private void deselectionnerTout() {
    donnees.setPtsChoisis(new ArrayList<Integer>());
    clearEllipse();
  }

  private void entourerSelection() {
    ArrayList<Integer> nos = setEllipse(donnees.getPts(), donnees.getEtatsChoixtPts());
    donnees.setPtsChoisis(nos);
  }

  private void supprimerPts() {
    int nb = Tableaux.compterValeursVraies(donnees.getEtatsChoixtPts());
    if (nb == 0) return;
    if (!GMessages.confirmationAction("Supprimer " + nb
            + " élément(s) de la représentation ?")) return;
    donnees.supPtsChoisis();
  }

  private void setPts(Matrix Pts) {
    nbPts = Pts.getRowDimension();
    if (nbPts == 0) {
      clearPts();
      return;
    }
    xMin = Pts.minColumn(0);
    yMin = Pts.minColumn(1);
    xRange = Pts.maxColumn(0) - xMin;
    yRange = Pts.maxColumn(1) - yMin;
    xPts = new double[nbPts];
    yPts = new double[nbPts];
    rects = new Rectangle[nbPts];
    for (int i = 0; i < nbPts; i++) {
      xPts[i] = Pts.get(i, 0) - xMin;
      yPts[i] = Pts.get(i, 1) - yMin;
    }
    ellipse = null;
  }

  private void clearPts() {
    nbPts = 0;
    xRange = 0;
    yRange = 0;
    xPts = new double[0];
    yPts = new double[0];
    rects = new Rectangle[0];
    ellipse = null;
  }

  private void setEtatPts(boolean[] etats) {
    etatPts = etats;
  }

  private void setCouleurs(Color[] couleurs) {
    this.couleurs = couleurs;
  }

  private ArrayList<Integer> setEllipse(Matrix Pts, boolean[] aentourer) {
    ArrayList<Integer> listenos = new ArrayList<Integer>();
    for (int i = 0; i < aentourer.length; i++) if (aentourer[i])
        listenos.add(i);
    if (listenos.isEmpty()) return listenos;
    Matrix PtsEntoures = getPtsUniqueCoord(Pts, listenos);
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
    if (s[1] > epsilon) {  // cas général
      for (int i = 0; i < nbPts; i++) {
        d2Pts[i] = (OPts.get(i, 0) * OPts.get(i, 0)) / s[0]
                + (OPts.get(i, 1) * OPts.get(i, 1)) / s[1];
        if (aentourer[i]) maxd2Pts = Math.max(maxd2Pts, d2Pts[i]);
      }
      listenos.clear();
      for (int i = 0; i < nbPts; i++) if (d2Pts[i] <= maxd2Pts) listenos.add(i);
    } else if (s[0] > epsilon) { // tous les points sont alignés
      for (int i = 0; i < nbPts; i++) {
        d2Pts[i] = (OPts.get(i, 0) * OPts.get(i, 0)) / s[0];
        if (aentourer[i]) maxd2Pts = Math.max(maxd2Pts, d2Pts[i]);
      }
      listenos.clear();
      for (int i = 0; i < nbPts; i++)
        if (Math.abs(OPts.get(i, 1)) < epsilon && d2Pts[i] <= maxd2Pts)
          listenos.add(i);
    }  // si tous les points sont confondus : on garde listenos et max2Pts=0
    double r = Math.sqrt(maxd2Pts);
    double grandaxe = r * Math.sqrt(s[0]);
    double petitaxe = r * Math.sqrt(s[1]);
    ellipse = new Ellipse(xcentre - xMin, ycentre - yMin,
            grandaxe, petitaxe, xrotation, yrotation);
    return listenos;
  }

  private void clearEllipse() {
    ellipse = null;
  }

  private ArrayList<Integer> getPts(Point pt) {
    ArrayList<Integer> nos = new ArrayList<Integer>();
    for (int no = 0; no < nbPts; no++) {
      if (rects[no].contains(pt)) {
        nos.add(no);
      }
    }
    return nos;
  }

  private static Matrix getPtsUniqueCoord(Matrix Pts, ArrayList<Integer> nos) {
    ArrayList<Integer> noselects = new ArrayList<Integer>();
    double dx, dy;
    for (int i : nos) {
      boolean unique = true;
      for (int i1 : noselects) {
        dx = Pts.get(i, 0) - Pts.get(i1, 0);
        dy = Pts.get(i, 1) - Pts.get(i1, 1);
        if (dx * dx + dy * dy < epsilon) {
          unique = false;
          break;
        }
      }
      if (unique) noselects.add(i);
    }
    int[] nosels = new int[noselects.size()];
    for (int i = 0; i < nosels.length; i++) nosels[i] = noselects.get(i);
    return Pts.getMatrix(nosels, 0, 1);
  }

  @Override
  public void paintComponent(Graphics g0) {
    if (nbPts == 0) return;
    Graphics2D g = (Graphics2D) g0;
    double echelle;
    int x0, y0, xrg, yrg, x, y;
    Dimension dim = getSize();
    if (xRange == 0 && yRange == 0) {
      x0 = (int) Math.round((dim.width) / 2.0);
      xrg = 0;
      y0 = (int) Math.round((dim.height) / 2.0);
      yrg = 0;
      echelle = 0;
    } else if (xRange == 0) {   // impossible en fait : xRange >= yRange
      x0 = (int) Math.round((dim.width) / 2.0);
      xrg = 0;
      y0 = marge;
      echelle = (dim.height - 2 * marge) / yRange;
      yrg = (int) Math.round(yRange * echelle);
    } else if (yRange == 0) {
      echelle = (dim.width - 2 * marge) / xRange;
      xrg = (int) Math.round(xRange * echelle);
      x0 = marge;
      yrg = 0;
      y0 = (int) Math.round((dim.height) / 2.0);
    } else {
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
    }
    g.drawRect(x0 - bord, y0 - bord, xrg + 2 * bord, yrg + 2 * bord);
    for (int i = 0; i < nbPts; i++) {
      x = x0 + (int) Math.round(echelle * xPts[i]);
      y = y0 + (int) Math.round(echelle * yPts[i]);
      g.setColor(couleurs[i]);
      rects[i] = new Rectangle(x - rayon, y - rayon, 2 * rayon, 2 * rayon);
      g.fillOval(x - rayon - 1, y - rayon - 1, 2 * rayon + 2, 2 * rayon + 2);
      g.setStroke(etatPts[i] ? ligneEpaisse : ligneFine);
      g.setColor(Color.BLACK);
      int epais = etatPts[i] ? epaisseurSelection - 1 : 0;
      g.drawOval(x - (rayon + epais), y - (rayon + epais), 2 * (rayon + epais), 2 * (rayon + epais));
    }
    if (ellipse == null) return;
    double xc = x0 + ellipse.xcentre * echelle;
    double yc = y0 + ellipse.ycentre * echelle;
    double ga = ellipse.grandAxe * echelle + rayon + epaisseurSelection - 1;
    double pa = ellipse.petitAxe * echelle + rayon + epaisseurSelection - 1;
    Ellipse2D.Double ell = new Ellipse2D.Double(xc - ga, yc - pa, 2 * ga, 2 * pa);
    g.setStroke(ligneFine);
    g.draw(AffineTransform.getRotateInstance(ellipse.xrotation, ellipse.yrotation, xc, yc).
            createTransformedShape(ell));
  }

  public Dimension initExport() {
    if (nbPts == 0) return new Dimension(dimExport, dimExport);
    if (xRange == 0 && yRange == 0) {
      echelleExport = 0;
      return new Dimension(2 * marge, 2 * marge);
    } else if (xRange == 0) { // impossible, en fait
      echelleExport = dimExport / yRange;
      return new Dimension(2 * marge, dimExport + 2 * marge);
    } else if (yRange == 0) {
      echelleExport = dimExport / xRange;
      return new Dimension(dimExport + 2 * marge, 2 * marge);
    } else {
      echelleExport = dimExport / xRange;
      return new Dimension(dimExport + 2 * marge,
              ((int) Math.round(yRange * echelleExport)) + 2 * marge);
    }
  }

  public Rectangle getCadre() {
    if (nbPts == 0) return null;
    if (xRange == 0 && yRange == 0)
      return new Rectangle(delta, delta, 2 * bord, 2 * bord);
    else if (xRange == 0)
      return new Rectangle(delta, delta, 2 * bord, dimExport + 2 * bord);
    else if (yRange == 0)
      return new Rectangle(delta, delta, dimExport + 2 * bord, 2 * bord);
    else
      return new Rectangle(delta, delta, dimExport + 2 * bord,
              ((int) Math.round(yRange * echelleExport)) + 2 * bord);
  }

  public CercleColore[] getCerclesColores() {
    CercleColore[] cercles = new CercleColore[nbPts];
    for (int i = 0; i < nbPts; i++) {
      cercles[i] = new CercleColore();
      cercles[i].cx = marge + (int) Math.round(echelleExport * xPts[i]);
      cercles[i].cy = marge + (int) Math.round(echelleExport * yPts[i]);
      cercles[i].couleur = couleurs[i];
      cercles[i].r = rayon;
    }
    return cercles;
  }
}
