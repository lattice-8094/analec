/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.texte;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import visuAnalec.*;
import visuAnalec.util.GMessages.*;
import visuAnalec.util.GVisu;
import visuAnalec.util.GVisu.*;

/**
 *
 * @author Bernard
 */
public class VisuStyleTexte extends JFrame {
  private static VisuStyleTexte instance = null;
  private PanneauEditeur pEditeur;
  private SimpleAttributeSet att;
  JComboBox saisieTaille;
  JComboBox saisiePolice;
  JCheckBox saisieGras;
  JCheckBox saisieItalique;
  JCheckBox saisieSouligne;
  CouleurComboBox saisieCouleur;
  CouleurComboBox saisieFond;
  JComboBox saisieInterligne;
  JComboBox saisieEspaceAvant;
  JComboBox saisieEspaceApres;
  JComboBox saisieAlignement;
  JComboBox saisieIndentGauche;
  JComboBox saisieIndentDroite;
  JComboBox saisieIndentPrem;
  public static void newVisuStyleTexte(PanneauEditeur pEditeur, SimpleAttributeSet att) {
    if (instance != null) instance.dispose();
    instance = new VisuStyleTexte(pEditeur, att);

  }
  private VisuStyleTexte(PanneauEditeur pEditeur, SimpleAttributeSet att) {
    super("Gestion du style du texte");
    Container fen = pEditeur.getTopLevelAncestor();
    Point pos = new Point(fen.getX() + 200, fen.getY() + 50);
    setLocation(pos);
    this.pEditeur = pEditeur;
    this.att = att;
    JPanel pStyleCaracteres = new JPanel(new GridLayout(0, 2, 2, 4));
    pStyleCaracteres.setBorder(new TitledBorder("Caractères"));
    pStyleCaracteres.add(new JLabel("Police", JLabel.TRAILING));
    String[] polices = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    saisiePolice = new JComboBox(polices);
    saisiePolice.setPrototypeDisplayValue("Times New Roman");
    saisiePolice.setEditable(false);
    saisiePolice.setSelectedItem(StyleConstants.getFontFamily(att));
    saisiePolice.setMaximumSize(new Dimension(40, 100));
    pStyleCaracteres.add(saisiePolice);
    pStyleCaracteres.add(new JLabel("Taille (pts)", JLabel.TRAILING));
    saisieTaille = new JComboBox(new String[]{"4", "8", "10", "12", "14", "16", "20"});
    saisieTaille.setEditable(true);
    saisieTaille.setSelectedItem(Integer.toString(StyleConstants.getFontSize(att)));
    pStyleCaracteres.add(saisieTaille);
    pStyleCaracteres.add(new JLabel("Couleur", JLabel.TRAILING));
    saisieCouleur = new CouleurComboBox(GVisu.getCouleursEtNoirEtBlanc());
    saisieCouleur.setEditable(true);
    saisieCouleur.setSelectedItem(StyleConstants.getForeground(att));
    pStyleCaracteres.add(saisieCouleur);
    pStyleCaracteres.add(new JLabel("Surbrillance", JLabel.TRAILING));
    saisieFond = new CouleurComboBox(GVisu.getCouleursEtNoirEtBlanc());
    saisieFond.setEditable(true);
    saisieFond.setSelectedItem(StyleConstants.getBackground(att));
    pStyleCaracteres.add(saisieFond);
    pStyleCaracteres.add(new JLabel("Caractères gras", JLabel.TRAILING));
    saisieGras = new JCheckBox("", StyleConstants.isBold(att));
    pStyleCaracteres.add(saisieGras);
    pStyleCaracteres.add(new JLabel("italiques", JLabel.TRAILING));
    saisieItalique = new JCheckBox("", StyleConstants.isItalic(att));
   pStyleCaracteres.add(saisieItalique);
    pStyleCaracteres.add(new JLabel("soulignés", JLabel.TRAILING));
    saisieSouligne = new JCheckBox("", StyleConstants.isUnderline(att));
    saisieSouligne.setHorizontalTextPosition(SwingConstants.LEADING);
    pStyleCaracteres.add(saisieSouligne);
    JPanel pStyleParagraphes = new JPanel(new GridLayout(0,2,2,4));
    pStyleParagraphes.setBorder(new TitledBorder("Paragraphes"));
    pStyleParagraphes.add(new JLabel("Alignement", JLabel.TRAILING));
    saisieAlignement = new JComboBox(PanneauTexte.nomsAlignement());
    saisieAlignement.setEditable(false);
    saisieAlignement.setSelectedItem(PanneauTexte.nomAlignement(StyleConstants.getAlignment(att)));
    pStyleParagraphes.add(saisieAlignement);
    pStyleParagraphes.add(new JLabel("Interligne (pts)", JLabel.TRAILING));
    saisieInterligne = new JComboBox(new String[]{"-0.2", "0", "0.2", "0.5", "1.0", "1.5", "2"});
    saisieInterligne.setEditable(true);
    saisieInterligne.setSelectedItem(Float.toString(StyleConstants.getLineSpacing(att)));
    pStyleParagraphes.add(saisieInterligne);
    pStyleParagraphes.add(new JLabel("Espacement avant (pts)", JLabel.TRAILING));
    saisieEspaceAvant = new JComboBox(new String[]{"0.0", "2.0", "5.0", "10.0", "15.0", "20.0"});
    saisieEspaceAvant.setEditable(true);
    saisieEspaceAvant.setSelectedItem(Float.toString(StyleConstants.getSpaceAbove(att)));
    pStyleParagraphes.add(saisieEspaceAvant);
    pStyleParagraphes.add(new JLabel("après (pts)", JLabel.TRAILING));
    saisieEspaceApres = new JComboBox(new String[]{"0.0", "2.0", "5.0", "10.0", "15.0", "20.0"});
    saisieEspaceApres.setEditable(true);
    saisieEspaceApres.setSelectedItem(Float.toString(StyleConstants.getSpaceBelow(att)));
    pStyleParagraphes.add(saisieEspaceApres);
    pStyleParagraphes.add(new JLabel("Retrait gauche (pts)", JLabel.TRAILING));
    saisieIndentGauche = new JComboBox(new String[]{"0.0", "2.0", "5.0", "10.0", "15.0", "20.0"});
    saisieIndentGauche.setEditable(true);
    saisieIndentGauche.setSelectedItem(Float.toString(StyleConstants.getLeftIndent(att)));
    pStyleParagraphes.add(saisieIndentGauche);
    pStyleParagraphes.add(new JLabel("droit (pts)", JLabel.TRAILING));
    saisieIndentDroite = new JComboBox(new String[]{"0.0", "2.0", "5.0", "10.0", "15.0", "20.0"});
    saisieIndentDroite.setEditable(true);
    saisieIndentDroite.setSelectedItem(Float.toString(StyleConstants.getRightIndent(att)));
    pStyleParagraphes.add(saisieIndentDroite);
    pStyleParagraphes.add(new JLabel("1ère ligne (pts)", JLabel.TRAILING));
    saisieIndentPrem = new JComboBox(new String[]{"0.0", "2.0", "5.0", "10.0", "15.0", "20.0"});
    saisieIndentPrem.setEditable(true);
    saisieIndentPrem.setSelectedItem(Float.toString(StyleConstants.getFirstLineIndent(att)));
    pStyleParagraphes.add(saisieIndentPrem);
   Box pSaisieItems = Box.createHorizontalBox();
   pSaisieItems.add(pStyleCaracteres);
    pSaisieItems.add(Box.createHorizontalGlue());
    pSaisieItems.add(pStyleParagraphes);
   JPanel  pCommandesValidation = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton valideModifs = new JButton(
            new GAction("Valider") {
              public void executer() {
                validerModifs();
              }
              ;
            });
    JButton annuleModifs = new JButton(
            new GAction("Annuler") {
              public void executer() {
                dispose();
              }
              ;
            });
    pCommandesValidation.add(valideModifs);
    pCommandesValidation.add(annuleModifs);
    // cadre global de la fenêtre
    JPanel cadre = new JPanel(new BorderLayout());
    cadre.add(pStyleCaracteres, BorderLayout.WEST);
   cadre.add(pStyleParagraphes, BorderLayout.EAST);
    cadre.add(pCommandesValidation, BorderLayout.SOUTH);
    setContentPane(cadre);
    pack();
    setVisible(true);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
      }
    });
  }
  private void validerModifs() {
    int entierSaisi;
    float decimalSaisi;
    StyleConstants.setFontFamily(att, (String) saisiePolice.getSelectedItem());
    StyleConstants.setForeground(att, (Color) saisieCouleur.getSelectedItem());
    StyleConstants.setBackground(att, (Color) saisieFond.getSelectedItem());
    try {
      entierSaisi = Integer.parseInt((String) saisieTaille.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      entierSaisi = StyleConstants.getFontSize(att);
    }
    StyleConstants.setFontSize(att, entierSaisi);
    StyleConstants.setBold(att, (boolean) saisieGras.isSelected());
    StyleConstants.setItalic(att, (boolean) saisieItalique.isSelected());
    StyleConstants.setUnderline(att, (boolean) saisieSouligne.isSelected());
    StyleConstants.setAlignment(att, PanneauTexte.codeAlignement((String) saisieAlignement.getSelectedItem()));
    try {
      decimalSaisi = Float.parseFloat((String) saisieEspaceAvant.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      decimalSaisi = StyleConstants.getSpaceAbove(att);
    }
    StyleConstants.setSpaceAbove(att, decimalSaisi);
    try {
      decimalSaisi = Float.parseFloat((String) saisieEspaceApres.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      decimalSaisi = StyleConstants.getSpaceBelow(att);
    }
    StyleConstants.setSpaceBelow(att, decimalSaisi);
    try {
      decimalSaisi = Float.parseFloat((String) saisieIndentGauche.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      decimalSaisi = StyleConstants.getLeftIndent(att);
    }
    StyleConstants.setLeftIndent(att, decimalSaisi);
    try {
      decimalSaisi = Float.parseFloat((String) saisieIndentDroite.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      decimalSaisi = StyleConstants.getRightIndent(att);
    }
    StyleConstants.setRightIndent(att, decimalSaisi);
    try {
      decimalSaisi = Float.parseFloat((String) saisieIndentPrem.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      decimalSaisi = StyleConstants.getFirstLineIndent(att);
    }
    StyleConstants.setFirstLineIndent(att, decimalSaisi);
    try {
      decimalSaisi = Float.parseFloat((String) saisieInterligne.getSelectedItem());
    } catch (NumberFormatException numberFormatException) {
      decimalSaisi = (Float) StyleConstants.getLineSpacing(att);
    }
    StyleConstants.setLineSpacing(att, decimalSaisi);
    pEditeur.changerStyleTexte(att);
    dispose();
  }
}