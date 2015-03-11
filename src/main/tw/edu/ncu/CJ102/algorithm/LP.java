package tw.edu.ncu.CJ102.algorithm;

import java.util.Collection;
import java.util.HashSet;

import edu.uci.ics.jung.graph.Graph;
/**
 * 
 * @author tingWen
 *
 * @param <V>
 * @param <E>
 */
public class LP<V, E> implements LinkPrediction<V, E> {
	// According to Paper, you don't need to find the optimized number. just give the EPSILON weight very small number.
	static final double EPSILON = 0.01; 
	
	Graph<V,E> graph;
	
	public LP(Graph<V,E> g){
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

}
