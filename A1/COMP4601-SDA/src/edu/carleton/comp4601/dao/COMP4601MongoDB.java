package edu.carleton.comp4601.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import edu.carleton.comp4601.model.COMP4601Document;

public class COMP4601MongoDB implements COMP4601Store {
	static String ID = "docId";
	static String URL = "URL";
	static String LINKS = "Links";
	static String DATE = "Date";
	static String CONTENT = "Content";
	static String METADATA = "Metadata";
	static String TYPE = "Type";

	static COMP4601Store instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> coll;

	public COMP4601MongoDB() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("crawler");
		coll = db.getCollection("comp4601documents");
	}

	public static COMP4601Store getInstance() {
		if (instance == null)
			instance = new COMP4601MongoDB();
		return instance;
	}

	@Override
	public void create(COMP4601Document d) {
		Document doc;
		doc = new Document(ID, d.getDocid()).append(URL, d.getUrl()).append(LINKS, d.getLinks())
				.append(DATE, d.getDate()).append(CONTENT, d.getContent()).append(METADATA, d.getMetadata())
				.append(TYPE, d.getType());
		coll.insertOne(doc);
	}

	@Override
	public COMP4601Document find(int docid) {
		FindIterable<Document> cursor = coll.find(Filters.eq(ID, docid));
		MongoCursor<Document> c = cursor.iterator();

		if (c.hasNext()) {
			Document object = c.next();
			return convertDoc(object);
		} else
			return null;
	}

	@Override
	public List<COMP4601Document> findAll() {
		FindIterable<Document> cursor = coll.find();
		MongoCursor<Document> c = cursor.iterator();
		List<COMP4601Document> docs = new ArrayList<COMP4601Document>();
		while (c.hasNext()) {
			Document object = c.next();
			docs.add(convertDoc(object));
		}
		return docs;
	}

	private COMP4601Document convertDoc(Document object) {
		COMP4601Document d = new COMP4601Document(object.getInteger(ID), object.getString(URL));
		d.setLinks((ArrayList<String>) object.get(LINKS));
		d.setDate(object.getDate(DATE));
		d.setContent(object.getString(CONTENT));
		d.setMetadata((Map<String, String>) object.get(METADATA));
		d.setType(object.getString(TYPE));
		return d;
	}

}
