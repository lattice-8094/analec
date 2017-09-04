/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec.donnees;

import java.io.*;
import java.util.*;
import visuAnalec.elements.*;

/**
 *
 * @author Bernard
 */
public class Structure implements Serializable {
  public final static String FORME = "#forme#";
  private HashMap<Class<? extends Element>, HashSet<String>> types =
          new HashMap<Class<? extends Element>, HashSet<String>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashSet<String>>> nomsProps =
          new HashMap<Class<? extends Element>, HashMap<String, HashSet<String>>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashSet<String>>>> valeursProps =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashSet<String>>>>();
  private HashMap<Class<? extends Element>, HashMap<String, HashMap<String, String>>> valeursParDefaut =
          new HashMap<Class<? extends Element>, HashMap<String, HashMap<String, String>>>();

  @SuppressWarnings("unchecked")
  public Structure() {
    for (Class<? extends Element> classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      types.put(classe, new HashSet<String>());
      nomsProps.put(classe, new HashMap<String, HashSet<String>>());
      valeursProps.put(classe, new HashMap<String, HashMap<String, HashSet<String>>>());
      valeursParDefaut.put(classe, new HashMap<String, HashMap<String, String>>());
    }
  }

  @Override
  public String toString() {
    String dump = "STRUCTURE :";
    dump += "\n\tUNITES : ";
    dump += "\n\t\tTYPES : ";
    for (String type : types.get(Unite.class)) {
      dump += type+"; ";
    }
    dump += "\n\t\tNOMSPROP : ";
    for (String type : nomsProps.get(Unite.class).keySet()) {
      dump += "\n\t\t\t"+type+" : ";
      for (String prop : nomsProps.get(Unite.class).get(type)) {
        dump += prop+"; ";
      }
    }
    dump += "\n\t\tVALEURSPROP : ";
    for (String type : valeursProps.get(Unite.class).keySet()) {
      dump += "\n\t\t\t"+type+" : ";
      for (String prop : valeursProps.get(Unite.class).get(type).keySet()) {
        dump += "\n\t\t\t\t"+prop+" : ";
        for (String val : valeursProps.get(Unite.class).get(type).get(prop)) {
          dump += val+"; ";
        }
      }
    }

    dump += "\n\tRELATIONS : ";
    dump += "\n\t\tTYPES : ";
    for (String type : types.get(Relation.class)) {
      dump += type+"; ";
    }
    dump += "\n\t\tNOMSPROP : ";
    for (String type : nomsProps.get(Relation.class).keySet()) {
      dump += "\n\t\t\t"+type+" : ";
      for (String prop : nomsProps.get(Relation.class).get(type)) {
        dump += prop+"; ";
      }
    }
    dump += "\n\t\tVALEURSPROP : ";
    for (String type : valeursProps.get(Relation.class).keySet()) {
      dump += "\n\t\t\t"+type+" : ";
      for (String prop : valeursProps.get(Relation.class).get(type).keySet()) {
        dump += "\n\t\t\t\t"+prop+" : ";
        for (String val : valeursProps.get(Relation.class).get(type).get(prop)) {
          dump += val+"; ";
        }
      }
    }

    dump += "\n\tSCHEMAS : ";
    dump += "\n\t\tTYPES : ";
    for (String type : types.get(Schema.class)) {
      dump += type+"; ";
    }
    dump += "\n\t\tNOMSPROP : ";
    for (String type : nomsProps.get(Schema.class).keySet()) {
      dump += "\n\t\t\t"+type+" : ";
      for (String prop : nomsProps.get(Schema.class).get(type)) {
        dump += prop+"; ";
      }
    }
    dump += "\n\t\tVALEURSPROP : ";
    for (String type : valeursProps.get(Schema.class).keySet()) {
      dump += "\n\t\t\t"+type+" : ";
      for (String prop : valeursProps.get(Schema.class).get(type).keySet()) {
        dump += "\n\t\t\t\t"+prop+" : ";
        for (String val : valeursProps.get(Schema.class).get(type).get(prop)) {
          dump += val+"; ";
        }
      }
    }


    return dump;
  }

