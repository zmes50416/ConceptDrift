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
	private int id;

	Graph<TermNode,CEdge> graph = new UndirectedSparseGraph<TermNode,CEdge>();
	public TopicCluster(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void addNode(TermNode node){
		this.graph.addVertex(node);
	}

	
	public void addEdge(CEdge c,TermNode nodeA,TermNode nodeB){
		this.graph.addEdge(c, nodeA, nodeB);
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

class TermNode{
	double termFreq;
	String term;
	TermNode(String _term){
		this.term = _term;
	}
	TermNode(String _term,double _termFreq){
		this.term = _term;
		this.termFreq = _termFreq;
	}
	@Override
	public String toString(){
		return term;
		
	}
	@Override
	public boolean equals(Object o){
		TermNode anotherNode = (TermNode)o;
		if(this.term.equals(anotherNode.term)){
			return true;
		}
		return false;
		
	}
	
	
}
