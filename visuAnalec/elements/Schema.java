/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.elements;

import java.util.*;

/**
 *
 * @author Bernard
 */
public class Schema extends Element {
  private HashSet<Element> contenu = new HashSet<Element>();
  public Schema() {
    super();
  }
  public Schema(String type) {
    super(type);
  }
  public boolean ajouter(Element elt) {
    // retourne false ssi pb de recursivité
    if (elt==null) return true;
    // à remplacer lors d'un chgt de version modifiant la signature de la classe
// Element par :if (elt.contientSchemaSousJacent(this)) return false;
    if (contientSchemaSousJacent(elt, this)) return false;
    contenu.add(elt);
    return true;
  }
  public boolean oter(Element elt) {
    return contenu.remove(elt);
  }
  public HashSet<Element> getContenu() {
    return contenu;
  }
  public boolean contient(Element elt) {
    return contenu.contains(elt);
  }
  protected HashSet<Unite> getUnitesSousjacentesNonTriees() {
    HashSet<Unite> units = new HashSet<Unite>();
    for (Element elt : contenu) {
      units.addAll(elt.getUnitesSousjacentesNonTriees());
    }
    return units;
  }
//  boolean contientSchemaSousJacent(Schema sch) {
//    if (coincide(sch)) return true;
//    for (Element elt : contenu) if (elt.contientSchemaSousJacent(sch))
//        return true;
//    return false;
//  }
  private static boolean contientSchemaSousJacent(Element elt, Schema sch) {
    // Solution provisoire en attendant le chgt de version modifiant la signature
    // de la classe Element
    if (elt.getClass()==Unite.class) return false;
    if (elt.getClass()==Relation.class) {
      Relation rel = (Relation) elt;
      return contientSchemaSousJacent(rel.getElt1(), sch)
              ||contientSchemaSousJacent(rel.getElt2(), sch);
    }
    if (elt.getClass()==Schema.class) {
      Schema sch1 = (Schema) elt;
      if (sch1.coincide(sch)) return true;
      for (Element elt1 : sch1.contenu) if (contientSchemaSousJacent(elt1, sch))
          return true;
      return false;
    }
    throw new IllegalArgumentException("Element de classe non identifiée");
  }
  public boolean coincide(Element elt) {
    if (elt.getClass()!=Schema.class) return false;
    Schema sch = (Schema) elt;
    if (!getType().equals(sch.getType())) return false;
    if (contenu.size()!=sch.getContenu().size()) return false;
    Element[] contenu1 = contenu.toArray(new Element[0]);
    Arrays.sort(contenu1);
    Element[] contenu2 = sch.getContenu().toArray(new Element[0]);
    Arrays.sort(contenu2);
    for (int i = 0; i<contenu1.length; i++)
      if (!contenu1[i].coincide(contenu2[i])) return false;
    return true;
  }
}
