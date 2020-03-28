package edu.carleton.comp4601.hits;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import Jama.Matrix;
import edu.carleton.comp4601.dao.GraphMongoDB;
import edu.carleton.comp4601.dao.GraphStore;
import edu.carleton.comp4601.graph.CrawlerGraph;
import edu.carleton.comp4601.graph.PageVertex;

public class Hits {
	private static DecimalFormat df = new DecimalFormat("#.##");
	GraphStore graphstore;

	public static void main(String[] args) {

		Hits h = new Hits();
		// h.getHits("eclipse");
		h.getHits7("jaguar");

	}

	private Graph<Integer, RelationshipEdge> make7Graph() {
		Graph<Integer, RelationshipEdge> g = new DefaultDirectedGraph<>(RelationshipEdge.class);

		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);

		g.addEdge(0, 2, new RelationshipEdge("car"));
		g.addEdge(1, 1, new RelationshipEdge("benz"));
		g.addEdge(1, 2, new RelationshipEdge("ford"));
		g.addEdge(2, 0, new RelationshipEdge("gm"));
		g.addEdge(2, 2, new RelationshipEdge("honda"));
		g.addEdge(2, 3, new RelationshipEdge("jaguar"));
		g.addEdge(3, 3, new RelationshipEdge("jag"));
		g.addEdge(3, 4, new RelationshipEdge("speed"));
		g.addEdge(4, 6, new RelationshipEdge("cat"));
		g.addEdge(5, 5, new RelationshipEdge("leopard"));
		g.addEdge(5, 6, new RelationshipEdge("tiger"));
		g.addEdge(6, 6, new RelationshipEdge("cheetah"));
		g.addEdge(6, 3, new RelationshipEdge("jaguar"));
		g.addEdge(6, 4, new RelationshipEdge("lion"));

		return g;
	}

	private void getHits7(String query) {
		int n = 7;
		Graph<Integer, RelationshipEdge> g = make7Graph();

		String anchor;
		Matrix m = new Matrix(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (g.containsEdge(i, j)) {
					anchor = (String) g.getEdge(i, j).getLabel();
					if (anchor != null && anchor.contains(query)) {
						m.set(i, j, 2.);
					} else {
						m.set(i, j, 1.);
					}
				} else {
					m.set(i, j, 0.);
				}
			}
		}

		printMatrix(m);

		Matrix h = new Matrix(1, n);
		for (int i = 0; i < n; i++) {
			h.set(0, i, 0);
		}
		h.set(0, 0, 1);

		Matrix aat = (m.times(m.transpose()));

		System.out.println("h");
		printMatrix(h);

		System.out.println("AAT");
		printMatrix(aat);

		for (int i = 0; i < 100; i++) {
			h = h.times(aat);
			h = h.times(1 / h.normInf());
		}

		System.out.println("H final");
		printMatrix(h);

		Matrix a = new Matrix(1, n);
		for (int i = 0; i < n; i++) {
			a.set(0, i, 0);
		}
		a.set(0, 0, 1);

		for (int i = 0; i < 100; i++) {
			a = a.times((m.transpose().times(m)));
			a = a.times(1 / a.normInf());

		}

		System.out.println("A final");
		printMatrix(a);

	}

	private void getHits(String query) {
		graphstore = GraphMongoDB.getInstance();
		CrawlerGraph g = graphstore.findOne();
		Set<PageVertex> pages = g.vertexSet();
		HashMap<Integer, PageVertex> numberedPages = new HashMap<Integer, PageVertex>();
		for (PageVertex p : pages) {
			numberedPages.put(p.getDoc().getDocid(), p);
		}

		int n = 100;
		String anchor;

		Matrix m = new Matrix(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (g.containsEdge(numberedPages.get(i), numberedPages.get(j))) {
					anchor = (String) numberedPages.get(j).getDoc().getMetadata().get("anchor");
					if (anchor != null && anchor.contains(query)) {
						m.set(i, j, 2.);
					} else {
						m.set(i, j, 1.);
					}
				} else {
					m.set(i, j, 0.);
				}
			}
		}

		printMatrix(m);

		Matrix h = new Matrix(1, n);
		for (int i = 0; i < n; i++) {
			h.set(0, i, 0);
		}
		h.set(0, 1, 1);

		Matrix aat = (m.times(m.transpose()));

		System.out.println("h");
		printMatrix(h);

		System.out.println("AAT");
		printMatrix(aat);

		for (int i = 0; i < 100; i++) {
			h = h.times(aat);
			h = h.times(1 / h.normInf());
		}

		System.out.println("H final");
		printMatrix(h);

		Matrix a = new Matrix(1, n);
		for (int i = 0; i < n; i++) {
			a.set(0, i, 0);
		}
		a.set(0, 1, 1);

		for (int i = 0; i < 100; i++) {
			a = a.times((m.transpose().times(m)));
			a = a.times(1 / a.normInf());

		}

		System.out.println("A final");
		printMatrix(a);

	}

	private void printMatrix(Matrix m) {
		int n = m.getRowDimension();
		int n2 = m.getColumnDimension();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n2; j++) {
				System.out.print(df.format(m.get(i, j)) + "\t");
			}
			System.out.println();
		}

		System.out.println();
	}

}

class RelationshipEdge extends DefaultEdge {
	private String label;

	/**
	 * Constructs a relationship edge
	 *
	 * @param label the label of the new edge.
	 * 
	 */
	public RelationshipEdge(String label) {
		this.label = label;
	}

	/**
	 * Gets the label associated with this edge.
	 *
	 * @return edge label
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
	}

}
