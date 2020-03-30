package edu.carleton.comp4601.dao;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.alg.scoring.PageRank;

import edu.carleton.comp4601.graph.CrawlerGraph;
import edu.carleton.comp4601.graph.GraphLayoutVisualizer;
import edu.carleton.comp4601.graph.PageVertex;
import edu.carleton.comp4601.model.COMP4601Document;

public class MongoClient {
	COMP4601Store docs;
	GraphStore graphs;

	public MongoClient() {
		docs = COMP4601MongoDB.getInstance();
		graphs = GraphMongoDB.getInstance();
	}

	public static void main(String[] args) {
		MongoClient m = new MongoClient();
		// m.addFakeDoc();
		// m.seeGraph();
		m.pageRank();

	}

	private void addFakeDoc() {
		COMP4601Document d = new COMP4601Document(9999, "fakeurl.com");
		d.setContent(getSpam());
		docs.create(d);
	}

	private String getSpam() {
		return "eclipse";
	}

	private void seeGraph() {
		GraphLayoutVisualizer.visualizeGraph(graphs.findOne());
	}

	private void pageRank() {
		CrawlerGraph g = graphs.findOne();
		PageRank pr = new PageRank(g);
		System.out.println(pr.getScores().toString());

		Map<PageVertex, Double> newMap = new HashMap<PageVertex, Double>();

		double max = 0;

		for (Object o : pr.getScores().entrySet()) {
			Map.Entry<PageVertex, Double> e = (Map.Entry<PageVertex, Double>) o;
			newMap.put(e.getKey(), e.getValue());
			// System.out.println(e.getValue() + ": " + e.getKey());
			if (e.getValue() > max) {
				max = e.getValue();
				System.out.println(e.getValue() + ":  " + e.getKey());
			}
		}

	}

}
