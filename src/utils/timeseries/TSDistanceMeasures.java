package utils.timeseries;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
//import org.apache.commons.math3.util.FastMath;        // SkewedNormal

//import utils.distribution.SkewedNormalDistribution;   // SkewedNormal
import utils.distance.DistanceFunction;

/**
 * Implements {@link TimeSeries} distance measures. ED, DTW, LTW and
 * RTW are implemented.<br/>
 * The Use of SkewedNormalDistribution has been tested but commented
 * in RTW implementation.
 * 
 * @author Atif Raza
 */
public class TSDistanceMeasures {
    /** Saves the cost matrix for DTW, LTW or RTW */
    private double[][] costMatrix;
    
    /**
     * Length of warping window in cells/units. Computed from the
     * window percentage passed
     */
    private int windowLen;
    /**
     * Ranking method for RTW. 1. Linear, *2. Exponential
     */
    private int rankingMethod = 2;
    
    /**
     * Random Number Generator for drawing Random Numbers from
     * required Probability Distribution
     */
    private RandomGenerator rng;
    
    /** Distribution for drawing Random Numbers */
    private AbstractRealDistribution distrib;
    
    /** Lower limit for Uniform distribution */
    private double lowerLim = 0;
    /** Upper limit for Uniform distribution */
    private double upperLim = 2;
    
    /** Mean value for Gaussian distribution */
    private double mean = 1.0;
    /** Standard Deviation for Gaussian distribution */
    private double std_dev = 1.0 / 3.0;
    
    /** Whether to save the warping path or not */
    private boolean saveWarpPath = false;
    
    // EXPERIMENTATION STUFF BELOW
    /** Whether to ignore cell distances and use pure randomization */
    private boolean isPureRandom = false;
    // private int distributionType;    // SkewedNormal
    // private double SKEW = 0;         // SkewedNormal
    
    /**
     * Constructs a TSDistanceMeasure object by initializing cost
     * matrix as per provided time series lengths and calculates the
     * window length according to the window percentage passed in.<br>
     * Used directly for LTW and DTW but called from Constructor for
     * RTW
     * 
     * @param tsILen Length of TimeSeries I
     * @param tsJLen Length of TimeSeries J
     * @param windowPercent Window size as percentage of TimeSeries
     *            length
     */
    public TSDistanceMeasures(int tsILen, int tsJLen, int windowPercent) {
        costMatrix = new double[tsILen][tsJLen];
        setWindowSize(tsILen, tsJLen, windowPercent);
    }
    
