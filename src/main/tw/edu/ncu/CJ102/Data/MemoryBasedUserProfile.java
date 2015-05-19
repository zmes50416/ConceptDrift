package tw.edu.ncu.CJ102.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private int longTermThreshold = 100; // Just for test
	private Logger loger = LoggerFactory.getLogger(this.getClass());
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
	public double getDecayRate(TopicTermGraph topic,int today) {
		if(topic.getUpdateDate()==today){
			return 1;
		}
		double decayRate;
		double timeFactor = Math.log10(today-topic.getUpdateDate());
		if(topic.isLongTermInterest()){
			decayRate = Math.pow(Math.E, -timeFactor*0.2);
		}else{
			double strength = Math.log10(topic.numberOfDocument);
			if(strength == 0){//when document only have 1, it will be 0
				strength = 1;//bug problme ,may need to think a better function
			}
			decayRate = Math.pow(Math.E, -timeFactor*this.getSizeOfShortTerm()/strength);
		}
		return decayRate;
	}

	@Override
	public double getCoOccranceThreshold() {
		// TODO implement complext threshold compute
		return 0.1;
	}
	
	@Override
	public void addDocument(Map<TopicTermGraph,TopicTermGraph> topicMap,int today) {
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
				mappedTopic.setUpdateDate(today);
			}else{
				if(!this.userTopics.add(topic)){
					throw new RuntimeException("Cant add topic");
				}
			}
			documentTopics.add(mappedTopic);
			double sumInterest = 0;
			if(!mappedTopic.isLongTermInterest()){
				for(TermNode term:mappedTopic.getVertices()){
					sumInterest += term.termFreq;
					if(sumInterest>=this.longTermThreshold){
						loger.info("Topic {} become long term Interest",mappedTopic);
						mappedTopic.setLongTermInterest(true);
						break;//reduce computing when long term is sured!
					}
				}
			}
			
			for(TermNode term:topic.getVertices()){//Record tf of new Term & document
				this.updateTermRemoveThreshold(term.termFreq);
				documentTf += term.termFreq;
			}
			
		}
		
		for(TopicTermGraph userTopic:topicMap.values()){//For topic coOccurance graph
			for(TopicTermGraph anotherUserTopic:documentTopics){
				if(userTopic!=anotherUserTopic){
					this.topicCOGraph.addEdge(new CEdge<TopicTermGraph>(new Pair<TopicTermGraph>(userTopic, anotherUserTopic)), userTopic, anotherUserTopic);
				}
			}
			documentTopics.remove(userTopic);
			
		}//end of CoOccurance Topics
		
		this.updateTopicRemoveThreshold(documentTf);
	}

	protected void updateTopicRemoveThreshold(double newDocumentTf) {
		this.topicRemoveThreshold = newDocumentTf + (this.topicRemoveThreshold/2);

	}

	protected void updateTermRemoveThreshold(double newTermTf) {
		this.termRemoveThreshold = newTermTf + (this.termRemoveThreshold/2);
	}


}
