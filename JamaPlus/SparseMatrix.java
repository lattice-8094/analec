/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JamaPlus;

import JamaPlus.SparseSVD.SparseSVDException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bernard
 */
public class SparseMatrix {
  private int m, n, nnz;
  private int[] colpts, rowinds;
  private double[] vals;
  private double[] tempRow;  // stockage temporaire pour certains calculs (opb)

  public SparseMatrix(int m, int n) { // null matrix
    this.m = m;
    this.n = n;
    nnz = 0;
    colpts = new int[n+1];
    rowinds = new int[0];
    vals = new double[0];
  }

   private static class Triple implements Comparable<Triple> {
    int row;
    int col;
    double val;

    private Triple(int row, int col, double val) {
      this.row = row;
      this.col = col;
      this.val = val;
    }
    private Triple(int row, int col) {
      this.row = row;
      this.col = col;
    }
    public int compareTo(Triple t) {
      int res = col - t.col;
      if(res==0) res = row - t.row;
      return res;
    }
  }

 public SparseMatrix(int m, int n, ArrayList<Integer> rows, ArrayList<Integer> cols,
          ArrayList<Double> values) {
    this.m = m;
    this.n = n;
    nnz = rows.size();
    if (nnz!=cols.size()||nnz!=rows.size())
      throw new ArrayIndexOutOfBoundsException(
              "Size of lines, cols and values don't agree");
    colpts = new int[n+1];
    rowinds = new int[nnz];
    vals = new double[nnz];
    Triple[] triples = new Triple[nnz];
    for(int i=0; i<nnz; i++) 
     triples[i] = new Triple(rows.get(i), cols.get(i), values.get(i));    
    Arrays.sort(triples);
    colpts[0] = 0;
    int c = 0;
    for(int i=0; i<nnz; i++) {
      if(i>0 && triples[i].row == triples[i-1].row && triples[i].col == triples[i-1].col) 
        throw new ArrayIndexOutOfBoundsException(
              "Two values with same row and column numbers");
      while(c<triples[i].col) colpts[++c] = i;
      rowinds[i] = triples[i].row;
      vals[i] = triples[i].val;
    }
    while(c<n) colpts[++c] = nnz;
  }

public SparseMatrix(int m, int n, ArrayList<Integer> rows, ArrayList<Integer> cols,
          double value) {
    this.m = m;
    this.n = n;
    nnz = rows.size();
    if (nnz!=cols.size()||nnz!=rows.size())
      throw new ArrayIndexOutOfBoundsException(
              "Size of lines, cols and values don't agree");
    colpts = new int[n+1];
    rowinds = new int[nnz];
        vals = new double[nnz];
    Triple[] triples = new Triple[nnz];
    for(int i=0; i<nnz; i++) 
     triples[i] = new Triple(rows.get(i), cols.get(i));    
    Arrays.sort(triples);
    colpts[0] = 0;
    int c = 0;
    for(int i=0; i<nnz; i++) {
      if(i>0 && triples[i].row == triples[i-1].row && triples[i].col == triples[i-1].col) 
        throw new ArrayIndexOutOfBoundsException(
              "Two values with same row and column numbers");
      while(c<triples[i].col) colpts[++c] = i;
      rowinds[i] = triples[i].row;
      vals[i] = value;
    }
    while(c<n) colpts[++c] = nnz;
  }

  private SparseMatrix(int m, int n, int nnz, int[] colpts, int[] rowinds, double[] vals) {
    this.m = m;
    this.n = n;
    this.nnz = nnz;
    this.colpts = colpts;
    this.rowinds = rowinds;
    this.vals = vals;
  }

  public SparseMatrix(Matrix mat) {  // convert a dense matrix
    m = mat.getRowDimension();
    n = mat.getColumnDimension();
    long nbval = mat.getNumberNonZeroValues();
    if (nbval>Integer.MAX_VALUE) throw new ArrayIndexOutOfBoundsException(
              "Number of non-zero values exceeds 32 bits integer");
    nnz = (int) nbval;
    colpts = new int[n+1];
    rowinds = new int[nnz];
    vals = new double[nnz];
    int nval = 0;
    for (int j = 0; j<n; j++) {
      colpts[j] = nval;
      for (int i = 0; i<m; i++) if (mat.get(i, j)!=0) {
          rowinds[nval] = i;
          vals[nval++] = mat.get(i, j);
        }
    }
    colpts[n] = nnz;
  }

