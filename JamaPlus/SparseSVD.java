/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JamaPlus;

import JamaPlus.util.*;
import java.util.*;

/**
 *
 * @author Bernard
 */
public class SparseSVD {  // based on code adapted from SVDLIBC - svdLAS2 (adapted from SVDPACKC)
  private boolean transpose; // si transposition de la matrice en entrée
  private int rows, cols, dim; // nb rows, nb cols, nb de val sing. désirées
  // (après transposition éventuelle)
  private double[][] Ut; // (d x m) transpose of left singular vectors (rows of Ut)
  private double[][] Vt; // (d x n) transpose of right singular vectors (rows of Vt)
  private double[] S; // d singular values
  /* Constantes */
  private final static int MAXLL = 2; // ??
  private final static double[] END = {-1e-30, 1e-30};
  private final static double EPS = Machine.eps(); // precision de la machine
  private final static double RAD_EPS = Math.sqrt(EPS);
  private final static double RAD34_EPS = RAD_EPS*Math.sqrt(RAD_EPS);
  private final static double KAPPA = Math.max(RAD34_EPS, 1e-6);
  private double EPS1;  // = precision * sqrt(nbcols)
  /* Variables de travail : mémoire à libérer en fin de calcul */
  private double[] alph;  // diagonale de la matrice tridiagonale T
  private double[] bet; // éléments hors-diagonale de T
  private double[] ritz; // valeurs propres de la  projection de A'A sur l'espace de Krylov
  private double[] bnd; // a vérifier : initialisation (127) nécessaire ?? 
  private double[] rj ;
  private  double[] qj;
  private  double[] qj1;
  private  double[] pj ;
  private  double[] pj1;
  private  double[] wk;
   private   double[] eta; 
  private  double[] oldeta; 


//    private static int irandom = 0;  // deboguage : à supprimer

  private static class Storage {  // store Lanczos vectors q(j) 
    private double[][] P, Q;  // P supplementary storage for j < MAXLL (j= 0 or 1)

    private Storage(int nb, int maxll) {
      P = new double[maxll][];
      Q = new double[nb][];
    }

    private void storeP(int n, int j, double[] p) {
      if (P[j]==null) P[j] = new double[n];
      System.arraycopy(p, 0, P[j], 0, n);
    }

    private void storeQ(int n, int j, double[] q) {
      if (Q[j]==null) Q[j] = new double[n];
      System.arraycopy(q, 0, Q[j], 0, n);
    }

    private void retrieveP(int n, int j, double[] p) {
      System.arraycopy(P[j], 0, p, 0, n);
    }

    private void retrieveQ(int n, int j, double[] q) {
      System.arraycopy(Q[j], 0, q, 0, n);
    }
  }
  private Storage storage;

 public enum CodeSVDException {
   DIM_OUT_OF_BOUNDS ("Nombre de valeurs singulières désirées invalide"),
   FIRST_STEP_FAILURE("Impossible d'initialiser le premier pas de calcul"),
   CONVERGENCE_FAILURE("Echec de convergence ");
   String message;
  CodeSVDException(String message) {
   this.message = message;
 }
 }
  public static class SparseSVDException extends Exception { // Erreurs
    private CodeSVDException code;
    private int k;

    private SparseSVDException(CodeSVDException code, int k) {
      super();
      this.code = code;
      this.k = k;
    }

    public CodeSVDException getCode() {
      return code;
    }

    public int getK() {
      return k;
    }

    @Override
    public String getMessage() {
      return super.getMessage()+"\ncode = "+getCode().message +", n = "+getK();
    }
  }

  public SparseSVD(SparseMatrix spm, int d) throws SparseSVDException {
    int iter = Math.min(spm.getRowDimension(), spm.getColumnDimension()); // nb d'itérations 
    if (d<=0 || d>iter)
      throw new SparseSVDException(CodeSVDException.DIM_OUT_OF_BOUNDS, d);
    dim = d;
    transpose = spm.getColumnDimension()>=spm.getRowDimension()*1.2;
    SparseMatrix A = transpose ? spm.transpose() : spm.copy();
    rows = A.getRowDimension();
    cols = A.getColumnDimension();
    EPS1 = EPS*Math.sqrt(cols);
    alph = new double[iter];  // diagonale de la matrice tridiagonale T
    bet = new double[iter+1]; // éléments hors-diagonale de T
    ritz = new double[iter+1];
    bnd = new double[iter+1]; // a vérifier : initialisation (127) nécessaire ?? 
    storage = new Storage(iter, MAXLL);
    // Calcul de la matrice tridiagonale 
    int[] steps_neig_ = lanso(A, iter);
    Ut = new double[dim][rows];
    Vt = new double[dim][cols];
    S = new double[dim];
    // Calcul des  vecteurs propres
    //  nsig  : nb de val. propres "kappa-acceptables"
    int nsig = ritvec(A, steps_neig_);
    if (nsig<dim) dim = nsig;  // moins de valeurs obtenues que demandées
    // libère la mémoire
    storage = null;
    alph =  bet = ritz = bnd = null;

  }

