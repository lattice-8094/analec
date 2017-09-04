/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.util;

import java.util.*;
import visuAnalec.elements.*;

/**
 *
 * @author Bernard
 */
public abstract class Tableaux {
  public static int compterValeursVraies(boolean[] bls) {
    int nb = 0;
    for(boolean b: bls) if(b) nb++;
    return nb;
  }
  public static String[] insererString(String[] strs, String str) {
    // renvoie un tableau trié
    String[] strs1 = insererStringEnTete(strs, str);
    Arrays.sort(strs1);
    return strs1;
  }
  public static String[] insererStringEnTete(String[] strs, String str) {
    String[] strs1 = new String[strs.length + 1];
    System.arraycopy(strs, 0, strs1, 1, strs.length);
    strs1[0] = str;
    return strs1;
  }
  static String[] oterString(String[] strs, String str) {
    // strs doit être triée
    int ind = Arrays.binarySearch(strs, str);
    if (ind < 0) return strs;
    return oterString(strs, ind);
  }
  public static String[] oterString(String[] strs, int ind) {
    String[] strs1 = new String[strs.length - 1];
    System.arraycopy(strs, 0, strs1, 0, ind);
    System.arraycopy(strs, ind + 1, strs1, ind, strs.length - ind - 1);
    return strs1;
  }
  public static String[] oterStrings(String[] strs, ArrayList<Integer> inds) {
    //  inds doit être croissant
    String[] strs1 = new String[strs.length - inds.size()];
    inds.add(strs.length);
    System.arraycopy(strs, 0, strs1, 0, inds.get(0));
    for (int i = 1; i < inds.size(); i++)
      System.arraycopy(strs, inds.get(i - 1) + 1, strs1, inds.get(i - 1) - i + 1, inds.get(i) - inds.get(i - 1) - 1);
    return strs1;
  }
//  public static String[][] oterStrings(String[][] strs, ArrayList<Integer> inds1, ArrayList<Integer> inds2) {
//    //  inds1 et inds2 doivent être de même taille
//    String[] strs1 = new String[strs.length - inds.size()];
//    inds.add(strs.length);
//    System.arraycopy(strs, 0, strs1, 0, inds.get(0));
//    for (int i = 1; i < inds.size(); i++)
//      System.arraycopy(strs, inds.get(i - 1) + 1, strs1, inds.get(i - 1) - i + 1, inds.get(i) - inds.get(i - 1) - 1);
//    return strs1;
//  }
  public static Element[] insererElementEnTete(Element[] elts, Element elt) {
    Element[] elts1 = new Element[elts.length + 1];
    System.arraycopy(elts, 0, elts1, 1, elts.length);
    elts1[0] = elt;
    return elts1;
  }
  public static Element[] oterElements(Element[] elts, ArrayList<Integer> inds) {
    //  inds doit être croissant
    Element[] elts1 = new Element[elts.length - inds.size()];
    inds.add(elts.length);
    System.arraycopy(elts, 0, elts1, 0, inds.get(0));
    for (int i = 1; i < inds.size(); i++)
      System.arraycopy(elts, inds.get(i - 1) + 1, elts1, inds.get(i - 1) - i + 1, inds.get(i) - inds.get(i - 1) - 1);
    return elts1;
  }
  public static Relation[] oterRelations(Relation[] elts, ArrayList<Integer> inds) {
    //  inds doit être croissant
    Relation[] elts1 = new Relation[elts.length - inds.size()];
    inds.add(elts.length);
    System.arraycopy(elts, 0, elts1, 0, inds.get(0));
    for (int i = 1; i < inds.size(); i++)
      System.arraycopy(elts, inds.get(i - 1) + 1, elts1, inds.get(i - 1) - i + 1, inds.get(i) - inds.get(i - 1) - 1);
    return elts1;
  }
}
