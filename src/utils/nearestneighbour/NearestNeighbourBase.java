package utils.nearestneighbour;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.timeseries.*;
import utils.distance.*;

/**
 * Provides functionality common between different {@link TimeSeries}
 * distance classes. This includes loading datasets from files,
 * keeping track of output folders, managing output file and buffer
 * writers, {@link StringBuilder} objects for per TimeSeries instance
 * accuracy and calculation time, time taken for the NN calculations
 * to finish using the particular method, window size used,
 * {@link DistanceFunction} to be used, {@link TimeSeries} distance
 * type to be used, etc.
 * 
 * @author Atif Raza
 */
public abstract class NearestNeighbourBase {
    /** Dataset to process */
    protected String datasetName;
    
    /** Program execution directory from where launched */
    protected String programDir;
    
    /** Data directory */
    protected String dataDir;
    
    /** Results directory */
    protected String rsltDir;
    
    /** File path and name excluding extension */
    protected String filePath;
    
    /** Test set */
    protected ArrayList<TimeSeries> testSet;
    
    /** Training set */
    protected ArrayList<TimeSeries> trainSet;
    
    /** Record time taken for NN classification */
    protected long totalTime;
    
    /** Saves the start time of classification process */
    protected long startTime;
    
    /** Saves the end time of classification process */
    protected long endTime;
    
    /**
     * Window size for restricting warping methods. Either user
     * provided or calculated through Leave One Out Cross Validation
     */
    protected int windowSize;
    
    /**
     * Explicitly provided window size. Used for keeping record when
     * using LOO CV for finding best window size
     */
    protected int windowSizeExplicit;
    
    /**
     * Instance number from Testing set to use as starting point when
     * running a split experiment
     */
    protected int startIndex;
    
    /**
     * Instance number from Testing set to use as finishing point when
     * running a split experiment
     */
    protected int endIndex;
    
    /**
     * Saves Calculation Time and Path Length for each instance before
     * results are saved to file
     */
    protected StringBuilder calcTimeAndPathLen;
    
    /**
     * Saves classification results for each instance before results
     * are saved to file
     */
    protected StringBuilder accuracy;
    
    /**
     * {@link DistanceFunction} to calculate distance between two time
     * points of a {@link TimeSeries}
     */
    protected DistanceFunction distFn;
    
    /**
     * {@link TSDistanceMeasures} object for calculating the distance
     * between two {@link TimeSeries} objects
     */
    protected TSDistanceMeasures tsDM;
    
    /**
     * {@link FileWriter} object for Calculation Time and Path Lengths
     */
    // protected FileWriter fwTimeAndLength;
    
    /** {@link FileWriter} object for Classification results */
    protected FileWriter fwAccuracy;
    
    /**
     * {@link FileWriter} object for Total Time taken by the NN
     * classification process using required
     * {@link TSDistanceMeasures}
     */
    protected FileWriter fwTotalTime;
    
    /**
     * {@link BufferedWriter} object for Calculation Time and Path
     * Lengths
     */
    // protected BufferedWriter bwTimeAndLength;
    
    /** {@link BufferedWriter} object for Classification results */
    protected BufferedWriter bwAccuracy;
    
    /**
     * {@link BufferedWriter} object for Total Time taken by the NN
     * classification using required {@link TSDistanceMeasures}
     */
    protected BufferedWriter bwTotalTime;
    
    /**
     * Whether results should be appended to the files or written over
     */
    protected boolean appendResults;
    
    /**
     * Saves the minimum observed distance for each test instance from
     * each class in the dataset
     */
    protected HashMap<Integer, DescriptiveStatistics> testInstDistancesMap;
    
