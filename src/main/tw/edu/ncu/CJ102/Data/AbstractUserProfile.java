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

	protected double removeRate; //興趣去除比例
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
	 * @param topic 使用者模型內的主題興趣
	 * @param date 今天的日期
	 * @return 該主題今日的遺忘因子值
	 */
	public abstract double updateDecayRate(TopicTermGraph topic,int date);

	/**
	 * 將配對好的文件主題加入至使用者主題內,並且記錄使用者主題共同出現的次數
	 * @param topicMap 文件主題(key)與使用者主題(value)的配對
	 * @param today 閱讀的日期
	 */
	public abstract void addDocument(Map<TopicTermGraph,TopicTermGraph> topicMap,int today);
	/**
	 * 取得主題移除門檻
	 * @return	主題移除門檻
	 */
	public double getTopicRemoveThreshold(){
		return topicRemoveThreshold * this.removeRate;
	}
	public double getTermRemoveThreshold(){
		return this.termRemoveThreshold * this.removeRate;
	}
	public abstract double getCoOccranceThreshold();

	public double getRemoveRate() {
		return removeRate;
	}

	public void setRemoveRate(double remove_rate) {
		this.removeRate = remove_rate;
	}

}
