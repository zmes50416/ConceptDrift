package tw.edu.ncu.CJ102.algorithm.impl;

import org.apache.commons.collections15.Transformer;

import tw.edu.ncu.CJ102.algorithm.CentralityAlgorithm;
import tw.edu.ncu.CJ102.algorithm.LinkPrediction;
import edu.uci.ics.jung.graph.Graph;

/**
 * implement Common Neighbor algorithm
 * @author TingWen
 *
 * @param <V> Node
 * @param <E> Edge
 */
public class CN<V,E> implements LinkPrediction<V, E>, CentralityAlgorithm<V,E> {
	
	Graph<V,E> graph;
	/**
	 * input the graph to compute
	 * @param g : graph you want to compute
	 */
	public CN(Graph<V,E> g){
		this.graph = g;
		
	}
	//Should I compute every Node score at once? or compute as predict demand?
	
	@Override
	public double predict(V startNode, V goalNode) {
		double index = 0;
		
		for(V n:this.graph.getNeighbors(startNode)){
			for(V n2: this.graph.getNeighbors(goalNode)){
				if(n.equals(n2)){
					index++;
				}
			}
		}
		
		return index;
	}

	@Override
	public double computeCentrality(V term) {
		return this.computeCentrality(term, new Transformer<E,Double>(){

			@Override
			public Double transform(E input) {
				return 1.0;
			}
			
		});
		
	}
	@Override
	public double computeCentrality(V term,
			Transformer<E, Double> edgeDistanceTransformer) {
		double score = 0;
		for(V neighborOfTerm: this.graph.getNeighbors(term)){
			E e = this.graph.findEdge(term, neighborOfTerm);
			double edgeWeight = edgeDistanceTransformer.transform(e);
			score += edgeWeight;
		}
		return score;
	}
	
	
	
	

}
