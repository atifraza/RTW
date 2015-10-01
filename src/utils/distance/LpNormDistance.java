package utils.distance;

public class LpNormDistance implements DistanceFunction {
    private double power;
    public LpNormDistance() {
        power = 0.5;

    }
    
    public void setPower(double pow) {
        this.power = pow;
    }

    public double calcDistance(double vector1, double vector2) {
        double diffSum = 0.0;
        diffSum = Math.pow(Math.abs(vector1 - vector2), power);

        return diffSum;
    }

}
