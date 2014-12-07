package tw.edu.ncu.CJ102.algorithm;

import edu.uci.ics.jung.graph.Graph;

/**
 * implement Common Neighbor algorithm
 * @author TingWen
 *
 * @param <V> Node
 * @param <E> Edge
 */
public class CN<V,E> implements LinkPrediction<V, E> {
	
	Graph<V,E> graph;
	CN(Graph<V,E> g){
		this.graph = g;
	}
	
	@Override
	public double predict(V target, V goal) {
		this.graph.containsVertex(target);
		this.graph.containsVertex(goal);
		return 0;
	}

}
