package tw.edu.ncu.CJ102.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.buffer.CircularFifoBuffer;

import com.google.common.collect.Lists;

import tw.edu.ncu.CJ102.algorithm.CentralityAlgorithm;
import tw.edu.ncu.CJ102.algorithm.LinkPrediction;
import tw.edu.ncu.CJ102.algorithm.impl.BetweennessCentralityWrapper;
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
	CentralityAlgorithm<TermNode,CEdge> centralityAlgorithm = new LP<TermNode,CEdge>(this); //Default Core algorithm
	public static int METHODTYPE = 0;
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
	public double getValue(){
		double value = 0;
		for(TermNode term:this.getVertices()){
			value += term.termFreq;
		}
		
		return value;
	}
	public void setBirthDate(int date){
		this.birthDate = date;
	}
	/**
	 * @return the birthDate
	 */
	public int getBirthDate() {
		return birthDate;
	}
	/**
	 * get the f(Ci) value
	 */
	public double getStrength(){
		double strength = 0;
		for(TermNode term :this.getVertices()){
			strength += term.termFreq;
		}
		return strength;
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
	/**
	 * @return the centralityAlgorithm
	 */
	public CentralityAlgorithm<TermNode, CEdge> getCentralityAlgorithm() {
		return centralityAlgorithm;
	}

	/**
	 * @param centralityAlgorithm the centralityAlgorithm to set
	 */
	public void setCentralityAlgorithm(CentralityAlgorithm<TermNode, CEdge> algo) {
		this.centralityAlgorithm = algo;
	}
	/**
	 * return the graph core term by Centrality algorithm
	 * @return
	 */
	public Collection<TermNode> getCoreTerm(){ 
		if(METHODTYPE==0){
			centralityAlgorithm = new CN<>(this);
		}else if(METHODTYPE ==1){
			centralityAlgorithm = new LP<>(this);
		}else if(METHODTYPE ==2){
			centralityAlgorithm = new BetweennessCentralityWrapper<TermNode,CEdge>(this);

		}
		HashMap<TermNode,Double> scoreSheet = new HashMap<>();
		for(TermNode term:this.getVertices()){
			scoreSheet.put(term, centralityAlgorithm.computeCentrality(term,new Transformer<CEdge,Double>(){

				@Override
				public Double transform(CEdge input) {
					return input.coScore;
				}
				
			}));
		}
		ArrayList<Entry<TermNode,Double>> sortTerm = Lists.newArrayList(scoreSheet.entrySet());
		Collections.sort(sortTerm,new nodeComparator());
		ArrayList<TermNode> coreTerm = new ArrayList<>();
		int currentIndex = sortTerm.size()-1;
		int count = 0;
		while(count<MAXCORESIZE){
			if(currentIndex<0){
				break;
			}
			TermNode term = sortTerm.get(currentIndex).getKey();
			coreTerm.add(term);
			currentIndex--;
			count++;
		}
		return coreTerm;
	}
	class nodeComparator implements Comparator<Entry<TermNode,Double>>{

		@Override
		public int compare(Entry<TermNode,Double> arg0, Entry<TermNode,Double> arg1) {
			Double degree0 = arg0.getValue();
			Double degree1 = arg1.getValue();
			if(degree0<degree1){
				return -1;
			}else if(degree0>degree1){
				return 1;
			}else{
				return 0;
			}
		}
		
	}
	
	@Override
	public String toString(){
		return Integer.toHexString(hashCode());
	}
	
	
	
}
