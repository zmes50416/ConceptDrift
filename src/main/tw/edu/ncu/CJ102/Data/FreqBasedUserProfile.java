package tw.edu.ncu.CJ102.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
/**
 * 透過同樣架構重現吳登翔學長的使用者模型，但目前學長的遺忘因子是綁定在字詞上面，這個作法弄錯了
 * @author TingWen
 *
 */
@Deprecated
public class FreqBasedUserProfile extends AbstractUserProfile {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<TopicTermGraph,Boolean> lastUpdateTopics = new HashMap<>();
	private HashMap<TopicTermGraph,Double> forgetFactorMap = new HashMap<>();
	public FreqBasedUserProfile() {
		// TODO Auto-generated constructor stub
	}
	public FreqBasedUserProfile(double _removeRate) {
		this.setRemoveRate(_removeRate);
	}

	@Override
	public double updateDecayRate(TopicTermGraph topic, int date) {
		Boolean isReadDocuments = this.lastUpdateTopics.get(topic);
		if(isReadDocuments == null){
			isReadDocuments = false;
		}
		double forgetFactor = this.forgetFactorMap.get(topic);
		if (isReadDocuments) {
			forgetFactor = forgetFactor - 0.079;
		} else {
			forgetFactor = forgetFactor + 0.079;
		}
		if (forgetFactor > 0.98) {
			forgetFactor = 0.98;
		} else if (forgetFactor < 0.45) {
			forgetFactor = 0.45;
		}
		this.forgetFactorMap.put(topic, forgetFactor);
		this.lastUpdateTopics.put(topic,false);
		return forgetFactor;

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
				this.lastUpdateTopics.put(mappedTopic, true);
				mappedTopic.setUpdateDate(today);
			}else{
				loger.debug("new Topic {} into the User Profile",topic);
				this.forgetFactorMap.put(topic, 0.921);
				if(!this.userTopics.add(topic)){
					throw new RuntimeException("Cant add topic");
				}
			}
			if(!mappedTopic.isLongTermInterest()&&today - mappedTopic.getBirthDate() >= 7){
				loger.info("Topic {} become long term Interest",mappedTopic);
				mappedTopic.setLongTermInterest(true);
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