  public HashSet<String> getNomsProps(Class<? extends Element> classe, String type) {
    if (nomsProps.get(classe).containsKey(type)) {
      return nomsProps.get(classe).get(type);
    }
    return new HashSet<String>();
  }

  public HashSet<String> getValeursProp(Class<? extends Element> classe, String type, String prop) {
    if (valeursProps.get(classe).containsKey(type)&&valeursProps.get(classe).get(type).containsKey(prop)) {
      return (valeursProps.get(classe).get(type).get(prop));
    }
    return new HashSet<String>();
  }

  public String getValeurParDefaut(Class<? extends Element> classe, String type, String prop) {
    if (valeursParDefaut.get(classe).containsKey(type)&&valeursParDefaut.get(classe).get(type).containsKey(prop)) {
      return (valeursParDefaut.get(classe).get(type).get(prop));
    }
    return "";
  }

  public HashSet<String> getTypes(Class<? extends Element> classe) {
    return (types.get(classe));
  }

  public boolean isVide() {
    for (Class classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      if (!types.get(classe).isEmpty()) return false;
    }
    return true;
  }

  void clear() {
    for (Class classe : new Class[]{Unite.class, Relation.class, Schema.class}) {
      types.get(classe).clear();
      nomsProps.get(classe).clear();
      valeursProps.get(classe).clear();
      valeursParDefaut.get(classe).clear();
    }
  }

  public void ajouterVal(Class<? extends Element> classe, String type, String prop, String val) {
    valeursProps.get(classe).get(type).get(prop).add(val);
  }

  void renommerVal(Class<? extends Element> classe, String type, String prop, String oldNom, String newNom) {
    valeursProps.get(classe).get(type).get(prop).add(newNom);
    valeursProps.get(classe).get(type).get(prop).remove(oldNom);
    if (oldNom.equals(valeursParDefaut.get(classe).get(type).get(prop))) {
      valeursParDefaut.get(classe).get(type).put(prop, newNom);
    }
  }

  void supprimerVal(Class<? extends Element> classe, String type, String prop, String oldNom) {
    valeursProps.get(classe).get(type).get(prop).remove(oldNom);
    if (oldNom.equals(valeursParDefaut.get(classe).get(type).get(prop))) {
      valeursParDefaut.get(classe).get(type).put(prop, "");
    }
  }

  public void modifierValParDefaut(Class<? extends Element> classe, String type, String prop, String val) {
    valeursParDefaut.get(classe).get(type).put(prop, val);
  }

  public void ajouterProp(Class<? extends Element> classe, String type, String prop) {
    nomsProps.get(classe).get(type).add(prop);
    valeursProps.get(classe).get(type).put(prop, new HashSet<String>());
    valeursParDefaut.get(classe).get(type).put(prop, "");
  }

  void renommerProp(Class<? extends Element> classe, String type, String oldNom, String newNom) {
    nomsProps.get(classe).get(type).add(newNom);
    nomsProps.get(classe).get(type).remove(oldNom);
    valeursProps.get(classe).get(type).put(newNom, valeursProps.get(classe).get(type).get(oldNom));
    valeursProps.get(classe).get(type).remove(oldNom);
    valeursParDefaut.get(classe).get(type).put(newNom, valeursParDefaut.get(classe).get(type).get(oldNom));
    valeursParDefaut.get(classe).get(type).remove(oldNom);
  }

  void fusionnerProp(Class<? extends Element> classe, String type, String oldNom, String newNom) {
    nomsProps.get(classe).get(type).remove(oldNom);
    valeursProps.get(classe).get(type).get(newNom).addAll(valeursProps.get(classe).get(type).get(oldNom));
    valeursProps.get(classe).get(type).remove(oldNom);
    if (!valeursParDefaut.get(classe).get(type).get(oldNom).isEmpty()) {
      valeursParDefaut.get(classe).get(type).put(newNom, valeursParDefaut.get(classe).get(type).get(oldNom));
    }
    valeursParDefaut.get(classe).get(type).remove(oldNom);
  }

