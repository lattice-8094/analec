/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.elements;

import java.util.*;
import visuAnalec.vue.Vue.*;

/**
 *
 * @author Bernard
 */
public class Relation extends Element {
  private Element elt1, elt2;
  public Relation() {
    super();
  }
  public Relation(String type, Element elt1, Element elt2) {
    super(type);
    setElt1(elt1);
    setElt2(elt2);
  }
  public Element getElt1() {
    return elt1;
  }
  public void setElt1(Element elt) {
    elt1 = elt;
  }
  public Element getElt2() {
    return elt2;
  }
  public void setElt2(Element elt) {
    elt2 = elt;
  }
  @Override
  public Element getElementSuivant(Jalon jalon) {
    switch (jalon.origine) {
      case ELT_2:
        if (elt2.getClass()==jalon.classe&&elt2.getType().equals(jalon.type))
          return elt2;
        return null;
      case ELT_1:
        if (elt1.getClass()==jalon.classe&&elt1.getType().equals(jalon.type))
          return elt1;
        return null;
      default:
        return super.getElementSuivant(jalon);
    }
  }
  protected HashSet<Unite> getUnitesSousjacentesNonTriees() {
    HashSet<Unite> units = elt1.getUnitesSousjacentesNonTriees();
    units.addAll(elt2.getUnitesSousjacentesNonTriees());
    return units;
  }
//  boolean contientSchemaSousJacent(Schema sch) {
//    return elt1.contientSchemaSousJacent(sch)||elt2.contientSchemaSousJacent(sch);
//  }
  public boolean coincide(Element elt) {
    if (elt.getClass()!=Relation.class) return false;
    Relation rel = (Relation) elt;
    return getType().equals(rel.getType())&&elt1.coincide(rel.elt1)&&elt2.coincide(rel.elt2);
  }
}
