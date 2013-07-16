package wiki_online_extractor;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import wiki_online_extractor.parser.SAXTerminatorExpection;
import wiki_online_extractor.parser.WikiPageXMLParser;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;

/**
 * Main application class.
 * 
 * @author Michal Samek, samek.michal @ gmail.com
 * 
 */
public class App {

	/**
	 * Limit count for retrieved records
	 */
	private static Long limit;

	/**
	 * Format output to JSON
	 */
	private static boolean jsonFormat = false;

	/**
	 * Verbose output?
	 */
	private static boolean verbose = false;

	/**
	 * Base URL for retrieving objects
	 */
	private static String URL_BASE = "http://en.wikipedia.org/w/api.php?action=query&generator=allpages&gaplimit=500&prop=info&inprop=url&format=xml&gapfrom=";

	private static String HELP_MSG = "URL extractor for wikipedia\n"
			+ "Through wikipedia api (using generator allpages) extracts titles and urls (nice urls)."
			+ "";

	public static void main(String[] args) throws JSAPException {

		/* Parsing command line arguments using JSAP */
		FlaggedOption startOption = new FlaggedOption("start")
				.setStringParser(JSAP.STRING_PARSER).setDefault("")
				.setRequired(false).setShortFlag('s').setLongFlag("start");
		startOption.setHelp("start offset of the dump: string (inclusive)");

		FlaggedOption endOption = new FlaggedOption("end")
				.setStringParser(JSAP.STRING_PARSER).setRequired(false)
				.setShortFlag('e').setLongFlag("end");
		endOption.setHelp("end offset of the dump: string (exclusive)");

		FlaggedOption langOption = new FlaggedOption("lang")
				.setStringParser(JSAP.STRING_PARSER).setRequired(false)
				.setDefault("en").setShortFlag('L').setLongFlag("lang");
		langOption.setHelp("set language distribution of wikipedia");

		FlaggedOption limitOption = new FlaggedOption("limit")
				.setStringParser(JSAP.LONG_PARSER).setRequired(false)
				.setShortFlag('l').setLongFlag("limit");
		limitOption
				.setHelp("set count limit for objects retrieved (over 500 uses multiplies of 500)");

		Switch jsonSwitch = new Switch("json", 'j', "json",
				"outputs output to json");
		Switch verboseSwitch = new Switch("verbose", 'v', "verbose",
				"print verbose output");

		SimpleJSAP jsap = new SimpleJSAP("url_extractor", HELP_MSG,
				new Parameter[] { startOption, endOption, langOption,
						limitOption, jsonSwitch });

		JSAPResult config = jsap.parse(args);

		if (jsap.messagePrinted())
			return;

		jsonFormat = config.getBoolean("json");
		verbose = config.getBoolean("verbose");

		try {
			URL url;
			WikiPageXMLParser parser = new WikiPageXMLParser();
			parser.setGapContinue(config.getString("start"));
			parser.setEnd(config.getString("end"));

			// Was language mutation specified
			if (config.contains("lang")) {
				URL_BASE = "http://" + config.getString("lang")
						+ URL_BASE.substring(9);
			}

			if (config.contains("limit")) {
				limit = config.getLong("limit");
				if (limit < 500)
					URL_BASE = URL_BASE.replace("500", String.valueOf(limit));

			}

			// Counter of successfuly retrieved records in all requests
			long counter = 0;
			// List with retrieved records in one request
			List<WikiPage> pages = null;

			if (jsonFormat)
				System.out.println("{\"pages\":[");

			// While we have not exceeded the limit and parser knows where to
			// continue
			while ((limit == null || counter < limit) && parser.hasNext()) {

				// Build new URL that will follow after the end of previous
				// request
				url = new URL(URL_BASE + parser.getGapContinue());
				try {
					parser.parse(url.openStream());
				} catch (SAXTerminatorExpection e) {
					// Parser has reached the limit of records requested ...
					break;
				} finally {
					pages = parser.getPages();
					Iterator<WikiPage> it = pages.iterator();
					while (it.hasNext()) {
						if (jsonFormat)
							System.out.print(it.next().toJSON() + ",");
						else
							System.out.println(it.next().toString());
					}
					System.out.flush();
					counter += pages.size();
					if (verbose) {
						System.err.println("\tretrieved " + counter
								+ " records");
						System.err.flush();
					}
				}
			}
			if (jsonFormat)
				System.out.println("]}");

			if (verbose)
				System.err
						.println("Processed " + counter + " records, bye ...");

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}
	}
}
