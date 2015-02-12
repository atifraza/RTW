package utils.dtw;

import java.util.Arrays;
//import java.util.Locale;
//import java.text.DecimalFormat;
//import java.text.DecimalFormatSymbols;
import java.util.Random;

import utils.timeseries.TimeSeries;
import utils.distance.DistanceFunction;
import utils.dtw.WarpInfo;

public class DynamicTimeWarping {
	public static WarpInfo getHeuristicDTW(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn, int windowPercent, int distribution) {
		int maxI = tsI.size();			// Maximum index number for TimeSeries I
		int maxJ = tsJ.size();			// maximum index number for TimeSeries J
		int i = 0;						// Current index number for TimeSeries I
		int j = 0;						// Current index number for TimeSeries J
		double epsilon = 1e-9, epsilon3x = 3e-9;
		double STD_DEV = 1.0/3, VARIANCE = STD_DEV*STD_DEV, MEAN = 1;
		
		Random rand = new Random();

		WarpInfo info = new WarpInfo();	// Warping Path info (e.g. length and path indices)
		
		double[][] costMatrix = new double[maxI][maxJ];		// Contains calculations of warping path
		
		double costDiag, costRight, costDown;	// cost variables for prospective successive directions 
		double costSum;							// cumulative cost 
		
		double[] probs = new double[3];			// Used to save the probabilities of the direction to take
		double selProb;							// Selection probability
		boolean isValidCellChosen;

		int w = Math.max( (int) Math.ceil( windowPercent*maxI/100.0 ),
		                  Math.abs(maxI-maxJ));	// window size for calculation of cost matrix entries
		
		for(double[] current : costMatrix) {	// Assign positive infinity to entire matrix
			Arrays.fill(current, Double.POSITIVE_INFINITY);
		}

		costMatrix[0][0] = distFn.calcDistance(tsI.get(i), tsJ.get(j));
		info.addLast(i, j);
		while(i<maxI && j<maxJ) {
			if(i+1<maxI && j+1<maxJ) {
				costDiag = distFn.calcDistance(tsI.get(i+1), tsJ.get(j+1));
			} else {
				costDiag = 1e12;
			}
			
			
			//if(i+1<maxI && Math.abs(i+1-j)<w) { // OLD Conditional, following is better to understand
			if(i+1<Math.min(w+j, maxI)) {
				costDown = distFn.calcDistance(tsI.get(i+1), tsJ.get(j));
			} else {
				costDown = 1e12;
			}
			
			//if(j+1<maxJ && Math.abs(j+1-i)<maxI) { // OLD Conditional, following is better to understand
			if(j+1<Math.min(w+i, maxJ)) {
				costRight = distFn.calcDistance(tsI.get(i), tsJ.get(j+1));
			} else {
				costRight = 1e12;
			}
			
			costSum = costDiag+costRight+costDown;
			isValidCellChosen = false;
//			Arrays.fill(probs, 0);				// always reinitialize the array to all zeros
			probs[0] = (costSum-costRight+epsilon) / (costSum + epsilon3x);
			probs[1] = (costSum-costDiag+epsilon) / (costSum + epsilon3x) + probs[0];
			probs[2] = (costSum-costDown+epsilon) / (costSum + epsilon3x) + probs[1];
			
			while(!isValidCellChosen) {
				if (distribution == 1) {
					selProb = rand.nextDouble() * 2;	// generate a uniform random number
					// the random number is between 0 and 1 so we multiply it with
					// 2 to get it between 0 and 2 
				} else {
					selProb = MEAN + rand.nextGaussian()*VARIANCE;	// generate a normally distributed
					// random number, it has a mean at 0 and a std of 1, so we add MEAN to it
					// to shift it's mean to 1 and multiply it with VARIANCE to generate random numbers
					// within required Standard Deviation
					if (selProb <0 || selProb >=2) {
						continue;
					}
				}
				
				if(selProb <= probs[0] && i<maxI && j+1<maxJ) {
					// Moving one cell Right
					costMatrix[i][j+1] = costMatrix[i][j] + costRight;
					j++;
					isValidCellChosen = true;
				} else if(selProb <= probs[1] && i+1<maxI && j+1<maxJ) {
					// Moving diagonally
					costMatrix[i+1][j+1] = costMatrix[i][j] + costDiag;
					i++; j++;
					isValidCellChosen = true;
				} else if(selProb <= probs[2] && i+1<maxI && j<maxJ) {
					// Moving one cell Down
					costMatrix[i+1][j] = costMatrix[i][j] + costDown;
					i++;
					isValidCellChosen = true;
				}
				if(isValidCellChosen) {
					info.addLast(i, j);
					Arrays.fill(probs, 0);				// reinitialize the probs array to all zeros
					break;
				}
				
//				for (int ind = 0; ind < probs.length; ind++) {
//					if (selProb <= probs[ind]) {
//						switch (ind) {
//							case 0:	{
//								if(i<maxI && j+1<maxJ) {		// Moving one cell Right
//									costMatrix[i][j+1] = costMatrix[i][j] + costRight;
//									j++;
//									isValidCellChosen = true;
//								}
//								break;
//							}
//							case 1:	{
//								if(i+1<maxI && j+1<maxJ) {		// Moving diagonally
//									costMatrix[i+1][j+1] = costMatrix[i][j] + costDiag;
//									i++; j++;
//									isValidCellChosen = true;
//								}
//								break;
//							}
//							case 2:	{
//								if(i+1<maxI && j<maxJ) {		// Moving one cell Down
//									costMatrix[i+1][j] = costMatrix[i][j] + costDown;
//									i++;
//									isValidCellChosen = true;
//								}
//								break;
//							}
//						}
//						if(isValidCellChosen) {
//							info.addLast(i, j);
//							break;
//						}
//					}
//				}				
			}
			if(i+1==maxI && j+1==maxJ) {
//				DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
//				symbols.setInfinity("∞");
//				DecimalFormat decimalFormat = new DecimalFormat("#.#", symbols);
//				System.out.print("\n\n\n");
//				for(int row = 0; row<maxI; row++) {
//					for (int col = 0; col<maxJ; col++) {
//						System.out.print(decimalFormat.format(costMatrix[row][col]) + "\t");
//					}
//					System.out.println();
//				}
				info.setWarpDistance(costMatrix[i][j]);
				break;
			}
		}
		return info;
	}
	
