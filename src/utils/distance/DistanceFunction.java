package utils.distance;

/**
 * The DistanceFunction interface provides a function calcDistance for
 * finding the Lp norm distance between two rational numbers with the
 * required p
 * 
 * @author Atif Raza
 */
public interface DistanceFunction {
    /**
     * calcDistance is supposed to be implemented in the class
     * implementing the DistanceFunction interface to provide the
     * specific implementation
     * 
     * @param point1
     * @param point2
     * @return Lp norm distance
     */
    public double calcDistance(double point1, double point2);
}
