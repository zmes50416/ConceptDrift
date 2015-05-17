package tw.edu.ncu.CJ102.algorithm;

import tw.edu.ncu.CJ102.Data.TopicTermGraph;
/**
 * Compute two topic's similarity
 * @author TingWen
 *
 */
public interface TopicMappingAlgorithm {
	public double computeSimilarity(TopicTermGraph theTopic,TopicTermGraph userTopic);

}
