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
	public static void main(String[] args) {
		Options options = constructCLIOptions();
		String fileName = null,
			   dtwType = "N",
			   ranking = "L";
		int window = 100;
		
		
		if(args.length<1) {
			System.out.println("-- USAGE --");
			printHelp(options, false, System.out);
//			System.err.println("Usage: java InitDTWTest FileName WindowSize_INT DTW_Type Start_Index NumOfInstnsToProcess\n" + 
//							   "e.g.:  java InitDTWTest Coffee 10 N 0 10\n" + 
//							   "DTW_Type is either of N (normal), L (lucky), U (uniform) or G (gaussian)" + 
//							   "FileName of training and testing set should end with _TRAIN and _TEST e.g. Dataset_TRAIN");
			System.exit(0);
		} else {
			try {
				CommandLineParser gnuCLIParser = new GnuParser();
				CommandLine commandLine = gnuCLIParser.parse(options, args);
				
				if (commandLine.hasOption("h") || commandLine.hasOption("help")) {
					printHelp(options, true, System.out);
					System.exit(0);
				}
				if (commandLine.hasOption("f") || commandLine.hasOption("file")) {
					fileName = commandLine.getOptionValue("f");
				}
				if (commandLine.hasOption("w") || commandLine.hasOption("window")) {
					window = Integer.parseInt(commandLine.getOptionValue("w"));
				}
				if (commandLine.hasOption("t") || commandLine.hasOption("dtw-type")) {
					dtwType = commandLine.getOptionValue("t");
				}
			} catch (ParseException parseException) // checked exception
			{
				System.err.println("Encountered exception while parsing using GnuParser:\n" + parseException.getMessage());
			}
		}
		
//		String fileName = args[0];
		
//		int window = Integer.parseInt(args[1]);
		int start = 0, inc = 0;
		if(args.length == 5) {
			start = Integer.parseInt(args[3]);
			inc = Integer.parseInt(args[4]);
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
			case "U":
				dtw = new HeuristicDTW(fileName, window, 1, start, inc);
				dtw.execute();
				break;
			case "G":
				dtw = new HeuristicDTW(fileName, window, 2, start, inc);
				dtw.execute();
				break;
		}
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
    	Option help = new Option("h", "prints the help for the program");
    	help.setLongOpt("help");
    	
    	Option fileName = new Option("f", "name of the dataset");
//    	fileName.setRequired(true);
    	fileName.setArgs(1);
    	fileName.setLongOpt("file");
    	fileName.setArgName("dataset");
    	
    	Option winSz = new Option("w", "DTW window size as a percentage e.g. 1, 5, 10");
    	winSz.setArgs(1);
    	winSz.setLongOpt("window");
    	winSz.setArgName("percent");
//    	winSz.setType(Number.class);
    	
    	Option type = new Option("t", "defines the DTW type, valid options are N, L, U and G for Normal, Lucky, Uniform and Gaussian");
//    	type.setRequired(true);
    	type.setArgs(1);
    	type.setLongOpt("dtw-type");
    	type.setArgName("type");
    	
    	Option passes = new Option("p", "number of restarts for heuristic DTW calculations");
    	passes.setArgs(1);
    	passes.setLongOpt("passes");
    	passes.setArgName("number");
    	
    	Option ranking = new Option("r", "type of ranking to use e.g. L (linear - default), E (exponential)");
    	ranking.setArgs(1);
    	ranking.setLongOpt("ranking");
    	ranking.setArgName("method");
    	
    	Option segment = new Option("s", "calculates the DTW of the instances starting from 'start' and upto 'start+increment' from the test set");
    	segment.setArgs(2);
//    	segment.setLongOpt("segmented");
    	segment.setArgName("start> <increment");   	
    	
    	Options opts = new Options();
    	opts.addOption(help).addOption(fileName).addOption(type).addOption(winSz).addOption(passes).addOption(segment).addOption(ranking);
    	return opts;
    }
}