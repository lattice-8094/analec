/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JamaPlus.util;

import java.util.*;

/**
 *
 * @author Bernard
 */
public class DoubleArraysPlus {
  public static void copyReverse(int length, double[] src, int srcStart,
          double[] dest, int destStart) {
    destStart += length-1;
    for (int i = 0; i<length; i++) dest[destStart-i] = src[srcStart+i];
  }

   private static class Couple implements Comparable<Couple> {
    double x1;
    Object x2;

    public int compareTo(Couple c) {
      return Double.compare(x1, c.x1);
    }
  }

  public static void sort2(double[] a1, double[] a2, int n, int istart) {
    // sort the first n elements of a1 and a2, starting at index istart,
    // in increasing order for a1 
    // (elements with indexes < istart or > istart + n preserved) 
    Couple[] cs = new Couple[n];
    for (int i = 0; i<n; i++) {
      cs[i] = new Couple();
      cs[i].x1 = a1[i+istart];
      cs[i].x2 = a2[i+istart];
    }
    Arrays.sort(cs);
    for (int i = 0; i<n; i++) {
      a1[i+istart] = cs[i].x1;
      a2[i+istart] = (Double) cs[i].x2;
    }
  }
 public static void sort2(double[] a1, double[][] a2, int n, int istart) {
    // sort the first n elements of a1 and a2[] (lines of array a2)
//    starting at index istart, in increasing order for a1
    // (elements with index < istart or > istart + n preserved) 
    Couple[] cs = new Couple[n];
    for (int i = 0; i<n; i++) {
      cs[i] = new Couple();
      cs[i].x1 = a1[i+istart];
      cs[i].x2 = a2[i+istart];
    }
    Arrays.sort(cs);
    for (int i = 0; i<n; i++) {
      a1[i+istart] = cs[i].x1;
      a2[i+istart] = (double[]) cs[i].x2;
    }
  }

  public static int indAbsMax(int n, double x[], int istart) {
    // finds the index of element having maximum absolute value 
    // among the first n elements of x starting at index istart
    if (n<1||istart<0||istart+n>x.length)
      throw new ArrayIndexOutOfBoundsException();
    if (n==1) return (istart);
    double max = Math.abs(x[istart]);
    int imax = istart;
    for (int i = istart+1; i<istart+n; i++) {
      double ax = Math.abs(x[i]);
      if (ax>max) {
        max = ax;
        imax = i;
      }
    }
    return imax;
  }

  public static double dot(double[] x, double[] y) {
    // dot product of vectors x and y
    int n = x.length;
    if (n!=y.length) throw new IllegalArgumentException();
    double res = 0;
    for (int i = 0; i<n; i++) res += x[i]*y[i];
    return res;
  }
  public static void rotateArray(double[][] a, int x) {
    // permutation circulaire des lignes de la matrice a
    // plaçant la ligne x en tête
  double[][] b =new double[a.length-x][];
 System.arraycopy(a, x, b, 0, b.length);
 System.arraycopy(a, 0, a, b.length, a.length-b.length);
 System.arraycopy(b, 0, a, 0, b.length);
     }

}
