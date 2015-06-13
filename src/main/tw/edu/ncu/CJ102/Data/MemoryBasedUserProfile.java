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
	public static int longTermThreshold = 50; //長期興趣門檻值
	private Logger loger = LoggerFactory.getLogger(this.getClass());
	public MemoryBasedUserProfile() {
		this.setRemoveRate(0.1);
	}
	public MemoryBasedUserProfile(double _removeRate){
		this.setRemoveRate(_removeRate);
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
	public double updateDecayRate(TopicTermGraph topic,int today) {

		double decayRate;
		int timeFactor = today-topic.getUpdateDate();
		if(topic.isLongTermInterest()){
			decayRate = Math.pow(Math.E, -timeFactor*0.02);
		}else{
			double strength = Math.log10(topic.numberOfDocument)+2;
			decayRate = Math.pow(Math.E, -(timeFactor*this.getSizeOfShortTerm()/this.getUserTopics().size())/strength);
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
			TopicTermGraph topic = topicPair.getKey();
			TopicTermGraph mappedTopic = topicPair.getValue();
			if(this.userTopics.contains(mappedTopic)){
				mappedTopic.merge(topic);
				if(recordTopics.add(mappedTopic)){ //Prevent Add too much when multiple topic mapping into one user topic
					mappedTopic.numberOfDocument++;
				}
				mappedTopic.setUpdateDate(today);
			}else{
				loger.info("new Topic {} into the User Profile",topic);
				if(!this.userTopics.add(topic)){
					throw new RuntimeException("Cant add topic");
				}
			}
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
