package utils.dtw;

import java.util.Arrays;



//import java.util.Locale;
//import java.text.DecimalFormat;
//import java.text.DecimalFormatSymbols;
//import java.util.Random;
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;

import utils.distribution.SkewedNormalDistribution;
import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.dtw.WarpInfo;

public class DynamicTimeWarping {
	private double[][] costMatrix;
	private int windowLen;
	private int rankingMethod = 2;		// 1: Linear Ranking, 2: Exponential Ranking (default), 3: SkewedNormal
	private RandomGenerator rng;
	private AbstractRealDistribution rand;
	private int distributionType;
	private double lowerLim = 0,
				   upperLim = 2;
	private double MEAN = 1.0,
				   STD_DEV = 1.0/3.0;
	private double SKEW = 0;
	
	public DynamicTimeWarping() {
		
	}
	
	public DynamicTimeWarping(int szI, int szJ, int windowPercent) {
		costMatrix = new double[szI][szJ];
		setWindowSize(szI, szJ, windowPercent);
		rng = null;
	}
	
	public DynamicTimeWarping(int szI, int szJ, int windowPercent, String methodName) {
		this(szI, szJ, windowPercent);
		rng = new Well19937c();
		if(methodName.equals("L")) {
			this.rankingMethod = 1;
		} else if(methodName.equals("E")) {
			this.rankingMethod = 2;
		}
	}
	
	public void setWindowSize(int tsILen, int tsJLen, int windowPercent) {
		// window size for calculation of cost matrix entries if windowSize is zero we got to a 1 length window equal to euclidean dist
		if (windowPercent == 0 || windowPercent == -1) {
			windowLen = 1;
		} else {
			windowLen = Math.max( (int) Math.ceil( windowPercent*tsILen/100.0 ), Math.abs(tsILen-tsJLen));
		}
	}
	
	public void initRNGDistribution(String distribution) {
		switch(distribution) {
			case "U":
				rand = new UniformRealDistribution(rng, lowerLim, upperLim);
				distributionType = 1;
				break;
			case "G":
				rand = new NormalDistribution(rng, MEAN, STD_DEV);
				distributionType = 2;
				break;
			case "S":
				rand = new SkewedNormalDistribution(rng, MEAN, STD_DEV, SKEW);
				distributionType = 3;
				break;
		}
	}
	
	private void updateProbDist(int i, int j, int tsLen) {
		if(this.distributionType == 3) {
			this.SKEW = 10.0*FastMath.pow((i-j), 2)/tsLen;	// Aggressive skewness
			//(double)(i-j)/tsLen;
			((SkewedNormalDistribution) rand).updateParams(MEAN, STD_DEV, SKEW);
		} 
	}
	
 	private double[] rankCandidates(double cRight, double cDiag, double cDown) {
		double[] probs = new double[2];
		double epsilon = 1e-9, epsilon3x = 3e-9;
		double alpha = 2;
		double costSum;
		if(this.rankingMethod==1) {
			costSum = cDiag+cRight+cDown;
			probs[0] = (costSum-cRight+epsilon) / (costSum + epsilon3x);
			probs[1] = (costSum-cDiag+epsilon) / (costSum + epsilon3x) + probs[0];
		} else if(this.rankingMethod==2) {
			cRight = Math.exp(-alpha * cRight);
			cDiag = Math.exp(-alpha * cDiag);
			cDown = Math.exp(-alpha * cDown);
			costSum = (cRight + cDiag + cDown)/2.0;  
			probs[0] = cRight/costSum;
			probs[1] = cDiag/costSum+probs[0];
			if( Double.isNaN(probs[0]) && Double.isNaN(probs[1])) {
			    probs[0] = 0.5;
			    probs[1] = 1.5;
			}
		}
		
		return probs;
	}
	
