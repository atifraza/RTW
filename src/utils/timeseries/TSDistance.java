package utils.timeseries;

import java.util.ArrayList;

/**
 * Provides the framework for keeping track of the distance and
 * warping information for {@link TimeSeries}
 * 
 * @author Atif Raza
 */
public class TSDistance {
    /** Distance value obtained for a TimeSeries pair */
    private double distance;
    /** List of Indices of TimeSeries I obtained from alignment */
    private ArrayList<Integer> tsIIndices;
    /** List of Indices of TimeSeries J obtained from alignment */
    private ArrayList<Integer> tsJIndices;
    
    /**
     * Initializes a TSDistance object
     */
    public TSDistance() {
        distance = 0;
        tsIIndices = new ArrayList<Integer>();
        tsJIndices = new ArrayList<Integer>();
    }
    
    /**
     * Adds an alignment pair to the start of alignment sequence
     * 
     * @param i Time point of TimeSeries I
     * @param j Time point of TimeSeries J
     */
    public void addToStart(int i, int j) {
        tsIIndices.add(0, i);
        tsJIndices.add(0, j);
    }
    
    /**
     * Adds an alignment pair to the end of alignment sequence
     * 
     * @param i Time point of TimeSeries I
     * @param j Time point of TimeSeries J
     */
    public void addToEnd(int i, int j) {
        tsIIndices.add(tsIIndices.size(), i);
        tsJIndices.add(tsJIndices.size(), j);
    }
    
    /**
     * Sets the distance
     * 
     * @param distance
     */
    public void setTSDistance(double distance) {
        this.distance = distance;
    }
    
    /**
     * Returns the distance
     * 
     * @return distance
     */
    public double getTSDistance() {
        return this.distance;
    }
    
    /**
     * @return Warping Path Length
     */
    public int getWarpPathLength() {
        return tsIIndices.size();
    }
    
    /**
     * Returns a String representation of the TimeSeries distance and
     * alignment (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Warping Distance: " + distance + "\nWarping path:\n");
        for (int i = 0; i < tsIIndices.size(); i++) {
            sb.append("(" + tsIIndices.get(i) + ", " + tsJIndices.get(i)
                      + ")\n");
        }
        return sb.toString();
    }
    
    /**
     * Returns a path element at required index
     * 
     * @return Warping Path Element [i, j]
     */
    public int[] getPathElement(int ind) {
        int[] elements = {tsIIndices.get(ind), tsJIndices.get(ind)};
        return elements;
    }
}