  private int[] lanso(SparseMatrix A, int iter) throws SparseSVDException {
    // Calcul de la matrice tridiagonale
    int neig = 0;
    // espace de travail
    rj = new double[cols];
    qj = new double[cols];
     qj1 = new double[cols];
     pj = new double[cols];
     pj1 = new double[cols];
     wk = new double[cols];
    // step one 
    double[] rnm_tol_ = stpone(A, cols); // initialise rmn et tol
     eta = new double[iter]; // orthogonality estimate of Lanczos vectors
    oldeta = new double[iter];
    if (rnm_tol_[0]==0) {
      ritz[0] = alph[0];
      storage.storeQ(cols, 0, qj);
      return new int[]{0, 1}; // steps = 0 et neig = 1
    }
    eta[0] = oldeta[0] = EPS1;
    boolean[] enough_ = {false}; // flag de sortie de boucle
    int steps = 0; // j = steps
    int[] ll_ = {0};  // 0, 1 or 2 initial Lanczos vectors in local orthog.
    int first = 1;
    int last = Math.min(dim+Math.max(8, dim), iter);  // = iter dans notre version
    int i, l;
    int intro = 0;
    while (!enough_[0]) {
      steps = lanczos_step(A, first, last, 
              ll_, enough_, rnm_tol_, cols);
      steps = enough_[0] ? steps-1 : last-1;
      first = steps+1;
      bet[first] = rnm_tol_[0];
      // analyse T
      l = 0;
      for (int id2 = 0; id2<steps; id2++) {
        if (l>steps) break;
        for (i = l; i<=steps; i++) if (bet[i+1]==0) break;
        if (i>steps) i = steps;
        // now i is at the end of an unreduced submatrix
        DoubleArraysPlus.copyReverse(i-l+1, alph, l, ritz, l);
        DoubleArraysPlus.copyReverse(i-l, bet, l+1, wk, l+1);
        imtqlb(i-l+1, l, ritz, wk, bnd);
        for (int id3 = l; id3<=i; id3++)
          bnd[id3] = rnm_tol_[0]*Math.abs(bnd[id3]);
        l = i+1;
      }
      // sort eigenvalues into increasing order
      DoubleArraysPlus.sort2(ritz, bnd, steps+1, 0);
      // massage error bounds for very close ritz values 
      neig = error_bound(enough_, END[0], END[1], steps, rnm_tol_[1]);
      // should we stop? 
      if (neig<dim) {
        if (neig==0) {
          last = first+9;
          intro = first;
        } else {
          last = first+Math.max(3, 1+((steps-intro)*(dim-neig))/neig);
        }
        last = Math.min(last, iter);
      } else {
        enough_[0] = true;
      }
      enough_[0] = enough_[0]||first>=iter;
    }
    storage.storeQ(cols, steps, qj);
    // libère la mémoire
     rj = qj =  qj1 = pj =  pj1 =  wk = null;
     eta = oldeta = null;
    return new int[]{steps, neig};
  }

