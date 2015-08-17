package tw.edu.ncu.CJ102.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class BaseLineUserProfile extends AbstractUserProfile {
	//長短期沒差的遺忘因子使用者模型，當作比較的基準點
	public BaseLineUserProfile(double removeRate) {
		this.removeRate = removeRate;
	}
	/**
	 * All update everyday
	 */
	@Override
	public double updateDecayRate(TopicTermGraph topic, int date) {
		return 0.98;
	}

	@Override
	public void addDocument(Map<TopicTermGraph, TopicTermGraph> topicMap,
			int today) {
		if(topicMap.isEmpty()){//document should have something to add
			throw new IllegalArgumentException("document topic should not be empty!");
		}
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
				loger.debug("new Topic {} into the User Profile",topic);
				if(!this.userTopics.add(topic)){
					throw new RuntimeException("Cant add topic");
				}
			}
			double sumInterest = 0;
			if(!mappedTopic.isLongTermInterest()){
				for(TermNode term:mappedTopic.getVertices()){
					sumInterest += term.termFreq;
					if(sumInterest>=this.getLongTermThreshold()){
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
	@Override
	public double getLongTermThreshold() {
		this.longTermThreshold = 1000;
	}


}
