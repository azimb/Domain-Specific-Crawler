package edu.carleton.comp4601.graph;

import java.io.Serializable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;

public class CrawlerGraph extends DefaultListenableGraph<PageVertex, DefaultEdge> implements Serializable {

	private static final long serialVersionUID = 1L;

	public CrawlerGraph(Graph<PageVertex, DefaultEdge> g) {
		super(g);
	}

	@Override
	public boolean addVertex(PageVertex v) {
		if (!super.addVertex(v))
			return false;
		// automatically add edge to parent
		for (PageVertex existing : this.vertexSet()) {
			if (v.getDoc().getParent() != null && v.getDoc().getParent().equals(existing.getDoc().getUrl())) {
				super.addEdge(existing, v);
			}
			if (existing.getDoc().getParent() != null && existing.getDoc().getParent().equals(v.getDoc().getUrl())) {
				super.addEdge(v, existing);
			}
		}
		return true;
	}

}
