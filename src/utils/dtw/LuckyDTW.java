package utils.dtw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.timeseries.TimeSeries;
import utils.dtw.DynamicTimeWarping;
import utils.dtw.WarpInfo;

public class LuckyDTW extends BaseDTW {
	protected long startTime, endTime;
	
	public LuckyDTW(String fName, String outDir, double distPower, int window, int startIndx, int numToProcess) {
		super(fName, outDir, distPower, window);
        warp = new DynamicTimeWarping(testSet.get(0).size(), trainSet.get(0).size(), this.windowSize);
		
		this.startIndex = startIndx;
		if(numToProcess != 0) {
			this.appendResults = true;
			this.endIndex = Math.min(this.startIndex+numToProcess, testSet.size());
		} else {
			endIndex = testSet.size();
		}
		
		try {
			this.filePath = this.rsltDir + this.fileName + "_" + this.windowSize + "_Lucky";
            if(distPower == 0) {
                this.filePath += "_Binary";
            } else if(distPower == 1) {
                this.filePath += "_Manhattan";
            } else if(distPower == 2) {
                this.filePath += "_Euclidean";
            } else {
                this.filePath += "_"+distPower;
            }
			//this.fwTimeAndLength = new FileWriter(this.filePath + "_Time_Length.csv", this.appendResults);
			this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv", this.appendResults);

			//this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
			this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
			
			if(!this.appendResults) {
				//this.bwTimeAndLength.write("Window,Test#,Train#,CalculationTime (ms),Length\n");
				this.bwAccuracy.write("Window,Test#,Actual_Class,Predicted_Class\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.windowSizeExplicit = this.windowSize;
        if(this.windowSize == -1) {
            this.findBestWindow();
        }
	}
	
	public void execute() {
		WarpInfo warpInfo = new WarpInfo();
		WarpInfo bestWarping = new WarpInfo();
		
		long instStartTime, instEndTime, instProcessingTime;
		TimeSeries test = null, train = null;
		int classPredicted;
		double bestDist;
		double outerStep = Math.round((endIndex-startIndex)/10.0);
		this.startTime = System.currentTimeMillis();
        for(int h=0; h<endIndex; h+=outerStep) {
            try {
                System.out.print((int)Math.floor(100.0*h/(endIndex-startIndex)) + "% ");
                //this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
                this.bwAccuracy.write(accuracy.toString());
                this.calcTimeAndPathLen.delete(0, calcTimeAndPathLen.length());
                this.accuracy.delete(0, accuracy.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
    		for(int i=0; i<outerStep && (i+h)<endIndex; i++) {
    			test = testSet.get(i+h);
    			testInstDistancesMap = new HashMap<Integer, DescriptiveStatistics>();
    			bestDist = Double.POSITIVE_INFINITY;
    			classPredicted = 0;
    			for(int j=0; j<trainSet.size(); j++) {
    				train = trainSet.get(j);
    				instStartTime = System.currentTimeMillis();
    				warpInfo = warp.getLuckyDTW(test, train, distFn);
    				if(warpInfo.getWarpDistance()<bestDist) {
    					bestDist = warpInfo.getWarpDistance();
    					classPredicted = train.getTSClass();
    					bestWarping = warpInfo;
    				}
    				instEndTime = System.currentTimeMillis();
    				instProcessingTime = instEndTime - instStartTime;
    				this.calcTimeAndPathLen.append(windowSizeExplicit+","+i+","+j+","+instProcessingTime+","+warpInfo.getWarpPathLength()+"\n");
                    if(testInstDistancesMap.containsKey(train.getTSClass())) {
                        testInstDistancesMap.get(train.getTSClass()).addValue(bestWarping.getWarpDistance());
                    } else {
                        testInstDistancesMap.put(train.getTSClass(), new DescriptiveStatistics());
                        testInstDistancesMap.get(train.getTSClass()).addValue(bestWarping.getWarpDistance());
                    }
                }
                DescriptiveStatistics temp;
                Set<Entry<Integer, DescriptiveStatistics>> set = testInstDistancesMap.entrySet();
                Iterator<Entry<Integer, DescriptiveStatistics>> iterator = set.iterator();
                String minDistPerClass = "";
                while(iterator.hasNext()) {
                    Map.Entry<Integer, DescriptiveStatistics> mapEntry = (Map.Entry<Integer, DescriptiveStatistics>)iterator.next();
                    temp = (DescriptiveStatistics)mapEntry.getValue();
                    minDistPerClass += "," + temp.getMin();
                }
    			this.accuracy.append(windowSizeExplicit+","+(i+h)+","+test.getTSClass()+","+classPredicted+minDistPerClass+"\n");
    		}
        }
        System.out.print("100%");
		this.endTime = System.currentTimeMillis();
		totalTime += this.endTime - this.startTime;
		try {
			//this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
	        //this.bwTimeAndLength.close();
	        this.bwAccuracy.write(accuracy.toString());
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
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}
}