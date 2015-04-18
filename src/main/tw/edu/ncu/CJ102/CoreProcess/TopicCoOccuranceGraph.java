package tw.edu.ncu.CJ102.CoreProcess;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class TopicCoOccuranceGraph extends UndirectedSparseGraph<TopicTermGraph, CEdge> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public boolean addEdge(CEdge e, TopicTermGraph v1,TopicTermGraph v2){
		
		if(super.addEdge(e, v1, v2)){
			return true;
		}else{
			CEdge rightEdge= super.findEdge(v1, v2);
			if(rightEdge.equals(e)){
				rightEdge.coScore += 1;
				return true;
			}
			throw new RuntimeException("Problem in add Edge");
		}
		
		 
	}

}