    /**
     * Constructs TSDistanceMeasures object for RTW, Calls
     * TSDistanceMeasures(int, int, int) for initialization of cost
     * matrix and window length calculation. Loads Properties file
     * warping.properties and uses std_dev (double) and/or
     * is_pure_random (true/false) keys (if present) to update std_dev
     * and/or isPureRandom. Initializes the ranking method and Random
     * Number Generator with the required seed
     * 
     * @param tsILen Length of TimeSeries I
     * @param tsJLen Length of TimeSeries J
     * @param windowPercent Window size as percentage of TS length
     * @param rngSeed Seed for RNG, -1: automatic, >=0: integer seed
     * @param rankingMethod L: Linear, E: Exponential
     */
    public TSDistanceMeasures(int tsILen, int tsJLen, int windowPercent,
                              long rngSeed, String rankingMethod) {
        this(tsILen, tsJLen, windowPercent);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("warping.properties"));
            if (props.containsKey("std_dev")) {
                this.std_dev = Double.parseDouble(props.getProperty("std_dev"));
            }
            if (props.containsKey("is_pure_random")) {
                this.isPureRandom = Boolean.valueOf(props.getProperty("is_pure_random",
                                                                      "false"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (rngSeed == -1) {
            this.rng = new Well19937c();
        } else {
            this.rng = new Well19937c(rngSeed);
        }
        
        if (rankingMethod.equals("L")) {
            this.rankingMethod = 1;
        } else if (rankingMethod.equals("E")) {
            this.rankingMethod = 2;
        }
    }
    
    /**
     * Set window size for restricting calculation of cost matrix
     * entries to neighborhood of diagonal path
     * 
     * @param tsILen Length of TimeSeries I
     * @param tsJLen Length of TimeSeries J
     * @param windowPercent Window size as percentage of TS length
     * @return Window length in cells/time points
     */
    public int setWindowSize(int tsILen, int tsJLen, int windowPercent) {
        // if windowPercent is 0 or -1(for leave one out cv)
        if (windowPercent == 0 || windowPercent == -1) {
            this.windowLen = 1;
        } else {
            this.windowLen = (int) Math.ceil(windowPercent * tsILen / 100.0);
        }
        return this.windowLen;
    }
    
    /**
     * Initializes the Probability Distribution for drawing Random
     * Numbers
     * 
     * @param distribution U: Uniform, G: Normal
     */
    public void initRNGDistribution(String distribution) {
        switch (distribution) {
            case "U":
                this.distrib = new UniformRealDistribution(this.rng,
                                                           this.lowerLim,
                                                           this.upperLim);
                // this.distributionType = 1; // SkewedNormal
                break;
            case "G":
                this.distrib = new NormalDistribution(this.rng, this.mean,
                                                      this.std_dev);
                // this.distributionType = 2; // SkewedNormal
                break;
            // case "S": // SkewedNormal
            // this.distrib = new SkewedNormalDistribution(this.rng, this.mean,
            // this.std_dev, this.SKEW);
            // this.distributionType = 3;
            // break;
        }
    }
    
    // SkewedNormal
    // private void updateProbDist(int i, int j, int tsLen) {
    // if (this.distributionType == 3) {
    // this.SKEW = 10.0 * FastMath.pow((i - j), 2) / tsLen;
    // // Aggressive skewness: (double)(i-j)/tsLen;
    // ((SkewedNormalDistribution) distrib).updateParams(this.mean, this.std_dev,
    // this.SKEW);
    // }
    // }
    
    /**
     * Ranks the distances on a [0, 2] scale so that each distance
     * value gets a corresponding slice from this range as per it's
     * magnitude and also with respect to the ranking method. If
     * cRight, cDiag, cDown are 1, 2, 3 respectively, the linear
     * ranking would produce 0.833, 0.667, 0.5, the exponential
     * ranking will produce 1.733, 0.234, 0.031
     * 
     * @param cRight Distance value for moving right
     * @param cDiag Distance value for moving diagonally
     * @param cDown Distance value for moving down
     * @return Array of doubles with ranks in range [0, 2]
     */
    private double[] rankCandidates(double cRight, double cDiag, double cDown) {
        // array of ranks, only 2 values need to be calculated because
        // the sum of values is 2 always and val[0] = x, val[1] = x+y,
        // and val[2] would have been 2-x+y eventually so no use
        // calculating
        double[] ranks = new double[2];
        
        double costSum;
        if (this.isPureRandom) {
            cRight = 1.0;
            cDiag = 1.0;
            cDown = 1.0;
        }
        if (this.rankingMethod == 2) {  // Exponentiate costs
            // Scaling factor, IF Pure Random 0 makes all costs 1
            double alpha = this.isPureRandom ? 0.0 : 2.0;
            
            cRight = Math.exp(-alpha * cRight);
            cDiag = Math.exp(-alpha * cDiag);
            cDown = Math.exp(-alpha * cDown);
        }
        costSum = cDiag + cRight + cDown;   // Take sum of costs
        if (this.rankingMethod == 1) {
            double eps = 1e-9;      // For avoiding situations when
            double eps3x = 3e-9;    // distances zero or similar
                                    // extremes
            ranks[0] = (costSum - cRight + eps) / (costSum + eps3x);
            ranks[1] = (costSum - cDiag + eps) / (costSum + eps3x) + ranks[0];
        } else if (this.rankingMethod == 2) {
            costSum /= 2.0;     // Required for exponential scaling
            ranks[0] = cRight / costSum;
            ranks[1] = cDiag / costSum + ranks[0];
            if (Double.isNaN(ranks[0]) && Double.isNaN(ranks[1])) {
                // IF distances and sums are zero this need to be done
                // to avoid being stuck in a loop
                ranks[0] = 0.5;
                ranks[1] = 1.5;
            }
        }
        return ranks;
    }
    
    /**
     * Calculates the RTW distance
     * 
     * @param tsI TimeSeries instance from test set
     * @param tsJ TimeSeries instance from train set
     * @param distFn DistanceFunction
     * @return TSDistance object with distance and alignment
     */
    public TSDistance getRTW(TimeSeries tsI, TimeSeries tsJ,
                             DistanceFunction distFn) {
        // Maximum index for TimeSeries I and J
        int maxI = tsI.size();
        int maxJ = tsJ.size();
        // Starting indices - i,j = 0
        int i = 0;
        int j = 0;
        
        TSDistance distInfo = new TSDistance(); // Distance/WarpPath
        
        double costDiag, costRight, costDown;	// cost variables
        
        for (double[] current : this.costMatrix) {
            // Assign positive infinity to entire matrix
            Arrays.fill(current, Double.POSITIVE_INFINITY);
        }
        
        double[] ranks = new double[2]; // Used to save probabilities
                                        // of the direction to take
        double rnd;
        boolean isValidCellChosen;
        
        this.costMatrix[0][0] = distFn.calcDistance(tsI.get(i), tsJ.get(j));
        
        if (this.saveWarpPath) {
            distInfo.addToEnd(i, j);
        }
        
        while (i < maxI && j < maxJ) {
            // Check if diagonal move is possible
            if (i + 1 < maxI && j + 1 < maxJ) {
                costDiag = distFn.calcDistance(tsI.get(i + 1), tsJ.get(j + 1));
            } else {
                costDiag = 1e12;
            }
            
            // If going down a row is possible
            // if winLen = 2, col (j) = 0, row (i) = 2, then
            // winLen (2) + col (0) = 2; so going down is not possible
            if (i + 1 < Math.min(this.windowLen + j, maxI)) {
                costDown = distFn.calcDistance(tsI.get(i + 1), tsJ.get(j));
            } else {
                costDown = 1e12;
            }
            
            // If going right a col is possible
            // if winLen = 2, row (i) = 0, col (j) = 2, then
            // winLen (2) + row (0) = 2; so going right is not
            // possible
            if (j + 1 < Math.min(this.windowLen + i, maxJ)) {
                costRight = distFn.calcDistance(tsI.get(i), tsJ.get(j + 1));
            } else {
                costRight = 1e12;
            }
            isValidCellChosen = false;
            // rank the direction costs to [0, 2] scale
            ranks = this.rankCandidates(costRight, costDiag, costDown);
            
            while (!isValidCellChosen) {
                // looping required for times when we are at the end
                // of warping path but a valid cell can't be chosen
                
                rnd = this.distrib.sample(); // draw random number
                
                if (rnd < ranks[0]
                    && j + 1 < Math.min(this.windowLen + i, maxJ)) {
                    // if rnd is less than rank for right cell and
                    // the right cell is inside the window or cost
                    // matrix
                    this.costMatrix[i][j + 1] = this.costMatrix[i][j]
                                                + costRight;
                    j++;
                    isValidCellChosen = true;
                } else if (rnd > ranks[1]
                           && i + 1 < Math.min(this.windowLen + j, maxI)) {
                    // if rnd is greater than rank for diagonal cell
                    // and the down cell is inside the window or cost
                    // matrix
                    this.costMatrix[i + 1][j] = this.costMatrix[i][j]
                                                + costDown;
                    i++;
                    isValidCellChosen = true;
                } else if (i + 1 < maxI && j + 1 < maxJ) {
                    // if rnd is neither smaller than rank for right
                    // and nor greater than rank for diagonal then
                    // check it must be less tha rank for diagonal
                    // check if diagonal move is possible
                    this.costMatrix[i + 1][j + 1] = this.costMatrix[i][j]
                                                    + costDiag;
                    i++;
                    j++;
                    isValidCellChosen = true;
                }
                if (isValidCellChosen) {
                    if (this.saveWarpPath) {
                        distInfo.addToEnd(i, j);
                    }
                    Arrays.fill(ranks, 0);  // reinitialize the ranks
                                            // array to all zeros
                    // SkewedNormal
                    // if (this.distributionType == 3) {
                    // updateProbDist(i, j, maxI);
                    // }
                    break;
                }
            }
            if (i + 1 == maxI && j + 1 == maxJ) {
                distInfo.setTSDistance(this.costMatrix[i][j]);
                break;
            }
        }
        return distInfo;
    }
    
    /**
     * Calculates the DTW distance
     * 
     * @param tsI TimeSeries instance from test set
     * @param tsJ TimeSeries instance from train set
     * @param distFn DistanceFunction
     * @return TSDistance object with distance and alignment
     */
    public TSDistance getDTW(TimeSeries tsI, TimeSeries tsJ,
                             DistanceFunction distFn) {
        // Maximum index for TimeSeries I and J
        int maxI = tsI.size();
        int maxJ = tsJ.size();
        
        TSDistance distInfo = new TSDistance(); // Distance/WarpPath
        
        double costDiag, costLeft, costUp;    // cost variables
        
        for (double[] current : this.costMatrix) {
            // Assign positive infinity to entire matrix
            Arrays.fill(current, Double.POSITIVE_INFINITY);
        }
        this.costMatrix[0][0] = distFn.calcDistance(tsI.get(0), tsJ.get(0));
        // Calculate the top row from column 1 to windowLen
        for (int c = 1; c < Math.min(this.windowLen, maxJ); c++) {
            // cell(0,c) = cell(0,c-1) + dist(tsI(0), tsJ(c))
            this.costMatrix[0][c] = this.costMatrix[0][c - 1]
                                    + distFn.calcDistance(tsI.get(0),
                                                          tsJ.get(c));
        }
        
        // First loop set calculates full cost matrix or upto
        // windowLen for restricted case then hands over to 
        // second loop set
        // Calculate cost matrix cells for row 1 to windowLen
        // Calculate the left column from row 1 to windowLen
        // and then calculate row cells
        for (int r = 1; r < Math.min(this.windowLen, maxI); r++) {
            // cell(r,0) = cell(r-1,0) + dist(tsI(r), tsJ(0))
            this.costMatrix[r][0] = this.costMatrix[r - 1][0]
                                    + distFn.calcDistance(tsI.get(r),
                                                          tsJ.get(0));
            for (int c = 1; c < Math.min(r + this.windowLen, maxJ); c++) {
                // cell(r,c) = min( (r,c-1), (r-1,c-1), (r-1,c) )
                //             + dist(tsI(0), tsJ(c))
                this.costMatrix[r][c] = distFn.calcDistance(tsI.get(r),
                                                            tsJ.get(c))
                                        + shortestPrevDist(r, c);
            }
        }
        
        if (this.windowLen < maxI) {
            // Second loop set
            // Calculate cost matrix cells for row >= windowLen to
            // row n - windowLen
            int k = 1;
            for (int r = this.windowLen; r < maxI - this.windowLen; r++, k++) {
                for (int c = k; c < r + this.windowLen; c++) {
                    this.costMatrix[r][c] = distFn.calcDistance(tsI.get(r),
                                                                tsJ.get(c))
                                            + shortestPrevDist(r, c);
                }
            }
            // Third loop set
            // Calculate cost matrix cells for row n - windowLen to n
            for (int r = maxI - this.windowLen; r < maxI; r++, k++) {
                for (int c = k; c < maxJ; c++) {
                    this.costMatrix[r][c] = distFn.calcDistance(tsI.get(r),
                                                                tsJ.get(c))
                                            + shortestPrevDist(r, c);
                }
            }
        }
        distInfo.setTSDistance(this.costMatrix[maxI - 1][maxJ - 1]);
        
        // IF saving warp path then add the warping path entries
        if (saveWarpPath) {
            // Starting indices - i,j = n,m
            int i = tsI.size() - 1;
            int j = tsJ.size() - 1;
            
            distInfo.addToStart(i, j);
            while ((i > 0) || (j > 0)) {
                // Check if diagonal move is possible
                if ((i > 0) && (j > 0)) {
                    costDiag = costMatrix[i - 1][j - 1];
                } else {
                    costDiag = Double.POSITIVE_INFINITY;
                }
                // If going left is possible
                if (j > 0) {
                    costLeft = costMatrix[i][j - 1];
                } else {
                    costLeft = Double.POSITIVE_INFINITY;
                }
                // If going up is possible
                if (i > 0) {
                    costUp = costMatrix[i - 1][j];
                } else {
                    costUp = Double.POSITIVE_INFINITY;
                }
                // Prefer moving diagonally and moving towards the
                // i==j axis of the matrix if there are ties.
                if ((costDiag <= costLeft) && (costDiag <= costUp)) {
                    i--;
                    j--;
                } else if ((costLeft < costDiag) && (costLeft < costUp)) {
                    j--;
                } else if ((costUp < costDiag) && (costUp < costLeft)) {
                    i--;
                } else if (i <= j) { // leftCost==rightCost > diagCost
                    i--;
                } else { // leftCost==rightCost > diagCost
                    j--;
                }
                distInfo.addToStart(i, j);
            }
        }
        return distInfo;
    }
    
    /**
     * Returns the shortest distance from diagonal, upper and left cell
     * 
     * @param i Row position
     * @param j Column position
     * @return Shortest value from diagonal, up and left cells
     */
    private double shortestPrevDist(int i, int j) {
        return Math.min(costMatrix[i - 1][j - 1],
                        Math.min(costMatrix[i - 1][j], costMatrix[i][j - 1]));
    }
    
    /**
     * Calculates the Euclidean distance without warping.
     * 
     * @param tsI TimeSeries instance from test set
     * @param tsJ TimeSeries instance from train set
     * @param distFn DistanceFunction
     * @return TSDistance object with distance and alignment
     */
    public TSDistance getED(TimeSeries tsI, TimeSeries tsJ,
                            DistanceFunction distFn) {
        // TODO Use BSF for early abandoning
        double dist = 0.0;
        for (int i = 0; i < tsI.size(); i++) {
            dist += distFn.calcDistance(tsI.get(i), tsJ.get(i));
            // if (dist > bsf) {
            //     break;
            // }
        }
        TSDistance distInfo = new TSDistance();
        distInfo.setTSDistance(dist);
        if (saveWarpPath) {
            for (int i = 0; i < tsI.size(); i++) {
                distInfo.addToEnd(i, i);
            }
        }
        return distInfo;
    }
    
    /**
     * Calculates the LTW distance
     * 
     * @param tsI TimeSeries instance from test set
     * @param tsJ TimeSeries instance from train set
     * @param distFn DistanceFunction
     * @return TSDistance object with distance and alignment
     */
    public TSDistance getLTW(TimeSeries tsI, TimeSeries tsJ,
                             DistanceFunction distFn) {
        // Maximum index for TimeSeries I and J
        int maxI = tsI.size();
        int maxJ = tsJ.size();
        // Starting indices - i,j = 0
        int i = 0;
        int j = 0;
        
        TSDistance distInfo = new TSDistance(); // Distance/WarpPath
        
        double costDiag, costRight, costDown;   // cost variables
        
        for (double[] current : this.costMatrix) {
            // Assign positive infinity to entire matrix
            Arrays.fill(current, Double.POSITIVE_INFINITY);
        }
        this.costMatrix[0][0] = distFn.calcDistance(tsI.get(i), tsJ.get(j));
        
        // IF saving warp path then add first entry as per LTW.
        if (this.saveWarpPath) {
            distInfo.addToStart(i, j);
        }
        
        while (i < maxI && j < maxJ) {
            // Check if diagonal move is possible
            if ((i + 1 < maxI) && (j + 1 < maxJ)) {
                costDiag = distFn.calcDistance(tsI.get(i + 1), tsJ.get(j + 1));
            } else {
                costDiag = 1e12;
            }
            
            // If going down a row is possible
            // if winLen = 2, col (j) = 0, row (i) = 2, then
            // winLen (2) + col (0) = 2; so going down is not possible
            if (i + 1 < Math.min(windowLen + j, maxI)) {
                costDown = distFn.calcDistance(tsI.get(i + 1), tsJ.get(j));
            } else {
                costDown = 1e12;
            }
            
            // If going right a col is possible
            // if winLen = 2, row (i) = 0, col (j) = 2, then
            // winLen (2) + row (0) = 2; so going right is not possible
            if (j + 1 < Math.min(windowLen + i, maxJ)) {
                costRight = distFn.calcDistance(tsI.get(i), tsJ.get(j + 1));
            } else {
                costRight = 1e12;
            }
            
            // Decide the move
            if ((costDiag <= costRight) && (costDiag <= costDown)) {
                // if diag is best or equal to either
                this.costMatrix[i + 1][j + 1] = this.costMatrix[i][j]
                                                + costDiag;
                i++;
                j++;
            } else if ((costRight < costDiag) && (costRight < costDown)) {
                // if right is shortest
                this.costMatrix[i][j + 1] = this.costMatrix[i][j] + costRight;
                j++;
            } else if ((costDown < costDiag) && (costDown < costRight)) {
                // if down is shortest
                this.costMatrix[i + 1][j] = this.costMatrix[i][j] + costDown;
                i++;
            } else { // costDown==costRight > costDiag
                if (Math.random() < 0.5) {
                    // Go down
                    this.costMatrix[i + 1][j] = this.costMatrix[i][j]
                                                + costDown;
                    i++;
                } else {
                    // Go right
                    this.costMatrix[i][j + 1] = this.costMatrix[i][j]
                                                + costRight;
                    j++;
                }
            }
            if (this.saveWarpPath) {
                distInfo.addToEnd(i, j);
            }
            
            if (i + 1 == maxI && j + 1 == maxJ) {
                // set the last cell entry as TimeSeries distance
                distInfo.setTSDistance(this.costMatrix[i][j]);
                break;
            }
        }
        return distInfo;
    }
}
