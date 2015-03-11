package tw.edu.ncu.CJ102.algorithm;

import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class TriangleDistance<V,E> implements LinkPrediction<V, E>,Transformable<V, E> {
	// TODO stub class, not Yet implement
	@Override
	public double predict(V startNode, V goalNode){
		
		return 0.0;
	}

	@Override
	public Set<Set<V>> transform(Graph<V, E> graph) {
		return null;
	}

}
