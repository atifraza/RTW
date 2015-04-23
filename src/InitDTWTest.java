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
	protected static String fileSwitch = "f",
							fileSwitchLong = "file";
	
	protected static String outDirSwitch = "o",
							outDirSwitchLong = "out-dir";
	
	protected static String helpSwitch = "h",
							helpSwitchLong = "help";
	
	protected static String bandSwitch = "b",
							bandSwitchLong = "band";
	
	protected static String twTypeSwitch = "t",
							twTypeSwitchLong = "type";
	
	protected static String rngSwitch = "g",
							rngSwitchLong = "rng";
	
	protected static String passesSwitch = "p",
							passesSwitchLong = "passes";
	
	protected static String rankingTypeSwitch = "r",
							rankingTypeSwitchLong = "ranking";
	
	protected static String segmentSwitch = "segment";
	
	public static void main(String[] args) {
		Options options = constructCLIOptions();
		String fileName = null, outDir = null, dtwType = null, rng = null, ranking = null, passes = null;
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
			outDir = cmdLine.getOptionValue(outDirSwitch, "");
			window = Integer.parseInt(cmdLine.getOptionValue(bandSwitch, "100"));
			dtwType = cmdLine.getOptionValue(twTypeSwitch, "N");
			
			if (dtwType.equals("H")) {
				rng = cmdLine.getOptionValue(rngSwitch, "G");
				ranking = cmdLine.getOptionValue(rankingTypeSwitch, "L");					
				passes = cmdLine.getOptionValue(passesSwitch, "5");
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
				dtw = new NormalDTW(fileName, outDir, window, start, inc);
				dtw.execute();
				break;
			case "L":
				dtw = new LuckyDTW(fileName, outDir, window, start, inc);
				dtw.execute();
				break;
			case "H":
				dtw = new HeuristicDTW(fileName, outDir, window, ranking, passes, rng, start, inc);
				dtw.execute();
				break;
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
    	help.setLongOpt(helpSwitchLong);
    	
    	Option datafileName = new Option(fileSwitch, "name of the dataset");
    	datafileName.setLongOpt(fileSwitchLong);
    	datafileName.setRequired(true);
    	datafileName.setArgs(1);
    	datafileName.setArgName("dataset");
    	
    	Option outputDir = new Option(outDirSwitch, "directory to use for saving the results");
    	outputDir.setLongOpt(outDirSwitchLong);
    	outputDir.setArgs(1);
    	outputDir.setArgName("directory");
    	
    	Option winSz = new Option(bandSwitch, "DTW window size as a percentage e.g. 1, 5, 10 100 (default)");
    	winSz.setLongOpt(bandSwitchLong);
    	winSz.setArgs(1);
    	winSz.setArgName("percent");
    	
    	Option dtwType = new Option(twTypeSwitch, "defines the DTW type, valid options are N (default), L, and H for Normal, Lucky, and Heuristic");
    	dtwType.setLongOpt(twTypeSwitchLong);
    	dtwType.setArgs(1);
    	dtwType.setArgName("warpingType");
    	
    	Option rngType = new Option(rngSwitch, "defines the RNG type, valid options are N (normally distributed random numbers - default), "+
    								"U (uniformly distributed random numbers)");
    	rngType.setLongOpt(rngSwitchLong);
    	rngType.setArgs(1);
    	rngType.setArgName("rngType");
    	
    	Option passes = new Option(passesSwitch, "number of restarts for heuristic DTW calculations 0 (no restarts), " + 
    							   "I (increasing with number of runs), positive integer (for constant number of restarts)");
    	passes.setLongOpt(passesSwitchLong);
    	passes.setArgs(1);
    	passes.setArgName("number");
    	
    	Option ranking = new Option(rankingTypeSwitch, "type of ranking to use e.g. L (linear - default), E (exponential)");
    	ranking.setLongOpt(rankingTypeSwitchLong);
    	ranking.setArgs(1);
    	ranking.setArgName("method");
    	
    	Option segment = new Option(segmentSwitch, "calculates the DTW of the instances starting from 'start' and upto 'start+increment' from the test set");
    	segment.setArgs(2);
    	segment.setArgName("startPoint> <increment");   	
    	
    	Options opts = new Options();
    	opts.addOption(help)
    		.addOption(datafileName)
    		.addOption(outputDir)
    		.addOption(dtwType)
    		.addOption(rngType)
    		.addOption(winSz)
    		.addOption(passes)
    		.addOption(segment)
    		.addOption(ranking);
    	return opts;
    }
}