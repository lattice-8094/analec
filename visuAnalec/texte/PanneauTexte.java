/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.texte;

import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.*;
import visuAnalec.PanneauEditeur.*;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;

/**
 *
 * @author Bernard
 */
public class PanneauTexte extends JTextPane {
  final private static Color ROUGE = new Color(0x800000);
  final private static Color BLEU = new Color(0x000080);
  final private static Color JAUNE_CLAIR = new Color(0xffffb0);
  final private static Color JAUNE_TRES_CLAIR = new Color(0xffffe0);
  final private static Color JAUNE_FONCE = new Color(0xffff60);
  final private static Color VERT_CLAIR = new Color(0xd8f0ff);
  final private static Color VERT_FONCE = new Color(0xb0d0ff);
  final private static Color VIOLET_CLAIR = new Color(0xffd8e0);
  final private static Color VIOLET_FONCE = new Color(0xffb0c0);
  final public static SimpleAttributeSet STANDARD_DEFAUT = creerStyleStandardDefaut();
  final protected static AttributeSet COLORATION_1 = creerCouleurFond(JAUNE_CLAIR);
  final protected static AttributeSet COLORATION_1_MULTI = creerCouleurFond(JAUNE_FONCE);
  final protected static AttributeSet COLORATION_2 = creerCouleurFond(VERT_CLAIR);
  final protected static AttributeSet COLORATION_2_MULTI = creerCouleurFond(VERT_FONCE);
  final protected static AttributeSet COLORATION_3 = creerCouleurFond(VIOLET_CLAIR);
  final protected static AttributeSet COLORATION_3_MULTI = creerCouleurFond(VIOLET_FONCE);
  final protected static AttributeSet SELECTION_1_1 = ajouterCouleurCar(COLORATION_1, BLEU);
  final protected static AttributeSet SELECTION_1_1_MULTI = ajouterCouleurCar(COLORATION_1_MULTI,
          BLEU);
  final protected static AttributeSet SELECTION_1_2 = ajouterCouleurCar(COLORATION_1, ROUGE);
  final protected static AttributeSet SELECTION_1_2_MULTI = ajouterCouleurCar(COLORATION_1_MULTI,
          ROUGE);
  final protected static AttributeSet SELECTION_1 = ajouterCouleurCar(COLORATION_1, ROUGE);
  final protected static AttributeSet SELECTION_1_MULTI = ajouterCouleurCar(COLORATION_1_MULTI,
          ROUGE);
  final protected static AttributeSet SELECTION_2 = ajouterCouleurCar(COLORATION_2, BLEU);
  final protected static AttributeSet SELECTION_2_MULTI = ajouterCouleurCar(COLORATION_2_MULTI,
          BLEU);
  final protected static AttributeSet SELECTION_3 = ajouterCouleurCar(COLORATION_3, ROUGE);
  final protected static AttributeSet SELECTION_3_MULTI = ajouterCouleurCar(COLORATION_3_MULTI,
          ROUGE);
  final protected static AttributeSet BORNAGE = ajouterCouleurCar(COLORATION_1, null);
  final private static HashMap<Integer, String> NOM_ALIGNEMENT = creerTableauNomsAlignement();

  private static SimpleAttributeSet creerStyleStandardDefaut() {
    String police;
    if (Arrays.binarySearch(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(),
            "Times New Roman")>=0) police = "Times New Roman";
    else police = "Serif";
    SimpleAttributeSet att = new SimpleAttributeSet();
    StyleConstants.setFontFamily(att, police);
    StyleConstants.setForeground(att, Color.BLACK);
    StyleConstants.setBackground(att, Color.WHITE);
    StyleConstants.setFontSize(att, 14);
    StyleConstants.setBold(att, false);
    StyleConstants.setItalic(att, false);
    StyleConstants.setUnderline(att, false);
    StyleConstants.setAlignment(att, StyleConstants.ALIGN_JUSTIFIED);
    StyleConstants.setFirstLineIndent(att, 20f);
    StyleConstants.setSpaceAbove(att, 4f);
    StyleConstants.setSpaceBelow(att, 0f);
    StyleConstants.setLeftIndent(att, 10f);
    StyleConstants.setRightIndent(att, 10f);
    StyleConstants.setLineSpacing(att, 0.1f);
    return att;
  }

  private static HashMap<Integer, String> creerTableauNomsAlignement() {
    HashMap<Integer, String> tab = new HashMap<Integer, String>();
    tab.put(StyleConstants.ALIGN_CENTER, "Centré");
    tab.put(StyleConstants.ALIGN_JUSTIFIED, "Justifié");
    tab.put(StyleConstants.ALIGN_LEFT, "Gauche");
    tab.put(StyleConstants.ALIGN_RIGHT, "Droite");
    return tab;
  }

