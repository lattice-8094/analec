/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package visuAnalec.statistiques;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import visuAnalec.util.GMessages;


/**
 *
 * @author Bernard
 */
public class GBarres extends JComponent implements TableModelListener {

  ModeleStatsFrequences modele;
  BarresPainter painter;

  GBarres(ModeleStatsFrequences modele) {
    super();
    setUI(painter = new BarresPainter());
    this.modele = modele;
//    System.out.println("---\nCréation : " + getPreferredSize() + " -> " + getSize());


  }

  public void tableChanged(TableModelEvent evt) {
    try {
      ArrayList<Float> longueurs = new ArrayList<Float>();
      ArrayList<String> noms = new ArrayList<String>();
      for (int i = 0; i < modele.getRowCount() - 1; i++) {
        if ((Integer) modele.getValueAt(i, 1) > 0) {
          longueurs.add((Float) modele.getValueAt(i, 2));
          noms.add((String) modele.getValueAt(i, 0));
        }
      }
//    System.out.println(noms + "\n" + getPreferredSize() + " -> " + getSize());
      painter.setLongueursBarres(longueurs.toArray(new Float[0]));
      painter.setNomsBarres(noms.toArray(new String[0]));
      repaint();
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
}
class BarresPainter extends ComponentUI {

  private String[] nomsBarres = new String[0];
  private Float[] longueursBarres = new Float[0];
  static BarresPainter barresUI = new BarresPainter();
  private Color[] couleurs = new Color[]{Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA,
    Color.RED, Color.YELLOW, Color.LIGHT_GRAY, Color.ORANGE, Color.PINK
  };
  private int margeX = 5;
  private int interligneY = 5;

  void setLongueursBarres(Float[] longueurs) {
    longueursBarres = longueurs;
  }

  void setNomsBarres(String[] noms) {
    nomsBarres = noms;
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    Dimension dim = c.getSize();
//    System.out.println(dim);
    int hauteurMin = g.getFontMetrics().getHeight() + 2;
    int nbBarres = nomsBarres.length;
    g.setColor(Color.BLACK);
    g.drawLine(margeX, interligneY, margeX, 2 * interligneY + (hauteurMin + interligneY) * nbBarres);
    int largeurMaxTexte = 0;
    for (int i = 0; i < nbBarres; i++) {
      largeurMaxTexte = Math.max(largeurMaxTexte,
              SwingUtilities.computeStringWidth(g.getFontMetrics(), nomsBarres[i]));
    }
    for (int i = 0; i < nbBarres; i++) {
      int y = interligneY + (hauteurMin + interligneY) * i;
      int longueur = Math.round((dim.width - largeurMaxTexte) * longueursBarres[i] / 100);
      g.setColor(Color.LIGHT_GRAY); // couleurs[i % couleurs.length]);
      g.fillRect(margeX, y, longueur, hauteurMin);
      g.setColor(Color.BLACK);
      g.drawRect(margeX, y, longueur, hauteurMin);
      g.drawString(nomsBarres[i], 2 * margeX + longueur, y + hauteurMin);
    }
  }

  public static ComponentUI createUI(JComponent c) {
    return barresUI;
  }
}


