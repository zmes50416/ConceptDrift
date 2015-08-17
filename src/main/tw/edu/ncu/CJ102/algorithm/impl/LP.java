package tw.edu.ncu.CJ102.algorithm.impl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections15.Transformer;

import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.algorithm.CentralityAlgorithm;
import tw.edu.ncu.CJ102.algorithm.LinkPrediction;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
/**
 * Local Path method for linkPrediction and centrality computing
 * @author tingWen
 *
 * @param <V>
 * @param <E>
 */
public class LP<V, E> implements LinkPrediction<V, E>, CentralityAlgorithm<V,E> {
	// According to Paper, you don't need to find the optimized number. just give the EPSILON weight very small number.
	static final double EPSILON = 0.01; 
	
	UndirectedGraph<V,E> graph;
	
	public LP(UndirectedGraph<V,E> g){
		this.graph = g;
	}

	@Override
	public double predict(V startNode, V goalNode) {
		double A2 = 0;
		double A3 = 0;
		
		//n(Adjacency length)
		for (V n : this.graph.getNeighbors(startNode)) {
			for (V n2 : this.graph.getNeighbors(n)) {
				if (goalNode.equals(n2)) {
					A2++;
				} else {
					for (V n3 : this.graph.getNeighbors(n2)) {
						if (goalNode.equals(n3)) {
							A3 = A3 + 1 * EPSILON;
						}
					}
				}

			}
		}
		
		return A2+A3;
	}
	/**
	 * Predict local path with customizable n length path
	 * @param startNode 
	 * @param goalNode
	 * @param times : n length
	 * @return LPindex of n
	 */
	public double predict(V startNode, V goalNode, int times){
		double index = 0;
		HashSet<V> nodes = new HashSet<V>(this.graph.getNeighbors(startNode));

		for(int i = 2;i<=times;i++){
			HashSet<V> temp = new HashSet<>();
			for(V n : nodes){
				for(V neighbor: this.graph.getNeighbors(n)){
				if(goalNode.equals(neighbor)){
					index=index+1 * Math.pow(EPSILON, i-1);
				}
					temp.add(neighbor);
				}
			}
			nodes = new HashSet<>(temp);
			
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
			Transformer<E, Double> edgeWeightTransformer) {
		double score = 0;
		for(V neighbor:graph.getNeighbors(term)){
			double A2 = 0;
			double A3 = 0;
			E e = this.graph.findEdge(term, neighbor);
			A2 += edgeWeightTransformer.transform(e); 
			for(V neighborOfNeigbor:graph.getNeighbors(neighbor)){
				E e2 = this.graph.findEdge(neighbor, neighborOfNeigbor);
				A3 += edgeWeightTransformer.transform(e2);
			}
			score +=  A2+(A3*0.01);
		}
		return score;
	}

}
