package utils.distance;

/**
 * EuclideanDistance implements the DistanceFunction interface to
 * provide the Euclidean (L2 norm) distance between two points.
 * 
 * @author Atif Raza
 */
public class EuclideanDistance implements DistanceFunction {
    /**
     * Implementation of the function calcDistance defined in
     * DistanceFunction Euclidean distance is the square of the
     * difference between two points Since Euclidean distance uses the
     * squares root which is a convex function omitting the root
     * provides the same effect in distance calculations
     * 
     * @param point1
     * @param point2
     * @return (p1-p2)^2
     *         
     * @see utils.distance.DistanceFunction#calcDistance(double,
     *      double)
     */
    public double calcDistance(double point1, double point2) {
        return Math.pow(point1 - point2, 2);
    }
}
