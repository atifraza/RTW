package utils.dtw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;

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
	
	protected FileWriter fwTimeAndLength,			// FileWriter for Calculation Time and path Lengths
						 fwAccuracy,				// FileWriter for Accuracy
						 fwTotalTime;				// FileWriter for Total Time Taken
	
	protected BufferedWriter bwTimeAndLength,		// BufferedWriter for Calculation Time and Path Lengths
							 bwAccuracy,			// BufferedWriter for Accuracy
							 bwTotalTime;			// BufferedWriter for Total Time Taken
	
	public BaseDTW(String fName, int window) {
		this.fileName = fName;
		this.windowSize = window;
		this.totalTime = 0;
		
		this.homeDir = System.getProperty("user.home");
		this.dataDir = this.homeDir + "/work/data/ucr_timeseries/";
		this.rsltDir = this.homeDir + "/work/results/ucr_timeseries/";
		this.testSet = readData(this.dataDir + this.fileName + "_TEST");
		this.trainSet = readData(this.dataDir + this.fileName + "_TRAIN");
		
		this.calcTimeAndPathLen = new StringBuilder();
		this.accuracy = new StringBuilder();
		this.distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
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