  private double[] stpone(SparseMatrix A,  int n)
          throws SparseSVDException {
    /*  stp-one performs the first step of the Lanczos algorithm.  It also
    does a step of extended local re-orthogonalization.
    Arguments 
    (input)
    n      dimension of the eigenproblem for matrix B
    (output)
    wptr   array of pointers that point to work space that contains
    wptr[0]             r[j]
    wptr[1]             q[j]
    wptr[2]             q[j-1]
    wptr[3]             p
    wptr[4]             p[j-1]
    alph             diagonal elements of matrix T 
    throw a SparseSVDException(-1) if the choice of initial vector (startv) failed
     */
    // get initial normalized vector; default is random 
    startv0(A, n);
    System.arraycopy(pj, 0, qj, 0, n);
    // take the first step   
    A.opb(pj, rj);
    alph[0] = DoubleArraysPlus.dot(rj, pj);
    for (int i = 0; i<n; i++) rj[i] -= alph[0]*qj[i];
    /* extended local reorthogonalization */
    double t = DoubleArraysPlus.dot(rj, pj);
    for (int i = 0; i<n; i++) rj[i] -= t*qj[i];
    alph[0] += t;
    System.arraycopy(rj, 0, pj1, 0, n);
    double rnm = Math.sqrt(DoubleArraysPlus.dot(pj1, pj1));
    double tol = RAD_EPS*(rnm+Math.abs(alph[0]));
    return new double[]{rnm, tol};
  }

//  private static double random2() {
//    double x = 0.11*(++irandom);
//    return x-Math.floor(x);
//  }
//
private void startv0(SparseMatrix A, int n)
          throws SparseSVDException {
    /* startv delivers a starting normalized vector in r, 
     * and throws a SparseSVDException(-1) if no starting vector can be found.
    Parameters 
    (input)
    n      dimension of the eigenproblem matrix B
    wptr   array of pointers that point to work space
    step      starting index for a Lanczos run
    (output)
    wptr   array of pointers that point to work space that contains
    r[j], q[j], q[j-1], p[j], p[j-1]
     */
// get initial vector; default is random
    double rnm2 = 0;
    Random random = new Random(918273);
    for (int id = 0; id<3; id++) {
      for (int i = 0; i<n; i++) wk[i]  = random.nextDouble(); 
      // apply operator to put r in range (essential if m singular)
      A.opb(wk, pj);
      rnm2 = DoubleArraysPlus.dot(pj, pj);
      if (rnm2>0) break;
    }
    // normalize
    if (rnm2<=0) throw new SparseSVDException(CodeSVDException.FIRST_STEP_FAILURE, 0);
    rnm2 = 1/Math.sqrt(rnm2);
    for (int i = 0; i<n; i++) pj[i] *= rnm2;
  }

  private int lanczos_step(SparseMatrix A, int first, int last,int[] ll_,   
          boolean[] enough_, double[] rnm_tol_, int n) throws SparseSVDException {
    /*   embodies a single Lanczos step
    Arguments 
    (input)
    n        dimension of the eigenproblem for matrix B
    first    start of index through loop				      
    last     end of index through loop				     
    wptr	    array of pointers pointing to work space		    
    alf	    array to hold diagonal of the tridiagonal matrix T
    eta      orthogonality estimate of Lanczos vectors at step j   
    oldeta   orthogonality estimate of Lanczos vectors at step j-1
    bet      array to hold off-diagonal of T                     
    ll       number of intitial Lanczos vectors in local orthog. 
    (has value of 0, 1 or 2)			
    enough   stop flag			
     */
    int j;
    double t;
    double mid[];
    for (j = first; j<last; j++) {
      mid = qj1;
      qj1 = qj;
      qj = mid;
      mid = pj;
      pj = pj1;
      pj1 = mid;
      storage.storeQ(n, j-1, qj1);
      if (j-1<MAXLL) storage.storeP(n, j-1, pj1);
      bet[j] = rnm_tol_[0];

      // restart if invariant subspace is found 
      if (bet[j]==0) {
        rnm_tol_[0] = startv(A, j, n);
        if (rnm_tol_[0]==0) enough_[0] = true;
      }
      if (enough_[0]) {
        /* added by Doug... */
        /* These lines fix a bug that occurs with low-rank matrices */
        mid = qj1;
        qj1 = qj;
        qj = mid;
        /* ...added by Doug */
        break;
      }
      /* take a lanczos step */
      t = 1.0/rnm_tol_[0];
      for (int i = 0; i<n; i++) qj[i] = t*rj[i];
      for (int i = 0; i<n; i++) pj[i] *= t;
      A.opb(pj, rj);
      for (int i = 0; i<n; i++) rj[i] -= rnm_tol_[0]*qj1[i];
      alph[j] = DoubleArraysPlus.dot(rj, pj);
      for (int i = 0; i<n; i++) rj[i] -= alph[j]*qj[i];
      /* orthogonalize against initial lanczos vectors */
      if (j<=MAXLL&&(Math.abs(alph[j-1])>4.0*Math.abs(alph[j])))
        ll_[0] = j;
      for (int i = 0; i<Math.min(ll_[0], j-1); i++) {
        storage.retrieveP(n, i, wk);
        t = DoubleArraysPlus.dot(wk, rj);
        storage.retrieveQ(n, i, wk);
        for (int ii = 0; ii<n; ii++) rj[ii] -= t*wk[ii];
        oldeta[i] = eta[i] = EPS1;
      }
      /* extended local reorthogonalization */
      t = DoubleArraysPlus.dot(rj, pj1);
      for (int i = 0; i<n; i++) rj[i] -= t*qj1[i];
      if (bet[j]>0) bet[j] += t;
      t = DoubleArraysPlus.dot(rj, pj);
      for (int i = 0; i<n; i++) rj[i] -= t*qj[i];
      alph[j] += t;
      System.arraycopy(rj, 0, pj1, 0, n);
      rnm_tol_[0] = Math.sqrt(DoubleArraysPlus.dot(rj, pj1));
      rnm_tol_[1] = RAD_EPS*(bet[j]+Math.abs(alph[j])+rnm_tol_[0]);
      /* update the orthogonality bounds */
      ortbnd(j, rnm_tol_[0]);
      /* restore the orthogonality state when needed */
      purge(n, ll_[0], j, rnm_tol_);
      if (rnm_tol_[0]<=rnm_tol_[1]) rnm_tol_[0] = 0;
    }
    return j;
  }

