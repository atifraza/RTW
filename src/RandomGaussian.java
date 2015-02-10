import java.util.Random;

public final class RandomGaussian {

	public static void main(String... aArgs) {
		Random fRandom = new Random();
		int MEAN = 1;
		double STD_DEV = 1/3.0; 
		double VARIANCE = Math.pow(STD_DEV, 2);
		double temp, sum=0;
		long idx, outside = 0, max = Long.MAX_VALUE;
		for (idx = 0; idx < max; idx++) {
			temp = MEAN + fRandom.nextGaussian() * VARIANCE;
			sum += temp;
			if (temp < 0.0 || temp > 2.0) {
				outside++;
			}
		}
		System.out.println("Mean " + sum/max);
		System.out.println("Max Gen: " + max + " Outside: " + outside);
		System.out.println("Percentage: " + 100.0*outside/max);
	}
}
