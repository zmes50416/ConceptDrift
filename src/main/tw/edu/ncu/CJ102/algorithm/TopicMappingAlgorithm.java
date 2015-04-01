package tw.edu.ncu.CJ102.algorithm;
import tw.edu.ncu.CJ102.CoreProcess.*;
public interface TopicMappingAlgorithm {
	
	public double computeSimilarity(TopicTermGraph theTopic,TopicTermGraph userTopic);

}
