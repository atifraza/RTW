import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.DynamicTimeWarping;
import utils.dtw.WarpInfo;

public class NormalDTW {
	public static void main(String[] args) throws Exception {
		if(args.length<2) {
			System.err.println("Usage: java NormalDTW WindowSize_INT FileName\n");
			System.err.println("FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		}
		long startTime, endTime, instStartTime, instEndTime;
		startTime = System.currentTimeMillis();
		int window = Integer.parseInt(args[0]);
		String fileName = args[1];
		TimeSeries test = null, train = null;
		WarpInfo infoNorm;
		int classPredicted = 0;
		double bestDist;
		StringBuilder calcTimeAndPathLen = new StringBuilder();
		StringBuilder accuracy = new StringBuilder();
		long timeNormal = 0;
		String homeDir = System.getProperty("user.home"), //"/home/atifraza", 
			   dataDir = homeDir+"/work/data/ucr_timeseries/",
			   rsltDir = homeDir+"/work/results/ucr_timeseries/",
			   testFile = dataDir + fileName + "_TEST",
			   trainFile = dataDir + fileName + "_TRAIN";
		ArrayList<TimeSeries> testing = readData(testFile);
		ArrayList<TimeSeries> training = readData(trainFile);
		FileWriter fwTimeAndLength = new FileWriter(rsltDir+fileName+"_"+window+"_Normal"+"_Time_Length.csv");
		BufferedWriter bwTimeAndLength = new BufferedWriter(fwTimeAndLength);
		FileWriter fwAccuracy = new FileWriter(rsltDir+fileName+"_"+window+"_Normal"+"_Accuracy.csv");
		BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
		DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
		calcTimeAndPathLen.append("Window,Test#,Train#,CalculationTime (ms),Length (Normal)\n");
		accuracy.append("Window,Test#,Actual_Class,Predicted_Class\n");
		System.out.println("Processing " + fileName +
		                   " Testing Set Size: " + testing.size() +
		                   " Window Size: " + window + "\n");
		DynamicTimeWarping warp = new DynamicTimeWarping(testing.get(0).size(), training.get(0).size());
		// warm up function call
		infoNorm = warp.getNormalDTW(testing.get(0), training.get(0), distFn, window);
		for(int i=0; i<testing.size(); i++) {
			if(i%100==0) {
				System.out.print(i+" ");
				bwTimeAndLength.write(calcTimeAndPathLen.toString());
				calcTimeAndPathLen.delete(0, calcTimeAndPathLen.length());
				bwAccuracy.write(accuracy.toString());
				accuracy.delete(0, accuracy.length());
			}
			test = testing.get(i);
			bestDist = Double.POSITIVE_INFINITY;
			classPredicted = 0;
			for(int j=0; j<training.size(); j++) {
				train = training.get(j);
				instStartTime = System.currentTimeMillis();
				infoNorm = warp.getNormalDTW(test, train, distFn, window);
				if(infoNorm.getWarpDistance()<bestDist) {
					bestDist = infoNorm.getWarpDistance();
					classPredicted = train.getTSClass();
				}
				instEndTime = System.currentTimeMillis();
				timeNormal = instEndTime - instStartTime;
				calcTimeAndPathLen.append(window + "," + i + "," + j + "," + timeNormal+ "," + infoNorm.getWarpPathLength() + "\n");
			}
			accuracy.append(window + "," + i + "," + test.getTSClass() + "," + classPredicted + "\n");
		}
		bwTimeAndLength.write(calcTimeAndPathLen.toString());
		calcTimeAndPathLen.delete(0, calcTimeAndPathLen.length());
		bwAccuracy.write(accuracy.toString());
		accuracy.delete(0, accuracy.length());
		bwTimeAndLength.close();
		bwAccuracy.close();
		System.out.println("Done");
		FileWriter fwTotalTime = new FileWriter(rsltDir+fileName+"_"+window+"_Normal"+"_TotalTime.csv");
		BufferedWriter bwTotalTime = new BufferedWriter(fwTotalTime);
		endTime = System.currentTimeMillis();
		bwTotalTime.append((endTime-startTime)/1000 + "\n");
		bwTotalTime.close();
	}
	
	public static ArrayList<TimeSeries> readData(String inFile ) {
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
