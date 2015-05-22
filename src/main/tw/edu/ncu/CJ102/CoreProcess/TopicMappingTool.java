package tw.edu.ncu.CJ102.CoreProcess;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.TermNode;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.TopicMappingAlgorithm;

/**
 * New 主題映射 程式
 * @author TingWen
 *
 */
public class TopicMappingTool {

	TopicMappingAlgorithm algorithm;
	double relateness_threshold;
	CompletionService<Double> tasker;
	
	public TopicMappingTool(TopicMappingAlgorithm _algorithm,double threshold) {
		this.algorithm = _algorithm;
		this.relateness_threshold = threshold;
		tasker = new ExecutorCompletionService<Double>(Executors.newFixedThreadPool(10));
	}
	/**
	 * 將一個topic與user內的所有Topic比較相似度，若大於門檻值則將相似度最高者放入配對，若小於則將自己放入配對(代表當作新的使用者興趣)
	 * @param _topic
	 * @param user
	 * @return 相對映的主題
	 */
	public TopicTermGraph map(TopicTermGraph _topic,AbstractUserProfile user){
		Collection<TopicTermGraph> userTopics = user.getUserTopics();
		TopicTermGraph mappedTopic = _topic; //
		
		int doc_topic_num = _topic.getVertexCount(); // 某一主題的字詞數量
		double similarityThreshold = 0; // 相似度門檻值
		double maximumSimilarity = 0; //目前最大相似者
		int taskCount = 0;
		HashMap<Future<Double>,TopicTermGraph> taskMap = new HashMap<>();
		for(TopicTermGraph topic:userTopics){
			taskMap.put(tasker.submit(new mapTask(topic,mappedTopic,this.algorithm)), topic);
			taskCount ++;
		}
		
		for(int i = 0;i<taskCount;i++){
			try {
				Future<Double> completedTask = tasker.take();
				TopicTermGraph userTopic = taskMap.remove(completedTask);
				int userTopicSize = userTopic.getVertexCount(); // 某一模型主題的字詞數量
				double userTopicTfSum = 0; // 某一模型主題的總TF值

				for (TermNode term : userTopic.getCoreTerm()) {// TF computing
					for (TermNode docTerm : mappedTopic.getCoreTerm()) {
						userTopicTfSum += term.termFreq * docTerm.termFreq / 2;
					}
				}

				similarityThreshold = (userTopicTfSum * relateness_threshold)
						/ (userTopic.getCoreTerm().size() * mappedTopic.getCoreTerm().size());
				// 文件的字詞總和可以分母分子化簡

				double similarity = completedTask.get();

				if (similarity > similarityThreshold
						&& similarity > maximumSimilarity) {
					maximumSimilarity = similarity;
					mappedTopic = userTopic;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}//end of all task for loop 
		if(!taskMap.isEmpty()){
			throw new RuntimeException("Should have finised all task");
		}
		LoggerFactory.getLogger("processDetail").info("topic {} map to {}",_topic,mappedTopic);
		return mappedTopic;
	}

}
class mapTask implements Callable<Double>{
	private TopicTermGraph theTopic;
	private TopicTermGraph userTopic;
	TopicMappingAlgorithm algorithm;

	public mapTask(TopicTermGraph userTopic, TopicTermGraph theTopic,TopicMappingAlgorithm algo){
		this.userTopic = userTopic;
		this.theTopic = theTopic;
		this.algorithm = algo;
	}
	@Override
	public Double call() throws Exception {
		
		return this.algorithm.computeSimilarity(theTopic, userTopic);
	}
	
}