  private double startv(SparseMatrix A, int step, int n)
          throws SparseSVDException {
    /* startv delivers a starting vector in r and returns |r|; it returns 
    zero if the range is spanned, and throws a SparseSVDException(-1) if no starting 
    vector within range of operator can be found.
    Parameters 
    (input)
    n      dimension of the eigenproblem matrix B
    wptr   array of pointers that point to work space
    step      starting index for a Lanczos run
    (output)
    wptr   array of pointers that point to work space that contains
    r[j], q[j], q[j-1], p[j], p[j-1]
     */
// get initial vector; default is random
    double rnm2 = DoubleArraysPlus.dot(rj, rj);
    Random random = new Random(918273+step);
    for (int id = 0; id<3; id++) {
      for (int i = 0; i<n; i++) rj[i] = random.nextDouble();
      System.arraycopy(rj, 0, pj, 0, n);
      // apply operator to put r in range (essential if m singular)
      A.opb(pj, rj);
      System.arraycopy(rj, 0, pj, 0, n);
      rnm2 = DoubleArraysPlus.dot(rj, pj);
      if (rnm2>0) break;
    }
    if (rnm2<=0) throw new SparseSVDException(CodeSVDException.FIRST_STEP_FAILURE, 0);
    double t;
    for (int j = 0; j<step; j++) {
      storage.retrieveQ(n, j, wk);
      t = -DoubleArraysPlus.dot(pj, wk);
      for (int i = 0; i<n; i++) rj[i] += t*wk[i];
    }
    // make sure q[step] is orthogonal to q[step-1] 
    t = DoubleArraysPlus.dot(pj1, rj);
    for (int i = 0; i<n; i++) rj[i] -= t*qj1[i];
    System.arraycopy(rj, 0, pj, 0, n);
    t = DoubleArraysPlus.dot(pj, rj);
    if (t<=EPS*rnm2) rnm2 = 0;
    else rnm2 = t;
    return Math.sqrt(rnm2);
  }

  private void ortbnd(int step, double rnm) {
    /*  updates the eta recurrence
    Arguments 
    (input)
    alf      array to hold diagonal of the tridiagonal matrix T         
    eta      orthogonality estimate of Lanczos vectors at step j        
    oldeta   orthogonality estimate of Lanczos vectors at step j-1     
    bet      array to hold off-diagonal of T                          
    n        dimension of the eigenproblem for matrix B		    
    j        dimension of T					  
    rnm	    norm of the next residual vector			 
    eps1	    roundoff estimate for dot product of two unit vectors
    (output)
    eta      orthogonality estimate of Lanczos vectors at step j+1     
    oldeta   orthogonality estimate of Lanczos vectors at step j        
     */
    if (step<1) return;
    if (rnm!=0) {
      if (step>1)
        oldeta[0] = (bet[1]*eta[1]+(alph[0]-alph[step])*eta[0]
                -bet[step]*oldeta[0])/rnm+EPS1;
      for (int i = 1; i<=step-2; i++)
        oldeta[i] = (bet[i+1]*eta[i+1]+(alph[i]-alph[step])*eta[i]
                +bet[i]*eta[i-1]-bet[step]*oldeta[i])/rnm+EPS1;
    }
    oldeta[step-1] = EPS1;
    double t;
    for (int i = 0; i<step; i++) {
      t = oldeta[i];
      oldeta[i] = eta[i];
      eta[i] = t;
    }
    eta[step] = EPS1;
  }

