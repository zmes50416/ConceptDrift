package tw.edu.ncu.CJ102.Data;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
	public MemoryBasedUserProfile() {
		this.setRemoveRate(0.1);
	}
	public MemoryBasedUserProfile(double _removeRate){
		this.setRemoveRate(_removeRate);
	}
	
	/**
	 * @return the sizeOfShortTerm
	 */
	private int getSizeOfShortTerm() {
		int sizeOfShortTerm = 0;
		for(TopicTermGraph topic : userTopics){
			if(!topic.isLongTermInterest()){
				sizeOfShortTerm++;
			}
		}
		return sizeOfShortTerm;
	}

	@Override
	public double updateDecayRate(TopicTermGraph topic,int today) {
		double decayRate;
		int timeFactor = today-topic.getUpdateDate();
		if(topic.isLongTermInterest()){
			decayRate = Math.pow(Math.E, -((timeFactor)*(Math.log(2)/30.0)));
		}else{
			double strength = topic.numberOfDocument;
			decayRate = Math.pow(Math.E, -timeFactor/strength);
		}
		return decayRate;
	}
	
	@Override
	public void addDocument(Map<TopicTermGraph,TopicTermGraph> topicMap,int today) {
		if(topicMap.isEmpty()){//document should have something to add
			throw new IllegalArgumentException("document topic should not be empty!");
		}
		
		HashSet<TopicTermGraph> documentTopics = new HashSet<>();//use for CoOccurance topic in document
		HashSet<TopicTermGraph> recordTopics = new HashSet<>();
		double documentTf = 0;
		int termCount =0;
		for(Entry<TopicTermGraph, TopicTermGraph> topicPair:topicMap.entrySet()){
			TopicTermGraph documentTopic = topicPair.getKey();
			TopicTermGraph mappedTopic = topicPair.getValue();
			if(this.userTopics.contains(mappedTopic)){ //mapped Topic already in user profile
				mappedTopic.merge(documentTopic);
				if(recordTopics.add(mappedTopic)){ //Prevent Add too much when multiple topic mapping into one user topic
					mappedTopic.numberOfDocument++;
				}
				mappedTopic.setUpdateDate(today);
			}else{
				loger.debug("new Topic {} into the User Profile",documentTopic);
				if(!this.userTopics.add(documentTopic)){
					throw new RuntimeException("Cant add topic");
				}
			}
			
			for(TermNode term:documentTopic.getVertices()){//Record tf of new Term & document
				this.updateTermRemoveThreshold(term.termFreq);
				documentTf += term.termFreq;
				termCount++;
			}
			
		}
		//average topic term freq instead of whole document 
		double avgDocumentTf = documentTf / topicMap.size();
		double avgTermTf = documentTf / termCount;

		this.updateTopicRemoveThreshold(avgDocumentTf);
		this.updateTermRemoveThreshold(avgTermTf);
	}

	protected void updateTopicRemoveThreshold(double newDocumentTf) {
		if(topicRemoveThreshold==0){
			this.topicRemoveThreshold = newDocumentTf;
		}
		this.topicRemoveThreshold = (newDocumentTf + this.topicRemoveThreshold)/2;

	}

	protected void updateTermRemoveThreshold(double newTermTf) {
		if(this.termRemoveThreshold == 0){
			this.termRemoveThreshold = newTermTf;
		}
		this.termRemoveThreshold = (newTermTf + this.termRemoveThreshold)/2;
	}


}
