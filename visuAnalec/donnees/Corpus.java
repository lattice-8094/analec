
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.donnees;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import javax.swing.*;
import visuAnalec.*;
import visuAnalec.elements.*;
import visuAnalec.Message.*;
import visuAnalec.texte.PanneauTexte;
import visuAnalec.util.*;

/**
 *
 * @author Bernard
 */
public class Corpus {
  public static interface CorpusListener extends EventListener {  // VisuAnalec, Vue et VisuStructure sont les seuls destinaires
    void traiterEvent(Message e);
  }
  private ArrayList<CorpusListener> listeCorrespondants = new ArrayList<CorpusListener>();
  private boolean modifie = false;
  private String texte = "";
  private ArrayList<Integer> finParagraphes = new ArrayList<Integer>();
  private Structure structure = new Structure();
  private HashMap<String, ArrayList<Unite>> unites = new HashMap<String, ArrayList<Unite>>();
  private HashMap<String, ArrayList<Relation>> relations = new HashMap<String, ArrayList<Relation>>();
  private HashMap<String, ArrayList<Schema>> schemas = new HashMap<String, ArrayList<Schema>>();
  public void addEventListener(CorpusListener correspondant) {
    listeCorrespondants.add(correspondant);
  }
  public void removeEventListener(CorpusListener correspondant) {
    listeCorrespondants.remove(correspondant);
  }
  private void fireMessage(Message evt) {
    modifie = !EnumSet.of(TypeMessage.CLEAR_CORPUS, TypeMessage.NEW_CORPUS).
            contains(evt.getType());
    for (Object corresp : listeCorrespondants.toArray()) {
      ((CorpusListener) corresp).traiterEvent(evt);
    }
  }
  public Structure getStructure() {
    return structure;
  }
  public String getTexte() {
    return texte;
  }
  public void setTexte(String texte) {
    this.texte = texte;
  }
  public Integer[] getFinParagraphes() {
    return finParagraphes.toArray(new Integer[0]);
  }
  public void setFinParagraphes(ArrayList<Integer> finpars) {
    finParagraphes = finpars;
  }
  public String getTexteUnite(Unite unit) {
    int deb = unit.getDeb();
    int fin = unit.getFin();
    return PanneauTexte.insereCarFinParagraphe(getTexte().substring(deb, fin),
            getFinParagraphes(), deb);
  }
  public void modifTexte(String texte, ArrayList<Integer> finparag) {
    this.texte = texte;
    this.finParagraphes = finparag;
    verifierFormesUnites();
    fireMessage(new Message(this, TypeMessage.MODIF_TEXTE));
  }
  private void verifierFormesUnites() {
    for (String type : structure.getTypes(Unite.class)) {
      if (structure.getNomsProps(Unite.class, type).contains(Structure.FORME)) {
        boolean modif = false;
        for (Unite unit : getUnites(type)) {
          String forme = getTexteUnite(unit);
          if (!forme.equals(unit.getProp(Structure.FORME))) {
            setValeurPropElement(unit, Structure.FORME, forme);
          }
        }
      }
    }
  }
  public void addFinParagraphe(int ind) {
    finParagraphes.add(ind);
  }
  public void verifierFinParagrapheFinTexte() {
    if (finParagraphes.isEmpty()
            ||finParagraphes.get(finParagraphes.size()-1)<texte.length())
      finParagraphes.add(texte.length());
  }
  public void trierFinParagraphes() {
    Integer[] fins = getFinParagraphes();
    Arrays.sort(fins);
    finParagraphes = new ArrayList<Integer>(Arrays.asList(fins));
  }
  public void verifierFinParagraphesTypesUnites(String[] types) {
    // Attention : les fins de paragraphes sont en désordre à la fin de la fonction
    Integer[] fins = getFinParagraphes();
    Arrays.sort(fins);
    ArrayList<Integer> newFinPars = new ArrayList<Integer>();
    for (String type : types) for (Unite unit : getUnites(type)) {
        Integer fin = unit.getFin();
        if (Arrays.binarySearch(fins, fin)<0) newFinPars.add(fin);
      }
    finParagraphes.addAll(newFinPars);
  }
  public int[] calculerNoParagraphe(Unite[] unites) {
    // les unites sont triées
    // les numéros de paragraphe commencent à 1 
    int[] nosPar = new int[unites.length];
    Integer[] finPar = getFinParagraphes();
    Arrays.sort(finPar);
    int nopar = 0;
    for (int i = 0; i<unites.length; i++) {
      while (unites[i].getDeb()>=finPar[nopar]) nopar++;
      nosPar[i] = nopar+1;
    }
    return nosPar;
  }
  public String[][] getConcordances(Element[] elts, int empan) {
    Integer[] finPar = getFinParagraphes();
    String[][] concordances = new String[elts.length][3];
    for (int i = 0; i<elts.length; i++) {
      Unite unite0 = elts[i].getUnite0();
      int debElt = unite0.getDeb();
      int finElt = unite0.getFin();
      int deb;
      if (debElt-empan>0) {
        concordances[i][0] = "[...]";
        deb = debElt-empan;
      } else {
        concordances[i][0] = "";
        deb = 0;
      }
      concordances[i][0] += PanneauTexte.insereCarFinParagraphe(
              texte.substring(deb, debElt), finPar, deb);
      concordances[i][1] = PanneauTexte.insereCarFinParagraphe(
              texte.substring(debElt, finElt), finPar, debElt);
      if (finElt+empan<texte.length()) {
        concordances[i][2] = PanneauTexte.insereCarFinParagraphe(
                texte.substring(finElt, finElt+empan), finPar, finElt)+"[...]";
      } else
        concordances[i][2] = PanneauTexte.insereCarFinParagraphe(
                texte.substring(finElt), finPar, finElt);
    }
    return concordances;
  }
  public void addUniteSaisie(String type, int deb, int fin) { // ajout d'une unité saisie
    Unite unite = new Unite(type, deb, fin);
    addValeursDefauts(unite);
    if (!unites.containsKey(type)) {
      unites.put(type, new ArrayList<Unite>());
    }
    unites.get(type).add(unite);
    fireMessage(new ElementEvent(this, TypeModifElement.AJOUT_UNITE, unite));
  }
  public void addUniteLue(Unite unite) { // ajout d'une unité créée par la lecture d'un corpus: pas de message 
    // et mise à jour éventuelle de la structure et de la HashMap unites
    String type = unite.getType();
    if (type==null||type.isEmpty()) return;
    structure.majNewElement(unite);
    if (!unites.containsKey(type)) {
      unites.put(type, new ArrayList<Unite>());
    }
    unites.get(type).add(unite);
  }
  public void modifBornesUnite(Unite unite, int deb, int fin) {
    unite.setDeb(deb);
    unite.setFin(fin);
    fireMessage(new ElementEvent(this, TypeModifElement.BORNES_UNITE, unite));
  }
  public void supUnite(Unite unite) {
    String type = unite.getType();
    unites.get(type).remove(unite);
    fireMessage(new ElementEvent(this, TypeModifElement.SUP_UNITE, unite));
    supLiensElement(unite);
  }
  public boolean checkPresenceUnite(String type, int deb, int fin) { // pas très efficace
    if (!unites.containsKey(type)) return false;
    for (Unite unit : getUnites(type))
      if (unit.getDeb()==deb&&unit.getFin()==fin) return true;
    return false;

  }
  private int checkFusionnerUnites(HashMap<String, ArrayList<Unite>> newunites, HashSet<Unite> unitesAajouter,
          HashMap<Unite, Unite> unitesAfusionner) {
    int nbEcrases = 0;
    for (String type : newunites.keySet()) {
      if (!unites.containsKey(type)) {
        unitesAajouter.addAll(newunites.get(type));
        continue;
      }
      for (Unite newunite : newunites.get(type)) {
        Unite unit = chercherUniteCoincidente(newunite, unites.get(type));
        if (unit==null) unitesAajouter.add(newunite);
        else {
          unitesAfusionner.put(newunite, unit);
          for (String prop : newunite.getProps().keySet())
            if (!unit.getProp(prop).isEmpty()&&!newunite.getProp(prop).isEmpty()
                    &&!unit.getProp(prop).equals(newunite.getProp(prop))) {
              nbEcrases++;
              break;
            }
        }
      }
    }
    return nbEcrases;
  }
  private Unite chercherUniteCoincidente(Unite unite, ArrayList<Unite> lunites) {
    for (Unite unit : lunites) if (unite.coincide(unit)) return unit;
    return null;
  }
  public void addRelationSaisie(String type, Element elt1, Element elt2) {
    Relation relation = new Relation(type, elt1, elt2);
    addValeursDefauts(relation);
    if (!relations.containsKey(type)) {
      relations.put(type, new ArrayList<Relation>());
    }
    relations.get(type).add(relation);
    relation.getElt2().addRelationEntrante(relation);
    relation.getElt1().addRelationSortante(relation);
    fireMessage(new ElementEvent(this, TypeModifElement.AJOUT_RELATION, relation));
  }
  public void addRelationLue(Relation relation) { // ajout d'une relation créée par la lecture d'un corpus: pas de message
    // et mise à jour éventuelle de la structure et de la HashMap relations
    String type = relation.getType();
    if (type==null||type.isEmpty()) return;
    structure.majNewElement(relation);
    if (!relations.containsKey(type)) {
      relations.put(type, new ArrayList<Relation>());
    }
    relations.get(type).add(relation);
    relation.getElt2().addRelationEntrante(relation);
    relation.getElt1().addRelationSortante(relation);
  }
  public void supRelation(Relation rel) {
    String type = rel.getType();
    relations.get(type).remove(rel);
    rel.getElt1().oteRelationSortante(rel);
    rel.getElt2().oteRelationEntrante(rel);
    fireMessage(new ElementEvent(this, TypeModifElement.SUP_RELATION, rel));
    supLiensElement(rel);
  }
  private int checkFusionnerRelations(HashMap<String, ArrayList<Relation>> newrels, HashSet<Relation> relsAajouter,
          HashMap<Relation, Relation> relsAfusionner) {
    int nbEcrases = 0;
    for (String type : newrels.keySet()) {
      if (!relations.containsKey(type)) {
        relsAajouter.addAll(newrels.get(type));
        continue;
      }
      for (Relation newrel : newrels.get(type)) {
        Relation rel = chercherRelationCoincidente(newrel, relations.get(type));
        if (rel==null) relsAajouter.add(newrel);
        else {
          relsAfusionner.put(newrel, rel);
          for (String prop : newrel.getProps().keySet())
            if (!rel.getProp(prop).isEmpty()&&!newrel.getProp(prop).isEmpty()
                    &&!rel.getProp(prop).equals(newrel.getProp(prop))) {
              nbEcrases++;
              break;
            }
        }
      }
    }
    return nbEcrases;
  }
  private Relation chercherRelationCoincidente(Relation rel, ArrayList<Relation> lrels) {
    for (Relation rel1 : lrels) if (rel.coincide(rel1)) return rel1;
    return null;
  }
  private static void fusionnerElementsRelation(Relation relation, HashMap<Unite, Unite> unitesAfusionner,
          HashMap<Relation, Relation> relationsAfusionner, HashMap<Schema, Schema> schemasAfusionner) {
    if (relation.getElt1().getClass()==Unite.class&&unitesAfusionner.containsKey(relation.getElt1()))
      relation.setElt1(unitesAfusionner.get(relation.getElt1()));
    else if (relation.getElt1().getClass()==Relation.class&&relationsAfusionner.containsKey(relation.getElt1()))
      relation.setElt1(relationsAfusionner.get(relation.getElt1()));
    else if (relation.getElt1().getClass()==Schema.class&&schemasAfusionner.containsKey(relation.getElt1()))
      relation.setElt1(schemasAfusionner.get(relation.getElt1()));
    if (relation.getElt2().getClass()==Unite.class&&unitesAfusionner.containsKey(relation.getElt2()))
      relation.setElt2(unitesAfusionner.get(relation.getElt2()));
    else if (relation.getElt2().getClass()==Relation.class&&relationsAfusionner.containsKey(relation.getElt2()))
      relation.setElt2(relationsAfusionner.get(relation.getElt2()));
    else if (relation.getElt2().getClass()==Schema.class&&schemasAfusionner.containsKey(relation.getElt2()))
      relation.setElt2(schemasAfusionner.get(relation.getElt2()));
  }
  public void addSchemaSaisi(String type, Element elt) {
    Schema schema = new Schema(type);
    addValeursDefauts(schema);
    if (!schemas.containsKey(type)) {
      schemas.put(type, new ArrayList<Schema>());
    }
    schemas.get(type).add(schema);
    if (elt!=null) {
      schema.ajouter(elt);
      elt.addSchemaContenant(schema);
    }
    fireMessage(new ElementEvent(this, TypeModifElement.AJOUT_SCHEMA, schema));
  }
  public void addSchemaLu(Schema schema) {  // ajout d'un schéma créé par la lecture d'un corpus: pas de message
    // et mise à jour éventuelle de la structure et de la HashMap schemas
    String type = schema.getType();
    if (type==null||type.isEmpty()) return;
    structure.majNewElement(schema);
    if (!schemas.containsKey(type)) {
      schemas.put(type, new ArrayList<Schema>());
    }
    schemas.get(type).add(schema);
    for (Element elt : schema.getContenu()) {
      elt.addSchemaContenant(schema);
    }
  }
  public void ajouterElementSchemaSaisi(Element elt, Schema sch) {
    if (!sch.ajouter(elt)) {
      GMessages.impossibiliteAction("Impossible d'ajouter cet élément "
              +"au schéma courant :\n problème de récursivité infinie");
    }
    elt.addSchemaContenant(sch);
    fireMessage(new ElementEvent(this, TypeModifElement.MODIF_SCHEMA, sch));
  }
  public void oterElementSchema(Element elt, Schema sch) {
    sch.oter(elt);
    elt.oteSchemaContenant(sch);
    fireMessage(new ElementEvent(this, TypeModifElement.MODIF_SCHEMA, sch));
  }
  public void supSchema(Schema sch) {
    String type = sch.getType();
    schemas.get(type).remove(sch);
    fireMessage(new ElementEvent(this, TypeModifElement.SUP_SCHEMA, sch));
    supLiensElement(sch);
  }
  private int checkFusionnerSchemas(HashMap<String, ArrayList<Schema>> newschs, HashSet<Schema> schsAajouter,
          HashMap<Schema, Schema> schsAfusionner) {
    int nbEcrases = 0;
    for (String type : newschs.keySet()) {
      if (!schemas.containsKey(type)) {
        schsAajouter.addAll(newschs.get(type));
        continue;
      }
      for (Schema newsch : newschs.get(type)) {
        Schema sch = chercherSchemaCoincident(newsch, schemas.get(type));
        if (sch==null) schsAajouter.add(newsch);
        else {
          schsAfusionner.put(newsch, sch);
          for (String prop : newsch.getProps().keySet())
            if (!sch.getProp(prop).isEmpty()&&!newsch.getProp(prop).isEmpty()
                    &&!sch.getProp(prop).equals(newsch.getProp(prop))) {
              nbEcrases++;
              break;
            }
        }
      }
    }
    return nbEcrases;
  }
  private Schema chercherSchemaCoincident(Schema sch, ArrayList<Schema> lschs) {
    for (Schema sch1 : lschs) if (sch.coincide(sch1)) return sch1;
    return null;
  }
  private static void fusionnerElementsSchema(Schema schema, HashMap<Unite, Unite> unitesAfusionner,
          HashMap<Relation, Relation> relationsAfusionner, HashMap<Schema, Schema> schemasAfusionner) {
    for (Element elt : schema.getContenu()) {
      if (elt.getClass()==Unite.class&&unitesAfusionner.containsKey(elt)) {
        schema.oter(elt);
        schema.ajouter(unitesAfusionner.get(elt));
      } else if (elt.getClass()==Relation.class&&relationsAfusionner.containsKey(elt)) {
        schema.oter(elt);
        schema.ajouter(relationsAfusionner.get(elt));
      } else if (elt.getClass()==Schema.class&&schemasAfusionner.containsKey(elt)) {
        schema.oter(elt);
        schema.ajouter(schemasAfusionner.get(elt));
      }
    }
  }
  private void supLiensElement(Element elt) {
    ArrayList<Relation> rels = elt.getRelations();
    for (Relation rel : rels) {
      supRelation(rel);
    }
    ArrayList<Schema> schs = elt.getSchemas();
    for (Schema sch : schs) {
      oterElementSchema(elt, sch);
    }
  }
  public void addValeur(Class<? extends Element> classe, String type, String prop, String valeur) {
    structure.ajouterVal(classe, type, prop, valeur);
    fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_VALEUR, classe, type, prop, valeur));
  }
  public void addValeursDefauts(Element elt) {
    for (String prop : structure.getNomsProps(elt.getClass(), elt.getType())) {
      elt.putProp(prop, structure.getValeurParDefaut(elt.getClass(), elt.getType(), prop));
    }
  }
  public void ajouterFormeUnite(String type) {
    structure.ajouterProp(Unite.class, type, Structure.FORME);
    fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_FORME_UNITE,
            Unite.class, type));
  }
  public void instancierFormeUnite(String type) {
    ArrayList<Unite> units = getUnites(type);
    for (Unite unit : units) {
      String forme = getTexteUnite(unit);
      setValeurPropElement(unit, Structure.FORME, forme);
    }
  }
  public void modifierValParDefaut(Class<? extends Element> classe, String type, String prop, String val) {
    structure.modifierValParDefaut(classe, type, prop, val);
    if (!val.isEmpty()) instancierParValeurDefaut(classe, type, prop, val);
    fireMessage(new StructureEvent(this, TypeModifStructure.VALEUR_PAR_DEFAUT, classe, type, prop, val));
  }
  private void instancierParValeurDefaut(Class<? extends Element> classe, String type, String prop, String val) {
    ArrayList<? extends Element> elts = getElements(classe, type);
    boolean noninstancies = false;
    for (Element elt : elts) if (elt.getProp(prop).isEmpty()) {
        noninstancies = true;
        break;
      }
    if (noninstancies&&JOptionPane.showConfirmDialog(null, "Voulez-vous donner "
            +"cette valeur par défaut aux éléments existants non encore instanciés",
            "Action optionnelle", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
            !=JOptionPane.NO_OPTION)
      for (Element elt : elts) if (elt.getProp(prop).isEmpty())
          elt.putProp(prop, val);
  }
  public void setValeurPropElement(Element elt, String prop, String newval) {
    if (newval.isEmpty()) {
      elt.supProp(prop);
    } else {
      if (!structure.getValeursProp(elt.getClass(), elt.getType(), prop).contains(newval)) {
        addValeur(elt.getClass(), elt.getType(), prop, newval);
      }
      elt.putProp(prop, newval);
    }
    fireMessage(new ElementEvent(this, TypeModifElement.MODIF_VALEUR, elt, prop, newval));

  }
  public ArrayList<? extends Element> getElements(Class<? extends Element> classe, String type) {
    if (classe==Unite.class) return getUnites(type);
    if (classe==Relation.class) return getRelations(type);
    if (classe==Schema.class) return getSchemas(type);
    throw new UnsupportedOperationException("sous-classe non traitée");
  }
  public ArrayList<Unite> getUnites(String type) {
    if (unites.get(type)==null) {
      return new ArrayList<Unite>();
    }
    return unites.get(type);
  }
  public ArrayList<Unite> getToutesUnites() {
    ArrayList<Unite> ttesUnites = new ArrayList<Unite>();
    for (String type : structure.getTypes(Unite.class)) {
      ttesUnites.addAll(getUnites(type));
    }
    return ttesUnites;
  }
  public ArrayList<Relation> getRelations(String type) {
    if (relations.get(type)==null) {
      return new ArrayList<Relation>();
    }
    return relations.get(type);
  }
  public ArrayList<Relation> getToutesRelations() {
    ArrayList<Relation> ttesRelations = new ArrayList<Relation>();
    for (String type : structure.getTypes(Relation.class)) {
      ttesRelations.addAll(getRelations(type));
    }
    return ttesRelations;
  }
  public ArrayList<Schema> getSchemas(String type) {
    if (schemas.get(type)==null) {
      return new ArrayList<Schema>();
    }
    return schemas.get(type);
  }
  public ArrayList<Schema> getTousSchemas() {
    ArrayList<Schema> tsSchemas = new ArrayList<Schema>();
    for (String type : structure.getTypes(Schema.class)) {
      tsSchemas.addAll(getSchemas(type));
    }
    return tsSchemas;
  }
// Modifs de la structure provenant de VisuStructure
  public String saisirAjouterType(Container fenetre, Class<? extends Element> classe) {
    String newNom = JOptionPane.showInputDialog(fenetre, "Nom du nouveau type ",
            "Ajouter un type "+GVisu.getNomDeClasse(classe),
            JOptionPane.PLAIN_MESSAGE);
    if (newNom==null) return null;
    newNom = newNom.trim();
    if (newNom.isEmpty()) return null;
    if (structure.getTypes(classe).contains(newNom)) {
      JOptionPane.showMessageDialog(fenetre, "Ajout impossible : le type "+"\""+newNom+"\""+" existe déjà",
              "Erreur d'ajout de type", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    ajouterType(classe, newNom);
    fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_TYPE, classe, newNom));
    return newNom;
  }
  private void ajouterType(Class<? extends Element> classe, String type) {
    structure.ajouterType(classe, type);
    if (classe==Unite.class) {
      unites.put(type, new ArrayList<Unite>());
    } else if (classe==Relation.class) {
      relations.put(type, new ArrayList<Relation>());
    } else if (classe==Schema.class) {
      schemas.put(type, new ArrayList<Schema>());
    }
  }
  public String saisirAjouterProp(Container fenetre, Class<? extends Element> classe, String type) {
    String newNom = JOptionPane.showInputDialog(fenetre, "Nom de la nouvelle propriété",
            "Ajouter une propriété au type "+"\""+type+"\"", JOptionPane.PLAIN_MESSAGE);
    if (newNom==null) return null;
    newNom = newNom.trim();
    if (newNom.isEmpty()) return null;
    if (Structure.FORME.equals(newNom)) {
      GMessages.impossibiliteAction("Le nom "+Structure.FORME+" est réservé par le système");
      return null;
    }
    if (structure.getNomsProps(classe, type).contains(newNom)) {
      JOptionPane.showMessageDialog(fenetre, "Ajout impossible :la propriété "+"\""+newNom+"\""+" existe déjà",
              "Erreur d'ajout de propriété", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    structure.ajouterProp(classe, type, newNom);
    fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_PROP, classe, type, newNom));
    return newNom;
  }
  public String saisirAjouterVal(Container fenetre, Class<? extends Element> classe, String type, String prop) {
    String newNom = JOptionPane.showInputDialog(fenetre, "Nom de la nouvelle valeur",
            "Ajouter une valeur pour la propriété "+"\""+prop+"\"", JOptionPane.PLAIN_MESSAGE);
    if (newNom==null) return null;
    newNom = newNom.trim();
    if (newNom.isEmpty()) return null;
    if (structure.getValeursProp(classe, type, prop).contains(newNom)) {
      JOptionPane.showMessageDialog(fenetre, "Ajout impossible : la valeur "+"\""+newNom+"\""+" existe déjà",
              "Erreur d'ajout de valeur", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    structure.ajouterVal(classe, type, prop, newNom);
    fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_VALEUR, classe, type, prop, newNom));
    return newNom;
  }
  public String saisirRenommerType(Container fenetre, Class<? extends Element> classe, String oldNom) {
    String newNom = (String) JOptionPane.showInputDialog(fenetre, "Nouveau nom pour le type "+"\""+oldNom+"\"",
            "Renommer un type "+GVisu.getNomDeClasse(classe), JOptionPane.PLAIN_MESSAGE, null, null, oldNom);
    if (newNom==null) return null;
    newNom = newNom.trim();
    if (oldNom.equals(newNom)||newNom.isEmpty()) return null;
    boolean fusion = structure.getTypes(classe).contains(newNom);
    if (fusion&&!GMessages.confirmationAction("Le type "+"\""+newNom+"\""
            +" existe déjà. Fusionner les deux types ? ")) return null;
    if (classe==Unite.class) {
      if (fusion) unites.get(newNom).addAll(unites.get(oldNom));
      else unites.put(newNom, unites.get(oldNom));
      for (Unite unit : unites.get(oldNom)) unit.setType(newNom);
      unites.remove(oldNom);
    } else if (classe==Relation.class) {
      if (fusion) relations.get(newNom).addAll(relations.get(oldNom));
      else relations.put(newNom, relations.get(oldNom));
      for (Relation rel : relations.get(oldNom)) {
        rel.getElt1().oteRelationSortante(rel);
        rel.getElt2().oteRelationEntrante(rel);
        rel.setType(newNom);
        rel.getElt1().addRelationSortante(rel);
        rel.getElt2().addRelationEntrante(rel);
      }
      relations.remove(oldNom);
    } else if (classe==Schema.class) {
      if (fusion) schemas.get(newNom).addAll(schemas.get(oldNom));
      else schemas.put(newNom, schemas.get(oldNom));
      for (Schema sch : schemas.get(oldNom)) {
        for (Element elt : sch.getContenu()) elt.oteSchemaContenant(sch);
        sch.setType(newNom);
        for (Element elt : sch.getContenu()) elt.addSchemaContenant(sch);
      }
      schemas.remove(oldNom);
    }
    if (fusion) {
      structure.fusionnerType(classe, oldNom, newNom);
      fireMessage(new StructureEvent(this, TypeModifStructure.FUSION_TYPE,
              classe, oldNom, newNom));
    } else {
      structure.renommerType(classe, oldNom, newNom);
      fireMessage(new StructureEvent(this, TypeModifStructure.RENOM_TYPE,
              classe, oldNom, newNom));
    }
    return newNom;
  }
  public String saisirRenommerProp(Container fenetre,
          Class<? extends Element> classe, String type, String oldNom) {
    String newNom = (String) JOptionPane.showInputDialog(fenetre, "Nouveau nom pour la propriété "+"\""+oldNom+"\"",
            "Renommer une propriété du type "+"\""+type+"\"", JOptionPane.PLAIN_MESSAGE, null, null, oldNom);
    if (newNom==null) return null;
    newNom = newNom.trim();
    if (oldNom.equals(newNom)||newNom.isEmpty()) return null;
    if (Structure.FORME.equals(newNom)) {
      GMessages.impossibiliteAction("Le nom "+Structure.FORME+" est réservé par le système");
      return null;
    }
    boolean fusion = false;
    ArrayList<? extends Element> elements = getElements(classe, type);
    if (structure.getNomsProps(classe, type).contains(newNom)) {
      int rep = JOptionPane.showOptionDialog(fenetre, "La propriété "+"\""+newNom+"\""+" existe déjà."
              +" Fusionner les deux propriétés ? ", "Propriété existante",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Oui", "Annuler"}, "Oui");
      if (rep==1) return null;
      int nbEcrases = 0;
      for (Element elt : elements) if (!elt.getProp(newNom).isEmpty()
                &&!elt.getProp(newNom).equals(elt.getProp(oldNom))) nbEcrases++;
      if (nbEcrases>0) {
        int rep1 = JOptionPane.showOptionDialog(fenetre, "L'opération de fusion va écraser l'ancienne valeur de la propriété "
                +"\""+newNom+"\""+" pour "+nbEcrases+" élément(s)", "Fusion de deux propriétés",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[]{"Continuer", "Annuler"}, "Continuer");
        if (rep1==1) return null;
      }
      fusion = true;
    }
    if (fusion) structure.fusionnerProp(classe, type, oldNom, newNom);
    else structure.renommerProp(classe, type, oldNom, newNom);
    for (Element elt : elements) {
      String val = elt.getProp(oldNom);
      if (!val.isEmpty()) elt.putProp(newNom, val);
      elt.getProps().remove(oldNom);
    }
    if (fusion)
      fireMessage(new StructureEvent(this, TypeModifStructure.FUSION_PROP, classe, type, oldNom, newNom));
    else
      fireMessage(new StructureEvent(this, TypeModifStructure.RENOM_PROP, classe, type, oldNom, newNom));
    return newNom;
  }
  public String saisirRenommerVal(Container fenetre, Class<? extends Element> classe, String type,
          String prop, String oldNom) {
    String newNom = (String) JOptionPane.showInputDialog(fenetre, "Nouveau nom pour la valeur "+"\""+oldNom+"\"",
            "Renommer une valeur de la propriété "+"\""+prop+"\"", JOptionPane.PLAIN_MESSAGE, null, null, oldNom);
    if (newNom==null) return null;
    newNom = newNom.trim();
    if (oldNom.equals(newNom)||newNom.isEmpty()) return null;
    boolean fusion = false;
    if (structure.getValeursProp(classe, type, prop).contains(newNom)) {
      int rep = JOptionPane.showOptionDialog(fenetre, "La valeur "+"\""+newNom+"\""+" existe déjà."
              +" Fusionner les deux valeurs ? ", "Valeur existante",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Oui", "Annuler"}, "Oui");
      if (rep==1) return null;
      fusion = true;
    }
    structure.renommerVal(classe, type, prop, oldNom, newNom);
    ArrayList<? extends Element> elements = getElements(classe, type);
    for (Element elt : elements) {
      String val = elt.getProp(prop);
      if (oldNom.equals(val)) {
        elt.putProp(prop, newNom);
      }
    }
    if (fusion)
      fireMessage(new StructureEvent(this, TypeModifStructure.FUSION_VALEUR, classe, type, prop, oldNom, newNom));
    else
      fireMessage(new StructureEvent(this, TypeModifStructure.RENOM_VALEUR, classe, type, prop, oldNom, newNom));
    return newNom;
  }
  public boolean saisirSupprimerType(Container fenetre, Class<? extends Element> classe, String type) {
    Element[] elements = new Element[0];
    if (classe==Unite.class) {
      if (unites.containsKey(type))
        elements = unites.get(type).toArray(elements);
    } else if (classe==Relation.class) {
      if (unites.containsKey(type))
        elements = relations.get(type).toArray(elements);
    } else if (classe==Schema.class) {
      if (unites.containsKey(type))
        elements = schemas.get(type).toArray(elements);
    }
    if (elements.length>0) {
      int nbSupprimes = elements.length;
      int rep = JOptionPane.showOptionDialog(fenetre, "La suppresion du type  "+"\""+type+"\""
              +" va entraîner la suppression "+(nbSupprimes==1 ? "d'un élément" : "de "+nbSupprimes+" éléments"),
              "Suppression "+GVisu.getNomDeClasse(classe)+"s",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Continuer", "Annuler"}, "Continuer");
      if (rep==1) return false;
      if (classe==Unite.class) {
        for (Element elt : elements) supUnite((Unite) elt);
        unites.remove(type);
      }
      if (classe==Relation.class) {
        for (Element elt : elements) supRelation((Relation) elt);
        relations.remove(type);
      }
      if (classe==Schema.class) {
        for (Element elt : elements) supSchema((Schema) elt);
        schemas.remove(type);
      }
    }
    structure.supprimerType(classe, type);
    fireMessage(new StructureEvent(this, TypeModifStructure.SUPPR_TYPE, classe, type));
    return true;
  }
  public boolean saisirSupprimerProp(Container fenetre, Class<? extends Element> classe, String type, String oldNom) {
    ArrayList<? extends Element> elements = getElements(classe, type);
    int nbSup = 0;
    for (Element elt : elements) if (!elt.getProp(oldNom).isEmpty()) nbSup++;
    if (nbSup>0) {
      int rep = JOptionPane.showOptionDialog(fenetre, "La propriété "+"\""+oldNom+"\""+" était instanciée pour "
              +(nbSup==1 ? "un élément" : nbSup+" éléments"), "Suppression d'une propriété",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Continuer", "Annuler"}, "Continuer");
      if (rep==1) return false;
    }
    for (Element elt : elements) elt.supProp(oldNom);
    structure.supprimerProp(classe, type, oldNom);
    fireMessage(new StructureEvent(this, TypeModifStructure.SUPPR_PROP, classe, type, oldNom));
    return true;
  }
  public boolean saisirSupprimerVal(Container fenetre, Class<? extends Element> classe, String type,
          String prop, String oldNom) {
    ArrayList<? extends Element> elements = getElements(classe, type);
    int nbSup = 0;
    for (Element elt : elements) if (oldNom.equals(elt.getProp(prop)))
        nbSup++;
    if (nbSup>0) {
      int rep = JOptionPane.showOptionDialog(fenetre, "La valeur "+"\""+oldNom+"\""+" était attribuée à "
              +(nbSup==1 ? "un élément" : nbSup+" éléments"), "Suppression d'une valeur",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Continuer", "Annuler"}, "Continuer");
      if (rep==1) return false;
    }
    structure.supprimerVal(classe, type, prop, oldNom);
    for (Element elt : elements) if (oldNom.equals(elt.getProp(prop)))
        elt.supProp(prop);
    fireMessage(new StructureEvent(this, TypeModifStructure.SUPPR_VALEUR, classe, type, prop, oldNom));
    return true;
  }
  public boolean saisirDispatcherProp(Container fenetre,
          Class<? extends Element> classe, String oldtype, String oldprop,
          String[] newtypes, String[] props, String[][] propsvals) {
    HashSet<String> types = structure.getTypes(classe);
    for (String type : newtypes) if (types.contains(type)) {
        GMessages.impossibiliteAction("Le type "+type+" existe déjà");
        return false;
      }
    ArrayList<? extends Element> elements = getElements(classe, oldtype);
    for (Element elt : elements) if (elt.getProp(oldprop).isEmpty()) {
        GMessages.impossibiliteAction("Certains éléments de type "+oldtype
                +" ne possèdent pas de valeur pour la propriété "+oldprop);
        return false;
      }
    structure.supprimerType(classe, oldtype);
    for (String type : newtypes) {
      structure.ajouterType(classe, type);
      for (int i = 0; i<props.length; i++) {
        structure.ajouterProp(classe, type, props[i]);
        for (String val : propsvals[i])
          structure.ajouterVal(classe, type, props[i], val);
      }
    }
    if (classe==Unite.class) {
      for (String type : newtypes) unites.put(type, new ArrayList<Unite>());
      for (Unite unit : unites.get(oldtype)) {
        String newtype = unit.getProp(oldprop);
        unit.supProp(oldprop);
        unit.setType(newtype);
        unites.get(newtype).add(unit);
      }
      unites.remove(oldtype);
    } else if (classe==Relation.class) {
      for (String type : newtypes)
        relations.put(type, new ArrayList<Relation>());
      for (Relation rel : relations.get(oldtype)) {
        String newtype = rel.getProp(oldprop);
        rel.supProp(oldprop);
        rel.getElt1().oteRelationSortante(rel);
        rel.getElt2().oteRelationEntrante(rel);
        rel.setType(newtype);
        rel.getElt1().addRelationSortante(rel);
        rel.getElt2().addRelationEntrante(rel);
        relations.get(newtype).add(rel);
      }
      relations.remove(oldtype);
    } else if (classe==Schema.class) {
      for (String type : newtypes) schemas.put(type, new ArrayList<Schema>());
      for (Schema sch : schemas.get(oldtype)) {
        String newtype = sch.getProp(oldprop);
        sch.supProp(oldprop);
        for (Element elt : sch.getContenu()) elt.oteSchemaContenant(sch);
        sch.setType(newtype);
        for (Element elt : sch.getContenu()) elt.addSchemaContenant(sch);
        schemas.get(newtype).add(sch);
      }
      schemas.remove(oldtype);
    }
    fireMessage(new StructureEvent(this, TypeModifStructure.SUPPR_TYPE,
            classe, oldtype));
    for (String type : newtypes) {
      fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_TYPE_ET_ELEMENTS,
              classe, type));
      for (String prop : props)
        fireMessage(new StructureEvent(this, TypeModifStructure.AJOUT_PROP,
                classe, type, prop));
    }
    return true;
  }
  public void clearAnnot() {
    finParagraphes.clear();
    unites.clear();
    relations.clear();
    schemas.clear();
  }
  public void clearAll() {
    clearAnnot();
    structure.clear();
    texte = "";
  }
  public boolean isModifie() {
    return modifie;
  }
  public boolean isVide() {
    return texte.isEmpty()&&structure.isVide();
  }
  public void fermer() {
    if (isVide()) return;
    clearAnnot();
    structure.clear();
    texte = "";
    fireMessage(new Message(this, TypeMessage.CLEAR_CORPUS));
  }
  public void fermerConserverStructure() {
    if (texte.isEmpty()) return;
    clearAnnot();
    texte = "";
    fireMessage(new Message(this, TypeMessage.CLEAR_CORPUS));
  }
  @SuppressWarnings("unchecked")
  public void lireJava(ObjectInputStream ois, boolean batch) throws IOException, ClassNotFoundException, ClassCastException {
    texte = (String) ois.readObject();
    finParagraphes = (ArrayList<Integer>) ois.readObject();
    unites = (HashMap<String, ArrayList<Unite>>) ois.readObject();
    relations = (HashMap<String, ArrayList<Relation>>) ois.readObject();
    schemas = (HashMap<String, ArrayList<Schema>>) ois.readObject();
    structure = Structure.lireJava(ois);
    if (!batch) fireMessage(new Message(this, TypeMessage.NEW_CORPUS));
  }
  @SuppressWarnings("unchecked")
  public void fusionnerAnnotJava(ObjectInputStream ois) throws IOException, ClassNotFoundException, ClassCastException {
    String texte1 = (String) ois.readObject();
    ArrayList<Integer> finParagraphes1 = (ArrayList<Integer>) ois.readObject();
    if (!texte1.equals(texte)||!finParagraphes1.equals(finParagraphes)) {
      JOptionPane.showMessageDialog(null, "Le texte du corpus à fusionner est différent du texte en cours",
              "Fusion d'annotations impossible.", JOptionPane.ERROR_MESSAGE);
      return;
    }
    HashMap<String, ArrayList<Unite>> unites1 = (HashMap<String, ArrayList<Unite>>) ois.readObject();
    HashMap<String, ArrayList<Relation>> relations1 = (HashMap<String, ArrayList<Relation>>) ois.readObject();
    HashMap<String, ArrayList<Schema>> schemas1 = (HashMap<String, ArrayList<Schema>>) ois.readObject();
    Structure structure1 = Structure.lireJava(ois);
    fusionnerStructureLue(structure1);
    HashSet<Unite> unitesAajouter = new HashSet<Unite>();
    HashMap<Unite, Unite> unitesAfusionner = new HashMap<Unite, Unite>();
    int nbUnitesEcrasees = checkFusionnerUnites(unites1, unitesAajouter, unitesAfusionner);
    HashSet<Relation> relationsAajouter = new HashSet<Relation>();
    HashMap<Relation, Relation> relationsAfusionner = new HashMap<Relation, Relation>();
    int nbRelationsEcrasees = checkFusionnerRelations(relations1, relationsAajouter, relationsAfusionner);
    HashSet<Schema> schemasAajouter = new HashSet<Schema>();
    HashMap<Schema, Schema> schemasAfusionner = new HashMap<Schema, Schema>();
    int nbSchemasEcrases = checkFusionnerSchemas(schemas1, schemasAajouter, schemasAfusionner);
    int nbEcrases = nbUnitesEcrasees+nbRelationsEcrasees+nbSchemasEcrases; // à améliorer
    if (nbUnitesEcrasees>0||nbRelationsEcrasees>0||nbSchemasEcrases>0) {
      int rep = JOptionPane.showOptionDialog(null, "L'opération de fusion va écraser d'anciennes valeurs de propriétés "
              +" pour "+nbEcrases+" élément(s)", "Fusion d'annotations",
              JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object[]{"Continuer", "Annuler"}, "Continuer");
      if (rep==1) return;
    }
    for (Unite unit : unitesAajouter) addUniteLue(unit);
    for (Unite unit : unitesAfusionner.keySet())
      for (String prop : unit.getProps().keySet())
        unitesAfusionner.get(unit).putProp(prop, unit.getProp(prop));
    for (Relation rel : relationsAajouter) {
      fusionnerElementsRelation(rel, unitesAfusionner, relationsAfusionner, schemasAfusionner);
      addRelationLue(rel);
    }
    for (Relation rel : relationsAfusionner.keySet())
      for (String prop : rel.getProps().keySet())
        relationsAfusionner.get(rel).putProp(prop, rel.getProp(prop));
    for (Schema sch : schemasAajouter) {
      fusionnerElementsSchema(sch, unitesAfusionner, relationsAfusionner, schemasAfusionner);
      addSchemaLu(sch);
    }
    for (Schema sch : schemasAfusionner.keySet())
      for (String prop : sch.getProps().keySet())
        unitesAfusionner.get(sch).putProp(prop, sch.getProp(prop));
    fireMessage(new Message(this, TypeMessage.NEW_CORPUS));
  }
  @SuppressWarnings("unchecked")
  public void concatenerCorpusJava(ObjectInputStream ois) throws IOException, ClassNotFoundException, ClassCastException {
    String texte1 = (String) ois.readObject();
    ArrayList<Integer> finParagraphes1 = (ArrayList<Integer>) ois.readObject();
    int offset = texte.length();
    texte += texte1;
    for (int finP : finParagraphes1) finParagraphes.add(finP+offset);
    verifierFinParagrapheFinTexte();
    HashMap<String, ArrayList<Unite>> unites1 = (HashMap<String, ArrayList<Unite>>) ois.readObject();
    HashMap<String, ArrayList<Relation>> relations1 = (HashMap<String, ArrayList<Relation>>) ois.readObject();
    HashMap<String, ArrayList<Schema>> schemas1 = (HashMap<String, ArrayList<Schema>>) ois.readObject();
    Structure structure1 = Structure.lireJava(ois);
    fusionnerStructureLue(structure1);
    for (String type : unites1.keySet()) for (Unite unit : unites1.get(type)) {
        unit.setDeb(unit.getDeb()+offset);
        unit.setFin(unit.getFin()+offset);
        addUniteLue(unit);
      }
    for (String type : relations1.keySet())
      for (Relation rel : relations1.get(type)) {
        addRelationLue(rel);
      }
    for (String type : schemas1.keySet())
      for (Schema sch : schemas1.get(type)) {
        addSchemaLu(sch);
      }
  }
  @SuppressWarnings("unchecked")
  public void extraireEtConcatenerCorpusJava(ObjectInputStream ois,
          String nomDocument, String type)
          throws IOException, ClassNotFoundException, ClassCastException {
//    System.out.println(nomDocument);
    String texte1 = (String) ois.readObject();
    ArrayList<Integer> finParagraphes1 = (ArrayList<Integer>) ois.readObject();
    HashMap<String, ArrayList<Unite>> unites1 = (HashMap<String, ArrayList<Unite>>) ois.readObject();
    if (unites1.containsKey(type)) {
      int indFinPar1 = -1;
      int offset1 = -1;
      int offset = -1;
      for (Unite unit : unites1.get(type)) {
        int finUnit = unit.getFin();
        if (indFinPar1<0||finUnit>finParagraphes1.get(indFinPar1)) {
          do indFinPar1++;
          while (finUnit>finParagraphes1.get(indFinPar1));
          offset1 = indFinPar1==0 ? 0 : finParagraphes1.get(indFinPar1-1);
          offset = texte.length();
          texte += texte1.substring(offset1, finParagraphes1.get(indFinPar1));
          finParagraphes.add(texte.length());
        }
        unit.setDeb(unit.getDeb()-offset1+offset);
        unit.setFin(unit.getFin()-offset1+offset);
        unit.putProp("sourceFile", nomDocument);
        addUniteLue(unit);
      }
    }
  }
  public void ecrireJava(ObjectOutputStream oos) throws IOException {
    oos.writeObject(texte);
    oos.writeObject(finParagraphes);
    oos.writeObject(unites);
    oos.writeObject(relations);
    oos.writeObject(schemas);
    structure.ecrireJava(oos);
    modifie = false;
  }
  @SuppressWarnings("unchecked")
  public void fusionnerStructureLue(Structure structureLue) {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class
    }) {
      for (String typeLu : structureLue.getTypes(classe)) {
        if (!structure.getTypes(classe).contains(typeLu))
          ajouterType(classe, typeLu);
        for (String propLue : structureLue.getNomsProps(classe, typeLu)) {
          if (!structure.getNomsProps(classe, typeLu).contains(propLue))
            structure.ajouterProp(classe, typeLu, propLue);
          for (String valLue : structureLue.getValeursProp(classe, typeLu, propLue))
            if (!structure.getValeursProp(classe, typeLu, propLue).contains(valLue))
              structure.ajouterVal(classe, typeLu, propLue, valLue);
          String valdefLue = structureLue.getValeurParDefaut(classe, typeLu, propLue);
          String valdef = structure.getValeurParDefaut(classe, typeLu, propLue);
          if (!valdefLue.isEmpty()&&!valdefLue.equals(valdef)) {
            structure.modifierValParDefaut(classe, typeLu, propLue, valdefLue);
          }
        }
      }
    }
  }
  public void ecrireStructureJava(ObjectOutputStream oos) throws IOException {
    structure.ecrireJava(oos);
  }
  public Structure lireStructureJava(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    return Structure.lireJava(ois);
  }
  public void lireTexte(BufferedReader text) throws IOException {
    String parag;
    String newtexte = "";
    ArrayList<Integer> newfinParag = new ArrayList<Integer>();
    while ((parag = text.readLine())!=null) {
      newtexte += parag;
      newfinParag.add(newtexte.length());
    }
    texte = newtexte;
    finParagraphes = newfinParag;
    fireMessage(new Message(this, TypeMessage.NEW_CORPUS));
  }
  public void newCorpusLu() {
    fireMessage(new Message(this, TypeMessage.NEW_CORPUS));
  }
}
