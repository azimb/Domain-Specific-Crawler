package edu.carleton.comp4601.dao;

import edu.carleton.comp4601.graph.CrawlerGraph;

public interface GraphStore {
	void create(CrawlerGraph g);

	CrawlerGraph findOne();
}
