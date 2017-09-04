/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.util;

import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author Bernard
 */
public class GMessages {
  public static boolean confirmationAction(String texte) {
    return (JOptionPane.showConfirmDialog(null, texte, "Confirmation d'action",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
            !=JOptionPane.CANCEL_OPTION);
  }

  public static void pasImplemente() {
    JOptionPane.showMessageDialog(null, "Désolé, cette fonctionnalité n'est pas encore implémentée",
            "Patience...", JOptionPane.INFORMATION_MESSAGE);
    return;
  }

  public static void impossibiliteAction(String texte) {
    JOptionPane.showMessageDialog(null, texte,
            "Désolé...", JOptionPane.INFORMATION_MESSAGE);
    return;
  }

  public static void erreurFatale(Throwable ex) {
    String message = "<center><b>Désolé pour ce bug... Vous allez devoir relancer Analec.</b></center>"
            +"<i><br>Veuillez recopier  le message d'erreur ci-dessous et l'envoyer au concepteur du logiciel"
            +" <br>en précisant les conditions dans lequel ce bug s'est produit</i>";
    String erreur = "<p>"+ex.toString();
    for (StackTraceElement tr : ex.getStackTrace())
      if (tr.getClassName().startsWith("visuAnalec")||tr.getClassName().startsWith("JamaPlus") )
        erreur += "<br>"+tr.getClassName()+" : "+tr.getMethodName()+" ligne "+tr.getLineNumber();
    JEditorPane dialogue = new JEditorPane("text/html", message+erreur);
    JOptionPane.showMessageDialog(null, dialogue, "Oups !", JOptionPane.ERROR_MESSAGE);
    System.exit(1);
  }

  public abstract static class GAction extends AbstractAction {
    public GAction(String nom) {
      super(nom);
    }

    public GAction(String nom, Icon icone) {
      super(nom, icone);
    }

    public abstract void executer();

    public void actionPerformed(ActionEvent evt) {
      try {
        executer();
      } catch (Throwable ex) {
        erreurFatale(ex);
      }
    }
  }

  public abstract static class GActionClavier extends GAction {
    public GActionClavier(JComponent composant, String nom, String... touches) {
      super(nom);
      for(String touche: touches) {
        composant.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(touche), nom);
      }
      composant.getActionMap().put(nom, this);
    }
  }
  public static class GProgression extends ProgressMonitor  {
    private int compteur;
    private String elementsTraites;

    public GProgression(int max, String traitement, String elementsTraites) {
      super(null, traitement + " en cours", "Démarrage", 0, max);
      this.elementsTraites = elementsTraites;
//      setMillisToPopup(0);
      compteur = 0;
     }

    public void setNbTraites(int nb) {
      compteur = nb;
      SwingUtilities.invokeLater(new MiseAJour());
    }

    class MiseAJour implements Runnable {
      public void run() {
        setProgress(compteur);
        setNote(compteur +" "+ elementsTraites + " traités");
      }
    }
  }
}