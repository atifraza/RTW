import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import utils.dtw.*;

public class InitDTWTest {
    protected static String fileSwitch          = "f", fileSwitchLong           = "file";    
    protected static String outDirSwitch        = "o", outDirSwitchLong         = "out-dir";
    protected static String helpSwitch          = "h", helpSwitchLong           = "help";
    protected static String bandSwitch          = "w", bandSwitchLong           = "window";
    protected static String twTypeSwitch        = "t", twTypeSwitchLong         = "type";
    protected static String rngSwitch           = "g", rngSwitchLong            = "rng";
    protected static String rngSeedSwitch       = "s", rngSeedSwitchLong        = "seed";
    protected static String passesSwitch        = "p", passesSwitchLong         = "passes";
    protected static String rankingTypeSwitch   = "r", rankingTypeSwitchLong    = "ranking";
    protected static String distanceTypeSwitch  = "d", distanceTypeSwitchLong   = "distance";
    protected static String segmentSwitch       = "segment";

    public static void main(String[] args) {
        Options options = constructCLIOptions();
        String fileName = null, outDir = null, dtwType = null, rng = null, 
                ranking = null, passes = null;
        int window = 0;
        int start = 0, inc = 0;
        long rngSeed = -1;
        double distanceType = 2;

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
            dtwType = cmdLine.getOptionValue(twTypeSwitch, "R");
            distanceType = Double.parseDouble(cmdLine.getOptionValue(distanceTypeSwitch, "2"));

            if (dtwType.equals("R")) {
                rng = cmdLine.getOptionValue(rngSwitch, "G");
                rngSeed = Long.parseLong(cmdLine.getOptionValue(rngSeedSwitch, "-1"));
                ranking = cmdLine.getOptionValue(rankingTypeSwitch, "E");
                passes = cmdLine.getOptionValue(passesSwitch, "0");
            }

            if (cmdLine.hasOption(segmentSwitch)) {
                String[] temp = cmdLine.getOptionValues(segmentSwitch);
                start = Integer.parseInt(temp[0]);
                inc = Integer.parseInt(temp[1]);
            }
        } catch (ParseException parseException) { // checked exception
            System.err.println("Encountered exception while parsing input arguments:\n" + 
                                parseException.getMessage() + "\n\n");
            printHelp(options, true, System.out);
            System.exit(0);
        }

        BaseDTW dtw;
        System.out.println("Dataset:          " + fileName);
        System.out.println("Type:             " + dtwType);
        if (!dtwType.equals("E")) {
            System.out.println("Window:           " + window);
            System.out.println("Lp norm power:    " + distanceType);
        }
        if (dtwType.equals("R")) {
            System.out.println("Ranking:          " + ranking);
            System.out.println("Passes:           " + passes);
            System.out.println("RNG:              " + rng);
        }
        if (inc != 0) {
            System.out.println("Start:            " + start);
            System.out.println("End:              " + inc);
        }
        switch (dtwType) {
            case "E":
                dtw = new EuclideanDistance(fileName, outDir, start, inc);
                dtw.execute();
                break;
            case "N":
                dtw = new NormalDTW(fileName, outDir, distanceType, window, start, inc);
                dtw.execute();
                break;
            case "L":
                dtw = new LuckyDTW(fileName, outDir, distanceType, window, start, inc);
                dtw.execute();
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
                dtw = new HeuristicDTW(fileName, "temp", distanceType, window, ranking, passes, rng, rngSeed, 0, 5);
                dtw.execute();
                dtw = new HeuristicDTW(fileName, outDir + dirPath, distanceType, window, ranking, passes, rng, rngSeed, start, inc);
                dtw.execute();
                break;
        }
        System.out.println("\nDone");
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
        if (detailed) {
            helpFormatter.printHelp(writer, 120, cliSyntax, "", options, 7, 1, "", true);
        } else {
            helpFormatter.printUsage(writer, 120, cliSyntax, options);
        }
        writer.flush();
    }

    private static Options constructCLIOptions() {
        Option help = new Option(helpSwitch, "prints the help for the program");
        help.setLongOpt(helpSwitchLong);

        Option datafileName = new Option(fileSwitch, "name of the dataset - REQUIRED Parameter");
        datafileName.setLongOpt(fileSwitchLong);
        datafileName.setRequired(true);
        datafileName.setArgs(1);
        datafileName.setArgName("dataset");

        Option dtwType = new Option(twTypeSwitch, "defines the warping/distance type, valid options are " + 
                                    "R - RandomizedTW (default), N - Normal DTW, L - LuckyTW, E - Euclidean Distance");
        dtwType.setLongOpt(twTypeSwitchLong);
        dtwType.setArgs(1);
        dtwType.setArgName("warpingType");

        Option winSz = new Option(bandSwitch, "Warping window size percentage e.g. 1, 5, 10, 50, 100 (default)\n" +
                                  "-1 for Calculating through Leave One Out Cross Validation of Training Set");
        winSz.setLongOpt(bandSwitchLong);
        winSz.setArgs(1);
        winSz.setArgName("percent");

        Option outputDir = new Option(outDirSwitch, "sub-directory of data directory to use for saving the results");
        outputDir.setLongOpt(outDirSwitchLong);
        outputDir.setArgs(1);
        outputDir.setArgName("directory");

        Option rngType = new Option(rngSwitch, "defines the RNG type, G (Gaussian random numbers - default), " +
                                    "U (Uniform random numbers)");
        rngType.setLongOpt(rngSwitchLong);
        rngType.setArgs(1);
        rngType.setArgName("rngType");

        Option rngSeed = new Option(rngSeedSwitch, "-1 (default) for automatic seeding of RNG,  or integer as seed" +
                "(0 or greater)");
        rngSeed.setLongOpt(rngSeedSwitchLong);
        rngSeed.setArgs(1);
        rngSeed.setArgName("rngSeed");
        
        Option passes = new Option(passesSwitch, "number of restarts for RandomizedTW calculations 0 (default), " +
                                   "I (increasing with number of runs), integer (for constant number of restarts)");
        passes.setLongOpt(passesSwitchLong);
        passes.setArgs(1);
        passes.setArgName("number");

        Option ranking = new Option(rankingTypeSwitch, "type of ranking e.g. E (exponential - default), L (linear)");
        ranking.setLongOpt(rankingTypeSwitchLong);
        ranking.setArgs(1);
        ranking.setArgName("method");

        Option distMeasure = new Option(distanceTypeSwitch, "type of distance measure e.g. 2 (Euclidean - default), " +
                                        "1 (Manhattan), p (positive number for Lp norm with power=p, 0 (Binary))");
        distMeasure.setLongOpt(distanceTypeSwitchLong);
        distMeasure.setArgs(1);
        distMeasure.setArgName("distMeasure");

        Option segment = new Option(segmentSwitch, "calculates the warping of the instances starting from 'start' " +
                                    "and upto 'start+increment' from the test set");
        segment.setArgs(2);
        segment.setArgName("startPoint> <increment");

        Options opts = new Options();
        opts.addOption(help).addOption(datafileName).addOption(outputDir).addOption(dtwType).addOption(rngType)
            .addOption(winSz).addOption(passes).addOption(segment).addOption(ranking).addOption(distMeasure)
            .addOption(rngSeed);
        return opts;
    }
}
