/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JamaPlus;

/**
 *
 * AFC analyse factorielle des correspondances (optimisé mémoire)
 * mat :  matrice binaire (0. ou 1.) profil des lignes (un individu par ligne, un caractère par colonne)
 * CP: composantes principales,
 * VP: valeurs d'inertie, 
 * CG: centres de gravité des CP contenant un même caractère (non calculé dans cette version)
 * [p,q] = size(mat); p:nb de lignes, q:nb de col
 * 
 * @author Bernard
 */
public class AFC {

  private double[][] CP;
  private double[] VP;
  private int p, q, n;

  AFC(Matrix Arg) {
    Matrix mat = Arg.copy();
    mat = mat.nulColumnSuppression();
    p = mat.getRowDimension();
    q = mat.getColumnDimension();
    n = Math.min(Math.min(p, q), 5); // n : nombre de composantes principales calculées
    if (n == 0) return;
    if (n == 1) {
      CP = new double[p][1];
      VP = new double[1];
      return;
    }
    double f = mat.elementSum();
    if (f == 0) return;
    mat = mat.timesEquals(1 / f);
    Matrix r = mat.lineSum();
    Matrix r1 = r.arrayInverse();
    Matrix r2 = r.arraySqrt();
    Matrix r3 = r2.arrayInverse();
    Matrix c = mat.columnSum();
    Matrix c1 = c.arrayInverse();
    Matrix c2 = c.arraySqrt();
    Matrix c3 = c2.arrayInverse();
    mat = mat.firstColumnTimesEqual(r3);
    mat = mat.firstLineTimesEqual(c3);
    SingularValueDecomposition svd = mat.svd();
    Matrix cp = svd.getU();
    Matrix vp = new Matrix(svd.getSingularValues(), 1);
    cp = cp.getMatrix(0, p - 1, 1, n - 1);
    cp = cp.firstColumnTimesEqual(r2);
    vp = vp.getMatrix(0, 0, 1, n - 1);
    cp = cp.firstLineTimesEqual(vp);
    cp.fixColumnSigns();
    cp = cp.firstColumnTimesEqual(r1);
    CP = cp.getArray();
    VP = vp.getColumnPackedCopy();
  }

  public Matrix getCP() {
    if (CP == null) return null;
    return new Matrix(CP, p, n - 1);
  }

  public double[] getVP() {
    return VP;
  }
}
