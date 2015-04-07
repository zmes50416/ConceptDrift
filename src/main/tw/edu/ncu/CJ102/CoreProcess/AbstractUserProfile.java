package tw.edu.ncu.CJ102.CoreProcess;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("serial")
public abstract class AbstractUserProfile implements Serializable{
	/**
	 * 使用者的辨識方法
	 */
	String id;

	double remove_rate; //興趣去除比例
	double interest_remove_rate; //主題去除的累計平均單文件總TF值比例
	double term_remove_rate; //字詞去除的累計平均單字詞TF值比例
	
	protected HashSet<TopicTermGraph> userTopics = new HashSet<>();
	protected HashMap<TopicTermGraph,Double> interestValueMap = new HashMap<>();
	/**
	 * 主題共現圖形
	 */
	protected TopicCoOccuranceGraph topicCOGraph;
	

	/**
	 * @return the userTopics
	 */
	public HashSet<TopicTermGraph> getUserTopics() {
		return userTopics;
	}
	
	public HashMap<TopicTermGraph,Double> getInterestValueMap() {
		return interestValueMap;
	}
	
	/**
	 * @return the topicCOGraph
	 */
	public TopicCoOccuranceGraph getTopicCOGraph() {
		return topicCOGraph;
	}

	/**
	 * 實作使用者模型的遺忘公式
	 * @param topic 使用者的興趣主題
	 * @param length 經過的時間長度
	 * @return 該主題的遺忘因子
	 */
	public abstract double getDecayRate(TopicTermGraph topic,int length);

	public abstract double getTopicRemoveThreshold();
	public abstract double getTermRemoveThreshold();

}
