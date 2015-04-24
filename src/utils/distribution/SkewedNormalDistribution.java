package utils.distribution;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
//import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
//import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;

/**
 * Implementation of the skewed normal (gaussian) distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Skew_normal_distribution"> Skewed Normal distribution (Wikipedia)</a>
 * @see <a href="http://azzalini.stat.unipd.it/SN/"> The Skew-Normal Probability Distribution</a>
 */


public class SkewedNormalDistribution extends AbstractRealDistribution {
    /**
	 * 
	 */
    private static final long serialVersionUID = -6652210374979944218L;
    /** Mean of this distribution. */
    private double mean;
    /** Standard deviation of this distribution. */
    private double standardDeviation;
    /** Skew of this distribution. */
    private double skew;
    /** Location of this distribution. */
    private double location;
    /** Scale of this distribution. */
    private double scale;
    /** Shape of this distribution. */
    private double shape;
    /** The value of BETA and other variables used again and again are made static and final for better performance */
    private static final double BETA = 2.0 - FastMath.PI/2.0;
    private static final double BETA_Squared_CubeRooted = FastMath.pow(BETA*BETA, 1.0/3.0);
    private static final double PI_by_2_Squared = FastMath.sqrt(FastMath.PI/2.0);
//    /** The value of {@code log(sd) + 0.5*log(2*pi)} stored for faster computation. */
//    private final double logStandardDeviationPlusHalfLog2Pi;
//    /** Inverse cumulative probability accuracy. */
//    private final double solverAbsoluteAccuracy;
//    /** &radic;(2) */
//    private static final double SQRT2 = FastMath.sqrt(2.0);

    /**
     * Create a skew normal distribution with mean equal to zero, standard
     * deviation equal to one and skew equal to zero.
     * <p>
     * <b>Note:</b> this constructor will implicitly create an instance of
     * {@link Well19937c} as random generator to be used for sampling only (see
     * {@link #sample()} and {@link #sample(int)}). In case no sampling is
     * needed for the created distribution, it is advised to pass {@code null}
     * as random generator via the appropriate constructors to avoid the
     * additional initialisation overhead.
     */
    public SkewedNormalDistribution() {
        this(0, 1, 0);
    }

    /**
     * Create a normal distribution using the given mean, standard deviation and
     * inverse cumulative distribution accuracy.
     * <p>
     * <b>Note:</b> this constructor will implicitly create an instance of
     * {@link Well19937c} as random generator to be used for sampling only (see
     * {@link #sample()} and {@link #sample(int)}). In case no sampling is
     * needed for the created distribution, it is advised to pass {@code null}
     * as random generator via the appropriate constructors to avoid the
     * additional initialisation overhead.
     *
     * @param mean Mean for this distribution.
     * @param sd Standard deviation for this distribution.
     * @param inverseCumAccuracy Inverse cumulative probability accuracy.
     * @throws NotStrictlyPositiveException if {@code sd <= 0}.
     * @since 2.1
     */
    public SkewedNormalDistribution(double mean, double sd, double sk)
        throws NotStrictlyPositiveException {
        this(new Well19937c(), mean, sd, sk);
    }

    /**
     * Creates a normal distribution.
     *
     * @param rng Random number generator.
     * @param mean Mean for this distribution.
     * @param sd Standard deviation for this distribution.
     * @param inverseCumAccuracy Inverse cumulative probability accuracy.
     * @throws NotStrictlyPositiveException if {@code sd <= 0}.
     * @since 3.1
     */
    public SkewedNormalDistribution(RandomGenerator rng,
                                    double mean,
                                    double sd,
                                    double sk)
        throws NotStrictlyPositiveException, NumberIsTooLargeException {
        super(rng);

        if (sd <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.STANDARD_DEVIATION, sd);
        }

//        final double MAX_SKEW = maxSkew();
//        
//        if (FastMath.abs(sk)>MAX_SKEW) {
//        	throw new NumberIsTooLargeException(LocalizedFormats.ARGUMENT_OUTSIDE_DOMAIN, sk, FastMath.copySign(MAX_SKEW, sk), false);
//        }
//        
        this.updateParams(mean, sd, sk);
    }
    
    public void updateParams(double mn, double sd, double sk) {
        double skew_Squared_CubeRooted = FastMath.pow(sk*sk, 1.0/3.0);        
        double eps_Squared = skew_Squared_CubeRooted/(skew_Squared_CubeRooted+BETA_Squared_CubeRooted);
        double eps = FastMath.copySign(FastMath.sqrt(eps_Squared), sk);
        double delta = eps * PI_by_2_Squared;
        
        this.mean = mn;
        this.standardDeviation = sd;
        this.skew = sk;
        
        this.shape = delta/FastMath.sqrt(1.0-delta*delta);
        this.scale = sd/FastMath.sqrt(1.0-eps*eps);
        this.location = mean - this.scale*eps;        
    }
    
//    /**
//     * Determines the maximum limit for skewness.
//     *
//     */
//    private double maxSkew() {
//    	double eps = FastMath.sqrt(2.0/FastMath.PI);
//    	return BETA * FastMath.pow(eps, 3.0) / FastMath.pow(1-eps*eps, 3.0/2.0) - 1e-16;
//    }

    /**
     * Access the mean.
     *
     * @return the mean for this distribution.
     */
    public double getMean() {
        return mean;
    }

    /**
     * Access the standard deviation.
     *
     * @return the standard deviation for this distribution.
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Access the skew.
     *
     * @return the mean for this distribution.
     */
    public double getSkew() {
        return skew;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
    	double u1 = this.random.nextGaussian(),
    		   u2 = this.random.nextGaussian();
    	if (u2 > this.shape*u1) {
    		u1 *= -1.0;
    	}
    	
    	return this.location + this.scale * u1;
    	
    }

	@Override
    public double getNumericalMean() {
	    return getMean();
    }

	@Override
    public double getNumericalVariance() {
	    // TODO Auto-generated method stub
	    return 0;
    }

	@Override
    public double cumulativeProbability(double x) {
	    // TODO Auto-generated method stub
	    return 0;
    }

	@Override
    public double density(double x) {
	    // TODO Auto-generated method stub
	    return 0;
    }

	@Override
    public double getSupportLowerBound() {
	    // TODO Auto-generated method stub
	    return 0;
    }

	@Override
    public double getSupportUpperBound() {
	    // TODO Auto-generated method stub
	    return 0;
    }

	@Override
    public boolean isSupportConnected() {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public boolean isSupportLowerBoundInclusive() {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public boolean isSupportUpperBoundInclusive() {
	    // TODO Auto-generated method stub
	    return false;
    }
}
