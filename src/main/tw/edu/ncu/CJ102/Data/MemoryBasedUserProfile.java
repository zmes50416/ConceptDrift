package tw.edu.ncu.CJ102.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * 基於長期以及短期記憶區塊的使用者模型，
 * @author TingWen
 *
 */
public class MemoryBasedUserProfile extends AbstractUserProfile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MemoryBasedUserProfile() {
		this.setRemove_rate(0.1);
	}
	public MemoryBasedUserProfile(double _removeRate){
		this.setRemove_rate(_removeRate);
	}
	
	/**
	 * @return the sizeOfShortTerm
	 */
	public int getSizeOfShortTerm() {
		int sizeOfShortTerm = 0;
		for(TopicTermGraph topic : userTopics){
			if(!topic.isLongTermInterest()){
				sizeOfShortTerm++;
			}
		}
		return sizeOfShortTerm;
	}

	@Override
	public double getDecayRate(TopicTermGraph topic,int updateDate) {
		double decayRate;
		if(topic.isLongTermInterest()){
			decayRate = Math.pow(Math.E, -Math.log10(updateDate-topic.getUpdateDate())*0.02);
		}else{
			decayRate = Math.pow(Math.E, -Math.log10(updateDate-topic.getUpdateDate())*this.getSizeOfShortTerm()/Math.log10(topic.numberOfDocument));
		}
		return decayRate;
	}

	@Override
	public double getCoOccranceThreshold() {
		// TODO implement complext threshold compute
		return 0.1;
	}
	
	@Override
	public void addDocument(Map<TopicTermGraph,TopicTermGraph> topicMap) {
		if(topicMap.isEmpty()){//document should have something to add
			throw new IllegalArgumentException("document topic should not be empty!");
		}
		
		HashSet<TopicTermGraph> documentTopics = new HashSet<>();//use for CoOccurance topic in document
		double documentTf = 0;
		for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
			TopicTermGraph topic = topicPair.getKey();
			TopicTermGraph mappedTopic = topicPair.getValue();
			if(this.userTopics.contains(mappedTopic)){
				mappedTopic.merge(topic);
				mappedTopic.numberOfDocument++;
			}else{
				if(!this.userTopics.add(topic)){
					throw new RuntimeException("Cant add topic");
				}
			}
			documentTopics.add(mappedTopic);
			for(TermNode term:topic.getVertices()){//Record tf of new Term & document
				this.updateTermRemoveThreshold(term.termFreq);
				documentTf += term.termFreq;
			}
			
		}//end of CoOccurance Topics
		
		for(TopicTermGraph userTopic:topicMap.values()){//For topic coOccurance graph
			for(TopicTermGraph anotherUserTopic:documentTopics){
				if(userTopic!=anotherUserTopic){
					this.topicCOGraph.addEdge(new CEdge<TopicTermGraph>(new Pair<TopicTermGraph>(userTopic, anotherUserTopic)), userTopic, anotherUserTopic);
				}
			}
			documentTopics.remove(userTopic);
			
		}
		
		this.updateTopicRemoveThreshold(documentTf);
	}

	protected void updateTopicRemoveThreshold(double newDocumentTf) {
		this.topicRemoveThreshold = newDocumentTf + (this.topicRemoveThreshold/2);

	}

	protected void updateTermRemoveThreshold(double newTermTf) {
		this.termRemoveThreshold = newTermTf + (this.termRemoveThreshold/2);
	}


}
