/*
 * Original version by Stan Salvador stansalvador@hotmail.com
 * 
 * Updates and Modification by Atif Raza The remaining classes in this
 * package follow the structure and naming in Stan Salvador's package
 * but they are different because of different program structure.
 */
package utils.distance;

/**
 * DistanceFunctionFactory provides the DistanceFunction object
 * corresponding to the requested distance type. The static
 * DistanceFunction object for the required distance type is returned.
 * Since the objects are static members, DistanceFunctionFactory need
 * not be instantiated.
 * 
 * @author Atif Raza
 */
public class DistanceFunctionFactory {
    /**
     * EUCLIDEAN_DIST_FN is the object corresponding to the Euclidean
     * distance
     */
    public static DistanceFunction EUCLIDEAN_DIST_FN = new EuclideanDistance();
    /**
     * MANHATTAN_DIST_FN is the object corresponding to Manhattan
     * distance
     */
    public static DistanceFunction MANHATTAN_DIST_FN = new ManhattanDistance();
    /**
     * BINARY_DIST_FN is the object corresponding to Binary distance
     */
    public static DistanceFunction BINARY_DIST_FN = new BinaryDistance();
    /**
     * LpNorm_DIST_FN is the object corresponding to Lp Norm distance
     */
    public static DistanceFunction LpNorm_DIST_FN = new LpNormDistance();
    
    /**
     * Returns the DistanceFunction object corresponding to the
     * required distance type.
     * 
     * @param distFnName Distance type required. Valid values are
     *            EuclideanDistance, ManhattanDistance, BinaryDistance
     *            or LpNormDistance
     * @return the object corresponding to the input parameter
     *         distFnName
     * @throws IllegalArgumentException if distFnName is not valid
     */
    public static DistanceFunction getDistFnByName(String distFnName) {
        if (distFnName.equals("EuclideanDistance")) {
            return EUCLIDEAN_DIST_FN;
        } else if (distFnName.equals("ManhattanDistance")) {
            return MANHATTAN_DIST_FN;
        } else if (distFnName.equals("BinaryDistance")) {
            return BINARY_DIST_FN;
        } else if (distFnName.equals("LpNormDistance")) {
            return LpNorm_DIST_FN;
        } else {
            throw new IllegalArgumentException("There is no DistanceFunction for the name "
                                               + distFnName);
        }
    }
}
