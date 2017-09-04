/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.elements;

import java.io.Serializable;
import java.util.*;
import java.util.ArrayList;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public abstract class Element implements Serializable, Comparable {
  private String type;
  private HashMap<String, String> props = new HashMap<String, String>();
  private HashMap<String, ArrayList<Relation>> relationsEntrantes = new HashMap<String, ArrayList<Relation>>();
  private HashMap<String, ArrayList<Relation>> relationsSortantes = new HashMap<String, ArrayList<Relation>>();
  private HashMap<String, ArrayList<Schema>> schemasContenants = new HashMap<String, ArrayList<Schema>>();
  Element() {
  }
  Element(String type) {
    this.type = type;
  }
  @Override
  public String toString() {
    String dump = "ELEMENT "+type;
    dump += "\n\tPROPS : ";
    for (String prop : props.keySet()) {
      dump += "\n\t\t"+prop+" : "+props.get(prop);
    }
    dump += "\n\tRELATIONS ENTRANTES : ";
    for (String typ : relationsEntrantes.keySet()) {
      dump += "\n\t\t"+typ+" : ";
      for (Relation rel : relationsEntrantes.get(typ)) {
        dump += rel.getElt1().getType();
      }
    }
    dump += "\n\tRELATIONS SORTANTES : ";
    for (String typ : relationsSortantes.keySet()) {
      dump += "\n\t\t"+typ+" : ";
      for (Relation rel : relationsSortantes.get(typ)) {
        dump += rel.getElt2().getType();
      }
    }
    return dump;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public HashMap<String, String> getProps() {
    return props;
  }
  public String getProp(String prop) {
    if (props.get(prop)==null) {
      return "";
    }
    return props.get(prop);
  }
  public void putProp(String prop, String valeur) {
    props.put(prop, valeur);
  }
  public void supProp(String prop) {
    props.remove(prop);
  }
  public void addRelationEntrante(Relation rel) {
    if (!relationsEntrantes.containsKey(rel.getType())) {
      relationsEntrantes.put(rel.getType(), new ArrayList<Relation>());
    }
    relationsEntrantes.get(rel.getType()).add(rel);
  }
  public void oteRelationEntrante(Relation rel) {
    relationsEntrantes.get(rel.getType()).remove(rel);
  }
  public void addRelationSortante(Relation rel) {
    if (!relationsSortantes.containsKey(rel.getType())) {
      relationsSortantes.put(rel.getType(), new ArrayList<Relation>());
    }
    relationsSortantes.get(rel.getType()).add(rel);
  }
  public void oteRelationSortante(Relation rel) {
    relationsSortantes.get(rel.getType()).remove(rel);
  }
  public void addSchemaContenant(Schema sch) {
    if (!schemasContenants.containsKey(sch.getType())) {
      schemasContenants.put(sch.getType(), new ArrayList<Schema>());
    }
    schemasContenants.get(sch.getType()).add(sch);
  }
  public void oteSchemaContenant(Schema sch) {
    schemasContenants.get(sch.getType()).remove(sch);
  }
  public ArrayList<Relation> getRelations() {
    ArrayList<Relation> rels = new ArrayList<Relation>();
    for (String typerel : relationsEntrantes.keySet()) {
      rels.addAll(relationsEntrantes.get(typerel));
    }
    for (String typerel : relationsSortantes.keySet()) {
      rels.addAll(relationsSortantes.get(typerel));
    }
    return rels;
  }
  public ArrayList<Schema> getSchemas() {
    ArrayList<Schema> schs = new ArrayList<Schema>();
    for (String typesch : schemasContenants.keySet()) {
      schs.addAll(schemasContenants.get(typesch));
    }
    return schs;
  }
  public Element getElementSuivant(Jalon jalon) {
    switch (jalon.origine) {
      case ELT_0:
        return this;
      case REL_SORTANTE:
        if (!relationsSortantes.containsKey(jalon.type)||relationsSortantes.get(jalon.type).isEmpty()) {
          return null;
        }
        return relationsSortantes.get(jalon.type).get(0);
      case REL_ENTRANTE:
        if (!relationsEntrantes.containsKey(jalon.type)||relationsEntrantes.get(jalon.type).isEmpty()) {
          return null;
        }
        return relationsEntrantes.get(jalon.type).get(0);
      case SCH_CONTENANT:
        if (!schemasContenants.containsKey(jalon.type)||schemasContenants.get(jalon.type).isEmpty()) {
          return null;
        }
        return schemasContenants.get(jalon.type).get(0);
      default:  // les cas ELT_1 et ELT_2 sont traités par la classe Relation
        throw new UnsupportedOperationException("Pas encore implémenté");
    }
  }
  protected abstract HashSet<Unite> getUnitesSousjacentesNonTriees();
  public Unite[] getUnitesSousjacentes() { // triées
    Unite[] units = getUnitesSousjacentesNonTriees().toArray(new Unite[0]);
    Arrays.sort(units);
    return units;
  }
  public Unite getUnite0() {
    Unite[] units = getUnitesSousjacentes();
    if (units.length==0) return null;
    return units[0];
  }
  public static Unite[] getUnites0(Element[] elts) {
    Unite[] units = new Unite[elts.length];
    for (int i = 0; i<elts.length; i++) units[i] = elts[i].getUnite0();
    return units;
  }
  public static Unite[] getUnitesSousjacentes(Element[] elts) { // triées : est-ce nécessaire ??
    ArrayList<Unite> lunits = new ArrayList<Unite>();
    for (Element elt : elts)
      lunits.addAll(elt.getUnitesSousjacentesNonTriees());
    Unite[] units = lunits.toArray(new Unite[0]);
    Arrays.sort(units);
    return units;
  }
  public static Element trouverElementParUniteSousjacente(Element[] elements, Unite unite) { // prend le premier
    for (Element elt : elements) {
      int i = Arrays.binarySearch(elt.getUnitesSousjacentes(), unite);
      if (i>=0) return elt;
    }
    return null;
  }
  public static ArrayList<Element> trouverElementsParUniteSousjacente(Element[] elements, Unite unite) {
    ArrayList<Element> elts = new ArrayList<Element>();
    for (Element elt : elements) {
      int i = Arrays.binarySearch(elt.getUnitesSousjacentes(), unite);
      if (i>=0) elts.add(elt);
    }
    return elts;
  }
  public int compareTo(Object obj) {
    if (obj.getClass().getSuperclass()!=Element.class) {
      throw new ClassCastException("Comparaison impossible : "+obj.toString()+" n'est pas une instance d'Elément");
    }
    Unite[] unites1 = getUnitesSousjacentes();
    Unite[] unites2 = ((Element) obj).getUnitesSousjacentes();
    int comp;
    for (int i = 0; i<unites1.length; i++) {
      if (i==unites2.length) return 1;
      if ((comp = unites1[i].compareTo(unites2[i]))!=0) return comp;
    }
    if (unites1.length<unites2.length) return -1;
    return Integer.signum(hashCode()-obj.hashCode());
  }
  abstract public boolean coincide(Element elt);
//  abstract boolean contientSchemaSousJacent(Schema sch);
}