    /**
     * Constructs a NearestNeighbourBase object and reads the test and
     * train set, assigns the correct DistanceFunction for calculating
     * distance between TimeSeries time points and creates the results
     * directory
     * 
     * @param dsName Dataset name
     * @param outDir Contains any sub-directory for saving results
     *            under the results directory
     * @param lpNormPower Lp norm power corresponding to the required
     *            distance function
     * @param startIndex Starting instance number from Test set
     * @param numToProcess Number of Test instances to process
     */
    public NearestNeighbourBase(String dsName, String outDir,
                                double lpNormPower, int startIndex,
                                int numToProcess) {
        this.datasetName = dsName;
        this.totalTime = 0;
        this.windowSize = 0;
        this.appendResults = false;
        
        this.programDir = System.getProperty("user.dir");
        this.dataDir = this.programDir + "/data/";
        this.rsltDir = this.programDir + "/results/";
        if (!outDir.equals("")) {
            this.rsltDir += outDir + "/";
        }
        File createDir = new File(this.rsltDir);
        createDir.mkdirs();
        this.testSet = readData(this.dataDir + this.datasetName + "_TEST");
        this.trainSet = readData(this.dataDir + this.datasetName + "_TRAIN");
        
        this.startIndex = startIndex;
        if (numToProcess != 0) {
            // If numToProcess is not zero then treat existing files
            // as previous results and append to them instead of
            // overwriting
            this.appendResults = true;
            this.endIndex = Math.min(this.startIndex + numToProcess,
                                     this.testSet.size());
        } else {
            //If numToProcess is zero then use the complete test set
            this.endIndex = this.testSet.size();
        }
                                      
        this.calcTimeAndPathLen = new StringBuilder();
        this.accuracy = new StringBuilder();
        if (lpNormPower == 0) {
            this.distFn = DistanceFunctionFactory.getDistFnByName("BinaryDistance");
        } else if (lpNormPower == 1) {
            this.distFn = DistanceFunctionFactory.getDistFnByName("ManhattanDistance");
        } else if (lpNormPower == 2) {
            this.distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
        } else {
            this.distFn = DistanceFunctionFactory.getDistFnByName("LpNormDistance");
            ((LpNormDistance) this.distFn).setPower(lpNormPower);
        }
    }
    
    /**
     * Constructs a NearestNeighbourBase object by invoking the
     * NearestNeighbourBase(String, String, double) constructor and
     * setting the window size parameter for the
     * {@link TSDistanceMeasure}
     * 
     * @param dsName Dataset name
     * @param outDir Contains any sub-directory for saving results
     *            under the results directory
     * @param lpNormPower Lp norm power corresponding to the required
     *            distance function
     * @param startIndex Starting instance number from Test set
     * @param numToProcess Number of Test instances to process
     * @param winSz Window size as a Percentage value
     */
    public NearestNeighbourBase(String dsName, String outDir,
                                double lpNormPower, int startIndex,
                                int numToProcess, int winSz) {
        this(dsName, outDir, lpNormPower, startIndex, numToProcess);
        this.windowSize = winSz;
        this.windowSizeExplicit = winSz;
    }
    
