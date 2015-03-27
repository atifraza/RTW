import utils.dtw.*;

public class InitDTWTest {
    public static void main(String[] args) throws Exception {
		if(args.length<2) {
			System.err.println("Usage: java InitDTWTest FileName WindowSize_INT DTW_Type Start_Index End_Index\n" + 
							   "e.g.:  java InitDTWTest Coffee 10 N\n" + 
							   "DTW_Type is either of N (normal), L (lucky), U (uniform) or G (gaussian)" + 
							   "FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		}
		
		String fileName = args[0];
		
		int window = Integer.parseInt(args[1]);
//		int start = 0, inc = 0;
//		if(args.length == 5) {
//			start = Integer.parseInt(args[3]);
//			inc = Integer.parseInt(args[4]);
//		}
		BaseDTW dtw;
		switch (args[2]) {
			case "N":
				dtw = new NormalDTW(fileName, window);
				dtw.execute();
				break;
			case "L":
				dtw = new LuckyDTW(fileName, window);
				dtw.execute();
				break;
			case "U":
				dtw = new HeuristicDTW(fileName, window, 1);
				dtw.execute();
				break;
			case "G":
				dtw = new HeuristicDTW(fileName, window, 2);
				dtw.execute();
				break;
		}
	}
}