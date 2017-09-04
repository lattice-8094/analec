/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petitsTests;

import java.util.Arrays;

/**
 *
 * @author Bernard
 */
public class petittest {
  final static String interdits = "[!\\\"#\\$%&'\\(\\)\\*+,/;<=>\\?"
          +"@\\[\\\\\\]\\^`\\{\\|\\}\\~]";
  public static void main(String[] args) {
for(int c=32; c<1024; c++) {
  System.out.println(c+": |"+((char)c)+"| : "+Character.valueOf((char)c).getType(c));
  
}
  }
}
