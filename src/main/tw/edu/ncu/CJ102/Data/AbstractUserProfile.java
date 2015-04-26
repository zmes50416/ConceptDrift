package tw.edu.ncu.CJ102.Data;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class AbstractUserProfile implements Serializable{
	/**
	 * 使用者的辨識方法,目前無實作
	 */
	String id;

	private double remove_rate; //興趣去除比例
	double topicRemoveThreshold;
	double termRemoveThreshold;
	protected Collection<TopicTermGraph> userTopics = new ArrayList<>();
	/**
	 * 主題共現圖形
	 */
	protected TopicCoOccuranceGraph topicCOGraph = new TopicCoOccuranceGraph();
	

	/**
	 * @return the userTopics
	 */
	public Collection<TopicTermGraph> getUserTopics() {
		return userTopics;
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

	/**
	 * 將配對好的文件主題加入至使用者主題內,並且記錄使用者主題共同出現的次數
	 * @param topicMap 文件主題(key)與使用者主題(value)的配對
	 */
	public abstract void addDocument(Map<TopicTermGraph,TopicTermGraph> topicMap);
	/**
	 * 取得主題移除門檻
	 * @return	主題移除門檻
	 */
	public double getTopicRemoveThreshold(){
		return topicRemoveThreshold;
	}
	public double getTermRemoveThreshold(){
		return this.termRemoveThreshold;
	}
	public abstract double getCoOccranceThreshold();

	public double getRemove_rate() {
		return remove_rate;
	}

	public void setRemove_rate(double remove_rate) {
		this.remove_rate = remove_rate;
	}

}
