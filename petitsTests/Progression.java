/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petitsTests;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import visuAnalec.util.GMessages;

/**
 *
 * @author Bernard
 */
public class Progression extends JFrame {
  public static void main(String[] args) {
    System.out.println("c'est parti");
    new Progression();
  }

  public Progression() {
    super("Démo");
    Rectangle rmax = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    setLocation(500, 50);
    setSize(rmax.width-600, rmax.height-100);
    setVisible(true);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    GMessages.GProgression progress = new GMessages.GProgression(200, "", "");
    for (int i = 0; i<1000000; i++) {
      if (i%10000==0) {
        progress.setNbTraites(i/10000);
        System.out.println("---"+i/10000);
        for (int j = 0; j<1000000; j++)
          if (j%1000==0) System.out.println(i+j/1000);
      }
    }
  }
  /*  public static class GProgression extends ProgressMonitor implements ActionListener {
  static int compteur;
  
  public GProgression(JFrame fenetre, int max) {
  super(fenetre, "Tâche en cours", "Démarrage", 0, max);
  compteur = 0;
  Timer timer = new Timer(500, this);
  timer.start();
  }
  
  public void setNbTraites(int nb) {
  //        setProgress(nb);
  //        setNote(nb +" fichiers traités");
  //        System.out.println(getNote());
  }
  
  public void actionPerformed(ActionEvent evt) {
  SwingUtilities.invokeLater(new MiseAJour());
  
  }
  
  class MiseAJour implements Runnable {
  public void run() {
  setProgress(compteur);
  setNote(compteur+" fichiers traités ");
  compteur += 2;
  }
  }
  }
   * */
}
