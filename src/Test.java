import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class Test {
	public static void main(String[] args) throws Exception {
//		if(args.length != 3) {
//			System.out.println("Usage: Test TestingFile TrainingFile [EuclideanDistance|ManhattanDistance|BinaryDistance]");
//			System.exit(1);
//		} else
		{
			final int MAX_RUNS_PER_INST = 1;
			int[] windowWidth = {5, 10, 100};
			TimeSeries test = null, train = null;
			double[][] costMatrix = null;
			WarpInfo infoHeu;
			WarpInfo infoNorm;
			
			int classPredicted = 0;
			double bestDist;

			StringBuilder heuPathLengthResults = new StringBuilder();
			StringBuilder heuAccuracyResults = new StringBuilder();
			String temp;
			long startTime, endTime;
			long costMatTime = 0, dtwHeuTime = 0, dtwNormTime = 0;
			double sumLength, sumTime;

			int fileNum = 0;
			String testFile, trainFile, dir = "/home/atif/work/TimeSeriesUCR/";
			while(fileNum < args.length) {
				System.out.println("Processing " + args[fileNum]);
				
				testFile =  dir + args[fileNum] + "_TEST";
				ArrayList<TimeSeries> testing = readData(testFile);

				trainFile =  dir + args[fileNum] + "_TRAIN";
				ArrayList<TimeSeries> training = readData(trainFile);

				FileWriter fwLengthTime = new FileWriter(dir+args[fileNum]+"_AvgLength+Times.csv");
				BufferedWriter bwLengthTime = new BufferedWriter(fwLengthTime);
				FileWriter fwAccuracy = new FileWriter(dir+args[fileNum]+"_Accuracy.csv");
				BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
				fileNum++;

				DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
				
				heuPathLengthResults.append("Window (%), Test#, Train#, AvgLen(" + MAX_RUNS_PER_INST + " run), NormLen, MatCalc (ms), DTWHeuristic (ms), DTWNormal (ms)\n");
				heuAccuracyResults.append("Window (%), Test#, Predicted_Class, Actual_Class\n");
				for(int window : windowWidth) {
					System.out.println("Current Window Size: " + window);
//					for(int dsRunNum = 0; dsRunNum<MAX_RUNS_PER_DATASET; dsRunNum++) {
						for(int i=0; i<testing.size(); i++) {
							//System.out.print(i + " ");
							
							test = testing.get(i);
							bestDist = Double.POSITIVE_INFINITY;
							classPredicted = 0;
							for(int j=0; j<training.size(); j++) {
								train = training.get(j);
								
								startTime = System.currentTimeMillis();
								costMatrix = utils.dtw.HeuristicDTW.calculateCostMatrix(test, train, distFn, window);
								endTime = System.currentTimeMillis();
								costMatTime = endTime - startTime;
								
								sumLength = 0;
								sumTime = 0;
								for(int instRunNum = 0; instRunNum<MAX_RUNS_PER_INST; instRunNum++) {
									startTime = System.currentTimeMillis();
									infoHeu = utils.dtw.HeuristicDTW.getDTW(costMatrix, test.size(), train.size(), true);
									endTime = System.currentTimeMillis();
									dtwHeuTime = endTime - startTime;
									
									sumTime += dtwHeuTime;
									sumLength += infoHeu.getWarpPathLength();

									if(infoHeu.getWarpDistance()<bestDist) {
										bestDist = infoHeu.getWarpDistance();
										classPredicted = train.getTSClass();
									}
								}

								startTime = System.currentTimeMillis();
								infoNorm = utils.dtw.HeuristicDTW.getDTW(costMatrix, test.size(), train.size(), false);
								endTime = System.currentTimeMillis();
								dtwNormTime = endTime - startTime;
								
								temp = window + ", " + i + ", " + j + ", ";
								heuPathLengthResults.append(temp +
								                       sumLength/10 + ", " +
								                       infoNorm.getWarpPathLength() + ", " +
								                       costMatTime + ", " + sumTime/10 +  ", " + dtwNormTime + "\n");
								//heuAccuracyResults.append(temp);

								bwLengthTime.write(heuPathLengthResults.toString());
								heuPathLengthResults.delete(0, heuPathLengthResults.length());
							}
							heuAccuracyResults.append(window + ", " + i + ", " + classPredicted + ", " + test.getTSClass() + "\n");
							bwAccuracy.write(heuAccuracyResults.toString());
							heuAccuracyResults.delete(0, heuAccuracyResults.length());
						}
//					}
					System.out.println();
				}
				bwLengthTime.close();
				bwAccuracy.close();
				System.out.println("Done");
			}
		}
	}
	
//	public static String calcStatistics(ArrayList<Integer> wpLength) {
//		long sum = 0;
//		for (Integer i : wpLength) {
//			sum += i.longValue();
//		}
//		double mean = (double) sum/wpLength.size();
//		double stdev = 0;
//		for (Integer i : wpLength) {
//			stdev += Math.pow(mean-i, 2);
//		}
//		stdev = Math.sqrt(stdev/wpLength.size());
//		return "Mean: " + mean + " Std Dev: " + stdev;
//	}
	
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