  private void purge(int n, int ll, int step, double[] rnm_tol_) {
    /*
    purge examines the state of orthogonality between the new Lanczos
    vector and the previous ones to decide whether re-orthogonalization 
    should be performed
    Arguments 
    (input)
    n        dimension of the eigenproblem for matrix B		       
    ll       number of intitial Lanczos vectors in local orthog.       
    r        residual vector to become next Lanczos vector            
    q        current Lanczos vector			           
    ra       previous Lanczos vector
    qa       previous Lanczos vector
    wrk      temporary vector to hold the previous Lanczos vector
    eta      state of orthogonality between r and prev. Lanczos vectors 
    oldeta   state of orthogonality between q and prev. Lanczos vectors
    j        current Lanczos step				     
    (output)
    r	    residual vector orthogonalized against previous Lanczos 
    vectors
    q        current Lanczos vector orthogonalized against previous ones    
     */
    if (step<ll+2) return;
    int k = DoubleArraysPlus.indAbsMax(step-(ll+1), eta, ll);
    if (Math.abs(eta[k])<=RAD_EPS) return;
    double REPS1 = EPS1/RAD_EPS;
    double t;
    for (int iteration = 0; iteration<2; iteration++) {
      if (rnm_tol_[0]<=rnm_tol_[1]) break;
      /* bring in a lanczos vector t and orthogonalize both 
       * r and q against it */
      double tq = 0;
      double tr = 0;
      for (int i = ll; i<step; i++) {
        storage.retrieveQ(n, i, wk);
        t = -DoubleArraysPlus.dot(pj, wk);
        tq += Math.abs(t);
        for (int ii = 0; ii<n; ii++) qj[ii] += t*wk[ii];
        t = -DoubleArraysPlus.dot(pj1, wk);
        tr += Math.abs(t);
        for (int ii = 0; ii<n; ii++) rj[ii] += t*wk[ii];
      }
      System.arraycopy(qj, 0, pj, 0, n);
      t = -DoubleArraysPlus.dot(rj, pj);
      tr += Math.abs(t);
      for (int i = 0; i<n; i++) rj[i] += t*qj[i];
      System.arraycopy(rj, 0, pj1, 0, n);
      rnm_tol_[0] = Math.sqrt(DoubleArraysPlus.dot(pj1, rj));
      if (tq<=REPS1&&tr<=REPS1*rnm_tol_[0]) break;
    }
    for (int i = ll; i<=step; i++) {
      eta[i] = EPS1;
      oldeta[i] = EPS1;
    }
  }

  private static void imtqlb(int n, int i0, double[] d, double[] e, double[] bnd)
          throws SparseSVDException {
    /*   imtqlb() finds the eigenvalues of a symmetric tridiagonal
    matrix by the implicit QL method.
    Arguments 
    (input)
    n      order of the symmetric tridiagonal matrix   
    d      contains the diagonal elements of the input matrix           
    e      contains the subdiagonal elements of the input matrix in its
    last n-1 positions.  e[0] is arbitrary
    i0   starting index for the 3 vectors
    (elements with index < i0 or >= i0+n are preserved)
    (output)
    d      contains the eigenvalues in ascending order.  if an error
    exit is made, the eigenvalues are correct and ordered for
    indices 0,1,...ierr, but may not be the smallest eigenvalues.
    e      has been destroyed.					    
    throws a SparseSVDException(j) if the j-th eigenvalue has
    not been determined after 30 iterations.		    
     */
    if (n==1) return;
    bnd[i0] = 1;
    for (int i = i0+1; i<i0+n; i++) {
      bnd[i] = 0;
      e[i-1] = e[i];
    }
    e[i0+n-1] = 0;
    for (int l = i0; l<i0+n; l++) {
      for (int iteration = 0; iteration<=30; iteration++) {
        double p = d[l];
        double f = bnd[l];
        int m;
        for (m = l; m<i0+n-1; m++) { // test de convergence
          double test = Math.abs(d[m])+Math.abs(d[m+1]);
          if (test+Math.abs(e[m])==test) break;
        }
        if (m==l) {
          d[l] = p;
          bnd[l] = f;
          /* order the eigenvalues */
          DoubleArraysPlus.sort2(d, bnd, l-i0+1, i0);
          break;
        } else {
          if (iteration==30) throw new SparseSVDException(CodeSVDException.CONVERGENCE_FAILURE, l);
          /*........ form shift ........*/
          double g = (d[l+1]-p)/(2.0*e[l]);
          double r = Math.hypot(g, 1);
          g = d[m]-p+e[l]/(g+Math.copySign(r, g));
          double s = 1;
          double c = 1;
          p = 0;
          boolean underflow = false;
          for (int i = m-1; i>=l; i--) {
            f = s*e[i];
            double b = c*e[i];
            r = Math.hypot(f, g);
            e[i+1] = r;
            if (r==0) {
              /*........ recover from underflow .........*/
              underflow = true;
              d[i+1] -= p;
              e[m] = 0.0;
              break;
            }
            s = f/r;
            c = g/r;
            g = d[i+1]-p;
            r = (d[i]-g)*s+2*c*b;
            p = s*r;
            d[i+1] = g+p;
            g = c*r-b;
            f = bnd[i+1];
            bnd[i+1] = s*bnd[i]+c*f;
            bnd[i] = c*bnd[i]-s*f;
          }

          if (!underflow) {
            d[l] -= p;
            e[l] = g;
            e[m] = 0.0;
          }
        }
      }
    }
  }

