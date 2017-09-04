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
public class Unite extends Element implements Comparable {
  private int deb;
  private int fin;
  public Unite() {
    super();
  }
  public Unite(String type, int deb, int fin) {
    super(type);
    this.deb = deb;
    this.fin = fin;
  }
  public int getDeb() {
    return deb;
  }
  public void setDeb(int deb) {
    this.deb = deb;
  }
  public int getFin() {
    return fin;
  }
  public void setFin(int fin) {
    this.fin = fin;
  }
//  boolean contientSchemaSousJacent(Schema sch) {
//    return false;
//  }
  public boolean coincide(Element elt) {
    if (elt.getClass()!=Unite.class) return false;
    Unite unit = (Unite) elt;
    return getType().equals(unit.getType())&&deb==unit.deb&&fin==unit.fin;
  }
  public int compareTo(Unite unit2) {
    int comp = Integer.signum(deb-unit2.deb);
    if (comp==0) {
      comp = -Integer.signum(fin-unit2.fin);
      if (comp==0) {
        comp = getType().compareTo(unit2.getType());
        if (comp==0) {
          comp = Integer.signum(hashCode()-unit2.hashCode());
        }
      }
    }
    return comp;
  }
  protected HashSet<Unite> getUnitesSousjacentesNonTriees() {
    HashSet<Unite> units = new HashSet<Unite>();
    units.add(this);
    return units;
  }
  @Override
  public Unite getUnite0() {
    return this;
  }
  @Override
  public Unite[] getUnitesSousjacentes() {
    return new Unite[]{this};
  }
}
