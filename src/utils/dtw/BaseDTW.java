package utils.dtw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.*;

public class BaseDTW {
	protected String fileName,		// Filename of the data set to process
					 homeDir,		// Home directory of the user
					 dataDir,		// Data directory    |
					 rsltDir,		// Results directory | results and data are stored relative to homeDir
					 filePath;		// File path excluding the ending
	
	protected ArrayList<TimeSeries> testSet,		// Test set
									trainSet;		// Training set
	
	protected long totalTime;		// variables to calculate the time taken to calculate the DTW
	
	protected int windowSize;		// Window size to use for DTW calculation
	
	protected StringBuilder calcTimeAndPathLen,		// StringBuffer to save Calculation Time and Path Lengths 
							accuracy;				// StringBuffer to save the accuracy results
	
	protected DistanceFunction distFn;			// Distance Function to use
	
	protected DynamicTimeWarping warp;
	
	protected FileWriter //fwTimeAndLength,			// FileWriter for Calculation Time and path Lengths
						 fwAccuracy,				// FileWriter for Accuracy
						 fwTotalTime;				// FileWriter for Total Time Taken
	
	protected BufferedWriter //bwTimeAndLength,		// BufferedWriter for Calculation Time and Path Lengths
							 bwAccuracy,			// BufferedWriter for Accuracy
							 bwTotalTime;			// BufferedWriter for Total Time Taken
	
	protected boolean appendResults;
	
	protected int startIndex, endIndex;
	
    public BaseDTW(String fName, String outDir) {
        this.fileName = fName;
        this.totalTime = 0;
        this.appendResults = false;
        
        this.homeDir = System.getProperty("user.dir");
        this.dataDir = this.homeDir + "/data/";
        this.rsltDir = this.homeDir + "/results/";
        if(!outDir.equals("")) {
            this.rsltDir += outDir + "/";
        }
        File createDir = new File(this.rsltDir);
        createDir.mkdirs();
        this.testSet = readData(this.dataDir + this.fileName + "_TEST");
        this.trainSet = readData(this.dataDir + this.fileName + "_TRAIN");
        
        this.calcTimeAndPathLen = new StringBuilder();
        this.accuracy = new StringBuilder();
    }
    
	public BaseDTW(String fName, String outDir, int window, double distPower) {
		this(fName, outDir);
		this.windowSize = window;
		if(distPower == 0){
            this.distFn = DistanceFunctionFactory.getDistFnByName("BinaryDistance");
        } else if(distPower == 1) {
		    this.distFn = DistanceFunctionFactory.getDistFnByName("ManhattanDistance");
		} else if(distPower == 2) {
            this.distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
        } else {
		    this.distFn = DistanceFunctionFactory.getDistFnByName("LpNormDistance");
		    ((LpNormDistance)this.distFn).setPower(distPower);
		}
	}
	
	public void findBestWindow() {
	    int bestWindow = 0;
	    double leastError = Double.MAX_VALUE;
	    double dist;
	    double bestDist;
	    double currError = 0;
	    int correctClassified;
	    int classPredicted=0;
	    TimeSeries testInst, trainInst;
	    WarpInfo warpInfo = new WarpInfo();
	    
	    for(int currWindow = 0; currWindow<=100; currWindow++) {
	        correctClassified = 0;
	        System.err.print(currWindow + " ");
	        for(int testInd=0; testInd<trainSet.size(); testInd++) {
	            dist = Double.MAX_VALUE;
                testInst = trainSet.get(testInd);
                bestDist = Double.MAX_VALUE;
                warp.setWindowSize(testInst.size(), testInst.size(), currWindow);
	            for(int trainInd=0; trainInd<trainSet.size(); trainInd++) {
	                if(testInd != trainInd) {
                        trainInst = trainSet.get(trainInd);
                        if(this instanceof NormalDTW) {
                            warpInfo = warp.getNormalDTW(testInst, trainInst, distFn);
                            dist = warpInfo.getWarpDistance();
                        } else if(this instanceof LuckyDTW) {
                            warpInfo = warp.getLuckyDTW(testInst, trainInst, distFn);
                            dist = warpInfo.getWarpDistance();
                        } else if(this instanceof HeuristicDTW) {
                            // Single Evaluation
//                            warpInfo = warp.getHeuristicDTW(testInst, trainInst, distFn);
//                            dist = warpInfo.getWarpDistance();
                            // Multiple Evaluations
                            for(int runNum=0; runNum<100; runNum++) {
                                warpInfo = warp.getHeuristicDTW(testInst, trainInst, distFn);
                                if(warpInfo.getWarpDistance()<dist) {
                                    dist = warpInfo.getWarpDistance();
                                }
                            }
                        }
                        if(dist<bestDist) {
                            bestDist = dist;
                            classPredicted = trainInst.getTSClass();
                        }
	                }
	            }
	            if(testInst.getTSClass()==classPredicted) {
	                correctClassified++;
	            }
	        }
	        currError = (double)(trainSet.size()-correctClassified)/trainSet.size();
	        if(currError<leastError) {
	            leastError = currError;
	            bestWindow = currWindow;
	        }
	    }
	    this.windowSize = bestWindow;
	    System.out.println();
	    System.out.println("Best window: " + bestWindow + ", Least error: " + leastError);
        System.out.println();
	}
	
	public void execute() {
		
	}
	
	private ArrayList<TimeSeries> readData(String inFile ) {
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
