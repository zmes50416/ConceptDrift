package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * use for interact with user profile,ex:update decay factor or add document into
 * @author TingWen
 *
 */
public class UserProfileManager {
//use for manipulate user profile, decoupling user profile's high dependency
	TopicMappingTool mapper;
	int currentDate;
	public UserProfileManager(TopicMappingTool _mapper) {
		this.mapper = _mapper;
		this.currentDate = 1;
	}
	/**
	 * 執行的使用者模型的一日更新
	 * 包含遺忘因子的作用與主題、字詞的去除
	 */
	public void updateUserProfile(int theDay,AbstractUserProfile user){
		HashSet<TopicTermGraph> userTopics = user.getUserTopics();
		HashMap<TopicTermGraph,Double> intersetValue = user.getInterestValueMap();
		if(userTopics.isEmpty()){
			throw new IllegalStateException("User have no topic to update!");
		}
		Iterator<TopicTermGraph> i = userTopics.iterator();//filiter remove element, so we can not use java default for each
		while(i.hasNext()){//遺忘因子流程
			TopicTermGraph topic = i.next();
			double topicInterest = intersetValue.get(topic);
			double decayFactor = user.getDecayRate(topic,theDay);
			topicInterest = topicInterest * decayFactor;
			
			if(topicInterest<user.getTopicRemoveThreshold()){//先判定興趣去除階段，如果需要移除就不用更新圖形內的字詞了
				i.remove();
			}else{
				double tempTopicInterest = 0; 
				for(TermNode term:topic.getVertices()){
					term.termFreq = term.termFreq*topicInterest;
					tempTopicInterest += term.termFreq;
				}
				if(topicInterest != tempTopicInterest){
					System.err.println("Value count differnt");
				}
				intersetValue.put(topic, tempTopicInterest);
				
			}
		}
	
		
	}
	
	public boolean removeTerm(TopicTermGraph topic,TermNode term)throws IllegalArgumentException{
		if(topic.removeVertex(term)){
			return true;
		}else{
			throw new IllegalArgumentException("term are not include in the Topic");
		}
		
	}
	public boolean removeTopic(AbstractUserProfile user,TopicTermGraph topic){
		HashSet<TopicTermGraph> topics = user.getUserTopics();
		if(topics.remove(topic)){
			return true;
		}else{
			return false;
		}
		
	}

	public double getAverageDocumentTermFreq(AbstractUserProfile user){
		//TODO implement
		return 0;
	}
	
	/**
	 * 
	 * @param user 使用者模型
	 * @param doc_term 文件內容資訊
	 */
	public void addTopic(Collection<TopicTermGraph> documentTopics, AbstractUserProfile user){
		for(TopicTermGraph topic:documentTopics){
			TopicTermGraph mappedTopic = this.mapper.map(topic, user);
			if(mappedTopic==topic){
				user.getUserTopics().add(topic);// Add new user topic if mapper can't find the better topic
			}else{//find the right topic and merge all term and edge into it
				
				
			}
		}
		
	}
	

}
