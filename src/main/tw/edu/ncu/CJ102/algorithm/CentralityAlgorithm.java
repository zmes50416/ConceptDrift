package tw.edu.ncu.CJ102.algorithm;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

/**
 * Compute the centrality of node
 * @author TingWen
 *
 */
public interface CentralityAlgorithm<V,E> {
	public double computeCentrality(V term);
	public double computeCentrality(V term,Transformer<E, Double> edgeWeightTransformer);//TODO should not use this way to determine transformer but in the conscutor
}
