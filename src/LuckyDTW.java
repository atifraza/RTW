import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class LuckyDTW {
	public static void main(String[] args) throws Exception {
		if(args.length<2) {
			System.err.println("Usage: java LuckyDTW WindowSize_INT FileName\n");
			System.err.println("FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		}
		int window = Integer.parseInt(args[0]);
		String fileName = args[1];
		TimeSeries test = null, train = null;
		WarpInfo infoLucky;
		
		int classPredicted = 0;
		double bestDist;

		StringBuilder pathLengths = new StringBuilder();
		StringBuilder accuracy = new StringBuilder();
		StringBuilder times = new StringBuilder();
		
		String temp;
		long startTime, endTime;
		long dtwLuckyTime = 0;

		String homeDir = "/home/atifraza", 
			   dataDir = homeDir+"/work/data/ucr_timeseries/",
			   rsltDir = homeDir+"/work/results/ucr_timeseries/lucky_dtw/",
			   testFile =  dataDir + fileName + "_TEST",
			   trainFile =  dataDir + fileName + "_TRAIN";

		ArrayList<TimeSeries> testing = readData(testFile);
		ArrayList<TimeSeries> training = readData(trainFile);

		FileWriter fwLength = new FileWriter(rsltDir+fileName+"_Lucky_PathLength.csv");
		BufferedWriter bwLength = new BufferedWriter(fwLength);
		FileWriter fwAccuracy = new FileWriter(rsltDir+fileName+"_Lucky_Accuracy.csv");
		BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
		FileWriter fwTimes = new FileWriter(rsltDir+fileName+"_Lucky_Times.csv");
		BufferedWriter bwTimes = new BufferedWriter(fwTimes);

		DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
		
		pathLengths.append("Window, Test#, Train#, LuckyDTW_Length\n");
		times.append("Window, Test#, Train#, LuckyDTW_Time (ms)\n");
		accuracy.append("Window, Test#, Actual_Class, Predicted_Class\n");
	
		System.out.println("Processing " + fileName +
		                   " Testing Set Size: " + testing.size() +
		                   " Window Size: " + window + "\n");
		for(int i=0; i<testing.size(); i++) {
			if(i%100==0) {
				System.out.print(i+" ");
			}
			test = testing.get(i);

			bestDist = Double.POSITIVE_INFINITY;
			classPredicted = 0;
			for(int j=0; j<training.size(); j++) {
				train = training.get(j);
				
				startTime = System.currentTimeMillis();
				infoLucky = utils.dtw.DynamicTimeWarping.getLuckyDTW(test, train, distFn, window);
				endTime = System.currentTimeMillis();
				dtwLuckyTime = endTime - startTime;
				
				if(infoLucky.getWarpDistance()<bestDist) {
					bestDist = infoLucky.getWarpDistance();
					classPredicted = train.getTSClass();
				}

				temp = window + ", " + i + ", " + j + ", ";
				pathLengths.append(temp + infoLucky.getWarpPathLength() + "\n");
				times.append(temp + dtwLuckyTime + "\n");

				bwLength.write(pathLengths.toString());
				pathLengths.delete(0, pathLengths.length());
				bwTimes.write(times.toString());
				times.delete(0, times.length());
			}

			accuracy.append(window + ", " + i + ", " + test.getTSClass() + ", " + classPredicted + "\n");
			bwAccuracy.write(accuracy.toString());
			accuracy.delete(0, accuracy.length());
		}
		bwLength.close();
		bwAccuracy.close();
		bwTimes.close();
		System.out.println("Done");
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
