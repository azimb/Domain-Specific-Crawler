package edu.carleton.comp4601.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.alg.scoring.PageRank;

import edu.carleton.comp4601.dao.COMP4601MongoDB;
import edu.carleton.comp4601.dao.COMP4601Store;
import edu.carleton.comp4601.dao.GraphMongoDB;
import edu.carleton.comp4601.dao.GraphStore;
import edu.carleton.comp4601.graph.CrawlerGraph;
import edu.carleton.comp4601.graph.PageVertex;
import edu.carleton.comp4601.model.COMP4601Document;

public class Search {

	private static final String INDEX_DIR = "./indexes";
	private static final String DOC_ID = "docId";
	private static final String MODIFIED = "modified";
	private static final String CONTENTS = "content";
	private static final String TYPE = "type";
	private static final String INDEXED_BY = "i";
	private static final String INDEXER = "Olivia";
	private static final String URL = "url";
	private static final String FEATURE = "features";
	private static final String PAGERANK = "pagerank";
	COMP4601Store docStore;
	static Search instance;

	Map<Integer, Double> pagerank;

	public Search() {
		docStore = COMP4601MongoDB.getInstance();
		pagerank = getPageRank();
	}

	public static Search getInstance() {
		if (instance == null)
			instance = new Search();
		return instance;
	}

	public static void main(String[] args) {
		// Search s = new Search();
		// s.indexMongoDocuments();
		// System.out.println(s.query("lucene"));
	}

	public void indexMongoDocuments() {
		IndexWriter writer = null;
		Directory dir = null;
		try {
			dir = FSDirectory.open(new File(INDEX_DIR).toPath());
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);

			indexDocuments(writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (dir != null) {
					dir.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void indexDocuments(IndexWriter writer) {
		List<COMP4601Document> docs = docStore.findAll();
		for (COMP4601Document d : docs) {
			indexADocument(d, writer);
		}
	}

	private void indexADocument(COMP4601Document d, IndexWriter writer) {
		Document doc = new Document();
		doc.add(new StoredField(DOC_ID, d.getDocid())); // IntField no longer exists
		doc.add(new TextField(URL, d.getUrl(), Field.Store.YES));
		doc.add(new TextField(MODIFIED, d.getDate().toString(), Field.Store.YES));
		String contentsAndMetadata = d.getContent() + "\n" + d.getMetadata().toString();
		doc.add(new TextField(CONTENTS, contentsAndMetadata, Field.Store.YES));
		doc.add(new TextField(INDEXED_BY, INDEXER, Field.Store.YES));
		doc.add(new TextField(TYPE, d.getType(), Field.Store.YES));
		doc.add(new FeatureField(FEATURE, PAGERANK, pagerank.get(d.getDocid()).floatValue()));
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<COMP4601Document> query(String searchString) {
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIR).toPath()));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser(CONTENTS, analyzer);
			Query q = parser.parse(searchString);
			Query boost = FeatureField.newSaturationQuery(FEATURE, PAGERANK);
			Query boostedQuery = new BooleanQuery.Builder().add(q, Occur.MUST).add(boost, Occur.SHOULD).build();
			TopDocs results = searcher.search(boostedQuery, 100); // 100 documents!
			ScoreDoc[] hits = results.scoreDocs;
			ArrayList<COMP4601Document> docs = getDocs(hits, searcher);
			reader.close();
			return docs;
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return new ArrayList<COMP4601Document>();
	}

	private ArrayList<COMP4601Document> getDocs(ScoreDoc[] hits, IndexSearcher searcher) {
		ArrayList<COMP4601Document> docs = new ArrayList<COMP4601Document>();

		try {
			for (ScoreDoc hit : hits) {
				Document indexDoc = searcher.doc(hit.doc);
				String id = indexDoc.get(DOC_ID);
				if (id != null) {
					COMP4601Document d = docStore.find(Integer.valueOf(id));
					if (d != null) {
						d.setScore(hit.score); // Used in display to user
						docs.add(d);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docs;
	}

	private Map<Integer, Double> getPageRank() {
		Map<Integer, Double> newMap = new HashMap<Integer, Double>();

		GraphStore graphs = GraphMongoDB.getInstance();
		CrawlerGraph g = graphs.findOne();
		PageRank pr = new PageRank(g);
		for (Object o : pr.getScores().entrySet()) {
			Map.Entry<PageVertex, Double> e = (Map.Entry<PageVertex, Double>) o;
			newMap.put(e.getKey().getDoc().getDocid(), e.getValue());
		}
		return newMap;
	}

}
