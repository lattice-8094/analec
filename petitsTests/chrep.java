/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petitsTests;

import java.io.*;
import visuAnalec.util.*;

/**
 *
 * @author Bernard
 */
public class chrep {
    public static void main(String[] strs) throws Exception {
File dir = new File("E:/Programmation/Analec/Données Agoravox/AgoravoxArticles");
File dir1 = new File("E:/Programmation/Analec/Données Agoravox/fichiersGlozz");
File[] dirs = dir.listFiles();
File fic = null;
for(File d: dirs) {
fic = new File(d, d.getName()+".aa");
fic.renameTo(new File(dir1, fic.getName()));
fic = new File(d, d.getName()+".ac");
fic.renameTo(new File(dir1, fic.getName()));
    }

    } 
}
