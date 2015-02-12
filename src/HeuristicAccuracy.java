import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class HeuristicAccuracy {
	public static void main(String[] args) throws Exception {
		{
			final int MAX_RUNS_PER_INST = 10;
			int[] windowWidth = {100, 50, 20, 15, 10, 5};
			TimeSeries test = null, train = null;
			WarpInfo infoUniform = null,
					 infoGaussian = null;

			int classUniform = 0, classGaussian = 0;
			double bestDistUniform, bestDistGaussian;
			long pathLengthUniform, pathLengthGaussian;

			StringBuilder accuracy = new StringBuilder();
			StringBuilder pathLengths = new StringBuilder();
			StringBuilder times = new StringBuilder();

			long startTime, endTime;
			long timeUniform = 0, timeGaussian = 0;

			int fileNum = 0;
			String testFile, trainFile, dir = "/home/atif/work/TimeSeriesUCR/", resultsDir = dir+"HeuristicResults/";

			while(fileNum < args.length) {
				System.out.println("Processing " + args[fileNum]);

				testFile =  dir + args[fileNum] + "_TEST";
				ArrayList<TimeSeries> testing = readData(testFile);

				trainFile =  dir + args[fileNum] + "_TRAIN";
				ArrayList<TimeSeries> training = readData(trainFile);

				FileWriter fwLengths = new FileWriter(resultsDir+args[fileNum]+"_PathLengths.csv");
				BufferedWriter bwLengths = new BufferedWriter(fwLengths);
				FileWriter fwAccuracy = new FileWriter(resultsDir+args[fileNum]+"_Accuracy.csv");
				BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
				FileWriter fwTimes = new FileWriter(resultsDir+args[fileNum]+"_Times.csv");
				BufferedWriter bwTimes = new BufferedWriter(fwTimes);
				fileNum++;

				DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");

				pathLengths.append("Run#, Window, Test#, Train#, UniformLength, GaussianLength\n");
				times.append("Run#, Window, Test#, Train#, Uniform (ms), Gaussian (ms)\n");
				accuracy.append("Run#, Window, Test#, Actual_Class, Predicted_Uniform, Predicted_Gaussian\n");

				for(int instRunNum = 1; instRunNum<=MAX_RUNS_PER_INST; instRunNum++) {
					System.out.println("Run #: " + instRunNum);
					System.out.println("Testing Set Size: " + testing.size());
					for(int window : windowWidth) {
						System.out.println("Current Window Size: " + window);
						for(int i=0; i<testing.size(); i++) {
							if(i%100==0) {
								System.out.print(i+" ");
							}
							test = testing.get(i);
	
							bestDistUniform = Double.POSITIVE_INFINITY;
							bestDistGaussian = Double.POSITIVE_INFINITY;
							
							classUniform = 0;
							classGaussian = 0;
							
							for(int j=0; j<training.size(); j++) {
								train = training.get(j);
								
								timeUniform = 0;
								timeGaussian = 0;
								
								pathLengthUniform = 0;
								pathLengthGaussian = 0;
								
								startTime = System.currentTimeMillis();
								infoUniform = utils.dtw.DynamicTimeWarping.getHeuristicDTW(test, train, distFn, window, 1);
								endTime = System.currentTimeMillis();
								timeUniform += endTime - startTime;
								
								pathLengthUniform += infoUniform.getWarpPathLength();
								if(infoUniform.getWarpDistance()<bestDistUniform) {
									bestDistUniform = infoUniform.getWarpDistance();
									classUniform = train.getTSClass();
								}
	
								startTime = System.currentTimeMillis();
								infoGaussian = utils.dtw.DynamicTimeWarping.getHeuristicDTW(test, train, distFn, window, 2);
								endTime = System.currentTimeMillis();
								timeGaussian += endTime - startTime;
								
								pathLengthGaussian += infoGaussian.getWarpPathLength();
								if(infoGaussian.getWarpDistance()<bestDistGaussian) {
									bestDistGaussian = infoGaussian.getWarpDistance();
									classGaussian = train.getTSClass();
								}
	
								pathLengths.append(instRunNum + ", " + window + ", " + i + ", " + j + ", " +
												   pathLengthUniform + ", " + pathLengthGaussian + ", " + "\n");
	
								times.append(instRunNum + ", " + window + ", " + i + ", " + j + ", " +
											 timeUniform + ", " + timeGaussian + ", " + "\n");
	
								bwLengths.write(pathLengths.toString());
								bwTimes.write(times.toString());
								pathLengths.delete(0, pathLengths.length());
								times.delete(0, times.length());
							}
							accuracy.append(instRunNum + ", " + window + ", " + i + ", " + test.getTSClass() + ", " +
							                classUniform + ", " + classGaussian + "\n");
							bwAccuracy.write(accuracy.toString());
							accuracy.delete(0, accuracy.length());
						}
						System.out.println();
					}
				}
				bwLengths.close();
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
