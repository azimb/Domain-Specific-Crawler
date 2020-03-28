package edu.carleton.comp4601.graph;

import java.awt.Dimension;
import javax.swing.JApplet;
import javax.swing.JFrame;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;

/*
 * A public utility class to visualize the graphs that
 * are generated as a result of a crawl. Thanks to the efforts
 * of COMP 4601 2017 class students.
 * 
 * Works for all of the JGrapht abstract graph classes; e.g.,
 * DefaultDirectedGraph 
 */

public class GraphLayoutVisualizer extends JApplet {

	public static String TITLE = "COMP 4601 Graph Visualization";
	private static JGraphXAdapter<PageVertex, DefaultEdge> jgxAdapter;
	private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

	public static <V, K> void visualizeGraph(AbstractGraph<K, V> g) {
		jgxAdapter = new JGraphXAdapter<PageVertex, DefaultEdge>((ListenableGraph<PageVertex, DefaultEdge>) g);

		GraphLayoutVisualizer applet = new GraphLayoutVisualizer();
		applet.init();

		JFrame frame = new JFrame();
		frame.getContentPane().add(applet);
		frame.setTitle("JGraphT Adapter to JGraphX Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

	@Override
	public void init() {
		super.init();

		// create a visualization using JGraph, via an adapter

		setPreferredSize(DEFAULT_SIZE);
		mxGraphComponent component = new mxGraphComponent(jgxAdapter);
		component.setConnectable(false);
		component.getGraph().setAllowDanglingEdges(false);
		getContentPane().add(component);
		resize(DEFAULT_SIZE);

		// positioning via jgraphx layouts
		mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);

		layout.execute(jgxAdapter.getDefaultParent());
	}

}