	public WarpInfo getHeuristicDTW(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn) {
		int maxI = tsI.size();			// Maximum index number for TimeSeries I
		int maxJ = tsJ.size();			// maximum index number for TimeSeries J
		int i = 0;						// Current index number for TimeSeries I
		int j = 0;						// Current index number for TimeSeries J
		
		WarpInfo info = new WarpInfo();	// Warping Path info (e.g. length and path indices)
		
		double costDiag, costRight, costDown;	// cost variables for prospective successive directions 
		
		double[] probs = new double[2];			// Used to save the probabilities of the direction to take
		double selProb;							// Selection probability
		boolean isValidCellChosen;

		for(double[] current : costMatrix) {	// Assign positive infinity to entire matrix
			Arrays.fill(current, Double.POSITIVE_INFINITY);
		}

		costMatrix[0][0] = distFn.calcDistance(tsI.get(i), tsJ.get(j));
// ################################################################################################
//		The following code statement is adding the entries to the warping path. To record the
//		warping path as well, uncomment the statements "info.addLast(i, j);"
//		info.addLast(i, j);
// ################################################################################################
		while(i<maxI && j<maxJ) {
			if(i+1<maxI && j+1<maxJ) {		// Check if move to diagonal element is valid
				costDiag = distFn.calcDistance(tsI.get(i+1), tsJ.get(j+1));
			} else {
				costDiag = 1e12;
			}
			if(i+1<Math.min(windowLen+j, maxI)) {	// Check if moving downwards is valid
				costDown = distFn.calcDistance(tsI.get(i+1), tsJ.get(j));
			} else {
				costDown = 1e12;
			}
			if(j+1<Math.min(windowLen+i, maxJ)) {	// Check if moving right is valid
				costRight = distFn.calcDistance(tsI.get(i), tsJ.get(j+1));
			} else {
				costRight = 1e12;
			}
			isValidCellChosen = false;
			
			probs = this.rankCandidates(costRight, costDiag, costDown);
			
			while(!isValidCellChosen) {		// loop used for times when we are at the end of warping path but a valid cell can't be chosen
				selProb = rand.sample();
				if(selProb < probs[0] && i<maxI && j+1<maxJ && j+1<j+windowLen) {	// j+1<j+windowLen added to restrict going out of window
					// Moving one cell Right
					costMatrix[i][j+1] = costMatrix[i][j] + costRight;
					j++;
					isValidCellChosen = true;
				} else if(selProb > probs[1] && i+1<maxI && j<maxJ && i+1<i+windowLen) {	// i+1<i+windowLen added to restrict going out of window
					// Moving one cell Down
					costMatrix[i+1][j] = costMatrix[i][j] + costDown;
					i++;
					isValidCellChosen = true;
				} else if(i+1<maxI && j+1<maxJ) {			// OLD Condition selProb <= probs[1] && i+1<maxI && j+1<maxJ 
					// Moving diagonally
					costMatrix[i+1][j+1] = costMatrix[i][j] + costDiag;
					i++; j++;
					isValidCellChosen = true;
				}
				if(isValidCellChosen) {
// ################################################################################################
//					The following code statement is adding the entries to the warping path.
//					info.addLast(i, j);
// ################################################################################################					
					Arrays.fill(probs, 0);				// reinitialize the probs array to all zeros
					if(this.distributionType == 3) {
						updateProbDist(i, j, maxI);
					}
					break;
				}
			}
			if(i+1==maxI && j+1==maxJ) {
				info.setWarpDistance(costMatrix[i][j]);
				break;
			}
		}
		return info;
	}
	
	public WarpInfo getLuckyDTW(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn) {
		int maxI = tsI.size();			// Maximum index number for TimeSeries I
		int maxJ = tsJ.size();			// maximum index number for TimeSeries J
		int i = 0;						// Current index number for TimeSeries I
		int j = 0;						// Current index number for TimeSeries J
		WarpInfo info = new WarpInfo();	// Warping Path info (e.g. length and path indices)
		
		double costDiag, costRight, costDown;	// cost variables for prospective successive directions 
		
		for(double[] current : costMatrix) {	// Assign positive infinity to entire matrix
			Arrays.fill(current, Double.POSITIVE_INFINITY);
		}
		costMatrix[0][0] = distFn.calcDistance(tsI.get(i), tsJ.get(j));
// ################################################################################################
//		The following code statement is adding the entries to the warping path as per LuckyTW. To
//		record the warping path as well, uncomment the statements "info.addLast(i, j);"
//		One other statement is in the body of the while loop 
//		info.addLast(i, j);
// ################################################################################################
		while(i<maxI && j<maxJ) {
			if(i+1<maxI && j+1<maxJ) {
				costDiag = distFn.calcDistance(tsI.get(i+1), tsJ.get(j+1));
			} else {
				costDiag = 1e12;
			}
			if(i+1<Math.min(windowLen+j, maxI)) {
				costDown = distFn.calcDistance(tsI.get(i+1), tsJ.get(j));
			} else {
				costDown = 1e12;
			}
			if(j+1<Math.min(windowLen+i, maxJ)) {
				costRight = distFn.calcDistance(tsI.get(i), tsJ.get(j+1));
			} else {
				costRight = 1e12;
			}
			if ((costDiag <= costRight) && (costDiag <= costDown)) {
				costMatrix[i+1][j+1] = costMatrix[i][j] + costDiag;
				i++;
				j++;
			} else if ((costRight < costDiag) && (costRight < costDown)) {
				costMatrix[i][j+1] = costMatrix[i][j] + costRight;
				j++;
			} else if ((costDown < costDiag) && (costDown < costRight)) {
				costMatrix[i+1][j] = costMatrix[i][j] + costDown;
				i++;
			} else { // costDown==costRight > costDiag
				if(Math.random()<0.5) {	// Go down
					costMatrix[i+1][j] = costMatrix[i][j] + costDown;
					i++;
				} else {				// Go right
					costMatrix[i][j+1] = costMatrix[i][j] + costRight;
					j++;
				}
			}
// ################################################################################################
//			The following code statement is adding the entries to the warping path as per LuckyTW.
//			info.addLast(i, j);
// ################################################################################################
			if(i+1==maxI && j+1==maxJ) {
				info.setWarpDistance(costMatrix[i][j]);
				break;
			}
		}
		return info;
	}
	
