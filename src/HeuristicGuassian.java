import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class HeuristicGuassian {
	public static void main(String[] args) throws Exception {
//		if(args.length != 3) {
//			System.out.println("Usage: Test TestingFile TrainingFile [EuclideanDistance|ManhattanDistance|BinaryDistance]");
//			System.exit(1);
//		} else
		{
			final int MAX_RUNS_PER_INST = 10;
			int[] windowWidth = {100, 10, 5};
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

			int fileNum = 0;
			String testFile, trainFile, dir = "/home/atif/work/TimeSeriesUCR/", resultsDir = dir+"Results/";
			while(fileNum < args.length) {
				System.out.println("Processing " + args[fileNum]);
				
				testFile =  dir + args[fileNum] + "_TEST";
				ArrayList<TimeSeries> testing = readData(testFile);

				trainFile =  dir + args[fileNum] + "_TRAIN";
				ArrayList<TimeSeries> training = readData(trainFile);

				FileWriter fwLengthTime = new FileWriter(resultsDir+args[fileNum]+"_GuassianHeuristic_LengthTimes.csv");
				BufferedWriter bwLengthTime = new BufferedWriter(fwLengthTime);
				FileWriter fwAccuracy = new FileWriter(resultsDir+args[fileNum]+"_GuassianHeuristic_Accuracy.csv");
				BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
				fileNum++;

				DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
				
				pathLengthAndTimes.append("Window (%), Test#, Train#, AvgLen(" + MAX_RUNS_PER_INST + " runs), DTWHeuristic (ms)\n");
				accuracy.append("Window (%), Test#, Predicted_Class_Uniform, Actual_Class\n");
				for(int window : windowWidth) {
					System.out.println("Current Window Size: " + window);
					System.out.println("Testing Set Size: " + testing.size());
					for(int i=0; i<testing.size(); i++) {
						System.out.print(".");
						if((i+1)%100==0) {
							System.out.println();
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
								infoHeu = utils.dtw.DynamicTimeWarping.getHeuristicDTW(test, train, distFn, window, 2);
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
					System.out.println();
				}
				bwLengthTime.close();
				bwAccuracy.close();
				System.out.println("Done");
			}
		}
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