	public static WarpInfo getLuckyDTW(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn, int windowPercent) {
		int maxI = tsI.size();			// Maximum index number for TimeSeries I
		int maxJ = tsJ.size();			// maximum index number for TimeSeries J
		int i = 0;						// Current index number for TimeSeries I
		int j = 0;						// Current index number for TimeSeries J
		WarpInfo info = new WarpInfo();	// Warping Path info (e.g. length and path indices)
		
		double[][] costMatrix = new double[maxI][maxJ];		// Contains calculations of warping path
		
		double costDiag, costRight, costDown;	// cost variables for prospective successive directions 

		int w = Math.max( (int) Math.ceil( windowPercent*maxI/100.0 ),
		                  Math.abs(maxI-maxJ));	// window size for calculation of cost matrix entries
		
		for(double[] current : costMatrix) {	// Assign positive infinity to entire matrix
			Arrays.fill(current, Double.POSITIVE_INFINITY);
		}

		costMatrix[0][0] = distFn.calcDistance(tsI.get(i), tsJ.get(j));
		info.addLast(i, j);
		while(i<maxI && j<maxJ) {
			if(i+1<maxI && j+1<maxJ) {
				costDiag = distFn.calcDistance(tsI.get(i+1), tsJ.get(j+1));
			} else {
				costDiag = 1e12;
			}
						
			//if(i+1<maxI && Math.abs(i+1-j)<w) { // OLD Conditional, following is better to understand
			if(i+1<Math.min(w+j, maxI)) {
				costDown = distFn.calcDistance(tsI.get(i+1), tsJ.get(j));
			} else {
				costDown = 1e12;
			}
			
			//if(j+1<maxJ && Math.abs(j+1-i)<maxI) { // OLD Conditional, following is better to understand
			if(j+1<Math.min(w+i, maxJ)) {
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
			info.addLast(i, j);

			if(i+1==maxI && j+1==maxJ) {
//				DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
//				symbols.setInfinity("∞");
//				DecimalFormat decimalFormat = new DecimalFormat("#.#", symbols);
//				System.out.print("\n\n\n");
//				for(int row = 0; row<maxI; row++) {
//					for (int col = 0; col<maxJ; col++) {
//						System.out.print(decimalFormat.format(costMatrix[row][col]) + "\t");
//					}
//					System.out.println();
//				}
				info.setWarpDistance(costMatrix[i][j]);
				break;
			}
		}
		return info;
	}
	
	public static WarpInfo getNormalDTW(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn, int windowPercent) {
		double[][] costMatrix = calculateCostMatrix(tsI, tsJ, distFn, windowPercent);
		return getNormalDTW(costMatrix, tsI.size(), tsJ.size());
	}

	public static double[][] calculateCostMatrix(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn, int windowPercent) {
		int maxI = tsI.size();
		int maxJ = tsJ.size();
		double[][] costMatrix = new double[maxI][maxJ];

		if(windowPercent<100) {
			int w = Math.max( (int) Math.ceil( windowPercent*maxI/100.0 ),
			                  Math.abs(maxI-maxJ));
			
			for(double[] current : costMatrix) {
				Arrays.fill(current, Double.POSITIVE_INFINITY);
			}

			costMatrix[0][0] = distFn.calcDistance(tsI.get(0), tsJ.get(0));
			for(int j=1; j<w; j++) {
				costMatrix[0][j] = costMatrix[0][j-1] + distFn.calcDistance(tsI.get(0), tsJ.get(j));
			}
			// First loop set
			for(int i=1; i<w; i++) {
				costMatrix[i][0] = costMatrix[i - 1][0] + distFn.calcDistance(tsI.get(i), tsJ.get(0));
				for(int j=1; j<i+w; j++) {
					costMatrix[i][j] = calcCost(tsI, tsJ, distFn, costMatrix, i, j);
				}
			}
			// Second loop set
			int k=1;
			for(int i=w; i<maxI-w; i++, k++) {
				for(int j=k; j<i+w; j++) {
					costMatrix[i][j] = calcCost(tsI, tsJ, distFn, costMatrix, i, j);
				}
			}
			// Third loop set
			for(int i=maxI-w; i<maxI; i++, k++) {
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
//		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
//		symbols.setInfinity("∞");
//		DecimalFormat decimalFormat = new DecimalFormat("#.#", symbols);
//		System.out.print("\n\n\n");
//		for(int row = 0; row<maxI; row++) {
//			for (int col = 0; col<maxJ; col++) {
//				System.out.print(decimalFormat.format(costMatrix[row][col]) + ",");
//			}
//			System.out.println();
//		}
		return costMatrix;
	}
	
	private static double calcCost(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn,
	                               double[][] costMatrix, int i, int j) {
		return distFn.calcDistance(tsI.get(i), tsJ.get(j))+ Math.min(costMatrix[i - 1][j],
		                                                             Math.min(costMatrix[i - 1][j - 1],
		                                                                      costMatrix[i][j - 1]));
	}
	
	public static WarpInfo getNormalDTW(double[][] costMatrix, int tsI_size, int tsJ_size) {
		double costDiag, costLeft, costDown;
		int i = tsI_size-1;	// tsI_size and tsJ_size have lengths of time series so subtract 
		int j = tsJ_size-1;	// 1 from them to point to the last element of the cost matrix
		double minDist = costMatrix[i][j];
		WarpInfo info = new WarpInfo();
		info.setWarpDistance(minDist);
		info.addFirst(i, j);

		while( (i>0) || (j>0) ) {
			if ((i > 0) && (j > 0))
				costDiag = costMatrix[i - 1][j - 1];
			else
				costDiag = Double.POSITIVE_INFINITY;

			if (j > 0)
				costLeft = costMatrix[i][j - 1];
			else
				costLeft = Double.POSITIVE_INFINITY;

			if (i > 0)
				costDown = costMatrix[i - 1][j];
			else
				costDown = Double.POSITIVE_INFINITY;

			// Prefer moving diagonally and moving towards the i==j axis  
			// of the matrix if there are ties.
			if ((costDiag <= costLeft) && (costDiag <= costDown)) {
				i--;
				j--;
			} else if ((costLeft < costDiag) && (costLeft < costDown)) {
				j--;
			} else if ((costDown < costDiag) && (costDown < costLeft)) {
				i--;
			} else if (i <= j) { // leftCost==rightCost > diagCost
				i--;
			} else { // leftCost==rightCost > diagCost
				j--;
			}
			info.addFirst(i, j);
		}
		return info;
//		double sumCost;
//			sumCost = diagCost + downCost + leftCost;
//			if (isHeuristic && sumCost != Double.POSITIVE_INFINITY && sumCost != 0) {
//				double[] probs = new double[3];
//				double normSumCost, selProb;
//				normSumCost = 2 * sumCost;
//				// Following 3 lines provide a Gaussian distributed random number between 0 and 1
////				Random rand = new Random();
////				double mean = 0.5, variance = 0.075;
////				selProb = mean + rand.nextGaussian() * variance;
//				// Following line provides a normally distributed random number between 0 and 1
//				selProb = Math.random();
//
//				probs[0] = (sumCost - leftCost) / normSumCost;
//				probs[1] = (sumCost - diagCost) / normSumCost + probs[0];
//				probs[2] = (sumCost - downCost) / normSumCost + probs[1];
//				for (int ind = 0; ind < probs.length; ind++) {
//					if (selProb <= probs[ind]) {
//						switch (ind) {
//							case 0:			// Moving one cell Left
//								j--; break;
//							case 1:			// Moving diagonally
//								i--; j--; break;
//							case 2:			// Moving one cell Down
//								i--; break;
//						}
//						break;
//					}
//				}
//			} else {
//				// Prefer moving diagonally and moving towards the i==j axis  
//				// of the matrix if there are ties.
//				if ((diagCost <= leftCost) && (diagCost <= downCost)) {
//					i--;
//					j--;
//				} else if ((leftCost < diagCost) && (leftCost < downCost)) {
//					j--;
//				} else if ((downCost < diagCost) && (downCost < leftCost)) {
//					i--;
//				} else if (i <= j) { // leftCost==rightCost > diagCost
//					i--;
//				} else { // leftCost==rightCost > diagCost
//					j--;
//				}
//			}
//			info.addFirst(i, j);
//		}
	}
}