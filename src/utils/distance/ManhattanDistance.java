package utils.distance;

public class ManhattanDistance implements DistanceFunction {
	public ManhattanDistance() {

	}

	public double calcDistance(double vector1, double vector2) {
		double diffSum = 0.0;
		diffSum = Math.abs(vector1 - vector2);

		return diffSum;
	}

}