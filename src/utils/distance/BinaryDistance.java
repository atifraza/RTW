package utils.distance;

public class BinaryDistance implements DistanceFunction {
	public BinaryDistance() {

	}

	public double calcDistance(double vector1, double vector2) {
		if (Math.abs(vector1-vector2)<1e-9) {
			return 0.0;
		} else {
			return 1.0;
		}
	}
}