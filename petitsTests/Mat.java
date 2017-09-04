/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petitsTests;

import JamaPlus.*;

/**
 *
 * @author Bernard
 */
public class Mat {
    static public void dumpMat(Matrix mat) {
         for (int i = 0; i < mat.getRowDimension(); i++) {
            System.out.println();
            for (int j = 0; j < mat.getColumnDimension(); j++)
                System.out.print(mat.get(i, j) + "  ");
        }
        System.out.println();
    }
    static public void main(String[] args) {
        Matrix mat = new Matrix(new double[][]{{1., 4.}, {2., -3.},
          {5, 1}, {6 , 2}, {7, 5}
        });
        dumpMat(mat);
        dumpMat(mat.columnCovariance());
        SingularValueDecomposition svd = new SingularValueDecomposition(mat.columnCovariance());
        dumpMat(svd.getU());
        double[] s = svd.getSingularValues();
        for(int i=0; i<s.length;i++) System.out.print(s[i]+"  ");
      
    }
}
 