  private int error_bound(boolean[] enough_, double endl, double endr,
          int step, double tol) {
    /*
    massages error bounds for very close ritz values by placing 
    a gap between them.  The error bounds are then refined to reflect this.
    Arguments 
    (input)
    endl     left end of interval containing unwanted eigenvalues
    endr     right end of interval containing unwanted eigenvalues
    ritz     array to store the ritz values
    bnd      array to store the error bounds
    enough   stop flag
     */
    int mid = DoubleArraysPlus.indAbsMax(step+1, bnd, 1);

    for (int i = step; i>mid; i--)
      if (Math.abs(ritz[i-1]-ritz[i])<RAD34_EPS*Math.abs(ritz[i]))
        if (bnd[i]>tol&&bnd[i-1]>tol) {
          bnd[i-1] = Math.hypot(bnd[i], bnd[i-1]);
          bnd[i] = 0;
        }
    for (int i = 1; i<mid; i++)
      if (Math.abs(ritz[i+1]-ritz[i])<RAD34_EPS*Math.abs(ritz[i]))
        if (bnd[i]>tol&&bnd[i+1]>tol) {
          bnd[i+1] = Math.hypot(bnd[i], bnd[i+1]);
          bnd[i] = 0.0;
        }
    /* refine the error bounds */
    int neig = 0;
    double gapl = ritz[step]-ritz[0];
    for (int i = 0; i<=step; i++) {
      double gap = gapl;
      if (i<step) gapl = ritz[i+1]-ritz[i];
      gap = Math.min(gap, gapl);
      if (gap>bnd[i]) bnd[i] *= (bnd[i]/gap);
      if (bnd[i]<=16.0*EPS*Math.abs(ritz[i])) {
        neig++;
        if (!enough_[0]) enough_[0] = endl<ritz[i]&&ritz[i]<endr;
      }
    }
    return neig;
  }

  private int ritvec0(SparseMatrix A, int[] steps_neig_) throws SparseSVDException {

    int js, jsq, i, k, /*size,*/ id2, tmp, nsig, x;
    double[] s, xv2;
    double tmp0, tmp1, xnorm;
    double[] w1 = Vt[0];
    double[] w2 = new double[cols];

    js = steps_neig_[0]+1;
    jsq = js*js;
    /*size = sizeof(double) * n;*/

    s = new double[jsq];
    xv2 = new double[cols];

    /* initialize s to an identity matrix */
    for (i = 0; i<jsq; i += (js+1)) s[i] = 1.0;
    DoubleArraysPlus.copyReverse(js, alph, 0, w1, 0);
    DoubleArraysPlus.copyReverse(steps_neig_[0], bet, 1, w2, 1);

    /* on return from imtql2(), w1 contains eigenvalues in ascending 
     * order and s contains the corresponding eigenvectors */
    double[][] z = new double[js][js];
    for (int r = 0; r<js; r++) for (int c = 0; c<js; c++) z[r][c] = s[r*js+c];
    imtql2(js, js, w1, w2, z);
    for (int r = 0; r<js; r++) for (int c = 0; c<js; c++) s[r*js+c] = z[r][c];


    /*fwrite((char *)&n, sizeof(n), 1, fp_out2);
    fwrite((char *)&js, sizeof(js), 1, fp_out2);
    fwrite((char *)&kappa, sizeof(kappa), 1, fp_out2);*/
    /*id = 0;*/
    nsig = 0;
    x = 0;
    id2 = jsq-js;
    for (k = 0; k<js; k++) {
      tmp = id2;
      if (bnd[k]<=KAPPA*Math.abs(ritz[k])&&k>js-steps_neig_[1]-1) {
        if (--x<0) x = dim-1;
        w1 = Vt[x];
        for (i = 0; i<cols; i++) w1[i] = 0.0;
        for (i = 0; i<js; i++) {
          storage.retrieveQ(cols, i, w2);
          for (int ii = 0; ii<cols; ii++) w1[ii] += s[tmp]*w2[ii];
          tmp -= js;
        }
        /*fwrite((char *)w1, size, 1, fp_out2);*/

        /* store the w1 vector row-wise in array xv1;   
         * size of xv1 is (steps+1) * (nrow+ncol) elements 
         * and each vector, even though only ncol long,
         * will have (nrow+ncol) elements in xv1.      
         * It is as if xv1 is a 2-d array (steps+1) by     
         * (nrow+ncol) and each vector occupies a row  */

        /* j is the index in the R arrays, which are sorted by high to low 
        singular values. */

        /*for (i = 0; i < n; i++) R->Vt->value[x]xv1[id++] = w1[i];*/
        /*id += nrow;*/
        nsig++;
      }
      id2++;
    }
    s = null;

    /* Rotate the singular vectors and values. */
    /* x is now the location of the highest singular value. */
    DoubleArraysPlus.rotateArray(Vt, x);
    dim = Math.min(dim, nsig);
    for (x = 0; x<dim; x++) {
      /* multiply by matrix B first */
      A.opb(Vt[x], xv2);
      tmp0 = DoubleArraysPlus.dot(Vt[x], xv2);
      for (int ii = 0; ii<cols; ii++) xv2[ii] -= tmp0*Vt[x][ii];
      tmp0 = Math.sqrt(tmp0);
      xnorm = Math.sqrt(DoubleArraysPlus.dot(xv2, xv2));

      /* multiply by matrix A to get (scaled) left s-vector */
      A.opa(Vt[x], Ut[x]);
      tmp1 = 1.0/tmp0;
      for (int ii = 0; ii<rows; ii++) Ut[x][ii] *= tmp1;
      xnorm *= tmp1;
      bnd[i] = xnorm;
      S[x] = tmp0;
    }

    xv2 = w2 = null;
    return nsig;
    /* multiply by matrix A to get (scaled) left s-vector */

  }

