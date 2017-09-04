/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.fichiers;

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

/**
 *
 * @author Bernard
 */
public abstract class FichiersGlozz {
  public static void convertirCorpusGlozz(final Corpus corpus) {
    File repGlozz;
    final File repJava;
    if ((repGlozz = Fichiers.choisirRepertoire("Choix du répertoire Glozz"))==null)
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
    final File[] fics = Fichiers.listerFichiers(repGlozz, Fichiers.TypeFichier.GLOZZ_CORPUS);
    final GMessages.GProgression progression =
            new GMessages.GProgression(fics.length, "Conversion", "fichiers");
    Thread traitement = new Thread() {
      @Override
      public void run() {
        try {
        for (int i = 0; i<fics.length; i++) {
          if (progression.isCanceled()) break;
          progression.setNbTraites(i+1);
          File fichierCorpus = fics[i];
          File fichierAnnot = Fichiers.trouverFichier(TypeFichier.GLOZZ_ANNOT, fichierCorpus);
          if (fichierAnnot==null) continue;
          importer(fichierCorpus, fichierAnnot, corpus);
          FichiersJava.enregistrerCorpus(corpus, Fichiers.creerFichier(repJava, TypeFichier.DOC_ANALEC, fichierCorpus));
//          corpus.fermer();
        }
        corpus.fermer();
        } catch (Throwable ex) {
          GMessages.erreurFatale(ex);
        }
      }
    };
    traitement.start();
  }
  public static void importerGlozz(Corpus corpus) {
    BufferedReader text = null;
    BufferedInputStream annot = null;
    File fichierCorpus;
    File fichierAnnot;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier corpus", Fichiers.TypeFichier.GLOZZ_CORPUS))==null)
      return;
    if ((fichierAnnot = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier annotations", Fichiers.TypeFichier.GLOZZ_ANNOT))==null)
      return;
    importer(fichierCorpus, fichierAnnot, corpus);
    corpus.newCorpusLu();
  }
  private static void importer(File fichierCorpus, File fichierAnnot, Corpus corpus) {
    BufferedReader text = null;
    BufferedInputStream annot = null;
    try {
      text = new BufferedReader(new InputStreamReader(new FileInputStream(fichierCorpus), "UTF-8"));
      annot = new BufferedInputStream(new FileInputStream(fichierAnnot));
      corpus.setTexte(text.readLine());
      SAXParserFactory pf = (SAXParserFactory) SAXParserFactory.newInstance();
      SAXParser sp = pf.newSAXParser();
      sp.parse(annot, new GlozzHandler(corpus));
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
        if (text!=null) text.close();
        if (annot!=null) annot.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture du fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  public static void importerModeleGlozz(Corpus corpus) {
    BufferedInputStream modele = null;
    File fichierModele;
    if ((fichierModele = Fichiers.choisirFichier(Commande.OUVRIR,
            "Choix du fichier modèle", Fichiers.TypeFichier.GLOZZ_MODELE))==null)
      return;
    try {
      modele = new BufferedInputStream(new FileInputStream(fichierModele));
      SAXParserFactory pf = (SAXParserFactory) SAXParserFactory.newInstance();
      SAXParser sp = pf.newSAXParser();
      Structure structureLue = new Structure();
      sp.parse(modele, new ModeleGlozzHandler(structureLue));
      corpus.fusionnerStructureLue(structureLue);
      corpus.newCorpusLu();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur de lecture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ParserConfigurationException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur de format de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (SAXException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur de format de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      try {
        if (modele!=null) modele.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, ex, "Erreur de fermeture du fichier lu", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  public static void exporterGlozz(Corpus corpus) {
    PrintWriter text = null;
    PrintWriter annot = null;
    PrintWriter model = null;
    File fichierCorpus;
    File fichierAnnot;
    File fichierModel;
    if ((fichierCorpus = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier corpus", Fichiers.TypeFichier.GLOZZ_CORPUS))==null) return;
    if ((fichierAnnot = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier annotations", Fichiers.TypeFichier.GLOZZ_ANNOT))==null)
      return;
    try {
      text = new PrintWriter(fichierCorpus, "UTF-8");
      annot = new PrintWriter(fichierAnnot, "UTF-8");
      text.print(corpus.getTexte());
      GlozzPrinter gp = new GlozzPrinter(corpus, annot);
      gp.ecrireAnnot();
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
      return;
    } finally {
      if (text!=null) text.close();
      if (annot!=null) annot.close();
    }
    if ((fichierModel = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Exporter aussi le modèle ?", Fichiers.TypeFichier.GLOZZ_MODELE))==null)
      return;
    try {
      model = new PrintWriter(fichierModel, "UTF-8");
      ModelGlozzPrinter gp = new ModelGlozzPrinter(corpus.getStructure(), model);
      gp.ecrireModel();
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
    } finally {
      if (model!=null) model.close();
    }
  }
  public static void exporterModeleGlozz(Structure structure) {
    PrintWriter model = null;
    File fichierModel;
    if ((fichierModel = Fichiers.choisirFichier(Commande.ENREGISTRER,
            "Nom du fichier modèle ?", Fichiers.TypeFichier.GLOZZ_MODELE))==null)
      return;
    try {
      model = new PrintWriter(fichierModel, "UTF-8");
      ModelGlozzPrinter gp = new ModelGlozzPrinter(structure, model);
      gp.ecrireModel();
    } catch (FileNotFoundException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(null, ex, "Erreur d'écriture de fichier", JOptionPane.ERROR_MESSAGE);
    } finally {
      if (model!=null) model.close();
    }
  }

  static class GlozzHandler extends DefaultHandler {
    Corpus corpus;
    Balise etatCourant = null;
    Element elementCourant = null;
    String champCourant = null;
    String valCourant = null;
    HashMap<String, Element> elements = new HashMap<String, Element>();
    ArrayList<Relation> relations = new ArrayList<Relation>();
    HashMap<Relation, ArrayList<String>> idTermes = new HashMap<Relation, ArrayList<String>>();
    ArrayList<Schema> schemas = new ArrayList<Schema>();
    HashMap<Schema, ArrayList<String>> idContenus = new HashMap<Schema, ArrayList<String>>();

    enum Balise {
      ANNOTATIONS, UNIT, RELATION, SCHEMA, METADATA, AUTHOR, CREATIONDATE,
      LASTMODIFIER, LASTMODIFICATIONDATE, CHARACTERISATION, TYPE, FEATURESET,
      FEATURE, POSITIONING, START, END, SINGLEPOSITION, TERM,
      EMBEDDEDUNIT, EMBEDDEDRELATION, EMBEDDEDSCHEMA,
      FLAG, COMMENT
    };
    static Balise balise(String nomBalise) {
      return Balise.valueOf(nomBalise.replaceAll("\\W", "").toUpperCase());
    }
    GlozzHandler(Corpus corpus) {
      super();
      this.corpus = corpus;
    }
    @Override
    public void startDocument() {
      corpus.clearAnnot();
    }
    @Override
    public void endDocument() throws SAXNotSupportedException {
      for (Relation rel : relations) {
        ArrayList<String> idterms = idTermes.get(rel);
        if (idterms.size()!=2) {
          throw new SAXNotSupportedException("Une relation de type "+rel.getType()+" n'a pas deux termes");
        }
        if (elements.get(idterms.get(0))==null) {
          throw new SAXNotSupportedException("Terme inconnu id = "+idterms.get(0)
                  +" pour une relation de type "+rel.getType());
        }
        rel.setElt1(elements.get(idterms.get(0)));
        if (elements.get(idterms.get(1))==null) {
          throw new SAXNotSupportedException("Terme inconnu id = "+idterms.get(1)
                  +" pour une relation de type "+rel.getType());
        }
        rel.setElt2(elements.get(idterms.get(1)));
        corpus.addRelationLue(rel);
      }
      for (Schema sch : schemas) {
        for (String id : idContenus.get(sch)) {
          if (elements.get(id)==null) {
            throw new SAXNotSupportedException("Contenu inconnu id = "+id
                    +" pour un schéma de type "+sch.getType());
          }
          sch.ajouter(elements.get(id));
        }
        corpus.addSchemaLu(sch);
      }
      corpus.verifierFinParagraphesTypesUnites(new String[]{
                "title", "subTitle", "sectionTitle", "subSectionTitle",
                "subSubSectionTitle", "subSubSubSectionTitle", "quote", "listItem"});
      corpus.trierFinParagraphes();
      corpus.verifierFinParagrapheFinTexte();
    }
    @Override
    public void startElement(String uri, String local, String nomBalise, Attributes attributs) throws SAXException {
      if (etatCourant==Balise.FLAG) return;
      switch (balise(nomBalise)) {
        case FLAG:
          etatCourant = Balise.FLAG;
          break;
        case UNIT:
          elementCourant = new Unite();
          elements.put(attributs.getValue("id"), elementCourant);
          break;
        case RELATION:
          elementCourant = new Relation();
          elements.put(attributs.getValue("id"), elementCourant);
          idTermes.put((Relation) elementCourant, new ArrayList<String>());
          break;
        case SCHEMA:
          elementCourant = new Schema();
          elements.put(attributs.getValue("id"), elementCourant);
          idContenus.put((Schema) elementCourant, new ArrayList<String>());
          break;
        case TYPE:
          etatCourant = Balise.TYPE;
          valCourant = "";
          break;
        case FEATURE:
          etatCourant = Balise.FEATURE;
          champCourant = attributs.getValue("name");
          valCourant = "";
          break;
        case START:
          etatCourant = Balise.START;
          break;
        case END:
          etatCourant = Balise.END;
          break;
        case SINGLEPOSITION:
          if (elementCourant.getClass()!=Unite.class) {
            throw new SAXNotSupportedException("La balise singlePosition ne peut être utilisée que pour des unités");
          }
          if (elementCourant.getType().equals("paragraph")) {
            if (etatCourant==Balise.END) {
              corpus.addFinParagraphe(Integer.parseInt(attributs.getValue("index")));
            }
          } else {
            switch (etatCourant) {
              case START:
                ((Unite) elementCourant).setDeb(Integer.parseInt(attributs.getValue("index")));
                break;
              case END:
                ((Unite) elementCourant).setFin(Integer.parseInt(attributs.getValue("index")));
                break;
            }
          }
          break;
        case TERM:
          if (elementCourant.getClass()!=Relation.class)
            throw new SAXNotSupportedException("La balise term ne peut être utilisée que pour des relations");
          idTermes.get((Relation) elementCourant).add(attributs.getValue("id"));
          break;

        case EMBEDDEDUNIT:
        case EMBEDDEDRELATION:
        case EMBEDDEDSCHEMA:
          if (elementCourant.getClass()!=Schema.class)
            throw new SAXNotSupportedException(
                    "Les balises embedded ne peuvent être utilisées que pour des schémas");
          idContenus.get((Schema) elementCourant).add(attributs.getValue("id"));
          break;
      }
    }
    @Override
    public void endElement(String uri, String local, String nomBalise) throws SAXException {
      if (etatCourant==Balise.FLAG) {
        if (balise(nomBalise)==Balise.FLAG) etatCourant = null;
        return;
      }
      String type = "";
      switch (balise(nomBalise)) {
        case UNIT:
          type = elementCourant.getType();
          if (type==null||type.isEmpty())
            throw new SAXNotSupportedException("Pas trouvé de type pour cette unité");
          if (!type.equals("paragraph")) {
            corpus.addUniteLue((Unite) elementCourant);
            elementCourant = null;
          }
          break;
        case RELATION:
          type = elementCourant.getType();
          if (type==null||type.isEmpty())
            throw new SAXNotSupportedException("Pas trouvé de type pour cette relation");
          relations.add((Relation) elementCourant);
          elementCourant = null;
          break;
        case SCHEMA:
          type = elementCourant.getType();
          if (type==null||type.isEmpty())
            throw new SAXNotSupportedException("Pas trouvé de type pour ce shéma");
          schemas.add((Schema) elementCourant);
          elementCourant = null;
          break;
        case TYPE:
          elementCourant.setType(valCourant);
          valCourant = null;
          etatCourant = null;
          break;
        case FEATURE:
          elementCourant.putProp(champCourant, valCourant);
          etatCourant = null;
          champCourant = null;
          valCourant = null;
          break;
        case START:
          etatCourant = null;
          break;
        case END:
          etatCourant = null;
          break;
      }
    }
    @Override
    public void characters(char[] ch, int start, int taille) throws SAXException {
      if (etatCourant==null||!EnumSet.of(Balise.TYPE, Balise.FEATURE).contains(etatCourant)) {
        return;
      }
      valCourant += new String(ch, start, taille);
    }
  }

  static class ModeleGlozzHandler extends DefaultHandler {
    Structure structure;
    boolean listeValeurs = false;
    Class<? extends Element> classeCourante = Unite.class;  // par défaut : balise <units> sous-entendue 
    String typeCourant = null;
    String champCourant = null;
    String valeurCourante = null;

    enum Balise {
      ANNOTATIONMODEL, UNITS, RELATIONS, SCHEMAS, TYPE, FEATURESET,
      FEATURE, POSSIBLEVALUES, VALUE
    };
    static Balise balise(String nomBalise) throws SAXNotRecognizedException {
      try {
        return Balise.valueOf(nomBalise.replaceAll("\\W", "").toUpperCase());
      } catch (Exception e) {
        throw new SAXNotRecognizedException("Balise non reconnue : "+nomBalise);
      }
    }
    ModeleGlozzHandler(Structure structure) {
      super();
      this.structure = structure;
    }
    @Override
    public void startDocument() {
    }
    @Override
    public void endDocument() throws SAXNotSupportedException {
    }
    @Override
    public void startElement(String uri, String local, String nomBalise, Attributes attributs) throws SAXException {
      String valdef = null;
      switch (balise(nomBalise)) {
        case UNITS:
          classeCourante = Unite.class;
          break;
        case RELATIONS:
          classeCourante = Relation.class;
          break;
        case SCHEMAS:
          classeCourante = Schema.class;
          break;
        case TYPE:
          typeCourant = attributs.getValue("name");
          if (typeCourant==null||typeCourant.isEmpty())
            throw new SAXNotSupportedException("Nom de type absent");
          structure.ajouterType(classeCourante, typeCourant);
          break;
        case FEATURE:
          if (typeCourant==null||typeCourant.isEmpty())
            throw new SAXNotSupportedException("Nom de type absent");
          champCourant = attributs.getValue("name");
          if (champCourant==null||champCourant.isEmpty())
            throw new SAXNotSupportedException("Nom de type absent");
          structure.ajouterProp(classeCourante, typeCourant, champCourant);
          break;
        case POSSIBLEVALUES:
          if (typeCourant==null||typeCourant.isEmpty()||champCourant==null||champCourant.isEmpty())
            throw new SAXNotSupportedException("Nom de type ou de feature absent");
          listeValeurs = true;
          valdef = attributs.getValue("default");
          if (valdef!=null&&!valdef.isEmpty())
            structure.modifierValParDefaut(classeCourante, typeCourant, champCourant, valdef);
          break;
        case VALUE:
          if (listeValeurs) valeurCourante = "";
          valdef = attributs.getValue("default");
          if (valdef!=null&&!valdef.isEmpty()) {
            if (typeCourant==null||typeCourant.isEmpty()||champCourant==null||champCourant.isEmpty())
              throw new SAXNotSupportedException("Nom de type ou de feature absent");
            structure.ajouterVal(classeCourante, typeCourant, champCourant, valdef);
            structure.modifierValParDefaut(classeCourante, typeCourant, champCourant, valdef);
          }
          break;
      }
    }
    @Override
    public void endElement(String uri, String local, String nomBalise) throws SAXException {
      switch (balise(nomBalise)) {
        case POSSIBLEVALUES:
          listeValeurs = false;
          break;
        case VALUE:
          if (listeValeurs&&valeurCourante!=null&&!valeurCourante.isEmpty())
            structure.ajouterVal(classeCourante, typeCourant, champCourant, valeurCourante);
          break;
      }
    }
    @Override
    public void characters(char[] ch, int start, int taille) throws SAXException {
      if (listeValeurs) {
        valeurCourante += new String(ch, start, taille);
      }
    }
  }

  static class GlozzPrinter {
    Corpus corpus;
    PrintWriter annot;
    HashMap<Element, String> idDates = new HashMap<Element, String>();
    String idAuteur = "analec0.2";
    String idDateParagraphes = "0";
    String idParagraphes = idAuteur+"_"+idDateParagraphes;
    GlozzPrinter(Corpus corpus, PrintWriter annot) {
      this.corpus = corpus;
      this.annot = annot;
      attribueIdDates();
    }
    void attribueIdDates() {
      long idDate = (new Date()).getTime();
      for (Unite unite : corpus.getToutesUnites()) {
        idDates.put(unite, Long.toString(idDate++));
      }
      for (Relation relation : corpus.getToutesRelations()) {
        idDates.put(relation, Long.toString(idDate++));
      }
      for (Schema schema : corpus.getTousSchemas()) {
        idDates.put(schema, Long.toString(idDate++));
      }
    }
    String getId(Element elt) {
      return idAuteur+"_"+idDates.get(elt);
    }
    void ecrireAnnot() {
      annot.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
      annot.println("<annotations>");
      ecrireFinParagraphes();
      ecrireUnites();
      ecrireRelations();
      ecrireSchemas();
      annot.println("</annotations>");
    }
    private void ecrireFinParagraphes() {
      int deb = 0;
      for (int fin : corpus.getFinParagraphes()) {
        ecrireUnite("paragraph", idAuteur, idDateParagraphes, idParagraphes, new HashMap<String, String>(), deb, fin);
        deb = fin;
      }
    }
    private void ecrireUnites() {
      for (Unite unit : corpus.getToutesUnites()) {
        ecrireUnite(unit.getType(), idAuteur, idDates.get(unit), getId(unit), unit.getProps(), unit.getDeb(), unit.getFin());
      }
    }
    private void ecrireRelations() {
      for (Relation rel : corpus.getToutesRelations()) {
        ecrireRelation(rel.getType(), idAuteur, idDates.get(rel), getId(rel), rel.getProps(), rel.getElt1(), rel.getElt2());
      }
    }
    private void ecrireSchemas() {
      for (Schema sch : corpus.getTousSchemas()) {
        ecrireSchema(sch.getType(), idAuteur, idDates.get(sch), getId(sch), sch.getProps(), sch.getContenu());
      }
    }
    private void ecrireUnite(String type, String idAuteur, String idDate, String id, HashMap<String, String> props, int deb, int fin) {
      annot.println("<unit id=\""+id+"\">");
      ecrireMetadata(idAuteur, idDate);
      ecrireProps(type, props);
      ecrirePos(deb, fin);
      annot.println("</unit>");
    }
    private void ecrireRelation(String type, String idAuteur, String idDate, String id, HashMap<String, String> props, Element elt1, Element elt2) {
      annot.println("<relation id=\""+id+"\">");
      ecrireMetadata(idAuteur, idDate);
      ecrireProps(type, props);
      ecrireRel(elt1, elt2);
      annot.println("</relation>");
    }
    private void ecrireSchema(String type, String idAuteur, String idDate, String id, HashMap<String, String> props, HashSet<Element> elts) {
      annot.println("<schema id=\""+id+"\">");
      ecrireMetadata(idAuteur, idDate);
      ecrireProps(type, props);
      ecrireSch(elts);
      annot.println("</schema>");
    }
    private void ecrireMetadata(String idAuteur, String id) {
      annot.println("<metadata>");
      annot.println("<author>"+idAuteur+"</author>");
      annot.println("<creation-date>"+id+"</creation-date>");
      annot.println("</metadata>");
    }
    private void ecrireProps(String type, HashMap<String, String> props) {
      annot.println("<characterisation>");
      annot.println("<type>"+encodeXMLTexte(type)+"</type>");
      if (props.isEmpty()) {
        annot.println("<featureSet/>");
      } else {
        annot.println("<featureSet>");
        for (String prop : props.keySet()) {
          if (props.get(prop)!=null) {
            annot.println("<feature name =\""+encodeXMLTexte(prop)+"\">"
                    +encodeXMLTexte(props.get(prop))+"</feature>");
          }
        }
        annot.println("</featureSet>");

      }
      annot.println("</characterisation>");
    }
    private void ecrirePos(int deb, int fin) {
      annot.println("<positioning>");
      annot.println("<start>");
      annot.println("<singlePosition index = \""+deb+"\"/>");
      annot.println("</start>");
      annot.println("<end>");
      annot.println("<singlePosition index = \""+fin+"\"/>");
      annot.println("</end>");
      annot.println("</positioning>");
    }
    private void ecrireRel(Element elt1, Element elt2) {
      annot.println("<positioning>");
      annot.println("<term id = \""+getId(elt1)+"\"/>");
      annot.println("<term id = \""+getId(elt2)+"\"/>");
      annot.println("</positioning>");
    }
    private void ecrireSch(HashSet<Element> elts) {
      annot.println("<positioning>");
      for (Element elt : elts) {
        Class classe = elt.getClass();
        if (classe==Unite.class) {
          annot.print("<embedded-unit ");
        }
        if (classe==Relation.class) {
          annot.print("<embedded-relation ");
        }
        if (classe==Schema.class) {
          annot.print("<embedded-schema ");
        }
        annot.println("id = \""+getId(elt)+"\"/>");
      }
      annot.println("</positioning>");
    }
  }

  static class ModelGlozzPrinter {
    Structure structure;
    PrintWriter model;
    ModelGlozzPrinter(Structure structure, PrintWriter model) {
      this.structure = structure;
      this.model = model;
    }
    void ecrireModel() {
      model.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
      model.println("<annotationModel>");
      ecrireUnites();
      ecrireRelations();
      ecrireSchemas();
      model.println("</annotationModel>");
    }
    private void ecrireUnites() {
      model.println("<units>");
      for (String type : structure.getTypes(Unite.class))
        ecrireType(Unite.class, type);
      model.println("</units>");
    }
    private void ecrireRelations() {
      model.println("<relations>");
      for (String type : structure.getTypes(Relation.class))
        ecrireType(Relation.class, type);
      model.println("</relations>");
    }
    private void ecrireSchemas() {
      model.println("<schemas>");
      for (String type : structure.getTypes(Schema.class))
        ecrireType(Schema.class, type);
      model.println("</schemas>");
    }
    private void ecrireType(Class<? extends Element> classe, String type) {
      model.print("<type name=\""+encodeXMLTexte(type)+"\"");
      if (classe==Relation.class) model.print(" oriented=\"true\"");
      model.println(">");
      model.println("<featureSet>");
      for (String prop : structure.getNomsProps(classe, type))
        ecrireProp(classe, type, prop);
      model.println("</featureSet>");
      model.println("</type>");
    }
    private void ecrireProp(Class<? extends Element> classe, String type, String prop) {
      model.println("<feature name=\""+encodeXMLTexte(prop)+"\">");
      if (structure.getValeursProp(classe, type, prop).isEmpty()) {
        model.println("<value type=\"free\" default=\"\" />");
      } else {
        model.println("<possibleValues default = \""
                +structure.getValeurParDefaut(classe, type, prop)+"\">");
        for (String val : structure.getValeursProp(classe, type, prop))
          model.println("<value>"+encodeXMLTexte(val)+"</value>");
        model.println("</possibleValues>");
      }
      model.println("</feature>");

    }
  }
  static String encodeXMLTexte(String ch) {
//    return ch;
    return ch.replaceAll("&", "&amp;").replaceAll("<", "&lt;").
            replaceAll(">", "&gt;").replaceAll("\"", "&quot;").
            replaceAll("\'", "&apos;");
  }
}
