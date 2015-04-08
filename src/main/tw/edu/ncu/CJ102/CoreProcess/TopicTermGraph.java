package tw.edu.ncu.CJ102.CoreProcess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
/**
 * 
 * @author TingWen
 *
 */
@SuppressWarnings("serial")
public class TopicTermGraph extends UndirectedSparseGraph<TermNode,CEdge> implements Serializable{
	private int id;
	private boolean isLongTermInterest;
	protected int birthDate;
	protected int updateDate;
	int numberOfDocument;
	public TopicTermGraph(int id,int birthDay){
		this.id = id;
		this.setLongTermInterest(false);
	}

	public int getId() {
		return id;
	}
	
	/**
	 * @return the birthDate
	 */
	public int getBirthDate() {
		return birthDate;
	}

	/**
	 * @return the updateDate
	 */
	public int getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(int updateDate) {
		this.updateDate = updateDate;
	}

	public boolean isLongTermInterest() {
		return isLongTermInterest;
	}

	public void setLongTermInterest(boolean isLongTermInterest) {
		this.isLongTermInterest = isLongTermInterest;
	}
	
	public void merge(TopicTermGraph anotherGraph){
		for(TermNode term: anotherGraph.getVertices()){
			this.addVertex(term);
		}
		for(CEdge edge: anotherGraph.getEdges()){
			Pair<TermNode> pair = anotherGraph.getEndpoints(edge);
			this.addEdge(edge,pair.getFirst(),pair.getSecond());//Need to use this override method
		}
		if(this.updateDate<anotherGraph.updateDate){
			this.updateDate = anotherGraph.updateDate;
		}
		
	}
	//Know condition:add a edge that v1 and v2 is not already add will cause v1&v2 add into Edge but if node already exist it will not add the value! 
	@Override
	public boolean addEdge(CEdge e,TermNode v1,TermNode v2){
		if(super.addEdge(e, v1, v2)){
			return true;
		}else{
			Pair<TermNode> pair = this.getEndpoints(e);
			CEdge rightEdge = this.findEdge(pair.getFirst(), pair.getSecond());
			if(rightEdge!=null){//edge exist
				e.distance += 1;
				return false;
			}else{//edge not exist
				throw new RuntimeException("problem in add Edge");
			}
		}
	}
	/**
	 * add vertex into topic
	 * @return true mean it add new term into it, false mean the term already in topic so add it up
	 * @throws runtimeException if it can not find vertex nor can it add to graph
	 */
	@Override
	public boolean addVertex(TermNode terms){
		if(super.addVertex(terms)){
			return true;
		}else{
			for(TermNode t:this.vertices.keySet()){
				if(t.equals(terms)){
					t.termFreq += terms.termFreq;
					return false;
				}
				
			}
			throw new RuntimeException("term can not find vertex nor can it add to graph");
		}
	}

	public Set<TermNode> getCoreTerm(){ //Should return all node bigger then K? or only k biggest core?
		Set<TermNode> core = new HashSet<>();
		//HashMap<TermNode,Double> termRank = new HashMap<TermNode,Double>();
		for(TermNode term:this.getVertices()){
			int degree = this.getNeighborCount(term);
			if(degree>=3){
				core.add(term);
			}
		}
		
		return core;
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
