package wiki_online_extractor.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wiki_online_extractor.WikiPage;


/**
 * XML parser implementation for handling XML documents provided by WikiMedia API.
 * 
 * @author Michal Samek, samek.michal @ gmail.com
 *
 */
public class WikiPageXMLParser extends DefaultHandler {

	/**
	 * Temporary buffer for one parsed page.
	 */
	private WikiPage page;
	
	/**
	 * List of parsed pages.
	 */
	private ArrayList<WikiPage> pages = new ArrayList<WikiPage>();
	
	/**
	 * String specifying position to continue in current dump stream.
	 */
	private String gapContinue = ""; // empty string will start from first page
	
	/**
	 * Counter for all parsed records.
	 */
	private Long counter = new Long(0);
	
	/**
	 * Instance of used SAX parser.
	 */
	private SAXParser parser;
	
	/**
	 * String specifying end boundary of dumped range - exclusive bound.
	 */
	private String end = null;

	
	/**
	 * Public constructor - only constructs appropriate SAXParser.
	 * 
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public WikiPageXMLParser() throws SAXException,
			ParserConfigurationException {
		// Create a "parser factory" for creating SAX parsers
		SAXParserFactory spfac = SAXParserFactory.newInstance();

		// Now use the parser factory to create a SAXParser object
		parser = spfac.newSAXParser();
	}

	/**
	 * Check if title is accepted in our specified range.
	 * Range <start, end) start is included, end is excluded.
	 * 
	 * @param title
	 * @return true if it should be accepted, false otherwise
	 */
	protected boolean accept(String title) {
		if (end == null || title.startsWith(end))
			return true;
		return false;
	}

	/**
	 * Parsing the InputStream.
	 * 
	 * @param input InputStream which is to be parsed
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parse(InputStream input) throws SAXException, IOException {
		gapContinue = null;
		pages.clear();
		parser.parse(input, this);
	}
	
	/**
	 * Getter for List containing parsed records.
	 * @return List of parsed WikiPage-s
	 */
	public List<WikiPage> getPages() {
		return pages;
	}

	/**
	 * Check if the parser has found gapcontinue attribute, that specify where
	 * do we start our next query.
	 * 
	 * @return true if gapcontinue was found, false otherwise
	 */
	public boolean hasNext() {
		return (this.gapContinue != null);
	}
	
	/**
	 * Sets gapContinue to HTML encoded version of str.
	 * @param str
	 * @throws SAXException
	 */
	public void setGapContinue(String str) throws SAXException {
		try {
			this.gapContinue = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
			throw new SAXException();
		}
	}

	/**
	 * Getter for gapContinue.
	 * @return
	 */
	public String getGapContinue() {
		return this.gapContinue;
	}

	/**
	 * Setter for end parameter specifying the end of range. 
	 * @param end
	 */
	public void setEnd(String end) {
		this.end = end;
	}

	
	/**
	 * We are interested only in <page>, <allpages> tags.
	 * 
	 * @throws SAXException
	 * @throws SAXTerminatorExpection if end of range was reached
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("page")) {
			final String parsedTitle = attributes.getValue("title");

			if (!accept(parsedTitle)) {
				throw new SAXTerminatorExpection();
			}

			page = new WikiPage(attributes.getValue("pageid"), parsedTitle,
					attributes.getValue("ns"),
					attributes.getValue("fullurl"),
					attributes.getValue("touched"),
					attributes.getValue("lastrevid"));
			pages.add(page);
			counter++;
		} else if (qName.equalsIgnoreCase("allpages")) {
			setGapContinue(attributes.getValue("gapcontinue"));
			System.err.println("\twill continue with: " + gapContinue);
			System.err.flush();
		}
	}
}
