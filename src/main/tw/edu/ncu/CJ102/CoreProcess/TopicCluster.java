package tw.edu.ncu.CJ102.CoreProcess;

import java.util.HashSet;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
/**
 * 
 * @author TingWen
 *
 */
class TopicCluster{
	static HashSet<TopicCluster> clusters;
	private int id;

	Graph<String,Double> graph = new UndirectedSparseGraph<String,Double>();
	public TopicCluster(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void addNode(String node){
		this.graph.addVertex(node);
	}
	
	public void addEdge(double distance,String nodeA,String nodeB){
		this.graph.addEdge(distance, nodeA, nodeB);
	}
	
	@Override
	public boolean equals(Object o){
		if(o.getClass()==TopicCluster.class){
			TopicCluster c = (TopicCluster)o;
			if(this.id == c.id){
				return true;
			}else{
				return false;
			}
		}else{
			throw new ClassCastException();
		}
		
	}
	@Override
	public int hashCode(){
		return id;
		
	}
	
}
