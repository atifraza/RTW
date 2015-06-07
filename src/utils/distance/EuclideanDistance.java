package utils.distance;

public class EuclideanDistance implements DistanceFunction {
	public EuclideanDistance() {

	}

	public double calcDistance(double vector1, double vector2) {
		double sqSum = 0.0;
		sqSum = Math.pow(vector1-vector2, 2);

		return sqSum;
	}
}