package edu.carleton.comp4601.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.lucene.Search;
import edu.carleton.comp4601.model.COMP4601Document;
import edu.carleton.comp4601.utility.SDAConstants;
import edu.carleton.comp4601.utility.SearchException;
import edu.carleton.comp4601.utility.SearchResult;
import edu.carleton.comp4601.utility.SearchServiceManager;

@Path("/dsse")
public class SearcharbleDocumentArchive {

	SearchServiceManager ssm;

	public SearcharbleDocumentArchive() {
		// Change DS host before initialization
		// SearchServiceManager.host = "address"

		ssm = SearchServiceManager.getInstance();
		Search.getInstance().indexMongoDocuments();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello() {
		return "COMP4601 Domain Specific Search Engine: Zachary Seguin and Azim Baghadiya";
	}

	@GET
	@Path("{docid}")
	public Document getDocument(@PathParam("docid") String id) {
		return null;
	}

	@DELETE
	public boolean deleteDocument(String id) {
		// If the document exists, an HTTP response code of 200 is returned, otherwise
		// 404.
		return false;
	}

	@GET
	@Path("documents")
	@Produces(MediaType.TEXT_XML)
	public DocumentCollection getDocumentsXml() {
		return null;
	}

	@GET
	@Path("documents")
	@Produces(MediaType.TEXT_HTML)
	public DocumentCollection getDocumentsHtml() {
		return null;
	}

	@GET
	@Path("search/{tags}")
	@Produces(MediaType.TEXT_HTML)
	public String search(@PathParam("tags") String tags) {
		// Example logging saved in ssm-log.html
		ssm.log(Level.INFO, "Search for " + tags + " started.");

		ArrayList<Document> docs = new ArrayList<Document>();

		try {
			// Perform distributed search
			SearchResult sr = ssm.search(tags);

			// Perform local search
			List<COMP4601Document> compDocs = Search.getInstance().query(tags);
			docs.addAll(convertCompDocs(compDocs));

			try {
				sr.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				ssm.reset();
			}

			// Take the state of the documents
			docs.addAll(sr.getDocs());

		} catch (ClassNotFoundException | IOException | SearchException e) {
			e.printStackTrace();
		}

		return documentsAsString(docs, tags);
	}

	private ArrayList<Document> convertCompDocs(List<COMP4601Document> compDocs) {
		ArrayList<Document> docs = new ArrayList<Document>();
		for (COMP4601Document d : compDocs) {
			Document doc = new Document();
			doc.setId(d.getDocid());
			doc.setUrl(d.getUrl());
			doc.setContent(d.getContent());
			doc.setScore(d.getScore());
			// doc.setName();
			docs.add(doc);
		}
		return docs;
	}

	@GET
	@Path("query/{tags}")
	@Produces(MediaType.APPLICATION_XML)
	public DocumentCollection queryAsXML(@PathParam("tags") String tags) {
		DocumentCollection dc = new DocumentCollection();

		// Perform local search
		List<COMP4601Document> compDocs = Search.getInstance().query(tags);
		dc.setDocuments(convertCompDocs(compDocs));

		// Return the XML version of the DocumentCollection
		return dc;
	}

	private String documentsAsString(ArrayList<Document> docs, String tags) {
		// Build page
		// TODO

		return "TODO: Documents as string";
	}

}