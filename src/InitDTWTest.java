import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;

import utils.dtw.*;

public class InitDTWTest {
	protected static String fileSwitch = "file";
	protected static String helpSwitch = "help";
	protected static String windowSwitch = "window";
	protected static String twTypeSwitch = "warping";
	protected static String rngSwitch = "rng";
	protected static String restartsSwitch = "restarts";
	protected static String rankingTypeSwitch = "ranking";
	protected static String segmentSwitch = "segment";
	
	public static void main(String[] args) {
		Options options = constructCLIOptions();
		String fileName = null, dtwType = null, rng = null, ranking = null, passes = null;
		int window = 0;
		int start = 0, inc = 0;
		
		try {
			CommandLineParser cliParser = new GnuParser();
			CommandLine cmdLine = cliParser.parse(options, args);
			
			if (cmdLine.getOptions().length == 0 || cmdLine.hasOption(helpSwitch)) {
				printHelp(options, true, System.out);
				System.exit(0);
			}
			
			fileName = cmdLine.getOptionValue(fileSwitch);
			window = Integer.parseInt(cmdLine.getOptionValue(windowSwitch, "100"));
			dtwType = cmdLine.getOptionValue(twTypeSwitch, "N");
			
			if (dtwType.equals("H")) {
				rng = cmdLine.getOptionValue(rngSwitch, "N");
				ranking = cmdLine.getOptionValue(rankingTypeSwitch, "L");					
				passes = cmdLine.getOptionValue(restartsSwitch, "5");
			}
			
			if (cmdLine.hasOption(segmentSwitch)) {
				String[] temp = cmdLine.getOptionValues(segmentSwitch);
				start = Integer.parseInt(temp[0]);
				inc = Integer.parseInt(temp[1]);
			}
		} catch (ParseException parseException) // checked exception
		{
			System.err.println("Encountered exception while parsing input arguments:\n" + parseException.getMessage() + "\n\n");
			printHelp(options, true, System.out);
			System.exit(0);
		}
		
		BaseDTW dtw;
		switch (dtwType) {
			case "N":
				dtw = new NormalDTW(fileName, window, start, inc);
				dtw.execute();
				break;
			case "L":
				dtw = new LuckyDTW(fileName, window, start, inc);
				dtw.execute();
				break;
			case "H":
				dtw = new HeuristicDTW(fileName, window, ranking, passes, rng);
				dtw.execute();
				break;
//			case "U":
//				dtw = new HeuristicDTW(fileName, window, ranking, passes, 1);
////				dtw = new HeuristicDTW(fileName, window, ranking, passes, 1, start, inc);
//				dtw.execute();
//				break;
//			case "G":
//				dtw = new HeuristicDTW(fileName, window, ranking, passes, 2);
////				dtw = new HeuristicDTW(fileName, window, ranking, passes, 2, start, inc);
//				dtw.execute();
//				break;
		}
		System.out.println("Done");
	}
    
	/**
	 * Print help to provided OutputStream.
	 * 
	 * @param options
	 *            Command-line options to be part of usage.
	 * @param out
	 *            OutputStream to which to write the usage information.
	 */
	public static void printHelp(Options options, boolean detailed, OutputStream out) {
		String cliSyntax = "java InitDTWTest";
		PrintWriter writer = new PrintWriter(out);
		HelpFormatter helpFormatter = new HelpFormatter();
		if(detailed) {
			helpFormatter.printHelp(writer, 120, cliSyntax, "", options, 7, 1, "", true);			
		} else {
			helpFormatter.printUsage(writer, 120, cliSyntax, options);
		}
		writer.flush();
	}
	
    private static Options constructCLIOptions() {
    	Option help = new Option(helpSwitch, "prints the help for the program");
    	
    	Option fileName = new Option(fileSwitch, "name of the dataset");
    	fileName.setRequired(true);
    	fileName.setArgs(1);
    	fileName.setArgName("dataset");
    	
    	Option winSz = new Option(windowSwitch, "DTW window size as a percentage e.g. 1, 5, 10 100 (default)");
//    	winSz.setRequired(true);
    	winSz.setArgs(1);
    	winSz.setArgName("percent");
//    	winSz.setType(Number.class);
    	
    	Option dtwType = new Option(twTypeSwitch, "defines the DTW type, valid options are N (default), L, and H for Normal, Lucky, and Heuristic");
//    	type.setRequired(true);
    	dtwType.setArgs(1);
    	dtwType.setArgName("warpingType");
    	
    	Option rngType = new Option(rngSwitch, "defines the RNG type, valid options are N (normally distributed random numbers - default), "+
    								"U (uniformly distributed random numbers)");
//    	type.setRequired(true);
    	rngType.setArgs(1);
    	rngType.setArgName("rngType");
    	
    	Option passes = new Option(restartsSwitch, "number of restarts for heuristic DTW calculations 0 (no restarts), " + 
    							   "I (increasing with number of runs), positive integer (for constant number of restarts)");
    	passes.setArgs(1);
    	passes.setArgName("number");
    	
    	Option ranking = new Option(rankingTypeSwitch, "type of ranking to use e.g. L (linear - default), E (exponential)");
    	ranking.setArgs(1);
    	ranking.setArgName("method");
    	
    	Option segment = new Option(segmentSwitch, "calculates the DTW of the instances starting from 'start' and upto 'start+increment' from the test set");
    	segment.setArgs(2);
    	segment.setArgName("startPoint> <increment");   	
    	
    	Options opts = new Options();
    	opts.addOption(help).addOption(fileName).addOption(dtwType).addOption(rngType).addOption(winSz).addOption(passes).addOption(segment).addOption(ranking);
    	return opts;
    }
}