	public WarpInfo getNormalDTW(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn) {
		int maxI = tsI.size();
		int maxJ = tsJ.size();
		for(double[] current : costMatrix) {
			Arrays.fill(current, Double.POSITIVE_INFINITY);
		}
		if(windowLen<maxI) {
			costMatrix[0][0] = distFn.calcDistance(tsI.get(0), tsJ.get(0));
			for(int j=1; j<windowLen; j++) {
				costMatrix[0][j] = costMatrix[0][j-1] + distFn.calcDistance(tsI.get(0), tsJ.get(j));
			}
			// First loop set
			for(int i=1; i<windowLen; i++) {
				costMatrix[i][0] = costMatrix[i - 1][0] + distFn.calcDistance(tsI.get(i), tsJ.get(0));
				for(int j=1; j<Math.min(i+windowLen, maxJ); j++) {
					costMatrix[i][j] = calcCost(tsI, tsJ, distFn, costMatrix, i, j);
				}
			}
			// Second loop set
			int k=1;
			for(int i=windowLen; i<maxI-windowLen; i++, k++) {
				for(int j=k; j<i+windowLen; j++) {
					costMatrix[i][j] = calcCost(tsI, tsJ, distFn, costMatrix, i, j);
				}
			}
			// Third loop set
			for(int i=maxI-windowLen; i<maxI; i++, k++) {
				for(int j=k; j<maxJ; j++) {
					costMatrix[i][j] = calcCost(tsI, tsJ, distFn, costMatrix, i, j);
				}
			}
		} else {
			costMatrix[0][0] = distFn.calcDistance(tsI.get(0), tsJ.get(0));
			for(int j=1; j<maxJ; j++) {
				costMatrix[0][j] = costMatrix[0][j-1] + distFn.calcDistance(tsI.get(0), tsJ.get(j));
			}
			for(int i=1; i<maxI; i++) {
				costMatrix[i][0] = costMatrix[i - 1][0] + distFn.calcDistance(tsI.get(i), tsJ.get(0));				
				for(int j=1; j<maxJ; j++) {
					costMatrix[i][j] = calcCost(tsI, tsJ, distFn, costMatrix, i, j);
				}
			}
		}
		int i = tsI.size()-1;	// tsI_size and tsJ_size have lengths of time series so subtract 
		int j = tsJ.size()-1;	// 1 from them to point to the last element of the cost matrix
		WarpInfo info = new WarpInfo();
		info.setWarpDistance(costMatrix[i][j]);
// ################################################################################################
//		The following code segment determines the warping path. The warping distance has been
//		calculated up to the previous point in the code. Since this code segment is not needed yet,
//		it has been commented.
//		double costDiag, costLeft, costDown;
//		info.addFirst(i, j);
//		while( (i>0) || (j>0) ) {
//			if ((i > 0) && (j > 0))
//				costDiag = costMatrix[i - 1][j - 1];
//			else
//				costDiag = Double.POSITIVE_INFINITY;
//
//			if (j > 0)
//				costLeft = costMatrix[i][j - 1];
//			else
//				costLeft = Double.POSITIVE_INFINITY;
//
//			if (i > 0)
//				costDown = costMatrix[i - 1][j];
//			else
//				costDown = Double.POSITIVE_INFINITY;
//
//			// Prefer moving diagonally and moving towards the i==j axis  
//			// of the matrix if there are ties.
//			if ((costDiag <= costLeft) && (costDiag <= costDown)) {
//				i--;
//				j--;
//			} else if ((costLeft < costDiag) && (costLeft < costDown)) {
//				j--;
//			} else if ((costDown < costDiag) && (costDown < costLeft)) {
//				i--;
//			} else if (i <= j) { // leftCost==rightCost > diagCost
//				i--;
//			} else { // leftCost==rightCost > diagCost
//				j--;
//			}
//			info.addFirst(i, j);
//		}
// ################################################################################################
		return info;
	}

	public WarpInfo getEuclideanDist(TimeSeries tsI, TimeSeries tsJ) {
		int maxLen = tsI.size();
		double dist = 0.0;
		for(int i=0; i<maxLen; i++) {
			dist += Math.pow(tsI.get(i)-tsJ.get(i), 2);
		}
		dist = Math.sqrt(dist);
		WarpInfo info = new WarpInfo();
		info.setWarpDistance(dist);
		return info;
	}

	private double calcCost(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn, double[][] costMatrix, int i, int j) {
		return distFn.calcDistance(tsI.get(i), tsJ.get(j))+ Math.min(costMatrix[i - 1][j], Math.min(costMatrix[i - 1][j - 1], costMatrix[i][j - 1]));
	}
}