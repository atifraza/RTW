package utils.dtw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import utils.timeseries.TimeSeries;
import utils.dtw.DynamicTimeWarping;
import utils.dtw.WarpInfo;

public class HeuristicDTW extends BaseDTW {
	protected long startTime, endTime;
	
	protected String numRestarts;
	
	private String hType;
	private int maxRuns;
	private double[] runTimes;
	private FileWriter fwRunTime;
	private BufferedWriter bwRunTime;

	public HeuristicDTW(String fName, int window, String ranking, String restarts, String type, int startIndx, int numToProcess) {
		super(fName, window);
		
		this.startIndex = startIndx;
		if(numToProcess != 0) {
			this.appendResults = true;
			this.endIndex = Math.min(this.startIndex+numToProcess, testSet.size());
		} else {
			endIndex = testSet.size();
		}
		this.numRestarts = restarts;
		this.hType = type;
		this.maxRuns = 10;
		this.runTimes = new double[this.maxRuns];
		warp = new DynamicTimeWarping(testSet.get(0).size(), trainSet.get(0).size(), ranking);

		try {
			this.filePath = this.rsltDir + this.fileName + "_" + this.windowSize;
			if(this.hType.equals("U")) {
				this.filePath +=  "_Uniform";
			} else if(this.hType.equals("N")) {
				this.filePath += "_Gaussian";
			}
			this.fwTimeAndLength = new FileWriter(this.filePath + "_Time_Length.csv");
			this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv");
			this.fwRunTime = new FileWriter(this.filePath + "_RunTime.csv");
			
			this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
			this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
			this.bwRunTime = new BufferedWriter(this.fwRunTime);
			
//			if(!this.appendResults) {
//				this.bwTimeAndLength.write("Run#,Window,Test#,Train#,CalculationTime (ms),Length\n");
//				this.bwAccuracy.write("Run#,Window,Test#,Actual_Class,Predicted_Class\n");
//				this.bwRunTime.write("Run#,Time\n");
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void execute() {
		warp.initRNGDistribution(hType);
		WarpInfo warpInfo = warp.getHeuristicDTW(testSet.get(this.startIndex), trainSet.get(0), distFn, windowSize);
		
		long instStartTime, instEndTime, instProcessingTime, runStartTime, runEndTime;
		TimeSeries test = null, train = null;
		int classPredicted, bestPathLength;
		int maxRunsLimit = 0;
		double bestDist;
		this.startTime = System.currentTimeMillis();
		for(int runNum = 1; runNum<=maxRuns; runNum++) {
			runStartTime = System.currentTimeMillis();
			switch(numRestarts) {
				case "I":
					maxRunsLimit = runNum;
					break;
				case "0":
					maxRunsLimit = 1;
					break;
				default:
					maxRunsLimit = Math.abs(Integer.parseInt(numRestarts));
			}
			for(int i=startIndex; i<endIndex; i++) {
				if(i%100==0) {
					try {
		                this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
		                this.bwAccuracy.write(accuracy.toString());
		                this.calcTimeAndPathLen.delete(0, calcTimeAndPathLen.length());
		                this.accuracy.delete(0, accuracy.length());
	                } catch (IOException e) {
		                e.printStackTrace();
	                }
				}
				test = testSet.get(i);
				bestDist = Double.POSITIVE_INFINITY;
				classPredicted = 0;
				bestPathLength = 0;
				for(int j=0; j<trainSet.size(); j++) {
					train = trainSet.get(j);
					instStartTime = System.currentTimeMillis();
//					for(int instRunNum = 0; instRunNum<1; instRunNum++) {		// 0 Restarts
//					for(int instRunNum = 0; instRunNum<maxRuns; instRunNum++) {	// Constant Restarts [equal to runs]
//					for(int instRunNum = 0; instRunNum<runNum; instRunNum++) {	// Increasing Restarts
					for(int instRunNum = 0; instRunNum<maxRunsLimit; instRunNum++) {		// 0 Restarts
						warpInfo = warp.getHeuristicDTW(test, train, distFn, windowSize);
						if(warpInfo.getWarpDistance()<bestDist) {
							bestDist = warpInfo.getWarpDistance();
							classPredicted = train.getTSClass();
							bestPathLength = warpInfo.getWarpPathLength();
						}						
					}
					instEndTime = System.currentTimeMillis();
					instProcessingTime = instEndTime - instStartTime;
					this.calcTimeAndPathLen.append(runNum+","+windowSize+","+i+","+j+","+instProcessingTime+","+bestPathLength+"\n");
				}
				this.accuracy.append(runNum+","+windowSize+","+i+","+test.getTSClass()+","+classPredicted+"\n");
			}
			runEndTime = System.currentTimeMillis();
			runTimes[runNum-1] += (runEndTime-runStartTime)/1000.0;
			
			try {
				this.bwTimeAndLength.write(this.calcTimeAndPathLen.toString());
		        this.bwAccuracy.write(this.accuracy.toString());
		        this.calcTimeAndPathLen.delete(0, this.calcTimeAndPathLen.length());
		        this.accuracy.delete(0, this.accuracy.length());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.endTime = System.currentTimeMillis();
		totalTime += this.endTime - this.startTime;
		try {
			this.bwTimeAndLength.write(this.calcTimeAndPathLen.toString());
	        this.bwTimeAndLength.close();
	        this.bwAccuracy.write(this.accuracy.toString());
			this.bwAccuracy.close();
			
	        double prevTime = 0;
	        if(this.appendResults && (new File(this.filePath + "_TotalTime.csv").exists())) {
	        	String temp = null;
	        	BufferedReader brTotalTime = null;
	        	try {
	        		brTotalTime = new BufferedReader(new FileReader(this.filePath + "_TotalTime.csv"));
	        		while((temp=brTotalTime.readLine())!=null) {
	        			prevTime=Double.parseDouble(temp);
	        		}
	        	} catch(Exception e) {
	        		e.printStackTrace();
	        	} finally {
	        		brTotalTime.close();
	        	}
	        }
			this.fwTotalTime = new FileWriter(this.filePath + "_TotalTime.csv");
			this.bwTotalTime = new BufferedWriter(fwTotalTime);
			this.bwTotalTime.write((1000*prevTime + totalTime)/1000.0 + "\n");
			this.bwTotalTime.close();
			
			for(int runNum = 1; runNum<=maxRuns; runNum++) {
				this.bwRunTime.write(runNum+","+runTimes[runNum-1]+"\n");
			}
			this.bwRunTime.close();
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}
}