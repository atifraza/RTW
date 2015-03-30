package utils.dtw;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import utils.timeseries.TimeSeries;
import utils.dtw.DynamicTimeWarping;
import utils.dtw.WarpInfo;

public class LuckyDTW extends BaseDTW {
	protected long startTime, endTime;
	
	protected DynamicTimeWarping warp;
	
	protected int startIndex, endIndex;
	
	public LuckyDTW(String fName, int window) {
		super(fName, window);
		
		startIndex = 0;
		endIndex = testSet.size();
		warp = new DynamicTimeWarping(testSet.get(0).size(), trainSet.get(0).size());

		try {
			this.filePath = this.rsltDir + this.fileName + "_" + this.windowSize + "_Lucky";
			this.fwTimeAndLength = new FileWriter(this.filePath + "_Time_Length.csv", true);
			this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv", true);

			this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
			this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
			
//			this.bwTimeAndLength.write("Window,Test#,Train#,CalculationTime (ms),Length\n");
//			this.bwAccuracy.write("Window,Test#,Actual_Class,Predicted_Class\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LuckyDTW(String fName, int window, int startIndx, int numToProcess) {
		this(fName, window);
		this.startIndex = startIndx;
		if(numToProcess != 0) {
			this.endIndex = Math.min(this.startIndex+numToProcess, testSet.size());
		}
	}
	
	public void execute() {
		// warm up call
		WarpInfo warpInfo = warp.getLuckyDTW(testSet.get(this.startIndex), trainSet.get(0), distFn, windowSize);
		
		long instStartTime, instEndTime, instProcessingTime;
		TimeSeries test = null, train = null;
		int classPredicted;
		double bestDist;
		this.startTime = System.currentTimeMillis();
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
			for(int j=0; j<trainSet.size(); j++) {
				train = trainSet.get(j);
				instStartTime = System.currentTimeMillis();
				warpInfo = warp.getLuckyDTW(test, train, distFn, windowSize);
				if(warpInfo.getWarpDistance()<bestDist) {
					bestDist = warpInfo.getWarpDistance();
					classPredicted = train.getTSClass();
				}
				instEndTime = System.currentTimeMillis();
				instProcessingTime = instEndTime - instStartTime;
				this.calcTimeAndPathLen.append(windowSize+","+i+","+j+","+instProcessingTime+","+warpInfo.getWarpPathLength()+"\n");
			}
			this.accuracy.append(windowSize+","+i+","+test.getTSClass()+","+classPredicted+"\n");
		}
		this.endTime = System.currentTimeMillis();
		totalTime += this.endTime - this.startTime;
		try {
			this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
	        this.bwTimeAndLength.close();
	        this.bwAccuracy.write(accuracy.toString());
	        this.bwAccuracy.close();
	        
			this.fwTotalTime = new FileWriter(this.filePath + "_TotalTime.csv");
			this.bwTotalTime = new BufferedWriter(fwTotalTime);
			this.bwTotalTime.write(totalTime/1000.0 + "\n");
			this.bwTotalTime.close();
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}
}