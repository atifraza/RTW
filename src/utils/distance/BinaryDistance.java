package utils.distance;

/**
 * BinaryDistance implements the DistanceFunction interface to provide
 * the Binary distance between two points. If the two points are
 * equal, then the distance is 0 and 1 otherwise
 * 
 * @author Atif Raza
 */
public class BinaryDistance implements DistanceFunction {
    /**
     * Implementation of the function calcDistance defined in
     * DistanceFunction If the two parameters are equal then the
     * distance between them is 0. Otherwise the distance is 1.
     * 
     * @param point1
     * @param point2
     * @return (double)0/1
     *         
     * @see utils.distance.DistanceFunction#calcDistance(double,
     *      double)
     */
    public double calcDistance(double point1, double point2) {
        return (Math.abs(point1 - point2) < 1e-9) ? 0.0 : 1.0;
    }
}
