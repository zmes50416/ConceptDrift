package tw.edu.ncu.CJ102.CoreProcess;

import java.util.HashSet;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
/**
 * 
 * @author TingWen
 *
 */
@SuppressWarnings("serial")
class TopicTermGraph extends UndirectedSparseGraph<TermNode,CEdge>{
	private int id;

	public TopicTermGraph(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object o){
		if(o.getClass()==TopicTermGraph.class){
			TopicTermGraph c = (TopicTermGraph)o;
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
