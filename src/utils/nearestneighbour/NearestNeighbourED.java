package utils.nearestneighbour;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.timeseries.*;

/**
 * Extends the {@link NearestNeighbourBase} class to implement the NN-ED
 * classification.
 * 
 * @author Atif Raza
 */
public class NearestNeighbourED extends NearestNeighbourBase {
    /**
     * Constructs a NearestNeighbourED object
     * 
     * @param dsName Dataset name
     * @param outDir Directory path under results directory 
     * @param startIndex Starting instance number from Test set
     * @param numToProcess Number of Test instances to process
     */
    public NearestNeighbourED(String dsName, String outDir, int startIndex,
                              int numToProcess) {
        // Call parent class constructor
        super(dsName, outDir, 2, startIndex, numToProcess);
        // Create a TSDistanceMeasures object
        this.tsDM = new TSDistanceMeasures(this.testSet.get(0).size(),
                                           this.trainSet.get(0).size(),
                                           this.windowSize);
        
        try {
            this.filePath = this.rsltDir + this.datasetName + "_"
                            + this.windowSize + "_ED";
            // this.fwTimeAndLength = new FileWriter(this.filePath +
            // "_Time_Length.csv", this.appendResults);
            // this.bwTimeAndLength = new BufferedWriter(this.fwTimeAndLength);
            
            this.fwAccuracy = new FileWriter(this.filePath + "_Accuracy.csv",
                                             this.appendResults);
            this.bwAccuracy = new BufferedWriter(this.fwAccuracy);
            
            if (!this.appendResults) {
                // this.bwTimeAndLength.write("Window,Test#,Train#," +
                //                            + "CalcTime(ms),Length\n");
                this.bwAccuracy.write("Window,Test#,Actual_Class,Predicted_Class\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see utils.nearestneighbour.NearestNeighbourBase#classify()
     */
    @Override
    public void classify() {
        TSDistance warpInfo = new TSDistance();
        
        long instStartTime, instEndTime, instProcessingTime;
        TimeSeries test = null, train = null;
        int classPredicted;
        double bestDist;
        double outerStep = ((endIndex - startIndex) < 10)
                                                          ? (endIndex - startIndex)
                                                          : Math.round((endIndex - startIndex)
                                                                       / 10.0);
        this.startTime = System.currentTimeMillis();
        for (int h = startIndex; h < endIndex; h += outerStep) {
            System.out.print((int) Math.floor(100.0 * (h - startIndex)
                                              / (endIndex - startIndex))
                             + "% ");
            try {
                // this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
                this.bwAccuracy.write(accuracy.toString());
                this.calcTimeAndPathLen.delete(0, calcTimeAndPathLen.length());
                this.accuracy.delete(0, accuracy.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < outerStep && (i + h) < endIndex; i++) {
                test = testSet.get(i + h);
                testInstDistancesMap = new HashMap<Integer, DescriptiveStatistics>();
                bestDist = Double.POSITIVE_INFINITY;
                classPredicted = 0;
                for (int j = 0; j < trainSet.size(); j++) {
                    train = trainSet.get(j);
                    instStartTime = System.currentTimeMillis();
                    warpInfo = tsDM.getED(test, train, distFn);
                    if (warpInfo.getTSDistance() < bestDist) {
                        bestDist = warpInfo.getTSDistance();
                        classPredicted = train.getTSClass();
                    }
                    instEndTime = System.currentTimeMillis();
                    instProcessingTime = instEndTime - instStartTime;
                    this.calcTimeAndPathLen.append(windowSize + "," + i + ","
                                                   + j + ","
                                                   + instProcessingTime + ","
                                                   + warpInfo.getWarpPathLength()
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
                this.accuracy.append(windowSize + "," + (i + h) + ","
                                     + test.getTSClass() + "," + classPredicted
                                     + minDistPerClass + "\n");
            }
        }
        System.out.print("100%");
        this.endTime = System.currentTimeMillis();
        totalTime += this.endTime - this.startTime;
        try {
            // this.bwTimeAndLength.write(calcTimeAndPathLen.toString());
            // this.bwTimeAndLength.close();
            this.bwAccuracy.write(accuracy.toString());
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
