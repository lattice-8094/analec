/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import visuAnalec.donnees.*;
import visuAnalec.elements.*;
import visuAnalec.util.*;
import visuAnalec.util.Fichiers.*;
import visuAnalec.util.GMessages.*;

/**
 *
 * @author Bernard
 */
public abstract class FichiersTEI {
  public static void convertirCorpusTEI(final Corpus corpus) {
    final File ficTEI;
    final File repJava;
    if ((ficTEI = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier corpus TEI", TypeFichier.TEI))==null)
      return;
    if ((repJava = Fichiers.choisirRepertoire("Choix du répertoire Java"))==null)
      return;
    if (!repJava.exists()) {
      if (!GMessages.confirmationAction("Le répertoire spécifié n'existe pas."
              +" Voulez-vous le créer ?")) return;
      if (!repJava.mkdir()) {
        GMessages.impossibiliteAction("Echec de la création du répertoire");
        return;
      }
    }
    convertir(ficTEI, repJava, corpus);
  }
  private static void convertir(File ficTEI, File repJava, Corpus corpus) {
    BufferedInputStream tei = null;
    String nomTEI = Fichiers.extraireNom(ficTEI)+"-";
    if (Fichiers.existeDebutFichier(repJava, TypeFichier.DOC_ANALEC, nomTEI))
      if (!GMessages.confirmationAction("Attention! Certains fichiers du "
              +"répertoire "+repJava.getName()
              +" risquent d'être écrasés")) return;
    try {
      tei = new BufferedInputStream(
              new ProgressMonitorInputStream(
              null, "Conversion du corpus TEI "+nomTEI,
              new FileInputStream(ficTEI)));
      SAXParserFactory pf = (SAXParserFactory) SAXParserFactory.newInstance();
      SAXParser sp = pf.newSAXParser();
      sp.parse(tei, new TEIHandler(corpus, repJava, nomTEI));
    } catch (IOException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ParserConfigurationException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de format de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (SAXException ex) {
      corpus.fermer();
      JOptionPane.showMessageDialog(null, ex, "Erreur de format de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (tei!=null) tei.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture du fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  static class TEIHandler extends DefaultHandler {
    File repJava;
    String nomTEI;
    Corpus corpus;
    int noDoc;
    String texte;
    ArrayList<Integer> finParagraphes = new ArrayList<Integer>();
    Balise etatCourant;
    String typeCourant;
    HashMap<String, Integer> ancres = new HashMap<String, Integer>();
    HashMap<Unite, String[]> posUnites = new HashMap<Unite, String[]>();
    HashMap<String, Element> idElements = new HashMap<String, Element>();
    HashMap<Relation, String[]> eltsRelations = new HashMap<Relation, String[]>();
    HashMap<Schema, String[]> eltsSchemas = new HashMap<Schema, String[]>();
    HashMap<Element, String> propsElements = new HashMap<Element, String>();
    HashMap<String, HashMap<String, String>> valsprops =
            new HashMap<String, HashMap<String, String>>();
    String idPropsEltCourant;
    String champCourant;
    String valCourant;

    enum Balise {
      TEICORPUS, TEIHEADER, TEIHEADER_CORPUS, TEI, TEXT, BODY, P, ANCHOR,
      BACK, SPANGRP, SPAN, JOINGRP, JOIN, JOINGRP_REL, JOIN_REL,
      JOINGRP_SCH, JOIN_SCH, FVLIB, FS, F, STRING, DEBUT_DOC, FIN_DOC,
      AUTRE
    };
    static Balise balise(String nomBalise) {
      try {
        return Balise.valueOf(nomBalise.replaceAll("\\W", "").toUpperCase());
      } catch (IllegalArgumentException e) {
        return Balise.AUTRE;
      }
    }
    TEIHandler(Corpus corpus, File repJava, String nomTEI) {
      super();
      this.corpus = corpus;
      this.repJava = repJava;
      this.nomTEI = nomTEI;
    }
    @Override
    public void startDocument() {
      noDoc = 0;
      etatCourant = Balise.DEBUT_DOC;
    }
    @Override
    public void endDocument() throws SAXNotSupportedException {
      if (etatCourant!=Balise.FIN_DOC)
        throw new SAXNotSupportedException("Fin de fichier inattendue");
    }
    @Override
    public void startElement(String uri, String local, String nomBalise,
            Attributes attributs) throws SAXException {
      switch (balise(nomBalise)) {
        case TEICORPUS:
          if (etatCourant==Balise.DEBUT_DOC) etatCourant = Balise.TEICORPUS;
          else
            throw new SAXNotSupportedException("Balise <teiCorpus> mal placée");
          break;
        case TEIHEADER:
          if (etatCourant==Balise.TEICORPUS)
            etatCourant = Balise.TEIHEADER_CORPUS;
          else if (etatCourant==Balise.TEI) etatCourant = Balise.TEIHEADER;
          else
            throw new SAXNotSupportedException("Balise <teiHeader> mal placée");

          break;
        case TEI:
          if (etatCourant==Balise.TEICORPUS) {
            noDoc++;
            initCorpus();
            etatCourant = Balise.TEI;
          } else
            throw new SAXNotSupportedException("Balise <tei> mal placée");
          break;
        case TEXT:
          if (etatCourant==Balise.TEI) {
            etatCourant = Balise.TEXT;
          } else
            throw new SAXNotSupportedException("Balise <text> mal placée");
          break;
        case BODY:
          if (etatCourant==Balise.TEXT) {
            etatCourant = Balise.BODY;
          } else
            throw new SAXNotSupportedException("Balise <body> mal placée");
          break;
        case P:
          if (etatCourant==Balise.BODY) {
            etatCourant = Balise.P;
          } else
            throw new SAXNotSupportedException("Balise <p> mal placée");
          break;
        case ANCHOR:
          if ("AnalecDelimiter".equals(attributs.getValue("type"))) {
            if (etatCourant==Balise.BODY||etatCourant==Balise.P) {
              ancres.put(attributs.getValue("xml:id"), texte.length());
            } else
              throw new SAXNotSupportedException("Balise <anchor> de type "
                      +"\"AnalecDelimiter\" mal placée");
          }
          break;
        case BACK:
          if (etatCourant==Balise.TEXT) {
            etatCourant = Balise.BACK;
          } else
            throw new SAXNotSupportedException("Balise <back> mal placée");
          break;
        case SPANGRP:
          if ("AnalecUnit".equals(attributs.getValue("type"))) {
            if (etatCourant==Balise.BACK) {
              typeCourant = decodeId(attributs.getValue("n"));
              if (typeCourant.isEmpty())
                throw new SAXNotSupportedException("Attribut \"n\" d'une balise "
                        +"<spanGrp> de type \"AnalecUnit\" manquant");
              etatCourant = Balise.SPANGRP;
            } else
              throw new SAXNotSupportedException("Balise <spanGrp> "
                      +"de type \"AnalecUnit\" mal placée");
          }
          break;
        case SPAN:
          if (etatCourant==Balise.SPANGRP) {
            Unite unit = new Unite();
            unit.setType(typeCourant);
            posUnites.put(unit, new String[]{
                      xmlId(attributs.getValue("from")),
                      xmlId(attributs.getValue("to"))});
            idElements.put(attributs.getValue("xml:id"), unit);
            propsElements.put(unit, xmlId(attributs.getValue("ana")));
            etatCourant = Balise.SPAN;
          }
          break;
        case JOINGRP:
          if ("AnalecRelation".equals(attributs.getValue("type"))) {
            if (etatCourant==Balise.BACK) {
              typeCourant = decodeId(attributs.getValue("n"));
              if (typeCourant.isEmpty())
                throw new SAXNotSupportedException("Attribut \"n\" d'une balise "
                        +"<joinGrp> de type \"AnalecRelation\" manquant");
              etatCourant = Balise.JOINGRP_REL;
            } else
              throw new SAXNotSupportedException("Balise <joinGrp> "
                      +"de type \"AnalecRelation\" mal placée");
          } else if ("AnalecSchema".equals(attributs.getValue("type"))) {
            if (etatCourant==Balise.BACK) {
              typeCourant = decodeId(attributs.getValue("n"));
              if (typeCourant.isEmpty())
                throw new SAXNotSupportedException("Attribut \"n\" d'une balise "
                        +"<joinGrp> de type \"AnalecSchema\" manquant");
              etatCourant = Balise.JOINGRP_SCH;
            } else
              throw new SAXNotSupportedException("Balise <joinGrp> "
                      +"de type \"AnalecSchema\" mal placée");
          }

          break;
        case JOIN:
          if (etatCourant==Balise.JOINGRP_REL) {
            Relation rel = new Relation();
            rel.setType(typeCourant);
            eltsRelations.put(rel, splitIds(attributs.getValue("target")));
            idElements.put(attributs.getValue("xml:id"), rel);
            propsElements.put(rel, xmlId(attributs.getValue("ana")));
            etatCourant = Balise.JOIN_REL;
          } else if (etatCourant==Balise.JOINGRP_SCH) {
            Schema sch = new Schema();
            sch.setType(typeCourant);
            eltsSchemas.put(sch, splitIds(attributs.getValue("target")));
            idElements.put(attributs.getValue("xml:id"), sch);
            propsElements.put(sch, xmlId(attributs.getValue("ana")));
            etatCourant = Balise.JOIN_SCH;
          }
          break;
        case FVLIB:
          if ("AnalecElementProperties".equals(attributs.getValue("n"))) {
            if (etatCourant==Balise.BACK) {
              etatCourant = Balise.FVLIB;
            } else
              throw new SAXNotSupportedException("Balise <fvLib> "
                      +"de type \"AnalecElementProperties\" mal placée");
          }
          break;
        case FS:
          if (etatCourant==Balise.FVLIB) {
            idPropsEltCourant = attributs.getValue("xml:id");
            valsprops.put(idPropsEltCourant, new HashMap<String, String>());
            etatCourant = Balise.FS;
          }
          break;
        case F:
          if (etatCourant==Balise.FS) {
            champCourant = decodeId(attributs.getValue("name"));
            if (champCourant.isEmpty())
              throw new SAXNotSupportedException("Attribut \"name\" "
                      + "d'une balise <f> dans le <fvLib> de type"
                      + " \"AnalecElementProperties\" manquant");

            etatCourant = Balise.F;
          }
          break;
        case STRING:
          if (etatCourant==Balise.F) {
            valCourant = "";
            etatCourant = Balise.STRING;
          }
          break;
      }
    }
    private String xmlId(String id) {
      if (id==null||id.isEmpty()||id.charAt(0)!='#') return null;
      return id.substring(1);
    }
    private String[] splitIds(String ids) {
      if(ids.isEmpty()) return new String[0];
      String[] idsplit = ids.split("\\s+");
      for (int i = 0; i<idsplit.length; i++) {
        idsplit[i] = xmlId(idsplit[i]);
      }
      return idsplit;
    }
    @Override
    public void endElement(String uri, String local, String nomBalise) throws SAXException {
      String type = "";
      switch (balise(nomBalise)) {
        case TEICORPUS:
          if (etatCourant==Balise.TEICORPUS) etatCourant = Balise.FIN_DOC;
          else
            throw new SAXNotSupportedException("Balise </teiCorpus> mal placée");
          break;
        case TEIHEADER:
          if (etatCourant==Balise.TEIHEADER_CORPUS)
            etatCourant = Balise.TEICORPUS;
          else if (etatCourant==Balise.TEIHEADER) etatCourant = Balise.TEI;
          else
            throw new SAXNotSupportedException("Balise </teiHeader> mal placée");
          break;
        case TEI:
          if (etatCourant==Balise.TEI) {
            remplirCorpus();
            if (!ecrireFichier()) throw new SAXException("Erreur d'écriture "
                      +"du fichier "+nomTEI+noDoc+".ec");
            etatCourant = Balise.TEICORPUS;
          } else
            throw new SAXNotSupportedException("Balise </tei> mal placée");
          break;
        case TEXT:
          if (etatCourant==Balise.TEXT) etatCourant = Balise.TEI;
          else
            throw new SAXNotSupportedException("Balise </text> mal placée");
          break;
        case BODY:
          if (etatCourant==Balise.BODY) {
            corpus.setTexte(texte);
            corpus.setFinParagraphes(finParagraphes);
            etatCourant = Balise.TEXT;
          } else
            throw new SAXNotSupportedException("Balise </body> mal placée");
          break;
        case P:
          if (etatCourant==Balise.P) {
            finParagraphes.add(texte.length());
            etatCourant = Balise.BODY;
          }
          break;
        case BACK:
          if (etatCourant==Balise.BACK) etatCourant = Balise.TEXT;
          else
            throw new SAXNotSupportedException("Balise </back> mal placée");
          break;
        case SPANGRP:
          if (etatCourant==Balise.SPANGRP) etatCourant = Balise.BACK;
          break;
        case SPAN:
          if (etatCourant==Balise.SPAN) etatCourant = Balise.SPANGRP;
          break;
        case JOINGRP:
          if (etatCourant==Balise.JOINGRP_REL||etatCourant==Balise.JOINGRP_SCH)
            etatCourant = Balise.BACK;
          break;
        case JOIN:
          if (etatCourant==Balise.JOIN_REL) etatCourant = Balise.JOINGRP_REL;
          else if (etatCourant==Balise.JOIN_SCH)
            etatCourant = Balise.JOINGRP_SCH;
          break;
        case FVLIB:
          if (etatCourant==Balise.FVLIB) etatCourant = Balise.BACK;
          break;
        case FS:
          if (etatCourant==Balise.FS) etatCourant = Balise.FVLIB;
          break;
        case F:
          if (etatCourant==Balise.F) etatCourant = Balise.FS;
          break;
        case STRING:
          if (etatCourant==Balise.STRING) {
            valsprops.get(idPropsEltCourant).put(champCourant, valCourant);
            etatCourant = Balise.F;
          }
          break;
      }
    }
    @Override
    public void characters(char[] ch, int start, int taille) throws SAXException {
      if (etatCourant==Balise.P)
        texte += decodeTexte(new String(ch, start, taille));
      if (etatCourant==Balise.STRING)
        valCourant += decodeTexte(new String(ch, start, taille));
    }
    private static String decodeTexte(String ch) {
      return ch.replaceAll("&amp;", "&").replaceAll("&lt;", "<").
              replaceAll("&gt;", ">").replaceAll("&quot;", "\"").
              replaceAll("&apos;", "\'").replaceAll("[\\t\\n\\r\\f]", "");
    }
    private static String decodeId(String ch) {
      if (ch==null||ch.isEmpty()) return "";
      String id = ch.charAt(0)=='_' ? ch.substring(1) : ch;
      return id.replaceAll("_", " ");
    }
    private void initCorpus() {
      corpus.clearAll();
      texte = "";
      finParagraphes.clear();
      ancres.clear();
      posUnites.clear();
      eltsRelations.clear();
      eltsSchemas.clear();
      idElements.clear();
      propsElements.clear();
      valsprops.clear();
    }
    private void remplirCorpus() throws SAXNotSupportedException {
      for (Unite unit : posUnites.keySet()) {
        String[] pos = posUnites.get(unit);
        if (pos[0]==null||!ancres.containsKey(pos[0])
                ||pos[1]==null||!ancres.containsKey(pos[1]))
          throw new SAXNotSupportedException("Pas trouvé d'ancrage pour "
                  +"une unité de type "+unit.getType());
        unit.setDeb(ancres.get(pos[0]));
        unit.setFin(ancres.get(pos[1]));
        String idProps = propsElements.get(unit);
        if (idProps==null||!valsprops.containsKey(idProps))
          throw new SAXNotSupportedException("Pas trouvé de propriétés pour "
                  +"une unité de type "+unit.getType());
        HashMap<String, String> props = valsprops.get(idProps);
        for (String champ : props.keySet())
          unit.putProp(champ, props.get(champ));
        corpus.addUniteLue(unit);
      }
      for (Relation rel : eltsRelations.keySet()) {
        String[] idElts = eltsRelations.get(rel);
        if (idElts.length!=2) {
          throw new SAXNotSupportedException("Une relation de type "
                  +rel.getType()+" n'a pas deux termes");
        }
        if (idElts[0]==null||!idElements.containsKey(idElts[0])
                ||idElts[1]==null||!idElements.containsKey(idElts[1])) {
          throw new SAXNotSupportedException("Errreur d'identification de termes "
                  +" pour une relation de type "+rel.getType());
        }
        rel.setElt1(idElements.get(idElts[0]));
        rel.setElt2(idElements.get(idElts[1]));
        String idProps = propsElements.get(rel);
        if (idProps==null||!valsprops.containsKey(idProps))
          throw new SAXNotSupportedException("Pas trouvé de propriétés pour "
                  +"une relation de type "+rel.getType());
        HashMap<String, String> props = valsprops.get(idProps);
        for (String champ : props.keySet())
          rel.putProp(champ, props.get(champ));
        corpus.addRelationLue(rel);
      }
      for (Schema sch : eltsSchemas.keySet()) {
        String[] idElts = eltsSchemas.get(sch);
        for (String idElt : idElts) {
          if (idElt==null||!idElements.containsKey(idElt)) {
            throw new SAXNotSupportedException("Errreur d'identification de termes "
                    +" pour un schema de type "+sch.getType());
          }
          sch.ajouter(idElements.get(idElt));
        }
        String idProps = propsElements.get(sch);
        if (idProps==null||!valsprops.containsKey(idProps))
          throw new SAXNotSupportedException("Pas trouvé de propriétés pour "
                  +"un schéma de type "+sch.getType());
        HashMap<String, String> props = valsprops.get(idProps);
        for (String champ : props.keySet())
          sch.putProp(champ, props.get(champ));
        corpus.addSchemaLu(sch);
      }
    }
    private boolean ecrireFichier() {
      return FichiersJava.enregistrerCorpus(corpus, Fichiers.creerFichier(
              repJava, TypeFichier.DOC_ANALEC, nomTEI+noDoc));
    }
  }
  public final static String TYPE_INFOS_META_DOCUMENTS = "Titre";
  public final static String CHAMP_NON_RENSEIGNE = "non renseigné";
  public final static String DEFAUT_TITRE_CORPUS = "";
  public final static String DEFAUT_ANNOTATEURS = "";
  public final static String DEFAUT_EDITEUR = "";
  public final static String DEFAUT_SOURCE_CORPUS = "";
  public static void exporterCorpusTEI(final Corpus corpus) {
    File repJava;
    final File ficTEI;
    final File[] fics = Fichiers.choisirCorpus("Choix du répertoire Java",
            TypeFichier.CORPUS_ANALEC);
    if (fics.length==0) return;
    final HashMap<String, String> infosCorpus = new HashMap<String, String>();
    saisirInfosCorpus(infosCorpus);
    if ((ficTEI = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier TEI", Fichiers.TypeFichier.TEI))==null) return;
    final PrintWriter teiWriter;
    try {
      teiWriter = new PrintWriter(ficTEI, "UTF-8");
    } catch (FileNotFoundException ex) {
      GMessages.impossibiliteAction("Pas de fichier TEI ? ");

      return;
    } catch (UnsupportedEncodingException ex) {
      GMessages.impossibiliteAction("Pas de codage UTF8 ?");
      return;
    }
    final TEIPrinter teiPrinter = new TEIPrinter(corpus, teiWriter);
    final GMessages.GProgression progression =
            new GMessages.GProgression(fics.length, "Conversion", "fichiers");
    Thread traitement = new Thread() {
      @Override
      public void run() {
        try {
          teiPrinter.ecrireEnteteCorpus(infosCorpus);
          for (int i = 0; i<fics.length; i++) {
            if (progression.isCanceled()) break;
            progression.setNbTraites(i+1);
            File ficJava = fics[i];
            if (FichiersJava.ouvrirCorpus(corpus, ficJava, true))
              teiPrinter.ecrireDocument(Fichiers.extraireNom(ficJava));
          }
          corpus.fermer();
          teiPrinter.ecrireFinCorpus();
          teiWriter.close();
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    };
    traitement.start();
  }
  private static boolean saisirInfosCorpus(HashMap<String, String> infosCorpus) {
    final boolean[] pvalide = new boolean[1];
    SaisieInfos fenetre = new SaisieInfos(pvalide, infosCorpus);
    return pvalide[0];
  }

  private static class SaisieInfos extends JDialog {
    private SaisieInfos(final boolean[] pvalide,
            final HashMap<String, String> infosCorpus) {
      super((JFrame) null, "Informations sur le corpus", true);
      JPanel cadre = new JPanel(new BorderLayout());
      setContentPane(cadre);
      Rectangle rmax = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
      int largeur = Math.min(rmax.width-100, 600);
      int hauteur = Math.min(rmax.height-100, 400);
      Point locFenetre = new Point((rmax.x+rmax.width)/2-largeur/2,
              (rmax.y+rmax.height)/2-hauteur/2);
      Dimension dimFenetre = new Dimension(largeur, hauteur);
      setLocation(locFenetre);
      setSize(dimFenetre);
      setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      int hauteurCars = cadre.getFontMetrics(cadre.getFont()).getHeight()+5;
      final JTextField champTitre = new JTextField(40);
      champTitre.setMaximumSize(new Dimension(500, hauteurCars));
      champTitre.setText(DEFAUT_TITRE_CORPUS);
      final JTextField champAnnot = new JTextField(40);
      champAnnot.setMaximumSize(new Dimension(500, hauteurCars));
      champAnnot.setText(DEFAUT_ANNOTATEURS);
      final JTextField champEditeur = new JTextField(40);
      champEditeur.setMaximumSize(new Dimension(500, hauteurCars));
      champEditeur.setText(DEFAUT_EDITEUR);
      final JTextField champSource = new JTextField(40);
      champSource.setMaximumSize(new Dimension(500, hauteurCars));
      champSource.setText(DEFAUT_SOURCE_CORPUS);
      Box box = Box.createVerticalBox();
      box.add(Box.createVerticalGlue());
      Box boxTitre = Box.createHorizontalBox();
      boxTitre.add(Box.createHorizontalGlue());
      boxTitre.add(new JLabel("Titre du corpus"));
      boxTitre.add(Box.createHorizontalStrut(5));
      boxTitre.add(champTitre);
      boxTitre.add(Box.createHorizontalGlue());
      box.add(boxTitre);
      box.add(Box.createVerticalGlue());
      Box boxAnnot = Box.createHorizontalBox();
      boxAnnot.add(Box.createHorizontalGlue());
      boxAnnot.add(new JLabel("Annotateurs"));
      boxAnnot.add(Box.createHorizontalStrut(5));
      boxAnnot.add(champAnnot);
      boxAnnot.add(Box.createHorizontalGlue());
      box.add(boxAnnot);
      box.add(Box.createVerticalGlue());
      Box boxEditeur = Box.createHorizontalBox();
      boxEditeur.add(Box.createHorizontalGlue());
      boxEditeur.add(new JLabel("Editeur"));
      boxEditeur.add(Box.createHorizontalStrut(5));
      boxEditeur.add(champEditeur);
      boxEditeur.add(Box.createHorizontalGlue());
      box.add(boxEditeur);
      box.add(Box.createVerticalGlue());
      Box boxSource = Box.createHorizontalBox();
      boxSource.add(Box.createHorizontalGlue());
      boxSource.add(new JLabel("Description du corpus"));
      boxSource.add(Box.createHorizontalStrut(5));
      boxSource.add(champSource);
      boxSource.add(Box.createHorizontalGlue());
      box.add(boxSource);
      box.add(Box.createVerticalGlue());
      add(box);
      JButton valideModifs = new JButton(
              new GAction("Valider") {
                @Override
                public void executer() {
                  pvalide[0] = true;
                  infosCorpus.put("Titre", champTitre.getText());
                  infosCorpus.put("Annot", champAnnot.getText());
                  infosCorpus.put("Editeur", champEditeur.getText());
                  infosCorpus.put("Source", champSource.getText());
                  dispose();
                }
              ;
      });
    JButton annuleModifs = new JButton(
              new GAction("Annuler") {
                @Override
                public void executer() {
                  pvalide[0] = false;
                  dispose();
                }
              ;
      });
     Box boutons = Box.createHorizontalBox();
      boutons.add(Box.createGlue());
      boutons.add(valideModifs, Box.CENTER_ALIGNMENT);
      boutons.add(Box.createHorizontalStrut(5));
      boutons.add(annuleModifs, Box.CENTER_ALIGNMENT);
      boutons.add(Box.createGlue());
      add(boutons, BorderLayout.SOUTH);
      setVisible(
              true);

    }
  }

  static private class TEIPrinter {
    static private class Delimiteur implements Comparable<Delimiteur> {
      private String id;
      private int taille; // signé : négatif pour les délimiteurs de fin
      private Delimiteur(String id, int taille) {
        this.id = id;
        this.taille = taille;
      }
      public int compareTo(Delimiteur d) {
        if (taille!=d.taille) {
          if (taille<0&&d.taille<0||taille>0&&d.taille>0)
            return d.taille-taille;
          else return taille-d.taille;
        } else return taille>0 ? -d.id.compareTo(id) : d.id.compareTo(id);
      }
    }
    private Corpus corpus;
    private PrintWriter writer;
    private String titreCorpus, annot, editeur, sourceCorpus;
    private HashMap<String, ArrayList<Unite>> unites =
            new HashMap<String, ArrayList<Unite>>();
    private HashMap<String, ArrayList<Relation>> relations =
            new HashMap<String, ArrayList<Relation>>();
    private HashMap<String, ArrayList<Schema>> schemas =
            new HashMap<String, ArrayList<Schema>>();
    private HashMap<Element, String> ids = new HashMap<Element, String>();
    private HashMap<String, String> encodeName = new HashMap<String, String>();
    private HashMap<Integer, ArrayList<Delimiteur>> delimiteurs =
            new HashMap<Integer, ArrayList<Delimiteur>>();
    TEIPrinter(Corpus corpus, PrintWriter writer) {
      this.corpus = corpus;
      this.writer = writer;
    }
    private void ecrireEnteteCorpus(HashMap<String, String> infosCorpus) {
      titreCorpus = traiterSaisie(infosCorpus.get("Titre"));
      annot = traiterSaisie(infosCorpus.get("Annot"));
      editeur = traiterSaisie(infosCorpus.get("Editeur"));
      sourceCorpus = traiterSaisie(infosCorpus.get("Source"));
      writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
      writer.println("<teiCorpus xmlns=\"http://www.tei-c.org/ns/1.0\">");
      writer.println(" <teiHeader>");
      writer.println("<fileDesc>");
      writer.println("<titleStmt>");
      writer.println("<title>"+titreCorpus+"</title>");
      writer.println("<respStmt>");
      writer.println("<resp>annotated by</resp>");
      writer.println("<name>"+annot+"</name>");
      writer.println("</respStmt>");
      writer.println("</titleStmt>");
      writer.println("<publicationStmt>");
      writer.println("<publisher>"+editeur+"</publisher>");
      writer.println("</publicationStmt>");
      writer.println("<sourceDesc>");
      writer.println("<bibl>"+sourceCorpus+"</bibl>");
      writer.println("</sourceDesc>");
      writer.println("</fileDesc>");
      writer.println("</teiHeader>");
    }
    private void ecrireFinCorpus() {
      writer.println("</teiCorpus>");
    }
    private void preparerDonnees() {
      unites.clear();
      relations.clear();
      schemas.clear();
      ids.clear();
      encodeName.clear();
      delimiteurs.clear();
      String codeType = "";
      for (String type : corpus.getStructure().getTypes(Unite.class)) {
        Unite[] units = corpus.getUnites(type).toArray(new Unite[0]);
        Arrays.sort(units);
        if (units.length>0) {
          unites.put(type, new ArrayList<Unite>());
          codeType = encodeId(type);
          encodeName.put(type, codeType);
          for (String prop : corpus.getStructure().getNomsProps(Unite.class, type))
            encodeName.put(prop, encodeId(prop));
        }
        for (int no = 0; no<units.length; no++) {
          Unite unit = units[no];
          String id = "u-"+codeType+"-"+(no+1);
          ids.put(unit, id);
          unites.get(type).add(unit);
          int taille = unit.getFin()-unit.getDeb();
          Delimiteur del = new Delimiteur(id, taille);
          addDel(unit.getDeb(), del);
          if (taille>0) {
            del = new Delimiteur(id, -taille);
            addDel(unit.getFin(), del);
          }
        }
      }
      for (String type : corpus.getStructure().getTypes(Relation.class)) {
        Relation[] rels = corpus.getRelations(type).toArray(new Relation[0]);
        Arrays.sort(rels);
        if (rels.length>0) {
          relations.put(type, new ArrayList<Relation>());
          codeType = encodeId(type);
          encodeName.put(type, codeType);
          for (String prop : corpus.getStructure().getNomsProps(Relation.class, type))
            encodeName.put(prop, encodeId(prop));

        }
        for (int no = 0; no<rels.length; no++) {
          Relation rel = rels[no];
          String id = "r-"+codeType+"-"+(no+1);
          ids.put(rel, id);
          relations.get(type).add(rel);
        }
      }
      for (String type : corpus.getStructure().getTypes(Schema.class)) {
        Schema[] schs = corpus.getSchemas(type).toArray(new Schema[0]);
        Arrays.sort(schs);
        if (schs.length>0) {
          schemas.put(type, new ArrayList<Schema>());
          codeType = encodeId(type);
          encodeName.put(type, codeType);
          for (String prop : corpus.getStructure().getNomsProps(Schema.class, type))
            encodeName.put(prop, encodeId(prop));
        }
        for (int no = 0; no<schs.length; no++) {
          Schema sch = schs[no];
          String id = "s-"+codeType+"-"+(no+1);
          ids.put(sch, id);
          schemas.get(type).add(sch);
        }
      }
      int deb = 0;
      for (int fin : corpus.getFinParagraphes()) {
        int taille = fin-deb;
        Delimiteur del = new Delimiteur("p", taille);
        addDel(deb, del);
        if (taille>0) {
          del = new Delimiteur("p", -taille);
          addDel(fin, del);
        }
        deb = fin;
      }
    }
    private void addDel(int pos, Delimiteur del) {
      if (!delimiteurs.containsKey(pos))
        delimiteurs.put(pos, new ArrayList<Delimiteur>());
      delimiteurs.get(pos).add(del);
    }
    private void ecrireDocument(String nomfic) {
      preparerDonnees();
      writer.println("<TEI>");
      ecrireEntete(nomfic);
      writer.println("<text>");
      ecrireTexte();
      ecrireAnnot();
      writer.println("</text>");
      writer.println("</TEI>");
    }
    private void ecrireEntete(String nomfic) {
      String titre = "";
      String auteur = "";
      String source = "";
      if (!corpus.getUnites(TYPE_INFOS_META_DOCUMENTS).isEmpty()) {
        Unite infos = corpus.getUnites(TYPE_INFOS_META_DOCUMENTS).get(0);
        titre = encodeTexte(infos.getProp("titre"));
        auteur = encodeTexte(infos.getProp("auteur"));
        source = encodeTexte(infos.getProp("source"));
      }
      if (titre.isEmpty()) titre = nomfic;
      if (auteur.isEmpty()) auteur = CHAMP_NON_RENSEIGNE;
      if (source.isEmpty()) source = CHAMP_NON_RENSEIGNE;
      writer.println("<teiHeader>");
      writer.println("<fileDesc>");
      writer.println("<titleStmt>");
      writer.println("<title>"+titre+"</title>");
      writer.println("<author>"+auteur+"</author>");
      writer.println("<respStmt>");
      writer.println("<resp>annotated by</resp>");
      writer.println("<name>"+annot+"</name>");
      writer.println("</respStmt>");
      writer.println("</titleStmt>");
      writer.println("<publicationStmt>");
      writer.println("<publisher>"+editeur+"</publisher>");
      writer.println("</publicationStmt>");
      writer.println("<sourceDesc>");
      writer.println("<bibl>"+source+"</bibl>");
      writer.println("</sourceDesc>");
      writer.println("</fileDesc>");
      writer.println("</teiHeader>");
    }
    private void ecrireTexte() {
      writer.println("<body>");
      Integer[] pos = delimiteurs.keySet().toArray(new Integer[0]);
      Arrays.sort(pos);
      ecrireDel(delimiteurs.get(0));
      int deb = 0;
      for (int i = 1; i<pos.length; i++) {
        int fin = pos[i];
        writer.print(encodeTexte(corpus.getTexte().substring(deb, fin)));
        ecrireDel(delimiteurs.get(fin));
        deb = fin;
      }
      writer.println("</body>");
    }
    private void ecrireDel(ArrayList<Delimiteur> delims) {
      Delimiteur[] dels = delims.toArray(new Delimiteur[0]);
      Arrays.sort(dels);
      for (Delimiteur del : dels) {
        if (del.id.equals("p")) {
          if (del.taille>0)
            writer.println("<p>");
          else {
            writer.println();
            if (del.taille<0) writer.println("</p>");
            else writer.println("<p/>");
          }
        } else {
          String ancre1 = "<anchor xml:id=\""+del.id;
          String ancre2 = "\" type=\"AnalecDelimiter\" subtype=\"Unit";
          if (del.taille>0)
            writer.print(ancre1+"-start"+ancre2+"Start\"/>");
          else if (del.taille<0)
            writer.print(ancre1+"-end"+ancre2+"End\"/>");
          else {  // ne devrait pas se produire : une unité de taille nulle
            writer.print(ancre1+"-start"+ancre2+"Start\"/>");
            writer.print(ancre1+"-end"+ancre2+"End\"/>");
          }
        }
      }
    }
    private void ecrireAnnot() {
      writer.println("<back>");
      for (String type : unites.keySet()) {
        writer.println("<spanGrp type=\"AnalecUnit\""
                +" n=\""+encodeName.get(type)+"\">");
        for (Unite unit : unites.get(type)) ecrireUnite(unit);
        writer.println("</spanGrp>");
      }
      for (String type : relations.keySet()) {
        writer.println("<joinGrp type=\"AnalecRelation\""
                +" n=\""+encodeName.get(type)+"\">");
        for (Relation rel : relations.get(type)) ecrireRelation(rel);
        writer.println("</joinGrp>");
      }
      for (String type : schemas.keySet()) {
        writer.println("<joinGrp type=\"AnalecSchema\""
                +" n=\""+encodeName.get(type)+"\">");
        for (Schema sch : schemas.get(type)) ecrireSchema(sch);
        writer.println("</joinGrp>");
      }
      writer.println("<fvLib n=\"AnalecElementProperties\">");
      for (String type : unites.keySet())
        for (Unite unit : unites.get(type)) ecrireProps(unit);
      for (String type : relations.keySet())
        for (Relation rel : relations.get(type)) ecrireProps(rel);
      for (String type : schemas.keySet())
        for (Schema sch : schemas.get(type)) ecrireProps(sch);
      writer.println("</fvLib>");
      writer.println("</back>");
    }
    private void ecrireUnite(Unite unit) {
      String id = ids.get(unit);
      writer.println("<span xml:id=\""+id+"\"");
      writer.println("from=\"#"+id+"-start\" to=\"#"+id+"-end\"");
      writer.println("ana=\"#"+id+"-fs\"/>");
    }
    private void ecrireRelation(Relation rel) {
      String id = ids.get(rel);
      writer.println("<join xml:id=\""+ids.get(rel)+"\"");
      writer.println("target=\"#"+ids.get(rel.getElt1())
              +" #"+ids.get(rel.getElt2())+"\"");
      writer.println("ana=\"#"+id+"-fs\"/>");
    }
    private void ecrireSchema(Schema sch) {
      String id = ids.get(sch);
      writer.println("<join xml:id=\""+ids.get(sch)+"\"");
      writer.print("target=\"");
      for (Element elt : sch.getContenu())
        writer.print("#"+ids.get(elt)+" ");
      writer.println("\"");
      writer.println("ana=\"#"+id+"-fs\"/>");
    }
    private void ecrireProps(Element elt) {
      writer.println("<fs xml:id=\""+ids.get(elt)+"-fs\">");
      HashMap<String, String> props = elt.getProps();
      for (String nom : props.keySet()) {
        if (props.get(nom)!=null)
          writer.println("<f name=\""+encodeName.get(nom)+"\"><string>"
                  +encodeTexte(props.get(nom))+"</string></f>");
      }
      writer.println("</fs>");
    }
    private static String encodeTexte(String ch) {
      return ch.replaceAll("&", "&amp;").replaceAll("<", "&lt;").
              replaceAll(">", "&gt;").replaceAll("\"", "&quot;").
              replaceAll("\'", "&apos;");
    }
    final static String interdits = "[!\\\"#\\$%&'\\(\\)\\*+,/;<=>\\?"
            +"@\\[\\\\\\]\\^`\\{\\|\\}\\~]";
    private static String encodeId(String ch) {
      String id = ch.replaceAll("\\s", "_");
      if (id.matches("[-\\.0-9];*")) id = '_'+id;
      id = id.replaceAll(interdits, "\u00b7");
      if (id.length()>2&&id.substring(0, 3).equalsIgnoreCase("xml")) id = '_'+id;
      return id;
    }
    private static String traiterSaisie(String ch) {
      if (ch==null) return CHAMP_NON_RENSEIGNE;
      if (ch.trim().isEmpty()) return CHAMP_NON_RENSEIGNE;
      return encodeTexte(ch);
    }
  }
}
