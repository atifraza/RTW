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

public class DTW_Test {
	public static void main(String[] args) throws Exception {
		if(args.length<2) {
			System.err.println("Usage: java DTW_Test WindowSize_INT FileName\n");
			System.err.println("FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		}
		final int MAX_RUNS_PER_INST = 10;
		int window = Integer.parseInt(args[0]);
		String fileName = args[1];
		TimeSeries test = null, train = null;
		WarpInfo infoNormal = null,
				 infoUniform = null,
				 infoGaussian = null,
				 infoLucky = null;

		int classNormal = 0, classUniform = 0, classGaussian = 0, classLucky = 0;
		double bestDistNormal, bestDistUniform, bestDistGaussian, bestDistLucky;
		long pathLengthUniform, pathLengthGaussian;

		StringBuilder accuracy = new StringBuilder();
		StringBuilder pathLengths = new StringBuilder();
		StringBuilder times = new StringBuilder();

		long startTime, endTime;
		long timeNormal = 0, timeUniform = 0, timeGaussian = 0, timeLucky = 0;

		String homeDir = "/home/atif", 
			   dataDir = homeDir+"/work/data/ucr_timeseries/",
			   rsltDir = homeDir+"/work/TimeSeriesUCR/Results/",
			   testFile =  dataDir + fileName + "_TEST",
			   trainFile =  dataDir + fileName + "_TRAIN";
		
		ArrayList<TimeSeries> testing = readData(testFile);
		ArrayList<TimeSeries> training = readData(trainFile);

		FileWriter fwLengths = new FileWriter(rsltDir+fileName+"_PathLengths.csv");
		BufferedWriter bwLengths = new BufferedWriter(fwLengths);
		FileWriter fwAccuracy = new FileWriter(rsltDir+fileName+"_Accuracy.csv");
		BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
		FileWriter fwTimes = new FileWriter(rsltDir+fileName+"_Times.csv");
		BufferedWriter bwTimes = new BufferedWriter(fwTimes);

		DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");

		pathLengths.append("Window, Test#, Train#, NormalLength, UniformLength, GaussianLength, LuckyLength\n");
		times.append("Window, Test#, Train#, Normal (ms), Uniform (ms), Gaussian (ms), Lucky (ms)\n");
		accuracy.append("Window, Test#, Actual_Class, Predicted_Normal, Predicted_Uniform, Predicted_Gaussian, Predicted_Lucky\n");

		System.out.println("Processing " + fileName +
		                   " Testing Set Size: " + testing.size() +
		                   " Window Size: " + window + "\n");
		DynamicTimeWarping warp = new DynamicTimeWarping(testing.get(0).size(), training.get(0).size());
		for(int i=0; i<testing.size(); i++) {
			if(i%100==0) {
				System.out.print(i+" ");
			}
			test = testing.get(i);

			bestDistNormal = Double.POSITIVE_INFINITY;
			bestDistUniform = Double.POSITIVE_INFINITY;
			bestDistGaussian = Double.POSITIVE_INFINITY;
			bestDistLucky = Double.POSITIVE_INFINITY;
			
			classNormal = 0;
			classUniform = 0;
			classGaussian = 0;
			classLucky = 0;
			
			for(int j=0; j<training.size(); j++) {
				train = training.get(j);
				
				timeNormal = 0;
				timeUniform = 0;
				timeGaussian = 0;
				timeLucky = 0;
				
				startTime = System.currentTimeMillis();
				infoNormal = warp.getNormalDTW(test, train, distFn, window);
				endTime = System.currentTimeMillis();
				timeNormal = endTime - startTime;
				
				if(infoNormal.getWarpDistance()<bestDistNormal) {
					bestDistNormal = infoNormal.getWarpDistance();
					classNormal = train.getTSClass();
				}

				startTime = System.currentTimeMillis();
				infoLucky = warp.getLuckyDTW(test, train, distFn, window);
				endTime = System.currentTimeMillis();
				timeLucky = endTime - startTime;
				
				if(infoLucky.getWarpDistance()<bestDistLucky) {
					bestDistLucky = infoLucky.getWarpDistance();
					classLucky = train.getTSClass();
				}

				pathLengthUniform = 0;
				pathLengthGaussian = 0;
				
				for(int instRunNum = 0; instRunNum<MAX_RUNS_PER_INST; instRunNum++) {
					startTime = System.currentTimeMillis();
					infoUniform = warp.getHeuristicDTW(test, train, distFn, window, 1);
					endTime = System.currentTimeMillis();
					timeUniform += endTime - startTime;
					
					pathLengthUniform += infoUniform.getWarpPathLength();
					if(infoUniform.getWarpDistance()<bestDistUniform) {
						bestDistUniform = infoUniform.getWarpDistance();
						classUniform = train.getTSClass();
					}

					startTime = System.currentTimeMillis();
					infoGaussian = warp.getHeuristicDTW(test, train, distFn, window, 2);
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
								   (double)pathLengthGaussian/MAX_RUNS_PER_INST + ", " +
								   infoLucky.getWarpPathLength() + "\n");

				times.append(window + ", " + i + ", " + j + ", " +
							 timeNormal + ", " +
							 (double)timeUniform/MAX_RUNS_PER_INST + ", " +
							 (double)timeGaussian/MAX_RUNS_PER_INST + ", " +
							 timeLucky + "\n");

				bwLengths.write(pathLengths.toString());
				bwTimes.write(times.toString());
				pathLengths.delete(0, pathLengths.length());
				times.delete(0, times.length());
			}
			accuracy.append(window + ", " + i + ", " + test.getTSClass() + ", " +
			                classNormal + ", " + classUniform + ", " +
			                classGaussian + ", " + classLucky + "\n");
			bwAccuracy.write(accuracy.toString());
			accuracy.delete(0, accuracy.length());
		}
		bwLengths.close();
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
