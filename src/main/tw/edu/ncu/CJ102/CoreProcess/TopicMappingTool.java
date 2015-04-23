package tw.edu.ncu.CJ102.CoreProcess;

import java.nio.file.Path;
import java.util.*;

import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

/**
 * New 主題映射 程式
 * @author TingWen
 *
 */
public class TopicMappingTool {

	TopicMappingAlgorithm algorithm;
	double relateness_threshold;
	public TopicMappingTool(TopicMappingAlgorithm _algorithm,double threshold) {
		this.algorithm = _algorithm;
		this.relateness_threshold = threshold;
	}
	/**
	 * 將一個topic與user內的所有Topic比較相似度，若大於門檻值則
	 * @param _topic
	 * @param user
	 * @return 相對映的主題
	 */
	public TopicTermGraph map(TopicTermGraph _topic,AbstractUserProfile user){
		Collection<TopicTermGraph> userTopics = user.getUserTopics();
		TopicTermGraph mappedTopic = _topic; //
		
		int doc_topic_num = _topic.getVertexCount(); // 某一主題的字詞數量
		double similarityThreshold = 0; // 相似度門檻值
		double maximumSimilarity = 0;
		for(TopicTermGraph userTopic:userTopics){
			int userTopicSize = userTopic.getVertexCount(); // 某一模型主題的字詞數量
			double userTopicTfSum = 0; // 某一模型主題的總TF值
			
			for(TermNode term:userTopic.getVertices()){//TF computing phase
				userTopicTfSum += term.termFreq;
			}
			
			similarityThreshold = (doc_topic_num * userTopicTfSum * relateness_threshold) / (doc_topic_num * userTopicSize);//文件的字詞總和可以分母分子化簡
			double similarity = this.algorithm.computeSimilarity(_topic, userTopic);
			
			if(similarity>similarityThreshold&&similarity>maximumSimilarity){
				maximumSimilarity = similarity;
				mappedTopic = userTopic;
			}
		}
		
		return mappedTopic;
	}

}
