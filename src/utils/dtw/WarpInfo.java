package utils.dtw;

import java.util.ArrayList;

public class WarpInfo {
	private double warpDist;
	private ArrayList<Integer> tsIindices;
	private ArrayList<Integer> tsJindices;

	public WarpInfo() {
		warpDist = 0;
		tsIindices = new ArrayList<Integer>();
		tsJindices = new ArrayList<Integer>();
	}

	public void addFirst(int i, int j) {
		tsIindices.add(0, i);
		tsJindices.add(0, j);
	}
	
	public void addLast(int i, int j) {
		tsIindices.add(tsIindices.size(), i);
		tsJindices.add(tsJindices.size(), j);
	}
	
	public void setWarpDistance(double dist) {
		warpDist = dist;
	}
	
	public double getWarpDistance() {
		return warpDist;
	}
	
	public int getWarpPathLength() {
		return tsIindices.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Warping Distance: " + warpDist + "\nWarping path:\n");
		for(int i=0; i<tsIindices.size(); i++) {
			sb.append("(" + tsIindices.get(i) + ", " + tsJindices.get(i) + ")\n");
		}
		return sb.toString();
	}
}
