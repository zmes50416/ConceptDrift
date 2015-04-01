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

/**
 *  
 * @author TingWen
 *
 */
public class UserProfileManager {
//use for manipulate user profile, decoupling user profile's high dependency
	TopicMappingTool mapper;
	public UserProfileManager(TopicMappingTool _mapper) {
		this.mapper = _mapper;
	}
	/**
	 * 執行的使用者模型的一日更新
	 * 包含遺忘因子的作用與主題、字詞的去除
	 */
	public void updateUserProfile(AbstractUserProfile user){
		HashSet<TopicTermGraph> userTopics = user.getUserTopics();
		HashMap<TopicTermGraph,Double> forgetMap = user.getForgettingFactorMap();
		if(userTopics.isEmpty()){
			throw new IllegalStateException("User have no topic to update!");
		}
		for(TopicTermGraph topic:userTopics){
			double forgetFactor = forgetMap.get(topic);
			//TODO update in progress
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
	public void addTopic(Collection<TopicTermGraph> docTerms, AbstractUserProfile user){
		
	}
	

}
