package utils.nearestneighbour;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.timeseries.*;

/**
 * Extends the {@link NearestNeighbourBase} class to implement the NN-RTW
 * classification.
 * 
 * @author Atif Raza
 */
public class NearestNeighbourRTW extends NearestNeighbourBase {
    protected String numRestarts;
    
    private String hType;
    private int maxRuns;
    private double[] runTimes;
    private FileWriter fwRunTime;
    private BufferedWriter bwRunTime;
    
    /**
     * Constructs a NearestNeighbourRTW object
     * 
     * @param dsName Dataset name
     * @param outDir Directory path under results directory 
     * @param startIndex Starting instance number from Test set
     * @param numToProcess Number of Test instances to process
     * @param distPower
     * @param window
     * @param ranking
     * @param restarts
     * @param type
     * @param rngSeed
     * @param fnPostfix
     */
    public NearestNeighbourRTW(String dsName, String outDir, int startIndex,
                               int numToProcess, double distPower, int window,
                               String ranking, String restarts, String type,
                               long rngSeed, String fnPostfix) {
        // Call parent class constructor
        super(dsName, outDir, distPower, startIndex, numToProcess, window);
        // Create a TSDistanceMeasures object
        tsDM = new TSDistanceMeasures(this.testSet.get(0).size(),
                                      this.trainSet.get(0).size(),
                                      this.windowSize, rngSeed, ranking);
        tsDM.initRNGDistribution(type);
        
        this.numRestarts = restarts;
        this.hType = type;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("warping.properties"));
            this.maxRuns = Integer.parseInt(props.getProperty("num_of_experiments",
                                                              "10"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.runTimes = new double[this.maxRuns];
        
        try {
            this.filePath = this.rsltDir + this.datasetName + "_"
                            + this.windowSize + "_RTW";
            if (this.hType.equals("U")) {
                this.filePath += "_Uniform";
            } else if (this.hType.equals("G")) {
                this.filePath += "_Gaussian";
            } else if (this.hType.equals("S")) {
                this.filePath += "_SkewedNormal";
            }
            if (distPower == 0) {
                this.filePath += "_Binary";
            } else if (distPower == 1) {
                this.filePath += "_Manhattan";
            } else if (distPower == 2) {
                this.filePath += "_Euclidean";
            } else {
                this.filePath += "_" + distPower;
            }
            if (!fnPostfix.equals("")) {
                this.filePath += "_" + fnPostfix;
            }
            // this.fwTimeAndLength = new FileWriter(this.filePath +
            // "_Time_Length.csv");
            // this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
            
            this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv");
            this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
            
            this.fwRunTime = new FileWriter(this.filePath + "_RunTime.csv");
            this.bwRunTime = new BufferedWriter(this.fwRunTime);
            
            if (!this.appendResults) {
                // this.bwTimeAndLength.write("Run#,Window,Test#,Train#,CalculationTime
                // (ms),Length\n");
                this.bwAccuracy.write("Run#,Window,Test#,Actual_Class,Predicted_Class\n");
                this.bwRunTime.write("Run#,Time\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (this.windowSize == -1) {
            this.findBestWindow();
        }
    }
    
    @Override
    public void classify() {
        TSDistance warpInfo = new TSDistance();
        
        long instStartTime, instEndTime, instProcessingTime, runStartTime,
                runEndTime;
        TimeSeries test = null, train = null;
        int classPredicted, bestPathLength;
        int maxRunsLimit = 0;
        double bestDist;
        double outerStep = Math.round((endIndex - startIndex) / 10.0);
        this.startTime = System.currentTimeMillis();
        for (int runNum = 1; runNum <= maxRuns; runNum++) {
            System.out.println("\nRun Num: " + runNum);
            runStartTime = System.currentTimeMillis();
            switch (numRestarts) {
                case "I":   // Increasing Restarts
                    maxRunsLimit = runNum;
                    break;
                case "0":   // 0 Restarts
                    maxRunsLimit = 1;
                    break;
                default:    // Constant Restarts per instance pair
                    maxRunsLimit = Math.abs(Integer.parseInt(numRestarts));
            }
            for (int h = 0; h < endIndex; h += outerStep) {
                try {
                    System.out.print((int) Math.floor(100.0 * h
                                                      / (endIndex - startIndex))
                                     + "% ");
                    // this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
                    this.bwAccuracy.write(accuracy.toString());
                    this.calcTimeAndPathLen.delete(0,
                                                   calcTimeAndPathLen.length());
                    this.accuracy.delete(0, accuracy.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < outerStep && (i + h) < endIndex; i++) {
                    test = testSet.get(i + h);
                    testInstDistancesMap = new HashMap<Integer, DescriptiveStatistics>();
                    bestDist = Double.POSITIVE_INFINITY;
                    classPredicted = 0;
                    bestPathLength = 0;
                    for (int j = 0; j < trainSet.size(); j++) {
                        train = trainSet.get(j);
                        instStartTime = System.currentTimeMillis();
                        for (int instRunNum = 0; instRunNum < maxRunsLimit; instRunNum++) {
                            warpInfo = tsDM.getRTW(test, train, distFn);
                            if (warpInfo.getTSDistance() < bestDist) {
                                bestDist = warpInfo.getTSDistance();
                                classPredicted = train.getTSClass();
                                bestPathLength = warpInfo.getWarpPathLength();
                            }
                        }
                        instEndTime = System.currentTimeMillis();
                        instProcessingTime = instEndTime - instStartTime;
                        this.calcTimeAndPathLen.append(runNum + ","
                                                       + windowSizeExplicit
                                                       + "," + i + "," + j + ","
                                                       + instProcessingTime
                                                       + "," + bestPathLength
                                                       + "\n");
                        if (testInstDistancesMap.containsKey(train.getTSClass())) {
                            testInstDistancesMap.get(train.getTSClass())
                                                .addValue(warpInfo.getTSDistance());
                        } else {
                            testInstDistancesMap.put(train.getTSClass(),
                                                     new DescriptiveStatistics());
                            testInstDistancesMap.get(train.getTSClass())
                                                .addValue(warpInfo.getTSDistance());
                        }
                    }
                    DescriptiveStatistics temp;
                    Set<Entry<Integer, DescriptiveStatistics>> set = testInstDistancesMap.entrySet();
                    Iterator<Entry<Integer, DescriptiveStatistics>> iterator = set.iterator();
                    String minDistPerClass = "";
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, DescriptiveStatistics> mapEntry = (Map.Entry<Integer, DescriptiveStatistics>) iterator.next();
                        temp = (DescriptiveStatistics) mapEntry.getValue();
                        minDistPerClass += "," + temp.getMin();
                    }
                    this.accuracy.append(runNum + "," + windowSizeExplicit + ","
                                         + (i + h) + "," + test.getTSClass()
                                         + "," + classPredicted
                                         + minDistPerClass + "\n");
                }
            }
            System.out.print("100%");
            runEndTime = System.currentTimeMillis();
            runTimes[runNum - 1] += (runEndTime - runStartTime) / 1000.0;
            
            try {
                // this.bwTimeAndLength.write(this.calcTimeAndPathLen.toString());
                this.bwAccuracy.write(this.accuracy.toString());
                this.calcTimeAndPathLen.delete(0,
                                               this.calcTimeAndPathLen.length());
                this.accuracy.delete(0, this.accuracy.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.endTime = System.currentTimeMillis();
        totalTime += this.endTime - this.startTime;
        try {
            // this.bwTimeAndLength.write(this.calcTimeAndPathLen.toString());
            // this.bwTimeAndLength.close();
            this.bwAccuracy.write(this.accuracy.toString());
            this.bwAccuracy.close();
            
            double prevTime = 0;
            if (this.appendResults
                && (new File(this.filePath + "_TotalTime.csv").exists())) {
                String temp = null;
                BufferedReader brTotalTime = null;
                try {
                    brTotalTime = new BufferedReader(new FileReader(this.filePath
                                                                    + "_TotalTime.csv"));
                    while ((temp = brTotalTime.readLine()) != null) {
                        prevTime = Double.parseDouble(temp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    brTotalTime.close();
                }
            }
            this.fwTotalTime = new FileWriter(this.filePath + "_TotalTime.csv");
            this.bwTotalTime = new BufferedWriter(fwTotalTime);
            this.bwTotalTime.write((1000 * prevTime + totalTime) / 1000.0
                                   + "\n");
            this.bwTotalTime.close();
            
            for (int runNum = 1; runNum <= maxRuns; runNum++) {
                this.bwRunTime.write(runNum + "," + runTimes[runNum - 1]
                                     + "\n");
            }
            this.bwRunTime.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
