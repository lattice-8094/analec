/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.texte;

import java.util.*;
import javax.swing.event.*;
import visuAnalec.PanneauEditeur.*;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;

/**
 *
 * @author Bernard
 */
public class PanneauModifTexte extends PanneauTexte implements DocumentListener {
  protected static class BorneUnite implements Comparable {
    public Unite unite;
    public int pos;
    public BorneUnite(Unite unite, int pos) {
      this.unite = unite;
      this.pos = pos;
    }
    public int compareTo(Object obj) {
      if (obj.getClass() != BorneUnite.class) {
        throw new ClassCastException("Comparaison impossible : " + obj.toString() +
                " n'est pas une instance de BorneUnite");
      }
      int comp = Integer.signum(pos - (((BorneUnite) obj).pos));
      if (comp == 0) {
        comp = -Integer.signum(unite.getFin() - unite.getDeb() - ((BorneUnite) obj).unite.getFin() +
                ((BorneUnite) obj).unite.getDeb());
      }
      return comp;
    }
  }
  private PanneauCommandesModifTexte panneauCommandes;
  private BorneUnite[] bornesGauches;
  private BorneUnite[] bornesDroites;
  public PanneauModifTexte(Corpus corpus) {
    super(corpus);
    setEditable(true);
  }
  public void setCommandesModifTexte(PanneauCommandesModifTexte panneauCommandes) {
    this.panneauCommandes = panneauCommandes;
  }
  void setBornesModifTexte() {
    ArrayList<Unite> units = corpus.getToutesUnites();
    bornesGauches = new BorneUnite[units.size()];
    bornesDroites = new BorneUnite[units.size()];
    for (int i = 0; i < units.size(); i++) {
      Unite unite = units.get(i);
      bornesGauches[i] = new BorneUnite(unite, unite.getDeb());
      bornesDroites[i] = new BorneUnite(unite, unite.getFin());
    }
    Arrays.sort(bornesGauches);
    Arrays.sort(bornesDroites);

  }
  public void init() {
    doc.removeDocumentListener(this);
    setTexteVisu();
    setEditable(true);
    doc.setParagraphAttributes(0, getText().length(), AttributsStandard, true);
    doc.setCharacterAttributes(0, getText().length(), AttributsStandard, true);
    setBornesModifTexte();
    doc.addDocumentListener(this);
  }
  public int preparerValiderModifTexte() {
    Comparator<BorneUnite> comp = new Comparator<BorneUnite>() {
      public int compare(BorneUnite b1, BorneUnite b2) {
        return b1.unite.compareTo(b2.unite);
      }
    };
    Arrays.sort(bornesGauches, comp);
    Arrays.sort(bornesDroites, comp);
    int nbSupprimes = 0;
    for (int i = 0; i < bornesGauches.length; i++) {
      if (bornesGauches[i].pos == bornesDroites[i].pos) {
        nbSupprimes++;
      }
    }
    return nbSupprimes;
  }
  public void validerModifTexte() {
    for (int i = 0; i < bornesGauches.length; i++) {
      Unite unite = bornesGauches[i].unite;
      if (bornesGauches[i].pos == unite.getDeb() && bornesDroites[i].pos == unite.getFin()) {
        continue;
      }
      if (bornesGauches[i].pos == bornesDroites[i].pos) {
        corpus.supUnite(unite);
      } else {
        corpus.modifBornesUnite(unite, bornesGauches[i].pos, bornesDroites[i].pos);
      }
    }
    setEditable(false);
    setTexteCorpus();  // Corpus enverra l'événement MODIF_TEXTE à tous les panneaux occurrence
  }
  public void insertUpdate(DocumentEvent evt) {
    try {
      String texte = getText();
      int debutInsertion = positionToIndex(evt.getOffset());
      int lengthInsertion = evt.getLength();
      int iIns = 0;
      while (iIns < posNewLines.size() && posNewLines.get(iIns) < evt.getOffset()) {
        iIns++;
      }
      for (int ind = iIns; ind < posNewLines.size(); ind++) {
        posNewLines.set(ind, posNewLines.get(ind) + evt.getLength());
      }
      for (int pos = evt.getOffset(); pos < evt.getOffset() + evt.getLength(); pos++) {
        if (texte.charAt(pos) == '\n') {
          lengthInsertion--;
          posNewLines.add(iIns++, pos);
        }
      }
      shiftDroitBornes(bornesGauches, debutInsertion, lengthInsertion);
      shiftDroitBornes(bornesDroites, debutInsertion, lengthInsertion);
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  public void removeUpdate(DocumentEvent evt) {
    try {
      int debutSuppression = positionToIndex(evt.getOffset());
      int lengthSuppression = evt.getLength();
      int iSup = 0;
      while (iSup < posNewLines.size() && posNewLines.get(iSup) < evt.getOffset()) {
        iSup++;
      }
      while (iSup < posNewLines.size() && posNewLines.get(iSup) < evt.getOffset() + evt.getLength()) {
        posNewLines.remove(iSup);
        lengthSuppression--;
      }
      for (int ind = iSup; ind < posNewLines.size(); ind++) {
        posNewLines.set(ind, posNewLines.get(ind) - evt.getLength());
      }
      shiftGaucheBornes(bornesGauches, debutSuppression, lengthSuppression);
      shiftGaucheBornes(bornesDroites, debutSuppression, lengthSuppression);
    } catch (Throwable ex) {
      GMessages.erreurFatale(ex);
    }
  }
  public void changedUpdate(DocumentEvent evt) {
    // rien à faire
  }
  void shiftDroitBornes(BorneUnite[] bornes, int deb, int shift) {
    for (int i = bornes.length - 1; i >= 0; i--) {
      if (bornes[i].pos < deb) {
        return;
      }
      bornes[i].pos += shift;
    }
  }
  void shiftGaucheBornes(BorneUnite[] bornes, int deb, int shift) {
    for (int i = bornes.length - 1; i >= 0; i--) {
      if (bornes[i].pos <= deb) {
        return;
      }
      if (bornes[i].pos < deb + shift) {
        bornes[i].pos = deb;
      } else {
        bornes[i].pos -= shift;
      }
    }
  }
}
