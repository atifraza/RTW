package utils.dtw;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import utils.timeseries.TimeSeries;
import utils.dtw.DynamicTimeWarping;
import utils.dtw.WarpInfo;

public class HeuristicDTW extends BaseDTW {
	protected long startTime, endTime;
	
	protected DynamicTimeWarping warp;
	
	protected int startIndex, endIndex;
	
	private int hType;
	private int maxRuns;
	private double[] runTimes;
	private FileWriter fwRunTime;
	private BufferedWriter bwRunTime;

	public HeuristicDTW(String fName, int window, int type) {
		super(fName, window);
		
		startIndex = 0;
		endIndex = testSet.size();

		this.hType = type;
		this.maxRuns = 10;
		this.runTimes = new double[this.maxRuns];
		warp = new DynamicTimeWarping(testSet.get(0).size(), trainSet.get(0).size());

		try {
			this.filePath = this.rsltDir + this.fileName + "_" + this.windowSize;
			if(this.hType == 1) {
				this.filePath +=  "_Uniform";
			} else if(this.hType == 2) {
				this.filePath += "_Gaussian";
			}
			this.fwTimeAndLength = new FileWriter(this.filePath + "_Time_Length.csv", true);
			this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv", true);
			this.fwRunTime = new FileWriter(this.filePath + "_RunTime.csv", true);
			
			this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
			this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
			this.bwRunTime = new BufferedWriter(this.fwRunTime);
			
//			this.bwTimeAndLength.write("Run#,Window,Test#,Train#,CalculationTime (ms),Length\n");
//			this.bwAccuracy.write("Run#,Window,Test#,Actual_Class,Predicted_Class\n");
//			this.bwRunTime.write("Run#,Time\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HeuristicDTW(String fName, int window, int type, int startIndx, int numToProcess) {
		this(fName, window, type);
		this.startIndex = startIndx;
		if(numToProcess != 0) {
			this.endIndex = Math.min(this.startIndex+numToProcess, testSet.size());
		}
	}
	
	public void execute() {
		WarpInfo warpInfo = warp.getHeuristicDTW(testSet.get(this.startIndex), trainSet.get(0), distFn, windowSize, hType);
		
		long instStartTime, instEndTime, instProcessingTime, runStartTime, runEndTime;
		TimeSeries test = null, train = null;
		int classPredicted, bestPathLength;
		double bestDist;
		this.startTime = System.currentTimeMillis();
		for(int runNum = 1; runNum<=maxRuns; runNum++) {
			runStartTime = System.currentTimeMillis();
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
					for(int instRunNum = 0; instRunNum<1; instRunNum++) {		// 0 Restarts
//					for(int instRunNum = 0; instRunNum<maxRuns; instRunNum++) {	// Constant Restarts [equal to runs]
//					for(int instRunNum = 0; instRunNum<runNum; instRunNum++) {	// Increasing Restarts
						warpInfo = warp.getHeuristicDTW(test, train, distFn, windowSize, hType);
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
			
			this.fwTotalTime = new FileWriter(this.filePath + "_TotalTime.csv");
			this.bwTotalTime = new BufferedWriter(fwTotalTime);
			this.bwTotalTime.write(totalTime/1000.0 + "\n");
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