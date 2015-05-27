package tw.edu.ncu.CJ102.algorithm.impl;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import tw.edu.ncu.CJ102.algorithm.CentralityAlgorithm;

public class BetweennessCentralityWrapper<V,E> implements CentralityAlgorithm<V, E> {
	Graph<V,E> mGraph;
	BetweennessCentrality<V,E> scorer;
	public BetweennessCentralityWrapper(Graph<V,E> graph) {
		this.mGraph = graph;
		
	}

	@Override
	public double computeCentrality(V term) {
		return this.computeCentrality(term, new Transformer<E,Double>(){

			@Override
			public Double transform(E arg0) {
				return 1.0D;
			}
			
		});
	}

	@Override
	public double computeCentrality(V term,
			Transformer<E, Double> edgeWeightTransformer) {
		if(scorer==null){
			scorer = new BetweennessCentrality<>(this.mGraph,edgeWeightTransformer);
		}
		
		return this.scorer.getVertexScore(term);

	}

}
