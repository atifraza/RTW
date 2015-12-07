import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.*;

import utils.nearestneighbour.*;

/**
 * InitTSDistanceTest is used for launching the distance functions
 * with the provided options after parsing the CLI arguments.
 * 
 * @author Atif Raza
 */
public class InitTSDistanceTest {
    // The following static String variables are the CLI switches
    protected static String fileSw = "f";
    protected static String fileSwL = "file";
    protected static String outDirSw = "o";
    protected static String outDirSwL = "out-dir";
    protected static String helpSw = "h";
    protected static String helpSwL = "help";
    protected static String windowSw = "w";
    protected static String bandSwL = "window";
    protected static String twTypeSw = "t";
    protected static String twTypeSwL = "type";
    protected static String rngSw = "g";
    protected static String rngSwL = "rng";
    protected static String rngSeedSw = "s";
    protected static String rngSeedSwL = "seed";
    protected static String passesSw = "p";
    protected static String passesSwL = "passes";
    protected static String rankingSw = "r";
    protected static String rankingSwL = "ranking";
    protected static String normSw = "d";
    protected static String normSwL = "distance";
    protected static String segmentSw = "segment";
    protected static String postfixSw = "postfix";
    
    /**
     * @param args contains CLI switches and parameters
     */
    public static void main(String[] args) {
        String dsName = null;       // Time series dataset name
        String outDir = null;       // Directory path for saving
                                    // results
        String tsDistType = null;   // Distance type
        String rng = null;          // Random Number Distribution
        String ranking = null;      // Linear or Exponential
        String passes = null;       // Number of
                                    // re-evaluations/restarts
        String fnPostfix = null;    // Distinguishing postfix for
                                    // filename
        
        int window = 0;             // Sakoe-Chiba window as
                                    // percentage
        int start = 0;              // Instance processing start point
        int inc = 0;                // Number of instances to process
                                    // starting from the "start" point
        
        long rngSeed = -1;          // Random Number Generator seed:
                                    // -1: Seed from system time; >=0:
                                    // Integer seed
        
        double normType = 2;        // Lp-norm exponent for distance
                                    // measure
        
        NearestNeighbourBase tsDist;    // Time series distance
                                        // measurement object
        
        Options options = constructCLIOptions();    // Constructs CLI
                                                    // options and
                                                    // corresponding
                                                    // help text
        try {
            // Create a GnuParser object
            CommandLineParser cliParser = new GnuParser();
            // Parse provided CLI arguments according to available options
            CommandLine cmdLine = cliParser.parse(options, args);
            if (cmdLine.getOptions().length == 0 || cmdLine.hasOption(helpSw)) {
                // No CLI argument provided OR Help required
                printHelp(options, true, System.out);   // Print help
                System.exit(0);                         // and exit
            }
            // Get the values provided with CLI arguments
            dsName = cmdLine.getOptionValue(fileSw);
            outDir = cmdLine.getOptionValue(outDirSw, "");
            window = Integer.parseInt(cmdLine.getOptionValue(windowSw, "100"));
            tsDistType = cmdLine.getOptionValue(twTypeSw, "R");
            normType = Double.parseDouble(cmdLine.getOptionValue(normSw, "2"));
            fnPostfix = cmdLine.getOptionValue(postfixSw, "");
            if (tsDistType.equals("R")) {
                rng = cmdLine.getOptionValue(rngSw, "G");
                rngSeed = Long.parseLong(cmdLine.getOptionValue(rngSeedSw,
                                                                "-1"));
                ranking = cmdLine.getOptionValue(rankingSw, "E");
                passes = cmdLine.getOptionValue(passesSw, "0");
            }
            if (cmdLine.hasOption(segmentSw)) {
                String[] temp = cmdLine.getOptionValues(segmentSw);
                start = Integer.parseInt(temp[0]);
                inc = Integer.parseInt(temp[1]);
            }
        } catch (ParseException parseException) {
            // Catch any exceptions here and print the help
            System.err.println("Encountered exception while parsing input arguments:\n"
                               + parseException.getMessage() + "\n\n");
            printHelp(options, true, System.out);
            System.exit(0);
        }
        
        System.out.println("Dataset:          " + dsName);
        System.out.println("Type:             " + tsDistType);
        if (!tsDistType.equals("E")) {
            System.out.println("Window:           " + window);
            System.out.println("Lp norm power:    " + normType);
        }
        if (tsDistType.equals("R")) {
            System.out.println("Ranking:          " + ranking);
            System.out.println("Passes:           " + passes);
            System.out.println("RNG:              " + rng);
        }
        if (inc != 0) {
            System.out.println("Start:            " + start);
            System.out.println("End:              " + inc);
        }
        switch (tsDistType) {
            case "E":
                tsDist = new NearestNeighbourED(dsName, outDir, start, inc);
                tsDist.classify();
                break;
            case "D":
                tsDist = new NearestNeighbourDTW(dsName, outDir, start,
                                                 inc, normType, window);
                tsDist.classify();
                break;
            case "L":
                tsDist = new NearestNeighbourLTW(dsName, outDir, start,
                                                 inc, normType, window);
                tsDist.classify();
                break;
            case "R":
                String dirPath = "";
                if (ranking.equals("E")) {
                    dirPath = "exp/";
                } else {
                    dirPath = "lin/";
                }
                dirPath += passes;
                if (!outDir.equals("")) {
                    outDir += "/";
                }
                tsDist = new NearestNeighbourRTW(dsName, "temp", 0,
                                                 5, normType, window, ranking,
                                                 passes, rng, rngSeed, fnPostfix);
                tsDist.classify();
                tsDist = new NearestNeighbourRTW(dsName, outDir + dirPath,
                                                 start, inc, normType,
                                                 window, ranking, passes,
                                                 rng, rngSeed, fnPostfix);
                tsDist.classify();
                break;
        }
        System.out.println("\nDone");
    }
    
