package tw.edu.ncu.CJ102.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;

import tw.edu.ncu.CJ102.algorithm.CentralityAlgorithm;
import tw.edu.ncu.CJ102.algorithm.LinkPrediction;
import tw.edu.ncu.CJ102.algorithm.impl.CN;
import tw.edu.ncu.CJ102.algorithm.impl.LP;
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
	public static int MAXCORESIZE = 10; //Default Core size
	CentralityAlgorithm<TermNode,CEdge> algo = new LP<TermNode,CEdge>(this); //Default Core algorithm
	private boolean isLongTermInterest;
	double averageTermTf;
	private int birthDate;
	private int updateDate;
	protected double decayRate;
	int numberOfDocument;
	public TopicTermGraph(int birthDay){
		this.setLongTermInterest(false);
		numberOfDocument = 1;
		this.birthDate = birthDay;
		this.updateDate = birthDay;
		this.decayRate = 1;
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

	/**
	 * @return the decayRate
	 */
	public double getDecayRate() {
		return decayRate;
	}

	/**
	 * @param decayRate the decayRate to set
	 */
	public void setDecayRate(double decayRate) {
		this.decayRate = decayRate;
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
			CEdge rightEdge = this.findEdge(v1, v2);
			if(rightEdge!=null){//edge exist
				e.coScore += 1;
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

	public Collection<TermNode> getCoreTerm(){ //Should return all node bigger then K? or only k biggest core?
		HashMap<TermNode,Double> scoreSheet = new HashMap<>();
		PriorityQueue<TermNode> core = new PriorityQueue<>(MAXCORESIZE, new nodeComparator());
		for(TermNode term:this.getVertices()){
			scoreSheet.put(term, algo.computeCentrality(term));
			if(core.size()==MAXCORESIZE){
				TermNode minTerm = core.poll();
				if(scoreSheet.get(minTerm)>=scoreSheet.get(term)){
					core.offer(minTerm);
					continue;
				}
			}
			core.offer(term);
		
		}
		return core;
	}
	class nodeComparator implements Comparator<TermNode>{

		@Override
		public int compare(TermNode arg0, TermNode arg1) {
			int degree0 = getNeighborCount(arg0);
			int degree1 = getNeighborCount(arg1);
			return degree0-degree1;
		}
		
	}
	
	@Override
	public String toString(){
		return Integer.toHexString(hashCode());
	}
	
	
	
}
