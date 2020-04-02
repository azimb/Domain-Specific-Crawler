package edu.carleton.comp4601.crawler;

import java.util.HashMap;
import java.util.List;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.carleton.comp4601.dao.COMP4601MongoDB;
import edu.carleton.comp4601.dao.COMP4601Store;
import edu.carleton.comp4601.dao.GraphMongoDB;
import edu.carleton.comp4601.dao.GraphStore;
import edu.carleton.comp4601.graph.CrawlerGraph;
import edu.carleton.comp4601.graph.GraphLayoutVisualizer;
import edu.carleton.comp4601.graph.PageVertex;
import edu.carleton.comp4601.model.COMP4601Document;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class BasicCrawlController {

	//public static void main(String[] args) throws Exception {
	public static void runCrawl(String topic) throws Exception {	
		Logger logger = LoggerFactory.getLogger(BasicCrawlController.class);

		COMP4601Store docStore;
		GraphStore graphStore;

		CrawlConfig config = new CrawlConfig();
		// Set the folder where intermediate crawl data is stored (e.g. list of urls
		// that are extracted from previously
		// fetched pages and need to be crawled later).
		config.setCrawlStorageFolder("/data/crawl/root");

		// Be polite: Make sure that we don't send more than 1 request per second (1000
		// milliseconds between requests).
		// Otherwise it may overload the target servers.
		config.setPolitenessDelay(1000);

		// You can set the maximum crawl depth here. The default value is -1 for
		// unlimited depth.
		config.setMaxDepthOfCrawling(-1);

		// You can set the maximum number of pages to crawl. The default value is -1 for
		// unlimited number of pages.
		config.setMaxPagesToFetch(5);
		//config.setMaxPagesToFetch(1000);

		// Should binary data should also be crawled? example: the contents of pdf, or
		// the metadata of images etc
		config.setIncludeBinaryContentInCrawling(true);

		// Do you need to set a proxy? If so, you can use:
		// config.setProxyHost("proxyserver.example.com");
		// config.setProxyPort(8080);

		// If your proxy also needs authentication:
		// config.setProxyUsername(username); config.getProxyPassword(password);

		// This config parameter can be used to set your crawl to be resumable
		// (meaning that you can resume the crawl from a previously
		// interrupted/crashed crawl). Note: if you enable resuming feature and
		// want to start a fresh crawl, you need to delete the contents of
		// rootFolder manually.
		config.setResumableCrawling(false);

		// Instantiate the controller for this crawl.
		CustomPageFetcher pageFetcher = new CustomPageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		// For each crawl, you need to add some seed urls. These are the first
		// URLs that are fetched and then the crawler starts following links
		// which are found in these pages
		
		
		controller.addSeed("https://en.wikipedia.org/wiki/" + topic);
		
		//controller.addSeed("https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/resources/N-0.html");
		// controller.addSeed("https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/handouts/");
		// controller.addSeed("https://en.wikipedia.org/wiki/Batman");

		// Number of threads to use during crawling. Increasing this typically makes
		// crawling faster. But crawling
		// speed depends on many other factors as well. You can experiment with this to
		// figure out what number of
		// threads works best for you.
		//int numberOfCrawlers = 8;
		int numberOfCrawlers = 1;

		// Start the crawl. This is a blocking operation, meaning that your code
		// will reach the line after this only when crawling is finished.
		// controller.start(factory, numberOfCrawlers);
		//controller.start(BasicCrawler.class, numberOfCrawlers);

		controller.start(BasicCrawler.class);
		
		Multigraph graph = new Multigraph(DefaultEdge.class);
		List<Object> crawlersLocalData = controller.getCrawlersLocalData();
		docStore = COMP4601MongoDB.getInstance();

		HashMap<String, PageVertex> vertexMap = new HashMap<String, PageVertex>();

		// Make all vertices
		for (Object localData : crawlersLocalData) {
			List<COMP4601Document> docs = (List<COMP4601Document>) localData;
			for (COMP4601Document doc : docs) {
				docStore.create(doc);
				PageVertex v = new PageVertex(doc);
				graph.addVertex(v);
				vertexMap.put(doc.getUrl(), v);
			}
		}

		// Make all edges
		for (Object localData : crawlersLocalData) {
			List<COMP4601Document> docs = (List<COMP4601Document>) localData;
			for (COMP4601Document doc : docs) {
				for (String link : doc.getLinks()) {
					PageVertex v1 = vertexMap.get(doc.getUrl());
					PageVertex v2 = vertexMap.get(link);
					if (v1 != null && v2 != null) {
						graph.addEdge(v1, v2);
					}
				}
			}
		}

		CrawlerGraph g = new CrawlerGraph(graph);

		graphStore = GraphMongoDB.getInstance();
		graphStore.create(g);

		logger.info(g.toString());
		logger.info("Edges: {}", g.edgeSet().toString());
		logger.info("Vertices: {}", g.vertexSet().toString());
		GraphLayoutVisualizer.visualizeGraph(g);
	}

}