  private int ritvec(SparseMatrix A, int[] steps_neig_) throws SparseSVDException {
    //  computes the singular vectors of matrix A	       *
/*  Parameters
    (input)
    nrow       number of rows of A
    steps      number of Lanczos iterations performed
    fp_out2    pointer to unformatted output file
    n	      dimension of matrix A
    kappa      relative accuracy of ritz values acceptable as 
    eigenvalues of A'A
    ritz       array of ritz values
    bnd        array of error bounds
    alf        array of diagonal elements of the tridiagonal matrix T
    bet        array of off-diagonal elements of T
    w1, w2     work space
    (output)
    xv1        array of eigenvectors of A'A (right singular vectors of A)
    ierr	      error code
    0 for normal return from imtql2()
    k if convergence did not occur for k-th eigenvalue in
    imtql2()
    nsig       number of accepted ritz values based on kappa
    (local)
    s	      work array which is initialized to the identity matrix
    of order (j + 1) upon calling imtql2().  After the call,
    s contains the orthonormal eigenvectors of the symmetric 
    tridiagonal matrix T
     */
    // int n = cols;

    double[] w1 = Vt[0];
    double[] w2 = new double[cols];
    int js = steps_neig_[0]+1;
    double[][] s = new double[js][js];
    double[] xv2 = new double[cols];
    /* initialize s to an identity matrix */
    for (int i = 0; i<js; i++) s[i][i] = 1;
    DoubleArraysPlus.copyReverse(js, alph, 0, w1, 0);
    DoubleArraysPlus.copyReverse(steps_neig_[0], bet, 1, w2, 1);
    /* on return from imtql2(), w1 contains eigenvalues in ascending 
     * order and s contains the corresponding eigenvectors */
    imtql2(js, js, w1, w2, s);
    int nsig = 0;
    int x = 0;
    for (int k = 0; k<js; k++) {
      if (bnd[k]<=KAPPA*Math.abs(ritz[k])&&k>js-steps_neig_[1]-1) {
        if (--x<0) x = dim-1;
        w1 = Vt[x];
        for (int i = 0; i<cols; i++) w1[i] = 0;
        for (int i = 0; i<js; i++) {
          storage.retrieveQ(cols, i, w2);
          for (int ii = 0; ii<cols; ii++) w1[ii] += s[k][js-1-i]*w2[ii];
        }
        /*fwrite((char *)w1, size, 1, fp_out2);*/

        /* store the w1 vector row-wise in array xv1;   
         * size of xv1 is (steps+1) * (nrow+ncol) elements 
         * and each vector, even though only ncol long,
         * will have (nrow+ncol) elements in xv1.      
         * It is as if xv1 is a 2-d array (steps+1) by     
         * (nrow+ncol) and each vector occupies a row  */

        /* j is the index in the R arrays, which are sorted by high to low 
        singular values. */

        /*for (i = 0; i < n; i++) R->Vt->value[x]xv1[id++] = w1[i];*/
        /*id += nrow;*/
        nsig++;
      }
    }
    s = null;  // libère la mémoire
  /* Rotate the singular vectors and values. */
    /* x is now the location of the highest singular value. */
    DoubleArraysPlus.rotateArray(Vt, x);
    int d = Math.min(dim, nsig);
    for (x = 0; x<d; x++) {
      /* multiply by matrix B first */
      A.opb(Vt[x], xv2);
      double tmp0 = DoubleArraysPlus.dot(Vt[x], xv2);
      for (int i = 0; i<cols; i++) xv2[i] -= tmp0*Vt[x][i];
      tmp0 = Math.sqrt(tmp0);
      double xnorm = Math.sqrt(DoubleArraysPlus.dot(xv2, xv2));
      /* multiply by matrix A to get (scaled) left s-vector */
      A.opa(Vt[x], Ut[x]);
      double tmp1 = 1/tmp0;
      for (int i = 0; i<rows; i++) Ut[x][i] *= tmp1;
      xnorm *= tmp1;
      //   bnd[i] = xnorm; bizarre ??
      S[x] = tmp0;
    }
    xv2 = w2 = null;
    return nsig;
  }

