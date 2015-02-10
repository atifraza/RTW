import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.distance.DistanceFunctionFactory;
import utils.dtw.WarpInfo;

public class DTW_Test {
	public static void main(String[] args) throws Exception {
		{
			final int MAX_RUNS_PER_INST = 10;
			int[] windowWidth = {100, 10, 5};
			TimeSeries test = null, train = null;
//			double[][] costMatrix = null;
			WarpInfo infoNormal = null,
					 infoUniform = null,
					 infoGaussian = null;

			int classNormal = 0, classUniform = 0, classGaussian = 0;
			double bestDistNormal, bestDistUniform, bestDistGaussian;
			long pathLengthUniform, pathLengthGaussian;

			StringBuilder accuracy = new StringBuilder();
			StringBuilder pathLengths = new StringBuilder();
			StringBuilder times = new StringBuilder();

			long startTime, endTime;
			long timeNormal = 0, timeUniform = 0, timeGaussian = 0;

			int fileNum = 0;
			String testFile, trainFile, dir = "/home/atif/work/TimeSeriesUCR/", resultsDir = dir+"Results/";

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

				pathLengths.append("Window, Test#, Train#, NormalLength, UniformLength, GaussianLength\n");
				times.append("Window, Test#, Train#, Normal (ms), Uniform (ms), Gaussian (ms)\n");
				accuracy.append("Window, Test#, Actual_Class, Predicted_Normal, Predicted_Uniform, Predicted_Gaussian\n");

				for(int window : windowWidth) {
					System.out.println("Current Window Size: " + window);
					System.out.println("Testing Set Size: " + testing.size());
					for(int i=0; i<testing.size(); i++) {
						if(i%10==0) {
							System.out.print(i+" ");
						}
						test = testing.get(i);

						bestDistNormal = Double.POSITIVE_INFINITY;
						bestDistUniform = Double.POSITIVE_INFINITY;
						bestDistGaussian = Double.POSITIVE_INFINITY;
						
						classNormal = 0;
						classUniform = 0;
						classGaussian = 0;
						
						for(int j=0; j<training.size(); j++) {
							train = training.get(j);
							
							timeNormal = 0;
							timeUniform = 0;
							timeGaussian = 0;
							
							startTime = System.currentTimeMillis();
							infoNormal = utils.dtw.DynamicTimeWarping.getNormalDTW(test, train, distFn, window);
							endTime = System.currentTimeMillis();
							timeNormal = endTime - startTime;
							
							if(infoNormal.getWarpDistance()<bestDistNormal) {
								bestDistNormal = infoNormal.getWarpDistance();
								classNormal = train.getTSClass();
							}

							pathLengthUniform = 0;
							pathLengthGaussian = 0;
							
							for(int instRunNum = 0; instRunNum<MAX_RUNS_PER_INST; instRunNum++) {
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
							}

							pathLengths.append(window + ", " + i + ", " + j + ", " +
											   infoNormal.getWarpPathLength() + ", " +
											   (double)pathLengthUniform/MAX_RUNS_PER_INST + ", " +
											   (double)pathLengthGaussian/MAX_RUNS_PER_INST + "\n");

							times.append(window + ", " + i + ", " + j + ", " +
										 timeNormal + ", " +
										 (double)timeUniform/MAX_RUNS_PER_INST + ", " +
										 (double)timeGaussian/MAX_RUNS_PER_INST + "\n");

							bwLengths.write(pathLengths.toString());
							bwTimes.write(times.toString());
							pathLengths.delete(0, pathLengths.length());
							times.delete(0, times.length());
						}
						accuracy.append(window + ", " + i + ", " + test.getTSClass() + ", " +
						                classNormal + ", " + classUniform + ", " + classGaussian + "\n");
						bwAccuracy.write(accuracy.toString());
						accuracy.delete(0, accuracy.length());
					}
					System.out.println();
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
