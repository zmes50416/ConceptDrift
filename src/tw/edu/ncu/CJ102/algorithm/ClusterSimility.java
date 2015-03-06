package tw.edu.ncu.CJ102.algorithm;

import java.util.Collection;
import java.util.Map;

/**
 * the algorithm between two cluster,E shuold be the cluster's data type itself
 * @author TingWen
 *
 */
public interface ClusterSimility<E,M> {
	
	public double simility(Map<E,M> cluster,Map<E,M> anotherCluster);

}