    /**
     * Print help to provided OutputStream.
     * 
     * @param options Command-line options to be part of usage.
     * @param detailed
     * @param out OutputStream to which to write the usage
     *            information.
     */
    public static void printHelp(Options options, boolean detailed,
                                 OutputStream out) {
        String cliSyntax = "java InitTSDistanceTest";
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (detailed) {
            helpFormatter.printHelp(writer, 120, cliSyntax, "", options, 7, 1,
                                    "", true);
        } else {
            helpFormatter.printUsage(writer, 120, cliSyntax, options);
        }
        writer.flush();
    }
    
    private static Options constructCLIOptions() {
        Option help = new Option(helpSw, "prints the help for the program");
        help.setLongOpt(helpSwL);
        
        Option datafileName = new Option(fileSw,
                                         "name of the dataset - REQUIRED Parameter");
        datafileName.setLongOpt(fileSwL);
        datafileName.setRequired(true);
        datafileName.setArgs(1);
        datafileName.setArgName("dataset");
        
        Option dtwType = new Option(twTypeSw,
                                    "defines the warping/distance type, valid options are "
                                              + "R - RandomizedTW (default), D - DTW, L - LuckyTW, E - Euclidean Distance");
        dtwType.setLongOpt(twTypeSwL);
        dtwType.setArgs(1);
        dtwType.setArgName("warpingType");
        
        Option winSz = new Option(windowSw,
                                  "Warping window size percentage e.g. 1, 5, 10, 50, 100 (default)\n"
                                            + "-1 for Calculating through Leave One Out Cross Validation of Training Set");
        winSz.setLongOpt(bandSwL);
        winSz.setArgs(1);
        winSz.setArgName("percent");
        
        Option outputDir = new Option(outDirSw,
                                      "sub-directory of data directory to use for saving the results");
        outputDir.setLongOpt(outDirSwL);
        outputDir.setArgs(1);
        outputDir.setArgName("directory");
        
        Option rngType = new Option(rngSw,
                                    "defines the RNG type, G (Gaussian random numbers - default), "
                                           + "U (Uniform random numbers)");
        rngType.setLongOpt(rngSwL);
        rngType.setArgs(1);
        rngType.setArgName("rngType");
        
        Option rngSeed = new Option(rngSeedSw,
                                    "-1 (default) for automatic seeding of RNG,  or integer as seed"
                                               + "(0 or greater)");
        rngSeed.setLongOpt(rngSeedSwL);
        rngSeed.setArgs(1);
        rngSeed.setArgName("rngSeed");
        
        Option passes = new Option(passesSw,
                                   "number of restarts for RandomizedTW calculations 0 (default), "
                                             + "I (increasing with number of runs), integer (for constant number of restarts)");
        passes.setLongOpt(passesSwL);
        passes.setArgs(1);
        passes.setArgName("number");
        
        Option ranking = new Option(rankingSw,
                                    "type of ranking e.g. E (exponential - default), L (linear)");
        ranking.setLongOpt(rankingSwL);
        ranking.setArgs(1);
        ranking.setArgName("method");
        
        Option distMeasure = new Option(normSw,
                                        "type of distance measure e.g. 2 (Euclidean - default), "
                                                + "1 (Manhattan), p (positive number for Lp norm with power=p, 0 (Binary))");
        distMeasure.setLongOpt(normSwL);
        distMeasure.setArgs(1);
        distMeasure.setArgName("distMeasure");
        
        Option segment = new Option(segmentSw,
                                    "calculates the warping of the instances starting from 'start' "
                                               + "and upto 'start+increment' from the test set");
        segment.setArgs(2);
        segment.setArgName("startPoint> <increment");
        
        Option postfix = new Option(postfixSw,
                                    "add postfix to file names for identifying different experiments");
        postfix.setArgs(1);
        postfix.setArgName("fileNamePostfix");
        
        Options opts = new Options();
        opts.addOption(help).addOption(datafileName).addOption(outputDir)
            .addOption(dtwType).addOption(rngType).addOption(winSz)
            .addOption(passes).addOption(segment).addOption(ranking)
            .addOption(distMeasure).addOption(rngSeed).addOption(postfix);
        return opts;
    }
}
