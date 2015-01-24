import java.util.Random;

public final class RandomGaussian {

	public static void main(String... aArgs) {
		Random fRandom = new Random();
		double MEAN = 0.5f;
		double VARIANCE = 0.075f;
		double temp, mean=0;
		long idx, outside = 0;;
		for (idx = 0; idx < Long.MAX_VALUE; idx++) {
			temp = MEAN + fRandom.nextGaussian() * VARIANCE;
			mean += temp;
			if (temp > 1.0 || temp < 0.0) {
				outside++;
				System.out.println(outside);
			}
		}
		System.out.println("Mean " + mean/idx);
		System.out.println(outside);
	}
}
