package utils.distance;

/**
 * ManhattanDistance implements the DistanceFunction interface to
 * provide the Manhattan or Taxicab distance between two points
 * 
 * @author Atif Raza
 */
public class ManhattanDistance implements DistanceFunction {
    /**
     * Implementation of the function calcDistance defined in
     * DistanceFunction Manhattan distance is the L1 distance
     * (|p1-p2|^1)
     * 
     * @param point1
     * @param point2
     * @return |p1-p2|
     *         
     * @see utils.distance.DistanceFunction#calcDistance(double,
     *      double)
     */
    public double calcDistance(double point1, double point2) {
        return Math.abs(point1 - point2);
    }
}
