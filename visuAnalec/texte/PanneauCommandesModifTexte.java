/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package visuAnalec.texte;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import visuAnalec.Message.*;
import visuAnalec.*;
import visuAnalec.PanneauEditeur.*;
import visuAnalec.elements.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.vue.*;

/**
 *
 * @author Bernard
 */
public class PanneauCommandesModifTexte extends Box  {
  Vue vue;
  PanneauModifTexte panneauTexte;
  PanneauEditeur panneauEditeur;
 public PanneauCommandesModifTexte( PanneauEditeur pEditeur, Vue vue, PanneauModifTexte pTexte) {
    super(BoxLayout.X_AXIS);
    this.panneauEditeur = pEditeur;
    this.vue = vue;
    this.panneauTexte = pTexte;
        add(Box.createHorizontalGlue());
    add( new JButton(new GAction("Valider") {
      public void executer() {
        validerModifTexte();
        panneauEditeur.finModifTexte();
      }
    }));
    add(Box.createHorizontalGlue());
    add( new JButton(new GAction("Annuler") {
      public void executer() {
        panneauEditeur.finModifTexte();
    }
    }));
    add(Box.createHorizontalGlue());
  }
 void validerModifTexte() {
   int nbSupprimes = panneauTexte.preparerValiderModifTexte();
  if (nbSupprimes > 0) {
      int rep = JOptionPane.showOptionDialog(getTopLevelAncestor(),
              "Les modifications du texte vont entraîner la suppression " +
              (nbSupprimes == 1 ? "d'une unité" : "de " + nbSupprimes + " unités"), "Suppression d'unités",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Continuer", "Annuler"}, "Continuer");
      if (rep == 1) return;
    }
    panneauTexte.validerModifTexte();
  }
 }