  private static void imtql2(int nm, int n, double d[], double[] e, double[][] z)
          throws SparseSVDException {
    /*   imtqlb() finds the eigenvalues  and eigenvectors of a symmetric tridiagonal
    matrix by the implicit QL method.
    Arguments 
    (input)
    nm     row dimension of the symmetric tridiagonal matrix   
    n      order of the symmetric tridiagonal matrix   
    d      contains the diagonal elements of the input matrix           
    e      contains the subdiagonal elements of the input matrix in its
    last n-1 positions.  e[0] is arbitrary
    z      contains the identity matrix
    (output)
    d      contains the eigenvalues in ascending order.  if an error
    exit is made, the eigenvalues are correct but unordered for
    indices 0,1,...ierr.
    e      has been destroyed.					    
    z      contains orthonormal eigenvectors of the symmetric   
    tridiagonal (or full) matrix.  if an error exit is made,
    z contains the eigenvectors associated with the stored 
    eigenvalues.					
    throws a SparseSVDException(j) if the j-th eigenvalue has
    not been determined after 30 iterations.		    
     */
    if (n==1) return;
    for (int i = 1; i<n; i++) e[i-1] = e[i];
    e[n-1] = 0.0;
    int nnm = n*nm;
    for (int l = 0; l<n; l++) {
      /* look for small sub-diagonal element */
      for (int iteration = 0; iteration<=30; iteration++) {
        double p = d[l];
        int m;
        for (m = l; m<n-1; m++) {// test de convergence
          double test = Math.abs(d[m])+Math.abs(d[m+1]);
          if (test+Math.abs(e[m])==test) break;
        }
        if (m==l) break;
        /* set error -- no convergence to an eigenvalue after
         * 30 iterations. */
        if (iteration==30) throw new SparseSVDException(CodeSVDException.CONVERGENCE_FAILURE, l);
        /* form shift */
        double g = (d[l+1]-p)/(2*e[l]);
        double r = Math.hypot(g, 1);
        g = d[m]-p+e[l]/(g+Math.copySign(r, g));
        double s = 1;
        double c = 1;
        p = 0;
        boolean underflow = false;
        for (int i = m-1; i>=l; i--) {
          double f = s*e[i];
          double b = c*e[i];
          r = Math.hypot(f, g);
          e[i+1] = r;
          if (r==0) {
            /*........ recover from underflow .........*/
            underflow = true;
            d[i+1] -= p;
            e[m] = 0.0;
            break;
          }
          s = f/r;
          c = g/r;
          g = d[i+1]-p;
          r = (d[i]-g)*s+2*c*b;
          p = s*r;
          d[i+1] = g+p;
          g = c*r-b;
          /* form vector */
          for (int k = 0; k<nm; k++) {
            f = z[i+1][k];
            z[i+1][k] = s*z[i][k]+c*f;
            z[i][k] = c*z[i][k]-s*f;
          }
        }
        if (!underflow) {
          d[l] -= p;
          e[l] = g;
          e[m] = 0.0;
        }
      }
    }
    /* order the eigenvalues and corresponding eigenvectors */
    DoubleArraysPlus.sort2(d, z, n, 0);
  }

  /* ------------------------
  Public Methods
   * ------------------------ */
  /** Return the left singular vectors
  @return     U
   */
  public Matrix getU() {
    return transpose ? (new Matrix(Vt, dim, cols)).transpose()
            : (new Matrix(Ut, dim, rows)).transpose();
  }

  /** Return the right singular vectors
  @return     V
   */
  public Matrix getV() {
    return transpose ? (new Matrix(Ut, dim, rows)).transpose()
            : (new Matrix(Vt, dim, cols)).transpose();
  }

  /** Return the one-dimensional array of singular values
  @return     diagonal of S.
   */
  public double[] getSingularValues() {
    return Arrays.copyOfRange(S, 0, dim);
  }
}