  void supprimerProp(Class<? extends Element> classe, String type, String prop) {
    nomsProps.get(classe).get(type).remove(prop);
    valeursProps.get(classe).get(type).remove(prop);
    valeursParDefaut.get(classe).get(type).remove(prop);
  }

  @SuppressWarnings("unchecked")
  public void ajouterType(Class<? extends Element> classe, String type) {
    types.get(classe).add(type);
    nomsProps.get(classe).put(type, new HashSet<String>());
    valeursProps.get(classe).put(type, new HashMap<String, HashSet<String>>());
    valeursParDefaut.get(classe).put(type, new HashMap<String, String>());
  }
  // }

  void renommerType(Class<? extends Element> classe, String oldType, String newType) {
    types.get(classe).add(newType);
    types.get(classe).remove(oldType);
    nomsProps.get(classe).put(newType, nomsProps.get(classe).get(oldType));
    nomsProps.get(classe).remove(oldType);
    valeursProps.get(classe).put(newType, valeursProps.get(classe).get(oldType));
    valeursProps.get(classe).remove(oldType);
    valeursParDefaut.get(classe).put(newType, valeursParDefaut.get(classe).get(oldType));
    valeursParDefaut.get(classe).remove(oldType);
  }

  void fusionnerType(Class<? extends Element> classe, String oldType, String newType) {
    types.get(classe).remove(oldType);
    for (String prop : nomsProps.get(classe).get(oldType)) {
      if (!nomsProps.get(classe).get(newType).contains(prop))
        ajouterProp(classe, newType, prop);
      for (String val : valeursProps.get(classe).get(oldType).get(prop))
        if (!valeursProps.get(classe).get(newType).get(prop).contains(val))
          ajouterVal(classe, newType, prop, val);
      String valdef1 = getValeurParDefaut(classe, oldType, prop);
      String valdef = getValeurParDefaut(classe, newType, prop);
      if (!valdef1.isEmpty()&&!valdef1.equals(valdef)) {
        modifierValParDefaut(classe, newType, prop, valdef1);
      }
    }
    nomsProps.get(classe).remove(oldType);
    valeursProps.get(classe).remove(oldType);
    valeursParDefaut.get(classe).remove(oldType);

  }

  void supprimerType(Class<? extends Element> classe, String type) {
    types.get(classe).remove(type);
    nomsProps.get(classe).remove(type);
    valeursProps.get(classe).remove(type);
    valeursParDefaut.get(classe).remove(type);
  }

  void majNewElement(Element element) { // appelé par corpus.addUniteLue, addRelationLue, addSchemaLu
    String type = element.getType();
    Class<? extends Element> classe = element.getClass();
    if (!types.get(classe).contains(type)) {
      ajouterType(classe, type);
    }
    HashMap<String, String> props = element.getProps();
    for (String prop : props.keySet()) {
      if (!nomsProps.get(classe).get(type).contains(prop)) {
        ajouterProp(classe, type, prop);
      }
      String val = props.get(prop);
      if (val!=null&&!val.isEmpty()&&!valeursProps.get(classe).get(type).get(prop).contains(val)) {
        ajouterVal(classe, type, prop, val);
      }
    }
  }

 @SuppressWarnings("unchecked")
   public static Structure lireJava(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    Structure structure = new Structure();
    structure.types = (HashMap<Class<? extends Element>, HashSet<String>>) ois.readObject();
    structure.nomsProps = (HashMap<Class<? extends Element>, HashMap<String, HashSet<String>>>) ois.readObject();
    structure.valeursProps = (HashMap<Class<? extends Element>, HashMap<String, HashMap<String, HashSet<String>>>>) ois.readObject();
    structure.valeursParDefaut = (HashMap<Class<? extends Element>, HashMap<String, HashMap<String, String>>>) ois.readObject();
    return structure;
  }

  public void ecrireJava(ObjectOutputStream oos) throws IOException {
    oos.writeObject(types);
    oos.writeObject(nomsProps);
    oos.writeObject(valeursProps);
    oos.writeObject(valeursParDefaut);
  }
}