    /**
     * Performs Leave One Out Cross Validation over the Training set
     * to find the best window size parameter for warping methods
     */
    public void findBestWindow() {
        TimeSeries testInst;
        TimeSeries trainInst;
        
        TSDistance tsDistanceInfo;
        
        int bestWindow = 0;         // Best window percentage so far
        
        int numCorrect;   // Total correctly classified
                                    // instances for current window
        int classPredicted = 0;
        
        int prevWinLen = 0;         // Previous window length in units
        
        int currWinLen;             // Current window length in units
        
        double currError = 0;       // Classification error with
                                    // current window size
        
        double currDist;            // Distance between current TS pair
        
        double leastDist;           // Shortest distance for current test instance
        
        double leastError = Double.MAX_VALUE;   // Smallest error
                                                // achieved for
                                                // classification of
                                                // training set
        
        System.out.println("Running Cross Validation ...");
        
        for (int currWinPercent = 0; currWinPercent <= 100; currWinPercent++) {
            currWinLen = this.tsDM.setWindowSize(this.trainSet.get(0).size(),
                                                 this.trainSet.get(0).size(),
                                                 currWinPercent);
            if (currWinLen == prevWinLen) {
                continue;
                // Skip this window percentage. Window length is same
            } else {
                prevWinLen = currWinLen;
            }
            numCorrect = 0;
            for (int testInd = 0; testInd < this.trainSet.size(); testInd++) {
                testInst = this.trainSet.get(testInd);
                currDist = Double.MAX_VALUE;
                leastDist = Double.MAX_VALUE;
                for (int trainInd = 0; trainInd < this.trainSet.size(); trainInd++) {
                    if (testInd != trainInd) {
                        trainInst = this.trainSet.get(trainInd);
                        if (this instanceof NearestNeighbourDTW) {
                            tsDistanceInfo = this.tsDM.getDTW(testInst,
                                                              trainInst,
                                                              this.distFn);
                            currDist = tsDistanceInfo.getTSDistance();
                        } else if (this instanceof NearestNeighbourLTW) {
                            tsDistanceInfo = this.tsDM.getLTW(testInst,
                                                              trainInst,
                                                              this.distFn);
                            currDist = tsDistanceInfo.getTSDistance();
                        } else if (this instanceof NearestNeighbourRTW) {
                            // Single Evaluation
                            // tsDistanceInfo = this.tsDM.getRTW(testInst,
                            //                                   trainInst,
                            //                                   this.distFn);
                            // currDist = tsDistanceInfo.getWarpDistance();
                            // Multiple Evaluations
                            for (int runNum = 0; runNum < 100; runNum++) {
                                tsDistanceInfo = this.tsDM.getRTW(testInst,
                                                                  trainInst,
                                                                  this.distFn);
                                if (tsDistanceInfo.getTSDistance() < currDist) {
                                    currDist = tsDistanceInfo.getTSDistance();
                                }
                            }
                        }
                        if (currDist < leastDist) {
                            leastDist = currDist;
                            classPredicted = trainInst.getTSClass();
                        }
                    }
                }
                if (testInst.getTSClass() == classPredicted) {
                    numCorrect++;
                }
            }
            currError = (double) (this.trainSet.size() - numCorrect)
                        / this.trainSet.size();
            if (currError < leastError) {
                leastError = currError;
                bestWindow = currWinPercent;
            }
        }
        this.windowSize = bestWindow;
        try {
            BufferedWriter bwBestWindow = new BufferedWriter(new FileWriter(this.filePath
                                                                            + "_BestWindow.csv",
                                                                            false));
            bwBestWindow.write(this.windowSize + "\n");
            bwBestWindow.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        System.out.println("Best window:      " + bestWindow);
        System.out.println("Least error:      " + leastError);
    }
    
    public void findIrregularWindow() {
        int bestWindow = 0;
        double leastError = Double.MAX_VALUE;
        double dist;
        double bestDist;
        double currError = 0;
        int correctClassified;
        int classPredicted=0;
        TimeSeries testInst, trainInst;
        TSDistance warpInfo = new TSDistance();
        TSDistance bestPath;
        ArrayList<int[][]> winMinMaxList;
        int[][] windowMinMaxCurrent;
        int[] pathElement = new int[2];
        
        for(int currWindow = 0; currWindow<=100; currWindow++) {
            correctClassified = 0;
            bestPath = new TSDistance();
            winMinMaxList = new ArrayList<int[][]>();

            for(int testInd=0; testInd<trainSet.size(); testInd++) {
                dist = Double.MAX_VALUE;
                testInst = trainSet.get(testInd);
                bestDist = Double.MAX_VALUE;
                tsDM.setWindowSize(testInst.size(), testInst.size(), currWindow);
                windowMinMaxCurrent = new int[trainSet.get(0).size()][2];
                for(int i = 0; i<trainSet.get(0).size(); i++) {
                    windowMinMaxCurrent[i][0] = Integer.MAX_VALUE;
                    windowMinMaxCurrent[i][1] = Integer.MIN_VALUE;
                }
                for(int trainInd=0; trainInd<trainSet.size(); trainInd++) {
                    if(testInd != trainInd) {
                        trainInst = trainSet.get(trainInd);
                        for(int run=1; run<=10; run++) {
                            warpInfo = tsDM.getRTW(testInst, trainInst, distFn);
                            dist = warpInfo.getTSDistance();
                            if(dist<bestDist) {
                                bestDist = dist;
                                bestPath = warpInfo;
                                classPredicted = trainInst.getTSClass();
                            }
                            
                        }
                    }
                }
                for(int i=0; i<bestPath.getWarpPathLength(); i++) {
                    pathElement = bestPath.getPathElement(i);
                    if(pathElement[1]<windowMinMaxCurrent[pathElement[0]][0]) {
                        windowMinMaxCurrent[pathElement[0]][0] = pathElement[1];
                    }
                    if(pathElement[1]>windowMinMaxCurrent[pathElement[0]][1]) {
                        windowMinMaxCurrent[pathElement[0]][1] = pathElement[1]+1;
                    }
                }
                winMinMaxList.add(windowMinMaxCurrent);
                if(testInst.getTSClass()==classPredicted) {
                    correctClassified++;
                }
            }
            
            DescriptiveStatistics statsMinEdge, statsMaxEdge;
            statsMinEdge = new DescriptiveStatistics();
            statsMaxEdge = new DescriptiveStatistics();
            
            int[][] windowMinMax = new int[trainSet.get(0).size()][2];
            double medianMinEdge, medianMaxEdge, minMinEdge, maxMaxEdge;
            for(int rowInd=0; rowInd<trainSet.get(0).size(); rowInd++) {
                statsMinEdge = new DescriptiveStatistics();
                statsMaxEdge = new DescriptiveStatistics();
                for(int ind=0; ind<winMinMaxList.size(); ind++) {
                    statsMinEdge.addValue(winMinMaxList.get(ind)[rowInd][0]);
                    statsMaxEdge.addValue(winMinMaxList.get(ind)[rowInd][1]);
                }
                minMinEdge = statsMinEdge.getMin();
                medianMinEdge = statsMinEdge.getPercentile(50);
                if(minMinEdge<Math.floor(medianMinEdge-statsMinEdge.getPercentile(25))) {
                    windowMinMax[rowInd][0] = (int)Math.floor(medianMinEdge-statsMinEdge.getPercentile(25));
                } else {
                    windowMinMax[rowInd][0] = (int)minMinEdge;
                }
                maxMaxEdge = statsMaxEdge.getMax();
                medianMaxEdge = statsMaxEdge.getPercentile(50);
                if(maxMaxEdge>Math.ceil(medianMaxEdge+statsMaxEdge.getPercentile(75))) {
                    windowMinMax[rowInd][1] = (int)Math.ceil(medianMaxEdge+statsMaxEdge.getPercentile(75));
                } else {
                    windowMinMax[rowInd][1] = (int)maxMaxEdge;
                }
            }
            currError = (double)(trainSet.size()-correctClassified)/trainSet.size();
            if(currError<leastError) {
                leastError = currError;
                bestWindow = currWindow;
                System.out.print("Window Size: " + currWindow + " - Window: ");
                for(int ind=0; ind<windowMinMax.length; ind++) {
                    System.out.print(windowMinMax[ind][0]+ ","+windowMinMax[ind][1]+";");
                    System.arraycopy(windowMinMax[ind], 0, tsDM.windowMinMax[ind], 0, windowMinMax[ind].length);
                }
                System.out.println();
            }
        }
        tsDM.setIrregularFlag();
        System.out.println("Best window: " + bestWindow + ", Least error: " + leastError);
        this.windowSize = -2;
    }

    /**
     * Method for performing the classification task
     */
    public abstract void classify();
    
    /**
     * Reads the dataset file and creates the ArrayList of
     * {@link TimeSeries} objects
     * 
     * @param inFile Complete input file path
     * @return ArrayList of TimeSeries objects
     */
    private ArrayList<TimeSeries> readData(String inFile) {
        ArrayList<TimeSeries> ret = new ArrayList<TimeSeries>();
        TimeSeries temp = null;
        BufferedReader brTrain = null;
        String line = null;
        try {
            brTrain = new BufferedReader(new FileReader(inFile));
            while ((line = brTrain.readLine()) != null) {
                if (line.matches("\\s*")) {
                    continue;
                }
                line = line.trim();
                temp = new TimeSeries(line, true, ' ');
                ret.add(temp);
            }
            brTrain.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