  public SparseMatrix copy() {
    int[] colpts1 = new int[n+1];
    int[] rowinds1 = new int[nnz];
    double[] vals1 = new double[nnz];
    System.arraycopy(colpts, 0, colpts1, 0, n+1);
    System.arraycopy(rowinds, 0, rowinds1, 0, nnz);
    System.arraycopy(vals, 0, vals1, 0, nnz);
    return new SparseMatrix(m, n, nnz, colpts1, rowinds1, vals1);
  }

  public int getRowDimension() {
    return m;
  }

  public int getColumnDimension() {
    return n;
  }

  public double get(int i, int j) {
    try {
      for (int nval = colpts[j]; nval<colpts[j+1]; nval++)
        if (rowinds[nval]==i) return vals[nval];
      return 0;
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new ArrayIndexOutOfBoundsException("Sparse matrix : "
              +ex.getLocalizedMessage());
    }
  }

  public SparseMatrix transpose() {
    int[] colpts1 = new int[m+1];
    int[] rowinds1 = new int[nnz];
    double[] vals1 = new double[nnz];
    for (int v = 0; v<nnz; v++) colpts1[rowinds[v]]++;  // nnz in each row
    colpts1[m] = nnz-colpts1[m-1];
    for (int r = m-1; r>0; r--)
      colpts1[r] = colpts1[r+1]-colpts1[r-1]; // starting point of the previous row
    colpts1[0] = 0;
    int i = 0;
    for (int c = 0; c<n; c++) while (i<colpts[c+1]) {
        int j = colpts1[rowinds[i]+1]++;  // j : index of the value in the new vectors
        rowinds1[j] = c;
        vals1[j] = vals[i++];
      }
    return new SparseMatrix(n, m, nnz, colpts1, rowinds1, vals1);
  }

  public void nulColumnSuppression() {
    ArrayList<Integer> colpts1 = new ArrayList<Integer>();
    colpts1.add(0);
    for (int i = 0; i<n; i++) if (colpts[i+1]>colpts[i])
        colpts1.add(colpts[i+1]);
    n = colpts1.size()-1;
    colpts = new int[n+1];
    for (int i = 0; i<=n; i++) colpts[i] = colpts1.get(i);
  }

  public double elementSum() {
    double s = 0;
    for (int i = 0; i<nnz; i++) s += vals[i];

    return s;
  }

  public Matrix lineSum() {
    double[] S = new double[m];
    for (int i = 0; i<nnz; i++) S[rowinds[i]] += vals[i];

    return new Matrix(S, m);
  }

  public Matrix columnSum() {
    double[] S = new double[n];
    for (int i = 0; i<n; i++) for (int j = colpts[i]; j<colpts[i+1]; j++)
        S[i] += vals[j];

    return new Matrix(S, 1);
  }

  public SparseMatrix timesEquals(double s) {
    for (int i = 0; i<nnz; i++) {
      vals[i] *= s;
    }
    return this;
  }

  public SparseMatrix firstColumnTimesEqual(Matrix B) {
    double[][] A = B.getArray();
    for (int i = 0; i<n; i++) for (int j = colpts[i]; j<colpts[i+1]; j++)
        vals[j] *= A[rowinds[j]][0];
    return this;
  }

  public SparseMatrix firstLineTimesEqual(Matrix B) {
    double[][] A = B.getArray();
    for (int i = 0; i<n; i++) for (int j = colpts[i]; j<colpts[i+1]; j++)
        vals[j] *= A[0][i];
    return this;
  }

  public void opa(double x[], double y[]) {
    // Computes y = Ax (x et y : length n)
    Arrays.fill(y, 0);
    for (int i = 0; i<n; i++) for (int j = colpts[i]; j<colpts[i+1]; j++)
        y[rowinds[j]] += vals[j]*x[i];
  }

  public void opb(double x[], double y[]) {
    // Computes y = A'Ax (x et y : length n)
    if (tempRow==null) tempRow = new double[m];
    else Arrays.fill(tempRow, 0);
    Arrays.fill(y, 0);
    for (int i = 0; i<n; i++) for (int j = colpts[i]; j<colpts[i+1]; j++)
        tempRow[rowinds[j]] += vals[j]*x[i];
    for (int i = 0; i<n; i++) for (int j = colpts[i]; j<colpts[i+1]; j++)
        y[i] += vals[j]*tempRow[rowinds[j]];
  }

  public SparseSVD svd(int dim) throws SparseSVD.SparseSVDException {
    return new SparseSVD(this, dim);
  }

  /** Analyse factorielle de correspondances
  @return     Analyse factorielle de correspondances
  @see Analyse factorielle de correspondances
   */
  public SparseAFC afc() {
    try {
      return new SparseAFC(this);
    } catch (SparseSVDException ex) {
      return null;
    }
  }
}
