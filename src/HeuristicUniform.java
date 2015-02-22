import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class HeuristicUniform {
	public static void main(String[] args) throws Exception {
		if(args.length<2) {
			System.err.println("Usage: java HeuristicUniform WindowSize_INT FileName\n");
			System.err.println("FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		}
		final int MAX_RUNS_PER_INST = 10;
		int window = Integer.parseInt(args[0]);
		String fileName = args[1];
		TimeSeries test = null, train = null;
		WarpInfo infoHeu;
		
		int classPredicted = 0;
		double bestDist;

		StringBuilder pathLengthAndTimes = new StringBuilder();
		StringBuilder accuracy = new StringBuilder();
		String temp;
		long startTime, endTime;
		long heuristicDTWTime = 0;
		double sumLength, sumTime;

		String homeDir = "/home/atif", 
				   dataDir = homeDir+"/work/data/ucr_timeseries/",
				   rsltDir = homeDir+"/work/TimeSeriesUCR/Results/",
				   testFile =  dataDir + fileName + "_TEST",
				   trainFile =  dataDir + fileName + "_TRAIN";
			
		ArrayList<TimeSeries> testing = readData(testFile);
		ArrayList<TimeSeries> training = readData(trainFile);

		FileWriter fwLengthTime = new FileWriter(rsltDir+fileName+"_UniformHeuristic_LengthTimes.csv");
		BufferedWriter bwLengthTime = new BufferedWriter(fwLengthTime);
		FileWriter fwAccuracy = new FileWriter(rsltDir+fileName+"_UniformHeuristic_Accuracy.csv");
		BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);

		DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
		
		pathLengthAndTimes.append("Window (%), Test#, Train#, AvgLen(" + MAX_RUNS_PER_INST + " runs), DTWHeuristic (ms)\n");
		accuracy.append("Window (%), Test#, Predicted_Class_Uniform, Actual_Class\n");
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
				
				sumLength = 0;
				sumTime = 0;
				for(int instRunNum = 0; instRunNum<MAX_RUNS_PER_INST; instRunNum++) {
					startTime = System.currentTimeMillis();
					infoHeu = utils.dtw.DynamicTimeWarping.getHeuristicDTW(test, train, distFn, window, 1);
					endTime = System.currentTimeMillis();
					heuristicDTWTime = endTime - startTime;
					
					sumTime += heuristicDTWTime;
					sumLength += infoHeu.getWarpPathLength();

					if(infoHeu.getWarpDistance()<bestDist) {
						bestDist = infoHeu.getWarpDistance();
						classPredicted = train.getTSClass();
					}
				}

				temp = window + ", " + i + ", " + j + ", ";
				pathLengthAndTimes.append(temp + sumLength/10 + ", " + sumTime/10 + "\n");

				bwLengthTime.write(pathLengthAndTimes.toString());
				pathLengthAndTimes.delete(0, pathLengthAndTimes.length());
			}
			accuracy.append(window + ", " + i + ", " + classPredicted + ", " + test.getTSClass() + "\n");
			bwAccuracy.write(accuracy.toString());
			accuracy.delete(0, accuracy.length());
		}
		bwLengthTime.close();
		bwAccuracy.close();
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
