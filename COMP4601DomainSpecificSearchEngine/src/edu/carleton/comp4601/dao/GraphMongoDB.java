package edu.carleton.comp4601.dao;

import java.io.IOException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import edu.carleton.comp4601.crawler.Marshaller;
import edu.carleton.comp4601.graph.CrawlerGraph;
import edu.carleton.comp4601.model.COMP4601Document;

public class GraphMongoDB implements GraphStore {

	static String GRAPH = "Graph";

	static GraphStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> coll;

	public GraphMongoDB() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("crawler");
		coll = db.getCollection("graphs");
	}

	@Override
	public void create(CrawlerGraph g) {
		Document doc;
		try {
			doc = new Document(GRAPH, Marshaller.serializeObject(g));
			coll.insertOne(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static GraphStore getInstance() {
		if (instance == null)
			instance = new GraphMongoDB();
		return instance;
	}

	@Override
	public CrawlerGraph findOne() {
		FindIterable<Document> cursor = coll.find();
		MongoCursor<Document> c = cursor.iterator();

		if (c.hasNext()) {
			Document object = c.next();
			try {
				return (CrawlerGraph) Marshaller
						.deserializeObject(object.get(GRAPH, org.bson.types.Binary.class).getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