  public static String nomAlignement(int code) {
    return NOM_ALIGNEMENT.get(code);
  }

  public static String[] nomsAlignement() {
    String[] noms = NOM_ALIGNEMENT.values().toArray(new String[0]);
    Arrays.sort(noms);
    return noms;
  }

  public static int codeAlignement(String nom) {
    for (int code : NOM_ALIGNEMENT.keySet()) if (nomAlignement(code).equals(nom))
        return code;
    return StyleConstants.ALIGN_JUSTIFIED;
  }

  private static SimpleAttributeSet creerCouleurFond(Color c) {
    SimpleAttributeSet att = new SimpleAttributeSet();
    StyleConstants.setBackground(att, c);
    return att;
  }

  private static SimpleAttributeSet ajouterCouleurCar(AttributeSet att0, Color c) {
    SimpleAttributeSet att = new SimpleAttributeSet(att0);
    StyleConstants.setBold(att, true);
    if (c!=null) StyleConstants.setForeground(att, c);
    return att;
  }

  public static Color getCouleurFond1() {
    return StyleConstants.getBackground(COLORATION_1);
  }

  public static Color getCouleurFond2() {
    return StyleConstants.getBackground(COLORATION_2);
  }

  public static Color getCouleurFond3() {
    return StyleConstants.getBackground(COLORATION_3);
  }

  public enum TypeMarquage {
    FOND1(5, COLORATION_1, COLORATION_1_MULTI),
    FOND1_2(5, COLORATION_1, COLORATION_1_MULTI),
    FOND2(6, COLORATION_2, COLORATION_2_MULTI),
    FOND3(7, COLORATION_3, COLORATION_3_MULTI),
    SELECT1(2, SELECTION_1, SELECTION_1_MULTI),
    SELECT1_2(1, SELECTION_1_1, SELECTION_1_1_MULTI),
    SELECT1_3(2, SELECTION_1_2, SELECTION_1_2_MULTI),
    SELECT2(3, SELECTION_2, SELECTION_2_MULTI),
    SELECT3(4, SELECTION_3, SELECTION_3_MULTI);
    protected AttributeSet attribut;
    protected AttributeSet attributMulti;
    protected int niveau;

    TypeMarquage(int niv, AttributeSet att, AttributeSet attMulti) {
      attribut = att;
      attributMulti = attMulti;
      niveau = niv;
    }
  }

  public static class UniteColoree {
    public Unite unite;
    public AttributeSet att;

    public UniteColoree(Unite unite, AttributeSet att) {
      this.att = att;
      this.unite = unite;
    }
  }

  protected static class Coloriage {
    private int[][] intervalles;
    private AttributeSet[] attributIntervalles;
    private PanneauTexte panneauTexte;

    protected Coloriage(PanneauTexte pTexte) {
      panneauTexte = pTexte;
      intervalles = new int[0][];
      attributIntervalles = new AttributeSet[0];
    }

    private void creerColoriage(UniteColoree[] unitsCol) {
      ArrayList<int[]> intervlles = new ArrayList<int[]>();
      ArrayList<AttributeSet> attIntervalle = new ArrayList<AttributeSet>();
      for (UniteColoree unitCol : unitsCol) {
        int deb = panneauTexte.indexToPosition(unitCol.unite.getDeb());
        int fin = panneauTexte.indexToPosition(unitCol.unite.getFin());
        intervlles.add(new int[]{deb, fin-deb});
        attIntervalle.add(unitCol.att);
      }
      intervalles = intervlles.toArray(new int[0][]);
      attributIntervalles = attIntervalle.toArray(new AttributeSet[0]);
    }
  }

  protected static class Marquage {
    private int[][] intervalles;
    private Unite[][] unitesIntervalles;
    private AttributeSet[] attributIntervalles;
    private HashMap<Unite, ArrayList<TypeMarquage>> typesMarquage =
            new HashMap<Unite, ArrayList<TypeMarquage>>();
    private PanneauTexte panneauTexte;

    protected Marquage(PanneauTexte pTexte) {
      panneauTexte = pTexte;
    }
    // Ordre de masquage décroissant : si une unité est présente dans plusieurs types de marquage, 
    // seule sa première mention sera prise en compte (grâce au HashMap typesMarquage)

