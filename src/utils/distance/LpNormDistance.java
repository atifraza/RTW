package utils.distance;

/**
 * LpNormDistance implements the DistanceFunction interface to provide
 * the Lp distance between two points with p set to a positive value
 * greater than 0
 * 
 * @author Atif Raza
 */
public class LpNormDistance implements DistanceFunction {
    /**
     * Variable to save the power attribute for Lp norm distance
     */
    private double power = 0.5;
    
    /**
     * setPower takes a single parameter to set the power attribute.
     * The value of power should be greater than 0
     * 
     * @param power
     */
    public void setPower(double power) {
        if (power > 0) {
            this.power = power;
        }
        // TODO if negative valued power is provided, either raise an
        // exception or notify user of alternate value set
    }
    
    /**
     * Implementation of the function calcDistance defined in
     * DistanceFunction Lp norm distance is the difference between two
     * points raised to the p Since Lp distance uses the p'th root
     * which is a convex function omitting the root provides the same
     * effect in distance calculations
     * 
     * @param point1
     * @param point2
     * @return (p1-p2)^2
     *         
     * @see utils.distance.DistanceFunction#calcDistance(double,
     *      double)
     */
    public double calcDistance(double point1, double point2) {
        return Math.pow(Math.abs(point1 - point2), power);
    }
}
