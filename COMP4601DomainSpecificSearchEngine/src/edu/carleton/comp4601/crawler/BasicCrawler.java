package edu.carleton.comp4601.crawler;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.carleton.comp4601.model.COMP4601Document;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class BasicCrawler extends WebCrawler {

	Logger logger;

	private final List<COMP4601Document> docs;

	private COMP4601Document d;

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif" + "|mp3|mp4|zip|gz))$");

	// private static final Pattern IMAGE_EXTENSIONS =
	// Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

	/**
	 * Creates a new crawler instance.
	 *
	 * @param numSeenImages This is just an example to demonstrate how you can pass
	 *                      objects to crawlers. In this example, we pass an
	 *                      AtomicInteger to all crawlers and they increment it
	 *                      whenever they see a url which points to an image.
	 */
	public BasicCrawler() {
		logger = LoggerFactory.getLogger(BasicCrawler.class);
		docs = new ArrayList<COMP4601Document>();
	}

	/**
	 * You should implement this function to specify whether the given url should be
	 * crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		// Ignore the url if it has an extension that matches our defined set of image
		// extensions.
		if (FILTERS.matcher(href).matches()) {
			return false;
		}

		// Only accept the url if it is in the wanted domain and protocol is "http".
		return href.startsWith("https://sikaman.dyndns.org");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed by
	 * your program.
	 */
	@Override
	public void visit(Page page) {
		long startTime = System.nanoTime();

		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();

		d = new COMP4601Document(docid, url);

		InputStream input = new ByteArrayInputStream(page.getContentData());
		Tika tika = new Tika();
		String type;
		try {
			type = tika.detect(input);
			logger.info("Type: {}", type);
			if (type.equals("text/html")) {
				d = parseHTML(page, d);
			} else {
				d = parseNonHTML(page, d);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String domain = page.getWebURL().getDomain();
		String path = page.getWebURL().getPath();
		String subDomain = page.getWebURL().getSubDomain();
		String parentUrl = page.getWebURL().getParentUrl();
		String anchor = page.getWebURL().getAnchor();

		logger.info("Docid: {}", docid);
		logger.info("URL: {}", url);
		logger.info("Domain: '{}'", domain);
		logger.info("Sub-domain: '{}'", subDomain);
		logger.info("Path: '{}'", path);
		logger.info("Parent page: {}", parentUrl);
		logger.info("Anchor text: {}", anchor);

		d.addToMetaData("domain", domain);
		d.addToMetaData("subDomain", subDomain);
		d.addToMetaData("path", path);
		d.addToMetaData("anchor", anchor);

		d.setParent(parentUrl);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

			ArrayList<String> outgoingUrls = new ArrayList<String>();
			for (WebURL link : links) {
				outgoingUrls.add(link.getURL());
			}

			d.setLinks(outgoingUrls);

			Document doc = Jsoup.parse(html);
			HashMap<String, String> soup_links = getLinks(doc);
			// TODO images
			HashMap<String, String> soup_imgs = getImages(doc);
			List<String> soup_text = getText(doc);

			d.setContent(String.join(" ", soup_text));
		}

		Header[] responseHeaders = page.getFetchResponseHeaders();
		if (responseHeaders != null) {
			logger.info("Response headers:");
			for (Header header : responseHeaders) {
				logger.info("\t{}: {}", header.getName(), header.getValue());
			}
		}

		d.setDate(new Date());

		long endTime = System.nanoTime();
		long pageVisitTime = endTime - startTime;
		logger.info("Page visit time: {}", pageVisitTime);
		logger.info("=============");
		docs.add(d);
	}

	public HashMap<String, String> getLinks(Document doc) {
		HashMap<String, String> list = new HashMap<String, String>();
		Elements links = doc.select("a[href]");
		logger.info("Links: ");
		for (Element link : links) {
			logger.info("\t{}: {}", link.text(), link.attr("href"));
			list.put(link.text(), link.attr("href"));
		}
		return list;
	}

	public HashMap<String, String> getImages(Document doc) {
		HashMap<String, String> list = new HashMap<String, String>();
		String selector = "img[src~=(?i)\\.(png|jpe?g|gif)]";
		Elements images = doc.select(selector);
		logger.info("Images: ");
		for (Element im : images) {
			logger.info("\t{}", im.attr("alt"));
			list.put(im.attr("src"), im.attr("alt"));
		}
		return list;
	}

	public List<String> getText(Document doc) {
		List<String> list = new ArrayList<String>();
		Elements texts = doc.select("p, h1, h2, h3, h4");
		logger.info("Text: ");
		for (Element t : texts) {
			logger.info("\t{}", t.text());
			list.add(t.text());
		}
		return list;
	}

	public COMP4601Document parseHTML(Page page, COMP4601Document d) {
		InputStream input = new ByteArrayInputStream(page.getContentData());
		ContentHandler handler;
		try {
			FileWriter wr = new FileWriter(documentName(page));
			handler = new BodyContentHandler(wr);
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
			Parser parser = new HtmlParser();
			String contents = page.getWebURL().getURL();

			try {
				parser.parse(input, handler, metadata, context);
				logger.info("TITLE: {}", metadata.get("title"));
				logger.info("AUTHOR: {}", metadata.get("Author"));
				logger.info("MIME_TYPE: {}", metadata.get("Content-Type"));

				d.setType(metadata.get("Content-Type"));

				System.out.println("METADATA: ");
				String[] metadataNames = metadata.names();
				for (String name : metadataNames) {
					System.out.println(name + ": " + metadata.get(name));
					d.addToMetaData(name, metadata.get(name));
				}
			} catch (IOException | SAXException | TikaException e) {
				e.printStackTrace();
			} finally {
				contents = new String(Files.readAllBytes(Paths.get(documentName(page))));
				d.setContent(contents);
			}
			wr.close();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return d;
	}

	public COMP4601Document parseNonHTML(Page page, COMP4601Document d) {
		InputStream input = new ByteArrayInputStream(page.getContentData());
		ContentHandler handler;
		try {
			FileWriter wr = new FileWriter(documentName(page));
			handler = new BodyContentHandler(wr);
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
			Parser parser = new AutoDetectParser();
			String contents = page.getWebURL().getURL();

			try {
				parser.parse(input, handler, metadata, context);

				logger.info("TITLE: {}", metadata.get("title"));
				logger.info("AUTHOR: {}", metadata.get("Author"));
				logger.info("MIME_TYPE: {}", metadata.get("Content-Type"));

				d.setType(metadata.get("Content-Type"));

				// getting the list of all meta data elements
				System.out.println("METADATA: ");
				String[] metadataNames = metadata.names();
				for (String name : metadataNames) {
					System.out.println(name + ": " + metadata.get(name));
					d.addToMetaData(name, metadata.get(name));
				}

			} catch (IOException | SAXException | TikaException e) {
				e.printStackTrace();
			} finally {
				contents = new String(Files.readAllBytes(Paths.get(documentName(page))));
				d.setContent(contents);
			}
//    		logger.info("CONTENTS:{}", contents);
			wr.close();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return d;
	}

	private String documentName(Page page) {
		int docid = page.getWebURL().getDocid();
		return "document" + docid + ".txt";
	}

	@Override
	public Object getMyLocalData() {
		return docs;
	}
}