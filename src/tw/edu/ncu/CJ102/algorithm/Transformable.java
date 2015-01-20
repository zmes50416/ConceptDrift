package tw.edu.ncu.CJ102.algorithm;

import java.util.Set;
import edu.uci.ics.jung.graph.Graph;

/**
 * Any algorithm that use weight in vertex should implement this
 * @author TingWen
 *
 */
public interface Transformable<V,E> {
	//TODO I havn't really start to design in this interface
	public Set<Set<V>> transform(Graph<V,E> graph);
}
