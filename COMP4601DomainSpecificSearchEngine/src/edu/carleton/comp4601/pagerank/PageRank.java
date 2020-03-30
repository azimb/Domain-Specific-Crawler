package edu.carleton.comp4601.pagerank;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import Jama.Matrix;

public class PageRank {
	private static DecimalFormat df = new DecimalFormat("#.##");

	public static void main(String[] args) {
		PageRank p = new PageRank();
		p.getPageRank();

	}
	
	private Graph<Integer, DefaultEdge> make3Graph() {
		Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);

		g.addEdge(1, 0);
		g.addEdge(0, 1);
		g.addEdge(2, 1);
		g.addEdge(1, 2);
		
		return g;
	}
	
	private Graph<Integer, DefaultEdge> make7Graph() {
		Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		
		g.addEdge(0,2);
		g.addEdge(1,1);
		g.addEdge(1,2);
		g.addEdge(2,0);
		g.addEdge(2,2);
		g.addEdge(2,3);
		g.addEdge(3,3);
		g.addEdge(3,4);
		g.addEdge(4,6);
		g.addEdge(5,5);
		g.addEdge(5,6);
		g.addEdge(6,6);
		g.addEdge(6,3);
		g.addEdge(6,4);
		
		return g;
	}
	
	public Matrix makeTransitionMatrix() {
		double[][] p = {{0.02, 0.02, 0.88, 0.02, 0.02, 0.02, 0.02},
				{0.02, 0.45, 0.45, 0.02, 0.02, 0.02, 0.02},
				{0.31, 0.02, 0.31, 0.31, 0.02, 0.02, 0.02},
				{0.02, 0.02, 0.02, 0.45, 0.45, 0.02, 0.02},
				{0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.88},
				{0.02, 0.02, 0.02, 0.02, 0.02, 0.45, 0.45},
				{0.02, 0.02, 0.02, 0.31, 0.31, 0.02, 0.31}};
		return new Matrix(p);
	}
	
	public List<Float> getPageRank(){
		ArrayList<Float> list = new ArrayList<Float>();
		Graph<Integer, DefaultEdge> g = make7Graph();
				
		int n = g.vertexSet().size();
				
		Matrix m = new Matrix(n,n);
		for (int i = 0; i < n; i ++) {
			int num_edges = g.outDegreeOf(i);
			for (int j = 0; j < n; j ++) {
				if(num_edges == 0) {
					m.set(i, j, 1f/n);
				} else if(g.containsEdge(i, j)) {
					m.set(i, j, 1f/num_edges);
				} else {
					m.set(i, j, 0);
				}
			}
		}
		printMatrix(m);
		
		System.out.println();
		
		double alpha = 0.14;
	
		m = m.times(1f-alpha);
		
		printMatrix(m);
				
		for (int i = 0; i < n; i ++) {
			for (int j = 0; j < n; j ++) {
				m.set(i, j, m.get(i, j)+(alpha/n));
			}
		}

		printMatrix(m);
		
		// m = makeTransitionMatrix();

		double[] d = {1d,0d,0d,0d,0d,0d,0d};
		Matrix start = new Matrix(d,1);
		printMatrix(start);
		
				
		for(int i = 0; i < 100; i ++) {
			start = start.times(m);
			start = start.times(1.0/start.normInf());
		}
		
		printMatrix(start);
		
		return list;
	}

	private void printMatrix(Matrix m) {
		int n = m.getRowDimension();
		int n2 = m.getColumnDimension();
		for (int i = 0; i < n; i ++) {
			for (int j = 0; j < n2; j ++) {
				System.out.print(df.format(m.get(i, j)) + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
	}
}
