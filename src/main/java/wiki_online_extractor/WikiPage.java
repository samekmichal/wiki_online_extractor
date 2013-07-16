package wiki_online_extractor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Class for representing parsed WikiPage.
 * 
 * @author Michal Samek, samek.michal @ gmail.com
 *
 */
public class WikiPage {

	private String title;
	private String id;
	private String namespace;
	private Date touched;
	private Long revid;
	private String fullUrl;

	public WikiPage(String id, String title, String namespace, String fullUrl,
			String touched, String revid) {
		this.id = id;
		this.title = title;
		this.namespace = namespace;
		this.fullUrl = fullUrl;
		DateFormat df = new SimpleDateFormat("yyyy-mm-dd'T'kk:mm:ss'Z'",
				Locale.ENGLISH);
		try {
			this.touched = df.parse(touched);
		} catch (ParseException e) {
		}
		this.revid = Long.valueOf(revid);
	}

	@Override
	public String toString() {
		return this.title + "\t" + this.id + "\t" + this.fullUrl;
	}

	public String toJSON() {
		return "{\"title\":\"" + this.title + "\"," +
				"\"id\":\"" + this.id + "\"," +
				"\"namespace\":\"" + this.namespace + "\"," +
				"\"fullUrl\":\"" + this.fullUrl + "\"," +
				"\"touched\":\"" + this.touched + "\"," + 
				"\"revid\":\"" + this.revid + "\"}";
	}
}
