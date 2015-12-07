package utils.timeseries;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * TimeSeries is used to save individual instances of time series in
 * an ArrayList along with the class label. It saves the time series
 * as Double values
 * 
 * @author Atif Raza
 */
public class TimeSeries {
    /**
     * Saves the class label for the TimeSeries instance
     */
    private int tsClass;
    /**
     * Saves the time series values
     */
    private ArrayList<Double> ts;
    
    /**
     * Instantiates a TimeSeries object with no data saved in it
     */
    public TimeSeries() {
        tsClass = 0;
        ts = new ArrayList<Double>();
    }
    
    /**
     * Instantiates the TimeSeries object from the String passed.
     * 
     * @param line String of time series with or without the label as
     *            first value
     * @param isFirstColClass Whether first value is the class label
     *            or not
     * @param delimiter Separator between values in the time series
     *            String
     */
    public TimeSeries(String line, boolean isFirstColClass, char delimiter) {
        this();
        StringTokenizer st = new StringTokenizer(line,
                                                 String.valueOf(delimiter));
        if (isFirstColClass) {
            tsClass = (int) Double.parseDouble(st.nextToken());
        }
        while (st.hasMoreTokens()) {
            ts.add(Double.parseDouble(st.nextToken()));
        }
    }
    
    /**
     * Query the TimeSeries object for it's length
     * 
     * @return the length of the TimeSeries object
     */
    public int size() {
        return ts.size();
    }
    
    /**
     * Get the TimeSeries value at time point {@code index}
     * 
     * @param index Time point of value required from the TimeSeries
     * @return Value of TimeSeries at time point {@code index}
     */
    public double get(int index) {
        return ts.get(index).doubleValue();
    }
    
    /**
     * Query the TimeSeries for it's class label
     * 
     * @return Class label of the TimeSeries object
     */
    public int getTSClass() {
        return tsClass;
    }
    
    /**
     * Returns the TimeSeries object as a String with it's class label
     * and length
     * 
     * @return TimeSeries as a String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Time Series:\n" + ts + "\n");
        str.append("Class of Time Series is: " + tsClass + "\n");
        str.append("Length is: " + ts.size() + "\n");
        
        return str.toString();
    }
}
