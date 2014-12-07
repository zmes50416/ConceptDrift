package tw.edu.ncu.CJ102.algorithm;

import java.util.Set;
import edu.uci.ics.jung.graph.Graph;

/**
 * Use for ForeCasting algorithm
 * all algorithm class should implement its, in order for Forecasting class to use
 * V : Vetric
 * E : Edge
 * @author TingWen
 *
 */
public interface LinkPrediction<V,E> {
	
	/**
	 * predict this two node should be link or not
	 * @param target : targetNode
	 * @param goal : goalNode
	 * @return number between 0.0 to 1.0 , bigger the number, bigger the chance 
	 */
	public double predict(V target, V goal);
	
}