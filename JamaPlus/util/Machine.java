/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JamaPlus.util;

/**
 *
 * @author Bernard
 */
public class Machine {

    /***********************************************************************
     *                                                                     *
     *				machar()			       *
     *                                                                     *
     ***********************************************************************/
    /***********************************************************************
    
    Description
    -----------
    
    This function is a partial translation of a Fortran-77 subroutine 
    written by W. J. Cody of Argonne National Laboratory.
    It dynamically determines the listed machine parameters of the
    floating-point arithmetic.  According to the documentation of
    the Fortran code, "the determination of the first three uses an
    extension of an algorithm due to M. Malcolm, ACM 15 (1972), 
    pp. 949-951, incorporating some, but not all, of the improvements
    suggested by M. Gentleman and S. Marovich, CACM 17 (1974), 
    pp. 276-277."  The complete Fortran version of this translation is
    documented in W. J. Cody, "Machar: a Subroutine to Dynamically 
    Determine Determine Machine Parameters," TOMS 14, December, 1988.
    
    
    Parameters reported 
    -------------------
    
    ibeta     the radix for the floating-point representation       
    it        the number of base ibeta digits in the floating-point
    significand					 
    irnd      0 if floating-point addition chops		      
    1 if floating-point addition rounds, but not in the 
    ieee style					
    2 if floating-point addition rounds in the ieee style
    3 if floating-point addition chops, and there is    
    partial underflow				
    4 if floating-point addition rounds, but not in the
    ieee style, and there is partial underflow    
    5 if floating-point addition rounds in the ieee style,
    and there is partial underflow                   
    machep    the largest negative integer such that              
    1.0+float(ibeta)**machep .ne. 1.0, except that 
    machep is bounded below by  -(it+3)          
    negeps    the largest negative integer such that          
    1.0-float(ibeta)**negeps .ne. 1.0, except that 
    negeps is bounded below by  -(it+3)	       
    
     ***********************************************************************/
    public static double eps(/*int res[]*/) {
        int ibeta, it, irnd, machep, negep;
        double beta, betain, betah, a, b = 0, ZERO, ONE, TWO, temp, tempa, temp1;
        int i, itemp;

        ONE = (double) 1;
        TWO = ONE + ONE;
        ZERO = ONE - ONE;

        a = ONE;
        temp1 = ONE;
        while (temp1 - ONE == ZERO) {
            a += a;
            temp = a + ONE;
            temp1 = temp - a;
            b += a; /* to prevent icc compiler error */
        }
        b = ONE;
        itemp = 0;
        while (itemp == 0) {
            b += b;
            temp = a + b;
            itemp = (int) (temp - a);
        }
        ibeta = itemp;
        beta = (double) ibeta;

        it = 0;
        b = ONE;
        temp1 = ONE;
        while (temp1 - ONE == ZERO) {
            it++;
            b *= beta;
            temp = b + ONE;
            temp1 = temp - b;
        }
        irnd = 0;
        betah = beta / TWO;
        temp = a + betah;
        if (temp - a != ZERO) irnd = 1;
        tempa = a + beta;
        temp = tempa + betah;
        if ((irnd == 0) && (temp - tempa != ZERO)) irnd = 2;

        negep = it + 3;
        betain = ONE / beta;
        a = ONE;
        for (i = 0; i < negep; i++) a *= betain;
        b = a;
        temp = ONE - a;
        while (temp - ONE == ZERO) {
            a *= beta;
            negep--;
            temp = ONE - a;
        }
        negep = -(negep);

        machep = -(it) - 3;
        a = b;
        temp = ONE + a;
        while (temp - ONE == ZERO) {
            a *= beta;
            machep += 1;
            temp = ONE + a;
        }
//        res[0] = ibeta;
//        res[1] = it;
//        res[2] = irnd;
//        res[3] = machep;
//        res[4] = negep;
        return a;
    }

    static public void main(String[] args) {
        int[] res = new int[5];
//        double precision = eps(res);
//        System.out.println("eps = " + precision);
        System.out.println("ibeta = " + res[0]);
        System.out.println("it = " + res[1]);
        System.out.println("irnd = " + res[2]);
        System.out.println("machep = " + res[3]);
        System.out.println("negep = " + res[4]);
    }
}
