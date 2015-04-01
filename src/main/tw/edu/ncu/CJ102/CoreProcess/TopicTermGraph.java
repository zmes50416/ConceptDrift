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
public class TopicTermGraph extends UndirectedSparseGraph<TermNode,CEdge>{
	private int id;
	private HashSet<TermNode> coreTerm = new HashSet<>();
	public TopicTermGraph(int id){
		this.id = id;
	}
	
	public HashSet<TermNode> getCoreTerm() {
		return coreTerm;
	}

	public int getId() {
		return id;
	}
	
	public void computeCoreTerm(){
		//TODO implement core term finding algorithm
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
