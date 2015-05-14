package tw.edu.ncu.CJ102.CoreProcess;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.CEdge;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicCoOccuranceGraph;
import tw.edu.ncu.CJ102.Data.TopicNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * use for interact with user profile,ex:update decay factor or add document into
 * @author TingWen
 *
 */
public class UserProfileManager {
//use for manipulate user profile, decoupling user profile's high dependency
	TopicMappingTool mapper;
	Logger loger = LoggerFactory.getLogger(UserProfileManager.class);
	
	public UserProfileManager(TopicMappingTool _mapper) {
		if(_mapper == null){
			throw new NullPointerException("Cant work without a mapper algorithm");
		}
		this.mapper = _mapper;
	}
	/**
	 * 執行的使用者模型的一日更新
	 * 包含遺忘因子的作用與主題、字詞的去除
	 */
	public void updateUserProfile(int theDay, AbstractUserProfile user) {
		Collection<TopicTermGraph> userTopics = user.getUserTopics();
		
		if (userTopics.isEmpty()) {
			if(loger.isInfoEnabled()){
				loger.info("Day{} , System have no topic to update",theDay);
			}
			return;
		}
		
		Iterator<TopicTermGraph> i = userTopics.iterator();// filiter remove element,do not use java default for each
		while (i.hasNext()) {// 遺忘因子流程
			TopicTermGraph topic = i.next();
			double decayFactor = user.getDecayRate(topic, theDay);
			double topicInterest = 0;
			
			for (TermNode term : topic.getVertices()) {
				term.termFreq = term.termFreq * decayFactor;
				topicInterest += term.termFreq;
			}

			if (topicInterest < user.getTopicRemoveThreshold()) {// 先判定興趣去除階段，如果需要移除就不用更新圖形內的字詞了
				loger.info("System remove a topic:{}, Interest value = {}",topic.toString(),topicInterest);
				i.remove();
			}
			
		}
		
		//TODO implement TopicCoOccurance Forgetting
		TopicCoOccuranceGraph topicCoGraph = user.getTopicCOGraph();
		for (Iterator<CEdge> iterator = topicCoGraph.getEdges().iterator(); iterator
				.hasNext();) {
			CEdge<TopicNode> edge = iterator.next();
			edge.setCoScore(edge.getCoScore()*0.9);
			
			if(edge.getCoScore() < user.getCoOccranceThreshold()){
				System.out.println("TopicCoOccurance CEdge removed:"+edge.toString());
				iterator.remove();
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
		Collection<TopicTermGraph> topics = user.getUserTopics();
		if(topics.remove(topic)){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 將每一個文件進行主題映射找出最相似的使用者主題
	 * @param user 使用者模型
	 * @param doc_term 文件內容資訊
	 * @return 文件主題(Key)與該使用者主題(Value)的配對
	 */
	public Map<TopicTermGraph,TopicTermGraph> mapTopics(Collection<TopicTermGraph> documentTopics, AbstractUserProfile user){
		HashMap<TopicTermGraph,TopicTermGraph> mappedTopics = new HashMap<>();
		for(TopicTermGraph topic:documentTopics){
			TopicTermGraph mappedTopic = this.mapper.map(topic, user);
			mappedTopics.put(topic, mappedTopic);
		}//end of for
		return mappedTopics;
		
	}
	

}
