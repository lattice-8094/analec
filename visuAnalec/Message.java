/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visuAnalec;

import java.util.*;
import visuAnalec.elements.*;

/**
 *
 * @author Bernard
 */
public class Message extends EventObject {
  public enum TypeMessage {
    CLEAR_CORPUS,
    NEW_CORPUS,
    MODIF_TEXTE,
    MODIF_ELEMENT,
    MODIF_STRUCTURE,
    MODIF_VUE
  }

  public enum TypeModifElement {
    AJOUT_UNITE,
    SUP_UNITE,
    BORNES_UNITE,
    AJOUT_RELATION,
    SUP_RELATION,
    AJOUT_SCHEMA,
    MODIF_SCHEMA,
    SUP_SCHEMA,
    MODIF_VALEUR,
  }

  public enum TypeModifStructure {
    AJOUT_TYPE_ET_ELEMENTS,
    AJOUT_TYPE,
    AJOUT_PROP,
    AJOUT_FORME_UNITE,
    AJOUT_VALEUR,
    RENOM_TYPE,
    RENOM_PROP,
    RENOM_VALEUR,
    FUSION_TYPE,
    FUSION_PROP,
    FUSION_VALEUR,
    SUPPR_TYPE,
    SUPPR_PROP,
    SUPPR_VALEUR,
    VALEUR_PAR_DEFAUT,
  }

  public enum TypeModifVue {
    EXTRACTION,
    NEW_VUE,
    VUE_DEFAUT,
  }
  TypeMessage type;
  public Message(Object source, TypeMessage type) {
    super(source);
    this.type = type;
  }
  public TypeMessage getType() {
    return type;
  }

  public static class VueEvent extends Message {
    private TypeModifVue modif;
    private Unite unite;
    private String[] args;
    public VueEvent(Object source, TypeModifVue modif, String... args) {
      super(source, TypeMessage.MODIF_VUE);
      this.modif = modif;
      this.args = args;
    }
    public VueEvent(Object source, TypeModifVue modif, Unite unite, String... args) {
      super(source, TypeMessage.MODIF_VUE);
      this.unite = unite;
      this.modif = modif;
      this.args = args;
    }
    public TypeModifVue getModif() {
      return modif;
    }
    public Unite getUnite() {
      return unite;
    }
    public String getArg() {
      return args[0];
    }
    public String getArg(int i) {
      return args[i];
    }
  }

  public static class ElementEvent extends Message {
    private TypeModifElement modif;
    private Element element;
    private String[] args;
    public ElementEvent(Object source, TypeModifElement modif, Element element, String... args) {
      super(source, TypeMessage.MODIF_ELEMENT);
      this.modif = modif;
      this.element = element;
      this.args = args;
    }
    public TypeModifElement getModif() {
      return modif;
    }
    public Element getElement() {
      return element;
    }
    public String getArg() {
      return args[0];
    }
    public String getArg(int i) {
      return args[i];
    }
  }

  public static class StructureEvent extends Message {
    private TypeModifStructure modif;
    private Class<? extends Element> classe;
    private String[] args;
    public StructureEvent(Object source, TypeModifStructure modif, Class<? extends Element> classe, String... args) {
      super(source, TypeMessage.MODIF_STRUCTURE);
      this.modif = modif;
      this.classe = classe;
      this.args = args;
    }
    public TypeModifStructure getModif() {
      return modif;
    }
    public Class<? extends Element> getClasse() {
      return classe;
    }
    public String getArg() {
      return args[0];
    }
    public String getArg(int i) {
      return args[i];
    }
  }
}




