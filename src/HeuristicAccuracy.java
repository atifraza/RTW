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
		if(args.length<2) {
			System.err.println("Usage: java HeuristicAccuracy WindowSize_INT FileName\n");
			System.err.println("FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		}
		final int MAX_RUNS_PER_INST = 10;
		int window = Integer.parseInt(args[0]);
		String fileName = args[1];
		TimeSeries test = null, train = null;
		WarpInfo infoUniform = null,
				 infoGaussian = null;

		int classUniform = 0, classGaussian = 0;
		double bestDistUniform, bestDistGaussian;
		long pathLengthUniform, pathLengthGaussian;
		
		StringBuilder pathLengths = new StringBuilder();
		StringBuilder accuracy = new StringBuilder();
		StringBuilder times = new StringBuilder();
		
		long startTime, endTime, instStartTime, instEndTime;
		long timeUniform = 0, timeGaussian = 0;
		
		String homeDir = "/home/atifraza", 
			   dataDir = homeDir+"/work/data/ucr_timeseries/",
			   rsltDir = homeDir+"/work/results/ucr_timeseries/heuristic_dtw/",
			   testFile =  dataDir + fileName + "_TEST",
			   trainFile =  dataDir + fileName + "_TRAIN";
		
		ArrayList<TimeSeries> testing = readData(testFile);
		ArrayList<TimeSeries> training = readData(trainFile);
		
		FileWriter fwLengths = new FileWriter(rsltDir+"Heuristic_"+window+"_"+fileName+"_PathLengths.csv");
		BufferedWriter bwLengths = new BufferedWriter(fwLengths);
		FileWriter fwAccuracy = new FileWriter(rsltDir+"Heuristic_"+window+"_"+fileName+"_Accuracy.csv");
		BufferedWriter bwAccuracy = new BufferedWriter(fwAccuracy);
		FileWriter fwTimes = new FileWriter(rsltDir+"Heuristic_"+window+"_"+fileName+"_Times.csv");
		BufferedWriter bwTimes = new BufferedWriter(fwTimes);
		FileWriter fwTotalTime = new FileWriter(rsltDir+"Lucky_"+window+"_"+fileName+"_TotalTime.csv");
		BufferedWriter bwTotalTime = new BufferedWriter(fwTotalTime);

		DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
		
		pathLengths.append("Run#,Window,Test#,Train#,UniformLength,GaussianLength\n");
		times.append("Run#,Window,Test#,Train#,Uniform (ms),Gaussian (ms)\n");
		accuracy.append("Run#,Window,Test#,Actual_Class,Predicted_Uniform,Predicted_Gaussian\n");
		
		startTime = System.currentTimeMillis();
		for(int instRunNum = 1; instRunNum<=MAX_RUNS_PER_INST; instRunNum++) {
			System.out.println("Run #: " + instRunNum);
			System.out.println("Processing " + fileName +
			                   " Testing Set Size: " + testing.size() +
			                   " Window Size: " + window + "\n");
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
					
					instStartTime = System.currentTimeMillis();
					infoUniform = utils.dtw.DynamicTimeWarping.getHeuristicDTW(test, train, distFn, window, 1);
					instEndTime = System.currentTimeMillis();
					timeUniform += instEndTime - instStartTime;
					
					pathLengthUniform += infoUniform.getWarpPathLength();
					if(infoUniform.getWarpDistance()<bestDistUniform) {
						bestDistUniform = infoUniform.getWarpDistance();
						classUniform = train.getTSClass();
					}

					instStartTime = System.currentTimeMillis();
					infoGaussian = utils.dtw.DynamicTimeWarping.getHeuristicDTW(test, train, distFn, window, 2);
					instEndTime = System.currentTimeMillis();
					timeGaussian += instEndTime - instStartTime;
					
					pathLengthGaussian += infoGaussian.getWarpPathLength();
					if(infoGaussian.getWarpDistance()<bestDistGaussian) {
						bestDistGaussian = infoGaussian.getWarpDistance();
						classGaussian = train.getTSClass();
					}

					pathLengths.append(instRunNum + "," + window + "," + i + "," + j + "," +
									   pathLengthUniform + "," + pathLengthGaussian + "\n");

					times.append(instRunNum + "," + window + "," + i + "," + j + "," +
								 timeUniform + "," + timeGaussian + "\n");

					bwLengths.write(pathLengths.toString());
					pathLengths.delete(0, pathLengths.length());
					bwTimes.write(times.toString());
					times.delete(0, times.length());
				}
				accuracy.append(instRunNum + "," + window + "," + i + "," + test.getTSClass() + "," +
				                classUniform + "," + classGaussian + "\n");
				bwAccuracy.write(accuracy.toString());
				accuracy.delete(0, accuracy.length());
			}
			System.out.println();
		}
		endTime = System.currentTimeMillis();
		bwTotalTime.append("Total Time Used (in seconds) for processing the Dataset: " + (endTime-startTime)/1000);
		bwTotalTime.close();
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
