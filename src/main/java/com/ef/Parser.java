package com.ef;

import com.wallet.Duration;
import com.wallet.domain.LogEntry;
import com.wallet.services.LoadLogFile;
import com.wallet.services.QueryLogFile;
import com.wallet.domain.ReportEntry;
import com.wallet.utils.DateParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Our main implementation into the parser algorithm for loading up a file.
 */
public class Parser {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Parser.class);


    /**
     * Call to parse out the request:
     *
     *     java -cp "parser.jar" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
     *
     * @param args Duration which can only be hourly or daily and int of threshold.
     */
    public static void main(String[] args) {

        Options options = new Options();
        options.addRequiredOption("s", "startDate", true, "start date/time for the parser search");
        options.addRequiredOption("d", "duration", true, "the duration type to search for");
        options.addRequiredOption("t", "threshold", true, "the amount of time in that duration");

        // if not passed in we assume they want to query off the current data
        options.addOption(
            Option.builder("a")
                .required(false)
                .hasArg(true)
                .longOpt("accesslog")
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            LocalDateTime startDate = DateParser.toLocalDateTimeCommandLine(commandLine.getOptionValue("startDate"));
            Duration duration = Duration.valueOf(commandLine.getOptionValue("duration").toUpperCase());
            int threshold = Integer.parseInt(commandLine.getOptionValue("threshold"));

            // Load if its not there
            if (commandLine.hasOption("accesslog")) {
                LoadLogFile loader = new LoadLogFile();
                loader.loadAccessLogs(commandLine.getOptionValue("accesslog"));
            }

            // Now query for the data
            QueryLogFile query = new QueryLogFile();
            List<ReportEntry> report =query.createLogFileReport(startDate, duration, threshold);

            // Print out the data
            printReport(report);
        } catch (ParseException exception) {
            log.error("Parse error: {}", exception.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("General exception: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static final void printReport(List<ReportEntry> reports) {
        for (ReportEntry report: reports) {
            System.out.println("IP : " + report.ip + " found " + report.count + " entries");
            for (LogEntry entry : report.entries) {
                System.out.println("\t\t : Date: " + entry.date + ", User-Agent: " + entry.userAgent + ", status: " + entry.status + ", request: " + entry.request);
            }
        }
    }
}
