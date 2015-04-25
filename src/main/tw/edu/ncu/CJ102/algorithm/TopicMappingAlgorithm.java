package tw.edu.ncu.CJ102.algorithm;
import tw.edu.ncu.CJ102.CoreProcess.*;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
public interface TopicMappingAlgorithm {
	
	public double computeSimilarity(TopicTermGraph theTopic,TopicTermGraph userTopic);

}
