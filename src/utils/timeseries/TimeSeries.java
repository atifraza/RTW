package utils.timeseries;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class TimeSeries {
	private int tsClass;
	private ArrayList<Double> ts;

	public TimeSeries() {
		tsClass = 0;
		ts = new ArrayList<Double>();
	}

	public TimeSeries(String line, boolean isFirstColClass, char delimiter) {
		this();
		StringTokenizer st = new StringTokenizer(line, String.valueOf(delimiter));

		if (isFirstColClass) {
			tsClass = (int) Double.parseDouble(st.nextToken());
		}
		while (st.hasMoreTokens()) {
			ts.add(Double.parseDouble(st.nextToken()));
		}
	}
	
	public int size() {
		return ts.size();
	}

	public double get(int index) {
		return ts.get(index).doubleValue();
	}
	
	public int getTSClass() {
		return tsClass;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Time Series:\n" + ts + "\n");
		str.append("Class of Time Series is: " + tsClass + "\n");
		str.append("Length is: " + ts.size() + "\n");

		return str.toString();
	}
}