    void creerMarquage(TypeMarquage[] typesMarq, EnumMap<TypeMarquage, Unite[]> unites) {
      HashMap<Integer, HashSet<Unite>> debs = new HashMap<Integer, HashSet<Unite>>();
      HashMap<Integer, HashSet<Unite>> fins = new HashMap<Integer, HashSet<Unite>>();
      HashSet<Integer> bornes = new HashSet<Integer>();
      HashMap<Unite, Boolean> multis = new HashMap<Unite, Boolean>();
      typesMarquage.clear();
      for (TypeMarquage typeM : typesMarq)
        for (Unite unit : unites.get(typeM)) {
          if (!typesMarquage.containsKey(unit)) {
            typesMarquage.put(unit, new ArrayList<TypeMarquage>());
            typesMarquage.get(unit).add(typeM);
            multis.put(unit, false);
          } else {
            ArrayList<TypeMarquage> types = typesMarquage.get(unit);
            if (!types.get(types.size()-1).equals(typeM)) types.add(typeM);
            else if (types.size()==1) multis.put(unit, true);
          }
          int deb = panneauTexte.indexToPosition(unit.getDeb());
          int fin = panneauTexte.indexToPosition(unit.getFin());
          bornes.add(deb);
          bornes.add(fin);
          if (!debs.containsKey(deb)) debs.put(deb, new HashSet<Unite>());
          debs.get(deb).add(unit);
          if (!fins.containsKey(fin)) fins.put(fin, new HashSet<Unite>());
          fins.get(fin).add(unit);
        }
      Integer[] brnes = bornes.toArray(new Integer[0]);
      Arrays.sort(brnes);
      ArrayList<Unite> units = new ArrayList<Unite>();
      ArrayList<int[]> intervlles = new ArrayList<int[]>();
      ArrayList<Unite[]> unitsIntervalle = new ArrayList<Unite[]>();
      ArrayList<AttributeSet> attIntervalle = new ArrayList<AttributeSet>();
      int deb = 0;  // début d'un intervalle
      int lg = 0;   // longueur d'un intervalle
      for (int borne : brnes) {
        if (!units.isEmpty()) {
          lg = borne-deb;
          intervlles.add(new int[]{deb, lg});
          unitsIntervalle.add(ordonnerUnites(units));
          attIntervalle.add(choisirAttribut(units, multis));
        }
        if (debs.containsKey(borne)) for (Unite unit : debs.get(borne))
            units.add(unit);
        if (fins.containsKey(borne)) for (Unite unit : fins.get(borne))
            units.remove(unit);
        if (!units.isEmpty()) deb = borne;
      }
      intervalles = intervlles.toArray(new int[0][]);
      unitesIntervalles = unitsIntervalle.toArray(new Unite[0][]);
      attributIntervalles = attIntervalle.toArray(new AttributeSet[0]);
    }

    private Unite[] ordonnerUnites(ArrayList<Unite> units) {
      Unite[] unites = new Unite[units.size()];
      unites[0] = units.get(0);
      for (int i = 1; i<unites.length; i++) {
        if (units.get(i).getFin()-units.get(i).getDeb()>=unites[0].getFin()-unites[0].getDeb())
          unites[i] = units.get(i);
        else {
          unites[i] = unites[0];
          unites[0] = units.get(i);
        }
      }
      return unites;
    }

    private AttributeSet choisirAttribut(ArrayList<Unite> units, HashMap<Unite, Boolean> multis) {
      AttributeSet att;
      if (units.size()==1) {
        att = multis.get(units.get(0)) ? typesMarquage.get(units.get(0)).get(0).attributMulti : typesMarquage.get(units.get(0)).get(0).attribut;
      } else {
        att = typesMarquage.get(units.get(0)).get(0).attributMulti;
        int niv = typesMarquage.get(units.get(0)).get(0).niveau;
        for (int i = 1; i<units.size(); i++) {
          if (typesMarquage.get(units.get(i)).get(0).niveau<niv) {
            att = typesMarquage.get(units.get(i)).get(0).attributMulti;
            niv = typesMarquage.get(units.get(i)).get(0).niveau;
          }
        }
      }
      return att;
    }

    public Unite[] getUnitesVisees(int pos) {   // peut être améliorée par une recherche dichotomique
      for (int i = 0; i<intervalles.length; i++) {
        int[] interv = intervalles[i];
        if (pos<interv[0]) return new Unite[0];
        if (pos<interv[0]+interv[1]) return unitesIntervalles[i];
      }
      return new Unite[0];
    }

