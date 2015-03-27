package utils.dtw;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import utils.timeseries.TimeSeries;
import utils.dtw.DynamicTimeWarping;
import utils.dtw.WarpInfo;

public class NormalDTW extends BaseDTW {
	protected long startTime, endTime;
	
	protected DynamicTimeWarping warp;
	
	protected int startIndex, endIndex;
	
	public NormalDTW(String fName, int window) {
		super(fName, window);
		
		startIndex = 0;
		endIndex = testSet.size();
		
		try {
			this.filePath = this.rsltDir + this.fileName + "_" + this.windowSize + "_Normal";
			this.fwTimeAndLength = new FileWriter(this.filePath + "_Time_Length.csv");
			this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv");

			this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
			this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
			
			this.bwTimeAndLength.write("Window,Test#,Train#,CalculationTime (ms),Length\n");
			this.bwAccuracy.write("Window,Test#,Actual_Class,Predicted_Class\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setIndices(int start, int end) {
		startIndex = start;
		endIndex = end;
	}

	public void execute() {
		// warm up call
		WarpInfo warpInfo = warp.getNormalDTW(testSet.get(startIndex), trainSet.get(0), distFn, windowSize);
		StringBuilder strBldrTimePathLen = new StringBuilder(),
					  strBldrAccuracy = new StringBuilder();
		
		long instStartTime, instEndTime, instProcessingTime;
		TimeSeries test = null, train = null;
		int classPredicted;
		double bestDist;
		this.startTime = System.currentTimeMillis();
		for(int i=startIndex; i<endIndex; i++) {
			test = testSet.get(i);
			bestDist = Double.POSITIVE_INFINITY;
			classPredicted = 0;
			for(int j=0; j<trainSet.size(); j++) {
				train = trainSet.get(j);
				instStartTime = System.currentTimeMillis();
				warpInfo = warp.getNormalDTW(test, train, distFn, windowSize);
				if(warpInfo.getWarpDistance()<bestDist) {
					bestDist = warpInfo.getWarpDistance();
					classPredicted = train.getTSClass();
				}
				instEndTime = System.currentTimeMillis();
				instProcessingTime = instEndTime - instStartTime;
				strBldrTimePathLen.append(windowSize+","+i+","+j+","+instProcessingTime+","+warpInfo.getWarpPathLength()+"\n");
			}
			strBldrAccuracy.append(windowSize+","+i+","+test.getTSClass()+","+classPredicted+"\n");
		}
		calcTimeAndPathLen.append(strBldrTimePathLen.toString());
		accuracy.append(strBldrAccuracy.toString());
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
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}
}