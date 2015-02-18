import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class NormalDTW {
	public static void main(String[] args) throws Exception {
//		if(args.length != 3) {
//			System.out.println("Usage: Test TestingFile TrainingFile [EuclideanDistance|ManhattanDistance|BinaryDistance]");
//			System.exit(1);
//		} else
		{
			int[] windowWidth = {100, 50, 20, 15, 10, 5};
			TimeSeries test = null, train = null;
			double[][] costMatrix = null;
			WarpInfo infoNorm;
			
			int classPredicted = 0;
			double bestDist;

			StringBuilder pathLengths = new StringBuilder();
			StringBuilder accuracy = new StringBuilder();
			StringBuilder times = new StringBuilder();
			
			String temp;
			long startTime, endTime;
			long dtwNormTime = 0;

			int fileNum = 0;
			String homeDir = "/home/atif", 
				   dataDir = homeDir+"/work/data/ucr_timeseries/",
				   rsltDir = homeDir+"/work/TimeSeriesUCR/Results/",
				   testFile, trainFile;
			
			while(fileNum < args.length) {
				System.out.println("Processing " + args[fileNum]);
				
				testFile =  dataDir + args[fileNum] + "_TEST";
				ArrayList<TimeSeries> testing = readData(testFile);

				trainFile =  dataDir + args[fileNum] + "_TRAIN";
				ArrayList<TimeSeries> training = readData(trainFile);

				FileWriter fwLengthTime = new FileWriter(rsltDir+args[fileNum]+"_DTW_PathLength.csv");
				BufferedWriter bwLength = new BufferedWriter(fwLengthTime);
				FileWriter fwAccuracy = new FileWriter(rsltDir+args[fileNum]+"_DTW_Accuracy.csv");
				BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
				FileWriter fwTimes = new FileWriter(rsltDir+args[fileNum]+"_DTW_Times.csv");
				BufferedWriter bwTimes = new BufferedWriter(fwTimes);

				fileNum++;

				DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
				
				pathLengths.append("Window, Test#, Train#, DTW_Length\n");
				times.append("Window, Test#, Train#, DTW_Time (ms)\n");
				accuracy.append("Window, Test#, Actual_Class, Predicted_Class\n");
				
				for(int window : windowWidth) {
					System.out.println("Current Window Size: " + window);
					System.out.println("Testing Set Size: " + testing.size());
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
							costMatrix = utils.dtw.DynamicTimeWarping.calculateCostMatrix(test, train, distFn, window);
							infoNorm = utils.dtw.DynamicTimeWarping.getNormalDTW(costMatrix, test.size(), train.size());
							endTime = System.currentTimeMillis();
							dtwNormTime = endTime - startTime;
							
							if(infoNorm.getWarpDistance()<bestDist) {
								bestDist = infoNorm.getWarpDistance();
								classPredicted = train.getTSClass();
							}

							temp = window + ", " + i + ", " + j + ", ";
							pathLengths.append(temp + dtwNormTime + "\n");
							times.append(temp + dtwNormTime + "\n");

							bwLength.write(pathLengths.toString());
							pathLengths.delete(0, pathLengths.length());
							bwTimes.write(times.toString());
							times.delete(0, times.length());
						}

						accuracy.append(window + ", " + i + ", " + classPredicted + ", " + test.getTSClass() + "\n");
						bwAccuracy.write(accuracy.toString());
						accuracy.delete(0, accuracy.length());
					}
					System.out.println();
				}
				bwLength.close();
				bwAccuracy.close();
				bwTimes.close();
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