    public Unite getUniteVisee(int pos) {   // peut être améliorée par une recherche dichotomique
      Unite[] units = getUnitesVisees(pos);
      if (units.length==0) return null;
      return units[0];
    }

    public TypeMarquage getMarquage(Unite unit) {
      return typesMarquage.get(unit).get(0);
    }

    public ArrayList<TypeMarquage> getMarquages(Unite unit) {
      return typesMarquage.get(unit);
    }
  }
  // Champs de la classe PanneauTexte
  protected SimpleAttributeSet AttributsStandard;
  final static String CAR_FIN_PARAGRAPHE = "[\u00b6] ";
  protected Corpus corpus;
  protected StyledDocument doc;
  protected Coloriage coloriage = null;
  protected ArrayList<Integer> posNewLines = new ArrayList<Integer>();

  public PanneauTexte(Corpus corpus) {
    super();
    this.corpus = corpus;
    AttributsStandard = new SimpleAttributeSet(STANDARD_DEFAUT);
    doc = getStyledDocument();
    setText("");
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  @Override
  public JToolTip createToolTip() {
    JToolTip jt = new JToolTip();
    jt.setBackground(JAUNE_TRES_CLAIR);
    return jt;
  }

  public SimpleAttributeSet getAttributsStandard() {
    return AttributsStandard;
  }

  public void setAttributsStandard(SimpleAttributeSet att) {
    AttributsStandard = att;
  }

//  private int debParag;
//  public void setTexteVisu() {
//    final String texte = corpus.getTexte();
//    posNewLines.clear();
//    debParag = 0;
//    final Integer[] finPars = corpus.getFinParagraphes();
//    final StringBuffer newTexte = new StringBuffer(texte.length()+finPars.length);
//    final GMessages.GProgression progression =
//            new GMessages.GProgression(finPars.length, "paragraphes");
//    Thread traitement = new Thread() {
//      @Override
//      public void run() {
//        for (int i = 0; i<finPars.length; i++) {
//          //           if(progression.isCanceled()) break;
//          newTexte.append(texte.substring(debParag, finPars[i])).append("\n");
//          posNewLines.add(newTexte.length()-1);
//          debParag = finPars[i];
//         progression.setNbTraites(i+1);
//         }
//        setText(new String(newTexte));
//        revalidate();
//        repaint();
//      }
//    };
//    traitement.start();
//  }
//
  public void setTexteVisu() {
    final String texte = corpus.getTexte();
    posNewLines.clear();
    int debParag = 0;
    Integer[] finPars = corpus.getFinParagraphes();
    StringBuffer newTexte = new StringBuffer(texte.length()+finPars.length);
    for (int i = 0; i<finPars.length; i++) {
      //           if(progression.isCanceled()) break;
      newTexte.append(texte.substring(debParag, finPars[i])).append("\n");
      posNewLines.add(newTexte.length()-1);
      debParag = finPars[i];
    }
    setText(new String(newTexte));
    revalidate();
    repaint();
  }

  private int getYHautAff() {
    return ((JViewport) getParent()).getViewPosition().y;
  }

  private void setYHautAff(int y) {
    ((JViewport) getParent()).setViewPosition(new Point(0, y));
  }

  public void initAscenseur() {
    setYHautAff(0);
    setCaretPosition(0);
  }

  public void ajusterAscenseur(PanneauTexte panneau) {
    setYHautAff(panneau.getYHautAff());
    setCurseur(panneau.getCaretPosition());
  }

  public void setCurseur(int pos) {
    int ymax = getYHautAff()+((JViewport) getParent()).getViewSize().height;
    pos = Math.max(pos, viewToModel(new Point(0, getYHautAff())));
    pos = Math.min(pos, viewToModel(new Point(0, ymax)));
    setCaretPosition(pos);
  }

  protected void placerAscenseur(Unite unite) {
    int posdeb = indexToPosition(unite.getDeb());
    placerAscenseur(posdeb, indexToPosition(unite.getFin()));
    setCaretPosition(posdeb);
  }

  protected void placerAscenseur(int posdeb, int posfin) {
    try {
      Rectangle rectdeb = modelToView(posdeb);
      Rectangle rectfin = modelToView(posfin);
      int hligne = rectdeb.height;
      int yhaut = rectdeb.y;
      int ybas = rectfin.y+hligne;
      int y0 = getYHautAff();
      int h = ((JViewport) getParent()).getExtentSize().height;
      if (yhaut<y0||ybas>y0+h) {
        setYHautAff(Math.max(0, modelToView(posdeb).y-h/3));
      }
    } catch (BadLocationException ex) {
      System.out.println("Erreur dans placerAscenseur : "+ex.getLocalizedMessage());
      System.exit(1);
    }
  }

  public int[] getIndSelection() {
    int deb = getSelectionStart();
    int fin = getSelectionEnd();
    if (deb==fin) return null;
    return new int[]{positionToIndex(deb), positionToIndex(fin)};
  }

  protected int indexToPosition(int ind) {
    int nbnl = 0;
    for (int finParag : corpus.getFinParagraphes()) {
      if (ind<finParag) {
        break;
      }
      nbnl++;
    }
    return ind+nbnl;
  }

  protected int positionToIndex(int pos) {
    int nbnl = 0;
    for (int posnl : posNewLines) {
      if (pos<=posnl) {
        break;
      }
      nbnl++;
    }
    return pos-nbnl;
  }

  public void colorierUnites(UniteColoree[] unitsCol) {
    coloriage.creerColoriage(unitsCol);
  }
  // Ordre de masquage décroissant : si une unité est présente dans plusieurs types de marquage, 
  // seule sa première mention sera prise en compte 

  protected void marquerUnites(Marquage marquage, TypeMarquage[] typesMarq, EnumMap<TypeMarquage, Unite[]> unites) {
    doc.setCharacterAttributes(0, getText().length(), AttributsStandard, true);
    doc.setParagraphAttributes(0, getText().length(), AttributsStandard, true);
    if (coloriage!=null&&typesMarq.length==0)
      for (int i = 0; i<coloriage.intervalles.length; i++)
        doc.setCharacterAttributes(
                coloriage.intervalles[i][0], coloriage.intervalles[i][1], coloriage.attributIntervalles[i], false);
    marquage.creerMarquage(typesMarq, unites);
    for (int i = 0; i<marquage.intervalles.length; i++)
      doc.setCharacterAttributes(
              marquage.intervalles[i][0], marquage.intervalles[i][1], marquage.attributIntervalles[i], false);
  }

  protected Unite getUniteVisee(Marquage marquage, Point pt) {
    return marquage.getUniteVisee(viewToModel(pt));
  }

  protected Unite[] getUnitesVisees(Marquage marquage, Point pt) {
    return marquage.getUnitesVisees(viewToModel(pt));
  }

//  public static int[] calculerNoParagraphe(Unite[] unites, Integer[] finPar) {
//    // les unites sont triées
//    // les numéros de paragraphe commencent à 1 
//    int[] nosPar = new int[unites.length];
//    int nopar = 0;
//    for (int i = 0; i<unites.length; i++) {
//      while (unites[i].getDeb()>finPar[nopar]) nopar++;
//      nosPar[i] = nopar+1;
//    }
//    return nosPar;
//  }
//
  public static String insereCarFinParagraphe(String texte, Integer[] finPar, int offset) {
    for (int ind : finPar) {
      ind -= offset;
      if (ind<=0) continue;
      if (ind>=texte.length()) break;
      texte = texte.substring(0, ind)+PanneauTexte.CAR_FIN_PARAGRAPHE+texte.substring(ind);
      offset -= CAR_FIN_PARAGRAPHE.length();
    }
    return texte;
  }

  public String getTexteUnite(Unite unit) {  // utilisé pour les tooltips et les popups : à revoir
    int deb = unit.getDeb();
    int fin = unit.getFin();
    return insereCarFinParagraphe(corpus.getTexte().substring(deb, fin), corpus.getFinParagraphes(), deb);
  }

  protected void setTexteCorpus() {
    String newTexte = "";
    ArrayList<Integer> newFinParag = new ArrayList<Integer>();
    String texte = getText();
    if (posNewLines.isEmpty()||posNewLines.get(posNewLines.size()-1)!=texte.length()-1) {
      posNewLines.add(texte.length());
    }
    int debParag = 0;
    for (int posnl : posNewLines) {
      newTexte += texte.substring(debParag, posnl);
      newFinParag.add(newTexte.length());
      debParag = posnl+1;
    }
    corpus.modifTexte(newTexte, newFinParag);
  }

  public void rechercherTexte(String chaine) {
    getCaret().setSelectionVisible(true);
    String texte = getText();
    int pos = texte.indexOf(chaine, getCaretPosition());
    if (pos<0) pos = texte.indexOf(chaine);
    if (pos>=0) {
      placerAscenseur(pos, pos+chaine.length());
      setCaretPosition(pos);
      moveCaretPosition(pos+chaine.length());
    } else moveCaretPosition(getCaretPosition());
  }
}
